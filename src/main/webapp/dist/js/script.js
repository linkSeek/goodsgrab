$(function () {
    //隐藏错误提示框
    $('.add-error-info').css("display", "none");
    $('.edit-error-info').css("display", "none");

    $("#jqGrid").jqGrid({
        url: 'script/list',
        datatype: "json",
        colModel: [
            {label: '脚本ID', name: 'scriptId', index: 'scriptId', width: 50, key: true},
            {label: '脚本描述', name: 'comment', index: 'comment', width: 50},
            {label: '时间间隔（单位：秒）', name: 'interval', index: 'interval', sortable: false, width: 80},
            {label: '脚本状态（1：开启，2：关闭）', name: 'status', index: 'status', sortable: false, width: 80}
        ],
        height: 485,
        rowNum: 10,
        rowList: [10, 30, 50],
        styleUI: 'Bootstrap',
        loadtext: '信息读取中...',
        rownumbers: false,
        rownumWidth: 35,
        autowidth: true,
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader: {
            root: "data.list",
            page: "data.currPage",
            total: "data.totalPage",
            records: "data.totalCount"
        },
        prmNames: {
            page: "page",
            rows: "limit",
            order: "order"
        },
        gridComplete: function () {
            //隐藏grid底部滚动条
            $("#jqGrid").closest(".ui-jqgrid-bdiv").css({"overflow-x": "hidden"});
        }
    });

    //importV1
    new AjaxUpload('#importV1Button', {
        action: 'goods/importV1',
        name: 'file',
        autoSubmit: true,
        responseType: "json",
        onSubmit: function (file, extension) {
            if (!(extension && /^(xlsx)$/.test(extension.toLowerCase()))) {
                swal('只支持xlsx格式的文件！', {
                    icon: "error",
                });
                return false;
            }
        },
        onComplete: function (file, r) {
            if (r.resultCode == 200) {
                swal("成功导入" + r.data + "条记录！", {
                    icon: "success",
                });
                reload();
                return false;
            } else {
                swal(r.message, {
                    icon: "error",
                });
            }
        }
    });

    //importV2
    new AjaxUpload('#uploadExcelV2', {
        action: 'upload/file',
        name: 'file',
        autoSubmit: true,
        responseType: "json",
        onSubmit: function (file, extension) {
            if (!(extension && /^(xlsx)$/.test(extension.toLowerCase()))) {
                swal('只支持xlsx格式的文件！', {
                    icon: "error",
                });
                return false;
            }
        },
        onComplete: function (file, r) {
            if (r.resultCode == 200) {
                console.log(r);
                $("#fileUrl").val(r.data);
                return false;
            } else {
                swal(r.message, {
                    icon: "error",
                });
            }
        }
    });
});

function showPriceDiffer() {
    $.ajax({
        type: 'GET',//方法类型
        dataType: "json",//预期服务器返回的数据类型
        url: 'goods/priceDiffer',//url
        contentType: "application/json; charset=utf-8",
        // data: JSON.stringify(data),
        beforeSend: function (request) {
            //设置header值
            request.setRequestHeader("token", getCookie("token"));
        },
        success: function (result) {
            console.log(result);//打印服务端返回的数据
            checkResultCode(result.resultCode);
            if (result.resultCode == 200) {
                $("#priceDiffer").val(result.data.price_differ);
            } else {
                $("#priceDiffer").val("获取价差失败，请联系管理员。。");
            }
            ;
        },
        error: function () {
            $("#priceDiffer").val("获取价差失败，请联系管理员。。");
        }
    });
    //点击添加按钮后执行操作
    var modal = new Custombox.modal({
        content: {
            effect: 'fadein',
            target: '#changePriceDiffer'
        }
    });
    modal.open();
}

function scriptEdit() {
    var id = getSelectedRow();
    if (id == null) {
        return;
    }

    $('#scriptId').val(id);

    //点击编辑按钮后执行操作
    var modal = new Custombox.modal({
        content: {
            effect: 'fadein',
            target: '#modalEdit'
        }
    });
    modal.open();
}

//绑定modal上的保存按钮
$('#saveButton').click(function () {
    //ajax
    var newPriceDiffer = $("#priceDiffer").val();
    var data = {"newPriceDiffer": newPriceDiffer};
    $.ajax({
        type: 'POST',//方法类型
        dataType: "json",//预期服务器返回的数据类型
        url: 'goods/updatePriceDiffer',//url
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(data),
        beforeSend: function (request) {
            //设置header值
            request.setRequestHeader("token", getCookie("token"));
        },
        success: function (result) {
            console.log(result);//打印服务端返回的数据
            checkResultCode(result.resultCode);
            if (result.resultCode == 200) {
                closeModal();
                swal("保存成功", {
                    icon: "success",
                });
                //reload
                reload();
            } else {
                closeModal();
                swal("保存成功", {
                    icon: "error",
                });
            }
            ;
        },
        error: function () {
            reset();
            swal("操作失败", {
                icon: "error",
            });
        }
    });
});

