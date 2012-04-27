<script>

YAHOO.namespace("example.container");
var poll = false;
var myPanel;
var type_stats;
var port_table;
var flow_table;
var divArray = new Array();
var selected_ports = new Array() ;
var count =0;




function add_ports_for_switch(){
	
	
	switch_id = document.getElementById('switch_selection').value;
	
	


	for (i =0; i< document.form_selection_type.type_selection.length; i++){
		
		if(document.form_selection_type.type_selection[i].checked){
			type_stats = document.form_selection_type.type_selection[i].value;
			break;
		}
	}

	
	if (type_stats == null){
		alert("please pick a type");
		return;
	}
		var ds ;
		if (type_stats == "controller"){
			
		ds=	new YAHOO.util.DataSource("webservice/data.cgi?action=get_switch_ports&switch_id="+switch_id);
		
		
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "port_id"},	
      
      ],
      metaFields: {
	error: "error"
      }
    };
	

		 var columns = [
      {key: "port_id", label: "Port", width: 30, sortable:true},
     
      
      ];
var configs = {
		height : '10em',
		
	};

			 port_table = new
			YAHOO.widget.ScrollingDataTable('ports', columns,ds,{height:"10em", selectionMode:"multi"});
			port_table.set("selectionMode", "multi");
			port_table.subscribe("rowClickEvent", port_table.onEventSelectRow);
			port_table.subscribe('initEvent',function() {

this.sortColumn(this.getColumn('port_id'),YAHOO.widget.DataTable.CLASS_ASC);
});
			
		}else{
			
			
			ds=	new YAHOO.util.DataSource("webservice/data.cgi?action=get_switch_ports&switch_id="+switch_id);
		
		
			ds.responseType = YAHOO.util.DataSource.TYPE_JSON;

		ds.responseSchema = {
      resultsList: "results",
      fields: [
        {key: "port_id"},	

      ],
      metaFields: {
	error: "error"
      }
    };
	

		 var columns = [
      {key: "port_id", label: "Sensor", width: 30, sortable:true},
  
      
      ];
var configs = {
		height : '10em',
		
	};

			 port_table = new
			YAHOO.widget.ScrollingDataTable('ports', columns,ds,{height:"10em", selectionMode:"multi"});
			port_table.set("selectionMode", "multi");
			port_table.subscribe("rowClickEvent", port_table.onEventSelectRow);
			
			port_table.subscribe('initEvent',function() {

this.sortColumn(this.getColumn('machine'),YAHOO.widget.DataTable.CLASS_ASC);
});
		}
	
		//end port table add
	
	
	
}

YAHOO.util.Event.onContentReady("graph_button", function() {

		var edit_add_rule_button = new YAHOO.widget.Button("graph_button", {
			label : "Graph"
		});
		edit_add_rule_button.addClass("button_panel");
		edit_add_rule_button.on("click", function() {
			poll = false;
			 myPanel = new YAHOO.widget.Panel("graph_section", {
    width: "1200px",
    height: "1200px", 
    fixedcenter: false, 
    constraintoviewport: true, 
    underlay: "shadow", 
    close: true, 
    visible: true, 
    draggable: true
});
		
		
		//var response=	new YAHOO.util.DataSource("webservice/data.cgi?action=get_rrdsr&switch_id="+switch_id);
		
			//myPanel.setBody("need to implement here under "+ type_stats + "and port table " + port_table.toString());
		
			var selected_port_indices = port_table.getSelectedRows();
			divArray = new Array();	
			var divString ="";
			 selected_ports = new Array();
			var port_table_records = port_table.getRecordSet();
			var heightValue = Math.round(1000/selected_port_indices.length);
var combination_type ;
	for (i =0; i< document.form_selection_type.combination_type.length; i++){
		
		if(document.form_selection_type.combination_type[i].checked){
			combination_type  = document.form_selection_type.combination_type[i].value;
			break;
		}
	}
			if(combination_type == "aggregate" ){
				heightValue = 900;
			}	
			for(i=0; i< selected_port_indices.length;i++){
				
				divString =  divString+ "<span align=\"center\"><h3> port "+ port_table_records.getRecord(selected_port_indices[i]).getData('port_id')+"</h3></span><div id=\"sensor_graph"+port_table_records.getRecord(selected_port_indices[i]).getData('port_id')+"\" style=\"width:1000px;height:"+heightValue+"px\">port  "+port_table_records.getRecord(selected_port_indices[i]).getData('port_id')+"</div>  "  ;
				
				divArray[i] = "sensor_graph"+port_table_records.getRecord(selected_port_indices[i]).getData('port_id');
		
				selected_ports[i] = port_table_records.getRecord(selected_port_indices[i]).getData('port_id');
	
		if(combination_type=="aggregate"){
				break;
			}
	}



	
			//document.getElementById('traffic_graphs').innerHTML = divString;
			myPanel.setBody(divString);
		myPanel.render();
				
				//generateChart();
				var t=setTimeout("generateChart()",1000);
			
			});
			

	
	});
	
	
