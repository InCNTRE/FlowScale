<script type="text/javascript">

type = "text/javascript" > YAHOO.namespace("example.container");
var xconnect_value_table_2 = null;
var xconnect_value_table_1 = null;
var xconnect_table = null;
function Object(id){
	
	this.id = id;
}

function x_connect_create(){
	
var ds = new YAHOO.util.DataSource("webservice/data.cgi?action=xconnect_values");
    ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

    ds.responseSchema = {
	 resultsList: "results",
	  fields: [
        { key: "switch1_name"},
        {key: "switch_1_id"},
        { key: "port_1_id"},
        { key: "switch2_name"},
         {key: "switch_2_id"},
        { key: "port_2_id"}, 
    ],
	  metaFields: {
	error: "error"
      },
	};

	
	
	  var columns = [
      {key: "switch1_name", label: "Switch1", width: 150} ,
      {key: "port_1_id", label: "Port1", width: 70} ,
      {key: "switch2_name", label: "Switch2", width: 150} ,
      {key: "port_2_id", label: "Port2", width: 70} ,
      {label: "Delete", width: 50, formatter: function(el, rec, col, data){
	var del_button = new YAHOO.widget.Button({label: "Delete"});
	del_button.addClass("page_button");
	var t = this;
	del_button.on("click", function(){
			//var interface = rec.getData('interface');
			//alert("Test");
			showConfirm("Are you sure you wish to delete the x-connect from "+ rec.getData('switch1_name')+ " to "+ rec.getData('switch2_name') +" ?",
				    function(){
				    
				    var deleteDs = new YAHOO.util.DataSource("webservice/admin.cgi?action=delete_xconnect&switch_1_id="+rec.getData('switch_1_id')+"&switch_2_id="+rec.getData('switch_2_id'));
				    deleteDs.responseType = YAHOO.util.DataSource.TYPE_JSON;

                    deleteDs.responseSchema = {
                        resultsList: "results",
                        fields: [{key: "success"},{key:"error"}],
                         metaFields: {
	error: "error"
      }
                    };


deleteDs.sendRequest('', {success: function(req, resp){
	
	if(resp.results[0].success == 0){
		alert(resp.results[0].error);
		return;
	}
	
        t.deleteRow(t.getRecordSet().getRecordIndex(rec));
							
                            
                            },
                            failure: function(req, resp){
                             

                                alert("Error while deleting device.");
                            }
                        });



				    	
					
				    },
				    function(){}
				    );

		      });
	del_button.appendTo(el);
      }
    }
	];


	xconnect_table = new YAHOO.widget.DataTable('x-connect_ports', columns,ds,null);

		
	
}


function populate_ports(id,obj){
//	alert(document.getElementById('switch_1_choice').value);
	var id_value= 0;
	var s_id_value  = 0;
	if (obj == null){
		id_value = id;
		s_id_value = id;
	}	else{
		id_value = obj.id;
		s_id_value = 1;
	}
	
	var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=get_switch_ports&switch_id="+document.getElementById('switch_'+s_id_value+'_choice').value);
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "port_id"},	
        {key: "port_address"},
      ],
      metaFields: {
	error: "error"
      }
    };
	

		 var columns = [
      {key: "port_id", label: "Port", width: 30},
      {key: "port_address", label: "Port Address", width: 100},
      
      ];

		if(id_value == 1){
			 xconnect_value_table_1 = new
			YAHOO.widget.DataTable('ports_'+id_value+'_table', columns,ds,null);
			xconnect_value_table_1.set("selectionMode", "single");
			xconnect_value_table_1.subscribe("rowClickEvent", xconnect_value_table_1.onEventSelectRow);
		}else if(id_value == 2){
			 xconnect_value_table_2 = new
			YAHOO.widget.DataTable('ports_'+id_value+'_table', columns,ds,null);
			xconnect_value_table_2.set("selectionMode", "single");
			xconnect_value_table_2.subscribe("rowClickEvent", xconnect_value_table_2.onEventSelectRow);
		}
	
	
}

function add_xconnect_func(){
	

	
	var selected = xconnect_value_table_1.getSelectedRows();
	var rset = xconnect_value_table_1.getRecordSet();
	
	
	var selected2 = xconnect_value_table_2.getSelectedRows();
	var rset2 = xconnect_value_table_2.getRecordSet();
	
	var port_1 = rset.getRecord(selected[0]).getData('port_id');
	var port_2 =  rset2.getRecord(selected2[0]).getData('port_id');

	var switch_1 = document.getElementById('switch_1_choice').value;
	
	var switch_2 =document.getElementById('switch_2_choice').value;
	
	
	
	
	   var addDs = new YAHOO.util.DataSource("webservice/admin.cgi?action=add_xconnect&switch_1_id="+switch_1+"&switch_2_id="+switch_2+"&port1="+port_1+"&port2="+port_2);
				    addDs.responseType = YAHOO.util.DataSource.TYPE_JSON;

                    addDs.responseSchema = {
                        resultsList: "results",
                        fields: [{key: "success"}]
                    };


addDs.sendRequest("", {success: function(req, resp){

 
 	if(resp.results[0].success == 0){
		alert(resp.results[0].error);
		return;
	}
 
xconnect_table.getDataSource().sendRequest('',
{ success: xconnect_table.onDataReturnInitializeTable,scope: xconnect_table});


							
                            
                            },
                            failure: function(req, resp){
                             

                                alert("Error while adding x-connect.");
                            }
                        });



}



function init(){

YAHOO.util.Event.onContentReady("add_xconnect", function(){ 
	var add_button = new YAHOO.widget.Button("add_xconnect",{label: "Add X-Connect"});
	
	add_button.addClass("button_panel");
	});
	

YAHOO.util.Event.onContentReady("x-connect_ports",function(){try{x_connect_create();}catch(e){alert(e);}});



YAHOO.example.container.add_panel = new YAHOO.widget.Panel("add_panel", {
		width : "320px",
		visible : false,
		constraintoviewport : true
	});
	YAHOO.example.container.add_panel.render();



YAHOO.util.Event.onContentReady("add_xconnect", function() {
		var add_button = new YAHOO.widget.Button("add_xconnect", {
			label : "Add X-Connect"
		});
		add_button.addClass("page_button");
		YAHOO.util.Event.addListener("add_xconnect", "click", YAHOO.example.container.add_panel.show, YAHOO.example.container.add_panel, true);
		var object = new Object(1);
		YAHOO.util.Event.addListener("add_xconnect", "click", populate_ports,object , true);
		object = new Object(2);
		YAHOO.util.Event.addListener("add_xconnect", "click", populate_ports,object , true);
	});





YAHOO.util.Event.onContentReady("submit_new_xconnect", function() {
		var add_button = new YAHOO.widget.Button("submit_new_xconnect", {
			label : "Add"
		});
		add_button.addClass("button_panel");
			YAHOO.util.Event.addListener("submit_new_xconnect", "click", add_xconnect_func,null , true);
		
	});
	
	

YAHOO.util.Event.onContentReady("clear_new_xconnect", function() {
		var clear_button = new YAHOO.widget.Button("clear_new_xconnect", {
			label : "Clear"
		});
		clear_button.addClass("button_panel");
	});
	



}


YAHOO.util.Event.addListener(window, "load", init);


</script>
