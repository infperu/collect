package org.openforis.collect.remoting.service.dataImport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportSummaryItem {

	private int entryId;
	private CollectRecord record;
	private CollectRecord conflictingRecord;
	private List<Step> steps;
	/**
	 * Map containing the warnings for each step (if any) 
	 */
	private Map<Step, List<String>> warnings;

	public DataImportSummaryItem(int entryId, CollectRecord record, List<Step> steps) {
		this.entryId = entryId;
		this.record = record;
		this.steps = steps;
		this.warnings = new HashMap<Step, List<String>>();
	}
	
	public DataImportSummaryItem(int entryId, CollectRecord record, List<Step> steps, CollectRecord conflictingRecord) {
		this(entryId, record, steps);
		this.conflictingRecord = conflictingRecord;
	}

	public int getEntryId() {
		return entryId;
	}

	public void setEntryId(int entryId) {
		this.entryId = entryId;
	}

	public CollectRecord getRecord() {
		return record;
	}

	public void setRecord(CollectRecord record) {
		this.record = record;
	}

	public CollectRecord getConflictingRecord() {
		return conflictingRecord;
	}

	public void setConflictingRecord(CollectRecord conflictingRecord) {
		this.conflictingRecord = conflictingRecord;
	}

	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

	public Map<Step, List<String>> getWarnings() {
		return warnings;
	}

	public void setWarnings(Map<Step, List<String>> warnings) {
		this.warnings = warnings;
	}
}
