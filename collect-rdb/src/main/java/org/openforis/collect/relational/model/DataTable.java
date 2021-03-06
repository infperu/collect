package org.openforis.collect.relational.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.relational.CollectRdbException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Node;
import org.openforis.idm.path.Path;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataTable extends AbstractTable<Node<?>> {

	private NodeDefinition definition;
	private Path relativePath;
	private DataTable parent;
	private List<DataTable> childTables;
	private Map<Integer, CodeValueFKColumn> foreignKeyCodeColumns;
	
	DataTable(String prefix, String name, DataTable parent, NodeDefinition defn, Path relativePath) throws CollectRdbException {
		super(prefix, name);
		this.definition = defn;
		this.parent = parent;
		this.relativePath = relativePath;
		this.childTables = new ArrayList<DataTable>();
		this.foreignKeyCodeColumns = new HashMap<Integer, CodeValueFKColumn>();
	}

	@Override
	void addColumn(Column<?> column) {
		super.addColumn(column);
		if ( column instanceof CodeValueFKColumn ) {
			int attrDefnId = ((CodeValueFKColumn) column).getAttributeDefinition().getId();
			foreignKeyCodeColumns.put(attrDefnId, (CodeValueFKColumn) column);
		}
	}
	
	public CodeValueFKColumn getForeignKeyCodeColumn(CodeAttributeDefinition defn) {
		return foreignKeyCodeColumns.get(defn.getId());
	}
	
	public NodeDefinition getNodeDefinition() {
		return definition;
	}

	public void print(PrintStream out) {
		out.printf("%-43s%s\n", getName()+":", getRelativePath());
		for (Column<?> col : getColumns()) {
			String name = col.getName();
			int type = col.getType();
			Integer length = col.getLength();
			String path = "";
			if ( col instanceof DataColumn ) {
				DataColumn dcol = (DataColumn) col;
				path = dcol.getRelativePath()+"";
			}
			out.printf("\t%-35s%-8s%-8s%s\n", name, type, length==null?"":length, path);
		}
		out.flush();
	}
	
	public DataTable getParent() {
		return parent;
	}
	
	public Path getRelativePath() {
		return relativePath;
	}
	
	void addChildTable(DataTable table) {
		childTables.add(table);
	}
	
	public List<DataTable> getChildTables() {
		return childTables;
	}

	public DataPrimaryKeyColumn getPrimaryKeyColumn() {
		List<Column<?>> columns = getColumns();
		for (Column<?> c : columns) {
			if ( c instanceof DataPrimaryKeyColumn ) {
				return (DataPrimaryKeyColumn) c;
			}
		}
		return null;
	}
	
	public DataParentKeyColumn getParentKeyColumn() {
		List<Column<?>> columns = getColumns();
		for (Column<?> c : columns) {
			if ( c instanceof DataParentKeyColumn ) {
				return (DataParentKeyColumn) c;
			}
		}
		return null;
	}
	
	public List<DataColumn> getDataColumns(AttributeDefinition attributeDefinition) {
		List<DataColumn> result = new ArrayList<DataColumn>();
		int attributeDefinitionId = attributeDefinition.getId();
		for ( Column<?> column : getColumns() ) {
			if ( column instanceof DataColumn ) {
				DataColumn dataCol = (DataColumn) column;
				AttributeDefinition columnAttrDefn = dataCol.getAttributeDefinition();
				if ( ! ( dataCol instanceof CodeValueFKColumn ) && 
						columnAttrDefn.getId() == attributeDefinitionId ) {
					result.add(dataCol); 
				}
			}
		}
		return result;
	}
	
	public DataColumn getDataColumn(FieldDefinition<?> fieldDefinition) {
		AttributeDefinition attributeDefinition = (AttributeDefinition) fieldDefinition.getParentDefinition();
		List<DataColumn> attributeDataColumns = getDataColumns(attributeDefinition);
		String fieldDefinitionName = fieldDefinition.getName();
		for ( DataColumn column : attributeDataColumns ) {
			NodeDefinition columnNodeDefn = column.getNodeDefinition();
			if ( columnNodeDefn instanceof FieldDefinition && 
					columnNodeDefn.getName().equals(fieldDefinitionName)) {
				return column; 
			}
		}
		return null;
	}
}