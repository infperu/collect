package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.detail.MultipleAttributeFormItem;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.UIUtil;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class MultipleAttributeFormItemPresenter extends AttributeFormItemPresenter {
		
		public function MultipleAttributeFormItemPresenter(view:MultipleAttributeFormItem) {
			super(view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			MultipleAttributeFormItem(_view).addButton.addEventListener(MouseEvent.CLICK, addButtonClickHandler);
			MultipleAttributeFormItem(_view).addButton.addEventListener(FocusEvent.FOCUS_IN, addButtonFocusInHandler);
		}
		
		private function get view():MultipleAttributeFormItem {
			return MultipleAttributeFormItem(_view);
		}
		
		override protected function updateView():void {
			if(view.dataGroup != null) {
				if(view.parentEntity != null) {
					var name:String = view.attributeDefinition.name
					var attributes:IList = view.parentEntity.getChildren(name);
					view.dataGroup.dataProvider = attributes;
				}
			}
		}

		protected function addButtonFocusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function addButtonClickHandler(event:MouseEvent):void {
			var req:UpdateRequest = new UpdateRequest();
			req.method = UpdateRequest$Method.ADD;
			req.parentNodeId = _view.parentEntity.id;
			req.nodeName = _view.attributeDefinition.name;
			ClientFactory.dataClient.updateActiveRecord(new AsyncResponder(addResultHandler, faultHandler, null), req);
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			var newAttribute:AttributeProxy = result.getItemAt(0) as AttributeProxy;
			_view.parentEntity.addChild(newAttribute);
			updateView();
		}
		
	}
}