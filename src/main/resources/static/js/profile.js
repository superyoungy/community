$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if ($(btn).hasClass("btn-info")) {
		$.ajax({
			type : "POST",
			url : CONTEXT_PATH + "/follow",
			data : {"entityType" : 3, "entityId" : $(btn).prev().val()},
			dataType : "json",
			success : function (data) {
				if (data.code == 0) {
					window.location.reload();
				} else {
					alert(data.msg);
				}
			}
		})
	} else {
		$.ajax({
			type : "POST",
			url : CONTEXT_PATH + "/unfollow",
			data : {"entityType" : 3, "entityId" : $(btn).prev().val()},
			dataType : "json",
			success : function (data) {
				if (data.code == 0) {
					window.location.reload();
				} else {
					alert(data.msg);
				}
			}
		})
	}
}