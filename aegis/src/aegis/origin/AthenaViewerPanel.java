package aegis.origin;

import java.awt.Color;

import aegis.Activator;
import athena.origin.AthenaPanel;
import athena.service.ComponentService;
import athena.service.ResourceService;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.common.tcviewer.TCComponentViewerInput;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.VerticalLayout;
import com.teamcenter.rac.util.viewer.AbstractViewPanel;
import com.teamcenter.rac.util.viewer.ViewerEvent;

public class AthenaViewerPanel extends AbstractViewPanel {
	private Aegis aegis;
	private TCComponent component;

	public AthenaViewerPanel() {
		super();
		aegis = Activator.getAegis();
		setLayout(new VerticalLayout());
	}

	public void setInput(Object object) {
		TCComponentViewerInput input = (TCComponentViewerInput)AdapterUtil.getAdapter(object, com.teamcenter.rac.common.tcviewer.TCComponentViewerInput.class);
		if (input != null) {
			component = (TCComponent)input.getViewableObj();
			setBusy(true);
			LoadOperation loadoperation = new LoadOperation(component);
			loadoperation.addOperationListener(new InterfaceAIFOperationListener() {
				public void startOperation(String s) {
				}

				public void endOperation() {
					setBusy(false);
				}
			});
			component.getSession().queueOperation(loadoperation);
		} else {
			ViewerEvent viewerevent = new ViewerEvent(this, "com.teamcenter.rac.util.viewer.NoViewDataFound");
			viewerevent.queueEvent();
		}
	}

	public class LoadOperation extends AbstractAIFOperation {
		private TCComponent component;

		public LoadOperation(TCComponent component) {
			super();
			this.component = component;
		}

		public void executeOperation() {
			component.getSession().setStatus(ResourceService.getString("Athena", "LoadingViewData"));
			try {
				AthenaPanel panel = (AthenaPanel)aegis.newInstance(ResourceService.getString(component.getType(), "Viewer.Class"), new Object[] {component});
				aegis.createPanel(panel);
				removeAll();
				setBackground(Color.WHITE);
				add("top.nobind.left", panel);
				if (panel.isEditable()) {
					add("bottom.bind", panel.getButtonPanel());
					add("bottom.bind", new Separator());
				}
				add("bottom.bind", panel.getStatusPanel());
				ComponentService.setBackground(AthenaViewerPanel.this);
				setVisible(true);
				validate();
				repaint();
				// ViewerEvent viewerevent = new ViewerEvent(AthenaViewerPanel.this, "com.teamcenter.rac.util.viewer.ReloadView");
				// viewerevent.queueEvent();
				// scrollPane.setViewportView((Component)Activator.newInstance(new String(component.getType() + ".Viewer"), new Object[] {component}));
				// repaint();
				// getViewer().setViewPanel(AthenaViewer.this);
			} catch (Exception e) {
				e.printStackTrace();
				// ViewerEvent viewerevent1 = new ViewerEvent(AthenaViewerPanel.this, "com.teamcenter.rac.util.viewer.NoViewDataFound");
				// viewerevent1.queueEvent();
			}
			component.getSession().setStatus(null);
		}
	}
}