/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Field;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 * */
public class AttributeProxy extends NodeProxy {

	private transient Attribute<?, ?> attribute;
	private ValidationResultsProxy validationResults;
	private boolean errorConfirmed;
	
	public AttributeProxy(EntityProxy parent, Attribute<?, ?> attribute, Locale locale) {
		super(parent, attribute, locale);
		this.attribute = attribute;
		ValidationResults validationRes = attribute.getValidationResults();
		if ( validationRes == null ) {
			validationRes = new ValidationResults();
		}
		this.validationResults = new ValidationResultsProxy(getMessageSource(), locale, attribute, validationRes);
		this.errorConfirmed = ((CollectRecord) attribute.getRecord()).isErrorConfirmed(attribute);
	}

	public ValidationResultsProxy getValidationResults(){
		return validationResults;
	}

	public void setValidationResults(ValidationResultsProxy value) {
		this.validationResults = value;
	}

	@ExternalizedProperty
	public List<FieldProxy> getFields() {
		List<Field<?>> fields = attribute.getFields();
		List<FieldProxy> result = new ArrayList<FieldProxy>(fields.size());
		for (Field<?> field : fields) {
			result.add(new FieldProxy(field));
		}
		return result;
	}
	
	public boolean isErrorConfirmed() {
		return errorConfirmed;
	}
	
	public void setErrorConfirmed(boolean value) {
		this.errorConfirmed = value;
	}

}
