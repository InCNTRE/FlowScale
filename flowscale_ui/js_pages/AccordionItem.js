/**
 * GRNOC Accordion
 * @module Accordion
 */

GRNOC.add_namespace("GRNOC.widget");

/**
 * Accordion Item 
 * @class AccordionItem
 * @namespace GRNOC.widget
 * @constructor
 * @param {object} object (see YUI 3 AccordionItem)
 */

GRNOC.widget.AccordionItem = function(obj){
  
  this.item = new Y.AccordionItem(obj);

/*  this.item.subscribe('expandedChange',function(o,args){
			if(this.get('expanded')){
			  Y.one(this.bodyNode._node).removeClass("GRNOC-ACCORDION-EXPANDED");
			  //YAHOO.util.Dom.removeClass(this.bodyNode._node,"GRNOC-ACCORDION-EXPANDED");
			}else{
			  Y.one(this.bodyNode._node).addClass("GRNOC-ACCORDION-EXPANDED");
			  //YAHOO.util.Dom.addClass(this.bodyNode._node,"GRNOC-ACCORDION-EXPANDED");
			}
		      });
  */
  if(obj.dataSrc){
    if(obj.dynamicLoad){
      if(obj.expanded){
	
	if(this.item._loaded){
	
	
	}else{
	  Y.io(obj.dataSrc,{on: {complete: function(o,args,ref){
				   ref.item.set('bodyContent',args.responseText);
				   ref.item.resize();
				   var content = new Y.Node(ref.item.bodyNode);
				   YAHOO.util.Event.on(ref.item.bodyNode._node,"change",function(){
							 ref.item.resize();
							 ref.item.fire("resize");
						       });
				   var scripts = content.getElementsByTagName("script")._nodes;
				   for (var j = 0; j < scripts.length; j++){
				     eval(scripts[j].innerHTML);
				   }
				   ref.item._loaded = true;
				   ref.item.fire("resize");
				 }},arguments: this});
	}
      }

      this.item.subscribe('expandedChange',function(o,args){
			    if(this.item._loaded){
			      
			    }else{
			      Y.io(obj.dataSrc,{on: {complete: function(o,args,ref){
						       ref.item.set('bodyContent',args.responseText);
						       ref.item.resize();
						       var content = new Y.Node(ref.item.bodyNode);
						       YAHOO.util.Event.on(ref.item.bodyNode._node,"change",function(){
									     ref.item.resize();
									     ref.item.fire("resize");
									   });
						       var scripts = content.getElementsByTagName("script")._nodes;
						       for (var j = 0; j < scripts.length; j++){
							 eval(scripts[j].innerHTML);
						       }
						       ref.item._loaded = true;
						       ref.item.fire("resize");
						     }},arguments: this});
			    }
			  },this);
      }else{
	Y.io(obj.dataSrc,{on: {complete: function(o,args,ref){
				 
				 ref.item.set('bodyContent',args.responseText);
				 ref.item.resize();
				 var content = new Y.Node(ref.item.bodyNode);
				 YAHOO.util.Event.on(ref.item.bodyNode._node,"change",function(){
						       
						       ref.item.resize();
						       ref.item.fire("resize");
						     });
				 var scripts = content.getElementsByTagName("script")._nodes;
				 for (var j = 0; j < scripts.length; j++){  
				   eval(scripts[j].innerHTML);
				 }
				 ref.item._loaded = true;
				 ref.item.fire("resize");
				 if(ref.item.get('expanded')){
//				   Y.one(ref.item.bodyNode._node).addClass("GRNOC-ACCORDION-EXPANDED");
				 }
			       }},arguments: this});
      }
    }

  
  /**
   * Fires a resize event when the content dynamically 
   * changes size
   * @event resize
   */
  
  return this.item;
    
};