/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.form.CodeListFormObject;
import org.openforis.collect.designer.form.CodeListFormObject.Type;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.validator.BaseValidator;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.viewmodel.referencedata.ReferenceDataImportErrorPopUpVM;
import org.openforis.collect.io.metadata.codelist.CodeListBatchExportJob;
import org.openforis.collect.io.metadata.codelist.CodeListBatchImportJob;
import org.openforis.collect.io.metadata.codelist.CodeListImportTask;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.dataexport.codelist.CodeListExportProcess;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.spring.SpringJobManager;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeList.CodeScope;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class CodeListsVM extends SurveyObjectBaseVM<CodeList> {

	public static final String EDITING_ATTRIBUTE_PARAM = "editingAttribute";
	public static final String SELECTED_CODE_LIST_PARAM = "selectedCodeList";
	private static final String CODE_LISTS_UPDATED_GLOBAL_COMMAND = "codeListsUpdated";
	private static final String SURVEY_CODE_LIST_GENERATED_LEVEL_NAME_LABEL_KEY = "survey.code_list.generated_level_name";
	public static final String CLOSE_CODE_LIST_ITEM_POP_UP_COMMAND = "closeCodeListItemPopUp";
	public static final String CLOSE_CODE_LIST_IMPORT_POP_UP_COMMAND = "closeCodeListImportPopUp";
	private static final String CSV_CONTENT_TYPE = "text/csv";
	
	private List<List<CodeListItem>> itemsPerLevel;
	private boolean newChildItem;
	private CodeListItem editedChildItem;
	private CodeListItem editedChildItemParentItem;
	private int editedChildItemLevelIndex;
	
	private List<CodeListItem> selectedItemsPerLevel;
	private Window codeListItemPopUp;
	private Window referencedNodesPopUp;
	private Window codeListImportPopUp;
	private boolean editingAttribute;
	
	@WireVariable
	private SurveyManager surveyManager;
	@WireVariable
	private CodeListManager codeListManager;
	@WireVariable
	private SpringJobManager springJobManager;
	private Window jobStatusPopUp;
	private CodeListBatchExportJob batchExportJob;
	private CodeListBatchImportJob batchImportJob;
	private Window dataImportErrorPopUp;
	
	public CodeListsVM() {
		super();
		formObject = createFormObject();
		fieldLabelKeyPrefixes.addAll(0, Arrays.asList("survey.code_list"));
	}
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam(EDITING_ATTRIBUTE_PARAM) Boolean editingAttribute, 
			@ExecutionArgParam(SELECTED_CODE_LIST_PARAM) CodeList selectedCodeList) {
		super.init();
		if ( selectedCodeList != null ) {
			selectionChanged(selectedCodeList);
		}
		this.editingAttribute = editingAttribute != null && editingAttribute.booleanValue();
	}
	
	@Override
	@AfterCompose
	public void doAfterCompose(@ContextParam(ContextType.VIEW) Component view){
		super.doAfterCompose(view);
	}
	
	@Override
	protected List<CodeList> getItemsInternal() {
		CollectSurvey survey = getSurvey();
		List<CodeList> codeLists = survey.getCodeLists(false);
		codeLists = sort(codeLists);
		return codeLists;
	}

	@Override
	protected void addNewItemToSurvey() {
		CollectSurvey survey = getSurvey();
		survey.addCodeList(editedItem);
		dispatchCodeListsUpdatedCommand();
		dispatchSurveySaveCommand();
		initItemsPerLevel();
		notifyChange("itemsPerLevel");
	}

	@Override
	protected void deleteItemFromSurvey(CodeList item) {
		codeListManager.delete(item);
		dispatchCodeListsUpdatedCommand();
		dispatchSurveySaveCommand();
	}
	
	@Override
	protected FormObject<CodeList> createFormObject() {
		return new CodeListFormObject();
	}

	protected void dispatchCodeListsUpdatedCommand() {
		BindUtils.postGlobalCommand(null, null, CODE_LISTS_UPDATED_GLOBAL_COMMAND, null);
	}
	
	protected void dispatchSurveySaveCommand() {
		BindUtils.postGlobalCommand(null, null, SurveyEditVM.BACKGROUD_SAVE_GLOBAL_COMMAND, null);
	}
	
	@Command
	@Override
	protected void performNewItemCreation(Binder binder) {
		super.performNewItemCreation(binder);
		notifyChange("listLevels","itemsPerLevel","selectedItemsPerLevel");
	}
	
	@Override
	protected CodeList createItemInstance() {
		CodeList instance = survey.createCodeList();
		instance.setCodeScope(CodeScope.LOCAL);
		return instance;
	}
	
	@Override
	protected void performItemSelection(CodeList item) {
		super.performItemSelection(item);
		notifyChange("listLevels","itemsPerLevel","selectedItemsPerLevel");
	}
	
	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {
		survey.moveCodeList(selectedItem, indexTo);
	}
	
	@Override
	@Command
	public void deleteItem(@BindingParam("item") final CodeList item) {
		List<NodeDefinition> references = getReferences(item);
		if ( ! references.isEmpty() ) {
			String title = Labels.getLabel("global.message.title.warning");
			String message = Labels.getLabel("survey.code_list.alert.cannot_delete_used_list");
			referencedNodesPopUp = SurveyErrorsPopUpVM.openPopUp(title, message, 
					references, new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					closeReferencedNodesPopUp();
				}
			}, true);
		} else {
			super.deleteItem(item);
		}
	}

	protected void closeReferencedNodesPopUp() {
		closePopUp(referencedNodesPopUp);
		referencedNodesPopUp = null;
	}

	protected List<NodeDefinition> getReferences(CodeList item) {
		List<NodeDefinition> references = new ArrayList<NodeDefinition>();
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntities = schema.getRootEntityDefinitions();
		Stack<NodeDefinition> stack = new Stack<NodeDefinition>();
		stack.addAll(rootEntities);
		while ( ! stack.isEmpty() ) {
			NodeDefinition defn = stack.pop();
			if ( defn instanceof EntityDefinition ) {
				stack.addAll(((EntityDefinition) defn).getChildDefinitions());
			} else if ( defn instanceof CodeAttributeDefinition ) {
				CodeList list = ((CodeAttributeDefinition) defn).getList();
				if ( list.equals(item) ) {
					references.add(defn);
				}
			};
		}
		return references;
	}
	
	@Command
	public void typeChanged(@BindingParam("type") String type) {
		Type typeEnum = CodeListFormObject.Type.valueOf(type);
		switch (typeEnum) {
		case HIERARCHICAL:
			addLevel();
			break;
		default:
			editedItem.removeLevel(0);
		}
		CodeListFormObject fo = (CodeListFormObject) formObject;
		fo.setType(type);
		notifyChange("formObject","listLevels");
	}
	
	@Command
	@NotifyChange({"listLevels"})
	public void addLevel() {
		List<CodeListLevel> levels = editedItem.getHierarchy();
		int levelPosition = levels.size() + 1;
		CodeListLevel level = new CodeListLevel();
		String generatedName = Labels.getLabel(SURVEY_CODE_LIST_GENERATED_LEVEL_NAME_LABEL_KEY, new Object[]{levelPosition});
		level.setName(generatedName);
		editedItem.addLevel(level);
	}

	@Command
	public void removeLevel() {
		final List<CodeListLevel> levels = editedItem.getHierarchy();
		if ( ! levels.isEmpty() ) {
			final int levelIndex = levels.size() - 1;
			if ( codeListManager.hasChildItemsInLevel(editedItem, levelIndex + 1) ) {
				ConfirmHandler handler = new ConfirmHandler() {
					@Override
					public void onOk() {
						performRemoveLevel(levelIndex);
					}
				};
				MessageUtil.ConfirmParams params = new MessageUtil.ConfirmParams(handler, "survey.code_list.alert.cannot_delete_non_empty_level");
				params.setOkLabelKey("global.delete_item");
				MessageUtil.showConfirm(params);
			} else {
				performRemoveLevel(levelIndex);
			}
		}
	}

	public String getHierarchyLevelNameValidationKey(int levelIdx) {
		return "hiearchyLevelName_" + levelIdx;
	}
	
	public Validator getHierarchyLevelNameValidator(final int levelIdx) {
		return new BaseValidator() {
			@Override
			public void validate(ValidationContext ctx) {
				String validationKey = getHierarchyLevelNameValidationKey(levelIdx);
				if ( validateRequired(ctx, null, validationKey) ) {
					validateInternalName(ctx, null, validationKey);
				}
			}
		};
	}
	
	protected void performRemoveLevel(int levelIndex) {
		codeListManager.removeLevel(editedItem, levelIndex + 1);
		deselectItemsAfterLevel(levelIndex);
		initItemsPerLevel();
		notifyChange("listLevels","selectedItemsPerLevel","itemsPerLevel");
	}
	
	@Command
	@NotifyChange({"itemsPerLevel"})
	public void addItemInLevel(@BindingParam("levelIndex") int levelIndex) {
		if ( checkCanLeaveForm() ) {
			newChildItem = true;
			editedChildItemLevelIndex = levelIndex;
			editedChildItem = createChildItem();
			if ( editedChildItemLevelIndex == 0 ) {
				editedChildItemParentItem = null;
			} else {
				editedChildItemParentItem = selectedItemsPerLevel.get(editedChildItemLevelIndex - 1);
			}
			openChildItemEditPopUp();
		}
	}

	protected CodeListItem createChildItem() {
		if ( editedItem.isExternal() ) {
			throw new UnsupportedOperationException("Cannot instantiate ExternalCodeListItem object");
		} else if ( editedItem.isEmpty() ) {
			return new PersistedCodeListItem(editedItem, editedChildItemLevelIndex + 1);
		} else {
			return editedItem.createItem(editedChildItemLevelIndex + 1);
		}
	}
	
	@Command
	@NotifyChange({"itemsPerLevel"})
	public void deleteCodeListItem(@BindingParam("item") final CodeListItem item) {
		if ( isSurveyPublished() && isEnumeratingCodeList() ) {
			MessageUtil.showWarning("survey.code_list.cannot_delete_enumerating_code_list_items");
		} else {
			String messageKey;
			if ( codeListManager.hasChildItems(item) ) {
				messageKey = "survey.code_list.confirm.delete_non_empty_item";
			} else {
				messageKey = "survey.code_list.confirm.delete_item";
			}
			MessageUtil.ConfirmParams params = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					performDeleteCodeListItem(item);
				}
			}, messageKey);
			params.setOkLabelKey("global.delete_item");
			MessageUtil.showConfirm(params);
		}
	}

	protected boolean isEnumeratingCodeList() {
		return editedItem.isEnumeratingList();
	}
	
	protected void performDeleteCodeListItem(CodeListItem item) {
		boolean selected = isCodeListItemSelected(item);
		int itemLevelIndex = getLevelIndex(item);
		codeListManager.delete(item);
		if ( selected ) {
			deselectItemsAfterLevel(itemLevelIndex);
		}
		initItemsPerLevel();
		notifyChange("itemsPerLevel","selectedItemsPerLevel");
		BindUtils.postNotifyChange(null, null, editedItem, ".");
	}
	
	@Command
	public void moveChildItem(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent event) {
		Listitem dragged = (Listitem) event.getDragged();
		Listitem dropped = (Listitem) event.getTarget();
		CodeListItem draggedItem = dragged.getValue();
		CodeListItem droppedItem = dropped.getValue();
		int indexTo = getItemIndex(droppedItem);
		moveChildItem(draggedItem, indexTo);
	}
	
	@Override
	public void setEditedItem(CodeList editedItem) {
		super.setEditedItem(editedItem);
		selectedItemsPerLevel = new ArrayList<CodeListItem>();
		initItemsPerLevel();
	}
	
	@Command
	public void editCodeListItem(@BindingParam("item") CodeListItem item) {
		newChildItem = false;
		editedChildItem = item;
		editedChildItemParentItem = codeListManager.loadParentItem(item);
		openChildItemEditPopUp();
	}
	
	
	@Command
	public void batchImportFileUploaded(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event) {
 		Media media = event.getMedia();
		String fileName = media.getName();
		String extension = FilenameUtils.getExtension(fileName);
		File tempFile = OpenForisIOUtils.copyToTempFile(media.getStreamData(), extension);

		batchImportJob = new CodeListBatchImportJob();
		batchImportJob.setJobManager(springJobManager);
		batchImportJob.setCodeListManager(codeListManager);
		batchImportJob.setSurvey(survey);
		batchImportJob.setOverwriteData(true);
		batchImportJob.setFile(tempFile);
		springJobManager.start(batchImportJob);
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(Labels.getLabel("survey.code_list.batch_import"), batchImportJob, true);
	}
	
	@Command
	public void batchExport() {
		batchExportJob = new CodeListBatchExportJob();
		batchExportJob.setJobManager(springJobManager);
		batchExportJob.setCodeListManager(codeListManager);
		batchExportJob.setSurvey(survey);
		springJobManager.start(batchExportJob);
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(Labels.getLabel("survey.code_list.batch_export"), batchExportJob, true);
	}
	
	protected void closeJobStatusPopUp() {
		closePopUp(jobStatusPopUp);
		jobStatusPopUp = null;
	}

	@GlobalCommand
	public void jobAborted(@BindingParam("job") Job job) {
		closeJobStatusPopUp();
		clearJob(job);
	}

	@GlobalCommand
	public void jobFailed(@BindingParam("job") Job job) {
		closeJobStatusPopUp();
		if (job instanceof CodeListBatchImportJob && job.getCurrentTask() != null) {
			CodeListImportTask lastTask = (CodeListImportTask) ((CodeListBatchImportJob) job).getCurrentTask();
			dataImportErrorPopUp = ReferenceDataImportErrorPopUpVM.showPopUp(lastTask.getErrors(), 
					Labels.getLabel("survey.code_list.import_data.error_popup.title", new String[]{lastTask.getEntryName()}));
		} else {
			String errorMessageKey = job.getErrorMessage();
			String errorMessage = StringUtils.defaultIfBlank(Labels.getLabel(errorMessageKey), errorMessageKey);
			MessageUtil.showError("global.job_status.failed.message", new String[]{errorMessage});
		}
		clearJob(job);
	}
	
	@GlobalCommand
	public void jobCompleted(@BindingParam("job") Job job) {
		closeJobStatusPopUp();
		if (job == batchExportJob) {
			downloadFile(batchExportJob.getOutputFile(), survey.getName() + "_code_lists.zip");
		} else if (job == batchImportJob) {
			notifyChange("items");
		}
		clearJob(job);
	}

	private void downloadFile(File file, String fileName) {
		try {
			Filedownload.save(new FileInputStream(file), "application/zip", fileName);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void clearJob(Job job) {
		if (job == batchExportJob) {
			batchExportJob = null;
		} else if (job == batchImportJob) {
			batchImportJob = null;
		}
	}
	
	protected String generateItemCode(CodeListItem item) {
		return "item_" + item.getId();
	}

	public void openChildItemEditPopUp() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(CodeListItemVM.ITEM_ARG, editedChildItem);
		args.put(CodeListItemVM.PARENT_ITEM_ARG, editedChildItemParentItem);
		args.put(CodeListItemVM.ENUMERATING_CODE_LIST_ARG, isSurveyPublished() && isEnumeratingCodeList());
		codeListItemPopUp = openPopUp(Resources.Component.CODE_LIST_ITEM_EDIT_POP_UP.getLocation(), true, args);
		Binder binder = ComponentUtil.getBinder(codeListItemPopUp);
		validateForm(binder);
	}

	@Command
	@NotifyChange({"itemsPerLevel","selectedItemsPerLevel"})
	public void listItemSelected(@BindingParam("item") CodeListItem item, 
			@BindingParam("levelIndex") int levelIndex) {
		deselectItemsAfterLevel(levelIndex);
		selectedItemsPerLevel.add(item);
		initItemsPerLevel();
	}
	
	@DependsOn("listLevels")
	public boolean isMultipleLevelsPresent() {
		if ( editedItem != null ) {
			return editedItem.getHierarchy().size() > 1;
		} else {
			return false;
		}
	}
	
	protected void deselectItemsAfterLevel(int levelIndex) {
		int maxSelectedLevelIndex = selectedItemsPerLevel.size() - 1;
		for (int i = maxSelectedLevelIndex; i >= levelIndex; i --) {
			selectedItemsPerLevel.remove(i);
		}
	}
	
	protected void moveChildItem(CodeListItem item, int toIndex) {
		codeListManager.shiftItem(item, toIndex);
		int levelIdx = getLevelIndex(item);
		List<CodeListItem> siblings = itemsPerLevel.get(levelIdx);
		CollectionUtils.shiftItem(siblings, item, toIndex);
		itemsPerLevel.set(levelIdx, siblings);
		if ( item instanceof PersistedCodeListItem ) {
			reloadSiblingsSortOrder((PersistedCodeListItem) item);
		}
		notifyChange("itemsPerLevel");
	}
	
	/**
	 * Reloads the siblings from the database.
	 * The sort order of these items changes after calling codeListManager.shiftItem method.
	 * 
	 * @param item
	 */
	protected void reloadSiblingsSortOrder(PersistedCodeListItem item) {
		int levelIdx = getLevelIndex(item);
		List<CodeListItem> newItems;
		if ( levelIdx == 0 ) {
			newItems = codeListManager.loadRootItems(item.getCodeList());
		} else {
			CodeListItem parentItem = codeListManager.loadParentItem(item);
			newItems = codeListManager.loadChildItems(parentItem);
		}
		List<CodeListItem> items = itemsPerLevel.get(levelIdx);
		for(int i=0; i < items.size(); i++) {
			CodeListItem oldItem = items.get(i);
			CodeListItem newItem = newItems.get(i);
			((PersistedCodeListItem) oldItem).setSortOrder(((PersistedCodeListItem) newItem).getSortOrder());
		}
	}

	protected int getLevelIndex(CodeListItem item) {
		for ( int index = 0; index < itemsPerLevel.size(); index++) {
			List<CodeListItem> items = itemsPerLevel.get(index);
			if ( items.contains(item) ) {
				return index;
			}
		}
		throw new IllegalArgumentException("Item not found in cache");
	}
	
	protected List<CodeListItem> getSiblings(CodeListItem item) {
		int levelIdx = getLevelIndex(item);
		List<CodeListItem> siblings = itemsPerLevel.get(levelIdx);
		return siblings;
	}
	
	protected int getItemIndex(CodeListItem item) {
		List<CodeListItem> siblings = getSiblings(item);
		int index = siblings.indexOf(item);
		return index;
	}
	
	@GlobalCommand
	public void closeCodeListItemPopUp(@BindingParam("undoChanges") boolean undoChanges) {
		closePopUp(codeListItemPopUp);
		codeListItemPopUp = null;
		if ( undoChanges ) {
			dispatchCurrentFormValidatedCommand(true);
		} else {
			if ( newChildItem ) {
				addChildItemToCodeList();
			} else {
				dispatchSurveySaveCommand();
				if ( editedChildItem instanceof PersistedCodeListItem ) {
					codeListManager.save((PersistedCodeListItem) editedChildItem);
				}
				BindUtils.postNotifyChange(null, null, editedChildItem, "*");
			}
		}
	}
	
	@Override
	public void commitChanges() {
		super.commitChanges();
		dispatchCodeListsUpdatedCommand();
	}

	@Command
	public void openCodeListImportPopUp() {
		if ( canImportCodeList() ) {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("codeListId", editedItem.getId());
			codeListImportPopUp = openPopUp(Resources.Component.CODE_LIST_IMPORT_POPUP.getLocation(), true, args);
		} else if ( editedItem.isExternal() ) {
			MessageUtil.showWarning("survey.code_list.cannot_import_items_on_external_code_list");
		} else {
			MessageUtil.showWarning("survey.code_list.cannot_import_items_on_enumerating_code_list");
		}
	}
	
	@Command
	public void exportCodeList() throws IOException {
		CollectSurvey survey = getSurvey();
		CodeListExportProcess codeListExportProcess = new CodeListExportProcess(codeListManager);
		File tempFile = File.createTempFile("code_list_" + editedItem.getName(), ".csv");
		FileOutputStream os = new FileOutputStream(tempFile);
		codeListExportProcess.exportToCSV(os, survey, editedItem.getId());
		Filedownload.save(tempFile, CSV_CONTENT_TYPE);
	}

	@GlobalCommand
	public void closeReferenceDataImportErrorPopUp() {
		closePopUp(dataImportErrorPopUp);
		dataImportErrorPopUp = null;
	}
	
	protected boolean canImportCodeList() {
		return ! editedItem.isExternal() && ! isUsedAsEnumeratorInPublishedSurvey();
	}

	private boolean isUsedAsEnumeratorInPublishedSurvey() {
		return isSurveyPublished() && isEnumeratingCodeList() && isCodeListInPublishedSurvey();
	}
	
	protected boolean isCodeListInPublishedSurvey() {
		SessionStatus sessionStatus = getSessionStatus();
		Integer publishedSurveyId = sessionStatus.getPublishedSurveyId();
		if ( publishedSurveyId != null ) {
			CollectSurvey publishedSurvey = surveyManager.getById(publishedSurveyId);
			CodeList oldPublishedCodeList = publishedSurvey.getCodeListById(editedItem.getId());
			return oldPublishedCodeList != null;
		} else {
			return false;
		}
	}

	@GlobalCommand
	public void closeCodeListImportPopUp() {
		closePopUp(codeListImportPopUp);
		codeListImportPopUp = null;
		if ( editedItem != null ) {
			boolean hasMultipleLevels = editedItem.getHierarchy().size() > 1;
			Type type = hasMultipleLevels ? Type.HIERARCHICAL: Type.FLAT;
			CodeListFormObject fo = (CodeListFormObject) formObject;
			fo.setType(type.name());
			selectedItemsPerLevel = new ArrayList<CodeListItem>();
			initItemsPerLevel();
			notifyChange("formObject","listLevels","selectedItemsPerLevel");
		}
	}
	
	@GlobalCommand
	public void closeCodeListsManagerPopUp() {
		resetEditedItem();
		notifyChange("items");
	}

	private void addChildItemToCodeList() {
		if ( editedItem.isEmpty() && isSurveyStored() ) {
			//persist item in db
			PersistedCodeListItem persistedChildItem = (PersistedCodeListItem) editedChildItem;
			if ( editedChildItemParentItem != null ) {
				persistedChildItem.setParentId(((PersistedCodeListItem) editedChildItemParentItem).getSystemId());
			}
			codeListManager.save(persistedChildItem);
			dispatchSurveySaveCommand();
		} else if ( editedChildItemParentItem == null ) {
			//add item among the root items
			editedItem.addItem(editedChildItem);
		} else {
			//add item as a child of the edited parent item in the code list
			editedChildItemParentItem.addChildItem(editedChildItem);
		}
		List<CodeListItem> itemsForCurrentLevel = itemsPerLevel.get(editedChildItemLevelIndex);
		itemsForCurrentLevel.add(editedChildItem);
		deselectItemsAfterLevel(editedChildItemLevelIndex);
		selectedItemsPerLevel.add(editedChildItem);
		initItemsPerLevel();
		notifyChange("itemsPerLevel","selectedItemsPerLevel");
		BindUtils.postNotifyChange(null, null, editedItem, ".");
	}

	protected void initItemsPerLevel() {
		itemsPerLevel = new ArrayList<List<CodeListItem>>();
		if ( /*isSurveyStored() && */ editedItem != null && ! editedItem.isExternal() ) {
			List<CodeListItem> rootItems = codeListManager.loadRootItems(editedItem);
			itemsPerLevel.add(new ArrayList<CodeListItem>(rootItems));
			for (CodeListItem selectedItem : selectedItemsPerLevel) {
				List<CodeListItem> childItems = codeListManager.loadChildItems(selectedItem);
				itemsPerLevel.add(new ArrayList<CodeListItem>(childItems));
			}
		} else {
			//add empty root items list
			itemsPerLevel.add(new ArrayList<CodeListItem>());
		}
	}
	
	public List<CodeListLevel> getListLevels() {
		List<CodeListLevel> levels = null;
		if ( editedItem != null ) {
			levels = editedItem.getHierarchy();
			if ( levels.isEmpty() ) {
				CodeListLevel fakeFirstLevel = new CodeListLevel();
				return Arrays.asList(fakeFirstLevel);
			}
		}
		return levels;
	}
	
	public List<CodeListItem> getSelectedItemsPerLevel() {
		return selectedItemsPerLevel;
	}
	
	@DependsOn("selectedItemsPerLevel")
	public int getLastSelectedLevelIndex() {
		return selectedItemsPerLevel.size() - 1;
	}
	
	public List<List<CodeListItem>> getItemsPerLevel() {
		return itemsPerLevel;
	}
	
	public boolean isCodeListItemSelected(CodeListItem item) {
		return selectedItemsPerLevel.contains(item);
	}
	
	public boolean hasChildItems(CodeListItem item) {
		boolean result = codeListManager.hasChildItems(item);
		return result;
	}
	
	public String getCodeListItemLabel(CodeListItem item) {
		String label = item.getLabel(currentLanguageCode);
		return label;
	}

	public boolean isEditingAttribute() {
		return editingAttribute;
	}
	
	public boolean hasWarnings(CodeList list) {
		return ! codeListManager.isInUse(list) || codeListManager.isEmpty(list);
	}
	
	public String getWarnings(CodeList list) {
		String messageKey;
		if ( codeListManager.isEmpty(list) ) {
			messageKey = "survey.validation.error.empty_code_list";
		} else if ( ! codeListManager.isInUse(list) ) {
			messageKey = "survey.validation.error.unused_code_list";
		} else {
			messageKey = null;
		}
		return messageKey == null ? null: Labels.getLabel(messageKey);
	}
	
	@GlobalCommand
	public void codeListAssigned(@BindingParam("list") CodeList list, @BindingParam("oldList") CodeList oldList) {
		BindUtils.postNotifyChange(null, null, list, ".");
		BindUtils.postNotifyChange(null, null, oldList, ".");
	}
	
	@Command
	public void close(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		event.stopPropagation();
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("editingAttribute", editingAttribute);
				params.put("selectedCodeList", selectedItem);
				BindUtils.postGlobalCommand((String) null, (String) null, "closeCodeListsManagerPopUp", params);
			}
		});
	}
	
}
