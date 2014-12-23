package org.openforis.collect.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.web.PersistedObjectForm;
import org.openforis.commons.web.Response;
import org.openforis.idm.metamodel.PersistedSurveyObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AbstractSurveyObjectEditFormController<T extends PersistedSurveyObject, 
			F extends PersistedObjectForm<T>, 
			M extends AbstractSurveyObjectManager<T, ?>>  
			extends AbstractPersistedObjectEditFormController<T, F, M> {
	
	@Autowired
	protected SessionManager sessionManager;
	
	protected abstract T createItemInstance(CollectSurvey survey);
	
	@Override
	protected T createItemInstance() {
		throw new UnsupportedOperationException();
	};
	
	@Override
	@RequestMapping(value="list.json", method = RequestMethod.GET)
	public @ResponseBody
	List<F> loadAll() {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		List<T> items = itemManager.loadBySurvey(survey);
		List<F> forms = new ArrayList<F>(items.size());
		for (T item : items) {
			forms.add(createFormInstance(item));
		}
		return forms;
	}
	
	@Override
	@RequestMapping(value = "/{id}.json", method = RequestMethod.GET)
	public @ResponseBody
	F load(@PathVariable int id) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		T item = itemManager.loadById(survey, id);
		F form = createFormInstance(item);
		return form;
	}
	
	@Override
	@RequestMapping(value="save.json", method = RequestMethod.POST)
	public @ResponseBody
	Response save(@Validated F form, BindingResult result) {
		List<ObjectError> errors = result.getAllErrors();
		SimpleFormUpdateResponse response;
		if (errors.isEmpty()) {
			CollectSurvey survey = sessionManager.getActiveSurvey();
			T item;
			if (form.getId() == null) {
				item = createItemInstance(survey);
			} else {
				item = itemManager.loadById(survey, form.getId());
			}
			form.copyTo(item);
			itemManager.save(item);
			F responseForm = createFormInstance(item);
			response = new SimpleFormUpdateResponse(responseForm);
		} else {
			response = new SimpleFormUpdateResponse(errors);
		}
		return response;
	}

}