function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#setTopBtn").attr("disabled", "disabled");
            } else {
                alert(data.message);
            }
        }
    );
}

function setWonderful() {

    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#setWonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.message);
            }
        }
    );
}

function remove() {

    $.post(
        CONTEXT_PATH + "/discuss/remove",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.message);
            }
        }
    );
}

$(
    function () {
        $("#setTopBtn").click(setTop);
        $("#setWonderfulBtn").click(setWonderful);
        $("#removeBtn").click(remove);
    }
);


function like(btn, entityUserId, entityType, entityId, postId) {
    $.ajax({
        type : "POST",
        url  : CONTEXT_PATH + "/like",
        data : {"entityUserId" : entityUserId, "entityType" : entityType, "entityId" : entityId, "postId" : postId },
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