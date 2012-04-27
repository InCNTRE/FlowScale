window.onload = init_graph();


function init_graph () {
	setupMeasurementGraph("156.56.5.43_25.rrd");
}


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
    this.POLL_INTERVAL    = 100000000;


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
           this.options.circuit_id = ""; 
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




