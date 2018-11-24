/**
 * Deletes the event with the given event id.
 *
 * This will go through checks on the server-side to make sure that
 * the event can actually be safely deleted.
 *
 * @param eventId
 */
function deleteEvent(eventId) {
    let data = {eventId: eventId};
    $.post("/event/delete", data, () => removeEvent(eventId));
}

function markAttending(eventId, onlyInterested) {
    let data = {
        eventId: eventId,
        onlyInterested: onlyInterested
    };
    $.post("/event/markInterested", data, () => {
        removeButtons(eventId);
        if (onlyInterested) {
            addGoingButton(eventId);
        }
    });
}

const eventDivSelector = (eventId) => "#Event" + eventId;
const eventButtonsSelector = (eventId) => eventDivSelector(eventId) + " .buttons";

function removeEvent(eventId) {
    $(eventDivSelector(eventId)).remove();
}

function removeButtons(eventId) {
    $(eventButtonsSelector(eventId)).remove();
    $("<div>")
        .addClass("buttons")
        .appendTo($(eventDivSelector(eventId)));
}

function addGoingButton(eventId) {
    $("<button>")
        .click(() => {
            markAttending(eventId, true)
        })
        .appendTo($(eventButtonsSelector(eventId)));
}
