const fetchGroupInfo = (groupId, callback) =>
    $.getJSON("/chat/api/group/" + groupId, callback);

const fetchGroupMembers = (groupId, callback) =>
    $.getJSON("/chat/api/members/" + groupId, callback);

const fetchMessages = (groupId, callback) =>
    $.getJSON("/chat/api/messages/" + groupId, callback);

function showMessages(groupInfo, groupMembers, messages) {
    function buildGroupOutline(groupInfo) {
        let outline = $("#Components #Outline").clone();

        // fill out the header
        outline.find("#GroupHeader img.profileImage").attr("src", groupInfo.imageUrl);
        outline.find("#GroupHeader #GroupName").text(groupInfo.name);
        outline.find("#GroupHeader #GroupDescription").text(groupInfo.name);

        // fill out the form
        outline.find("#MessageComposer input[name='groupId']").val(groupInfo.id);

        return outline;
    }

    function buildMessageEntry(groupMembers, message) {
        let member = groupMembers[message.senderId];
        let item = $("#Components .historyItem").clone();
        item.find("img.profileImage").prop("src", member.profileImageURL);
        item.find("p.senderName").text(member.firstName + " " + member.lastName);
        item.find("p.messageText").text(message.message);
        return item;
    }

    function addMessagesTo(container, groupMembers, messages) {
        for (let i = 0; i < messages.length; i++) {
            buildMessageEntry(groupMembers, messages[i]).appendTo(container);
        }
    }

    let history = $("#MessageHistory").empty();
    buildGroupOutline(groupInfo).appendTo(history);
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
