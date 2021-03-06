/**
 * 
 */
package org.openforis.idm.model;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.path.Path;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 * @author D. Wiell
 */
public class Record {

	private Map<Integer, Node<? extends NodeDefinition>> nodesByInternalId;
	private Survey survey;

	private Integer id;
	private ModelVersion modelVersion;
	private int nextId;
	private Entity rootEntity;

	boolean toBeUpdated; //if true, enables validation dependency graph
	NodeDependencyGraph calculatedAttributeDependencies;
	RelevanceDependencyGraph relevanceDependencies;
	RequirenessDependencyGraph requirenessDependencies;
	ValidationDependencyGraph validationDependencies;

	public Record(Survey survey, String version) {
		this(survey, version, true);
	}
	
	public Record(Survey survey, String version, boolean toBeUpdated) {
		if (survey == null) {
			throw new IllegalArgumentException("Survey required");
		}
		this.survey = survey;
		if (version == null) {
			if (!survey.getVersions().isEmpty()) {
				throw new IllegalArgumentException("Invalid version '"
						+ version + '"');
			}
		} else {
			this.modelVersion = survey.getVersion(version);
			if (modelVersion == null) {
				throw new IllegalArgumentException("Version not specified");
			}
		}
		this.toBeUpdated = toBeUpdated;
		reset();
	}

	public Entity createRootEntity(String name) {
		Schema schema = survey.getSchema();
		EntityDefinition def = schema.getRootEntityDefinition(name);
		return createRootEntity(def);
	}

	public Entity createRootEntity(int id) {
		Schema schema = survey.getSchema();
		EntityDefinition def = schema.getRootEntityDefinition(id);
		return createRootEntity(def);
	}

	protected Entity createRootEntity(EntityDefinition def) {
		if (rootEntity != null) {
			throw new IllegalStateException(
					"Record already has an associated root entity");
		}
		setRootEntity((Entity) def.createNode());
		return rootEntity;
	}

	public void replaceRootEntity(Entity rootEntity) {
		reset();
		setRootEntity(rootEntity);
	}

	private void setRootEntity(Entity entity) {
		this.rootEntity = entity;
		put(rootEntity);
	}

	protected void reset() {
		this.nodesByInternalId = new HashMap<Integer, Node<? extends NodeDefinition>>();
		this.nextId = 1;
		resetValidationDependencies();
	}

	protected void resetValidationDependencies() {
		this.calculatedAttributeDependencies = new CalculatedAttributeDependencyGraph(survey);
		this.relevanceDependencies = new RelevanceDependencyGraph(survey);
		this.requirenessDependencies = new RequirenessDependencyGraph(survey);
		this.validationDependencies = new ValidationDependencyGraph(survey);
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public SurveyContext getSurveyContext() {
		return survey.getContext();
	}

	public Survey getSurvey() {
		return this.survey;
	}

	public Entity getRootEntity() {
		return this.rootEntity;
	}

	public ModelVersion getVersion() {
		return this.modelVersion;
	}

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		sw.append("id: ").append(String.valueOf(id)).append("\n");
		rootEntity.write(sw, 0);
		return sw.toString();
	}

	public Node<?> getNodeByInternalId(int id) {
		return this.nodesByInternalId.get(id);
	}

	public Node<?> findNodeByPath(String path) {
		List<Node<?>> nodes = findNodesByPath(path);
		if ( nodes.size() == 0 ) {
			return null;
		} else if ( nodes.size() == 1 ) {
			return nodes.get(0);
		} else {
			throw new IllegalArgumentException(
					"Multiple nodes found for path: " + path);
		}
	}

	public List<Node<?>> findNodesByPath(String path) {
		Path p = Path.parse(path);
		List<Node<?>> result = p.evaluate(this);
		return result;
	}
	
