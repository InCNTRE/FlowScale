<script type = "text/javascript" >

var rules_table;
var edit_group_value_table;
var input_table_edit;
var output_table_edit;

var input_table_add;
var output_table_add;
var addedTablePorts = new Array();
var addedTableRules= new Array();
var index_panel = 0;

var arrayIndex = 0;
var accordionView;

 YAHOO.namespace("example.container");

function Group(id, input_ports_table, output_ports_table ,group_values_table,index) {

	this.id = id;
	this.input_ports_table = input_ports_table;
	this.output_ports_table = output_ports_table; 
	this.group_values_table = group_values_table;
	this.index = index;
}

function delete_button(e, obj) {

	showConfirm("Are you sure you wish to delete the group ?", function() {
	
	var ds =  new YAHOO.util.DataSource("webservice/admin.cgi?action=delete_group&group_id="+obj.id);



 ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

                    ds.responseSchema = {
                        resultsList: "results",
                        fields: [{key: "success"},{key:"error"}],
                         metaFields: {
	error: "error"
      }
                    };


ds.sendRequest('', {success: function(req, resp){
      
      if(resp.results[0].success == 0){
     	alert(resp.results[0].error);
      }else{
      	 	alert("group is deleted");
      	 	 
      	  accordionView.removePanel(obj.index);
      	index_panel--;
      }
     },
              failure: function(req, resp){
                             

                                alert("Error while deleting  Group.");
      //                          alert(resp.error);
                            }
                        });
	

	
	
}, function() {
	});

}
function edit_button_func(e, obj) {

	//alert("test" +element.getElementsByTagName('span').namedItem('mac_address').firstChild.innerHTML);

//populate fields 	
document.getElementById('edit_group_id').value = obj.id;
document.getElementById('edit_group_name').value = trim(document.getElementById(obj.id+"_name").innerHTML);

	
var input_ports = new Array();
var output_ports = new Array();
var group_values = new Array();

for (i = 0 ; i < obj.input_ports_table.getRecordSet().getLength(); i++){
	
	
	input_ports[i] = obj.input_ports_table.getRecord(i).getData('port_id'); 
}

for (i = 0 ; i < obj.output_ports_table.getRecordSet().getLength(); i++){
	
	
	output_ports[i] = obj.output_ports_table.getRecord(i).getData('port_id'); 
}

for (i = 0 ; i < obj.group_values_table.getRecordSet().getLength(); i++){
	
	
	output_ports[i] = obj.group_values_table.getRecord(i).getData('port_id'); 
}


	
document.getElementById("edit_priority").value = document.getElementById("priority_"+ obj.id).innerHTML;


document.getElementById('original_input_ports').value = input_ports;
document.getElementById('original_output_ports').value = output_ports;

document.getElementById('original_group_values').value = group_values;


//create table for values 

var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=group_values&group_id="+obj.id);
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "group_value"},
	
      ],
      metaFields: {
	error: "error"
      }
    };
	

		 var columns = [
      {key: "group_value", label: "Value", width: 70},
      { label: "Delete", width: 70 ,
      		formatter : function(el, rec, col, data) {
			var del_button = new YAHOO.widget.Button({
				label : "Delete"
			});
			
			del_button.addClass( "button_panel");

			var t = this;
			del_button.on("click", function() {
				//var interface = rec.getData('interface');

					edit_group_value_table.deleteRow(edit_group_value_table.getRecordSet().getRecordIndex(rec));
					
			
			});
			del_button.appendTo(el);
		} }
      ];

		     var configs = {
		height : '150px',
	
	};

			 edit_group_value_table = new
			YAHOO.widget.ScrollingDataTable('edit_group_value_table', columns,ds,configs);



	YAHOO.example.container.edit_panel.show();

	



}
function trim(stringToTrim) {
	return stringToTrim.replace(/^\s+|\s+$/g,"");
}



