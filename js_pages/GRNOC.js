/**
 * @module GRNOC
 */

/**
 * GRNOC widgets
 * @class GRNOC
 * @namespace GRNOC
 */

if (typeof GRNOC=="undefined"||!GRNOC){
  var GRNOC = {};
}

if (typeof YAHOO=="undefined"||!YAHOO){
  var YAHOO = {};
}

if (typeof Y=="undefined"||!Y){
  var Y = {};
}

/**
 * 
 */

GRNOC.requires = new Array();

/**
 * Fired when the YUI Loader has successfully loaded all JS
 * @event loaded
 */

GRNOC.loaded = new YAHOO.util.CustomEvent('loaded',this);

/**
 * Creates a YUI Loader and loads all the JS required
 * On success fires the loaded event
 * @method init
 * @param {array} an array of strings of Required modules
 * @param {string} yui_path the path to YUI (optional)
 */

GRNOC.init = function(){

  if(this.yui_path === undefined){
    this.yui_path = '/yui/2.8.0r4/';
  }

  GRNOC.requires.push("history");
  GRNOC.requires.push(function(Y3){
			Y = Y3;
			YAHOO = Y3.YUI2;
			GRNOC.history = new Y.HistoryHash();
			GRNOC.loaded.fire();
		      });
  
  var tmp = YUI({ base: '/yui/3.4.0/',
		  combine: true,
		  comboBase: '/yui/combo.cgi?',
		  root: 'path=3.4.0/',
		  loadOptional: true,
		  groups: {
		    yui2:{
		      combine: false,
		      ext: true,
		      base: "/yui/3.4.0/yui2/",
		      comboBase: '/yui/combo.cgi?',
		      root: 'path=3.4.0/yui2/',
		      patterns:{
			'yui2-':{
			  configFn: function(me){
			    if (/-skin|reset|fonts|grids|base/.test(me.name)) {
			      me.type = 'css';
			      me.path = me.path.replace(/\.js/, '.css');
			      // this makes skins in builds earlier than
                              // 2.6.0 work as long as combine is false
                              me.path = me.path.replace(/\/yui2-skin/,'/assets/skins/sam/yui2-skin');
			    }
			  }
			}
		      }
		    },
		    gallery: {
		      combine: true,
		      comboBase: '/yui/combo.cgi?',
		      base: "/yui/gallery/",
		      root: 'path=gallery/',
		      modules:{
			accordion: {
			  path: "accordion/accordion-min.js",
			  requires: [ "event",
				      "anim-easing",
				      "widget",
				      "widget-stdmod",
				      "json-parse",
				      "skin-sam-accordion"
				    ],
			  optional: [ "dd-constrain",
			              "dd-proxy",
				      "dd-drop"
				    ]
			},
			"skin-sam-accordion":{
			  path: "accordion/assets/skins/sam/accordion.css",
			  type: "css"
			}
		      }
		    },
		    GRNOC: {
		      combine: true,
		      base: '/glue/index.cgi?method=get_files',
		      comboBase: '/glue/index.cgi?method=get_files',
		      root: '&path=',
		      modules: {
			GRNOC_DataTable_1:{
			  path: "GRNOC/widget/DataTable/1/DataTable.js",
			  requires: ["yui2-datatable","yui2-paginator"],
			  type: 'js'
			},
			GRNOC_DataSource_1:{
			  path: "GRNOC/util/DataSource/1/DataSource.js",
			  requires: ["yui2-datasource","yui2-connection"],
			  type: 'js'
			},
			GRNOC_KeyValueTable_1:{
			  path: "GRNOC/widget/KeyValueTable/1/KeyValueTable.js",
			  requires: ["GRNOC_DataTable_1", "yui2-button"],
			  type: 'js'
			},
			Pop_1:{
			  path: "GRNOC/widget/Pop/1/POP.js",
			  requires: ["GRNOC_DataTable_1","GRNOC_DataSource_1"],
			  type: 'js'
			},
			Basic_1:{
			  path: "GRNOC/widget/Basic/1/Basic.js",
			  requires: ["yui2-menu","yui2-animation","Basic_1_css","AdminNetwork_1","scrollview","scrollview-paginator","transition","panel"],
			  type: 'js'
			},
			Basic_1_css:{
			  path: "GRNOC/widget/Basic/1/Basic.css&type=css",
			  type: 'css'
			},
			Circuit_1:{
			  path: "GRNOC/widget/Circuit/1/Circuit.js",
			  requires: ["GRNOC_DataSource_1","GRNOC_KeyValueTable_1"],
			  type: "js"
			},
			Node_1:{
			  path: "GRNOC/widget/Node/1/Node.js",
			  requires: ["yui2-element","yui2-dom","GRNOC_DataSource_1","GRNOC_DataTable_1"],
			  type: 'js'			  
			},
			AdminNetwork_1:{
			  path: "GRNOC/widget/AdminNetwork/1/AdminNetwork.js",
			  requires: ["GRNOC_DataSource_1","yui2-element","yui2-dom"],
			  type: 'js'
			},
			Help_1:{
			  path: "GRNOC/widget/Help/1/Help.js",
			  requires: ["GRNOC_DataSource_1","GRNOC_DataTable_1"],
			  type: 'js'
			},
			Queries_1:{
			  path: "GRNOC/widget/Queries/1/Queries.js",
			  requires: ["GRNOC_DataTable_1","GRNOC_DataSource_1"],
			  type: "js"
			},
			Locality_1:{
			  path: "GRNOC/widget/Locality/1/Locality.js",
			  requires: ["GRNOC_DataSource_1","GRNOC_DataTable_1"],
			  type: 'js'
			},
			Rack_1:{
			  path: "GRNOC/widget/Rack/1/Rack.js",
			  requires: ["GRNOC_DataSource_1","GRNOC_DataTable_1","yui2-carousel","yui2-tabview"],
			  type: "js"
			},
			Workflow_1:{
			  path: "GRNOC/widget/Workflow/1/Workflow.js",
			  requires: ["GRNOC_DataSource_1","GRNOC_DataTable_1","yui2-button","yui2-tabview",'yui2-calendar'],
			  type: "js"
			},
			GRNOC_Accordion_1_css:{
			  type: "css",
			  path: "GRNOC/widget/Accordion/1/Accordion.css&type=css"
			},
			GRNOC_Accordion_1:{
			  path: "GRNOC/widget/Accordion/1/Accordion.js",
			  requires: ["accordion","io-base","GRNOC_Accordion_1_css"],
			  type: "js"
			}
		      }
		    }
		  }
		}
	       );
  
  
  tmp.use.apply(tmp,GRNOC.requires);

};

/* Simple function to add a new namespace under the GRNOC namespace */
GRNOC.add_namespace = function(new_name){
  
  var pieces = new_name.split(".");
  var root;
  
  for (var i = 0; i < pieces.length; i++){

    if(i == 0){
      if(pieces[i] != 'GRNOC'){
	alert('Only GRNOC NameSpace is allowed');
	return;
      }
      root = GRNOC;
      continue;
    }
    
    var name = pieces[i];
    
    //have we seen this before?  if not created it
    if(! root[name]){
      root[name] = {};
    }
    
    root=root[name];
  }

};

GRNOC.add_namespace("GRNOC.util");
GRNOC.add_namespace("GRNOC.widget");
YAHOO.util.Event.onDOMReady(function(){GRNOC.init();});