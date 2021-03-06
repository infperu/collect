/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeDefinitionFormObject extends AttributeDefinitionFormObject<CodeAttributeDefinition> {
	
	private boolean key;
	private CodeList list;
	private CodeAttributeDefinition parentCodeAttributeDefinition;
	private boolean strict;
	private boolean allowValuesSorting;
	private String layoutType;
	private boolean showAllowedValuesPreview;
	private String layoutDirection;
	private boolean showCode;
	
	CodeAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
		strict = true;
		allowValuesSorting = false;
		showAllowedValuesPreview = (Boolean) Annotation.SHOW_ALLOWED_VALUES_PREVIEW.getDefaultValue();
		layoutType = Annotation.CODE_ATTRIBUTE_LAYOUT_TYPE.getDefaultValue();
		layoutDirection = Annotation.CODE_ATTRIBUTE_LAYOUT_DIRECTION.getDefaultValue();
		showCode = (Boolean) Annotation.CODE_ATTRIBUTE_SHOW_CODE.getDefaultValue();
	}

	@Override
	public void saveTo(CodeAttributeDefinition dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setList(list);
		dest.setKey(key);
		dest.setAllowUnlisted(! strict);
		dest.setParentCodeAttributeDefinition(parentCodeAttributeDefinition);
		dest.setAllowValuesSorting(dest.isMultiple() && allowValuesSorting);
		
		CollectSurvey survey = (CollectSurvey) dest.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		uiOptions.setShowAllowedValuesPreviewValue(dest, showAllowedValuesPreview);
		
		uiOptions.setLayoutType(dest, layoutType);
		uiOptions.setLayoutDirection(dest, layoutDirection);
		uiOptions.setShowCode(dest, showCode);
	}
	
	@Override
	public void loadFrom(CodeAttributeDefinition source, String languageCode) {
		super.loadFrom(source, languageCode);
		key = source.isKey();
		list = source.getList();
		parentCodeAttributeDefinition = source.getParentCodeAttributeDefinition();
		strict = ! source.isAllowUnlisted();
		allowValuesSorting = source.isMultiple() && source.isAllowValuesSorting();
		
		CollectSurvey survey = (CollectSurvey) source.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		showAllowedValuesPreview = uiOptions.getShowAllowedValuesPreviewValue(source);
		
		layoutType = uiOptions.getLayoutType(source);
		layoutDirection = uiOptions.getLayoutDirection(source);
		showCode = uiOptions.getShowCode(source);
	}

	public boolean isKey() {
		return key;
	}

	public void setKey(boolean key) {
		this.key = key;
	}

	public CodeList getList() {
		return list;
	}

	public void setList(CodeList list) {
		this.list = list;
	}

	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}
	
	public boolean isAllowValuesSorting() {
		return allowValuesSorting;
	}
	
	public void setAllowValuesSorting(boolean allowValuesSorting) {
		this.allowValuesSorting = allowValuesSorting;
	}
	
	public CodeAttributeDefinition getParentCodeAttributeDefinition() {
		return parentCodeAttributeDefinition;
	}
	
	public void setParentCodeAttributeDefinition(CodeAttributeDefinition parentCodeAttributeDefinition) {
		this.parentCodeAttributeDefinition = parentCodeAttributeDefinition;
	}
	
	public String getLayoutDirection() {
		return layoutDirection;
	}
	
	public void setLayoutDirection(String layoutDirection) {
		this.layoutDirection = layoutDirection;
	}
	
	public String getLayoutType() {
		return layoutType;
	}
	
	public void setLayoutType(String layoutType) {
		this.layoutType = layoutType;
	}
	
	public boolean isShowAllowedValuesPreview() {
		return showAllowedValuesPreview;
	}

	public void setShowAllowedValuesPreview(boolean showAllowedValuesPreview) {
		this.showAllowedValuesPreview = showAllowedValuesPreview;
	}

	public boolean isShowCode() {
		return showCode;
	}

	public void setShowCode(boolean showCode) {
		this.showCode = showCode;
	}
	
}
