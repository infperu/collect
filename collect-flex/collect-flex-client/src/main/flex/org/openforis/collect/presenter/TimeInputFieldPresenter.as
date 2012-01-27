package org.openforis.collect.presenter {
	import flash.events.FocusEvent;
	
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TimeInputField;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class TimeInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:TimeInputField;
		
		public function TimeInputFieldPresenter(inputField:TimeInputField = null) {
			_view = inputField;
			super(inputField);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.hoursTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.minutesTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.hoursTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.minutesTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.hoursTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.minutesTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
		}
		
		override public function set value(value:*):void {
		}
		
		override public function createValue():* {
			var result:* = null;
			return result;
			/*
			var newAttributeValue:AbstractValue = new AbstractValue();
			newAttributeValue.text1 = _timeInputField.hoursTextInput.text;
			newAttributeValue.text2 = _timeInputField.minutesTextInput.text;
			
			if(value != null) {
				//copy old informations
				newAttributeValue.remarks = value.remarks;
			}
			return newAttributeValue;
			*/
		}
		
		protected function setTimeOnFields(time:Object):void {
			_view.hoursTextInput.text = String(time.hour);
			_view.minutesTextInput.text = String(time.minute);
		}
		
		protected function getTimeFromFields():Date {
			//check if input text is valid
			if(StringUtil.isNotBlank(_view.hoursTextInput.text) && 
				StringUtil.isNotBlank(_view.minutesTextInput.text)) {
			}
			return null;
		}
		
	}
}
