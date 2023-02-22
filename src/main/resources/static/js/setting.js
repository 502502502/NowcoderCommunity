$(function (){
    //拦截提交事件
    $("#upBtn").click(update)
});
function update(){
    //获取文件
    var file = $("input[name='file']").prop("files")[0];
    var fileName = file.name;
    //获取签证
    let updata = getPolicy();
    //构建请求体
    var formData = new FormData();
    formData.append("OSSAccessKeyId", updata.OSSAccessKeyId);
    formData.append("signature", updata.signature);
    formData.append("policy", updata.policy);
    formData.append("key", fileName);
    formData.append("file", file);
    formData.append("success_action_status", 200);
    //发生异步请求
    $.ajax({
        url: updata.host,
        type: "POST",
        data: formData,
        contentType: false,
        processData: false,
        success: function (data, status, response) {
            //上传成功，更新数据库
            if (status === 'success') {
                updateUrl(fileName);
            }
        },
        error: function (e) {
            console.log(e);
            alert("上传失败！");
        }
    });
}
//获取签证
function getPolicy(){
    var restultData = null;
    //发送同步请求
    $.ajax({
        url: CONTEXT_PATH +"/user/getPolicy",
        type: "GET",
        async: false,
        success: function (data, status, request) {
            data = $.parseJSON(data);
            if(data.code == 0){
                restultData = data;
            }else{
                alert(data.msg);
            }
        },
        error: function (e) {
            console.log("失败", e);
        }
    });
    return restultData;
}


//更新数据库
function updateUrl(fileName){
    $.post(
        CONTEXT_PATH+"/user/updateUrl",
        {"fileName":fileName},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0){
                window.location.reload();
            }else{
                alert(data.msg);
            }
        }
    )
}