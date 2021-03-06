/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.IntegerRange;
import org.openforis.idm.model.IntegerRangeAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NumericRange;
import org.openforis.idm.model.RealRange;
import org.openforis.idm.model.RealRangeAttribute;
import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class RangeAttributeDefinition extends NumericAttributeDefinition {

	private static final long serialVersionUID = 1L;
	
	public static final String FROM_FIELD = "from";
	public static final String TO_FIELD = "to";

	private final FieldDefinition<Integer> integerFromField = new FieldDefinition<Integer>(FROM_FIELD, "f", FROM_FIELD, Integer.class, this);
	private final FieldDefinition<Integer> integerToField = new FieldDefinition<Integer>(TO_FIELD, "t", TO_FIELD, Integer.class, this);
	private final FieldDefinition<Double> realFromField = new FieldDefinition<Double>(FROM_FIELD, "f", FROM_FIELD, Double.class, this);
	private final FieldDefinition<Double> realToField = new FieldDefinition<Double>(TO_FIELD, "t", TO_FIELD, Double.class, this);
	private final FieldDefinition<String> unitNameField = new FieldDefinition<String>(UNIT_NAME_FIELD, "u_name", "unit", String.class, this);;
	private final FieldDefinition<Integer> unitIdField = new FieldDefinition<Integer>(UNIT_FIELD, "u", "unit_id", Integer.class, this);;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private final List integerTypeFieldDefinitions = Collections.unmodifiableList(Arrays.asList(integerFromField, integerToField, unitNameField, unitIdField));
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private final List realTypeFieldDefinitions = Collections.unmodifiableList(Arrays.asList(realFromField, realToField, unitNameField, unitIdField));
	
	RangeAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	@Override
	public Node<?> createNode() {
		Type effectiveType = getType();
		switch (effectiveType) {
		case INTEGER:
			return new IntegerRangeAttribute(this);
		case REAL:
			return new RealRangeAttribute(this);
		default:
			throw new UnsupportedOperationException("Unknown type");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public NumericRange<? extends Number> createValue(String string) {
		if ( StringUtils.isBlank(string) ) {
			return null;
		} 
		Unit unit = getDefaultUnit();
		if (isInteger()) {
			return IntegerRange.parseIntegerRange(string, unit);
		} else if (isReal()) {
			return RealRange.parseRealRange(string, unit);
		}
		throw new RuntimeException("Invalid range type " + getType());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<FieldDefinition<?>> getFieldDefinitions() {
		switch (getType()) {
		case INTEGER:
			return integerTypeFieldDefinitions;
		case REAL:
			return realTypeFieldDefinitions;
		default:
			throw new UnsupportedOperationException("Unknown type");
		}
	}
	
	@Override
	public FieldDefinition<?> getFieldDefinition(String name) {
		for (FieldDefinition<?> def : getFieldDefinitions()) {
			if(def.getName().equals(name)) {
				return def;
			}
		}
		return null;
	}
	
	@Override
	public Class<? extends Value> getValueType() {
		Type type = getType();
		switch (type) {
		case INTEGER:
			return IntegerRange.class;
		case REAL:
			return RealRange.class;
		default:
			throw new UnsupportedOperationException("Unknown type");
		}
	}
	
	@Override
	public String getMainFieldName() {
		throw new IllegalArgumentException("Main field not defined");
	}
	
	@Override
	protected FieldDefinitionMap getFieldDefinitionMap() {
		throw new UnsupportedOperationException();
	}
	
}
