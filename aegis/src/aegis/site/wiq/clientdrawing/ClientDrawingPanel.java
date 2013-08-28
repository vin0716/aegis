package aegis.site.wiq.clientdrawing;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.Document;

import aegis.origin.Aegis;
import athena.origin.AthenaOperation;
import athena.origin.AthenaTCPanel;
import athena.service.ComponentService;
import athena.service.DatabaseService;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.PropertyLayout;

public class ClientDrawingPanel extends AthenaTCPanel {
	public ClientDrawingPanel(Aegis aegis, TCComponentItem item) {
		super(aegis, item);
	}

	public ClientDrawingPanel(Aegis aegis, TCComponentItemRevision revision) {
		super(aegis, revision);
	}

	public void deploy() {
		mainPanel.setLayout(new PropertyLayout());
		ComponentService.deployComponent(hashtable, mainPanel, new String[] {"item_id", "object_name", "R.a2Client", "R.a2ProductCode", "R.a2Equipment", "R.object_desc"});
	}

	protected AthenaOperation createOperation() {
		return new ClientDrawingUpdateOperation(athena, session, revision, hashtable, columns);
	}
}