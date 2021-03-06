package org.openforis.idm.model;

import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.Languages.Standard;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class TaxonAttribute extends Attribute<TaxonAttributeDefinition, TaxonOccurrence> {

	private static final long serialVersionUID = 1L;
	
	public TaxonAttribute(TaxonAttributeDefinition definition) {
		super(definition);
	}
	
	@SuppressWarnings("unchecked")
	public Field<String> getCodeField() {
		return (Field<String>) getField(0);
	}

	@SuppressWarnings("unchecked")
	public Field<String> getScientificNameField() {
		return (Field<String>) getField(1);
	}

	@SuppressWarnings("unchecked")
	public Field<String> getVernacularNameField() {
		return (Field<String>) getField(2);
	}

	@SuppressWarnings("unchecked")
	public Field<String> getLanguageCodeField() {
		return (Field<String>) getField(3);
	}

	@SuppressWarnings("unchecked")
	public Field<String> getLanguageVarietyField() {
		return (Field<String>) getField(4);
	}

	public String getCode() {
		return getCodeField().getValue();
	}
	
	public void setCode(String value) {
		getCodeField().setValue(value);
	}
	
	public String getScientificName() {
		return getScientificNameField().getValue();
	}
	
	public void setScientificName(String name) {
		getScientificNameField().setValue(name);
	}

	public String getVernacularName() {
		return getVernacularNameField().getValue();
	}
	
	public void setVernacularName(String name) {
		getVernacularNameField().setValue(name);
	}

	public String getLanguageCode() {
		return getLanguageCodeField().getValue();
	}
	
	public void setLanguageCode(String code) {
		checkValidLanguageCode(code);
		getLanguageCodeField().setValue(code);
	}

	private void checkValidLanguageCode(String code) {
		if ( code != null && ! Languages.exists(Standard.ISO_639_3, code) ) {
			throw new LanguageCodeNotSupportedException("Language code not supported: " + code);
		}
	}

	public String getLanguageVariety() {
		return getLanguageVarietyField().getValue();
	}
	
	public void setLanguageVariety(String var) {
		getLanguageVarietyField().setValue(var);
	}
	
	@Override
	public TaxonOccurrence getValue() {
		String code = getCodeField().getValue();
		String scientificName = getScientificName();
		String vernacularName = getVernacularName();
		String languageCode = getLanguageCode();
		String languageVariety = getLanguageVariety();
		return new TaxonOccurrence(code, scientificName, vernacularName, languageCode, languageVariety);
	}

	@Override
	protected void setValueInFields(TaxonOccurrence value) {
		String code = value.getCode();
		String scientificName = value.getScientificName();
		String vernacularName = value.getVernacularName();
		String languageCode = value.getLanguageCode();
		String languageVariety = value.getLanguageVariety();
		checkValidLanguageCode(languageCode);

		getCodeField().setValue(code);
		getScientificNameField().setValue(scientificName);
		getVernacularNameField().setValue(vernacularName);
		getLanguageCodeField().setValue(languageCode);
		getLanguageVarietyField().setValue(languageVariety);
	}
	
	public static class LanguageCodeNotSupportedException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public LanguageCodeNotSupportedException() {
			super();
		}

		public LanguageCodeNotSupportedException(String message, Throwable cause) {
			super(message, cause);
		}

		public LanguageCodeNotSupportedException(String message) {
			super(message);
		}

		public LanguageCodeNotSupportedException(Throwable cause) {
			super(cause);
		}
		
	}
}
