<script>
 YAHOO.namespace("example.container");

var accordionView = null;

var index_panel = 0;

function Switch(id, mac_address, ip_address, name, description,index) {

	this.id = id;
	this.mac_address = mac_address;
	this.ip_address = ip_address;
	this.name = name;
	this.description = description;
	this.index = index;
}


function add_xconnect_for_switch(switch_id){
	
			YAHOO.util.Event.onContentReady("xconnect_ports_"+switch_id, function() {

		var ds_xconnect = new YAHOO.util.DataSource("webservice/data.cgi?action=xconnect_values&switch_id="+switch_id);
    ds_xconnect.responseType = YAHOO.util.DataSource.TYPE_JSON;

    ds_xconnect.responseSchema = {
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
      ];
      var configs = {
		height : '70px',
	
	};

	xconnect_table = new YAHOO.widget.ScrollingDataTable('xconnect_ports_'+switch_id, columns,ds_xconnect,configs);

		});
	
}


function add_ports_for_switch(switch_id){
	
		YAHOO.util.Event.onContentReady("port_table_"+switch_id, function() {
		
		var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=get_switch_ports&switch_id="+switch_id);
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "port_id"},	
        {key: "port_address"},
        {key: "state"}
      ],
      metaFields: {
	error: "error"
      }
    };
	

		 var columns = [
      {key: "port_id", label: "Port", width: 30, sortable:true},
      {key: "port_address", label: "Port Address", width: 100},
      {key: "state", label: "State", width:30 , formatter: function(el, rec, col, data){

	      // gets returned as Mbps
	      var bandwidth = data;

	      if (bandwidth ==0 ){
		  el.innerHTML = "<img src='media/link_up.png' width='25px' height='25px' />";
	      }
	      else{
		  el.innerHTML = "<img src='media/link_down.png' width='25px' height='25px' />";
	      }
	      
	  }}
      
      ];
var configs = {
		height : '10em',
		
	};

			var port_table = new
			YAHOO.widget.ScrollingDataTable('port_table_'+switch_id, columns,ds,{height:"10em"});
			
			port_table.subscribe('initEvent',function() {

this.sortColumn(this.getColumn('port_id'),YAHOO.widget.DataTable.CLASS_ASC);
});
			
		
		});
		//end port table add
	
	
	
}

function edit_button_func(e, obj) {

	//alert("test" +element.getElementsByTagName('span').namedItem('mac_address').firstChild.innerHTML);

	document.getElementById("edit_switch_name").value  = trim(document.getElementById("" + obj.id + "_name").innerHTML);
	document.getElementById("edit_mac_address").value = document.getElementById("mac_"+obj.id).innerHTML;
	document.getElementById("edit_datapath_id").value = document.getElementById("" + obj.id).innerHTML;
	document.getElementById("edit_ip_address").value = document.getElementById("ip_"+obj.id).innerHTML;

	YAHOO.example.container.edit_panel.show();
}



