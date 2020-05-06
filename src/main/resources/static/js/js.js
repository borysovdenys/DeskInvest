$(document).ready(function () {
    $("#locales").change(function () {
        var selectedOption = $('#locales').val();
        if (selectedOption != '') {
            window.location.replace('?lang=' + selectedOption);
        }
    });
});

$(document).ready(function () {
    var isInlineEdit = "[[${takenItem} != null ? true:false]]";
    if (isInlineEdit) {
        $('#myModal').modal('show');
    }
});

$(document).ready(function () {
    var isInlineEdit = "[[${deleteItem} != null ? true:false]]";
    if (isInlineEdit) {
        $('#deleteModal').modal('show');
    }
});


var wasSubmitted = false;
function checkBeforeSubmit() {
        if (!wasSubmitted) {
            wasSubmitted = true;
            return wasSubmitted;
        }
        return false;
};