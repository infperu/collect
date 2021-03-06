/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;

/**
 * @author M. Togna
 * 
 */
public abstract class AttributeDefinitionProxy extends NodeDefinitionProxy implements Proxy {

	private transient AttributeDefinition attributeDefinition;

	public AttributeDefinitionProxy(EntityDefinitionProxy parent, AttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}

	@ExternalizedProperty
	public boolean isKey() {
		if(this.attributeDefinition instanceof KeyAttributeDefinition) {
			return ((KeyAttributeDefinition) this.attributeDefinition).isKey(); 
		} else {
			return false;
		}
	}
	
	@ExternalizedProperty
	public boolean isDefaultValueApplicable() {
		return ! attributeDefinition.getAttributeDefaults().isEmpty();
	}
	
	@ExternalizedProperty
	public boolean isAutocomplete() {
		if ( attributeDefinition instanceof TextAttributeDefinition ) {
			String autoCompleteGroup = getAnnotations().getAutoCompleteGroup((TextAttributeDefinition) attributeDefinition);
			return StringUtils.isNotBlank(autoCompleteGroup);
		} else {
			return false;
		}
	}
	
	@ExternalizedProperty
	public List<Integer> getVisibleFieldIndexes() {
		List<Integer> result = new ArrayList<Integer>();
		UIOptions uiOptions = getUIOptions();
		String[] fieldNames = uiOptions.getVisibleFields(attributeDefinition);
		for (String fieldName : fieldNames) {
			FieldDefinition<?> field = attributeDefinition.getFieldDefinition(fieldName);
			result.add(field.getIndex());
		}
		return result;
	}
	
	protected boolean isFieldVisible(String field) {
		UIOptions uiOptions = getUIOptions();
		return uiOptions.isVisibleField(attributeDefinition, field);
	}

	@ExternalizedProperty
	public boolean isCalculated() {
		return attributeDefinition.isCalculated();
	}

	@ExternalizedProperty
	public List<FieldLabelProxy> getFieldLabels() {
		return FieldLabelProxy.fromFieldLabelList(attributeDefinition.getFieldLabels());
	}
	
	@ExternalizedProperty
	public boolean isEditable() {
		return getAnnotations().isEditable(attributeDefinition);
	}

}
