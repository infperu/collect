package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_ERROR_REPORT_ITEM_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataErrorReportItem.OFC_DATA_ERROR_REPORT_ITEM;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.jooq.BatchBindStep;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataErrorReport;
import org.openforis.collect.datacleansing.DataErrorReportItem;
import org.openforis.collect.datacleansing.DataErrorReportItem.Status;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.PersistedObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataErrorReportItemRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class DataErrorReportItemDao extends MappingJooqDaoSupport<DataErrorReportItem, DataErrorReportItemDao.JooqDSLContext> {

	private static Field<?>[] FIELDS = new Field<?>[] {
		OFC_DATA_ERROR_REPORT_ITEM.ID,
		OFC_DATA_ERROR_REPORT_ITEM.NODE_INDEX,
		OFC_DATA_ERROR_REPORT_ITEM.PARENT_ENTITY_ID,
		OFC_DATA_ERROR_REPORT_ITEM.RECORD_ID,
		OFC_DATA_ERROR_REPORT_ITEM.REPORT_ID,
		OFC_DATA_ERROR_REPORT_ITEM.STATUS,
		OFC_DATA_ERROR_REPORT_ITEM.VALUE
	};
	
	public DataErrorReportItemDao() {
		super(DataErrorReportItemDao.JooqDSLContext.class);
	}

	public List<DataErrorReportItem> loadByReport(DataErrorReport report) {
		JooqDSLContext dsl = dsl(report);
		Select<OfcDataErrorReportItemRecord> select = dsl
			.selectFrom(OFC_DATA_ERROR_REPORT_ITEM)
			.where(OFC_DATA_ERROR_REPORT_ITEM.REPORT_ID.eq(report.getId()));
		Result<OfcDataErrorReportItemRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	public void insert(DataErrorReport report, List<DataErrorReportItem> items) {
		JooqDSLContext dsl = dsl(report);
		int nextId = dsl.nextId();
		int maxId = nextId;
		Insert<OfcDataErrorReportItemRecord> insert = dsl.createInsertStatement();
		BatchBindStep batch = dsl.batch(insert);
		for (DataErrorReportItem item : items) {
			Integer id = item.getId();
			if ( id == null ) {
				id = nextId++;
				item.setId(id);
			}
			Object[] values = dsl.extractValues(item);
			batch.bind(values);
			maxId = Math.max(maxId, id);
		}
		batch.execute();
		dsl.restartSequence(maxId + 1);
	}
	
	@Override
	protected JooqDSLContext dsl() {
		throw new UnsupportedOperationException();
	}
	
	protected JooqDSLContext dsl(DataErrorReport report) {
		return new JooqDSLContext(getConnection(), report);
	}
	
	protected static class JooqDSLContext extends PersistedObjectMappingDSLContext<DataErrorReportItem> {

		private static final long serialVersionUID = 1L;
		
		private DataErrorReport report;
		
		public JooqDSLContext(Connection connection, DataErrorReport report) {
			super(connection, OFC_DATA_ERROR_REPORT_ITEM.ID, OFC_DATA_ERROR_REPORT_ITEM_ID_SEQ, DataErrorReportItem.class);
			this.report = report;
		}
		
		@Override
		protected DataErrorReportItem newEntity() {
			return new DataErrorReportItem(report);
		}
		
		@Override
		protected void fromRecord(Record r, DataErrorReportItem o) {
			super.fromRecord(r, o);
			o.setNodeIndex(r.getValue(OFC_DATA_ERROR_REPORT_ITEM.NODE_INDEX));
			o.setParentEntityId(r.getValue(OFC_DATA_ERROR_REPORT_ITEM.PARENT_ENTITY_ID));
			o.setRecordId(r.getValue(OFC_DATA_ERROR_REPORT_ITEM.RECORD_ID));
			o.setStatus(Status.fromCode(r.getValue(OFC_DATA_ERROR_REPORT_ITEM.STATUS).charAt(0)));
			o.setValue(r.getValue(OFC_DATA_ERROR_REPORT_ITEM.VALUE));
		}
		
		@Override
		protected void fromObject(DataErrorReportItem o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_ERROR_REPORT_ITEM.NODE_INDEX, o.getNodeIndex());
			q.addValue(OFC_DATA_ERROR_REPORT_ITEM.PARENT_ENTITY_ID, o.getParentEntityId());
			q.addValue(OFC_DATA_ERROR_REPORT_ITEM.RECORD_ID, o.getRecordId());
			q.addValue(OFC_DATA_ERROR_REPORT_ITEM.REPORT_ID, o.getReport().getId());
			q.addValue(OFC_DATA_ERROR_REPORT_ITEM.STATUS, String.valueOf(o.getStatus().getCode()));
		}
		
		public Insert<OfcDataErrorReportItemRecord> createInsertStatement() {
			Object[] valuesPlaceholders = new String[FIELDS.length];
			Arrays.fill(valuesPlaceholders, "?");
			return insertInto(OFC_DATA_ERROR_REPORT_ITEM, FIELDS).values(valuesPlaceholders);
		}
		
		public Object[] extractValues(DataErrorReportItem item) {
			return new Object[] {
				item.getId(),
				item.getNodeIndex(),
				item.getParentEntityId(),
				item.getRecordId(),
				item.getReport().getId(),
				item.getStatus(),
				item.getValue()
			};
		}

	}
}
