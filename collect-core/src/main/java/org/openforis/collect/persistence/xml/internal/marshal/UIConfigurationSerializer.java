package org.openforis.collect.persistence.xml.internal.marshal;

import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.ATTRIBUTE_ID;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.AUTOCOMPLETE;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.COLUMN;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.COLUMN_GROUP;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.COUNT;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.ENTITY_ID;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.FIELD;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.FORM;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.FORM_SECTION;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.FORM_SET;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.FORM_SETS;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.ID;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.LABEL;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.SHOW_ROW_NUMBERS;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.TABLE;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.UI_NAMESPACE_URI;
import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.UI_PREFIX;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.ui.Column;
import org.openforis.collect.metamodel.ui.ColumnGroup;
import org.openforis.collect.metamodel.ui.Field;
import org.openforis.collect.metamodel.ui.Form;
import org.openforis.collect.metamodel.ui.FormComponent;
import org.openforis.collect.metamodel.ui.FormContentContainer;
import org.openforis.collect.metamodel.ui.FormSection;
import org.openforis.collect.metamodel.ui.FormSet;
import org.openforis.collect.metamodel.ui.Table;
import org.openforis.collect.metamodel.ui.TableHeadingComponent;
import org.openforis.collect.metamodel.ui.UIConfiguration;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.xml.IdmlConstants;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 * 
 * @author S. Ricci
 *
 */
public class UIConfigurationSerializer {

	private static final String INDENT = "http://xmlpull.org/v1/doc/features.html#indent-output";

	public void write(UIConfiguration options, Writer out) {
		try {
			XmlSerializer serializer = createXmlSerializer();
			serializer.setOutput(out);

			serializer.startTag(UI_NAMESPACE_URI, FORM_SETS);
			List<FormSet> formSets = options.getFormSets();
			for (FormSet formSet : formSets) {
				write(serializer, formSet);
			}
			serializer.endTag(UI_NAMESPACE_URI, FORM_SETS);
			serializer.flush();
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
	}

	protected XmlSerializer createXmlSerializer() throws XmlPullParserException,
			IOException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlSerializer serializer = factory.newSerializer();
		serializer.setFeature(INDENT, true);
		serializer.setPrefix(UI_PREFIX, UI_NAMESPACE_URI);
		return serializer;
	}

	protected void write(XmlSerializer serializer, FormSet formSet)
			throws IOException {
		serializer.startTag(UI_NAMESPACE_URI, FORM_SET);
		serializer.attribute("", ID, Integer.toString(formSet.getId()));
		serializer.attribute("", ENTITY_ID, Integer.toString(formSet.getRootEntityDefinition().getId()));
		List<Form> forms = formSet.getForms();
		for (Form form : forms) {
			write(serializer, form);
		}
		serializer.endTag(UI_NAMESPACE_URI, FORM_SET);
	}
	
	protected void write(XmlSerializer serializer, Form form)
			throws IOException {
		serializer.startTag(UI_NAMESPACE_URI, FORM);
		serializer.attribute("", ID, Integer.toString(form.getId()));
		List<LanguageSpecificText> labels = form.getLabels();
		for (LanguageSpecificText label : labels) {
			writeLabel(serializer, label);
		}
		writeFormContent(serializer, form);
		serializer.endTag(UI_NAMESPACE_URI, FORM);
	}
	
	private void writeFormContent(XmlSerializer serializer, FormContentContainer section) throws IOException {
		List<FormComponent> children = section.getChildren();
		for (FormComponent child : children) {
			if ( child instanceof Field ) {
				write(serializer, (Field) child);
			} else if ( child instanceof Table ) {
				write(serializer, (Table) child);
			} else if (child instanceof FormSection ) {
				write(serializer, (FormSection) child);
			} else {
				throw new IllegalArgumentException("Container subclass not supported as child of FormSection object: " + child.getClass().getSimpleName());
			}
		}
		List<Form> forms = section.getForms();
		for (Form subForm : forms) {
			write(serializer, subForm);
		}
	}

