
var SUCCESS = 'success';
var ERROR = 'error';

var serverErrorMessage = 'Oops, something wrong :(';

$(document).ready(function() {
    $('#queryDataTable').DataTable( {

        //用完即摧毁
        destroy: true,

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
            { "data": "id" }
        ]
    } );






$('#queryModal').on('shown.bs.modal', function () {
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
      $('#queryForm').on('submit', function(e){
         var formData = $("#queryForm").serializeObject();
          //dataTable返回jq对象，DataTable是新对象，包含一系列api。可以不用这个，而用dataTable.api()访问。
          e.preventDefault();
           $.ajax({
                url: "/query",
                type: "POST",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                data: formData,
                success:function(response){
                   if(response.status == "success") {
                       $('#queryModal').modal('hide');
                       $('#queryDataTable').DataTable( {

                           //用完即摧毁
                           destroy: true,

                           //定义某些列可以排序
                           "columnDefs": [
                               { "sortable": false, "targets": [1,2,3,4,5] },
                               { "visible" : false, "targets": [6]},
                               {"className": "dt-center", "targets": "_all"}  //获取所有目标
                           ],

                           "order": [[ 6, 'asc' ]],

                           //直接利用返回结果
                           data:response.data.detail,


                           "columns": [
                               { "data": "date" },
                               { "data": "user"},
                               { "data": "io" },
                               { "data": "amount" },
                               { "data": "balance" },
                               { "data": "reason" },
                               { "data": "whetherLatest"},
                               { "data": "id" }
                           ]
                       } );




                         // //添加一行新的数据，用dataTable对象操作
                         // var newDet = jQuery.parseJSON(formData);
                         // newDet['id'] = response.data['id'];
                         // newDet['balance'] = response.data['balance']
                         // newDet['whetherLatest'] = 1
                         // queryTable.fnAddData([newDet]);
                         //
                         //
                         // //更新整个表
                         // $('#accountDataTable').DataTable().ajax.reload();
                         //
                         //
                         //
                         // showSuccessAlert(response.msg);

                   } else {
                        $('#queryModal').modal('hide');
                        showErrorAlert(response.msg);
                   }
                },
                error: function(){
                    $('#queryModal').modal('hide');
                    showErrorAlert(serverErrorMessage);
                }

            });
            return false;
      });



});

