AJS.toInit(function($) {
    var sidebar = $("#personal-info-sidebar");
    if(sidebar.length) {
        var content = $("#content"),
            sidebarControl = $("#personal-info-sidebar .sidebar-collapse"),
            contentContainer = $("#full-height-container"),
            contentContainerHeight = contentContainer.height(),
            contentHeight = content.height();

        sidebar.bind("toggled", function() {
            // force page to stay tall enough to cope with sidebar
            if (!sidebar.hasClass("collapsed") && $("#main").hasClass("has-personal-sidebar")) {
                if (content.hasClass("sidebar-collapsed")) {
                    $("#page").height(contentContainerHeight);
                    content.height(contentHeight);
                    sidebarControl.height(sidebar.height());
                } else {
                    $("#page").height("auto");
                    content.height("auto");
                }
            }
        });
    }
});