function submit_new_group(){
	
	
	//alert("right");
	//get all elements,
	//create datasource//
	//send it to database
	
	
	
	//get ports

var input_ports = new Array();

var output_ports = new Array();

var selectedRows = input_table_add.getSelectedRows();

var inputRecordObject =  new Array();
inputRecordObject = input_table_add.getRecordSet();

for (i = 0 ; i < selectedRows.length; i++){
	
	
	input_ports[i] = inputRecordObject.getRecord(selectedRows[i]).getData('port_id'); 
}




selectedRows = output_table_add.getSelectedRows();
var outputRecordObject =  new Array();
outputRecordObject = output_table_add.getRecordSet();

for (i = 0 ; i < selectedRows.length; i++){
	
	
	output_ports[i] = outputRecordObject.getRecord(selectedRows[i]).getData('port_id'); 
}


var records = new Array();
var recordsObject =rules_table.getRecordSet();
for ( i = 0 ; i < rules_table.getRecordSet().getLength(); i++){
	records[i] = recordsObject.getRecord(i).getData('Value');
	
} 


/*	alert(document.getElementById('add_group_name').value +" input switch "+ 
	document.getElementById('add_input_switch').value + " output switch " +
	document.getElementById('add_output_switch').value + " group type  " +
	document.getElementById('add_group_type').value + "  priority " +
	document.getElementById('new_priority').value + "  group values " +
	records +" ports selected "+
	input_ports +" "+ output_ports);
	
	*/
	
	var group_name = document.getElementById('add_group_name').value;
	var input_switch = document.getElementById('add_input_switch').value;
	var output_switch = document.getElementById('add_output_switch').value;
	var group_type = document.getElementById('add_group_type').value;
	var priority = document.getElementById('new_priority').value ;
	var maximum_allowed = document.getElementById('new_maximum_flow_allowed').value;
	
	//alert(records);
	alert(maximum_allowed);
	var ds =  new YAHOO.util.DataSource("webservice/admin.cgi?action=add_group&group_name="+ group_name +"&input_switch="+input_switch+
								"&output_switch="+output_switch +"&priority="+priority+"&input_ports="+input_ports+"&output_ports="+output_ports+"&group_values="+records+"&group_type="+group_type
								+"&maximum_allowed="+maximum_allowed);

	var group_id = 0;
ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

                    ds.responseSchema = {
                        resultsList: "results",
                        fields: [{key: "success"},{key:"error"},{key:"transaction_id"}],
                         metaFields: {
	error: "error"
      }
                    };


ds.sendRequest('', {success: function(req, resp){
        
       if(resp.results[0].success == 0){
        	
        	alert(resp.results[0].error);
        	
        	return;
        	
        }
        
      
        var group_id =resp.results[0].transaction_id;
        alert("group added");
        
        //add accordion here 
       
      var body =  '<div id="[$  group.group_id $]_information">'+
'				<span><button id="edit_[$ group.group_id $]"></button></span>'+
'						<span><button id="delete_[$ group.group_id $]"></button></span>'+	'	<div id="group_headers_1" class="panel_content">'+
	'			<div id="[$ group.group_id $]_name" name="[$ group.group_id $]_name" style="visibility:hidden;"></div>'+
	'			<ul>'+
	'				<li>'+
	'					<label for="priority_[$ group.group_id $]">Priority:</label><span id="priority_[$ group.group_id $]">[$ group.priority $]</span>'+
	'				</li>'+
	'				<li>'+
	'					<label for="input_switch_[$ group.group_id $]">Input Switch: </label><span id="input_switch_[$ group.group_id $]">[$ group.input_switch_name $] </span>'+
	'				</li>'+
	'				<li>'+
	'					<label for="input_ports_[$ group.group_id  $]"> Input ports: </label>'+
	'					<div id="input_ports_[$ group.group_id $]" class="datatable">  </div>'+
	'				</li>'+
	'				<li>'+
	'					<label for="output_switch_[$ group.group_id $]"> Output Switch: </label><span id="output_switch_[$ group.group_id $]"> [$ group.output_switch_name $] </span>'+
	'				</li>'+
	'				<li>'+
	'					<label for="output_ports_[$ group.group_id $]"> Output Ports: </label>'+
	'					<div id="output_ports_[$ group.group_id $]" class="datatable"> </div>'+
	'				</li>'+
	'				<li>'+
	'					<label for="rules[$ group.group_id $]">Rules: </label>'+
	'					<div id="rules_[$ group.group_id $]" class="datatable"></div>'+
	'				</li>'+
	'			</ul>'+
	'		</div>'+
	'	</div>';
			
			
 var body1 = body.replace(/\[\$ group\.group_id \$\]/g, group_id);
		var body2 =	body1.replace(/\[\$ group\.priority \$\]/g, priority);
		var body3 =	body2.replace(/\[\$ group\.input_switch_name \$\]/g, input_switch);
		var body4 =	body3.replace(/\[\$ group\.output_switch_name \$\]/g, output_switch);
	
        attrs = {
        	label: group_name, 
        	content: body4
        	
        }
        
        
        populate_group_ports_table(group_id);
        populate_rules_table(group_id);
       arrayIndex++;
        YAHOO.example.container.add_panel.hide();
        accordionView.addPanel(attrs);
        index_panel++;
        //call the above 2 function 
   
     	YAHOO.util.Event.onContentReady("edit_"+group_id, function() {
		
			var edit_button = new YAHOO.widget.Button("edit_"+group_id, {
				label : "Edit"
			});
			edit_button.addClass("button_panel");
			var groupObject = new Group(group_id , null, null, null, index_panel);
			YAHOO.util.Event.addListener("edit_"+group_id, "click", edit_button_func, groupObject);

		});

		YAHOO.util.Event.onContentReady("delete_"+group_id, function() {
			var del_button = new YAHOO.widget.Button("delete_"+group_id, {
				label : "Delete"
				});
				del_button.addClass("button_panel");
			var groupObject = new Group(group_id, null,null,null, index_panel);
			
			YAHOO.util.Event.addListener("delete_"+group_id, "click", delete_button, groupObject);

		});

        
        
        
     
        
        //put action here     
                        },
              failure: function(req, resp){
                             

                                alert("Error while adding group");
                            }
                        });


}


