$(function (){
    $("#topBtn").click(topPost);
    $("#wonderfulBtn").click(wonderfulPost);
    $("#deleteBtn").click(deletePost);
});

function like(btn,entityType, entityId, entityUserId,postId){
    $.post(
        CONTEXT_PATH +"/like" ,
        {"entityType":entityType,"entityId":entityId, "entityUserId":entityUserId,"postId":postId},
        function (data){
            data = $.parseJSON(data);
            if(data.code == 0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 0 ? '赞':'已赞');
            }else{
                alert(data.msg);
            }
        }
    )
}

function topPost(){
    $.post(
        CONTEXT_PATH +"/discuss/top",
        {"postId":$("#postId").val()},
        function (data){
            data = JSON.parse(data);
            if(data.code == 0){
                $("#topBtn").attr("disabled","disabled");
            }else{
                alert(data.msg);
            }
        }
    );
}
function wonderfulPost(){
    $.post(
        CONTEXT_PATH +"/discuss/wonderful",
        {"postId":$("#postId").val()},
        function (data){
            data = JSON.parse(data);
            if(data.code == 0){
                $("#wonderfulBtn").attr("disabled","disabled");
            }else{
                alert(data.msg);
            }
        }
    );
}
function deletePost(){
    $.post(
        CONTEXT_PATH +"/discuss/delete",
        {"postId":$("#postId").val()},
        function (data){
            data = JSON.parse(data);
            if(data.code == 0){
                window.history.back();
            }else{
                alert(data.msg);
            }
        }
    );
}