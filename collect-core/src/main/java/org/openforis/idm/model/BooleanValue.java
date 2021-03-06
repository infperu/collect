package org.openforis.idm.model;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author G. Miceli
 *
 */
public final class BooleanValue implements Value {

	private Boolean value;
	
	public BooleanValue(Boolean value) {
		this.value = value;
	}
	
	public BooleanValue(String string) {
		if ( StringUtils.isBlank(string) ) {
			this.value = null;
		} else {
			this.value = Boolean.parseBoolean(string);
		}
	}

	public Boolean getValue() {
		return value;
	}
	
	public int compareTo(Value o) {
		if ( o instanceof BooleanValue ) {
			return ObjectUtils.compare(value, ((BooleanValue) o).value);
		} else {
			throw new IllegalArgumentException("Cannot compare boolean value with " + o.getClass());
		}
	}
	
	@Override
	public String toString() {
		return value == null ? null: value.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BooleanValue other = (BooleanValue) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
