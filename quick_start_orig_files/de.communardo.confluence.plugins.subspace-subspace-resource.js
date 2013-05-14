var nextId = 0;
var firsttime = true;

function minus(id) {
	var row = document.getElementById(id);
	var tbody = row.parentNode;
	if (jQuery(tbody).children().length >= 1) {
		jQuery(row).remove();
	}
}
function plus(path) {
	if (firsttime) {
		nextId = jQuery(document.getElementById("addlink_tbody")).children().length + 1;
		firsttime = false;
	}

	var tbody = document.getElementById("addlink_tbody");
	var row = document.createElement("tr");
	row.setAttribute("id", "row_" + nextId);

	var data1 = document.createElement("td");
	var data2 = document.createElement("td");
	var data3 = document.createElement("td");

	var input1 = document.createElement("input");
	input1.setAttribute("name", "alias_" + nextId);
	input1.setAttribute("id", "alias_" + nextId);
	input1.setAttribute("type", "text");
	input1.setAttribute("size", "30");

	var input2 = document.createElement("input");
	input2.setAttribute("name", "adress_" + nextId);
	input2.setAttribute("id", "adress_" + nextId);
	input2.setAttribute("type", "text");
	input2.setAttribute("size", "30");
	input2.setAttribute("value", "http://");

	var image = document.createElement("img");
	image.setAttribute("id", "img_" + nextId);
	image
			.setAttribute(
					"src",
					path+"/download/resources/de.communardo.confluence.plugins.subspace:subspace-resource/images/minus.png");

	image.setAttribute("onclick", "minus('row_" + nextId + "')");
	image.setAttribute("onmouseover", "this.style.cursor='pointer'");

	jQuery(data1).append(input1);
	jQuery(data2).append(input2);
	jQuery(data3).append(image);

	jQuery(row).append(data1);
	jQuery(row).append(data2);
	jQuery(row).append(data3);
	jQuery(tbody).append(row);

	document.getElementById("img_" + nextId).onclick = new Function(
			"minus('row_" + nextId + "')");
	document.getElementById("img_" + nextId).onmouseover = new Function(
			"this.style.cursor='pointer'");

	nextId++;
}
function countRows() {
	var tbody = document.getElementById("addlink_tbody");
	var numberofRows = getInterestingRows().length;
	document.getElementById("numberOfRows").setAttribute("value", numberofRows);

	// force continuous numbering
	for ( var i = 0; i < numberofRows; i++) {
		oldIndex = jQuery(tbody).children()[i].id.substr(4, jQuery(tbody)
				.children()[i].id.length);
		document.getElementById("row_" + oldIndex).setAttribute("id",
				"line_" + i);
		
		var object;
		object = document.getElementById("alias_" + oldIndex);
		object.name = "newalias_" + i;

		object = document.getElementById("adress_" + oldIndex);
		object.name = "newadress_" + i;
	}
}

function getInterestingRows() {
    return jQuery("tr[id^='row_']" ,document.getElementById("addlink_tbody"));
}

var operationInProgressArray = new Array(); // use this array to prevent the user from triggering off another labelling operation when one is in progress

function subspacesToggleStar(imgElement)
{
   var imagePath = imgElement.src;
   if (imgElement.src.indexOf("star_grey.gif") != -1)
       imgElement.src = contextPath + '/images/icons/star_yellow.gif';
   else
       imgElement.src = contextPath + '/images/icons/star_grey.gif';
}

function subspacesAddOrRemoveFromFavourites(spaceKey, imgElement)
{
   if (operationInProgressArray[imgElement.id] == null) {

       operationInProgressArray[imgElement.id] = true;

       var url;

       if (imgElement.src.indexOf("star_yellow.gif") != -1) { // if on
           url = contextPath + "/labels/removespacefromfavourites.action";
       }
       else {
           url = contextPath + "/labels/addspacetofavourites.action";
       }

       jQuery.ajax({
           url: url,
           type: "POST",
           data: { "key" : spaceKey },
           success: function() {
        	   subspacesToggleStar(imgElement);
               operationInProgressArray[imgElement.id] = null;
           },
           error: function(xhr, text, error) {
               alert("Error : " + text);
               operationInProgressArray[imgElement.id] = null;
            }
       });
   }
}

function subspacesGotoUrl(url)
{
   for (var elementId in operationInProgressArray)
   {
       if (operationInProgressArray[elementId] == true)
           return;
   }
   window.location = url;
}

