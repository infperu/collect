/**
 * Generated by Gas3 v2.2.0 (Granite Data Services).
 *
 * NOTE: this file is only generated if it does not exist. You may safely put
 * your custom code here.
 */

package org.openforis.collect.model.proxy {
	import mx.collections.ArrayCollection;
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.collections.Sort;
	
	import org.granite.collections.IMap;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NumberAttributeDefinitionProxy;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.ObjectUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;

	/**
	 * @author S. Ricci
	 */
    [Bindable]
    [RemoteClass(alias="org.openforis.collect.model.proxy.EntityProxy")]
    public class EntityProxy extends EntityProxyBase {
		
		private static const _SORT:Sort = new Sort();
		
		private static const KEY_LABEL_SEPARATOR:String = "-";
		private static const FULL_KEY_LABEL_SEPARATOR:String = " ";
		
		private var _keyText:String;
		private var _fullKeyText:String;
		private var _compactKeyText:String;
		private var _enumeratedEntitiesCodeWidths:Array;
		
		override public function init():void {
			super.init();
			updateEnumeratedCodeWidths();
			updateKeyText();
		}
		
		protected function updateEnumeratedCodeWidths():void {
			_enumeratedEntitiesCodeWidths = new Array();
			var entities:IList = getChildEntities();
			for each (var e:EntityProxy in entities) {
				if(e.enumerated) {
					var name:String = e.name;
					var maxWidth:Number = _enumeratedEntitiesCodeWidths[name];
					var keyAttribute:CodeAttributeProxy = e.getKeyAttribute();
					if(keyAttribute != null && keyAttribute.codeListItem != null) {
						var label:String = keyAttribute.codeListItem.getLabelText();
						//var width:Number = label.length * 7;
						var width:Number = UIUtil.measureFixedCodeWidth(label);
						if(keyAttribute.codeListItem.qualifiable) {
							width += 104; //space for qualifier text input
						}
						if(!isNaN(maxWidth)) {
							maxWidth = Math.max(maxWidth, width);
						} else {
							maxWidth = width;
						}
					}
					_enumeratedEntitiesCodeWidths[name] = maxWidth;
				}
			}
		}
		
		protected function getKeyAttribute():CodeAttributeProxy {
			var children:IList = getChildren();
			for each (var child:NodeProxy in children) {
				if(child is CodeAttributeProxy) {
					var codeAttribute:CodeAttributeProxy = CodeAttributeProxy(child);
					if(codeAttribute.enumerator) {
						return codeAttribute;
					}
				}
			}
			return null;
		}
		
		/**
		 * Traverse each child and pass it to the argument function
		 * */
		public function traverse(funct:Function):void {
			var stack:Array = new Array();
			var children:IList = getChildren();
			ArrayUtil.addAll(stack, children.toArray());
			while ( stack.length > 0 ) {
				var node:NodeProxy = NodeProxy(stack.pop());
				funct(node);
				if ( node is EntityProxy ) {
					children = EntityProxy(node).getChildren();
					ArrayUtil.addAll(stack, children.toArray());
				}
			}
		}
		
		override protected function setParentReferencesOnChildren():void {
			var children:IList = getChildren();
			for each (var child:NodeProxy in children) {
				child.parent = this;
			}
		}
		
		public function getSingleAttribute(attributeName:String):AttributeProxy {
			var attributes:IList = childrenByName.get(attributeName);
			if(attributes != null) {
				if(attributes.length == 1) {
					var attribute:AttributeProxy = attributes.getItemAt(0) as AttributeProxy;
					return attribute;
				} else if (attributes.length > 1) {
					throw new Error("Single attribute expected");
				}
			}
			return null;
		}
		
		public function getDescendantSingleAttribute(defnId:int):AttributeProxy {
			var leafAttrs:IList = getLeafAttributes();
			for each (var attr:AttributeProxy in leafAttrs) {
				if ( attr.definition.id == defnId ) {
					return attr;
				}
			}
			return null;
		}
		
		public function getChildren(nodeName:String = null):IList {
			var result:ArrayCollection;
			if(nodeName != null) {
				result = childrenByName.get(nodeName);
			} else {
				result = new ArrayCollection();
				var childDefns:IList = EntityDefinitionProxy(definition).childDefinitions;
				for each (var childDefn:NodeDefinitionProxy in childDefns) {
					var childrenPart:IList = childrenByName.get(childDefn.name);
					if ( CollectionUtil.isNotEmpty(childrenPart) ) {
						result.addAll(childrenPart);
					}
				}
			} 
			return result;
		}
		
		public function getDescendants(descendantDefn:NodeDefinitionProxy):IList {
			var relativePath:String = descendantDefn.getRelativePath(EntityDefinitionProxy(this.definition));
			var parts:Array = relativePath.split("/");
			var currentLevelNodes:IList = new ArrayCollection();
			currentLevelNodes.addItem(this);
			for each (var part:String in parts) {
				var nextLevelNodes:IList = new ArrayCollection();
				for each (var currentNode:NodeProxy in currentLevelNodes) {
					if ( currentNode is EntityProxy ) {
						var childrenPart:IList = EntityProxy(currentNode).getChildren(part);
						CollectionUtil.addAll(nextLevelNodes, childrenPart);
					} else {
						throw new Error("Entity expected, found: " + currentNode);
					}
				}
				currentLevelNodes = nextLevelNodes;
			}
			return currentLevelNodes;
		}
		
		public function getLeafAttributes():IList {
			var result:ArrayCollection = new ArrayCollection();
			var stack:Array = new Array();
			var children:IList = getChildren();
			ArrayUtil.addAll(stack, children.toArray());
			while ( stack.length > 0 ) {
				var node:NodeProxy = NodeProxy(stack.pop());
				if ( node is EntityProxy ) {
					children = EntityProxy(node).getChildren();
					ArrayUtil.addAll(stack, children.toArray());
				} else {
					result.addItemAt(node, 0);
				}
			}
			return result;
		}
		
		/**
		 * For backwards compatibility: it can happen that the "parent" entity can be a single entity (deprecated),
		 * so the effective parent entity must be this single entity and not the ancestor entity
		 */
		public function getDescendantNearestParentEntity(descendantDefn:NodeDefinitionProxy):EntityProxy {
			var nearestParentDefn:EntityDefinitionProxy = descendantDefn.parent;
			if ( this.definition == nearestParentDefn ) {
				return this;
			} else {
				var effectiveParentEntities:IList = this.getDescendants(nearestParentDefn);
				if ( effectiveParentEntities.length == 1 ) {
					return EntityProxy(effectiveParentEntities.getItemAt(0));
				} else {
					return this;
				}
			}
		}
		
		
		public function getLeafFields():IList {
			var result:IList = new ArrayCollection();
			var leafAttributes:IList = getLeafAttributes();
			for each (var a:AttributeProxy in leafAttributes) {
				var fields:ListCollectionView = a.fields;
				CollectionUtil.addAll(result, fields);
			}
			return result;
		}
		
		public static function sortEntitiesByKey(entities:IList):IList {
			var result:ArrayCollection = null;
			if ( entities != null ) {
				result = new ArrayCollection(entities.toArray());
				var sort:Sort = new Sort();
				sort.compareFunction = entitiesKeyCompareFunction;
				result.sort = sort;
				result.refresh();
				//to prevent issue due to use of a custom sort function in data providers...
				result = new ArrayCollection(result.toArray());
			}
			return result;
		}
		
		protected static function entitiesKeyCompareFunction(entity1:EntityProxy, entity2:EntityProxy, fields:Array = null):int {
			if ( entity1 == null && entity2 == null ) {
				return 0;
			} else if ( entity1 == null ) {
				return -1;
			} else if ( entity2 == null ) {
				return 1;
			} else if ( entity1 == entity2 ) {
				return 0;
			} else {
				var keyValues1:Array = entity1.getKeyValues();
				if ( keyValues1.length > 0 ) {
					var keyValues2:Array = entity2.getKeyValues();
					for (var i:int = 0; i < keyValues1.length; i++) {
						var keyValue1:Object = keyValues1[i];
						var keyValue2:Object = keyValues2[i];
						var compareResult:int = compareKeyValues(keyValue1, keyValue2, fields);
						if ( compareResult != 0 ) {
							return compareResult;
						}
					}
				} else {
					return _SORT.compareFunction.call(null, entity1.index, entity2.index, fields);
				}
			}
			return 0;
		}
	
		protected static function compareKeyValues(keyValue1:Object, keyValue2:Object, fields:Array = null):int {
			if ( ObjectUtil.isNumber(keyValue1) && ObjectUtil.isNumber(keyValue2) ) {
				keyValue1 = ObjectUtil.toNumber(keyValue1);
				keyValue2 = ObjectUtil.toNumber(keyValue2);
			}
			var partialCompareResult:int = _SORT.compareFunction.call(null, keyValue1, keyValue2, fields);
			return partialCompareResult;
		}
		
		public function getChild(nodeName:String, index:int):NodeProxy {
			var children:IList = getChildren(nodeName);
			if(children != null && children.length > index) {
				return children.getItemAt(index) as NodeProxy;
			} else {
				return null;
			}
		}
		
		public function getChildEntities():IList {
			var entities:IList = new ArrayCollection();
			var values:IList = childrenByName.values;
			for each (var childList:IList in values) {
				for each (var child:NodeProxy in childList) {
					if(child is EntityProxy) {
						entities.addItem(child);
					}
				}
			}
			return entities;
		}
		
		public function addChild(node:NodeProxy):void {
			var name:String = node.name;
			var children:ArrayCollection = childrenByName.get(name);
			if(children == null) {
				children = new ArrayCollection();
				childrenByName.put(name, children);
			}
			node.parent = this;
			node.init();
			children.addItem(node);
			showErrorsOnChild(name);
		}
		
		public function removeChild(node:NodeProxy):void {
			var name:String = node.name;
			var children:IList = childrenByName.get(name);
			var index:int = children.getItemIndex(node);
			if(index >= 0) {
				children.removeItemAt(index);
			}
			showErrorsOnChild(name);
		}
		
		public function replaceChild(oldNode:NodeProxy, newNode:NodeProxy):void {
			var name:String = oldNode.name;
			var children:ArrayCollection = childrenByName.get(name);
			var index:int = children.getItemIndex(oldNode);
			children.setItemAt(newNode, index);
		}
		
		public function moveChild(node:NodeProxy, index:int):void {
			var children:IList = getChildren(node.name);
			CollectionUtil.moveItem(children, node, index);
			for each (var child:NodeProxy in children){
				child.updateIndex();
			}
		}
		
		public function updateKeyText():void {
			var keyDefs:IList = EntityDefinitionProxy(definition).keyAttributeDefinitions;
			if(keyDefs.length > 0) {
				var shortKeyParts:Array = new Array();
				var fullKeyParts:Array = new Array();
				for each (var def:AttributeDefinitionProxy in keyDefs) {
					var keyAttr:AttributeProxy = getSingleAttribute(def.name);
					if(keyAttr != null) {
						var keyValue:Object = getKeyLabelPart(def, keyAttr);
						if(keyValue != null && StringUtil.isNotBlank(keyValue.toString())) {
							shortKeyParts.push(keyValue.toString());
							var label:String = def.getInstanceOrHeadingLabelText();
							var fullKeyPart:String = label + " " + keyValue;
							fullKeyParts.push(fullKeyPart);
						}
					}
				}
				keyText = StringUtil.concat(KEY_LABEL_SEPARATOR, shortKeyParts);
				fullKeyText = StringUtil.concat(FULL_KEY_LABEL_SEPARATOR, fullKeyParts);
			} else if(parent != null) {
				var siblings:IList = parent.getChildren(name);
				var itemIndex:int = siblings.getItemIndex(this);
				keyText = String(itemIndex + 1);
				fullKeyText = keyText;
			}
		}
		
		protected function getKeyValues():Array {
			var result:Array = new Array();
			var keyDefs:IList = EntityDefinitionProxy(definition).keyAttributeDefinitions;
			if(keyDefs.length > 0) {
				for each (var def:AttributeDefinitionProxy in keyDefs) {
					var keyAttr:AttributeProxy = getSingleAttribute(def.name);
					var keyValue:Object = getKeyLabelPart(def, keyAttr);
					result.push(keyValue);
				}
			}
			return result;
		}
		
		private function getKeyLabelPart(attributeDefn:AttributeDefinitionProxy, attribute:AttributeProxy):Object {
			var result:Object = null;
			var f:FieldProxy = attribute.getField(0);
			var value:Object = f.value;
			if(ObjectUtil.isNotNull(value)) {
				if(attributeDefn is NumberAttributeDefinitionProxy) {
					var numberDefn:NumberAttributeDefinitionProxy = NumberAttributeDefinitionProxy(attributeDefn);
					if ( numberDefn.integer ) {
						result = int(value);
					} else {
						result = Number(value);
					}
				} else {
					result = value;
				}
			}
			return result;
		}
		
		public function updateChildrenMinCountValiditationMap(map:IMap):void {
			updateMap(childrenMinCountValidationMap, map);
		}
		
		public function updateChildrenMaxCountValiditationMap(map:IMap):void {
			updateMap(childrenMaxCountValidationMap, map);
		}

		public function updateChildrenRelevanceMap(map:IMap):void {
			updateMap(childrenRelevanceMap, map);
		}

		public function updateChildrenRequiredMap(map:IMap):void {
			updateMap(childrenRequiredMap, map);
		}
		
		public function showErrorsOnChild(name:String):void {
			showChildrenErrorsMap.put(name, true);
		}
		
		public function isErrorOnChildVisible(name:String):Boolean {
			var result:Boolean = showChildrenErrorsMap.get(name);
			return result;
		}
		
		public function isRequired(childName:String):Boolean {
			var required:Boolean = childrenRequiredMap.get(childName);
			return required == true;
		}
		
		public function get childDefinitionNames():IList {
			//taken from showChildrenErrorsMap that is fully populated from the server
			//with an entry for each child definition
			var names:ArrayCollection = showChildrenErrorsMap.keySet;
			return names;
		}
		
		override public function hasErrors():Boolean {
			var children:IList = getChildren();
			for each(var child:NodeProxy in children){
				if( child.hasErrors() ) {
					return true;
				}
			}
			return false;
		}
		
		public function childContainsErrors(childName:String):Boolean {
			var children:IList = getChildren(childName);
			for each(var child:NodeProxy in children){
				if( child.hasErrors() ) {
					return true;
				}
			}
			return false;
		}
		
		public function hasConfirmedError(childName:String):Boolean {
			var children:IList = getChildren(childName);
			for each(var child:NodeProxy in children){
				if(child is AttributeProxy){
					var attr:AttributeProxy = child as AttributeProxy;
					if( !attr.errorConfirmed ){
						return false;
					}
				} else {
					return false;
				}
			}
			return true;
		}
		
		public function getEnumeratedCodeWidth(entityName:String):Number {
			var result:Number = _enumeratedEntitiesCodeWidths[entityName];
			return result;
		}
		
		public function getCount(childName:String):int {
			var children:IList = getChildren(childName);
			return children.length;
		}
		
		override public function get empty():Boolean {
			var children:IList = getChildren();
			for each (var child:NodeProxy in children ) {
				if ( ! child.empty ) {
					return false;
				}
			}
			return true;
		}
		
		public function hasDescendantWithBlankField():Boolean {
			var children:IList = getChildren();
			var nodes:Array = children.toArray();
			while ( nodes.length > 0 ) {
				var node:NodeProxy = NodeProxy(nodes.pop());
				if ( node is AttributeProxy && AttributeProxy(node).hasBlankField() ) {
					return true;
				} else if (node is EntityProxy) {
					var descendants:IList = EntityProxy(node).getChildren();
					nodes = nodes.concat(descendants.toArray());
				}
			}
			return false;
		}
		
		protected function updateMap(map:IMap, newMap:IMap):void {
			if(map != null && newMap != null) {
				var newKeys:ArrayCollection = newMap.keySet;
				for each (var key:* in newKeys) {
					var value:* = newMap.get(key);
					if(value != null) {
						map.put(key, value);
					}
				}
			}
		}
		
		/*
		* GETTERS AND SETTERS
		*/
		public function get keyText():String {
			return _keyText;
		}
		
		public function set keyText(value:String):void {
			_keyText = value;
		}
		
		public function get fullKeyText():String {
			return _fullKeyText;
		}
		
		public function set fullKeyText(value:String):void {
			_fullKeyText = value;
		}
		
		public function get enumeratedEntitiesCodeWidths():Array {
			return _enumeratedEntitiesCodeWidths;
		}

		public function set enumeratedEntitiesCodeWidths(value:Array):void {
			_enumeratedEntitiesCodeWidths = value;
		}

	}
}