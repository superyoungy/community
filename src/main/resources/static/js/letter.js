$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");

	var targetName = $("#recipient-name").val();
	var content = $("#message-text").val();

	$.ajax ({
		type : "POST",
		url : CONTEXT_PATH + "/letter/send",
		data : {"targetName":targetName,"content":content},
		dataType : "json",
		success : function(data) {
			if (data.code == 0) {
				$("#hintBody").text("发送成功！");
			} else {
				$("#hintBody").text(data.msg);
			}

			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
		});
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}