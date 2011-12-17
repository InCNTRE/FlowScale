window.onload = function() {
     // id of Cytoscape Web container div
       var div_id = "cytoscapeweb";
       var myTabs = new YAHOO.widget.TabView("demo");


       var network_json = {
		// NOTE the parent attribute
		data: {
                            nodes: [ 
                                     { id: "Input Ports" },
                                     { id: "BL Sensors" }, 
                                     { id: "Loadbalancers" },
                                     { id: "1", parent: "BL Sensors" },
                                     { id: "2", parent: "BL Sensors" },
                                     { id: "3", parent: "BL Sensors" },
                                     { id: "4", parent: "BL Sensors" },
                                     { id: "5", parent: "BL Sensors" },
                                     { id: "6", parent: "BL Sensors" },
                                     { id: "7", parent: "BL Sensors" },
                                     { id: "8", parent: "BL Sensors" },
                                     { id: "9", parent: "BL Sensors" },
                                     { id: "10", parent: "BL Sensors" },
                                     { id: "11", parent: "BL Sensors" },
                                     { id: "12", parent: "BL Sensors" },
                                     { id: "cr3.bldc", parent: "Input Ports" },
                                     { id: "cr4.bldc", parent: "Input Ports", },
                                     { id: "cr5.bldc", parent: "Input Ports" },
                                     { id: "dcr3.bldc", parent: "Input Ports" },                                 
                                     { id: "dcr4.bldc", parent: "Input Ports" },
                                     { id: "lb1.bldc", parent: "Loadbalancers" },
                                     { id: "cr3.hper", parent: "Input Ports" },
                                     { id: "cr4.hper", parent: "Input Ports" },
                                     { id: "cr5.hper", parent: "Input Ports" },
                            ],
          		      edges: [ 
                                    { target: "lb1.bldc", source: "cr3.bldc" },
                                    { target: "lb1.bldc", source: "cr4.bldc" },
                                    { target: "lb1.bldc", source: "cr5.bldc" },
                                    { target: "lb1.bldc", source: "dcr3.bldc" },
                                    { target: "lb1.bldc", source: "dcr4.bldc" },
                                    { target: "lb1.bldc", source: "cr3.hper" },
                                    { target: "lb1.bldc", source: "cr4.hper" },
                                    { target: "lb1.bldc", source: "cr5.hper" },
                                    { target: "1", source: "lb1.bldc" },
                                    { target: "2", source: "lb1.bldc" },
                                    { target: "3", source: "lb1.bldc" },
                                    { target: "4", source: "lb1.bldc" },
                                    { target: "5", source: "lb1.bldc" },
                                    { target: "6", source: "lb1.bldc" },
                                    { target: "7", source: "lb1.bldc" },
                                    { target: "8", source: "lb1.bldc" },
                                    { target: "9", source: "lb1.bldc" },
                                    { target: "10", source: "lb1.bldc" },
                                    { target: "11", source: "lb1.bldc" },
                                    { target: "12", source: "lb1.bldc" },
                            ]
                        }
                };

		// Visual style
		var visual_style = {
                    nodes: {
                        color: {
                                discreteMapper: {
                                attrName: "id",
                                entries: [
                                    { attrValue: "lb1.bldc", value: "#25E86D" },
                                    { attrValue: "1", value: "#25E86D" },
                                    { attrValue: "2", value: "#25E86D" },
                                    { attrValue: "3", value: "#25E86D" },
                                    { attrValue: "4", value: "#25E86D" },
                                    { attrValue: "5", value: "#25E86D" },
                                    { attrValue: "6", value: "#25E86D" },
                                    { attrValue: "7", value: "#25E86D" },
                                    { attrValue: "8", value: "#25E86D" },
                                    { attrValue: "9", value: "#25E86D" },
                                    { attrValue: "10", value: "#25E86D" },
                                    { attrValue: "11", value: "#25E86D" },
                                    { attrValue: "12", value: "#F28F96" },
                                    { attrValue: "cr4.bldc", value: "#25E86D" },
                                    { attrValue: "cr3.bldc", value: "#25E86D" },
                                    { attrValue: "cr5.bldc", value: "#25E86D" },
                                    { attrValue: "dcr3.bldc", value: "#F28F96" },
                                    { attrValue: "dcr4.bldc", value: "#F28F96" },
                                    { attrValue: "cr3.hper", value: "#F28F96" },
                                    { attrValue: "cr4.hper", value: "#F28F96" },
                                    { attrValue: "cr5.hper", value: "#F28F96" },
                                    { attrValue: "srv1.ictc", value: "#25E86D" },
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

		// initialization options
		var options = {
                    swfPath: "/swf/CytoscapeWeb",
                    flashInstallerPath: "/swf/playerProductInstall",
                };
                
                var vis = new org.cytoscapeweb.Visualization(div_id, options);

                vis.ready(function() {

                    // add a listener for when nodes and edges are clicked
		    vis.addListener("click", "nodes", function(event) {
                        handle_click(event);
                    });

                    function handle_click(event) {
                         var target = event.target;
                         
                         clear();
        
                         var file = "156.56.5.43_41.rrd";

                         if (event.target.data.id == "1") { file = "156.56.5.43_1.rrd"; }
                         if (event.target.data.id == "2") { file = "156.56.5.43_4.rrd"; }
                         if (event.target.data.id == "3") { file = "156.56.5.43_5.rrd"; }
                         if (event.target.data.id == "4") { file = "156.56.5.43_8.rrd"; }
                         if (event.target.data.id == "5") { file = "156.56.5.43_9.rrd"; }
                         if (event.target.data.id == "6") { file = "156.56.5.43_12.rrd"; }
                         if (event.target.data.id == "7") { file = "156.56.5.43_13.rrd"; }
                         if (event.target.data.id == "8") { file = "156.56.5.43_16.rrd"; }
                         if (event.target.data.id == "9") { file = "156.56.5.43_17.rrd"; }
                         if (event.target.data.id == "10") { file = "156.56.5.43_20.rrd"; }
                         if (event.target.data.id == "11") { file = "156.56.5.43_21.rrd"; }
 
                         if (event.target.data.id == "Loadbalancers") { file = "156.56.5.43_25.rrd:156.56.5.43_26.rrd:156.56.5.43_27.rrd:140.221.223.201_51.rrd"; }
                         if (event.target.data.id == "BL Sensors") { file = "156.56.5.43_1.rrd:156.56.5.43_4.rrd:156.56.5.43_5.rrd:156.56.5.43_8.rrd:156.56.5.43_9.rrd:156.56.5.43_12.rrd:156.56.5.43_13.rrd:156.56.5.43_16.rrd:156.56.5.43_17.rrd:156.56.5.43_20.rrd:156.56.5.43_21.rrd"; }
                        
                         if (event.target.data.id == "Input Ports") { file = "156.56.5.43_25.rrd:156.56.5.43_26.rrd:156.56.5.43_27.rrd:140.221.223.201_51.rrd"; } 
        
                          if (event.target.data.id == "cr3.bldc") { file = "156.56.5.43_25.rrd"; }
                         if (event.target.data.id == "cr4.bldc") { file = "156.56.5.43_26.rrd"; }
                         if (event.target.data.id == "cr5.bldc") { file = "156.56.5.43_27.rrd"; }

                        
                         if (event.target.data.id == "lb1.bldc") { file = "156.56.5.43_25.rrd:156.56.5.43_26.rrd:156.56.5.43_27.rrd"; }

	
			//graph.render(); 
			   var date = new Date();
                           var now  = date.valueOf() / 1000;
                           var then = now - 600;
                           var graph = new MeasurementGraph("traffic_graph",
                                     "traffic_legend",
                                     {
                                         title:      "foobar",
                                         circuit_id: file,
                                         start:      then,
                                         end:        now
                                     }
                                     );

                         graph.render();
			//setupMeasurementGraph(file);
			 if (event.target.data.parent == null) {
                            document.getElementById("note").innerHTML = "<p>" + event.target.data.id + "</p>"; }
                         else {
                            document.getElementById("note").innerHTML = "<p>" + event.target.data.parent + ": " + event.target.data.id + "</p>"; }

                         //for (var i in target.data) {
                         //   var variable_name = i;
                         //   var variable_value = target.data[i];
                         //   print( "event.target.data." + variable_name + " = " + variable_value );
                         //}
                         }

			function clear() {
                           document.getElementById("note").innerHTML = "";
                         }
                
                        function print(msg) {
                           document.getElementById("note").innerHTML += "<p>" + msg + "</p>";
                        } 

                   });
                
                   var draw_options = {
		 	// your data goes here
		 	network: network_json,
                        // this is the best layout to use when the network has compound nodes
			layout: "CompoundSpringEmbedder",
			// set the style at initialisation
			visualStyle: visual_style,
			// hide pan zoom
		        panZoomControlVisible: true
	            };
		    
		    vis.draw(draw_options);
                    setupMeasurementGraph();
            };


	    function setupMeasurementGraph(file){
                var date = new Date();
                var now  = date.valueOf() / 1000;
                var then = now - 600;
                var graph = new MeasurementGraph("traffic_graph",
                                     "traffic_legend",
                                     {
                                         title:      "foobar",
                                         circuit_id: file,
                                         start:      then,
                                         end:        now
                                     }
                                     );

                var time_select = new YAHOO.util.Element(YAHOO.util.Dom.get("traffic_time"));
                time_select.on("change", function(){
                        var new_start = this.get('element').options[this.get('element').selectedIndex].value;
                        var date = new Date();
                        graph.options.end   = date.valueOf() / 1000;
                        graph.options.start = graph.options.end - new_start;
                        graph.render();
                });

                return graph;
	    }


function MeasurementGraph(container, legend_container, options){

    this.container        = container;
    this.legend_container = legend_container;
    this.options          = options;
    this.graph            = null;
    this.panel            = null;
    this.updating         = null;
    this.POLL_INTERVAL    = 10000;


    var round = function(value){
        return Math.round(value*100)/100;
    }

    this.convertToSI = function(value){
        if (value == 0 || value == null){
            return "0";
        }
        if (value < 1){
            return round(value * 1000) + " m";
        }
        if (value >= 1000*1000*1000){
            return round(value / (1000*1000*1000)) + " G";
        }
        if (value >= 1000*1000){
            return round(value / (1000*1000)) + " M";
        }
        if (value >= 1000){
            return round(value / (1000)) + " k";
        }
        return round(value);
    };

    

    this.GRAPH_CONFIG = {lines: {
                                  show: true, 
                                  lineWidth: 2,
                                  fill: false
                         },
                         grid: {
                                  hoverable: true,
                                  backgroundColor: "white",
                                  borderWidth: 1
                         },
                         xaxis: {
                                  mode: "time",
                                  ticks: 7
                         },
                         yaxis: {
                                  tickFormatter: this.convertToSI,
                                  min: 0
                         },
                         legend: {
                                  container: legend_container,
                                  noColumns: 3
                         },
                         selection:{
                                  mode: "x",
                                  color: "yellow"
                         },
                         crosshair:{
                                  mode: "x",
                                  color: "#999900"
                         }
    };
    this._getPanelCoordinates = function(){
        var region = YAHOO.util.Dom.getRegion(this.container);

        return [region.right - (region.width / 2) - 120, // subtract half the panel width
	        region.top + (region.height / 2) - 40  // subtract half-ish the panel height
	           ];
    };

    this._showLoading = function(){

        if (this.panel) this.panel.destroy();

        this.panel = new YAHOO.widget.Panel("wait",
                                            { width:"240px",
                                              close:true,
                                              draggable:false,
                                              zindex:4,
                                              eisible:false,
                                              xy: this._getPanelCoordinates()
                                            }
                                            );

        this.panel.setHeader("Loading...");
        this.panel.setBody("<center><img src='media/loading.gif'></center>");

        this.panel.render(container);
        this.panel.show();
    };

    this._showBuilding = function(){

        if (this.panel) this.panel.destroy();

        this.panel = new YAHOO.widget.Panel("build",
                                            { width:"240px",
                                              close:true,
                                              draggable:false,
                                              zindex:4,
                                              visible:false,
                                              xy: this._getPanelCoordinates()
                                            }
                                            );

        this.panel.setHeader("Building...");
        this.panel.setBody("<center>Data collection for this circuit is building, one moment...</center>");

        this.panel.render(container);
        this.panel.show();
    }

    this._hideLoading = function(){
        if (this.panel){
            this.panel.destroy();
            this.panel = null;
        }
    };

    this._showError = function(){ 

        if (this.panel) this.panel.destroy();

        this.panel = new YAHOO.widget.Panel("error",
                                            { width:"240px",
                                              close:true,
                                              draggable:false,
                                              zindex:4,
                                              visible:false,
		xy: this._getPanelCoordinates()
                                            }
                                            );

        this.panel.setHeader("Error in Traffic Data");
        this.panel.setBody("There was an error fetching traffic data. If this problem persists, please contact your system administrator.");

        this.panel.render(container);
        this.panel.show();

    };
    
    this._renderGraph = function(request, response){

        if (response && response.meta.in_progress == 1){
            this._showBuilding();

            this.updating = setTimeout(function(self){
                    return function(){
                        self.render(true);
                    }
                }(this), 3000);

            return;
        }

        var results = response.results;

        if (! results || results.length == 0){
            this._showError();
            return;
        }

        var shown_data = [];

        for (var i = 0; i < results.length; i++){

            var name   = results[i].name;

            var data = results[i].data;

            var setup = {data: data,
                         control: "time",
                         label: name,
                         name: name
            };

            if (name == "Ping (ms)"){
                this.GRAPH_CONFIG["yaxis2"] = {};
                this.GRAPH_CONFIG["yaxis2"]["tickFormatter"] = this.convertToSI;
                setup["yaxis"] = 2;
            }

            if (name == "Input (bps)"){
                setup["lines"] = {fill: .6};
                setup["color"] = "#00FF00";
            }

            if (name == "Output (bps)"){
                setup["color"] = "#0000FF";
            }
            if (name == "filename"){
            //  document.getElementById("note").innerHTML = data;
            }       

            shown_data.push(setup);

        }

        this.graph = new YAHOO.widget.Flot(this.container,
                                shown_data,
                                this.GRAPH_CONFIG);

	         this._hideLoading();   

        this.updating = setTimeout(function(self){
                return function(){
                    self.options.start += (self.POLL_INTERVAL / 1000);
                    self.options.end   += (self.POLL_INTERVAL / 1000);
                    self.render(true);
                }
            }(this), this.POLL_INTERVAL);

    };
    
    this.render = function(skip_show){

        if (this.updating){
            clearTimeout(this.updating);
        }

        if (! skip_show){
            this._showLoading();
        }

        if (! this.options.circuit_id){
           this.options.circuit_id = "156.56.5.43_25.rrd:156.56.5.43_26.rrd:156.56.5.43_27.rrd"; 
        }
        
        var ds = new YAHOO.util.DataSource("webservice/fs_measurement_agg.cgi?file="+this.options.circuit_id+"&start="+parseInt(this.options.start));
        ds.responseType = YAHOO.util.DataSource.TYPE_JSON;


        ds.responseSchema = {
            resultsList: "results",
            fields: [{key: "name"},
                     {key: "data"}
                     ],
            metaFields: {
                "in_progress": "in_progress",
                "error": "error"
            }
        };

        ds.sendRequest("", {success: this._renderGraph,
                            failure: function(req, resp){
                               this._hideLoading();
                               this._showError();
                               throw("Error getting graph data.\n" + YAHOO.lang.dump(resp));
                           },
                           scope: this
            });
                
    };

    
    this.render();

    return this;
}




