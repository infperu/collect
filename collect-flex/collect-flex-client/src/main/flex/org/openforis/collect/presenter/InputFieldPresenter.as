package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import mx.core.UIComponent;
	
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author M. Togna
	 * @author S. Ricci
	 * */
	public class InputFieldPresenter extends AbstractPresenter {
		
		protected var _path:String;
		private var _view:InputField;
		
		protected var _attributeValue:*;
		
		public function InputFieldPresenter(inputField:InputField = null) {
			_view = inputField;
			super();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
			_view.addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);
			
			if(_view.textInput != null) {
				_view.textInput.addEventListener(Event.CHANGE, changeHandler);
				_view.textInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
				_view.textInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			}
		}
		
		protected function changeHandler(event:Event):void {
			//if autocomplete enabled show autocomplete popup...
			
		}
		
		protected function focusOutHandler(event:FocusEvent):void {
			//TO DO
			applyChanges();
		}
		
		protected function mouseOverHandler(event:MouseEvent):void {
			var target:UIComponent = event.currentTarget as UIComponent;
			if(target != null && target.document != null) {
				var inputFieldEvent:InputFieldEvent = new InputFieldEvent(InputFieldEvent.INPUT_FIELD_MOUSE_OVER);
				inputFieldEvent.inputField = target.document as InputField;
				eventDispatcher.dispatchEvent(inputFieldEvent);
			}
		}
		
		protected function mouseOutHandler(event:MouseEvent):void {
			var target:UIComponent = event.currentTarget as UIComponent;
			if(target != null && target.document != null) {
				var inputFieldEvent:InputFieldEvent = new InputFieldEvent(InputFieldEvent.INPUT_FIELD_MOUSE_OUT);
				inputFieldEvent.inputField = target.document as InputField;
				eventDispatcher.dispatchEvent(inputFieldEvent);
			}
		}

		protected function applyChanges(newAttributeValue:* = null):void {
			//prepare request
			if(newAttributeValue == null) {
				newAttributeValue = createValue();
			}
			//send request to server and wait for the answer...
		}
		
		protected function focusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function modelSyncResultHandler(event:Event):void {
			//if event.path == this.path (or what else)
			//update input field
			this.value = null;
			
			_view.currentState = InputField.STATE_SAVE_COMPLETE;
		}

		protected function modelSyncErrorHandler(event:Event):void {
			_view.currentState = InputField.STATE_ERROR_SAVING;
		}
		
		public function get value():* {
			return _attributeValue;
		}

		public function set value(value:*):void {
			_attributeValue = value;
			/*
			if(_attributeValue != null && _attributeValue.path == this._path) {
				//this._inputField.attribute = attribute;
				this._inputField.error = _attributeValue.error;
				this._inputField.warning = _attributeValue.warning;
				this._inputField.approved = _attributeValue.approved;
				this._inputField.remarks = _attributeValue.remarks;
			}
			*/
		}

		public function createValue():* {
			var result:* = null;
			return result;
			/*
			var newAttributeValue:* = new AbstractValue();
			newAttributeValue.text1 = _inputField.text;
			if(value != null) {
				//copy old informations
				newAttributeValue.remarks = value.remarks;
			}
			return newAttributeValue;
			*/
		}
		
		public function get path():String {
			return _path;
		}

		public function set path(value:String):void {
			_path = value;
		}
		
		public function changeReasonBlank(symbol:*):void {
			/*
			var newAttributeValue:AbstractValue = new AbstractValue();
			newAttributeValue.symbol = symbol;
			if(_attributeValue != null) {
				//copy old value infos
				newAttributeValue.remarks = _attributeValue.remarks;
			}
			applyChanges(newAttributeValue);
			*/
		}
		
	}
}
