/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.openforis.idm.path.InvalidPathException;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class CodeAttributeDefinition extends AttributeDefinition implements KeyAttributeDefinition {

	private static final long serialVersionUID = 1L;
	
	public static final String CODE_FIELD = "code";
	public static final String QUALIFIER_FIELD = "qualifier";
	
	private final FieldDefinitionMap fieldDefinitionByName = new FieldDefinitionMap(
		new FieldDefinition<String>(CODE_FIELD, "c", null, String.class, this),
		new FieldDefinition<String>(QUALIFIER_FIELD, "q", "other", String.class, this)
	);
	
	private boolean key;
	private boolean allowUnlisted;
	private CodeList list;
	private CodeAttributeDefinition parentCodeAttributeDefinition;
	private String tempParentExpression;
	
	/**
	 * If true, the sort order of the associated values will be considered as a ranking
	 */
	private boolean allowValuesSorting;
	
	CodeAttributeDefinition(Survey survey, int id) {
		super(survey, id);
		this.allowValuesSorting = false;
	}
	
	@Override
	protected void init() {
		super.init();
		if ( StringUtils.isNotBlank(tempParentExpression) ) { 
			initParentCodeAttributeDefinition(tempParentExpression);
		}
		tempParentExpression = null;
	}

	@Override
	void detach() {
		super.detach();
		removeParentCodeAttributeDependencies();
	}
	
	@Override
	public boolean hasDependencies() {
		return super.hasDependencies() || hasDependentCodeAttributeDefinitions();
	}
	
	public CodeList getList() {
		return this.list;
	}

	public void setList(CodeList list) {
		if ( list == null ) {
			throw new IllegalArgumentException("Cannot add a null list");
		} else if ( list.getSurvey() == null || ! list.getSurvey().equals(this.getSurvey() )) {
			throw new IllegalArgumentException("Cannot add a list from a different survey");
		} else {
			CodeList oldList = this.list;
			if ( oldList != null && oldList.isHierarchical() && ! oldList.equals(list) ) {
				removeParentCodeAttributeDependencies();
			}
			this.list = list;
		}
	}

	private void removeParentCodeAttributeDependencies() {
		for (CodeAttributeDefinition dependant : getDependentCodeAttributeDefinitions()) {
			dependant.setParentCodeAttributeDefinition(null);
		}
	}
	
	public String getListName() {
		return list == null ? null : list.getName();
	}
	
	public void setListName(String name) {
		Survey survey = getSurvey();
		if ( survey == null ) {
			throw new DetachedNodeDefinitionException(CodeAttributeDefinition.class, Survey.class);
		}
		CodeList newList = survey.getCodeList(name);
		if ( newList == null ) {
			throw new IllegalArgumentException("Code list '"+name+"' not defined");
		}
		this.list = newList;
	}
	
	public CodeAttributeDefinition getParentCodeAttributeDefinition() {
		return parentCodeAttributeDefinition;
	}
	
	public void setParentCodeAttributeDefinition(CodeAttributeDefinition parentCodeAttributeDefinition) {
		if ( parentCodeAttributeDefinition == null ) {
			this.parentCodeAttributeDefinition = null;
		} else if ( parentCodeAttributeDefinition == this ) {
			throw new IllegalArgumentException("Parent code attribute must be different from this");
		} else if ( ! this.getList().isHierarchical() ) {
			throw new IllegalArgumentException("Cannot associate a parent code attribute: hierachycal code list required");
		} else if ( parentCodeAttributeDefinition.getList() != this.getList() ) {
			throw new IllegalArgumentException("Parent code attribute must be associated to the same code list");
		} else {
			if ( this.parentCodeAttributeDefinition != null && ! this.parentCodeAttributeDefinition.equals(parentCodeAttributeDefinition) ) {
				removeParentCodeAttributeDependencies();
			}
			this.parentCodeAttributeDefinition = parentCodeAttributeDefinition;
		}
	}
	
	public List<CodeAttributeDefinition> getAncestorCodeAttributeDefinitions() {
		List<CodeAttributeDefinition> result = new ArrayList<CodeAttributeDefinition>();
		CodeAttributeDefinition parent = getParentCodeAttributeDefinition();
		while ( parent != null ) {
			result.add(0, parent);
			parent = parent.getParentCodeAttributeDefinition();
		}
		return result;
	}
	
	public String getParentExpression() {
		if ( parentCodeAttributeDefinition == null ) {
			return null;
		} else {
			StringBuilder sb = new StringBuilder(64);
			EntityDefinition firstCommonAncestor = parentCodeAttributeDefinition.getParentEntityDefinition();
			EntityDefinition ancestor = this.getParentEntityDefinition();
			while ( ancestor != null && ancestor != firstCommonAncestor ) {
				sb.append("parent()");
				sb.append("/");
				ancestor = ancestor.getParentEntityDefinition();
			}
			if ( ancestor == firstCommonAncestor ) {
				sb.append(parentCodeAttributeDefinition.getName());
				return sb.toString();
			} else {
				throw new IllegalArgumentException("Parent code attribute definition must be associated to the same entity or to an ancestor of this");
			}
		}
	}
	
	public void setParentExpression(String parentExpression) {
		if ( getParentDefinition() == null ) {
			//detached node, postpone parent code attribute initialization
			tempParentExpression = parentExpression;
		} else if (StringUtils.isNotBlank(parentExpression) ) {
			initParentCodeAttributeDefinition(parentExpression);
		} else {
			parentCodeAttributeDefinition = null;
		}
	}

	private void initParentCodeAttributeDefinition(String parentExpression) {
		NodeDefinition parentDefinition = getParentDefinition();
		try {
			parentCodeAttributeDefinition = (CodeAttributeDefinition) parentDefinition.getDefinitionByPath(parentExpression);
		} catch (InvalidPathException e) {
			throw new IllegalStateException("Invalid parent paths should not be allowed");
		}
	}
	
	public Collection<CodeAttributeDefinition> getAssignableParentCodeAttributeDefinitions() {
		if ( list != null && list.isHierarchical() && getDependentCodeAttributeDepth() < list.getHierarchy().size() - 1 ) {
			Set<CodeAttributeDefinition> result = new HashSet<CodeAttributeDefinition>();
			EntityDefinition parent = getParentEntityDefinition();
			while ( parent != null ) {
				for (NodeDefinition nodeDefn : parent.getChildDefinitions()) {
					if ( nodeDefn instanceof CodeAttributeDefinition && nodeDefn != this ) {
						CodeAttributeDefinition codeAttrDefn = (CodeAttributeDefinition) nodeDefn;
						if ( isAssignableParentCodeAttributeDefinition(codeAttrDefn) ) {
							result.add(codeAttrDefn);
						}
					}
				}
				parent = parent.getParentEntityDefinition();
			}
			return result;
		} else {
			return Collections.emptyList();
		}
	}
	
	private boolean isAssignableParentCodeAttributeDefinition(CodeAttributeDefinition codeAttrDefn) {
		return codeAttrDefn.getList() == list && 
				! this.isAncestorCodeAttributeDefinitionOf(codeAttrDefn) && 
					list.getHierarchy().size() > codeAttrDefn.getLevelIndex() + 1;
	}
	
	/**
	 * Returns true if this is among the ancestor code attribute definitions of the specified code attribute definition
	 */
	private boolean isAncestorCodeAttributeDefinitionOf(CodeAttributeDefinition codeAttrDefn) {
		boolean isAncestor = false;
		for (CodeAttributeDefinition ancestor : codeAttrDefn.getAncestorCodeAttributeDefinitions()) {
			if ( ancestor == this ) {
				isAncestor = true;
				break;
			}
		}
		return isAncestor;
	}

	public Collection<CodeAttributeDefinition> getDependentCodeAttributeDefinitions() {
		final Set<CodeAttributeDefinition> result = new HashSet<CodeAttributeDefinition>();
		EntityDefinition rootEntity = getRootEntity();
		final int thisId = this.getId();
		rootEntity.traverse(new NodeDefinitionVisitor() {
			@Override
			public void visit(NodeDefinition node) {
				if ( node instanceof CodeAttributeDefinition ) {
					CodeAttributeDefinition codeAttrDefn = (CodeAttributeDefinition) node;
					CodeAttributeDefinition parentAttrDefn = codeAttrDefn.getParentCodeAttributeDefinition();
					if ( parentAttrDefn != null && thisId == parentAttrDefn.getId() ) {
						result.add(codeAttrDefn);
					}
				}
			}
		});
		return result;
	}
	
	public int getDependentCodeAttributeDepth() {
		int maxDepth = 0;
		Collection<CodeAttributeDefinition> nextLevelDependents = new ArrayList<CodeAttributeDefinition>(getDependentCodeAttributeDefinitions());
		while ( ! nextLevelDependents.isEmpty() ) {
			maxDepth ++;
			Collection<CodeAttributeDefinition> dependents = nextLevelDependents;
			nextLevelDependents = new ArrayList<CodeAttributeDefinition>();
			for (CodeAttributeDefinition codeAttributeDefinition : dependents) {
				nextLevelDependents.addAll(codeAttributeDefinition.getDependentCodeAttributeDefinitions());
			}
		}
		return maxDepth;
	}

	public boolean hasDependentCodeAttributeDefinitions() {
		return list != null && list.isHierarchical() && ! getDependentCodeAttributeDefinitions().isEmpty();
	}
	
	public Integer getListLevelIndex() {
		if ( list.isHierarchical() ) {
			int idx = -1;
			CodeAttributeDefinition currCode = this;
			while ( currCode != null ) {
				idx++;
				currCode = currCode.getParentCodeAttributeDefinition();
			}
			return idx;
		} else {
			return null;
		}
	}

	@Override
	public Node<?> createNode() {
		return new CodeAttribute(this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Code createValue(String string) {
		if ( StringUtils.isBlank(string) ) {
			return null;
		} else {
			return new Code(string);
		}
	}
	
	@Override
	protected FieldDefinitionMap getFieldDefinitionMap() {
		return fieldDefinitionByName;
	}
	
	@Override
	public String getMainFieldName() {
		return CODE_FIELD;
	}
	
	@Override
	public Class<? extends Value> getValueType() {
		return Code.class;
	}

	@Override
	public boolean isKey() {
		return key;
	}
	
	@Override
	public void setKey(boolean key) {
		this.key = key;
	}

	public boolean isAllowUnlisted() {
		return allowUnlisted;
	}
	
	public void setAllowUnlisted(Boolean allowUnlisted) {
		this.allowUnlisted = allowUnlisted;
	}
	
	public int getLevelPosition() {
		return getLevelIndex() + 1;
	}

	public int getLevelIndex() {
		if ( list == null || list.getHierarchy().isEmpty() ) {
			return 0;
		} else {
			int level = 0;
			CodeAttributeDefinition ptr = getParentCodeAttributeDefinition();
			while ( ptr != null ) {
				level ++;
				ptr = ptr.getParentCodeAttributeDefinition();
			}
			return level;
		}
	}

	public boolean isAllowValuesSorting() {
		return allowValuesSorting;
	}
	
	public void setAllowValuesSorting(boolean allowValuesSorting) {
		this.allowValuesSorting = allowValuesSorting;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (allowUnlisted ? 1231 : 1237);
		result = prime * result + (allowValuesSorting ? 1231 : 1237);
		result = prime * result + (key ? 1231 : 1237);
		result = prime * result + ((list == null) ? 0 : list.hashCode());
		result = prime
				* result
				+ ((parentCodeAttributeDefinition == null) ? 0
						: parentCodeAttributeDefinition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CodeAttributeDefinition other = (CodeAttributeDefinition) obj;
		if (allowUnlisted != other.allowUnlisted)
			return false;
		if (allowValuesSorting != other.allowValuesSorting)
			return false;
		if (key != other.key)
			return false;
		if (list == null) {
			if (other.list != null)
				return false;
		} else if (!list.equals(other.list))
			return false;
		if (parentCodeAttributeDefinition == null) {
			if (other.parentCodeAttributeDefinition != null)
				return false;
		} else if (!parentCodeAttributeDefinition
				.equals(other.parentCodeAttributeDefinition))
			return false;
		return true;
	}

}
