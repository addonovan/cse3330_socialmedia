function showNormalPost() {
    $("#PostForm").removeClass("hidden");
    $("#EventForm").addClass("hidden");
    $("#PollForm").addClass("hidden");
}

function showEventPost() {
    $("#PostForm").addClass("hidden");
    $("#EventForm").removeClass("hidden");
    $("#PollForm").addClass("hidden");
}

function showPollPost() {
    $("#PostForm").addClass("hidden");
    $("#EventForm").addClass("hidden");
    $("#PollForm").removeClass("hidden");
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

            case "poll":
                showPollPost();
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
    $("#Post" + postId + " > .content > .reactions .buttons").remove();
}

function removeAnswer(button) {
    let container = $(button).parent();
    if (container.parent().children().length > 2) {
        container.remove();
    }
}

function addAnswer(button) {
    let btn = $(button);
    let newAnswer = btn.prev().clone();
    newAnswer.find("input[type='text']").val("");
    newAnswer.insertBefore(btn);
}

function postPoll(wallId) {
    let data = {
        wallId: wallId,
        posterId: $("#PollForm select[name='posterId']").val() || 0,
        question: $("#PollForm input[name='question']").val(),
        endDate: $("#PollForm input[name='endDate']").val(),
        endTime: $("#PollForm input[name='endTime']").val()
    };

    $("#PollFormQuestions").find("input[type='text']")
        .each((i, it) => {
            data["answer" + i] = it.value;
        });

    $.post("/post/poll/submit", data, () => {
        location.reload(true);
    });
}

$(() => {
    attachEventTypeListener();
    selectPostDefault();
});
