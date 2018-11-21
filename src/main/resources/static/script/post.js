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

$(() => {
    attachEventTypeListener();
});
