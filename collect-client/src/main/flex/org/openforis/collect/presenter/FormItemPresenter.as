package org.openforis.collect.presenter
{
	import flash.events.Event;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.events.PropertyChangeEvent;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.NodeEvent;
	import org.openforis.collect.metamodel.proxy.SchemaProxy;
	import org.openforis.collect.model.proxy.AttributeChangeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.NodeChangeProxy;
	import org.openforis.collect.model.proxy.NodeChangeSetProxy;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.ui.component.detail.CollectFormItem;
	import org.openforis.collect.ui.component.detail.RelevanceDisplayManager;
	import org.openforis.collect.ui.component.detail.ValidationDisplayManager;
	import org.openforis.collect.ui.component.input.FormItemContextMenu;
	import org.openforis.collect.util.UIUtil;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class FormItemPresenter extends AbstractPresenter {
		
		protected var _validationDisplayManager:ValidationDisplayManager;
		protected var _relevanceDisplayManager:RelevanceDisplayManager;
		private var _contextMenu:FormItemContextMenu;
		
		{
			eventDispatcher.addEventListener(NodeEvent.MOVE, nodeMoveHandler);
		}
		
		public function FormItemPresenter(view:CollectFormItem) {
			super(view);
			_relevanceDisplayManager = new RelevanceDisplayManager(view);
			_contextMenu = new FormItemContextMenu(view);
		}
		
		private function get view():CollectFormItem {
			return CollectFormItem(_view);
		}
		
		override public function init():void {
			super.init();
			updateView();
		}
		
		private static function nodeMoveHandler(event:NodeEvent):void {
			var schema:SchemaProxy = Application.activeSurvey.schema;
			var record:RecordProxy = Application.activeRecord;
			var node:NodeProxy = event.node;
			var index:int = event.index;
			var parent:EntityProxy = EntityProxy(record.getNode(node.parentId));
			parent.moveChild(node, index);
			var nodeId:int = node.id;
			var responder:IResponder = new AsyncResponder(moveResultHandler, faultHandler); 
			ClientFactory.dataClient.moveNode(responder, nodeId, index);
		}
		
		private static function moveResultHandler(event:ResultEvent, token:Object = null):void {
			//do nothing
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			
			ChangeWatcher.watch(view, "parentEntity", parentEntityChangeHandler);
			
			view.addEventListener(Event.REMOVED_FROM_STAGE, removedFromStageHandler);
		}
		
		override protected function initBroadcastEventListeners():void {
			super.initBroadcastEventListeners();
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
			eventDispatcher.addEventListener(ApplicationEvent.RECORD_SAVED, recordSavedHandler);
			eventDispatcher.addEventListener(ApplicationEvent.ASK_FOR_SUBMIT, askForSubmitHandler);
		}
		
		override protected function removeBroadcastEventListeners():void {
			super.removeBroadcastEventListeners();
			eventDispatcher.removeEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
			eventDispatcher.removeEventListener(ApplicationEvent.RECORD_SAVED, recordSavedHandler);
			eventDispatcher.removeEventListener(ApplicationEvent.ASK_FOR_SUBMIT, askForSubmitHandler);
		}
		
		protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			if(view.parentEntity != null) {
				var changeSet:NodeChangeSetProxy = NodeChangeSetProxy(event.result);
				for each (var change:NodeChangeProxy in changeSet.changes) {
					if ( change.nodeId == view.parentEntity.id) {
						updateValidationDisplayManager();
						updateRelevanceDisplayManager();
						_contextMenu.updateItems();
						break;
					}
				}
			}
		}
		
		protected function recordSavedHandler(event:ApplicationEvent):void {
			updateValidationDisplayManager();
		}
		
		protected function askForSubmitHandler(event:ApplicationEvent):void {
			updateValidationDisplayManager();
		}
		
		protected function parentEntityChangeHandler(event:PropertyChangeEvent):void {
			updateView();
		}
		
		protected function updateView():void {
			updateRelevanceDisplayManager();
			updateValidationDisplayManager();
			_contextMenu.updateItems();
		}
		
		protected function initValidationDisplayManager():void {
			_validationDisplayManager = new ValidationDisplayManager(view, view);
		}
		
		protected function updateRelevanceDisplayManager():void {
			
		}
		
		protected function updateValidationDisplayManager():void {
			if(_validationDisplayManager == null) {
				initValidationDisplayManager();
			}
		}
		
		protected function get validationDisplayManager():ValidationDisplayManager {
			return _validationDisplayManager;
		}
		
		protected function get relevanceDisplayManager():RelevanceDisplayManager {
			return _relevanceDisplayManager;
		}
	}
}