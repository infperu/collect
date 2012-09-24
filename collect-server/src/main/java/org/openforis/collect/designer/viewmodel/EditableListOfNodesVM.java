package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.ui.UIConfiguration;
import org.openforis.collect.model.ui.UIConfiguration.Layout;
import org.openforis.collect.model.ui.UITab;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;

/**
 * 
 * @author S. Ricci
 *
 */
public class EditableListOfNodesVM extends BaseVM {

	private UITab tab;

	@Init
	public void init(@ExecutionArgParam("tab") UITab tab) {
		this.tab = tab;
	}
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
		 Selectors.wireEventListeners(view, this);
	}
	
	@Listen("onDrop = listbox#listOfNodesListbox")
	@NotifyChange({"nodesPerTab"})
	public void listOfNodesDropHandler(DropEvent evt) {
		Component dragged = evt.getDragged();
		if ( dragged instanceof Treeitem ) {
			Treeitem draggedTreeItem = (Treeitem) dragged;
			TreeNode<?> value = draggedTreeItem.getValue();
			Object data = value.getData();
			if ( data instanceof NodeDefinition ) {
				NodeDefinition nodeDefn = (NodeDefinition) data;
				UIConfiguration uiConf = getUIConfiguration();
				if ( uiConf.isAssignableTo(nodeDefn, tab) ) {
					uiConf.associateWithTab(nodeDefn, tab);
				} else {
					MessageUtil.showWarning("survey.layout.cannot_add_node_to_tab");
				}
			}
		}
	}
	
	public boolean isEntity(NodeDefinition nodeDefn) {
		return nodeDefn instanceof EntityDefinition;
	}
	
	@Command
	@NotifyChange({"nodesPerTab"})
	public void removeTabAssociation(@BindingParam("node") NodeDefinition node) {
		UIConfiguration uiConf = getUIConfiguration();
		uiConf.removeTabAssociation(node);
	}
	
	@Command
	@NotifyChange({"nodesPerTab"})
	public void setLayout(@BindingParam("type") String type, @BindingParam("node") NodeDefinition node) {
		UIConfiguration uiConf = getUIConfiguration();
		Layout layout = Layout.valueOf(type);
		uiConf.setLayout(node, layout);
	}
	
	public UITab getTab() {
		return tab;
	}
	
	public String getTemplateName(NodeDefinition nodeDefn) {
		if ( nodeDefn instanceof EntityDefinition ) {
			if ( nodeDefn.isMultiple() ) {
				UIConfiguration uiConf = getUIConfiguration();
				Layout layout = uiConf.getLayout((EntityDefinition) nodeDefn);
				switch ( layout ) {
				case FORM:
					return "multiple_entity_form";
				default:
					return "multiple_entity_table";
				}
			} else {
				return "entity";
			}
		} else {
			return "attribute";
		}
	}
	
	public boolean hasLayout(EntityDefinition entityDefn, String layout) {
		UIConfiguration uiConf = getUIConfiguration();
		Layout nodeLayout = uiConf.getLayout(entityDefn);
		return nodeLayout.name().equals(layout);
	}
	
	public List<NodeDefinition> getNodesPerTab() {
		UIConfiguration uiConf = getUIConfiguration();
		List<NodeDefinition> result = uiConf.getNodesPerTab(tab, false);
		return result;
	}
	
	public List<NodeDefinition> getChildDefinitions(EntityDefinition entityDefn) {
		List<NodeDefinition> childDefinitions = entityDefn.getChildDefinitions();
		return childDefinitions;
	}
	
	public List<NodeDefinition> getChildDefinitionsInTab(EntityDefinition entityDefn) {
		UIConfiguration uiConf = getUIConfiguration();
		List<NodeDefinition> childDefinitions = entityDefn.getChildDefinitions();
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		for (NodeDefinition nodeDefn : childDefinitions) {
			UITab nodeTab = uiConf.getTab(nodeDefn);
			if ( nodeTab == tab ) {
				result.add(nodeDefn);
			}
		}
		return result;
	}
	
	protected CollectSurvey getSurvey() {
		SessionStatus sessionStatus = super.getSessionStatus();
		CollectSurvey survey = sessionStatus.getSurvey();
		return survey;
	}
	
	protected UIConfiguration getUIConfiguration() {
		CollectSurvey survey = getSurvey();
		UIConfiguration uiConf = survey.getUIConfiguration();
		uiConf.setSurvey(survey);
		return uiConf;
	}
	
	
}