YAHOO.util.Event.onContentReady("flow_button", function() {

		var flow_button = new YAHOO.widget.Button("flow_button", {
			label : "Flows"
		});
		flow_button.addClass("button_panel");
	flow_button.on("click", function() {
	var time1;
	var time2;
	var totalRecs ;
	var currentDate = new Date();

		time1  = dateFormat(currentDate, "yyyy-mm-dd HH:MM:ss");
		var  beforeDate  = new Date();
		beforeDate.setTime(currentDate.getTime()- 300000);
		time2  = dateFormat(beforeDate, "yyyy-mm-dd HH:MM:ss");

		var flowDS
		flowDS=	new YAHOO.util.DataSource("webservice/flow_stat.cgi?first_time="+time1+"&second_time="+time2);
			flowDS.responseType = YAHOO.util.DataSource.TYPE_JSON;
		flowDS.responseSchema = {
      resultsList: "response.results",
      fields: [
        {key: "match_string"},	
        {key: "packet_count"},	
        {key: "action"},	
      
      ],
      metaFields: {
	total: "response.total",
      }
    };
	

		 var columns = [
      {key: "match_string", label: "Match", width: 300, sortable:true},
      {key: "packet_count", label: "Packets Received", width: 100, sortable:true}, 
      {key: "packet_count", label: "Percentage", width: 100, sortable:true, formatter: function(el,rec,col,data){
		el.innerHTML = roundNumber((data/parseInt(totalRecs)) * 100 , 5);
		

}},
      {key: "action", label: "Action", width: 100, sortable:true},
	
      
      ];
var configs = {
		height : '10em',
		selectionMod: 'multi',
		paginator: new YAHOO.widget.Paginator({ rowsPerPage: 50}),
		
	};

			

			 flow_table = new YAHOO.widget.DataTable('flow_div', columns,flowDS,configs);
			flow_table.set("selectionMode", "multi");
			flow_table.subscribe("rowClickEvent", flow_table.onEventSelectRow);
			flow_table.subscribe('initEvent',function() {

this.sortColumn(this.getColumn('packet_count'),YAHOO.widget.DataTable.CLASS_DESC);
});
			


flow_table.subscribe( 'dataReturnEvent', function(oArgs) {
     //
     // oArgs contains .request and .response elements,
     //   futhermore,   .response contains .meta and .results

     totalRecs = oArgs.response.meta.total;

	

	
     //
     //    fill the innerHTML with the row totals
     //
});


});	
});

function roundNumber(rnum, rlength) { // Arguments: number to round, number of decimal places
  var newnumber = Math.round(rnum*Math.pow(10,rlength))/Math.pow(10,rlength);
	return newnumber;
}


function pollingInterval (){
	//document.getElementById('mychart').innerHTML = "test before";
if (poll){
var t=setTimeout("generateChart()",20000);
}
}
		