function edit_group(){

//get group values;
/*var records = new Array();
var recordsObject =edit_group_value_table.getRecordSet();
for ( i = 0 ; i < edit_group_value_table.getRecordSet().getLength(); i++){
	records[i] = recordsObject.getRecord(i).getData('group_value');
	
} 
*/


var input_ports = new Array();

var output_ports = new Array();

var original_input_ports = document.getElementById('original_input_ports').value;
var original_output_ports = document.getElementById('original_output_ports').value;

var original_group_values = document.getElementById('original_group_values').value;

alert(original_input_ports +","+ original_output_ports);
var selectedRows = input_table_edit.getSelectedRows();

var inputRecordObject =  new Array();
inputRecordObject = input_table_edit.getRecordSet();

for (i = 0 ; i < selectedRows.length; i++){
	
	
	input_ports[i] = inputRecordObject.getRecord(selectedRows[i]).getData('port_id'); 
}




selectedRows = output_table_edit.getSelectedRows();
var outputRecordObject =  new Array();
outputRecordObject = output_table_edit.getRecordSet();

for (i = 0 ; i < selectedRows.length; i++){
	
	
	output_ports[i] = outputRecordObject.getRecord(selectedRows[i]).getData('port_id'); 
}


var group_values = new Array();

var value_record_set = edit_group_value_table.getRecordSet();

for (i = 0 ; i < value_record_set.getLength(); i++ ){
	
	group_values[i] = value_record_set.getRecord(i).getData("group_value");
	
}









//alert(group_values);
//alert(document.getElementById('edit_group_id').value +" "+ document.getElementById('edit_input_switch_id').value + " " +
 //document.getElementById('edit_output_switch_id').value + " "+ document.getElementById('edit_priority').value + " "+ document.getElementById('edit_group_type').value +"  "+ " " + input_ports +" "+ output_ports+" "+
//group_values 
// );
 
 
//this is the part where the database is contacted


var group_id = document.getElementById('edit_group_id').value;
var input_switch_id = document.getElementById('edit_input_switch_id').value;
var output_switch_id = document.getElementById('edit_output_switch_id').value;
var edit_priority  = document.getElementById('edit_priority').value;
var edit_type = document.getElementById('edit_group_type').value ;


var ds =  new YAHOO.util.DataSource("webservice/admin.cgi?action=edit_group&group_id="+ group_id +"&input_switch_id="+input_switch_id+
								"&output_switch_id="+output_switch_id +"&priority="+edit_priority+"&input_ports="+input_ports+"&output_ports="+output_ports+"&group_values="+group_values+"&group_type="+edit_type +
								"&original_input_ports="+original_input_ports+"&original_output_ports="+original_output_ports+"&original_group_values="+original_group_values);



 ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

                    ds.responseSchema = {
                        resultsList: "results",
                        fields: [{key: "success"},{key:"error"}],
                         metaFields: {
						error: "error"
      }
                    };
/*

ds.sendRequest('', {success: function(req, resp){
        
        if(resp.results[0].success == 0){
        	
        	alert(resp.results[0].error);
        	return;
      }
        
         YAHOO.example.container.edit_panel.hide();
        
        alert("group updated");
        
        
        var mac_string = "mac_"+ switch_id;
        var ip_string = "ip_" + switch_id;
        
        document.getElementById(mac_string).innerHTML = switch_mac_address;
        document.getElementById(ip_string).innerHTML = switch_ip_address;
          document.getElementById(switch_id+"_name").innerHTML = switch_name;
        
        //change the element values 
        
        add_xconnect_for_switch(document.getElementByIdDocument('edit_switch_id').value);
        add_ports_for_switch(document.getElementByIdDocument('edit_switch_id').value);
        
                               },
              failure: function(req, resp){
                             

                                alert("Error while updating groups.");
                            }
                        });


// end edit 




*/


	
}

