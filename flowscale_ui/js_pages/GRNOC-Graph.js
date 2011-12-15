/**
 * Creates a pretty Flot based graph based on the parameters given
 * @class Graph
 * @namespace GRNOC.widget.Graph
 * @constructor
 * @param holder_div {string} HTML div element's id that will hold the graph
 * @param legend_div {string} HTML div element's id that will hold the legend. Can be null
 * @param datasource {YUI DataSource} YUI Datasource object that will return data for the graph to consume
 * @param options    {object} Javascript object containing various configuration options
 */

function Graph(holder_div, legend_div, datasource, options){
  var DEFAULT_OVERVIEW_CONFIG = {legend: { 
				    show: false
				  },
				  lines: { 
				    show: true, 
				    lineWidth: 1 
				  },
				  xaxis: { 
				    mode:'time', 
				    ticks: 4 
				  },
				  yaxis: { 
				    tickFormatter: convertToHumanReadable
				  },
				  grid: { 
				    backgroundColor: 'white',
				    borderWidth: 1
				  },
				  selection:{
				    color: "yellow"
				  }
				 };

  var DEFAULT_GRAPH_CONFIG = {lines: {
				 show: true, 
				 lineWidth: 1,
				 fill: false
			       },
			       grid: {
				 hoverable: true,
				 backgroundColor: "white",
				 autoHighlight: true,
				 borderWidth: 1
			       },
			       xaxis: {
				 mode: "time",
				 ticks: 7
			       },
			       yaxis: {
				 tickFormatter: convertToHumanReadable
			       },
			       legend: {
				 container: "legend_div",
				 noColumns: 3,
				 labelFormatter: function(label){
				   return "<strong>"+label+"</strong>";
				 },
				 backgroundOpacity: 0.0
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
  
  this.UPDATE_SUCCESS = "grnoc_graph_successful_update";
  this.UPDATE_FAILURE = "grnoc_graph_failed_update";
  
  var active_ds     = options.active_datasources;
  var graph_options = options.graph_options; 
  var graph_type    = options.graph_type || "normal";
  var ds_parameters = options.ds_parameters;
  var debug         = options.debug;

  var graph         = null;
  var overview      = null;
  var poll_interval = null;
  var timer         = null;
  var cache         = null;
  var zooming       = false;
  var context_menu  = null;
  
  var events        = {}; 
  events['render_event'] = new YAHOO.util.CustomEvent("renderEvent"); 
  
  if (options.poll_interval){
    poll_interval = options.poll_interval;
  }
  
  if (! graph_options){
    if (options.is_overview){
      graph_options = DEFAULT_OVERVIEW_CONFIG;     
    }
    else{
      graph_options = DEFAULT_GRAPH_CONFIG;
      graph_options.legend.container = legend_div;
    }
  }
  
  // pass it along to the YUI flot stuff inside
  if (options.title){
    graph_options.title = options.title;
  }
   

  // figure out how many columns we are able to show. 200 is just a high guesstimate of how wide
  // the columns are going to be
  if (legend_div){
    var legend_el = legend_div;

    if (typeof legend_div === "string"){
      legend_el = document.getElementById(legend_div);
    }

    graph_options.legend.noColumns = Math.max(3, parseInt(legend_el.offsetWidth / 200) - 1);  
  }

  
  if (! options.is_overview){
    
    var el = YAHOO.util.Dom.get(holder_div);
    
    var context_name = 'graphcontextmenu' + el.id;
        
    context_menu = new YAHOO.widget.ContextMenu(context_name, 
						{itemdata: ["Get as PNG",
							    "Get as CSV"],    
						 trigger: holder_div
						});
  
    context_menu.subscribe("click", function(p_sType, p_aArgs, myself){
			     
			     var clickIndex = p_aArgs[1].index;
			     
			     var dataurl;
			     
			     var width  = YAHOO.util.Dom.get(holder_div).offsetWidth + 20;
			     var height = YAHOO.util.Dom.get(holder_div).offsetHeight + 20;
			     
			     // to PNG, it's the first option
			     if (clickIndex == 0){
			       dataurl = myself.to_png();
			     }
			     // to CSV
			     else{
			       dataurl = myself.to_csv();
			     }
			     
			     if (! dataurl){
			       return;
			     }
			     
			     var childWindow = window.open(dataurl, "Graph", "menubar=no,toolbar=no,scrollbars=yes,width="+(width+20)+",height="+(height+20));
			     childWindow.focus();
			     
			   },
			   this);

    context_menu.render(document.body);      
  }

  // if stuff moves around, we need to redraw where the status ball is  
  YAHOO.util.Event.on(window, 'resize', function(args, self){
			if (self.status_image){
			  if (YAHOO.util.Dom.hasClass(self.status_image, self.UPDATE_SUCCESS)){
			    self.set_status_image(self.UPDATE_SUCCESS);
			  }
			  else{
			    self.set_status_image(self.UPDATE_FAILURE);
			  }

			}
		      }, this);

  /**
   * Returns the actual internal graph so you can make calls to its API.
   * @method get_graph
   * @return YUI-Flot-Graph
   */
  this.get_graph = function(){
    return graph;
  };
  
  this.destroy_canvas = function(){
    if (overview){
      overview.destroy();
      overview = null;
    }

    if (graph){
      graph.destroy();
      graph = null;
    }

  };
  
  this.destroy_peripherals = function(){
    this.set_status_image(null);  
  };
  
  this.destroy = function(){
    this.cancel_polling();
    
    this.destroy_peripherals();
    this.destroy_canvas();
    
    if (context_menu){
      context_menu.destroy();
    }
    
    datasource = null;
    ds_parameters = null;
    active_ds = null;
    holder_div = null;
    legend_div = null;
    cache = null;
    graph_options = null;
    options = null;
    
    events['render_event'].unsubscribeAll();   
    
  };
  
  /**
   * Returns the array of names of currently active datasources being shown on the graph.
   * @method get_active_datasources
   * @return array [strings]
   */
  this.get_active_datasources = function(){
    return active_ds;
  };
  
  this.update_polling_interval = function(new_value){
    poll_interval = new_value;
  };
  
  /**
   * Updates a parameter that will be passed in when the datasource sends it request.
   * @method update_parameter
   * @param parameter {string} The name of the parameter to update
   * @param value     {string} The new value for the parameter
   * @return void
   */
  this.update_parameter = function(parameter, value){
      ds_parameters[parameter] = value; 
  };
  
  /**
   * Updates a parameter that will be passed to the YUI flot object.
   * @method update_parameter
   * @param name   {string} The name of the option to update, same as in the constructor
   * @param value  {string} The new value for the option
   * @return void
   */
  this.update_graph_option = function(name, value){
      graph_options[name] = value;
  };
  
  /**
   * Returns a parameter that will be passed to the YUI flot object.
   * @method update_parameter
   * @param name   {string} The name of the option to whose value to return
   * @return variable
   */
  this.get_graph_option = function(name){
    return graph_options[name];
  };
  
  /**
   * Returns the value associated with the given parameter for the datasource.
   * @method get_parameter
   * @param parameter {string} The name of the parameter whose value to return
   * @return string
   */
  this.get_parameter = function(parameter){
    return ds_parameters[parameter];
  };
  
  /**
   * Updates the Y-axis maximum value on the graph. Can also redraw the graph.
   * @method set_y_axis_max
   * @param new_value {integer} The new max y-axis value
   * @param redraw    {boolean} Whether to do a redraw or not. Defaults to no.
   * @return void
   */
  this.set_y_axis_max = function(new_value, redraw){
    graph_options.yaxis.max = new_value;
    graph_options.yaxis.min = 0;
    if (redraw){
      this.redraw();
    }
  };
  
  /**
   * Updates what active sources are being drawn on the graph. Causes a redraw.
   * @method change_active_datasources
   * @param new_datasources {array [strings]} Array of datasource names to display on the graph.
   * @return void
   */
  this.change_active_datasources = function(new_datasources){
    active_ds = new_datasources;
    
    this.destroy_canvas();
    
    if (cache){
      this._drawGraph(null, cache);      
    }
    else{
      this.render();
    }    
  };
  
  /**
   * Updates the YUI datasource that the graph is using. Optionally can choose not to update the overview as well,
   * if applicable, to emulate "zooming".
   * @method change_datasource
   * @param new_datasource {YUI Datasource} The new datasource to fetch data with.
   * @param keep_overview  {boolean} Whether to apply the datasource to the overview as well or leave it alone.
   * @return void
   */
  this.change_datasource = function(new_datasource, keep_overview){
    datasource = new_datasource;
    zooming    = keep_overview;
    this.render();
  };
  
  /**
   * Cancels any polling that the graph might be doing to update itself.
   * @method cancel_polling
   * @return void
   */
  this.cancel_polling = function(){
    if (timer)
      clearTimeout(timer);
  };
  
  /**
   * Simply redraws the graph. Useful for if you have resized it or changed a viewbale datasource or something.
   * @return void
   */
  this.redraw = function(skip_render_event, vertical_padding){
    this.destroy_canvas();
    this._drawGraph(null, cache, skip_render_event, vertical_padding);
  };

  /**
   * The main function for any graph. Causes the graph to fetch its data and draw itself, its legend, and overview.
   * @method render
   * @param keep_overview {boolean} Whether to not apply the render to the overview as well.
   * @return void
   */
  this.render = function(keep_overview){
    
    this.cancel_polling();

    var old_graph, old_overview;

    // remember these since we'll need to destroy them at the end
  
    if (graph)
      old_graph = graph;
    if (overview && ! keep_overview)
      old_overview = overview;

    zooming = keep_overview;

    // if it's an overview, we already have the data
    if (options.is_overview){
      this._drawGraph(null, datasource);
      
      if (old_overview){
	old_overview.destroy();					  
      }
    }
    else{
      var params = "";

      if (ds_parameters){
	var arr = [];
	
	for (var i in ds_parameters){
	  if (i == "toJSONString") continue; // json.js adds this to every object, skip it
	  arr.push(i + "=" + ds_parameters[i]);
	}
	
	params = arr.join("&");
      }
      
      datasource.sendRequest(params, {success: function(req, resp, old_graphs){
					if (poll_interval && !zooming){
					  this.set_status_image(this.UPDATE_SUCCESS);
					}
					else{
					  this.set_status_image(null);
					}
					  
					this._drawGraph(req, resp);					
					
					if (old_graphs['graph']){
					  old_graphs['graph'].destroy();
					}

					if (old_graphs['overview']){
					  old_graphs['overview'].destroy();
					}
					
					old_graphs = null;
					
				      },
				      failure: function(req, resp){		
					if (debug)					   
					  alert("Failed to fetch data!\n" + YAHOO.lang.dump(resp));
					
					// still fire the render event because it's likely someone has
					// hooked into it
					events['render_event'].fire();
					
					if (poll_interval){
					
					  this.set_status_image(this.UPDATE_FAILURE);
					  
					  // if we failed to poll, try try again  
					  if (options.poll_callback){
					    this.start_polling();
					  }
					
					}
					else{
					  this.set_status_image(null);
					}
				      },
				      scope: this,
				      argument: {graph: old_graph, overview: old_overview}
				     }, null);          
    }
    old_graph = null;
    old_overview = null;
  };
  
  this.start_polling = function(){
    timer = setTimeout(function(graph){
			 return function(){	
			   options.poll_callback(graph);			     
			   graph.render();
			 };
		       }(this), poll_interval);
  };
  
  this._getSeriesData = function(response){
    var seriesData = [];
    
    var stats = response.meta.statistics;

    // in case we're overlaying
    var below = new Array();
    var last_below = true;
    
    // in case we're stacking
    var previous_totals = new Array();   
        
    // set up objects for flot-yui chart
    for (var i=0; i < response.meta.schema.length; i++){      
           
      var series = response.meta.schema[i];      
      
      var data = response.results[0][series];      
      
      if (graph_type == "overlay" || graph_type == "stacked"){
	
	var s_split = series.split(" => ");
	var c_name  = s_split[0];
	var ds_name = s_split[1];

	// same as above
	if (active_ds.indexOf(ds_name) == -1)
	  continue;
	
	if (previous_totals[ds_name] == undefined){
	  previous_totals[ds_name] = new Array();
	}
	
	// if we're stacking we need to adjust the numbers to account
	// for everything that came before (below) this area
	if (graphType == "stacked"){
	  data = data.map(function(val, ind, obj){
			    var offset = previous_totals[ds_name][ind];
			    if (isNaN(offset)){
			      offset = 0;
			    }
			    if (isNaN(previous_totals[ds_name][ind])){
			      previous_totals[ds_name][ind] = 0;
			    }  
			    previous_totals[ds_name][ind] += val[1];
			  
			    return [val[0], val[1] + offset, offset];
			  });
	}
     	  
	if (below[ds_name] == undefined){
	  last_below = ! last_below;
	  below[ds_name] = last_below;	  
	}
	
	// if this is one we decided to graph on the bottom, remap all its values by -1 to flip it
	if (below[ds_name] == true){
	  data = data.map(function(val, ind, obj){
			    return [val[0], val[1] * -1, val[2] * -1];
			  });	  
	}	
      }
    
      var opts = {data: data,
		  control: "time",
		  label: response.meta.labels[series],
		  name: series		  
		 };
	
      if (graph_type == "normal" && active_ds.indexOf(series) == -1){
	opts.disabled = 1;
      }
      
      if (response.meta.colors && response.meta.colors[series]){
	opts.color = response.meta.colors[series];
      }
            
      if (response.meta.fill && response.meta.fill[series]){
	opts.lines = {fill: .6, lineWidth:0};    
	seriesData.unshift(opts);
      } 
      else{
	seriesData.push(opts);
      }      
    }
    
    return seriesData;
  };
  
  /**
   * Private method that does the actual drawing, figuring out which datasources to show. Handles setting up things like
   * stacked datasources, etc.
   * @method _drawGraph
   * @param request {object} The request object, same as in the description for a YUI callback
   * @param response {object} The response object, same as in the description for a YUI callback
   * @return void
   */
  this._drawGraph = function(request, response, skip_render_event, vertical_padding){
    
    if (!response){
      return;
    }       
    
    if (poll_interval && ! zooming){    
      this.cancel_polling();

      if (options.poll_callback){	
	this.start_polling();
      }
      
    }
     
    var seriesData = this._getSeriesData(response);
    
    graph_options.vertical_padding = vertical_padding || 0;
    
    // make graph    
    graph = new YAHOO.widget.Flot(holder_div, seriesData, graph_options);      

    graph_options.vertical_padding = null;

    if (options.overview_div && ! zooming){
      // if we've got an overview specified, make it in the container given.
      // we pass in the same response we got for the main graph so we don't requery
      // for the same data.

      overview = new Graph(options.overview_div,
			   null,
			   response,
			   {is_overview       : true,
			    active_datasources: active_ds,
			    graph_type        : graph_type}
			   );

      overview.render();      
    }
    
    if (legend_div && options.legend_handler){
      
      var legend_elements = YAHOO.util.Dom.getElementsByClassName("legendLabel", "", legend_div);
	
      for (var j = 0; j < seriesData.length; j++){

	if (seriesData[j].disabled == 1)
	  continue;
	
	var series        = seriesData[j].name;
	
	var series_stats  = response.meta.statistics[series];
	var element       = legend_elements.shift();
	
	options.legend_handler(element, series, series_stats);
      }                 
    }
    
    if (options.hover_handler){
      graph.subscribe("plothover", options.hover_handler);
      
      // fire the event handler at the farthest leftmost point so that the graph legends
      // update off the bat rather than waiting for a mouse move event to trigger them
      if (graph.getAxes().xaxis.min)      
	options.hover_handler({pos: {x: graph.getAxes().xaxis.min, y: graph.getAxes().yaxis.min}}); 
    }

    if (options.selection_handler)
      graph.subscribe("plotselected", options.selection_handler);    
    
    // save the response in case we're going to be adding / removing datasources from view
    cache = response;
    
    if (! skip_render_event)
      events['render_event'].fire();

  };
    
  /**
   * Refreshes the graph, causing new data to be fetched and drawn.
   * @method refresh
   * @return void
   */
  this.refresh = function(){
    this.render();
  };
  
  /**
   * Returns a reference to the graph's overview Graph object
   * @method get_overview
   * @return Graph
   */
  this.get_overview = function(){
    return overview;
  };
  
  /**
   * Returns the data structure representing the graph's contents
   * @method get_data
   * @return object
   */
  
  this.get_data = function(){
    return graph.getData();
  };
  
  /**
   * Designed to mimic's YUI's subscription system, lets you bind callbacks to a graph's events
   * @method on
   * @param event_name {string} Name of the event you want to listen to
   * @param callback   {function} Function to be called when the event happens.
   * @return void
   */
  this.on = function(event_name, callback){  
    if (events[event_name]){
      events[event_name].subscribe(callback);
    }
  };
  
  this.set_status_image = function(class_name){

    if (this.status_image){
      this.status_image.destroy();
    }
    
    if (class_name){
            
      var status_img = document.createElement('div'); 
    
      var el = new YAHOO.util.Element(status_img);
      YAHOO.util.Dom.addClass(el, "graph-status-image");
      
      var border_color;
      if (class_name == this.UPDATE_FAILURE){
	border_color = "#990000";
	status_img.title = "Not Streaming Data";
      }
      else{
	border_color = "#00AA00";
	status_img.title = "Streaming Data";
      }     
      
      YAHOO.util.Dom.addClass(el, class_name);
            
      el.setStyle("width", "13px");
      el.setStyle("height", "13px");
      el.setStyle("position", "absolute");
      el.setStyle("border", "1.5px " + border_color + " solid");
      el.appendTo(document.body);
      
      el.on('click', function(event, graph_obj){	      
	      // if we're green, change to red and cancel
	      if (class_name == graph_obj.UPDATE_SUCCESS){
		graph_obj.cancel_polling();
		graph_obj.set_status_image(graph_obj.UPDATE_FAILURE);
	      }
	      // otherwise change to green and redraw
	      else{
		if (options.poll_callback)
		  options.poll_callback(graph_obj);
		  graph_obj.render();
	      }
	      
	    }, this);
      
      var region = YAHOO.util.Region.getRegion(YAHOO.util.Dom.get(holder_div));      
      YAHOO.util.Dom.setX(el, region.right - 26);
      
      var title_length = this.get_graph_option('title').split('\n').length;
      
      YAHOO.util.Dom.setY(el, region.top + (15 * title_length));
      
      this.status_image = el;
    }   
   
  };
  
  /**
   * Exports the current graph's data into CSV.
   * @return URI
   */
  this.to_csv = function(){
    
    if (! cache){
      return;
    }
    
    var seriesData = this._getSeriesData(cache);

    // don't include ninetyfifth repeated over and over again
    for (var i = seriesData.length - 1; i > -1; i--){
      if (seriesData[i].name == "ninetyfifth"){
	seriesData.splice(i, 1);
      }
    }
    
    // grab all the names (prefaced with time) for the columns
    var csv_string = "time," + seriesData.map(function(val,ind,obj){
						return seriesData[ind].name;
					      }).join(",");    
    
    var ds_name = seriesData[0].name;
    
    // iterate through each series so we can build up a nice table
    for (var timeIndex = 0; timeIndex < seriesData[0].data.length; timeIndex++){
      
      var rowValues = [];
      
      // grab the time out
      var timeValue = seriesData[0].data[timeIndex][0];
      
      if (! timeValue){
	continue;
      }
      
      rowValues.push(timeValue);
      
      // now grab each value
      for (var dsIndex = 0; dsIndex < seriesData.length; dsIndex++){

	var dsValue = seriesData[dsIndex].data[timeIndex][1];
	
	if (isNaN(dsValue) || dsValue == null){
	  dsValue = "null";     
	}
	else{
	  dsValue = round(dsValue);
	}
	
	rowValues.push(dsValue);	
      }
      
      csv_string += "\n" + rowValues.join(",");      
    }
    
    return "data:text/plain;charset=utf-8," + encodeURIComponent(csv_string);
  };
  
  /**
   * 
   * Exports the current graph's data URL as a PNG string. Used to take a snapshot of the current graph.
   * Legend gets redrawn into the graph so that is is exported with it.
   * @method to_png
   * @return URI
   * 
   */
  this.to_png = function(){   
    
    // I bet you can guess what this is for
    if (! graph.getCanvas().toDataURL){
      alert("Canvas is not supported in your browser properly.");
      return null;
    }

    var stats = {};

    // since the canvas version won't have access to the same callbacks, we need to
    // give it the human readable versions pre-calculated
    for (var i = 0; i < cache.meta.schema.length; i++){
      
      var series = cache.meta.schema[i];

      if (cache.meta.statistics[series]){
	      
	stats[series] = {};
      
	stats[series]['average'] = convertToHumanReadable(cache.meta.statistics[series]['average']);
	stats[series]['current'] = convertToHumanReadable(cache.meta.statistics[series]['current']);
	stats[series]['max']     = convertToHumanReadable(cache.meta.statistics[series]['max']);
      }

      // ninetyfifth is special since it's not an actual datasource, it's calculated on the fly
      // client side and thus doesn't appear in the statistics
      else if (series == "ninetyfifth"){
	stats[series] = {};

	stats[series]['average'] = convertToHumanReadable(cache.results[0][series][0][1]);
	stats[series]['current'] = convertToHumanReadable(cache.results[0][series][0][1]);
	stats[series]['max']     = convertToHumanReadable(cache.results[0][series][0][1]);

      }
            
    }
    
    // ** the next few steps are kind of confusing
    
    // how much spacing we need to add into the canvas element to put the legend
    var padding = 200 * (1 + (active_ds.length / graph_options.legend.noColumns));
    
    var holder = YAHOO.util.Dom.get(holder_div);

    var old_height = parseInt(YAHOO.util.Dom.getStyle(holder, 'height'),10);
  
    // we have to temporarily resize the containing element to bigger than it used to be
    // to include the legend
    YAHOO.util.Dom.setStyle(holder, 'height', old_height + padding + 'px');

    // whenever you resize a canvas element, you have to redraw the canvas contents or it will bork.
    // This is sort of a waste but I believe it just needs to be done. However, we re-draw
    // it including the extra padding so that the graph doesn't expand to the full size, leaving
    // room in the canvas element for the legend.
    this.redraw(null, padding);
  
    // now go ahead and draw the canvas based legend, it should fit due to padding
    graph.drawCanvasLegend(stats);    
    
    // grab the data url of this new 100% canvas graph
    var dataURL = graph.getCanvas().toDataURL();

    // resize the holder back to its original size
    YAHOO.util.Dom.setStyle(holder, 'height', old_height + 'px');
  
    // redraw the element again. All this should happen fast enough that it is transparent
    // to the end user
    this.redraw();
  
    return dataURL;

  };
  
}