function submit_new_switch(obj){
	
	//alert ( document.getElementById('new_switch').value +"mac address "+
//	document.getElementById('new_mac_address').value +"  datapath id " +
	//document.getElementById('new_datapath_id').value +" ip _address "+ 
	//document.getElementById('new_ip_address').value);
	
	var switch_id = document.getElementById('new_datapath_id').value;
	var switch_name = document.getElementById('new_switch').value;
	var switch_mac_address = document.getElementById('new_mac_address').value;
	var switch_ip_address = document.getElementById('new_ip_address').value;

var ds =  new YAHOO.util.DataSource("webservice/admin.cgi?action=submit_new_switch&new_switch="+ switch_id +"&mac_address="+ switch_mac_address+
								"&datapath_id="+switch_id+"&ip_address="+ switch_ip_address+"&switch_name="+switch_name	);


//alert("webservice/admin.cgi?action=submit_new_switch&new_switch="+ switch_id +"&mac_address="+ switch_mac_address+
	//						"&datapath_id="+switch_id+"&ip_address="+ switch_ip_address+"&switch_name="+switch_name);

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
        	
        	return;
        	
        }
        
        
        alert("switch added");
        
        //add accordion here 
       
      var body =  '<div id="[$ switch.datapath_id $]_information">'+
			'<div id="switch_headers_1">	'+
			'	<div id="[$ switch.datapath_id $]_name" name="[$ switch.datapath_id $]_name" style="display:none;">'+
			'		[$ switch.switch_name $]'+
			'	</div>'+
			'	<span><button id="edit_[$ switch.datapath_id $]"></button></span>'+
			'	<span><button id="delete_[$ switch.datapath_id $]"></button></span>'+
			'	<br />'+
			'	<b> Ports </b>'+
			'	<br />'+
			'	<label for="[$ switch.datapath_id $]">Datapath ID:</label><span id="[$ switch.datapath_id $]">[$ switch.datapath_id $]</span> <br />'+
			'	<label for="mac_[$ switch.datapath_id $]">MAC Address:</label><span id="mac_[$ switch.datapath_id $]">[$ switch.mac_address $]</span> <br />'+
			'	<label for="ip_[$ switch.datapath_id $]">IP Address:</label><span id="ip_[$ switch.datapath_id $]">[$ switch.ip_address $]</span>'+
			'	<div id ="port_table_[$ switch.datapath_id $]"></div>'+
			'	<!--b>X-Connect Ports:</b-->'+
			'	<div id="xconnect_ports_[$ switch.datapath_id $]"></div>'+
			'</div>';
			
			
 var body1 = body.replace(/\[\$ switch\.datapath_id \$\]/g, switch_id);
		var body2 =	body1.replace(/\[\$ switch.\ip_address \$\]/g, switch_ip_address);
		var body3 =	body2.replace(/\[\$ switch\.mac_address \$\]/g, switch_mac_address);
		var body4 =	body3.replace(/\[\$ switch\.switch_name \$\]/g, switch_name);
	
        attrs = {
        	label: switch_name, 
        	content: body4
        	
        }
       
        YAHOO.example.container.add_panel.hide();
        accordionView.addPanel(attrs);
        ;
        //call the above 2 function 
        add_xconnect_for_switch(switch_id);
        add_ports_for_switch(switch_id);
        
        YAHOO.util.Event.onContentReady("edit_"+switch_id, function() {
			var edit_button = new YAHOO.widget.Button("edit_"+switch_id, {
				label : "Edit"
			});
			edit_button.addClass("button_panel");
			var switchObject = new Switch	(switch_id,switch_mac_address,switch_ip_address, switch_name, null);

			YAHOO.util.Event.addListener("edit_"+switch_id, "click", edit_button_func, switchObject);

		});

		YAHOO.util.Event.onContentReady("delete_"+switch_id, function() {
			var del_button = new YAHOO.widget.Button("delete_"+switch_id, {
				label : "Delete"
			});
			del_button.addClass("button_panel");
			var switchObject = new Switch(switch_id,null,null,null,null, index_panel);
			 index_panel++ 
			YAHOO.util.Event.addListener("delete_"+switch_id, "click", delete_switch, switchObject);

		});
        
        
        
        
        
        //put action here     
                        },
              failure: function(req, resp){
                             

                              //  alert("Error while adding device.");
                            }
                        });


}

function edit_switch(){

	alert( document.getElementById('edit_switch_name').value + " mac address "+
	document.getElementById('edit_mac_address').value +" datapath id  " +
	document.getElementById('edit_datapath_id').value + " ip address  " +
 	document.getElementById('edit_ip_address').value );
	
	var switch_id = document.getElementById('edit_datapath_id').value;
	var switch_name = document.getElementById('edit_switch_name').value;
	var switch_mac_address = document.getElementById('edit_mac_address').value;
	var switch_ip_address = document.getElementById('edit_ip_address').value;

	
	
	var ds =  new YAHOO.util.DataSource("webservice/admin.cgi?action=edit_switch&switch_name="+ switch_name +"&mac_address="+switch_mac_address+
								"&datapath_id="+switch_id +"&ip_address="+switch_ip_address);



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
        	
        	alert(resp.results[0].error)
        	
        }else{
        
         YAHOO.example.container.edit_panel.hide();
        
        alert("switch updated");
        
        
        var mac_string = "mac_"+ switch_id;
        var ip_string = "ip_" + switch_id;
        
        document.getElementById(mac_string).innerHTML = switch_mac_address;
        document.getElementById(ip_string).innerHTML = switch_ip_address;
          document.getElementById(switch_id+"_name").innerHTML = switch_name;
        
        //change the element values 
        
        add_xconnect_for_switch(document.getElementByIdDocument('edit_switch_id').value);
        add_ports_for_switch(document.getElementByIdDocument('edit_switch_id').value);
        
        
        //document.getElementById('edit_datapath_id').value
        
        //put action here     
        
        }
                        },
              failure: function(req, resp){
                             

                                alert("Error while updating device.");
                            }
                        });
	

}