function populate_ports(id,obj){
	

	var id_value= 0;
	var s_id_value  = 0;
	if (obj == null){
		id_value = id;
		s_id_value = id;
	}	else{
		id_value = obj.id;
		s_id_value = 1;
	}
	var choice ="";
	if(s_id_value ==1){
		choice = "edit_input_switch_id";
	}else if (s_id_value == 2){
		choice = "edit_output_switch_id";
	}else if (s_id_value == 3){
		choice = "add_input_switch";
	}else if (s_id_value == 4){
		choice  = "add_output_switch";		
	}
	
	
	var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=get_switch_ports&switch_id="+document.getElementById(choice).value);
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
 //     {key: "port_address", label: "Port Address", width: 100},
      
      ];

     var configs = {
//		height : '150px',
	
	};
		if(id_value == 1){
			 input_table_edit = new
			YAHOO.widget.ScrollingDataTable('edit_input_ports', columns,ds,configs);
			input_table_edit.set("selectionMode", "multi");
			input_table_edit.subscribe("rowClickEvent", input_table_edit.onEventSelectRow);
		}else if(id_value == 2){
			 output_table_edit = new
			YAHOO.widget.ScrollingDataTable('edit_output_ports', columns,ds,configs);
			output_table_edit.set("selectionMode", "multi");
			output_table_edit.subscribe("rowClickEvent", output_table_edit.onEventSelectRow);
		}
		
		
			if(id_value == 3){
			 input_table_add = new
			YAHOO.widget.ScrollingDataTable('add_input_ports', columns,ds,configs);
			input_table_add.set("selectionMode", "multi");
			input_table_add.subscribe("rowClickEvent", input_table_add.onEventSelectRow);
		}else if(id_value == 4){
			 output_table_add = new
			YAHOO.widget.ScrollingDataTable('add_output_ports', columns,ds,configs);
			output_table_add.set("selectionMode", "multi");
			output_table_add.subscribe("rowClickEvent", output_table_add.onEventSelectRow);
		}
	
	
}


 function populate_group_ports_table(group_id){
 	YAHOO.util.Event.onContentReady("input_ports_"+group_id, function(){
			
			var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=group_input_ports&group_id="+group_id);
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "port_id"},	
//        {key: "port_address"},
      ],
      metaFields: {
	error: "error"
      }
    };
	

		 var columns = [
      {key: "port_id", label: "Port", width: 30},
//      {key: "port_address", label: "Port Address", width: 100},
      
      ];
			
			     var configs = {
	//	height : '70px',
	
	};
				 addedTablePorts[arrayIndex]= new  YAHOO.widget.ScrollingDataTable('input_ports_'+group_id, columns,ds,configs);
			
		});
		
		
/***************************************************group output port table *******************************************/		
			YAHOO.util.Event.onContentReady("output_ports_"+group_id, function(){
			
			var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=group_output_ports&group_id="+group_id);
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "port_id"},	
  //      {key: "port_address"},
      ],
      metaFields: {
	error: "error"
      }
    };
	

		 var columns = [
      {key: "port_id", label: "Port", width: 30},
    //  {key: "port_address", label: "Port Address", width: 100},
      
      ];
			
			var configs = {
	//	height : '150px',
	//	width : '310px'
	};

			  addedTablePorts[arrayIndex] = new  YAHOO.widget.ScrollingDataTable('output_ports_'+group_id, columns,ds,configs);
			
		});

 	
 }
