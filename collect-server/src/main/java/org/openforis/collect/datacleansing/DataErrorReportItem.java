package org.openforis.collect.datacleansing;

import org.openforis.collect.datacleansing.json.JSONValueParser;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.PersistedObject;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Value;

/**
 * 
 * @author A. Modragon
 *
 */
public class DataErrorReportItem extends PersistedObject {
	
	public enum Status {
		PENDING('p'),
		FIXED('f');
		
		public static Status fromCode(char code) {
			for (Status status : values()) {
				if (status.code == code) {
					return status;
				}
			}
			return null;
		}
		
		private char code;

		Status(char code) {
			this.code = code;
		}
		
		public char getCode() {
			return code;
		}
		
	}
	
	private DataErrorReport report;
	private int recordId;
	private int parentEntityId;
	private int nodeIndex;
	private String value;
	private Status status;

	public DataErrorReportItem(DataErrorReport report) {
		super();
		this.report = report;
	}
	
	public AttributeDefinition getAttributeDefinition() {
		DataQuery query = report.getQuery();
		Schema schema = query.getSchema();
		AttributeDefinition def = (AttributeDefinition) schema.getDefinitionById(query.getAttributeDefinitionId());
		return def;
	}
	
	public Value extractAttributeValue() {
		AttributeDefinition def = getAttributeDefinition();
		Value val = new JSONValueParser().parseValue(def, value);
		return val;
	}

	public DataErrorReport getReport() {
		return report;
	}

	public int getRecordId() {
		return recordId;
	}
	
	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	public int getParentEntityId() {
		return parentEntityId;
	}

	public void setParentEntityId(int parentEntityId) {
		this.parentEntityId = parentEntityId;
	}

	public int getNodeIndex() {
		return nodeIndex;
	}

	public void setNodeIndex(int nodeIndex) {
		this.nodeIndex = nodeIndex;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}