//绑定modal上的编辑按钮
$('#editButton').click(function () {
    //验证数据
    if (validObjectForEdit()) {
        //一切正常后发送网络请求
        var scriptId = $("#scriptId").val();
        var interval = $("#intervalEdit").val();
        var status = $("#statusEdit").val();
        var data = {"scriptId": scriptId, "interval": interval, "status": status};
        $.ajax({
            type: 'POST',//方法类型
            dataType: "json",//预期服务器返回的数据类型
            url: 'script/update',//url
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(data),
            beforeSend: function (request) {
                //设置header值
                request.setRequestHeader("token", getCookie("token"));
            },
            success: function (result) {
                checkResultCode(result.resultCode);
                console.log(result);//打印服务端返回的数据
                if (result.resultCode == 200) {
                    closeModal();
                    swal("修改成功", {
                        icon: "success",
                    });
                    //reload
                    reload();
                } else {
                    closeModal();
                    swal(result.message, {
                        icon: "error",
                    });
                }
                ;
            },
            error: function () {
                reset();
                swal(result.message, {
                    icon: "error",
                });
            }
        });

    }
});

//绑定modal上的编辑按钮
$('#importV2Button').click(function () {
    var fileUrl = $("#fileUrl").val();
    $.ajax({
        type: 'POST',
        dataType: "json",
        url: 'goods/importV2?fileUrl=' + fileUrl,
        contentType: "application/json; charset=utf-8",
        success: function (result) {
            checkResultCode(result.resultCode);
            console.log(result);
            if (result.resultCode == 200) {
                closeModal();
                reload();
                swal("成功导入" + result.data + "条记录！", {
                    icon: "success",
                });
            } else {
                closeModal();
                swal(result.message, {
                    icon: "error",
                });
            }
            ;
        },
        error: function () {
            reset();
            swal("操作失败", {
                icon: "error",
            });
        }
    });
});

/**
 * 用户导入功能V2
 */
function importV2() {
    //点击编辑按钮后执行操作
    var modal = new Custombox.modal({
        content: {
            effect: 'fadein',
            target: '#importV2Modal'
        }
    });
    modal.open();
}


//添加Modal关闭
$('#cancelAdd').click(function () {
    closeModal();
})

//编辑Modal关闭
$('#cancelEdit').click(function () {
    closeModal();
})

//导入Modal关闭
$('#cancelImportV2').click(function () {
    closeModal();
})

/**
 * 数据验证
 */
function validObjectForAdd() {
    var userName = $('#userName').val();
    if (isNull(userName)) {
        showErrorInfo("用户名不能为空!");
        return false;
    }
    if (!validUserName(userName)) {
        showErrorInfo("请输入符合规范的用户名!");
        return false;
    }
    var password = $('#password').val();
    if (isNull(password)) {
        showErrorInfo("密码不能为空!");
        return false;
    }
    if (!validPassword(password)) {
        showErrorInfo("请输入符合规范的密码!");
        return false;
    }
    return true;
}

/**
 * 数据验证
 */
function validObjectForEdit() {
    var scriptId = $("#scriptId").val();
    var interval = $("#intervalEdit").val();
    var editStatus = $("#statusEdit").val();
    console.log(editStatus);
    if (isNull(scriptId) || scriptId < 1) {
        showErrorInfo("数据错误！");
        return false;
    }
    if (isNull(interval)) {
        showErrorInfo("间隔时间不能为空!");
        return false;
    }

    if (isNull(editStatus)) {
        showErrorInfo("脚本状态不能为空!");
        return false;
    }
    if (editStatus != 1 && editStatus != 0) {
        showErrorInfo("请规范脚本状态设置!");
        return false;
    }
    return true;
}

/**
 * 关闭modal
 */
function closeModal() {
    //关闭前清空输入框数据
    reset();
    Custombox.modal.closeAll();
}

/**
 * 重置
 */
function reset() {
    //隐藏错误提示框
    $('.add-error-info').css("display", "none");
    $('.edit-error-info').css("display", "none");
    //清空数据
    $('#priceDiffer').val('');
    $('#intervalEdit').val('');
    $('#statusEdit').val('');
}

/**
 * jqGrid重新加载
 */
function reload() {
    reset();
    var page = $("#jqGrid").jqGrid('getGridParam', 'page');
    $("#jqGrid").jqGrid('setGridParam', {
        page: page
    }).trigger("reloadGrid");
}