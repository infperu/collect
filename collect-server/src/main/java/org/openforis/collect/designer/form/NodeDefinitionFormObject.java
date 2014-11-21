package org.openforis.collect.designer.form;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.model.AttributeType;
import org.openforis.collect.designer.model.NodeType;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Orientation;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.Calculable;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Prompt;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class NodeDefinitionFormObject<T extends NodeDefinition> extends VersionableItemFormObject<T> {
	
	public static final String REQUIRED_FIELD = "required";
	
	@SuppressWarnings("unused")
	private EntityDefinition parentDefinition;
	
	//generic
	private String name;
	private String description;
	private boolean multiple;
	private boolean required;
	private String requiredExpression;
	private String relevantExpression;
	private boolean hideWhenNotRelevant;
	private Integer minCount;
	private Integer maxCount;
	private boolean calculated;
	private boolean includeInDataExport;
	private boolean showInUI;
	
	//labels
	private String headingLabel;
	private String instanceLabel;
	private String numberLabel;
	private String interviewPromptLabel;
	private String paperPromptLabel;
	private String handheldPromptLabel;
	private String pcPromptLabel;
	
	//layout
	private String tabName;
	private int column;
	private int columnSpan;
	private Integer width;
	private Integer labelWidth;
	private String labelOrientation;
	private String labelColor;
	
	NodeDefinitionFormObject(EntityDefinition parentDefn) {
		this.parentDefinition = parentDefn;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static NodeDefinitionFormObject<? extends NodeDefinition> newInstance(EntityDefinition parentDefn, NodeType nodeType, AttributeType attributeType) {
		if ( nodeType == null ) {
			return null;
		} else {
			switch ( nodeType) {
			case ENTITY:
				return new EntityDefinitionFormObject(parentDefn);
			case ATTRIBUTE:
				return (NodeDefinitionFormObject<NodeDefinition>) newInstance(parentDefn, attributeType);
			default:
				throw new IllegalArgumentException("Unsupported node type: " + nodeType.getClass().getName());
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static AttributeDefinitionFormObject<?> newInstance(EntityDefinition parentDefn, AttributeType attributeType) {
		if ( attributeType != null ) {
			switch (attributeType) {
			case BOOLEAN:
				return new BooleanAttributeDefinitionFormObject(parentDefn);
			case CODE:
				return new CodeAttributeDefinitionFormObject(parentDefn);
			case COORDINATE:
				return new CoordinateAttributeDefinitionFormObject(parentDefn);
			case DATE:
				return new DateAttributeDefinitionFormObject(parentDefn);
			case FILE:
				return new FileAttributeDefinitionFormObject(parentDefn);
			case NUMBER:
				return new NumberAttributeDefinitionFormObject(parentDefn);
			case RANGE:
				return new RangeAttributeDefinitionFormObject(parentDefn);
			case TAXON:
				return new TaxonAttributeDefinitionFormObject(parentDefn);
			case TEXT:
				return new TextAttributeDefinitionFormObject(parentDefn);
			case TIME:
				return new TimeAttributeDefinitionFormObject(parentDefn);
			default:
				throw new IllegalStateException("Attribute type not supported");
			}
		} else {
			return null;
		}
	}
	
	@Override
	public void loadFrom(T source, String language) {
		super.loadFrom(source, language);
		CollectSurvey survey = (CollectSurvey) source.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		
		//generic
		name = source.getName();
		multiple = source.isMultiple();
		Integer nodeMinCount = source.getMinCount();
		required = nodeMinCount != null && nodeMinCount.intValue() > 0;
		if ( required ) {
			requiredExpression = null;
		} else {
			requiredExpression = source.getRequiredExpression();
		}
		relevantExpression = source.getRelevantExpression();
		minCount = nodeMinCount;
		if ( multiple ) {
			maxCount = source.getMaxCount();
		} else {
			maxCount = null;
		}
		
		//labels
		headingLabel = source.getLabel(Type.HEADING, language);
		instanceLabel = source.getLabel(Type.INSTANCE, language);
		numberLabel = source.getLabel(Type.NUMBER, language);
		interviewPromptLabel = source.getPrompt(Prompt.Type.INTERVIEW, language);
		paperPromptLabel = source.getPrompt(Prompt.Type.PAPER, language);
		handheldPromptLabel = source.getPrompt(Prompt.Type.HANDHELD, language);
		pcPromptLabel = source.getPrompt(Prompt.Type.PC, language);
		description = source.getDescription(language);
		//layout
		hideWhenNotRelevant = uiOptions.isHideWhenNotRelevant(source);
		column = uiOptions.getColumn(source);
		columnSpan = uiOptions.getColumnSpan(source);
		width = uiOptions.getWidth(source);
		labelWidth = uiOptions.getLabelWidth(source);
		labelOrientation = uiOptions.getLabelOrientation(source).name();
		labelColor = uiOptions.getLabelColor(source);

		CollectAnnotations annotations = survey.getAnnotations();

		if ( source instanceof Calculable ) {
			calculated = ((Calculable) source).isCalculated();
			//show in UI
			showInUI = ! uiOptions.isHidden(source);
			
			includeInDataExport = annotations.isIncludedInDataExport(source);
		}
	}

	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		if ( ! name.equals(dest.getName()) ) {
			dest.rename(name);
		}
		dest.setLabel(Type.HEADING, languageCode, headingLabel);
		dest.setLabel(Type.INSTANCE, languageCode, instanceLabel);
		dest.setLabel(Type.NUMBER, languageCode, numberLabel);
		dest.setPrompt(Prompt.Type.HANDHELD, languageCode, handheldPromptLabel);
		dest.setPrompt(Prompt.Type.INTERVIEW, languageCode, interviewPromptLabel);
		dest.setPrompt(Prompt.Type.PAPER, languageCode, paperPromptLabel);
		dest.setPrompt(Prompt.Type.PC, languageCode, pcPromptLabel);
		dest.setDescription(languageCode, description);
		dest.setMultiple(multiple);
		dest.setMinCount(null);
		dest.setMaxCount(null);
		dest.setRequiredExpression(null);
		if ( multiple ) {
			dest.setMinCount(minCount);
			dest.setMaxCount(maxCount);
		} else if (required) {
			dest.setMinCount(1);
		} else {
			dest.setRequiredExpression(StringUtils.trimToNull(requiredExpression));
		}
		dest.setRelevantExpression(StringUtils.trimToNull(relevantExpression));
		
		CollectSurvey survey = (CollectSurvey) dest.getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();
		UIOptions uiOptions = survey.getUIOptions();
		
		//layout
		uiOptions.setHideWhenNotRelevant(dest, hideWhenNotRelevant);
		uiOptions.setColumn(dest, column);
		uiOptions.setColumnSpan(dest, columnSpan);
		uiOptions.setWidth(dest, width);
		uiOptions.setLabelWidth(dest, labelWidth);
		uiOptions.setLabelOrientation(dest, Orientation.valueOf(labelOrientation));
		uiOptions.setLabelColor(dest, labelColor);
		
		if ( dest instanceof Calculable ) {
			((Calculable) dest).setCalculated(calculated);
			
			//include in data export
			annotations.setIncludeInDataExport(dest, includeInDataExport);
			
			//show in ui
			uiOptions.setHidden(dest, ! showInUI);
		}
	}

	@Override
	protected void reset() {
		calculated = false;
		showInUI = true;
		includeInDataExport = true;
	}
	
	protected UIOptions getUIOptions(NodeDefinition nodeDefn) {
		CollectSurvey survey = (CollectSurvey) nodeDefn.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		return uiOptions;
	}
	
	public void setParentDefinition(EntityDefinition parentDefinition) {
		this.parentDefinition = parentDefinition;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isCalculated() {
		return calculated;
	}
	
	public void setCalculated(boolean calculated) {
		this.calculated = calculated;
	}
	
	public boolean isIncludeInDataExport() {
		return includeInDataExport;
	}
	
	public void setIncludeInDataExport(boolean includeInDataExport) {
		this.includeInDataExport = includeInDataExport;
	}
	
	public boolean isShowInUI() {
		return showInUI;
	}
	
	public void setShowInUI(boolean showInUI) {
		this.showInUI = showInUI;
	}
	
	public String getHeadingLabel() {
		return headingLabel;
	}
	
	public void setHeadingLabel(String headingLabel) {
		this.headingLabel = headingLabel;
	}
	
	public String getInstanceLabel() {
		return instanceLabel;
	}
	
	public void setInstanceLabel(String instanceLabel) {
		this.instanceLabel = instanceLabel;
	}
	
	public String getNumberLabel() {
		return numberLabel;
	}
	
	public void setNumberLabel(String numberLabel) {
		this.numberLabel = numberLabel;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isMultiple() {
		return multiple;
	}
	
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}
	
	public String getInterviewPromptLabel() {
		return interviewPromptLabel;
	}

	public void setInterviewPromptLabel(String interviewPromptLabel) {
		this.interviewPromptLabel = interviewPromptLabel;
	}

	public String getPaperPromptLabel() {
		return paperPromptLabel;
	}

	public void setPaperPromptLabel(String paperPromptLabel) {
		this.paperPromptLabel = paperPromptLabel;
	}

	public String getHandheldPromptLabel() {
		return handheldPromptLabel;
	}

	public void setHandheldPromptLabel(String handheldPromptLabel) {
		this.handheldPromptLabel = handheldPromptLabel;
	}

	public String getPcPromptLabel() {
		return pcPromptLabel;
	}

	public void setPcPromptLabel(String pcPromptLabel) {
		this.pcPromptLabel = pcPromptLabel;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required =  required;
	}

	public String getRequiredExpression() {
		return requiredExpression;
	}

	public void setRequiredExpression(String requiredExpression) {
		this.requiredExpression = requiredExpression;
	}

	public String getRelevantExpression() {
		return relevantExpression;
	}

	public void setRelevantExpression(String relevantExpression) {
		this.relevantExpression = relevantExpression;
	}

	public boolean isHideWhenNotRelevant() {
		return hideWhenNotRelevant;
	}
	
	public void setHideWhenNotRelevant(boolean hideWhenNotRelevant) {
		this.hideWhenNotRelevant = hideWhenNotRelevant;
	}
	
	public Integer getMinCount() {
		return minCount;
	}

	public void setMinCount(Integer minCount) {
		this.minCount = minCount;
	}

	public Integer getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(Integer maxCount) {
		this.maxCount = maxCount;
	}

	public String getTabName() {
		return tabName;
	}

	public void setTabName(String tabName) {
		this.tabName = tabName;
	}

	public int getColumn() {
		return column;
	}
	
	public void setColumn(int column) {
		this.column = column;
	}
	
	public int getColumnSpan() {
		return columnSpan;
	}
	
	public void setColumnSpan(int columnSpan) {
		this.columnSpan = columnSpan;
	}
	
	public Integer getWidth() {
		return width;
	}
	
	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getLabelWidth() {
		return labelWidth;
	}
	
	public void setLabelWidth(Integer labelWidth) {
		this.labelWidth = labelWidth;
	}
	
	public String getLabelOrientation() {
		return labelOrientation;
	}
	
	public void setLabelOrientation(String labelOrientation) {
		this.labelOrientation = labelOrientation;
	}
	
}
