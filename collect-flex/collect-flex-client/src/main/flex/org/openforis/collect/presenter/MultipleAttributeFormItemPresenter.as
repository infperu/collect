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
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.remoting.service.UpdateResponse;
	import org.openforis.collect.ui.component.detail.MultipleAttributeFormItem;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.UIUtil;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class MultipleAttributeFormItemPresenter extends FormItemPresenter {
		
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
		
		override protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			if(view.dataGroup.dataProvider == null) {
				updateView();
			}
		}
		
		override protected function updateView():void {
			if(view.dataGroup != null && view.parentEntity != null) {
				var attributes:IList = getAttributes();
				view.dataGroup.dataProvider = attributes;
			}
		}

		protected function getAttributes():IList {
			var name:String = view.attributeDefinition.name;
			var attributes:IList = view.parentEntity.getChildren(name);
			return attributes;
		}
		
		protected function addButtonFocusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function addButtonClickHandler(event:MouseEvent):void {
			var attributes:IList = getAttributes();
			var maxCount:Number = view.attributeDefinition.maxCount
			if(isNaN(maxCount) || CollectionUtil.isEmpty(attributes) || attributes.length < maxCount) {
				var req:UpdateRequest = new UpdateRequest();
				req.method = UpdateRequest$Method.ADD;
				req.parentEntityId = view.parentEntity.id;
				req.nodeName = view.attributeDefinition.name;
				ClientFactory.dataClient.updateActiveRecord(new AsyncResponder(addResultHandler, faultHandler, null), req);
			} else {
				var labelText:String = view.attributeDefinition.getLabelText();
				AlertUtil.showError("edit.maxCountExceed", [maxCount, labelText]);
			}	
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			var response:UpdateResponse = UpdateResponse(event.result);
			Application.activeRecord.update(response);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = response;
			eventDispatcher.dispatchEvent(appEvt);
			
			view.callLater(function():void {
				UIUtil.ensureElementIsVisible(view.addButton);
			});
		}
		
	}
}