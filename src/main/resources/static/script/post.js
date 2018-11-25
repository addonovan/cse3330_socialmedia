function showEventPost() {
    $("#PostForm").addClass("hidden");
    $("#EventForm").removeClass("hidden");
}

function showNormalPost() {
    $("#PostForm").removeClass("hidden");
    $("#EventForm").addClass("hidden");
}

function attachEventTypeListener() {
    $("#PostTypeSelector input[name='postType']").change(function(event) {
        switch ($(this).val()) {
            case "post":
                showNormalPost();
                break;

            case "event":
                showEventPost();
                break;

            default:
                console.error("Unrecognized post type option!");
                break;
        }
    })
}

function selectPostDefault() {
    $("#PostTypeSelector input[name='postType'][value='post']")
        .prop("checked", true);
    showNormalPost();
}

function sendReaction(postId, emotionId) {
    let data = {
        postId: postId,
        emotionId: emotionId
    };
    $.post("/post/react", data, () => {
        hideReactionButtons(postId)
    });
}

function hideReactionButtons(postId) {
    $("#Post" + postId + " .reactions .buttons").remove();
}

$(() => {
    attachEventTypeListener();
    selectPostDefault();
});
