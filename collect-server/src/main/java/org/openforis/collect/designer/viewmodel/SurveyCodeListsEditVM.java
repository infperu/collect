/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.form.CodeListFormObject;
import org.openforis.collect.designer.form.ItemFormObject;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLevel;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zkplus.databind.BindingListModelList;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyCodeListsEditVM extends SurveyItemEditVM<CodeList> {

	private static final String CODE_LIST_ITEM_EDIT_POP_UP_URL = "survey_edit_code_list_item_popup.zul";
	public static final String CLOSE_CODE_LIST_ITEM_POP_UP_COMMAND = "closeCodeListItemPopUp";

	private DefaultTreeModel<CodeListItem> treeModel;
	
	private List<List<CodeListItem>> itemsPerLevel;
	
	private List<CodeListItem> selectedItemsPerLevel;
	private Window codeListItemPopUp;
	
	@Override
	public BindingListModelList<CodeList> getItems() {
		CollectSurvey survey = getSurvey();
		List<CodeList> codeLists = survey.getCodeLists();
		return new BindingListModelList<CodeList>(codeLists, false);
	}

	@Override
	protected void addNewItemToSurvey() {
		CollectSurvey survey = getSurvey();
		survey.addCodeList(editedItem);
	}

	@Override
	protected void deleteItemFromSurvey(CodeList item) {
		CollectSurvey survey = getSurvey();
		survey.removeCodeList(item);
	}
	
	@Override
	protected ItemFormObject<CodeList> createFormObject() {
		return new CodeListFormObject();
	}

	@Command
	@NotifyChange({"formObject","editingItem","editedItem","items","selectedItem"})
	public void newItem() {
		super.newItem();
		editedItem.setSurvey(survey);
	}
	
	@Command
	@NotifyChange({"listLevels"})
	public void addLevel() {
		CodeListLevel level = new CodeListLevel();
		editedItem.addLevel(level);
	}

	@Command
	@NotifyChange({"listLevels"})
	public void removeLevel() {
		List<CodeListLevel> levels = editedItem.getHierarchy();
		if ( ! levels.isEmpty() ) {
			CodeListLevel lastLevel = levels.get(levels.size() - 1);
			editedItem.removeLevel(lastLevel.getId());
		}
	}

	@Command
	@NotifyChange({"itemsPerLevel"})
	public void addItemInLevel(@BindingParam("levelIndex") int levelIndex) {
		CodeListItem item = new CodeListItem();
		if ( levelIndex == 0 ) {
			editedItem.addItem(item);
		} else {
			CodeListItem parentItem = selectedItemsPerLevel.get(levelIndex - 1);
			parentItem.addChildItem(item);
		}
		List<CodeListItem> itemsForCurrentLevel = itemsPerLevel.get(levelIndex);
		itemsForCurrentLevel.add(item);
		openChildItemEditPopUp(item, levelIndex);
	}
	
	@Override
	public void setEditedItem(CodeList editedItem) {
		super.setEditedItem(editedItem);
		initTreeModel();
		selectedItemsPerLevel = new ArrayList<CodeListItem>();
		initItemsPerLevel();
	}
	
	public void openChildItemEditPopUp(CodeListItem item, int levelIndex) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("item", item);
		args.put("levelIndex", levelIndex);
		codeListItemPopUp = (Window) Executions.createComponents(
				CODE_LIST_ITEM_EDIT_POP_UP_URL, null, args);
		codeListItemPopUp.doModal();
	}

	@Command
	@NotifyChange({"itemsPerLevel","selectedItemsPerLevel"})
	public void listItemSelected(@BindingParam("item") CodeListItem item, 
			@BindingParam("levelIndex") int levelIndex) {
		deselectItemsAfterLevel(levelIndex);
		selectedItemsPerLevel.add(item);
		initItemsPerLevel();
	}

	private void deselectItemsAfterLevel(int levelIndex) {
		int maxSelectedLevelIndex = selectedItemsPerLevel.size() - 1;
		for (int i = maxSelectedLevelIndex; i >= levelIndex; i --) {
			selectedItemsPerLevel.remove(i);
		}
	}
	
	@GlobalCommand
	@NotifyChange({"itemsPerLevel"})
	public void closeCodeListItemPopUp() {
		closePopUp(codeListItemPopUp);
	}

	private void initItemsPerLevel() {
		itemsPerLevel = new ArrayList<List<CodeListItem>>();
		if ( editedItem != null ) {
			List<CodeListItem> items = new ArrayList<CodeListItem>(editedItem.getItems());
			itemsPerLevel.add(items);
			for (CodeListItem selectedItem : selectedItemsPerLevel) {
				List<CodeListItem> childItems = new ArrayList<CodeListItem>(selectedItem.getChildItems());
				itemsPerLevel.add(childItems);
			}
		}
	}
	
	public List<CodeListLevel> getListLevels() {
		List<CodeListLevel> levels = null;
		if ( editedItem != null ) {
			levels = editedItem.getHierarchy();
		}
		return levels;
	}
	
	public List<CodeListItem> getSelectedItemsPerLevel() {
		return selectedItemsPerLevel;
	}
	
	public List<List<CodeListItem>> getItemsPerLevel() {
		return itemsPerLevel;
	}
	
	protected void addTreeNode(CodeListItem item) {
		CodeListItemTreeNode treeNode = new CodeListItemTreeNode(item);
		int[] selectionPath = treeModel.getSelectionPath();
		if ( selectionPath == null || item.getParentItem() == null ) {
			treeModel.getRoot().add(treeNode);
		} else {
			TreeNode<CodeListItem> selectedTreeNode = treeModel.getChild(selectionPath);
			selectedTreeNode.add(treeNode);
		}
		treeModel.addOpenObject(treeNode.getParent());
		treeModel.setSelection(Arrays.asList(treeNode));
	}
	
	private void removeSelectedTreeNode() {
		int[] selectionPath = treeModel.getSelectionPath();
		TreeNode<CodeListItem> treeNode = treeModel.getChild(selectionPath);
		TreeNode<CodeListItem> parentTreeNode = treeNode.getParent();
		parentTreeNode.remove(treeNode);
	}
	
	public DefaultTreeModel<CodeListItem> getChildItems() {
		return treeModel;
    }

	private void initTreeModel() {
		if ( editedItem != null ) {
			List<CodeListItem> items = editedItem.getItems();
			List<TreeNode<CodeListItem>> treeNodes = CodeListItemTreeNode.fromList(items);
			TreeNode<CodeListItem> root = new CodeListItemTreeNode(null, treeNodes);
			treeModel = new DefaultTreeModel<CodeListItem>(root);
		} else {
			treeModel = null;
		}
	}
	
	public static class CodeListItemTreeNode extends DefaultTreeNode<CodeListItem> {
	     
		private static final long serialVersionUID = 1L;
		
		public CodeListItemTreeNode(CodeListItem data) {
			this(data, null);
		}

		public CodeListItemTreeNode(CodeListItem data, Collection<TreeNode<CodeListItem>> children) {
			super(data, children);
		}

		@Override
		public List<TreeNode<CodeListItem>> getChildren() {
			CodeListItem codeListItem = getData();
			if ( codeListItem != null ) {
				List<CodeListItem> items = codeListItem.getChildItems();
				return fromList(items);
			} else {
				return super.getChildren();
			}
		}

		public static List<TreeNode<CodeListItem>> fromList(List<CodeListItem> items) {
			List<TreeNode<CodeListItem>> result = null;
			if ( items != null ) {
				result = new ArrayList<TreeNode<CodeListItem>>();
				for (CodeListItem item : items) {
					List<CodeListItem> childItems = item.getChildItems();
					List<TreeNode<CodeListItem>> childrenNodes = fromList(childItems);
					CodeListItemTreeNode node = new CodeListItemTreeNode(item, childrenNodes);
					result.add(node);
				}
			}
			return result;
		}

	}
}
