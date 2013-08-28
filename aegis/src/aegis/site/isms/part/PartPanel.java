package aegis.site.isms.part;

import aegis.origin.Aegis;
import athena.origin.AthenaOperation;
import athena.origin.AthenaTCPanel;
import athena.service.ComponentService;

import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.PropertyLayout;

public class PartPanel extends AthenaTCPanel {
	public PartPanel(Aegis aegis, TCComponentItem item) {
		super(aegis, item);
	}

	public PartPanel(Aegis aegis, TCComponentItemRevision revision) {
		super(aegis, revision);
	}

	public void deploy() {
		mainPanel.setLayout(new PropertyLayout());
		ComponentService.deployComponent(hashtable, mainPanel, new String[] {"item_id", "object_name", "R.object_desc"});
	}

	protected AthenaOperation createOperation() {
		return new PartUpdateOperation(athena, revision, hashtable, columns);
	}
}