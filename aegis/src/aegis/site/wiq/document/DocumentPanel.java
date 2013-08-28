package aegis.site.wiq.document;

import javax.swing.JTextField;

import aegis.origin.Aegis;
import athena.origin.AthenaOperation;
import athena.origin.AthenaTCPanel;
import athena.service.ComponentService;
import athena.service.DatabaseService;

import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.PropertyLayout;

public class DocumentPanel extends AthenaTCPanel {
	public DocumentPanel(Aegis aegis, TCComponentItem item) {
		super(aegis, item);
	}

	public DocumentPanel(Aegis aegis, TCComponentItemRevision revision) {
		super(aegis, revision);
	}

	public void deploy() {
		mainPanel.setLayout(new PropertyLayout());
		ComponentService.deployComponent(hashtable, mainPanel, new String[] {"R.a2DocumentType", "R.a2ProjectCode", "projectName", "object_name", "R.object_desc"});
	}

	protected AthenaOperation createOperation() {
		return new DocumentUpdateOperation(athena, session, revision, hashtable, columns);
	}
}