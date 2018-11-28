const fetchSearchResults = (text, callback) =>
    $.getJSON("/account/search", { text: text }, callback);

function showSearchResults(list, results) {
    list.empty();
    for (let i = 0; i < results.length; i++) {
        let link = $("<a>").prop('href', results[i]);
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
