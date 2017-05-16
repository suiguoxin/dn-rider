(function ($) {

    var $appsQuickAccessInput = $("#appsQuickAccessInput");
    var apps = $appsQuickAccessInput.data('apps');

    $appsQuickAccessInput.autocomplete({
        source: function(request, response) {
                    var results = $.ui.autocomplete.filter(apps, request.term);
                    response(results.slice(0, 10)); //limit the number of results to 10
                 },
        //add a div when a item is selected
        select: function (event, ui) {
                    event.preventDefault();
                    $(this).val('');
                    $('#modal-chips').append(" <div class=\"chip\"> <span>"+ ui.item.value+ "</span> <button type=\"button\" class=\"close\" onclick=\"this.parentElement.remove()\"><span aria-hidden=\"true\">&times;</span> </button> </div>");
                 }
    });

    //build a string to save appsQuickAccess in cookie
    $("#formModal").submit(function(event){
             var apps = [];
             $('#modal-chips .chip:hidden').remove();
             $('#modal-chips .chip>span').each(function(index){
                apps.push($(this).text());
             });
             apps = apps.join("_");
             $('#appsQuickAccess').val(apps);
             return;
     });
    //hide the div when click on the close button
    $("button[class$='close']").click(function(){
            $(this).parent().hide();
    });
    //show all the divs hidden if the user cancel the change of preference
    $('#modalPreference').on('show.bs.modal', function (e) {
        $('#modal-chips .chip:hidden').show();
    })
}(jQuery));