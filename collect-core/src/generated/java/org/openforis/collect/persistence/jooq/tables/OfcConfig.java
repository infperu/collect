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
public class OfcConfig extends org.jooq.impl.TableImpl<org.openforis.collect.persistence.jooq.tables.records.OfcConfigRecord> {

	private static final long serialVersionUID = -1094307752;

	/**
	 * The singleton instance of <code>collect.ofc_config</code>
	 */
	public static final org.openforis.collect.persistence.jooq.tables.OfcConfig OFC_CONFIG = new org.openforis.collect.persistence.jooq.tables.OfcConfig();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.openforis.collect.persistence.jooq.tables.records.OfcConfigRecord> getRecordType() {
		return org.openforis.collect.persistence.jooq.tables.records.OfcConfigRecord.class;
	}

	/**
	 * The column <code>collect.ofc_config.name</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcConfigRecord, java.lang.String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(25).nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_config.value</code>.
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcConfigRecord, java.lang.String> VALUE = createField("value", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * Create a <code>collect.ofc_config</code> table reference
	 */
	public OfcConfig() {
		this("ofc_config", null);
	}

	/**
	 * Create an aliased <code>collect.ofc_config</code> table reference
	 */
	public OfcConfig(java.lang.String alias) {
		this(alias, org.openforis.collect.persistence.jooq.tables.OfcConfig.OFC_CONFIG);
	}

	private OfcConfig(java.lang.String alias, org.jooq.Table<org.openforis.collect.persistence.jooq.tables.records.OfcConfigRecord> aliased) {
		this(alias, aliased, null);
	}

	private OfcConfig(java.lang.String alias, org.jooq.Table<org.openforis.collect.persistence.jooq.tables.records.OfcConfigRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, org.openforis.collect.persistence.jooq.Collect.COLLECT, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.openforis.collect.persistence.jooq.tables.records.OfcConfigRecord> getPrimaryKey() {
		return org.openforis.collect.persistence.jooq.Keys.OFC_CONFIG_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.openforis.collect.persistence.jooq.tables.records.OfcConfigRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.openforis.collect.persistence.jooq.tables.records.OfcConfigRecord>>asList(org.openforis.collect.persistence.jooq.Keys.OFC_CONFIG_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.openforis.collect.persistence.jooq.tables.OfcConfig as(java.lang.String alias) {
		return new org.openforis.collect.persistence.jooq.tables.OfcConfig(alias, this);
	}

	/**
	 * Rename this table
	 */
	public org.openforis.collect.persistence.jooq.tables.OfcConfig rename(java.lang.String name) {
		return new org.openforis.collect.persistence.jooq.tables.OfcConfig(name, null);
	}
}
