$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	//隐藏输入框
	$("#publishModal").modal("hide");
	//获取文本
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();

	//发送异步请求
	$.post(
		CONTEXT_PATH +"/discuss/add",
		{"title":title,"content":content},
		function (data){
			data = $.parseJSON(data);
			//在提示框中显示消息
			$("#hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			//隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 刷新页面
				if(data.code == 0) {
					window.location.reload();
				}
			}, 2000);
		}
	);
}