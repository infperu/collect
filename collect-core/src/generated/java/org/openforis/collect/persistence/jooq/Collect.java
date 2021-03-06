/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.4.2" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Collect extends org.jooq.impl.SchemaImpl {

	private static final long serialVersionUID = 29635315;

	/**
	 * The singleton instance of <code>collect</code>
	 */
	public static final Collect COLLECT = new Collect();

	/**
	 * No further instances allowed
	 */
	private Collect() {
		super("collect");
	}

	@Override
	public final java.util.List<org.jooq.Sequence<?>> getSequences() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getSequences0());
		return result;
	}

	private final java.util.List<org.jooq.Sequence<?>> getSequences0() {
		return java.util.Arrays.<org.jooq.Sequence<?>>asList(
			org.openforis.collect.persistence.jooq.Sequences.OFC_CODE_LIST_ID_SEQ,
			org.openforis.collect.persistence.jooq.Sequences.OFC_RECORD_ID_SEQ,
			org.openforis.collect.persistence.jooq.Sequences.OFC_SAMPLING_DESIGN_ID_SEQ,
			org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_ID_SEQ,
			org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_WORK_ID_SEQ,
			org.openforis.collect.persistence.jooq.Sequences.OFC_TAXON_ID_SEQ,
			org.openforis.collect.persistence.jooq.Sequences.OFC_TAXONOMY_ID_SEQ,
			org.openforis.collect.persistence.jooq.Sequences.OFC_TAXON_VERNACULAR_NAME_ID_SEQ,
			org.openforis.collect.persistence.jooq.Sequences.OFC_USER_ID_SEQ,
			org.openforis.collect.persistence.jooq.Sequences.OFC_USER_ROLE_ID_SEQ);
	}

	@Override
	public final java.util.List<org.jooq.Table<?>> getTables() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getTables0());
		return result;
	}

	private final java.util.List<org.jooq.Table<?>> getTables0() {
		return java.util.Arrays.<org.jooq.Table<?>>asList(
			org.openforis.collect.persistence.jooq.tables.OfcApplicationInfo.OFC_APPLICATION_INFO,
			org.openforis.collect.persistence.jooq.tables.OfcCodeList.OFC_CODE_LIST,
			org.openforis.collect.persistence.jooq.tables.OfcConfig.OFC_CONFIG,
			org.openforis.collect.persistence.jooq.tables.OfcLogo.OFC_LOGO,
			org.openforis.collect.persistence.jooq.tables.OfcRecord.OFC_RECORD,
			org.openforis.collect.persistence.jooq.tables.OfcSamplingDesign.OFC_SAMPLING_DESIGN,
			org.openforis.collect.persistence.jooq.tables.OfcSurvey.OFC_SURVEY,
			org.openforis.collect.persistence.jooq.tables.OfcSurveyWork.OFC_SURVEY_WORK,
			org.openforis.collect.persistence.jooq.tables.OfcTaxon.OFC_TAXON,
			org.openforis.collect.persistence.jooq.tables.OfcTaxonomy.OFC_TAXONOMY,
			org.openforis.collect.persistence.jooq.tables.OfcTaxonVernacularName.OFC_TAXON_VERNACULAR_NAME,
			org.openforis.collect.persistence.jooq.tables.OfcUser.OFC_USER,
			org.openforis.collect.persistence.jooq.tables.OfcUserRole.OFC_USER_ROLE);
	}
}
