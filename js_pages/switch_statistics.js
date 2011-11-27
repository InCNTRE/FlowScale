<script type="text/javascript">


function load_tables(){
	
	
//load_aggregate_statistics();

load_flow_statistics();

//load_table_statistics();

//load_port_statistics();

	
	
	
	
}


function load_aggregate_statistics(){
		var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=get_switch_statistics&switch_id="+document.getElementById('switch_selection').value+"&type=aggregate");
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "packet_count"},	
        {key: "flow_counts"},
      ],
      metaFields: {
	error: "error"
      }
    };
	
	
	

		 var columns = [
      {key: "packet_count", label: "Packet Count", width: 30},
      {key: "flow_count", label: "Flow count", width: 100},
      
      ];

		
			 var input_table_edit = new
			YAHOO.widget.DataTable('aggregate_statistics', columns,ds,null);
	
}


function load_flow_statistics(){
	
		var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=get_switch_statistics&switch_id="+document.getElementById('switch_selection').value+"&type=flow");
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "actions"},	
        {key: "hard_timeout"},
        {key: "idle_timeout"},
        {key: "match"},
        {key: "priority"},
        {key: "packet_count"},
        {key: "table_id"},
      ],
      metaFields: {
	error: "error"
      }
    };
	
	
      function goFormat(el, rec, col, data){

	      // gets returned as Mbps
	      var bandwidth = data;
		data1 =data.split("[");
		data2 = data1[1].split("]");
		  el.innerHTML =data2[0];
	      }
	      
	  
		 var columns = [ 		 
       {key: "match", label: "Match", width: 600, formatter: goFormat},
       {key: "priority", label: "Priority", width: 40},
        {key: "packet_count", label: "Packet Count", width: 200},
         {key: "table_id", label: "Table ID", width: 40},
      {key: "actions", label: "Action", width: 150},
      ];

	
		var configs = {

 paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
		
	};	
			 var input_table_edit = new
			YAHOO.widget.DataTable('flow_statistics', columns,ds,configs);
	
	
}


function load_table_statistics(){
	
		var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=get_switch_statistics&switch_id="+document.getElementById('switch_selection').value+"&type=table");
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "match_count"},	
        {key: "maximum_entries"},
        {key: "table_id"},
        {key: "name"},
      ],
      metaFields: {
	error: "error"
      }
    };
	

		 var columns = [
      {key: "match_count", label: "Match Count", width: 30},
      {key: "maximum_entries", label: "maximum entries", width: 100},
      {key: "name", label: "Name", width: 100},
      {key: "table_id", label: "Table", width: 100},
      
      ];

		
			 var input_table_edit = new
			YAHOO.widget.DataTable('table_statistics', columns,ds,null);
	
	
}


function load_port_statistics(){
	

		var ds =	new YAHOO.util.DataSource("webservice/data.cgi?action=get_switch_statistics&switch_id="+document.getElementById('switch_selection').value+"&type=port");
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "port_id"},	
        {key: "receive_packets"},
        {key: "transmit_packets"},
      ],
      metaFields: {
	error: "error"
      }
    };
	

		 var columns = [
      {key: "port_id", label: "Port", width: 30},
      {key: "receive_packets", label: "Received Packets", width: 100},
      {key: "transmit_packets", label: "Transmit Packets", width: 100},
      ];

		
			 var input_table_edit = new
			YAHOO.widget.DataTable('port_statistics', columns,ds,null);
	
}


</script>
