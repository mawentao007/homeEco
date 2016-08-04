var SUCCESS = 'success';
var ERROR = 'error';

var serverErrorMessage = 'Oops, something wrong :(';

$(document).ready(function () {

    $('#query_button').click(function () {
        $('#insert_button').addClass("modal disabled");
    })

    $('#queryModal').on('shown.bs.modal', function () {
        $('#accountForm').trigger("reset");
    });

// Show success alert message
    var showSuccessAlert = function (message) {
        $.toaster({priority: 'success', title: 'Success', message: message});
    }

// Show error alert message


    var showErrorAlert = function (message) {
        $.toaster({priority: 'danger', title: 'Error', message: message});
    }

// Convert form data in JSON format
    $.fn.serializeObject = function () {
        var o = {};
        var a = this.serializeArray();
        $.each(a, function () {
            if (o[this.name] !== undefined) {
                if (!o[this.name].push) {
                    o[this.name] = [o[this.name]];
                }
                o[this.name].push(this.value || '');
            } else {
                if (this.name == 'id' | this.name == 'whetherLatest') {
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
    $('#queryForm').on('submit', function (e) {
        var formData = $("#queryForm").serializeObject();
        //dataTable返回jq对象，DataTable是新对象，包含一系列api。可以不用这个，而用dataTable.api()访问。
        e.preventDefault();
        $.ajax({
            url: "/query",
            type: "POST",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: formData,
            success: function (response) {
                if (response.status == "success") {
                    $('#queryModal').modal('hide');
                    $('#accountDataTable').DataTable({

                        //用完即摧毁
                        destroy: true,

                        //定义某些列可以排序
                        "columnDefs": [
                            {"sortable": false, "targets": [0, 1, 2, 3, 4, 5, 6 ,8]},
                            {"visible": false, "targets": [7]},
                            {"className": "dt-center", "targets": "_all"}  //获取所有目标
                        ],

                        "order": [[8, 'asc']],

                        //直接利用返回结果
                        data: response.data.detail,


                        "columns": [
                            {"data": "date"},
                            {"data": "user"},
                            {"data": "io"},
                            {"data": "kind"},
                            {"data": "amount"},
                            {"data": "balance"},
                            {"data": "reason"},
                            {"data": "whetherLatest"},
                            {"data": "id"}
                        ]
                    });

                    showPieChart('#income_container',response.data.incomeJson,"收入");
                    showPieChart('#expense_container',response.data.expenseJson,"支出");
                    showPieChart('#kind_container',response.data.kindJson,"分类支出");
                    showColumnChart('#range_container',response.data.xAxisJson,response.data.yDataJson,"趋势图")
                } else {
                    $('#queryModal').modal('hide');
                    showErrorAlert(response.msg);
                }
            },
            error: function () {
                $('#queryModal').modal('hide');
                showErrorAlert(serverErrorMessage);
            }

        });
        return false;
    });


    var showPieChart = function (divId,chartData,title) {
        $(divId).highcharts({
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false,
                type: 'pie'
            },
            title: {
                text: title
            },
            // tooltip: {
            //     pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            // },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        format: '<b>{point.name}</b>: {y} ({point.percentage:.1f}%)',
                        style: {
                            color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                        }
                    }
                }
            },
            series: [{
                name: 'Brands',
                colorByPoint: true,
                data: chartData
            }]
        });
    };

    var showColumnChart = function (divId,xJson,yJson,title) {
        $(divId).highcharts({
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false,
                type: 'column'
            },
            title: {
                text: title
            },

            plotOptions: {
                series: {
                    allowPointSelect: true
                }
            },

            xAxis: {
                categories: xJson
            },

            series: [{
                name: '金额',
                colorByPoint: true,
                data: yJson
            }]
        });
    };
});

