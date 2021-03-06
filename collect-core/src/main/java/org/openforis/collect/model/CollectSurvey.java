/**
 * 
 */
package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptionsConstants;
import org.openforis.collect.persistence.jooq.tables.OfcSamplingDesign;
import org.openforis.idm.metamodel.ApplicationOptions;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class CollectSurvey extends Survey {

	private static final long serialVersionUID = 1L;
	
	public static final String SAMPLING_DESIGN_CODE_LIST_NAME = "sampling_design";
	
	private boolean work;
	
	private CollectAnnotations annotations;
	
	protected CollectSurvey(SurveyContext surveyContext) {
		super(surveyContext);
		this.work = false;
		this.annotations = new CollectAnnotations(this);
	}

	@Override
	public CollectRecord createRecord(String version) {
		return new CollectRecord(this, version);
	}
	
	public UIOptions createUIOptions() {
		return new UIOptions(this);
	}
	
	public UIOptions getUIOptions() {
		ApplicationOptions applicationOptions = getApplicationOptions(UIOptionsConstants.UI_TYPE);
		return (UIOptions) applicationOptions;
	}
	
	@Override
	public void addApplicationOptions(ApplicationOptions options) {
		super.addApplicationOptions(options);
		if ( options instanceof UIOptions ) {
			((UIOptions) options).setSurvey(this);
		}
	}

	public CollectAnnotations getAnnotations() {
		return annotations;
	}
	
	public CodeList getSamplingDesignCodeList() {
		for (CodeList list : getCodeLists()) {
			if ( OfcSamplingDesign.OFC_SAMPLING_DESIGN.getName().equals(list.getLookupTable()) ) {
				return list;
			}
		}
		return null;
	}
	
	public CodeList addSamplingDesignCodeList() {
		CodeList list = createCodeList();
		list.setName(SAMPLING_DESIGN_CODE_LIST_NAME);
		list.setLookupTable(OfcSamplingDesign.OFC_SAMPLING_DESIGN.getName());
		//add hierarchy levels
		String[] levels = new String[] { 
				OfcSamplingDesign.OFC_SAMPLING_DESIGN.LEVEL1.getName(), 
				OfcSamplingDesign.OFC_SAMPLING_DESIGN.LEVEL2.getName(), 
				OfcSamplingDesign.OFC_SAMPLING_DESIGN.LEVEL3.getName() 
		};
		for (String name : levels) {
			CodeListLevel level = new CodeListLevel();
			level.setName(name);
			list.addLevel(level);
		}
		addCodeList(list);
		return list;
	}
	
	@Override
	public List<CodeList> getCodeLists() {
		return getCodeLists(true);
	}
	
	public List<CodeList> getCodeLists(boolean includeSamplingDesignList) {
		List<CodeList> codeLists = new ArrayList<CodeList>(super.getCodeLists());
		if ( ! includeSamplingDesignList ) {
			CodeList samplingDesignCodeList = getSamplingDesignCodeList();
			if ( samplingDesignCodeList != null ) { 
				Iterator<CodeList> iterator = codeLists.iterator();
				while (iterator.hasNext()) {
					CodeList list = (CodeList) iterator.next();
					if ( list.getId() == samplingDesignCodeList.getId() ) {
						iterator.remove();
						break;
					}
				}
			}
		}
		return codeLists;
	}
	
	public boolean isWork() {
		return work;
	}

	public void setWork(boolean work) {
		this.work = work;
	}

}
