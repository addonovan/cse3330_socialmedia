let currentGroupInfo = null;
let currentGroupMembers = null;

function fetchMessages(callback) {
    $.getJSON("/chat/api/messages/" + currentGroupInfo.id, (data) => callback(data))
}

function showMessages(messages) {
    function buildGroupOutline(groupInfo) {
        let outline = $("#Components #Outline").clone();

        // fill out the header
        outline.find("#GroupHeader img.profileImage").src(groupInfo.imageUrl);
        outline.find("#GroupHeader #GroupName").text(groupInfo.name);
        outline.find("#GroupHeader #GroupDescription").text(groupInfo.name);

        // fill out the form
        outline.find("#MessageComposer input[name='groupId']").val(groupInfo.id);

        return outline;
    }

    function buildMessageEntry(groupMembers, message) {
        let member = groupMembers[message.senderId];
        let item = $("#Components .historyItem").clone();
        item.find("img.profileImage").src(member.profileImageUrl);
        item.find("p.senderName").text(member.name);
        item.find("p.messageText").text(message.message)
    }

    function addMessagesTo(container, groupMembers, messages) {
        for (let i = 0; i < messages.length; i++) {
            buildMessageEntry(groupMembers, messages[i]).appendTo(container);
        }
    }

    let history = $("#MessageHistory").empty();
    buildGroupOutline(currentGroupInfo).appendTo(history);
    addMessagesTo(history.find("#Messages"), currentGroupMembers, messages);
    return history;
}

function selectGroup(groupId) {
    currentGroupInfo = null;
    currentGroupMembers = null;

    $.getJSON("/chat/api/group/" + groupId, (groupInfo) => {
        currentGroupInfo = groupInfo;
        $.getJSON("/chat/api/members" + groupId, (members) => {
            currentGroupMembers = members;
            fetchMessages(showMessages)
        })
    });
}
