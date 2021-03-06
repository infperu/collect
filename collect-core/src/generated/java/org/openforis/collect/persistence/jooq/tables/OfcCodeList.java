/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.4.2" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcCodeList extends org.jooq.impl.TableImpl<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord> {

	private static final long serialVersionUID = 1719719355;

	/**
	 * The singleton instance of <code>collect.ofc_code_list</code>
	 */
	public static final org.openforis.collect.persistence.jooq.tables.OfcCodeList OFC_CODE_LIST = new org.openforis.collect.persistence.jooq.tables.OfcCodeList();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord> getRecordType() {
		return org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord.class;
	}

	/**
	 * The column <code>collect.ofc_code_list.id</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_code_list.survey_id</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.Integer> SURVEY_ID = createField("survey_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>collect.ofc_code_list.survey_work_id</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.Integer> SURVEY_WORK_ID = createField("survey_work_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>collect.ofc_code_list.code_list_id</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.Integer> CODE_LIST_ID = createField("code_list_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_code_list.item_id</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.Integer> ITEM_ID = createField("item_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_code_list.parent_id</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.Integer> PARENT_ID = createField("parent_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>collect.ofc_code_list.sort_order</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.Integer> SORT_ORDER = createField("sort_order", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_code_list.code</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.String> CODE = createField("code", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_code_list.qualifiable</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.Boolean> QUALIFIABLE = createField("qualifiable", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

	/**
	 * The column <code>collect.ofc_code_list.since_version_id</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.Integer> SINCE_VERSION_ID = createField("since_version_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>collect.ofc_code_list.deprecated_version_id</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.Integer> DEPRECATED_VERSION_ID = createField("deprecated_version_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>collect.ofc_code_list.label1</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.String> LABEL1 = createField("label1", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>collect.ofc_code_list.label2</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.String> LABEL2 = createField("label2", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>collect.ofc_code_list.label3</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.String> LABEL3 = createField("label3", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>collect.ofc_code_list.description1</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.String> DESCRIPTION1 = createField("description1", org.jooq.impl.SQLDataType.VARCHAR.length(500), this, "");

	/**
	 * The column <code>collect.ofc_code_list.description2</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.String> DESCRIPTION2 = createField("description2", org.jooq.impl.SQLDataType.VARCHAR.length(500), this, "");

	/**
	 * The column <code>collect.ofc_code_list.description3</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.String> DESCRIPTION3 = createField("description3", org.jooq.impl.SQLDataType.VARCHAR.length(500), this, "");

	/**
	 * The column <code>collect.ofc_code_list.level</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, java.lang.Integer> LEVEL = createField("level", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * Create a <code>collect.ofc_code_list</code> table reference
	 */
	public OfcCodeList() {
		this("ofc_code_list", null);
	}

	/**
	 * Create an aliased <code>collect.ofc_code_list</code> table reference
	 */
	public OfcCodeList(java.lang.String alias) {
		this(alias, org.openforis.collect.persistence.jooq.tables.OfcCodeList.OFC_CODE_LIST);
	}

	private OfcCodeList(java.lang.String alias, org.jooq.Table<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord> aliased) {
		this(alias, aliased, null);
	}

	private OfcCodeList(java.lang.String alias, org.jooq.Table<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, org.openforis.collect.persistence.jooq.Collect.COLLECT, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord> getPrimaryKey() {
		return org.openforis.collect.persistence.jooq.Keys.OFC_CODE_LIST_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord>>asList(org.openforis.collect.persistence.jooq.Keys.OFC_CODE_LIST_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord, ?>>asList(org.openforis.collect.persistence.jooq.Keys.OFC_CODE_LIST__OFC_CODE_LIST_SURVEY_FKEY, org.openforis.collect.persistence.jooq.Keys.OFC_CODE_LIST__OFC_CODE_LIST_SURVEY_WORK_FKEY, org.openforis.collect.persistence.jooq.Keys.OFC_CODE_LIST__OFC_CODE_LIST_PARENT_FKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.openforis.collect.persistence.jooq.tables.OfcCodeList as(java.lang.String alias) {
		return new org.openforis.collect.persistence.jooq.tables.OfcCodeList(alias, this);
	}

	/**
	 * Rename this table
	 */
	public org.openforis.collect.persistence.jooq.tables.OfcCodeList rename(java.lang.String name) {
		return new org.openforis.collect.persistence.jooq.tables.OfcCodeList(name, null);
	}
}