function generateChart(){	
count =0;
var time_type;
	for (i =0; i< document.form_selection_type.time_type.length; i++){
		
		if(document.form_selection_type.time_type[i].checked){
			time_type  = document.form_selection_type.time_type[i].value;
			break;
		}
	}

	var time1;
	var time2;
	var currentDate = new Date();
	if ( document.form_selection_type.real_time.checked == true){

		time1  = dateFormat(currentDate, "yyyy-mm-dd HH:MM:ss");
		var  beforeDate  = new Date();
		beforeDate.setTime(currentDate.getTime()- 300000);
	//	alert(currentDate.getTime()+ "  "+ beforeDate.getTime());
		time2  = dateFormat(beforeDate, "yyyy-mm-dd HH:MM:ss");
	}
	else if(time_type == "10_minutes"){
	                time1  = dateFormat(currentDate, "yyyy-mm-dd HH:MM:ss");
                var  beforeDate  = new Date();
                beforeDate.setTime(currentDate.getTime()- 600000);
        //      alert(currentDate.getTime()+ "  "+ beforeDate.getTime());
                time2  = dateFormat(beforeDate, "yyyy-mm-dd HH:MM:ss");


	}else if (time_type == "30_minutes"){

                time1  = dateFormat(currentDate, "yyyy-mm-dd HH:MM:ss");
                var  beforeDate  = new Date();
                beforeDate.setTime(currentDate.getTime()- 1800000);
        //      alert(currentDate.getTime()+ "  "+ beforeDate.getTime());
                time2  = dateFormat(beforeDate, "yyyy-mm-dd HH:MM:ss");
        

	}else if(time_type == "hour"){

                time1  = dateFormat(currentDate, "yyyy-mm-dd HH:MM:ss");
                var  beforeDate  = new Date();
                beforeDate.setTime(currentDate.getTime()- 3600000);
        //      alert(currentDate.getTime()+ "  "+ beforeDate.getTime());
                time2  = dateFormat(beforeDate, "yyyy-mm-dd HH:MM:ss");
        

	}else if(time_type =="date_range"){
		time1 = document.form_selection_type.date_range_2.value;
		time2 =	document.form_selection_type.date_range_1.value;
	}
var j =0;
var xmlhttp=new XMLHttpRequest();
var result;
var combination_type ;
	for (i =0; i< document.form_selection_type.combination_type.length; i++){
		
		if(document.form_selection_type.combination_type[i].checked){
			combination_type  = document.form_selection_type.combination_type[i].value;
			break;
		}
	}
//alert("webservice/port_stat.cgi?first_time="+time1+"&second_time="+time2+"&port_id="+selected_ports+"&combination="+combination_type);
if(type_stats == "controller"){
alert ("webservice/port_stat.cgi?first_time="+time1+"&second_time="+time2+"&port_id="+selected_ports+"&combination="+combination_type,true);
xmlhttp.open("GET","webservice/port_stat.cgi?first_time="+time1+"&second_time="+time2+"&port_id="+selected_ports+"&combination="+combination_type,true);
}else if(type_stats == "cluster"){

xmlhttp.open("GET","webservice/capstats.cgi?first_time="+time1+"&second_time="+time2+"&port_id="+selected_ports+"&combination="+combination_type,true);

}
xmlhttp.send();
xmlhttp.onreadystatechange=function(){
  if (xmlhttp.readyState==4 && xmlhttp.status==200){
    result =xmlhttp.responseText;
var port_result;
if(combination_type = "side_by_side"){
 port_result = result.split("=");
}
else if (combination_type="aggregate"){
port_result = result;
}
for(j = 0; j< divArray.length;j++){

/*get dat range */
//	var a = new Date();
//	var b = new Date(a.getTime()-20000);
//	var c = new Date(a.getTime()-40000);
//	var d = new Date(a.getTime()-60000);
//	var e = new Date(a.getTime()-80000);
	
	
		//document.getElementById(divArray[i]).innerHTML = "";
	YUI().use('charts', function (Y) {
    // Charts is available and ready for use. Add implementation
    // code here.

//alert(document.getElementById('2').innerHTML);
// Instantiate and render the chart
//alert(divArray[0]);
document.getElementById(divArray[count]).innerHTML ="";
var divS = "#"+divArray[count];
    /*	    var myDataValues = [
    {category:e.format("HH:MM:ss"), values:2000},
    {category:d.format("HH:MM:ss"), values:50},
    {category:c.format("HH:MM:ss"), values:400},
    {category:b.format("HH:MM:ss"), values:200},
    {category:a.format("HH:MM:ss"), values:i}
];
      */
var intervals = port_result[count].split("-");
count++;
var myDataValues = new Array();
var intervals_label = Math.round((intervals.length/5));
for(y = 0; y < intervals.length; y++){
var more_info = intervals[y].split(",");
var generatedDate = new Date(parseInt(more_info[0]));
var kiloPackets;
var packetIndex;
var packet_type;
	for (i =0; i< document.form_selection_type.packet_type.length; i++){
		
		if(document.form_selection_type.packet_type[i].checked){
			packet_type  = document.form_selection_type.packet_type[i].value;
			break;
		}
	}
if (packet_type =="received"){
packetIndex = 2;

}else if(packet_type =="transmitted"){

packetIndex =1;
}
if(type_stats == "controller"){
kiloPackets = more_info[packetIndex]/1000;
}else{
kiloPackets = more_info[packetIndex] * 8;
}



if((y % intervals_label) ==0){
myDataValues[y] = {category:generatedDate.format("HH:MM:ss"),values:kiloPackets};
     
}else{

myDataValues[y] = {category:"",values:kiloPackets};

}}
var titleName

if(type_stats == "controller"){
titleName = "kpps";
}else if (type_stats == "cluster"){

titleName = "mbps";

}

//define axes  and series collection
    var myAxes = {
        kilopackets:{
            keys:["values"],
            position:"left",
            type:"numeric",
		title:"kpps",
            styles:{
                majorTicks:{
                    display: "none"
                },
                title: {                        //style a title
                fontSize: "90%"
            }
            }
        },
        time:{
            keys:["category"],
            position:"bottom",
            type:"category",
                title: "Time",   //add a title
             styles:{
                majorTicks:{
                    display: "none"
                },
                label: {
                    rotation:0,
                    margin:{top:5}
                },
                title: {                        //style a title
                fontSize: "90%"
            }
            }
        }
    };


   //define the series
    var seriesCollection = [
     {
            type:"area",
            xAxis:"time",
            yAxis:"kilopackets",
            xKey:"category",
            yKey:"values",
            xDisplayName:"Time",
            yDisplayName:"KiloPackets",
	 line: {
                    color: "#ff7200"
                },
         marker: {
                fill: {
                    color: "#ff9f3b"
                },
                border: {
                    color: "#ff7200",
                    weight: 1
                },
                over: {
                    width: 12,
                    height: 12
                },
                width:9,
                height:9
            },

            styles: {
                border: {
                    weight: 1,
                    color: "#467F88"
                   // color: "#58006e"
                },
                over: {
                    fill: {
                        alpha: 0.7
                    }
                }
            }
        },
]

//end define axes 



                var mychart= new Y.Chart({
    dataProvider: myDataValues,
	axes:myAxes,
	seriesCollection:seriesCollection,
    render:divS,
	horizontalGridlines: true,
	verticalGridlines:true
  //  horizontalGridlines: {
  //                          styles: {
    //                            line: {
   //                                 color: "#dad8c9"
    //                            }
   //                         }
    //                    },
//	verticalGridlines: {
  //  styles: {
//	line: {
//		    color: "#dad8c9"
//		}
//	    }
//	}
	                       
 
});

//  mychart.render();   
});
if(combination_type == "aggregate"){
alert("break");
break;
}
}
  

  }
  
else{
	//alert(xmlhttp.status);
} 
}
if(document.form_selection_type.real_time.checked ){
poll=true;
pollingInterval();
}

}
	


	
	
	


</script>