function delete_switch(e,obj){
		showConfirm("Are you sure you wish to delete the switch ?", function() {
	
var ds =  new YAHOO.util.DataSource("webservice/admin.cgi?action=delete_switch&switch_id="+obj.id);



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
      	 	alert("switch is deleted");
      	 	 
      	  accordionView.removePanel(obj.index);
      	index_panel--;
      }
     },
              failure: function(req, resp){
                             

                                alert("Error while device device.");
                                alert(resp.error);
                            }
                        });
	
	



	}, function() {
	});
	
	
	
	
}



function trim(stringToTrim) {
	return stringToTrim.replace(/^\s+|\s+$/g,"");
}


function init() {

	// Instantiate a Panel from markup
	YAHOO.example.container.edit_panel = new YAHOO.widget.Panel("edit_panel", {
		width : "350px",
		visible : false,
		constraintoviewport : true
	});
	YAHOO.example.container.edit_panel.render();

	YAHOO.example.container.add_panel = new YAHOO.widget.Panel("add_panel", {
		width : "350px",
		visible : false,
		constraintoviewport : true
	});
	YAHOO.example.container.add_panel.render();

	YAHOO.util.Event.onContentReady("switch_content", function() {
		try {
			accordionView = new YAHOO.widget.AccordionView('switch_content', {
				collapsible : true,
				expandable : true,
				width : '70%',
				animate : true,
				animationSpeed : '0.1'
			});
		} catch(e) {alert(e);
		}
		[% i = 0 %]
		[% FOREACH switch IN content %]

		YAHOO.util.Event.onContentReady("edit_[% switch.datapath_id %]", function() {
			var edit_button = new YAHOO.widget.Button("edit_[% switch.datapath_id %]", {
				label : "Edit"
			});
			edit_button.addClass("button_panel");
			var switchObject = new Switch	('[% switch.datapath_id %]','[% switch.mac_address %]','[% switch.ip_address%]', '[% switch.switch_name %]', null);

			YAHOO.util.Event.addListener("edit_[% switch.datapath_id %]", "click", edit_button_func, switchObject);

		});

		YAHOO.util.Event.onContentReady("delete_[% switch.datapath_id %]", function() {
			var del_button = new YAHOO.widget.Button("delete_[% switch.datapath_id %]", {
				label : "Delete"
			});
			del_button.addClass("button_panel");
			var switchObject = new Switch('[% switch.datapath_id %]',null,null,null,null,[% i %]);
			YAHOO.util.Event.addListener("delete_[% switch.datapath_id %]", "click", delete_switch, switchObject);

		});
		//port table details:

	add_ports_for_switch('[% switch.datapath_id %]');

		//add x-connect ports

//add_xconnect_for_switch('[% switch.datapath_id %]');
		//end x-connect
[% i = i +1 %]
		[% END %]

index_panel = [% i %] ;

	});

	YAHOO.util.Event.onContentReady("add_new_button", function() {
		var add_button = new YAHOO.widget.Button("add_new_button", {
			label : "Add Switch"
		});
		add_button.addClass("page_button");
		YAHOO.util.Event.addListener("add_new_button", "click", YAHOO.example.container.add_panel.show, YAHOO.example.container.add_panel, true);
	});

	YAHOO.util.Event.onContentReady("edit_switch", function() {
		var edit_button = new YAHOO.widget.Button("edit_switch", {
			label : "Edit"
		});
		edit_button.addClass("button_panel");
		YAHOO.util.Event.addListener("edit_switch", "click",edit_switch, null,true);
	});

	YAHOO.util.Event.onContentReady("clear_edit_switch", function() {
		var clear_edit_button = new YAHOO.widget.Button("clear_edit_switch", {
			label : "Clear"
		});
			clear_edit_button.addClass("button_panel");
		YAHOO.util.Event.addListener("clear_edit_switch", "click", null);
	});

	YAHOO.util.Event.onContentReady("submit_new_switch", function() {
		var add_new_button = new YAHOO.widget.Button("submit_new_switch", {
			label : "Add"
		});
		add_new_button.addClass("button_panel");
		var switchObject = new Switch(null,null,null,null,null,index_panel);
		YAHOO.util.Event.addListener("submit_new_switch", "click",submit_new_switch, null,true);
	});

	YAHOO.util.Event.onContentReady("clear_new_switch", function() {
		var clear_new_button = new YAHOO.widget.Button("clear_new_switch", {
			label : "Clear"
		});
		clear_new_button.addClass("button_panel");
		YAHOO.util.Event.addListener("clear_new_switch", "click", null);
	});
}

YAHOO.util.Event.addListener(window, "load", init);

</script>