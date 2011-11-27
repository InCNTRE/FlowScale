/**
 * GRNOC Custom Accordion widget
 * @module Accordion
 */
GRNOC.add_namespace("GRNOC.widget");

/**
 * GRNOC Accordion widgets
 * @class Accordion
 * @constructor
 * @namespace GRNOC.widget
 * @param {object} obj the configuration object (see YUI 3 Accordion)
 */
GRNOC.widget.Accordion = function(obj){
  
  this.ac = new Y.Accordion(obj);
  this.ac.numItems = 0;
  /**
   * Add a new Item to the Accordion
   * @method Add
   * @param {object} item a GRNOC.widget.AccordionItem to be added
   */
  
  this.ac._do_resize = function(event,ref){
    var height=0;
    for(var i=0;i<this.numItems;i++){
      var item = this.getItem(i);
      if(item.get("expanded")){
        var body = item.getStdModNode( Y.WidgetStdMod.BODY );
	var bodyContent = body.get( "children" ).item(0);
	if(bodyContent){
	  var region = Y.one(bodyContent._node).get('region');
	  height += region.bottom - region.top;
	}
	
	var region = YAHOO.util.Dom.getRegion(item.headerNode._node);
	height += region.bottom - region.top;;
	
	//height += bodyContent ? this._getNodeOffsetHeight( bodyContent ) : 0;
      }else{			 
	var region = YAHOO.util.Dom.getRegion(item.headerNode._node);
	height += region.bottom - region.top;
      }
    }
    
    Y.log("height: " + height);
    this.set("height",(height + 10) + "px");
  };
  
  this.ac.on("itemResized",function(event){
	       this._do_resize();
	     });
  
  this.ac.Add = function(item){
    this.addItem(item);
    this.numItems++;
    
    item.on("expandedChange",function(event){
		     this._do_resize();
		   },this);

    item.after("resize",function(event){
		     this._do_resize();
		   },this);
  
    item.after("contentHeightChange",function(event){
		 this._do_resize();
	       },this);
    
  };
  
  return this.ac;
  
};