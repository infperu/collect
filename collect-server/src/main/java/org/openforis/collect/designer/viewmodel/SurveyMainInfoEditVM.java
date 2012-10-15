/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.form.ItemFormObject;
import org.openforis.collect.designer.form.SurveyMainInfoFormObject;
import org.openforis.collect.model.CollectSurvey;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyMainInfoEditVM extends SurveyItemEditVM<CollectSurvey> {
	
	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
		editedItem = getSurvey();
	}

	@Override
	public ItemFormObject<CollectSurvey> getFormObject() {
		if ( formObject == null ) {
			CollectSurvey survey = getSurvey();
			formObject = createFormObject();
			formObject.loadFrom(survey, currentLanguageCode);
		}
		return formObject;
	}
	
	@Override
	protected CollectSurvey createItemInstance() {
		//do nothing, no child instances created
		return null;
	}
	
	@Override
	protected ItemFormObject<CollectSurvey> createFormObject() {
		return new SurveyMainInfoFormObject();
	}

	@Override
	protected List<CollectSurvey> getItemsInternal() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void addNewItemToSurvey() {}

	@Override
	protected void deleteItemFromSurvey(CollectSurvey item) {}

	@Override
	protected void moveSelectedItem(int indexTo) {}
}
