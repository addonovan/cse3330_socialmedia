const fetchGroupInfo = (groupId, callback) =>
    $.getJSON("/chat/api/group/" + groupId, callback);

const fetchGroupMembers = (groupId, callback) =>
    $.getJSON("/chat/api/members/" + groupId, callback);

const fetchMessages = (groupId, callback) =>
    $.getJSON("/chat/api/messages/" + groupId, callback);

function showMessages(groupInfo, groupMembers, messages) {
    function buildGroupOutline(groupInfo, groupMembers) {
        let outline = $("#Components #Outline").clone();

        // fill out the header
        outline.find("#GroupHeader img.profileImage").attr("src", groupInfo.pictureUrl);
        outline.find("#GroupHeader #GroupName").text(groupInfo.name);
        outline.find("#GroupHeader #GroupDescription").text(groupInfo.description);

        outline.find("#GroupHeader #EditGroup").click(() => {
            editGroup(groupInfo)
        });

        let memberList = outline.find("#GroupMembers");
        Object.keys(groupMembers).forEach((id) => {
            let member = groupMembers[id];
            let name = member.firstName + " " + member.lastName;
            $("<li>")
                .append($("<p>").text(name))
                .appendTo(memberList);
        });

        // fill out the form
        outline.find("#MessageComposer input[name='groupId']").val(groupInfo.id);

        return outline;
    }

    function buildMessageEntry(groupMembers, message) {
        let member = groupMembers[message.senderId];
        let item = $("#Components .historyItem").clone();
        item.find("img.profileImage").prop("src", member.profileImageURL);
        item.find("p.senderName").text(member.firstName + " " + member.lastName);

        let messageExpands = message.mediaUrl === null ? "" : "flex-expand";

        if (message.message != null) {
            item.find("p.messageText")
                .text(message.message)
                .addClass(messageExpands);
        }

        if (message.mediaUrl != null) {
            item.find("img.messageImage")
                .prop("src", message.mediaUrl)
                .addClass("flex-expand");
        }

        return item;
    }

    function addMessagesTo(container, groupMembers, messages) {
        for (let i = 0; i < messages.length; i++) {
            buildMessageEntry(groupMembers, messages[i]).appendTo(container);
        }
    }

    let history = $("#MessageHistory").empty();
    buildGroupOutline(groupInfo, groupMembers).appendTo(history);
    addMessagesTo(history.find("#Messages"), groupMembers, messages);
}

function selectGroup(groupId) {
    // oh yeah, now THAT's hot
    fetchGroupInfo(groupId, (groupInfo) => {
        fetchGroupMembers(groupId, (groupMembers) => {
            fetchMessages(groupId, (messages) => {
                showMessages(groupInfo, groupMembers, messages);
            });
        });
    });
}

function editGroup(groupInfo) {
    let header = $("#MessageHistory #GroupHeader").empty();

    let form = $("#Components #GroupUpdater").clone();
    form.find("input[name='id']").val(groupInfo.id);
    form.find("input[name='name']").val(groupInfo.name);
    form.find("input[name='description']").val(groupInfo.description);

    header.append(form);
}

function addFriend() {
    $("#FriendSelector").clone()
        .removeAttr("id")
        .removeClass("hidden")
        .appendTo(
            $("#ChatInfo > div")
        )
}

function removeFriend(button) {
    $(button).parent().remove();
}

function createGroupChat() {
    let data = {
        name: $("#ChatInfo input[name='name']").val(),
        description: $("#ChatInfo input[name='description']").val(),
    };

    $("#ChatInfo > div").find("select")
        .each((i, it) => {
            data["member" + i] = it.value;
        });

    $.post("/chat/create", data, () => {
        location.assign("/chat");
    });
}
