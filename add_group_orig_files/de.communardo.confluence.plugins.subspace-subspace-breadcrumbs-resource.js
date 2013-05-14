AJS.toInit(	function() {
	modifyConfluenceBreadCrums();
});

function modifyConfluenceBreadCrums() {
	
	if( typeof( window[ 'subspaceBreadCrumPath' ] ) != "undefined" && subspaceBreadCrumPath.length > 0) {
		if(jQuery('#breadcrumbs').children()[1]) {
			jQuery(jQuery('#breadcrumbs').children()[1]).remove();
		}
		for ( var pathCount = subspaceBreadCrumPath.length-1; pathCount >= 0; pathCount--) {
			jQuery('#breadcrumbs').children(':first').after("<li><a href='"+contextPath+subspaceBreadCrumPath[pathCount][1]+"'>"+subspaceBreadCrumPath[pathCount][0]+'</a></li>');
		}
	}
}
