function like(btn, entityUserId, entityType, entityId) {
    $.ajax({
        type : "POST",
        url  : CONTEXT_PATH + "/like",
        data : {"entityUserId" : entityUserId, "entityType" : entityType, "entityId" : entityId },
        dataType : "json",
        success : function(data) {
            if (data.code == 0) {
                $(btn).children("b").text(data.likeEntityStatus == 1 ? "已赞" : "赞");
                $(btn).children("i").text(data.likeCountEntity);
            } else {
                alert(data.msg);
            }
        }
    });
}