	/**
	 * Clear all node states
	 * 
	 * @param record
	 */
	public void clearNodeStates() {
		rootEntity.traverse(new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				if (node instanceof Attribute) {
					Attribute<?, ?> attribute = (Attribute<?, ?>) node;
					attribute.clearFieldStates();
				} else if (node instanceof Entity) {
					Entity entity = (Entity) node;
					entity.clearChildStates();
				}
			}
		});
	}

	void put(Node<?> node) {
		initialize(node);
		if ( toBeUpdated ) {
			initDependecyGraph(node);
		}
	}

	protected void initDependecyGraph(Node<?> node) {
		calculatedAttributeDependencies.add(node);
		relevanceDependencies.add(node);
		requirenessDependencies.add(node);
		validationDependencies.add(node);

		if (node instanceof Entity) {
			for (Node<?> child : ((Entity) node).getChildren()) {
				initDependecyGraph(child);
			}
		}
	}

	protected void initialize(Node<?> node) {
		int id = this.nextId();
		node.internalId = id;
		node.setRecord(this);

		nodesByInternalId.put(id, node);

		if (node instanceof Entity) {
			for (Node<?> child : ((Entity) node).getChildren()) {
				initialize(child);
			}
		}
	}

	protected void remove(Node<?> node) {
		node.parent = null;
		node.setRecord(null);
		nodesByInternalId.remove(node.internalId);

		calculatedAttributeDependencies.remove(node);
		relevanceDependencies.remove(node);
		requirenessDependencies.remove(node);
		validationDependencies.remove(node);
	}

	int nextId() {
		return nextId++;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Attribute<?, ?>> determineCalculatedAttributes(Node<?> node) {
		List dependenciesFor = calculatedAttributeDependencies.dependenciesFor(node);
		return dependenciesFor;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Attribute<?, ?>> determineCalculatedAttributes(Set<Node<?>> nodes) {
		List dependenciesFor = calculatedAttributeDependencies.dependenciesFor(nodes);
		return dependenciesFor;
	}

	public List<NodePointer> determineConstantRelevancePointers(Node<?> node) {
		final Set<NodePointer> result = new LinkedHashSet<NodePointer>();
		Stack<Node<?>> stack = new Stack<Node<?>>();
		stack.push(node);
		while(!stack.isEmpty()) {
			Node<?> n = stack.pop();
			NodeDefinition def = n.getDefinition();
			if ( StringUtils.isNotBlank(def.getRelevantExpression()) && survey.getRelevanceSources(def).isEmpty() ) {
				result.add(new NodePointer(n));
			}
			if(n instanceof Entity) {
				stack.addAll(((Entity) n).getChildren());
			}
		}
		return new ArrayList<NodePointer>(result);
	}
	
	public List<NodePointer> determineRelevanceDependentNodes(Collection<Node<?>> nodes) {
		List<NodePointer> result = relevanceDependencies.dependenciesFor(nodes);
		return result;
	}

	public Collection<NodePointer> determineRequirenessDependentNodes(Collection<NodePointer> nodePointers) {
		Collection<NodePointer> result = requirenessDependencies.dependenciesForNodePointers(nodePointers);
		return result;
	}

	public Set<Attribute<?, ?>> determineValidationDependentNodes(Collection<Node<?>> nodes) {
		Set<Attribute<?, ?>> result = validationDependencies.dependentAttributes(nodes);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((modelVersion == null) ? 0 : modelVersion.hashCode());
		result = prime * result
				+ ((rootEntity == null) ? 0 : rootEntity.hashCode());
		result = prime * result + ((survey == null) ? 0 : survey.hashCode());
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
		Record other = (Record) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (modelVersion == null) {
			if (other.modelVersion != null)
				return false;
		} else if (!modelVersion.equals(other.modelVersion))
			return false;
		if (rootEntity == null) {
			if (other.rootEntity != null)
				return false;
		} else if (!rootEntity.equals(other.rootEntity))
			return false;
		if (survey == null) {
			if (other.survey != null)
				return false;
		} else if (!survey.equals(other.survey))
			return false;
		return true;
	}

}