function populate_rules_table(group_id){
	
	
		YAHOO.util.Event.onContentReady("rules_"+group_id, function(){
			
			var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=group_values&group_id="+group_id);
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "group_value"},	
  
      ],
      metaFields: {
	error: "error"
      }
    };
	

		 var columns = [
      {key: "group_value", label: "Rules", width: 100},
    
      
      ];
      
      var configs = {
		height : '70px',
//		width : '310px'
	};

			
			
				 addedTableRules[arrayIndex] = new  YAHOO.widget.ScrollingDataTable('rules_'+group_id, columns,ds,configs);
			
		});

	
	
}


function init() {



//begin group content
	YAHOO.util.Event.onContentReady("group_content", function() {
		try {
			accordionView = new YAHOO.widget.AccordionView('group_content', {
				collapsible : true,
				expandable : true,
				width : '70%',
				animate : true,
				animationSpeed : '0.1'
			});
		} catch(e) {alert(e);}

		//end defining add group panel

		//defining buttons
			[% i = 0 %]
		
[% FOREACH group IN content.0 %]


var rule_table_[% group.group_id %] ;
var input_ports_table_[% group.group_id %];
var output_ports_table_[% group.group_id %];

/***************************************************group input port table *******************************************/

	YAHOO.util.Event.onContentReady("input_ports_[% group.group_id %]", function(){
			
			var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=group_input_ports&group_id=[% group.group_id %]");
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
     // {key: "port_address", label: "Port Address", width: 100},
      
      ];
			
			     var configs = {
		height : '70px',
	
	};
				 input_ports_table_[% group.group_id %] = new  YAHOO.widget.ScrollingDataTable('input_ports_[% group.group_id %]', columns,ds,configs);
			
		});
		
		
/***************************************************group output port table *******************************************/		
			YAHOO.util.Event.onContentReady("output_ports_[% group.group_id %]", function(){
			
			var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=group_output_ports&group_id=[% group.group_id %]");
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "port_id"},	
       /// {key: "port_address"},
      ],
      metaFields: {
	error: "error"
      }
    };
	

		 var columns = [
      {key: "port_id", label: "Port", width: 30},
    //  {key: "port_address", label: "Port Address", width: 100},
      
      ];
			
			var configs = {
//		height : '150px',
//		width : '310px'
	};

			 output_ports_table_[% group.group_id %] = new  YAHOO.widget.ScrollingDataTable('output_ports_[% group.group_id %]', columns,ds,configs);
			
		});


/***************************************************group rules table *******************************************/		
		YAHOO.util.Event.onContentReady("rules_[% group.group_id %]", function(){
			
			var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=group_values&group_id=[% group.group_id %]");
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "group_value"},	
  
      ],
      metaFields: {
	error: "error"
      }
    };
	

		 var columns = [
      {key: "group_value", label: "Rules", width: 100},
    
      
      ];
      
      var configs = {
//		height : '70px',
//		width : '310px'
	};

			
			
				 rules_table_[% group.group_id %] = new  YAHOO.widget.ScrollingDataTable('rules_[% group.group_id %]', columns,ds,configs);
			
		});

/***************************************************handle callback for buttons *******************************************/	


		YAHOO.util.Event.onContentReady("edit_[% group.group_id %]", function() {
		
			var edit_button = new YAHOO.widget.Button("edit_[% group.group_id %]", {
				label : "Edit"
			});
			edit_button.addClass("button_panel");
			var groupObject = new Group([% group.group_id %] , input_ports_table_[% group.group_id %], output_ports_table_[% group.group_id %], rules_table_[% group.group_id %], [% i %]);
			YAHOO.util.Event.addListener("edit_[% group.group_id %]", "click", edit_button_func, groupObject);

		});

		YAHOO.util.Event.onContentReady("delete_[% group.group_id %]", function() {
			var del_button = new YAHOO.widget.Button("delete_[% group.group_id %]", {
				label : "Delete"
				});
				del_button.addClass("button_panel");
			var groupObject = new Group([% group.group_id %], null,null,null, [% i %]);
			
			YAHOO.util.Event.addListener("delete_[% group.group_id %]", "click", delete_button, groupObject);

		});











//end group content
[% i = i +1 %]
		[% END %]

index_panel = [% i %] ;
	});
	//defining edit panel

	YAHOO.example.container.edit_panel = new YAHOO.widget.Panel("edit_panel", {
		width : "400px",
		visible : false,
		constraintoviewport : true
	});
	YAHOO.example.container.edit_panel.render();

	//end defining edit panel

	//define add group panel

	YAHOO.example.container.add_panel = new YAHOO.widget.Panel("add_panel", {
		width : "370px",
		visible : false,
		constraintoviewport : true
	});
	YAHOO.example.container.add_panel.render();
