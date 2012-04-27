window.onload = load_cyto();


function createRequest() {

  try {
     request = new XMLHttpRequest();
  } catch (tryMS) {
    try {
        request = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (failed) {
	request = null;
    }
   }

   return request;
}


function load_cyto() {

   request = createRequest();

   if (request == null) {
	alert ("Unable to create request");
	return;
   }

   var url = "webservice/topo_json.cgi";

   request.open("GET",url,true);
   request.onreadystatechange = update_cyto;
   request.send(null);
	
} 


function update_cyto() {


  
   if (request.readyState == 4) {
	if (request.status == 200) {

	  var resp = request.responseText;
          var network_json = JSON.parse(resp); 
         //document.getElementById("note").innerHTML += "<p>" + resp + "</p>";

    var div_id = "cytoscapeweb";


    var visual_style = {
	nodes: {
		       color: {
			 discreteMapper: {
                                attrName: "status",
                                entries: [
                                    { attrValue: 0, value: "#25E86D" },
                                    { attrValue: 1, value: "#F28F96" }
				]
				}
			},
                        compoundShape: "RECTANGLE",
                        label: { passthroughMapper: { attrName: "id" } } ,
                        compoundLabel: { passthroughMapper: { attrName: "id" } } ,
                        borderWidth: 2,
                        compoundBorderWidth: 1,
                        borderColor: "#666666",
                        compoundBorderColor: "#999999",
                        size: 25,
                        compoundColor: "#eaeaea",
                }
	  };
	
       var options = {
                    swfPath: "/swf/CytoscapeWeb",
                    flashInstallerPath: "/swf/playerProductInstall",
                };


	 var vis = new org.cytoscapeweb.Visualization(div_id, options);
	  // callback when Cytoscape Web has finished drawing
	   vis.ready(function() {
		   vis.addListener("click", "nodes", function(event) {
                        handle_click(event);
                    })
                    .addListener("click", "edges", function(event) {
                        handle_click(event);
                    });
                    
                    function handle_click(event) {
                         var target = event.target;
                         
                         clear();
			  var rrdfile = target.data["rrd"];
                         print(target.data["id"] + " RRD: " + rrdfile); 

			// Clear old graph
			 document.getElementById("traffic_graph").innerHTML = "";
	          	 
			 // Create new graph
			 setupMeasurementGraph(rrdfile);

                    }
                    
                    function clear() {
                        document.getElementById("note").innerHTML = "";
                    }
                
                    function print(msg) {
                        document.getElementById("note").innerHTML += "<p>" + msg + "</p>";
                    }
                });

	   // draw options
	    var draw_options = {
		network: network_json,
		panZoomControlVisible: true,
		visualStyle: visual_style
	    };

	    vis.draw(draw_options);
	}
     }
}        



