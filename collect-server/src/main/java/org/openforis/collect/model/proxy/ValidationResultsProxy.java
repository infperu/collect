/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.model.validation.SpecifiedValidator;
import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class ValidationResultsProxy implements Proxy {

	private transient Attribute<?, ?> attribute;
	private transient ValidationResults validationResults;
	private transient MessageSource messageSource;
	private transient Locale locale;

	public ValidationResultsProxy(MessageSource messageSource, Locale locale, Attribute<?, ?> attribute, ValidationResults validationResults) {
		this.messageSource = messageSource;
		this.attribute = attribute;
		this.validationResults = validationResults;
		this.locale = locale;
	}

	@ExternalizedProperty
	public List<String> getErrors() {
		return extractValidationResultMessages(validationResults.getErrors());
	}

	@ExternalizedProperty
	public List<String> getWarnings() {
		return extractValidationResultMessages(validationResults.getWarnings());
	}

	@ExternalizedProperty
	public boolean isSpecifiedErrorPresent() {
		List<ValidationResult> errors = validationResults.getErrors();
		for (ValidationResult validationResult : errors) {
			if(validationResult.getValidator() instanceof SpecifiedValidator) {
				return true;
			}
		}
		return false;
	}
	
	private List<String> extractValidationResultMessages(List<ValidationResult> validationResultList) {
		List<String> result = new ArrayList<String>();
		ValidationMessageBuilder validationMessageBuilder = ValidationMessageBuilder.createInstance(messageSource);
		for (ValidationResult validationResult : validationResultList) {
			result.add(validationMessageBuilder.getValidationMessage(attribute, validationResult, locale));
		}
		return result;
	}
}
