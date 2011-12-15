




<script>
/*
	YAHOO.namespace("example.container");	




	
function index_init(){

var ds = new YAHOO.util.DataSource("services/test.cgi");
    ds.responseType = YAHOO.util.DataSource.TYPE_TEXT;

    ds.responseSchema = {
	resultsList: "results",
	  fields: [
        { key: "port_id"}
        
    ],
	recordDelim: "\n",
	fieldDelim: "\n",
	  metaFields: {
	error: "error"
      },
	};
	var fullResponse;
	
	  var columns = [
      {key: "port_id", label: "ports", width: 300} ,
      {key: "port_status", label: "Status", width: 300} 
	];


	var port_table = new YAHOO.widget.DataTable('port_table', columns,ds,null);
	port_table.set("selectionMode", "cellrange");
port_table.subscribe("cellClickEvent", port_table.onEventSelectCell);


	  var columns2 = [
      {key: "port_id", label: "ports", width: 300},
      {key: "port_status", label: "Status", width: 300} ,
	];


	var port_table2 = new YAHOO.widget.DataTable('port_table2', columns,ds,null);
	port_table2.set("selectionMode", "cellrange");
port_table2.subscribe("cellClickEvent", port_table2.onEventSelectCell);



}

YAHOO.util.Event.onContentReady("port_table2",function(){try{index_init();}catch(e){alert(e);}});


function x_connect_create(){
	
var ds = new YAHOO.util.DataSource("services/test2.cgi");
    ds.responseType = YAHOO.util.DataSource.TYPE_TEXT;

    ds.responseSchema = {
	resultsList: "results",
	  fields: [
        { key: "Switch"},
        { key: "Port"},
        
    ],
	recordDelim: "\n",
	fieldDelim: ",",
	  metaFields: {
	error: "error"
      },
	};
	var fullResponse;
	
	  var columns = [
      {key: "Switch", label: "Switch", width: 300} ,
      {key: "Port", label: "Port", width: 300} 
	];


	var xconnect_table = new YAHOO.widget.DataTable('x-connect_ports', columns,ds,null);
	xconnect_table.set("selectionMode", "cellrange");
xconnect_table.subscribe("cellClickEvent", xconnect_table.onEventSelectCell);
	
	var xconnect_table = new YAHOO.widget.DataTable('x-connect_ports2', columns,ds,null);
	xconnect_table.set("selectionMode", "cellrange");
xconnect_table.subscribe("cellClickEvent", xconnect_table.onEventSelectCell);
	
		
	
}

YAHOO.util.Event.onContentReady("x-connect_ports2",function(){try{x_connect_create();}catch(e){alert(e);}});

//now let's define some a button :



function load_switch_accordion(){
	

var menu6 = new YAHOO.widget.AccordionView('mymenu6', {collapsible: true, expandable: true, width: '70%', animate: true, animationSpeed: '0.1'});		
		var myPanels = 
		[
		{label: 'Switch 1 ', content: '<span><button id="edit_switch_1"></button></span>'+
		'<span><button id="delete_switch_1"></button></span>'+
		'<div id="switch_information"> '+
		'<b> Ports </b> <br />'+
		'<label for="mac_address">MAC Address</label> <span id="mac_address">00:00:00:00:00:05</span>'+
		'<label for="datapath_id">Datapath ID</label> <span id="datapath_id">01234ABCDE</span>'+
		'<label for="switch_ip">IP Address</label> <span id="switch_ip">156.56.5.41</span>'+
		'<div id ="port_table"></div>'+
		'<b>X-Connect Ports</b>'+
		'<div id="x-connect_ports"></div>'},
		
		{label: 'Switch 2 ', content: '<span><button id="edit_switch_2"></button></span>'+
		'<span><button id="delete_switch_2"></button></span>'+
		'<div id="switch_information"> '+
		'<b> Ports </b> <br />'+
		'<label for="mac_address">MAC Address</label> <span id="mac_address">00:00:00:00:00:06</span>'+
		'<label for="datapath_id">Datapath ID</label> <span id="datapath_id">01234ABCDE</span>'+
		'<label for="switch_ip">IP Address</label> <span id="switch_ip">156.56.5.31</span>'+
			'<div id ="port_table"></div>'+
		'<b>X-Connect Ports</b>'+
		'<div id ="port_table2"></div>'+
		'<div id="x-connect_ports2"></div>'},
		
		 ];
		
		menu6.addPanels(myPanels);
		menu6.appendTo('mycontainer');
	
}

YAHOO.util.Event.onContentReady("mycontainer",function(){try{load_switch_accordion();}catch(e){alert("accordion "+e);}});


YAHOO.util.Event.onContentReady("delete_switch_1", function(){ var del_button = new YAHOO.widget.Button("delete_switch_1",{label: "Delete"});
del_button.on("click", function(){

showConfirm("Are you sure you wish to delete the switch ?",
				    function(){
					alert("deleted");
				    },
				    function(){}
				    );

});
});


YAHOO.util.Event.onContentReady("delete_switch_2", function(){ var del_button = new YAHOO.widget.Button("delete_switch_2",{label: "Delete"});
del_button.on("click", function(){

showConfirm("Are you sure you wish to delete the switch ?",
				    function(){
					alert("deleted");
				    },
				    function(){}
				    );

});
});


YAHOO.util.Event.onContentReady("edit_switch_1",
 function(){
 	 var edit_button = new YAHOO.widget.Button("edit_switch_1",{label: "Edit"});
 	 edit_button.on("click", function() {show_edit_pane();});
 	 
 	 
 	}
 	
 );

YAHOO.util.Event.onContentReady("edit_switch_2", 
function(){
	 var edit_button = new YAHOO.widget.Button("edit_switch_2",{label: "Edit"});
	 edit_button.on("click",function() {show_edit_pane();});

}
);

YAHOO.util.Event.onContentReady("edit_switch_panel", function(){

YAHOO.example.container.editPanel = new YAHOO.widget.Panel("edit_switch_panel", {
    width: "400px", 
    fixedcenter: false, 
    constraintoviewport: true, 
    underlay: "shadow", 
    close: true, 
    visible: true, 
    draggable: false,
    
   
});


YAHOO.util.Event.addListener("edit_switch_1", "click", YAHOO.example.container.editPanel.show, YAHOO.example.container.editPanel, true);
});

YAHOO.util.Event.onContentReady("add_switch", function(){ var add_switch_button = new YAHOO.widget.Button("add_switch",{label: "Add Switch"});






add_switch_button.on("click",function(){show_add_pane();});


}
);



function show_add_pane(){
	

		myPanel = new YAHOO.widget.Panel("switch_panel", {
    width: "400px", 
    fixedcenter: false	, 
    constraintoviewport: false, 
    underlay: "shadow", 
    close: true, 
    visible: true, 
    draggable: false,
   
});

myPanel.setBody('<p>Add Switch </p>'+
'Switch IP address : <input type="text" size=20 /> <br />'+
'Switch MAC Address : <input type="text" size=20 /> <br />'+
'Switch Datapath ID: <input type="text" size=20 /> <br />'+
'<button id="add_switch_button"> Add </button> <button id="add_switch_button"> clear  </button>');
myPanel.render();

}





function show_edit_pane(){		

	
YAHOO.example.container.editPanel.setBody('<p>edit switch </p>'+
'Switch IP address : <input type="text" size=20 /> <br />'+
'Switch MAC Address : <input type="text" size=20 /> <br />'+
'Switch Datapath ID: <input type="text" size=20 /> <br />'+
'<button id="add_switch_button"> Add </button> <button id="add_switch_button"> clear  </button>');

YAHOO.example.container.editPanel.render();


	
}

function clickForPanel(){
	
	
	
}

*/
</script>


