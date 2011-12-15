<script>



function init(){
	[% FOREACH switch IN content %]
			YAHOO.util.Event.onContentReady("switch[% switch.datapath_id %]_status_table", function() {

		var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=get_switch_status&switch_id=[% switch.datapath_id %]");
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "port_id"},	
        {key: "port_address"},
        {key: "port_status"}
      ],
      metaFields: {
	error: "error"
      }
    };
	

		 var columns = [
      {key: "port_id", label: "Port", width: 30},
      {key: "port_address", label: "Port Address", width: 100},
      {key: "port_status", label: "Status" , width: 50},
      
      ];

			var port_table = new
			YAHOO.widget.DataTable('switch[% switch.datapath_id %]_status_table', columns,ds,null);
		
		});
	
	[% FOREACH switch IN content %]
}

YAHOO.util.Event.addListener(window, "load", init);


</script>