<script>

function Cookie(){
  
  this.data = {};
  
  this.load = function(){
		var cookies = document.cookie.split("; ");
		
		for (var i = 0; i < cookies.length; i++){
		  
		  var candidate = cookies[i];
		  
		  var kvpair = candidate.split("=");
		  
		  if (kvpair[0] == "data"){
		    this.data = JSON.parse(decodeURIComponent(kvpair[1]));
		  }
		  
		}
		  
  };
  
  this.clear = function(){
    this.data = {};
    this.save();
  }
  
  this.save = function(){
		var expires = new Date();
		expires.setDate(expires.getDate() + 1);    
		document.cookie = "data=" + encodeURIComponent(JSON.stringify(this.data)) + ";path=/;expires=" + expires.toUTCString();				  
  }

  return this;  
}

var session = new Cookie();

session.load();

</script>