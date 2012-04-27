


<script>
YAHOO.util.Event.onContentReady("inject_flows_button", function(){ 
	var inject_button = new YAHOO.widget.Button("inject_flows_button",{label: "Inject!"});
	
	inject_button.addClass("button_panel");

	inject_button.on("click", function(){
	switch_id = document.flow_form.flow_number.value;
			    
	for (i =0; i< document.form_selection_type.type_selection.length; i++){
		
		if(document.form_selection_type.type_selection[i].checked){
			type_stats = document.form_selection_type.type_selection[i].value;
			break;
		}
	}

			    var injectDS = new YAHOO.util.DataSource("webservice/admin.cgi?action=inject_flowst&switch_1_id="+switch_id+"&flow_number="+flow_number);
					    injectDS.responseType = YAHOO.util.DataSource.TYPE_JSON;

			    injectDs.responseSchema = {
				resultsList: "results",
				fields: [{key: "success"},{key:"error"}],
				 metaFields: {
		error: "error"
	      }
			    };


	injectDs.sendRequest('', {success: function(req, resp){
		
		if(resp.results[0].success == 0){
			alert(resp.results[0].error);
			return;
		}
		
								
				    
				    },
				    failure: function(req, resp){
				     

					alert("Error while deleting device.");
				    }
				});



						
						
			      });
	});


YAHOO.util.Event.onContentReady("delete_flows_button", function(){ 
	var delete_button = new YAHOO.widget.Button("delete_flows_button",{label: "Flush Table!"});
	
	delete_button.addClass("button_panel");
	delete_button.on("click", function(){
					    

			    var deleteDS = new YAHOO.util.DataSource("webservice/admin.cgi?action=delete_flowst&switch_1_id="+switch_id);
					    deleteDS.responseType = YAHOO.util.DataSource.TYPE_JSON;

			    deletetDs.responseSchema = {
				resultsList: "results",
				fields: [{key: "success"},{key:"error"}],
				 metaFields: {
		error: "error"
	      }
			    };


	deletetDs.sendRequest('', {success: function(req, resp){
		
		if(resp.results[0].success == 0){
			alert(resp.results[0].error);
			return;
		}
		
								
				    
				    },
				    failure: function(req, resp){
				     

					alert("Error while deleting device.");
				    }
				});



						
						
			      });
	});
	

</script>
