approveFollow = (followerId) => updateFollow("approve", followerId);
denyFollow = (followerId) => updateFollow("reject", followerId);

function updateFollow(action, followerId) {
    $.post("/account/follow/" + action + "/" + followerId, () => {
        removeRequestFrom(followerId)
    });
}

function removeRequestFrom(followerId) {
    $("#FollowRequst" + followerId).remove();
}