	protected void write(XmlSerializer serializer, FormSection formSection)
			throws IOException {
		serializer.startTag(UI_NAMESPACE_URI, FORM_SECTION);
		serializer.attribute("", ID, Integer.toString(formSection.getId()));
		serializer.attribute("", ENTITY_ID, Integer.toString(formSection.getEntityDefinition().getId()));
		writeFormContent(serializer, formSection);
		serializer.endTag(UI_NAMESPACE_URI, FORM_SECTION);
	}

	protected void write(XmlSerializer serializer, Field field) throws IOException {
		serializer.startTag(UI_NAMESPACE_URI, FIELD);
		serializer.attribute("", ID, Integer.toString(field.getId()));
		serializer.attribute("", ATTRIBUTE_ID, Integer.toString(field.getAttributeDefinitionId()));
		String autoCompleteGroup = field.getAutoCompleteGroup();
		if ( StringUtils.isNotBlank(autoCompleteGroup) ) {
			serializer.attribute("", AUTOCOMPLETE, Boolean.TRUE.toString());
		}
		serializer.endTag(UI_NAMESPACE_URI, FIELD);
	}

	protected void write(XmlSerializer serializer, Table table)
			throws IOException {
		serializer.startTag(UI_NAMESPACE_URI, TABLE);
		serializer.attribute("", ID, Integer.toString(table.getId()));
		serializer.attribute("", ENTITY_ID, Integer.toString(table.getEntityDefinition().getId()));
		if ( table.isCountInSummaryList() ) {
			serializer.attribute("", COUNT, Boolean.TRUE.toString());
		}
		if ( table.isShowRowNumbers() ) {
			serializer.attribute("", SHOW_ROW_NUMBERS, Boolean.TRUE.toString());
		}
		List<TableHeadingComponent> headingComponents = table.getHeadingComponents();
		write(serializer, headingComponents);
		serializer.endTag(UI_NAMESPACE_URI, TABLE);
	}

	protected void write(XmlSerializer serializer,
			List<TableHeadingComponent> headingComponents) throws IOException {
		for (TableHeadingComponent headingComponent : headingComponents) {
			if ( headingComponent instanceof Column ) {
				write(serializer, (Column) headingComponent);
			} else if ( headingComponent instanceof ColumnGroup ) {
				write(serializer, (ColumnGroup) headingComponent);
			} else {
				throw new UnsupportedOperationException("Table Heading Component not supported: " + headingComponent.getClass().getSimpleName());
			}
		}
	}
	
	protected void write(XmlSerializer serializer, ColumnGroup columnGroup)
			throws IOException {
		serializer.startTag(UI_NAMESPACE_URI, COLUMN_GROUP);
		serializer.attribute("", ID, Integer.toString(columnGroup.getId()));
		serializer.attribute("", ENTITY_ID, Integer.toString(columnGroup.getEntityDefinition().getId()));
		List<TableHeadingComponent> headingComponents = columnGroup.getHeadingComponents();
		write(serializer, headingComponents);
		serializer.endTag(UI_NAMESPACE_URI, COLUMN_GROUP);
	}
	
	protected void write(XmlSerializer serializer, Column column) throws IOException {
		serializer.startTag(UI_NAMESPACE_URI, COLUMN);
		serializer.attribute("", ID, Integer.toString(column.getId()));
		serializer.attribute("", ATTRIBUTE_ID, Integer.toString(column.getAttributeDefinitionId()));
		serializer.endTag(UI_NAMESPACE_URI, COLUMN);
	}
	
	protected void writeLabel(XmlSerializer serializer, LanguageSpecificText label)
			throws IOException {
		serializer.startTag(UI_NAMESPACE_URI, LABEL);
		String lang = label.getLanguage();
		if ( lang != null ) {
			serializer.attribute(IdmlConstants.XML_NAMESPACE_URI, IdmlConstants.XML_LANG_ATTRIBUTE, lang);
		}
		serializer.text(label.getText());
		serializer.endTag(UI_NAMESPACE_URI, LABEL);
	}
}