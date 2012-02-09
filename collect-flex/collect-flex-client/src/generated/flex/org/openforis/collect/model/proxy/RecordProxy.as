/**
 * Generated by Gas3 v2.2.0 (Granite Data Services).
 *
 * NOTE: this file is only generated if it does not exist. You may safely put
 * your custom code here.
 */

package org.openforis.collect.model.proxy {
	import mx.collections.IList;
	
	import org.granite.collections.IMap;

    [Bindable]
    [RemoteClass(alias="org.openforis.collect.model.proxy.RecordProxy")]
    public class RecordProxy extends RecordProxyBase {
		
		public function getNode(id:int):NodeProxy {
			return rootEntity.getNode(id);
		}
		
		public function update(nodes:IList):void {
			for each (var node:NodeProxy in nodes) {
				var parent:NodeProxy = getNode(node.parentId);
				if(parent is EntityProxy) {
					var parentEntity:EntityProxy = EntityProxy(parent);
					var oldNode:NodeProxy = parentEntity.getChildById(node.id);
					if(oldNode == null) {
						//add node
						parentEntity.addChild(node);
					} else {
						//update node
						var children:IList = parentEntity.getChildren(node.name);
						var index:int = children.getItemIndex(oldNode);
						children.removeItemAt(index);
						children.addItemAt(node, index);
					}
				} else {
					throw new Error("Entity expected");
				}
			}
			
		}
    }
}