package org.openforis.collect.designer.composer;

import org.openforis.collect.designer.viewmodel.ProcessStatusPopUpVM;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.BindUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;

/**
 * 
 * @author S. Ricci
 *
 */
@Deprecated
public class ProcessStatusPopUpComposer extends BindComposer<Component> {

	private static final long serialVersionUID = 1L;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		Selectors.wireEventListeners(comp, this);
	}
	
	@Listen("onTimer=#processStatusTimer")
	public void onSurveysListUpdateTimeout(Event event) throws InterruptedException {
		BindUtils.postGlobalCommand(null, null, ProcessStatusPopUpVM.UPDATE_PROGRESS_COMMAND, null);
	}
	
}