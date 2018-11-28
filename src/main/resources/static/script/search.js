const fetchSearchResults = (text, callback) =>
    $.getJSON("/account/api/search", { text: text }, callback);

function showSearchResults(list, results) {
    const formatName = (account) => {
        if ("name" in account) return account.name;
        else return account.firstName + " " + account.lastName;
    };

    list.empty();
    for (let i = 0; i < results.length; i++) {
        let account = results[i];
        let link = $("<a>")
            .prop("href", "/account/" + account.id)
            .addClass("accountName")
            .text(formatName(account));
        let listItem = $("<li>").append(link);
        list.append(listItem);
    }
}

function registerSearchListener(inputField, list) {
    let lastTimer;
    inputField.keypress(() => {
        clearTimeout(lastTimer);

        let text = inputField.val();
        lastTimer = setTimeout(() => {
            fetchSearchResults(text, (data) => showSearchResults(list, data));
        }, 100);
    })
}

$(() => {
    console.log("Search component loaded!");
    registerSearchListener(
        $(".searchbar > input"),
        $(".searchbar > ul")
    );
});
