package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_CODE_LIST_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcCodeList.OFC_CODE_LIST;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.jooq.BatchBindStep;
import org.jooq.Condition;
import org.jooq.DeleteConditionStep;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectQuery;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.MappingDSLContext;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyObject;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class CodeListItemDao extends MappingJooqDaoSupport<PersistedCodeListItem, CodeListItemDao.JooqDSLContext> {
	
	@SuppressWarnings("rawtypes")
	private static final TableField[] LABEL_FIELDS = {OFC_CODE_LIST.LABEL1, OFC_CODE_LIST.LABEL2, OFC_CODE_LIST.LABEL3}; 
	@SuppressWarnings("rawtypes")
	private static final TableField[] DESCRIPTION_FIELDS = {OFC_CODE_LIST.DESCRIPTION1, OFC_CODE_LIST.DESCRIPTION2, OFC_CODE_LIST.DESCRIPTION3}; 
	@SuppressWarnings("rawtypes")
	private static final TableField[] FIELDS = {
			OFC_CODE_LIST.ID,
			OFC_CODE_LIST.SURVEY_ID,
			OFC_CODE_LIST.SURVEY_WORK_ID,
			OFC_CODE_LIST.CODE_LIST_ID,
			OFC_CODE_LIST.ITEM_ID,
			OFC_CODE_LIST.PARENT_ID,
			OFC_CODE_LIST.LEVEL,
			OFC_CODE_LIST.SORT_ORDER,
			OFC_CODE_LIST.CODE,
			OFC_CODE_LIST.QUALIFIABLE,
			OFC_CODE_LIST.SINCE_VERSION_ID,
			OFC_CODE_LIST.DEPRECATED_VERSION_ID,
			OFC_CODE_LIST.LABEL1, 
			OFC_CODE_LIST.LABEL2, 
			OFC_CODE_LIST.LABEL3,
			OFC_CODE_LIST.DESCRIPTION1, 
			OFC_CODE_LIST.DESCRIPTION2, 
			OFC_CODE_LIST.DESCRIPTION3
	};
	
	private boolean useCache;
	private CodeListItemCache cache;
	
	public CodeListItemDao() {
		super(CodeListItemDao.JooqDSLContext.class);
		useCache = false;
		cache = new CodeListItemCache();
	}

	public PersistedCodeListItem loadById(CodeList list, int id) {
		JooqDSLContext jf = dsl(list);
		ResultQuery<?> selectQuery = jf.selectByIdQuery(id);
		Record r = selectQuery.fetchOne();
		if ( r == null ) {
			return null;
		} else {
			return jf.fromRecord(r);
		}
	}

	@Override
	public void insert(PersistedCodeListItem item) {
		JooqDSLContext jf = dsl(item.getCodeList());
		jf.insertQuery(item).execute();
	}
	
	/**
	 * Inserts the items in batch.
	 * 
	 * @param items
	 */
	public void insert(List<PersistedCodeListItem> items) {
		if ( items != null && items.size() > 0 ) {
			PersistedCodeListItem firstItem = items.get(0);
			CodeList list = firstItem.getCodeList();
			JooqDSLContext jf = dsl(list);
			int nextId = jf.nextId();
			int maxId = nextId;
			Insert<OfcCodeListRecord> query = jf.createInsertStatement();
			BatchBindStep batch = jf.batch(query);
			for (PersistedCodeListItem item : items) {
				Integer id = item.getSystemId();
				if ( id == null ) {
					id = nextId++;
					item.setSystemId(id);
				}
				Object[] values = jf.extractValues(item);
				batch.bind(values);
				maxId = Math.max(maxId, id);
			}
			batch.execute();
			jf.restartSequence(maxId + 1);
		}
	}
	
	public void cloneItems(int oldSurveyId, boolean oldSurveyWork, int newSurveyId, boolean newSurveyWork) {
		JooqDSLContext jf = dsl(null);
		int minId = loadMinId(jf, oldSurveyId, oldSurveyWork);
		int nextId = jf.nextId();
		int idGap = nextId - minId;
		Integer selectSurveyIdValue = newSurveyWork ? null: newSurveyId;
		Integer selectSurveyWorkIdValue = newSurveyWork ? newSurveyId: null;
		Field<?>[] selectFields = {
				OFC_CODE_LIST.ID.add(idGap),
				DSL.val(selectSurveyIdValue, OFC_CODE_LIST.SURVEY_ID),
				DSL.val(selectSurveyWorkIdValue, OFC_CODE_LIST.SURVEY_WORK_ID),
				OFC_CODE_LIST.CODE_LIST_ID,
				OFC_CODE_LIST.ITEM_ID,
				OFC_CODE_LIST.PARENT_ID.add(idGap),
				OFC_CODE_LIST.LEVEL,
				OFC_CODE_LIST.SORT_ORDER,
				OFC_CODE_LIST.CODE,
				OFC_CODE_LIST.QUALIFIABLE,
				OFC_CODE_LIST.SINCE_VERSION_ID,
				OFC_CODE_LIST.DEPRECATED_VERSION_ID
			};
		selectFields = ArrayUtils.addAll(selectFields, LABEL_FIELDS);
		selectFields = ArrayUtils.addAll(selectFields, DESCRIPTION_FIELDS);
		TableField<OfcCodeListRecord, Integer> oldSurveyIdField = getSurveyIdField(oldSurveyWork);
		TableField<OfcCodeListRecord, Integer> oppositeOldSurveyIdField = getSurveyIdField(! oldSurveyWork);
		Select<?> select = jf.select(selectFields)
			.from(OFC_CODE_LIST)
			.where(oldSurveyIdField.equal(oldSurveyId)
					.and(oppositeOldSurveyIdField.isNull()))
			.orderBy(OFC_CODE_LIST.PARENT_ID, OFC_CODE_LIST.ID);
		Insert<OfcCodeListRecord> insert = jf.insertInto(OFC_CODE_LIST, FIELDS).select(select);
		insert.execute();
		restartIdSequence(jf);
	}

	private void restartIdSequence(JooqDSLContext jf) {
		int maxId = loadMaxId(jf);
		jf.restartSequence(OFC_CODE_LIST_ID_SEQ, maxId + 1);
	}

	@Override
	public void update(PersistedCodeListItem item) {
		CodeList codeList = item.getCodeList();
		JooqDSLContext jf = dsl(codeList);
		jf.updateQuery(item).execute();
		
		if ( useCache ) {
			CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
			cache.removeItemsByCodeList(survey.getId(), survey.isWork(), codeList.getId());
		}
	}
	
	public void shiftItem(PersistedCodeListItem item, int toIndex) {
		CodeList list = item.getCodeList();
		List<PersistedCodeListItem> siblings = loadChildItems(list, item.getParentId()); 
		int newSortOrder;
		int prevItemIdx;
		if ( toIndex >= siblings.size() ) {
			prevItemIdx = siblings.size() - 1;
		} else {
			prevItemIdx = toIndex;
		}
		PersistedCodeListItem previousItem = siblings.get(prevItemIdx);
		newSortOrder = previousItem.getSortOrder();
		updateSortOrder(item, newSortOrder);
	}

	protected void updateSortOrder(PersistedCodeListItem item, int newSortOrder) {
		CodeList list = item.getCodeList();
		CollectSurvey survey = (CollectSurvey) list.getSurvey();
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(survey.isWork());
		TableField<OfcCodeListRecord, Integer> oppositeSurveyIdField = getSurveyIdField(! survey.isWork());
		JooqDSLContext dsl = dsl(list);
		Integer oldSortOrder = item.getSortOrder();
		if ( newSortOrder == oldSortOrder ) {
			return;
		} else {
			//give top sort order to the item
			dsl.update(OFC_CODE_LIST)
				.set(OFC_CODE_LIST.SORT_ORDER, 0)
				.where(OFC_CODE_LIST.ID.equal(item.getSystemId()))
				.execute();
			Condition parentIdCondition = item.getParentId() == null ? 
				OFC_CODE_LIST.PARENT_ID.isNull(): 
				OFC_CODE_LIST.PARENT_ID.equal(item.getParentId());
			if ( newSortOrder > oldSortOrder ) {
				//move backwards previous items
				dsl.update(OFC_CODE_LIST)
					.set(OFC_CODE_LIST.SORT_ORDER, OFC_CODE_LIST.SORT_ORDER.sub(1))
					.where(
						surveyIdField.equal(survey.getId()),
						oppositeSurveyIdField.isNull(),
						OFC_CODE_LIST.CODE_LIST_ID.equal(list.getId()),
						parentIdCondition,
						OFC_CODE_LIST.SORT_ORDER.greaterThan(oldSortOrder),
						OFC_CODE_LIST.SORT_ORDER.lessOrEqual(newSortOrder)
					).execute();
			} else {
				//move forward next items
				dsl.update(OFC_CODE_LIST)
					.set(OFC_CODE_LIST.SORT_ORDER, OFC_CODE_LIST.SORT_ORDER.add(1))
					.where(
						surveyIdField.equal(survey.getId()),
						oppositeSurveyIdField.isNull(),
						OFC_CODE_LIST.CODE_LIST_ID.equal(list.getId()),
						parentIdCondition,
						OFC_CODE_LIST.SORT_ORDER.greaterOrEqual(newSortOrder),
						OFC_CODE_LIST.SORT_ORDER.lessThan(oldSortOrder)
					).execute();
			}
			//set item sort order to final value
			dsl.update(OFC_CODE_LIST)
				.set(OFC_CODE_LIST.SORT_ORDER, newSortOrder)
				.where(OFC_CODE_LIST.ID.equal(item.getSystemId()))
				.execute();
		}
	}

	public void delete(PersistedCodeListItem item) {
		CodeList codeList = item.getCodeList();

		JooqDSLContext dsl = dsl(null);
		dsl.deleteQuery(item.getSystemId()).execute();

		if ( dsl.getDialect() == SQLDialect.SQLITE ) {
			//SQLite foreign keys support disabled in order to have better performances
			//delete referencing items programmatically
			deleteInvalidParentReferenceItems(codeList);
		}
		if ( useCache ) {
			CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
			cache.removeItemsByCodeList(survey.getId(), survey.isWork(), codeList.getId());
		}
	}

	public void deleteByCodeList(CodeList list) {
		DeleteConditionStep<OfcCodeListRecord> q = createDeleteQuery(list);
		q.execute();
		if ( useCache ) {
			CollectSurvey survey = (CollectSurvey) list.getSurvey();
			cache.removeItemsByCodeList(survey.getId(), survey.isWork(), list.getId());
		}
	}
	
	protected DeleteConditionStep<OfcCodeListRecord> createDeleteQuery(CodeList list) {
		JooqDSLContext jf = dsl(null);
		CollectSurvey survey = (CollectSurvey) list.getSurvey();
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(survey.isWork());
		TableField<OfcCodeListRecord, Integer> oppositeSurveyIdField = getSurveyIdField(! survey.isWork());
		DeleteConditionStep<OfcCodeListRecord> q = jf.delete(OFC_CODE_LIST)
			.where(
					surveyIdField.equal(survey.getId()),
					oppositeSurveyIdField.isNull(),
					OFC_CODE_LIST.CODE_LIST_ID.equal(list.getId())
			);
		return q;
	}
	
	public void deleteBySurvey(int surveyId) {
		deleteBySurvey(surveyId, false);
	}
	
	public void deleteBySurveyWork(int surveyId) {
		deleteBySurvey(surveyId, true);
	}
	
	public void deleteBySurvey(int surveyId, boolean work) {
		JooqDSLContext jf = dsl(null);
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(work);
		TableField<OfcCodeListRecord, Integer> oppositeSurveyIdField = getSurveyIdField(! work);
		jf.delete(OFC_CODE_LIST)
			.where(
					surveyIdField.equal(surveyId)).
						and(oppositeSurveyIdField.isNull())
			.execute();
		
		if ( useCache ) {
			cache.removeItemsBySurvey(surveyId, work);;
		}
	}
	
	public void deleteInvalidCodeListReferenceItems(CollectSurvey survey) {
		//create delete where condition
 		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(survey.isWork());
		TableField<OfcCodeListRecord, Integer> oppositeSurveyIdField = getSurveyIdField(! survey.isWork());

		Condition whereCondition = surveyIdField.equal(survey.getId())
										.and(oppositeSurveyIdField.isNull());
		
		List<Integer> codeListsIds = new ArrayList<Integer>();
		if ( ! survey.getCodeLists().isEmpty() ) {
			//include items that belongs to detached code lists
	 		for (CodeList codeList : survey.getCodeLists()) {
				codeListsIds.add(codeList.getId());
			}
			whereCondition = whereCondition.and(OFC_CODE_LIST.CODE_LIST_ID.notIn(codeListsIds));
		}
		//execute delete
		JooqDSLContext jf = dsl(null);
		jf.delete(OFC_CODE_LIST).where(whereCondition).execute();
		
		if ( useCache ) {
			for (Integer codeListId : codeListsIds) {
				cache.removeItemsByCodeList(survey.getId(), survey.isWork(), codeListId);
			}
		}
	}
	
	public void deleteInvalidParentReferenceItems(CodeList codeList) {
		CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(survey.isWork());
		TableField<OfcCodeListRecord, Integer> oppositeSurveyIdField = getSurveyIdField(! survey.isWork());
		int codeListId = codeList.getId();
		JooqDSLContext jf = dsl(null);
		jf.delete(OFC_CODE_LIST)
			.where(OFC_CODE_LIST.CODE_LIST_ID.eq(codeListId)
				.and(surveyIdField.eq(survey.getId()))
				.and(oppositeSurveyIdField.isNull())
				.and(OFC_CODE_LIST.PARENT_ID.isNotNull())
				.and(OFC_CODE_LIST.PARENT_ID.notIn(
					jf.select(OFC_CODE_LIST.ID)
						.from(OFC_CODE_LIST)
						.where(OFC_CODE_LIST.CODE_LIST_ID.eq(codeListId))
						)
					)
				)
			.execute();
		
		if ( useCache ) {
			cache.removeItemsByCodeList(survey.getId(), survey.isWork(), codeListId);
		}
	}
	
	public void removeVersioningInfo(CodeList codeList, ModelVersion version) {
		JooqDSLContext jf = dsl(null);
		CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(survey.isWork());
		TableField<OfcCodeListRecord, Integer> oppositeSurveyIdField = getSurveyIdField(! survey.isWork());
		int codeListId = codeList.getId();
		jf.update(OFC_CODE_LIST)
			.set(OFC_CODE_LIST.SINCE_VERSION_ID, (Integer) null)
			.where(surveyIdField.eq(survey.getId())
				.and(oppositeSurveyIdField.isNull())
				.and(OFC_CODE_LIST.CODE_LIST_ID.eq(codeListId))
				.and(OFC_CODE_LIST.SINCE_VERSION_ID.eq(version.getId()))
				)
			.execute();

		jf.update(OFC_CODE_LIST)
			.set(OFC_CODE_LIST.DEPRECATED_VERSION_ID, (Integer) null)
			.where(surveyIdField.eq(survey.getId())
				.and(oppositeSurveyIdField.isNull())
				.and(OFC_CODE_LIST.CODE_LIST_ID.eq(codeListId))
				.and(OFC_CODE_LIST.DEPRECATED_VERSION_ID.eq(version.getId()))
				)
			.execute();

		if ( useCache ) {
			cache.removeItemsByCodeList(survey.getId(), survey.isWork(), codeListId);
		}
	}

	public void moveItemsToPublishedSurvey(int surveyWorkId, int publishedSurveyId) {
		JooqDSLContext jf = dsl(null);
		jf.update(OFC_CODE_LIST)
			.set(OFC_CODE_LIST.SURVEY_ID, publishedSurveyId)
			.set(OFC_CODE_LIST.SURVEY_WORK_ID, (Integer) null)
			.where(OFC_CODE_LIST.SURVEY_WORK_ID.equal(surveyWorkId)
					.and(OFC_CODE_LIST.SURVEY_ID.isNull()))
			.execute();
		
		if ( useCache ) {
			cache.removeItemsBySurvey(surveyWorkId, true);
		}
	}

	public List<PersistedCodeListItem> loadRootItems(CodeList codeList) {
		return loadChildItems(codeList, (Integer) null, (ModelVersion) null);
	}
	
	public PersistedCodeListItem loadRootItem(CodeList codeList, String code) {
		return loadRootItem(codeList, code, (ModelVersion) null);
	}
	
	public PersistedCodeListItem loadRootItem(CodeList codeList, String code, ModelVersion version) {
		return loadItem(codeList, (Integer) null, code, version);
	}
	
	public List<PersistedCodeListItem> loadChildItems(PersistedCodeListItem item) {
		return loadChildItems(item, (ModelVersion) null);
	}
	
	public List<PersistedCodeListItem> loadChildItems(PersistedCodeListItem item, ModelVersion version) {
		return loadChildItems(item.getCodeList(), item.getSystemId(), version);
	}
	
	protected List<PersistedCodeListItem> loadChildItems(CodeList codeList, Integer parentItemId) {
		return loadChildItems(codeList, parentItemId, (ModelVersion) null);
	}
	
	protected List<PersistedCodeListItem> loadChildItems(CodeList codeList, Integer parentItemId, ModelVersion version) {
		JooqDSLContext dsl = dsl(codeList);
		SelectQuery<Record> q = createSelectChildItemsQuery(dsl, codeList, parentItemId, true);
		Result<Record> result = q.fetch();
		return dsl.fromResult(result);
	}
	
	public List<PersistedCodeListItem> loadItemsByLevel(CodeList list, int levelPosition) {
		JooqDSLContext jf = dsl(list);
		SelectQuery<Record> q = createSelectFromCodeListQuery(jf, list);
		q.addConditions(OFC_CODE_LIST.LEVEL.equal(levelPosition));
		Result<Record> result = q.fetch();
		return jf.fromResult(result);
	}
	
	public boolean isEmpty(CodeList list) {
		return ! hasChildItems(list, (Integer) null);
	}

	public boolean hasChildItems(CodeList codeList, Integer parentItemId) {
		JooqDSLContext jf = dsl(codeList);
		SelectQuery<Record> q = createSelectChildItemsQuery(jf, codeList, parentItemId, false);
		q.addSelect(DSL.count());
		Record record = q.fetchOne();
		Integer count = (Integer) record.getValue(0);
		return count > 0;
	}
	
	public boolean hasItemsInLevel(CodeList codeList, int level) {
		JooqDSLContext jf = dsl(codeList);
		SelectQuery<Record> q = createSelectFromCodeListQuery(jf, codeList);
		q.addSelect(DSL.count());
		q.addConditions(OFC_CODE_LIST.LEVEL.equal(level));
		Record record = q.fetchOne();
		Integer count = (Integer) record.getValue(0);
		return count > 0;
	}
	
	public void removeItemsInLevel(CodeList list, int level) {
		DeleteConditionStep<OfcCodeListRecord> q = createDeleteQuery(list);
		q.and(OFC_CODE_LIST.LEVEL.equal(level));
		q.execute();
	}

	public boolean hasQualifiableItems(CodeList codeList) {
		JooqDSLContext jf = dsl(codeList);
		CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(survey.isWork());
		TableField<OfcCodeListRecord, Integer> oppositeSurveyIdField = getSurveyIdField(! survey.isWork());
		SelectConditionStep<Record1<Integer>> q = jf.selectCount()
				.from(OFC_CODE_LIST)
				.where(
					surveyIdField.equal(survey.getId()),
					oppositeSurveyIdField.isNull(),
					OFC_CODE_LIST.CODE_LIST_ID.equal(codeList.getId()),
					OFC_CODE_LIST.QUALIFIABLE.equal(Boolean.TRUE)
				);
		Record r = q.fetchOne();
		if ( r == null ) {
			return false;
		} else {
			Integer count = (Integer) r.getValue(0);
			return count > 0;
		}
	}
	
	public PersistedCodeListItem loadItem(CodeList codeList, Integer parentItemId, String code, ModelVersion version) {
		if ( useCache && version == null ) {
			CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
			PersistedCodeListItem item = cache.getItem(survey.getId(), survey.isWork(), codeList.getId(), parentItemId, code);
			if ( item != null ) {
				return item;
			}
		}
		JooqDSLContext jf = dsl(codeList);
		SelectQuery<Record> q = createSelectChildItemsQuery(jf, codeList, parentItemId, false);
		q.addConditions(
				OFC_CODE_LIST.CODE.equal(code)
		);
		Result<Record> result = q.fetch();
		List<PersistedCodeListItem> list = jf.fromResult(result);
		
		List<PersistedCodeListItem> filteredByVersion = filterApplicableItems(list, version);
		
		PersistedCodeListItem item = filteredByVersion.isEmpty() ? null: filteredByVersion.get(0);
		
		if ( useCache && version == null ) {
			cache.addItem(codeList, parentItemId, code,item);
		}
		return item;
	}
	
	public PersistedCodeListItem loadItem(CodeList codeList, String code, ModelVersion version) {
		JooqDSLContext jf = dsl(codeList);
		SelectQuery<Record> q = createSelectFromCodeListQuery(jf, codeList);
		q.addConditions(OFC_CODE_LIST.CODE.equal(code));
		Result<Record> result = q.fetch();
		List<PersistedCodeListItem> list = jf.fromResult(result);
		List<PersistedCodeListItem> filteredByVersion = filterApplicableItems(list, version);
		if ( filteredByVersion.isEmpty() ) {
			return null;
		} else {
			return filteredByVersion.get(0);
		}
	}

	protected List<PersistedCodeListItem> filterApplicableItems(List<PersistedCodeListItem> list, ModelVersion version) {
		if ( version == null ) {
			return list;
		} else {
			List<PersistedCodeListItem> appliable = version.filterApplicableItems(list);
			return appliable;
		}
	}
	
	protected int loadMinId(JooqDSLContext jf, int surveyId, boolean work) {
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(work);
		TableField<OfcCodeListRecord, Integer> oppositeSurveyIdField = getSurveyIdField(! work);
		Integer result = jf.select(DSL.min(OFC_CODE_LIST.ID))
				.from(OFC_CODE_LIST)
				.where(surveyIdField.equal(surveyId)
						.and(oppositeSurveyIdField.isNull()))
				.fetchOne(0, Integer.class);
		return result == null ? 0: result.intValue();
	}

	protected int loadMaxId(JooqDSLContext jf) {
		Integer result = jf.select(DSL.max(OFC_CODE_LIST.ID))
				.from(OFC_CODE_LIST)
				.fetchOne(0, Integer.class);
		return result == null ? 0: result.intValue();
	}

	protected static SelectQuery<Record> createSelectChildItemsQuery(JooqDSLContext jf, CodeList codeList, Integer parentItemId, boolean addOrderByClause) {
		SelectQuery<Record> q = createSelectFromCodeListQuery(jf, codeList);
		addFilterByParentItemConditions(q, codeList, parentItemId);
		if ( addOrderByClause ) {
			q.addOrderBy(OFC_CODE_LIST.SORT_ORDER);
		}
		return q;
	}

	protected static void addFilterByParentItemConditions(SelectQuery<Record> select,
			CodeList codeList, Integer parentItemId) {
		Condition condition;
		if ( parentItemId == null ) {
			condition = OFC_CODE_LIST.PARENT_ID.isNull();
		} else {
			condition = OFC_CODE_LIST.PARENT_ID.equal(parentItemId);
		}
		select.addConditions(condition);
	}
	
	protected static SelectQuery<Record> createSelectFromCodeListQuery(JooqDSLContext dsl, CodeList codeList) {
		SelectQuery<Record> select = dsl.selectQuery();	
		select.addFrom(OFC_CODE_LIST);
		CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(survey.isWork());
		TableField<OfcCodeListRecord, Integer> oppositeSurveyIdField = getSurveyIdField(! survey.isWork());
		select.addConditions(
				surveyIdField.equal(survey.getId()),
				oppositeSurveyIdField.isNull(),
				OFC_CODE_LIST.CODE_LIST_ID.equal(codeList.getId())
				);
		return select;
	}
	
	
	@Override
	protected JooqDSLContext dsl() {
		throw new UnsupportedOperationException();
	}
	
	protected JooqDSLContext dsl(CodeList codeList) {
		Connection connection = getConnection();
		return new JooqDSLContext(connection, codeList);
	}
	
	public int nextSystemId() {
		JooqDSLContext jf = dsl(null);
		return jf.nextId();
	}
	
	public boolean isUseCache() {
		return useCache;
	}
	
	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	protected static TableField<OfcCodeListRecord, Integer> getSurveyIdField(
			boolean work) {
		TableField<OfcCodeListRecord, Integer> surveyIdField = work ? 
				OFC_CODE_LIST.SURVEY_WORK_ID: OFC_CODE_LIST.SURVEY_ID;
		return surveyIdField;
	}
	
	protected static class JooqDSLContext extends MappingDSLContext<PersistedCodeListItem> {

		private static final long serialVersionUID = 1L;
		
		private CodeList codeList;
		
		public JooqDSLContext(Connection connection, CodeList codeList) {
			super(connection, OFC_CODE_LIST.ID, OFC_CODE_LIST_ID_SEQ, PersistedCodeListItem.class);
			this.codeList = codeList;
		}
		
		@Override
		protected PersistedCodeListItem newEntity() {
			throw new UnsupportedOperationException();
		}
		
		protected PersistedCodeListItem newEntity(int itemId, int level) {
			return new PersistedCodeListItem(codeList, itemId, level);
		}
		
		@Override
		public PersistedCodeListItem fromRecord(Record record) {
			int itemId = record.getValue(OFC_CODE_LIST.ITEM_ID);
			int level = record.getValue(OFC_CODE_LIST.LEVEL);
			PersistedCodeListItem entity = newEntity(itemId, level);
			fromRecord(record, entity);
			return entity;
		}

		@Override
		public void fromRecord(Record r, PersistedCodeListItem i) {
			i.setSystemId(r.getValue(OFC_CODE_LIST.ID));
			i.setParentId(r.getValue(OFC_CODE_LIST.PARENT_ID));
			i.setSortOrder(r.getValue(OFC_CODE_LIST.SORT_ORDER));
			i.setCode(r.getValue(OFC_CODE_LIST.CODE));
			i.setQualifiable(r.getValue(OFC_CODE_LIST.QUALIFIABLE));
			i.setSinceVersion(extractModelVersion(r, i, OFC_CODE_LIST.SINCE_VERSION_ID));
			i.setDeprecatedVersion(extractModelVersion(r, i, OFC_CODE_LIST.DEPRECATED_VERSION_ID));
			extractLabels(r, i);
			extractDescriptions(r, i);
		}

		protected ModelVersion extractModelVersion(Record r,
				SurveyObject surveyObject,
				TableField<OfcCodeListRecord, Integer> versionIdField) {
			Survey survey = surveyObject.getSurvey();
			Integer versionId = r.getValue(versionIdField);
			ModelVersion version = versionId == null ? null: survey.getVersionById(versionId);
			return version;
		}
		
		protected void extractLabels(Record r, PersistedCodeListItem item) {
			Survey survey = codeList.getSurvey();
			item.removeAllLabels();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < languages.size(); i++) {
				String lang = languages.get(i);
				@SuppressWarnings("unchecked")
				String label = r.<String>getValue(LABEL_FIELDS[i]);
				item.setLabel(lang, label);
			}
		}
		
		protected void extractDescriptions(Record r, PersistedCodeListItem item) {
			Survey survey = codeList.getSurvey();
			item.removeAllDescriptions();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < languages.size(); i++) {
				String lang = languages.get(i);
				@SuppressWarnings("unchecked")
				String description = r.<String>getValue(DESCRIPTION_FIELDS[i]);
				item.setDescription(lang, description);
			}
		}

		@Override
		public void fromObject(PersistedCodeListItem item, StoreQuery<?> q) {
			q.addValue(OFC_CODE_LIST.ID, item.getSystemId());
			CollectSurvey survey = (CollectSurvey) item.getSurvey();
			Integer surveyId = survey.getId();
			if ( survey.isWork() ) {
				q.addValue(OFC_CODE_LIST.SURVEY_ID, (Integer) null);
				q.addValue(OFC_CODE_LIST.SURVEY_WORK_ID, surveyId);
			} else {
				q.addValue(OFC_CODE_LIST.SURVEY_ID, surveyId);
				q.addValue(OFC_CODE_LIST.SURVEY_WORK_ID, (Integer) null);
			}
			q.addValue(OFC_CODE_LIST.CODE_LIST_ID, item.getCodeList().getId());
			q.addValue(OFC_CODE_LIST.ITEM_ID, item.getId());
			q.addValue(OFC_CODE_LIST.PARENT_ID, item.getParentId());
			Integer sortOrder = item.getSortOrder();
			if ( sortOrder == null ) {
				sortOrder = nextSortOrder(item);
				item.setSortOrder(sortOrder);
			}
			q.addValue(OFC_CODE_LIST.LEVEL, item.getLevel());
			q.addValue(OFC_CODE_LIST.SORT_ORDER, sortOrder);
			q.addValue(OFC_CODE_LIST.CODE, item.getCode());
			q.addValue(OFC_CODE_LIST.QUALIFIABLE, item.isQualifiable());
			Integer sinceVersionId = item.getSinceVersion() == null ? null: item.getSinceVersion().getId();
			q.addValue(OFC_CODE_LIST.SINCE_VERSION_ID, sinceVersionId);
			Integer deprecatedVersionId = item.getDeprecatedVersion() == null ? null: item.getDeprecatedVersion().getId();
			q.addValue(OFC_CODE_LIST.DEPRECATED_VERSION_ID, deprecatedVersionId);
			addLabelValues(q, item);
			addDescriptionValues(q, item);
		}
		
		public Insert<OfcCodeListRecord> createInsertStatement() {
			Object[] valuesPlaceholders = new String[FIELDS.length];
			Arrays.fill(valuesPlaceholders, "?");
			return insertInto(OFC_CODE_LIST, FIELDS).values(valuesPlaceholders);
		}
		
		protected Object[] extractValues(PersistedCodeListItem item) {
			CodeList list = item.getCodeList();
			CollectSurvey survey = (CollectSurvey) item.getSurvey();
			Integer surveyId = survey.getId();
			boolean surveyWork = survey.isWork();
			ModelVersion sinceVersion = item.getSinceVersion();
			Integer sinceVersionId = sinceVersion == null ? null: sinceVersion.getId();
			ModelVersion deprecatedVersion = item.getDeprecatedVersion();
			Integer deprecatedVersionId = deprecatedVersion == null ? null: deprecatedVersion.getId();
			Object[] values = {item.getSystemId(), surveyWork ? null: surveyId, surveyWork ? surveyId: null, 
					list.getId(), item.getId(), item.getParentId(), item.getLevel(), item.getSortOrder(), item.getCode(), 
					item.isQualifiable(), sinceVersionId, deprecatedVersionId};
			Object[] labelValues = getLabelValues(item);
			values = ArrayUtils.addAll(values, labelValues);
			Object[] descriptionValues = getDescriptionValues(item);
			values = ArrayUtils.addAll(values, descriptionValues);
			return values;
		}

		private Integer nextSortOrder(PersistedCodeListItem item) {
			SelectQuery<Record> select = createSelectFromCodeListQuery(this, codeList);
			select.addSelect(DSL.max(OFC_CODE_LIST.SORT_ORDER));
			addFilterByParentItemConditions(select, codeList, item.getParentId());
			Record record = select.fetchOne();
			Integer max = (Integer) record.getValue(0);
			return max == null ? 1: max + 1;
		}

		protected void addLabelValues(StoreQuery<?> q, PersistedCodeListItem item) {
			String[] values = getLabelValues(item);
			for (int i = 0; i < LABEL_FIELDS.length; i++) {
				@SuppressWarnings("unchecked")
				TableField<?, String> field = LABEL_FIELDS[i];
				String label = values[i];
				q.addValue(field, label);
			}
		}
		
		protected String[] getLabelValues(PersistedCodeListItem item) {
			int size = LABEL_FIELDS.length;
			String[] result = new String[size];
			Survey survey = item.getSurvey();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < size; i++) {
				String label;
				if ( i < languages.size() ) {
					String lang = languages.get(i);
					label = item.getLabel(lang);
				} else {
					label = null;
				}
				result[i] = label;
			}
			return result;
		}

		protected void addDescriptionValues(StoreQuery<?> q, PersistedCodeListItem item) {
			String[] values = getDescriptionValues(item);
			for (int i = 0; i < DESCRIPTION_FIELDS.length; i++) {
				@SuppressWarnings("unchecked")
				TableField<?, String> field = DESCRIPTION_FIELDS[i];
				String description = values[i];
				q.addValue(field, description);
			}
		}

		protected String[] getDescriptionValues(PersistedCodeListItem item) {
			int size = DESCRIPTION_FIELDS.length;
			String[] result = new String[size];
			Survey survey = item.getSurvey();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < size; i++) {
				String description;
				if ( i < languages.size() ) {
					String lang = languages.get(i);
					description = item.getDescription(lang);
				} else {
					description = null;
				}
				result[i] = description;
			}
			return result;
		}
		
		@Override
		protected void setId(PersistedCodeListItem t, int id) {
			t.setSystemId(id);
		}

		@Override
		protected Integer getId(PersistedCodeListItem t) {
			return t.getSystemId();
		}
		
		@Override
		public int nextId() {
			return super.nextId();
		}
		
		@Override
		public void restartSequence(Number value) {
			super.restartSequence(value);
		}
	}

}
