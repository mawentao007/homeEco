
var SUCCESS = 'success';
var ERROR = 'error';

var serverErrorMessage = 'Oops, something wrong :(';

$(document).ready(function() {
    $('#accountDataTable').DataTable( {

    //定义某些列可以排序
        "columnDefs": [
              { "sortable": false, "targets": [1,2,3,4,5] },
              { "visible" : false, "targets": [6]},
              {"className": "dt-center", "targets": "_all"}  //获取所有目标
           ],

        "order": [[ 6, 'asc' ]],

        "ajax": {
            "url": "/detail/list",
            "dataType": "json"
        },
         "columns": [
                    { "data": "date" },
                    { "data": "user"},
                    { "data": "io" },
                    { "data": "amount" },
                    { "data": "balance" },
                    { "data": "reason" },
                    { "data": "whetherLatest"},
                    //TODO 编辑功能暂时不开通
//                    {  data: "id" ,
//                     "render": function ( data,type,full) {             //通过full可以访问所有字段,注意参数必须是三个固定
//                                  if(full.whetherLatest == 1){
//                                    return '<i id=" ' + data +' " class="edit-button glyphicon glyphicon-edit cursorPointer" ></i>';
//                                  }else{
//                                    return '<i id=" ' + data +' " class="" ></i>'
//                                  }
//                                }
//                            },
                     { data: "id" ,
                        "render": function ( data,type,full ) {
                                    if(full.whetherLatest == 1){
                                        return '<i id=" ' + data +' " class="remove-button glyphicon glyphicon-trash cursorPointer"> ' + data + '</i>';
                                    }else{
                                        return '<i id= ' + data + ' >' +  data + '</i>'
                                    }

                        }
                     }

                ]


    } );

    var tableAccount = $('#accountDataTable').DataTable();

    // 删除条目
    $("body").on( 'click', '.remove-button', function () {
        var currentRow = $(this);
        var detailId = $(this).attr('id').trim();
         bootbox.confirm("确定删除该条目?", function(result) {
            if(result) {
                    $.ajax({
                     url: "/detail/delete",
                     type: "GET",
                     data: {detId: detailId},
                     success:function(response){
                               if(response.status == SUCCESS) {
                                  showSuccessAlert(response.msg);
                                  tableAccount.row(currentRow.parents('tr') ).remove().draw();
                              } else {
                                  showErrorAlert(serverErrorMessage);
                              }
                        },
                     error: function(){
                          showErrorAlert(serverErrorMessage);
                       }
                  });
            } else {
               //
              }
         });
    });
     

     // 编辑条目
     $("body").on( 'click', '.edit-button', function () {
            var detailId = $(this).attr('id').trim();
             $.ajax({
                   url: "/detail/edit",
                   type: "GET",
                   data: {detId: detailId},
                   success:function(response){
                             $('#detailEditModal').modal('show');
                             $.each(response.data, function(key, value){
                                $('#detailEditForm input[name="'+key+'"]').val(value);
                             });
                      },
                   error: function(){
                             showErrorAlert(serverErrorMessage);
                     }
                });
          });


$('#detailModal').on('shown.bs.modal', function () {
  $('#accountForm').trigger("reset");
});

// Show success alert message
var showSuccessAlert = function (message) {
   	$.toaster({ priority : 'success', title : 'Success', message : message});
}

// Show error alert message
var showErrorAlert = function (message) {
    $.toaster({ priority : 'danger', title : 'Error', message : message});
}

// Convert form data in JSON format
$.fn.serializeObject = function() {
           var o = {};
           var a = this.serializeArray();
           $.each(a, function() {
                    if (o[this.name] !== undefined) {
                        if (!o[this.name].push) {
                            o[this.name] = [o[this.name]];
                        }
                        o[this.name].push(this.value || '');
                    } else {
                          if(this.name == 'id'| this.name == 'whetherLatest'){
                                o[this.name] = parseInt(this.value);
                          } else if (this.name == 'amount') {
                                o[this.name] = parseFloat(this.value);
                          } else {
                             o[this.name] = this.value || '';
                          }
                    }
               });
            return JSON.stringify(o);
        };

// 提交新条目的处理请求
      $('#detailForm').on('submit', function(e){
         var formData = $("#detailForm").serializeObject();
         var detailTable = $('#accountDataTable').dataTable();
          //dataTable返回jq对象，DataTable是新对象，包含一系列api。可以不用这个，而用dataTable.api()访问。
          e.preventDefault();
           $.ajax({
                url: "/detail/create",
                type: "POST",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                data: formData,
                success:function(response){
                   if(response.status == "success") {
                         $('#detailModal').modal('hide');

                         //添加一行新的数据，用dataTable对象操作
                         var newDet = jQuery.parseJSON(formData);
                         newDet['id'] = response.data['id'];
                         newDet['balance'] = response.data['balance']
                         newDet['whetherLatest'] = 1
                         detailTable.fnAddData([newDet]);

                         //通过cell访问单个节点值
                         //alert(detailTable.api().cell(1,1).data());
                         //更新某个节点值
                         //detailTable.fnUpdate(0,detailTable.api().data().count()-2,7);


                         //更新整个表
                         $('#accountDataTable').DataTable().ajax.reload();
                         //清空列表
                       // detailTable.fnClearTable();


                         showSuccessAlert(response.msg);
                         //重定向链接
                       // location.href = "http://127.0.0.1:9000"
                   } else {
                        $('#detailModal').modal('hide');
                        showErrorAlert(response.msg);
                   }
                },
                error: function(){
                    $('#detailModal').modal('hide');
                    showErrorAlert(serverErrorMessage);
                }

            });
            return false;
      });

// 处理条目更新请求
$('#detailEditForm').on('submit', function(e){
               var formData = $("#detailEditForm").serializeObject();
                e.preventDefault();
                 $.ajax({
                      url: "/detail/update",
                      type: "POST",
                      contentType: "application/json; charset=utf-8",
                      dataType: "json",
                      data: formData,
                      success:function(response){
                         if(response.status == SUCCESS) {
                               $('#detailEditModal').modal('hide');
                               $('#accountDataTable').DataTable().ajax.reload();
                               showSuccessAlert(response.msg)
                         } else {
                            $('#detailEditModal').modal('hide');
                            showErrorAlert(response.msg);
                         }
                      },
                      error: function(){
                          $('#detailEditModal').modal('hide');
                          showErrorAlert(serverErrorMessage);
                      }

                  });
                  return false;
            });

});

