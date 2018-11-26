let currentGroupInfo = null;
let currentGroupMembers = null;

function fetchMessages(callback) {
    $.getJSON("/chat/api/messages/" + currentGroupInfo.id, (data) => callback(data))
}

function showMesssages(messages) {
    function buildGroupHeader(groupInfo) {
        let header = $("#Components #GroupHeader").clone();
        header.find("img.profileImage").src(groupInfo.imageUrl);
        header.find("#GroupName").text(groupInfo.name);
        header.find("#GroupDescription").text(groupInfo.name);
        return header;
    }

    function buildMessageEntry(groupMembers, message) {
        let member = groupMembers[message.senderId];
        let item = $("#Components .historyItem").clone();
        item.find("img.profileImage").src(member.profileImageUrl);
        item.find("p.senderName").text(member.name);
        item.find("p.messageText").text(message.message)
    }

    function buildMessageHistory(groupMembers, messages) {
        let container = $("<div>");
        for (let i = 0; i < messages.length; i++) {
            buildMessageEntry(groupMembers, messages[i]).appendTo(container);
        }
        return container;
    }

    let history = $("#MessageHistory").empty();
    buildMessageHistory(currentGroupMembers, messages).appendTo(history);
    buildGroupHeader(currentGroupInfo).appendTo(history);
    return history;
}

function selectGroup(groupId) {
    currentGroupInfo = null;
    currentGroupMembers = null;

    $.getJSON("/chat/api/group/" + groupId, (groupInfo) => {
        currentGroupInfo = groupInfo;
        $.getJSON("/chat/api/members" + groupId, (members) => {
            currentGroupMembers = members;
            fetchMessages(showMesssages)
        })
    });
}
