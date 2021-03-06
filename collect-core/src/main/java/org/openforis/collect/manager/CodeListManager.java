package org.openforis.collect.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.exception.CodeListImportException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.CodeListItemDao;
import org.openforis.collect.persistence.DatabaseExternalCodeListProvider;
import org.openforis.collect.service.CollectCodeListService;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.xml.CodeListImporter;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@Transactional
public class CodeListManager {
	
	@Autowired(required=false)
	private DatabaseExternalCodeListProvider provider;
	@Autowired(required=false)
	private CodeListItemDao codeListItemDao;
	
	public void importCodeLists(CollectSurvey survey, InputStream is) throws CodeListImportException {
		int nextSystemId = codeListItemDao.nextSystemId();
		CollectCodeListService service = new CollectCodeListService();
		service.setCodeListManager(this);
		CodeListImporter binder = new CodeListImporter(service, nextSystemId);
		try {
			binder.importCodeLists(survey, is);
		} catch (IdmlParseException e) {
			throw new CodeListImportException(e);
		}
	}
	
	public void importCodeLists(CollectSurvey survey, File file) throws CodeListImportException {
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			importCodeLists(survey, is);
		} catch (FileNotFoundException e) {
			throw new CodeListImportException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> T loadItemByAttribute(CodeAttribute attribute) {
		CodeAttributeDefinition defn = attribute.getDefinition();
		CodeList list = defn.getList();
		boolean persistedSurvey = list.getSurvey().getId() != null;
		if ( persistedSurvey && list.isExternal() ) {
			return (T) provider.getItem(attribute);
		} else if ( persistedSurvey && list.isEmpty() ) {
			return (T) loadPersistedItem(attribute);
		} else {
			return (T) getInternalCodeListItem(attribute);
		}
	}

	protected CodeListItem getInternalCodeListItem(CodeAttribute attribute) {
		Code code = attribute.getValue();
		if (code != null) {
			String codeValue = code.getCode();
			if (StringUtils.isNotBlank(codeValue)) {
				ModelVersion currentVersion = attribute.getRecord().getVersion();
				CodeAttributeDefinition definition = attribute.getDefinition();
				String parentExpression = definition.getParentExpression();
				if (StringUtils.isBlank(parentExpression)) {
					return findCodeListItem(definition.getList().getItems(), codeValue, currentVersion);
				} else {
					CodeAttribute codeParent = attribute.getCodeParent();
					if (codeParent != null) {
						CodeListItem codeListItemParent = loadItemByAttribute(codeParent);
						if (codeListItemParent != null) {
							return findCodeListItem(codeListItemParent.getChildItems(), codeValue, currentVersion);
						}
					}
				}
			}
		}
		return null;
	}

	protected PersistedCodeListItem loadPersistedItem(CodeAttribute attribute) {
		Code code = attribute.getValue();
		if ( code == null || StringUtils.isBlank(code.getCode()) ) {
			return null;
		} else {
			String codeVal = code.getCode();
			CodeAttributeDefinition defn = attribute.getDefinition();
			CodeList list = defn.getList();
			Record record = attribute.getRecord();
			ModelVersion version = record.getVersion();
			if ( StringUtils.isBlank(defn.getParentExpression()) ) {
				CodeListItem item = codeListItemDao.loadRootItem(list, codeVal, version);
				return (PersistedCodeListItem) item;
			} else {
				PersistedCodeListItem parentItem = (PersistedCodeListItem) loadParentItem(attribute);
				if ( parentItem == null ) {
					return null;
				} else {
					CodeListItem item = codeListItemDao.loadItem(list, parentItem.getSystemId(), codeVal, version);
					return (PersistedCodeListItem) item;
				}
			}
		}
	}
	
	protected CodeListItem loadParentItem(CodeAttribute attribute) {
		CodeList list = attribute.getDefinition().getList();
		boolean persistedSurvey = list.getSurvey().getId() != null;
		Record record = attribute.getRecord();
		ModelVersion version = record.getVersion();
		if ( persistedSurvey && list.isExternal() ) {
			ExternalCodeListItem item = (ExternalCodeListItem) loadItemByAttribute(attribute);
			return provider.getParentItem(item);
		} else if ( persistedSurvey && list.isEmpty() ) {
			PersistedCodeListItem lastParentItem = null;
			List<CodeAttribute> codeAncestors = attribute.getCodeAncestors();
			for (int i = 0; i < codeAncestors.size(); i++) {
				CodeAttribute ancestor = codeAncestors.get(i);
				Integer lastParentItemId = lastParentItem == null ? null: lastParentItem.getSystemId();
				Code code = ancestor.getValue();
				lastParentItem = codeListItemDao.loadItem(list, lastParentItemId, code.getCode(), version);
				if ( lastParentItem == null ) {
					break;
				}
			}
			return lastParentItem;
		} else {
			CodeAttribute codeParent = attribute.getCodeParent();
			if ( codeParent == null ) {
				return null;
			} else {
				return loadItemByAttribute(codeParent);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> List<T> loadItems(CodeList list, int level) {
		boolean persistedSurvey = list.getSurvey().getId() != null;
		if ( list.isExternal() ) {
			throw new UnsupportedOperationException();
		} else if ( persistedSurvey && list.isEmpty() ) {
			return (List<T>) codeListItemDao.loadItemsByLevel(list, level);
		} else {
			return (List<T>) list.getItems(level - 1);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> List<T> loadRootItems(CodeList list) {
		boolean persistedSurvey = list.getSurvey().getId() != null;
		if ( persistedSurvey && list.isExternal() ) {
			return (List<T>) provider.getRootItems(list);
		} else if ( persistedSurvey && list.isEmpty() ) {
			return (List<T>) codeListItemDao.loadRootItems(list);
		} else {
			return list.getItems();
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> T loadRootItem(CodeList list, String code, ModelVersion version) {
		boolean persistedSurvey = list.getSurvey().getId() != null;
		if ( persistedSurvey && list.isExternal() ) {
			return (T) provider.getRootItem(list, code);
		} else if ( persistedSurvey && list.isEmpty() ) {
			return (T) codeListItemDao.loadRootItem(list, code, version);
		} else {
			return (T) list.getItem(code, 0, version);
		}
	}
	
	public boolean hasChildItemsInLevel(CodeList list, int level) {
		boolean persistedSurvey = list.getSurvey().getId() != null;
		if ( list.isExternal() ) {
			throw new UnsupportedOperationException();
		} else if ( persistedSurvey && list.isEmpty() ) {
			return codeListItemDao.hasItemsInLevel(list, level);
		} else {
			return list.hasItemsInLevel(level - 1);
		}
	}
	
	public void removeLevel(CodeList list, int level) {
		boolean persistedSurvey = list.getSurvey().getId() != null;
		if ( list.isExternal() ) {
			throw new UnsupportedOperationException();
		} else if ( persistedSurvey && list.isEmpty() ) {
			codeListItemDao.removeItemsInLevel(list, level);
			list.removeLevel(level - 1);
		} else {
			list.removeLevel(level - 1);
		}
		
	}
	
	protected CodeListItem findCodeListItem(List<CodeListItem> siblings, String code, ModelVersion version) {
		String adaptedCode = code.trim();
		adaptedCode = adaptedCode.toUpperCase();
		//remove initial zeros
		adaptedCode = adaptedCode.replaceFirst("^0+", "");
		adaptedCode = Pattern.quote(adaptedCode);
		Pattern pattern = Pattern.compile("^[0]*" + adaptedCode + "$", Pattern.CASE_INSENSITIVE);

		for (CodeListItem item : siblings) {
			if ( version == null || version.isApplicable(item) ) {
				String itemCode = item.getCode();
				Matcher matcher = pattern.matcher(itemCode);
				if(matcher.find()) {
					return item;
				}
			}
		}
		return null;
	}
	
	public CodeListItem findValidItem(Entity parent,
			CodeAttributeDefinition defn, String code) {
		List<CodeListItem> items = findValidItems(parent, defn, code);
		return items.size() == 1 ? items.get(0): null;
	}

	public List<CodeListItem> findValidItems(Entity parent, CodeAttributeDefinition defn, String... codes) {
		List<CodeListItem> result = new ArrayList<CodeListItem>();
		List<CodeListItem> assignableItems = loadValidItems(parent, defn);
		if ( ! assignableItems.isEmpty() ) {
			Record record = parent.getRecord();
			ModelVersion version = record.getVersion();
			for (String code: codes) {
				CodeListItem item = findCodeListItem(assignableItems, code, version);
				if ( item != null ) {
					result.add(item);
				}
			}
		}
		return CollectionUtils.unmodifiableList(result);
	}

	public List<CodeListItem> loadValidItems(Entity parent, CodeAttributeDefinition def) {
		List<? extends CodeListItem> items = null;
		CodeList list = def.getList();
		if ( StringUtils.isEmpty(def.getParentExpression()) ) {
			items = loadRootItems(list);
		} else {
			CodeAttribute parentCodeAttribute = getCodeParent(parent, def);
			if ( parentCodeAttribute != null ) {
				CodeListItem parentCodeListItem = loadItemByAttribute(parentCodeAttribute);
				if ( parentCodeListItem != null ) {
					items = loadChildItems(parentCodeListItem);
				}
			}
		}
		Record record = parent.getRecord();
		ModelVersion version = record.getVersion();
		return filterApplicableItems(items, version);
	}

	protected List<CodeListItem> filterApplicableItems(
			List<? extends CodeListItem> items, ModelVersion version) {
		if ( items == null ) {
			return Collections.emptyList();
		} else {
			List<? extends CodeListItem> result;
			if ( version == null ) {
				result = items;
			} else {
				result = version.filterApplicableItems(items);
			}
			return CollectionUtils.unmodifiableList(result);
		}
	}
	
	protected CodeAttribute getCodeParent(Entity context, CodeAttributeDefinition def) {
		try {
			Survey survey = context.getSurvey();
			SurveyContext surveyContext = survey.getContext();
			ExpressionEvaluator expressionEvaluator = surveyContext.getExpressionEvaluator();
			String parentExpr = def.getParentExpression();
			Node<?> parentNode = expressionEvaluator.evaluateNode(context, null, parentExpr);
			if (parentNode != null && parentNode instanceof CodeAttribute) {
				return (CodeAttribute) parentNode;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> List<T> loadChildItems(CodeListItem parent) {
		CodeList list = parent.getCodeList();
		if ( list.isExternal() ) {
			return (List<T>) provider.getChildItems((ExternalCodeListItem) parent);
		} else if ( list.isEmpty() ) {
			return (List<T>) codeListItemDao.loadChildItems((PersistedCodeListItem) parent);
		} else {
			return parent.getChildItems();
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> T loadChildItem(CodeList list,
			String code, ModelVersion version) {
		if ( list.isExternal() ) {
			//TODO
			return null;
		} else if ( list.isEmpty() ) {
			return (T) codeListItemDao.loadItem(list, code, version);
		} else {
			Stack<CodeListItem> stack = new Stack<CodeListItem>();
			stack.addAll(list.getItems());
			while ( ! stack.isEmpty() ) {
				CodeListItem item = stack.pop();
				if ( item.matchCode(code) ) {
					return (T) item;
				} else {
					stack.addAll(item.getChildItems());
				}
			}
			return null;
		}
	}
	
	public boolean isEmpty(CodeList list) {
		if ( list.isExternal() ) {
			//TODO 
			return false;
		} else if ( list.isEmpty() ) {
			return codeListItemDao.isEmpty(list);
		} else {
			return false;
		}
	}
	
	public boolean hasChildItems(CodeListItem parent) {
		CodeList list = parent.getCodeList();
		if ( list.isExternal() ) {
			return provider.hasChildItems((ExternalCodeListItem) parent);
		} else if ( list.isEmpty() ) {
			return codeListItemDao.hasChildItems(list, ((PersistedCodeListItem) parent).getSystemId());
		} else {
			List<CodeListItem> childItems = parent.getChildItems();
			return ! childItems.isEmpty();
		}
	}
	
	public boolean hasQualifiableItems(CodeList list) {
		if ( list.isExternal() ) {
			return false;
		} else if ( list.isEmpty() ) {
			return codeListItemDao.hasQualifiableItems(list);
		} else {
			return list.isQualifiable();
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> T loadChildItem(T parent, String code, ModelVersion version) {
		CodeList list = parent.getCodeList();
		if ( list.isExternal() ) {
			return (T) provider.getChildItem((ExternalCodeListItem) parent, code);
		} else if ( list.isEmpty() ) {
			return (T) codeListItemDao.loadItem(list, ((PersistedCodeListItem) parent).getSystemId(), code, version);
		} else {
			return (T) parent.getChildItem(code);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> T loadParentItem(T item) {
		CodeList list = item.getCodeList();
		if ( list.isExternal() ) {
			return (T) provider.getParentItem((ExternalCodeListItem) item);
		} else if ( list.isEmpty() ) {
			Integer parentId = ((PersistedCodeListItem) item).getParentId();
			if ( parentId != null ) {
				return (T) codeListItemDao.loadById(list, parentId);
			} else {
				return null;
			}
		} else {
			return (T) item.getParentItem();
		}
	}
	
	@Transactional
	public void publishCodeLists(int surveyWorkId, int publishedSurveyId) {
		codeListItemDao.deleteBySurvey(publishedSurveyId);
		codeListItemDao.moveItemsToPublishedSurvey(surveyWorkId, publishedSurveyId);
	}
	
	@Transactional
	public void cloneCodeLists(CollectSurvey fromSurvey, CollectSurvey toSurvey) {
		codeListItemDao.cloneItems(fromSurvey.getId(), fromSurvey.isWork(), toSurvey.getId(), toSurvey.isWork());
	}
	
	public void save(PersistedCodeListItem item) {
		if ( item.getSystemId() == null ) {
			codeListItemDao.insert(item);
		} else {
			codeListItemDao.update(item);
		}
	}
	
	public void save(List<PersistedCodeListItem> items) {
		codeListItemDao.insert(items);
	}
	
	public void saveItemsAndDescendants(List<CodeListItem> items) {
		List<PersistedCodeListItem> persistedItems = createPersistedItems(items, codeListItemDao.nextSystemId(), null);
		save(persistedItems);
	}
	
	protected List<PersistedCodeListItem> createPersistedItems(Collection<CodeListItem> items,
			int nextId,
			Integer parentItemId) {
		List<PersistedCodeListItem> result = new ArrayList<PersistedCodeListItem>();
		int sortOrder = 1;
		for (CodeListItem item : items) {
			PersistedCodeListItem persistedChildItem = PersistedCodeListItem.fromItem(item);
			persistedChildItem.setParentId(parentItemId);
			int id = nextId++;
			persistedChildItem.setSystemId(id);
			persistedChildItem.setSortOrder(sortOrder++);
			result.add(persistedChildItem);
			List<PersistedCodeListItem> temp = createPersistedItems(item.getChildItems(), nextId, id);
			nextId+=temp.size();
			result.addAll(temp);
		}
		return result;
	}
	
	@Transactional
	public void delete(CodeList codeList) {
		Survey survey = codeList.getSurvey();
		deleteAllItems(codeList);
		survey.removeCodeList(codeList);
	}

	public void delete(CodeListItem item) {
		CodeList list = item.getCodeList();
		if ( list.isExternal() ) {
			throw new UnsupportedOperationException();
		} else if ( list.isEmpty() ) {
			codeListItemDao.delete((PersistedCodeListItem) item);
		} else {
			CodeListItem parentItem = item.getParentItem();
			int id = item.getId();
			if ( parentItem == null ) {
				CodeList codeList = item.getCodeList();
				codeList.removeItem(id);
			} else {
				parentItem.removeChildItem(id);
			}
		}
	}
	
	public void deleteAllItems(CodeList list) {
		if ( list.isExternal()) {
			throw new UnsupportedOperationException();
		} else if ( list.isEmpty() ) {
			codeListItemDao.deleteByCodeList(list);
		} else {
			list.removeAllItems();
		}
	}

	public void deleteAllItemsBySurvey(int surveyId, boolean work) {
		codeListItemDao.deleteBySurvey(surveyId, work);
	}

	public void deleteInvalidCodeListReferenceItems(CollectSurvey survey) {
		codeListItemDao.deleteInvalidCodeListReferenceItems(survey);
	}
	
	public void removeVersioningReference(CollectSurvey survey, ModelVersion version) {
		List<CodeList> codeLists = survey.getCodeLists();
		for (CodeList codeList : codeLists) {
			if ( codeList.isExternal() ) {
				//TODO
			} else if ( codeList.isEmpty() ) {
				codeListItemDao.removeVersioningInfo(codeList, version);
			} else {
				codeList.removeVersioningRecursive(version);
			}
		}
	}

	public void deleteUnusedCodeLists(CollectSurvey survey) {
		Set<CodeList> unusedCodeLists = getUnusedCodeLists(survey);
		for (CodeList codeList : unusedCodeLists) {
			delete(codeList);
		}
	}
	
	public void shiftItem(CodeListItem item, int indexTo) {
		CodeList list = item.getCodeList();
		if ( list.isExternal() ) {
			throw new UnsupportedOperationException();
		} else if ( list.isEmpty()) {
			codeListItemDao.shiftItem((PersistedCodeListItem) item, indexTo);
		} else {
			CodeListItem parentItem = item.getParentItem();
			if ( parentItem == null ) {
				CodeList codeList = item.getCodeList();
				codeList.moveItem(item, indexTo);
			} else {
				parentItem.moveChildItem(item, indexTo);
			}
		}
	}
	
	protected Set<CodeList> getUnusedCodeLists(CollectSurvey survey) {
		Set<CodeList> result = new HashSet<CodeList>();
		List<CodeList> codeLists = survey.getCodeLists();
		for (CodeList list : codeLists) {
			if ( ! isInUse(list) ) {
				result.add(list);
			}
		}
		return result;
	}
	
	public boolean isInUse(CodeList list) {
		Survey survey = list.getSurvey();
		Schema schema = survey.getSchema();
		Stack<NodeDefinition> stack = new Stack<NodeDefinition>();
		stack.addAll(schema.getRootEntityDefinitions());
		while ( ! stack.isEmpty() ) {
			NodeDefinition node = stack.pop();
			if ( node instanceof CodeAttributeDefinition ) {
				if ( list.equals(((CodeAttributeDefinition) node).getList()) ) {
					return true;
				}
			} else if ( node instanceof EntityDefinition ) {
				for (NodeDefinition nodeDefinition : ((EntityDefinition) node).getChildDefinitions()) {
					stack.add(nodeDefinition);
				}
			} 
		}
		return false;
	}
	
	public int nextSystemId() {
		return codeListItemDao.nextSystemId();
	}
	
	public void setExternalCodeListProvider(
			DatabaseExternalCodeListProvider externalCodeListProvider) {
		this.provider = externalCodeListProvider;
	}

	public void setCodeListItemDao(CodeListItemDao codeListItemDao) {
		this.codeListItemDao = codeListItemDao;
	}


}
