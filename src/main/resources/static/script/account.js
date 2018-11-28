function findPostsOnDate(wallId, button) {
    let div = $(button).parent();
    let date = div.find("input[type='date']").val();

    let data = {
        wallId: wallId,
        date: date
    };
    $.getJSON("/account/api/postCountByDate", data, (count) => {
        div.find("span.ui").text(count);
    })
}