// end add group panel



	YAHOO.util.Event.onContentReady("add_new_button", function() {
		var add_button = new YAHOO.widget.Button("add_new_button", {
			label : "Add group"
		});
		add_button.addClass("page_button");
		YAHOO.util.Event.addListener("add_new_button", "click", YAHOO.example.container.add_panel.show, YAHOO.example.container.add_panel, true);
	});

	YAHOO.util.Event.onContentReady("edit_group", function() {
		var edit_button = new YAHOO.widget.Button("edit_group", {
			label : "Edit"
		});
		edit_button.addClass("button_panel");
		YAHOO.util.Event.addListener("edit_group", "click", null);
	});

	YAHOO.util.Event.onContentReady("clear_edit_group", function() {
		var clear_edit_button = new YAHOO.widget.Button("clear_edit_group", {
			label : "Clear"
		});
		clear_edit_button.addClass("button_panel");
		YAHOO.util.Event.addListener("clear_edit_group", "click", null);
	});

	YAHOO.util.Event.onContentReady("submit_new_group", function() {
		var add_new_button = new YAHOO.widget.Button("submit_new_group", {
			label : "Add"
		});

		add_new_button.addClass("button_panel")
		YAHOO.util.Event.addListener("submit_new_group", "click",submit_new_group, null,true);
	});
		
	YAHOO.util.Event.onContentReady("edit_group", function() {
		var edit_button = new YAHOO.widget.Button("edit_group", {
			label : "Edit"
		});
		edit_button.addClass("button_panel");
		
		YAHOO.util.Event.addListener("edit_group", "click",edit_group, null,true);
	});		


	YAHOO.util.Event.onContentReady("clear_new_group", function() {
		var clear_new_button = new YAHOO.widget.Button("clear_new_group", {
			label : "Clear"
		});
		clear_new_button.addClass("button_panel");
		YAHOO.util.Event.addListener("clear_new_group", "click",null, null,true);
	});
	//end defining buttons

	//datatable for adding rules to the group

	var ds = new YAHOO.util.DataSource([]);
	ds.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;

	var cols = [{
		key : "Value",
		label : "Value",
		width : 195,
	}, {
		key : "Delete",
		label : "Delete",
		width : 50,
		formatter : function(el, rec, col, data) {
			var del_button = new YAHOO.widget.Button({
				label : "Delete"
			});
			del_button.addClass("button_panel");
	//		YAHOO.util.Dom.addClass(del_button, "endpoint_delete_button");

			var t = this;
			del_button.on("click", function() {
				//var interface = rec.getData('interface');

					rules_table.deleteRow(rules_table.getRecordSet().getRecordIndex(rec));
					
			
			});
			del_button.appendTo(el);
		}
	}];

	var configs = {
		height : '70px',
		width : '310px'
	};

	 rules_table = new YAHOO.widget.ScrollingDataTable("rules_table", cols, ds, configs);

	YAHOO.util.Event.onContentReady("add_rule_button", function() {

		var add_rule_button = new YAHOO.widget.Button("add_rule_button", {
			label : "Add Rule"
		});
		add_rule_button.addClass("button_panel");
		add_rule_button.on("click", function() {

			rules_table.addRow({
				'Value' : document.getElementById('group_value').value 
				});
			});
			

	
	});
	
	
	YAHOO.util.Event.onContentReady("edit_add_rule_button", function() {

		var edit_add_rule_button = new YAHOO.widget.Button("edit_add_rule_button", {
			label : "Add Rule"
		});
		edit_add_rule_button.addClass("button_panel");
		edit_add_rule_button.on("click", function() {

			edit_group_value_table.addRow({
				'group_value' : document.getElementById('edit_group_value').value 
				});
			});
			

	
	});
	
	//end cadding rules to the group

YAHOO.util.Event.onContentReady("edit_input_switch_id", function() {
populate_ports(1,null);
});

YAHOO.util.Event.onContentReady("edit_output_switch_id", function() {
populate_ports(2,null);
});



YAHOO.util.Event.onContentReady("add_input_switch", function() {
populate_ports(3,null);
});

YAHOO.util.Event.onContentReady("add_output_switch", function() {
populate_ports(4,null);
});



}
YAHOO.util.Event.addListener(window, "load", init);

</script>
