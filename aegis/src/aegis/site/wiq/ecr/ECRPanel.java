package aegis.site.wiq.ecr;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import aegis.origin.Aegis;
import athena.origin.AthenaOperation;
import athena.origin.AthenaTCPanel;
import athena.service.ComponentService;

import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.VerticalLayout;

public class ECRPanel extends AthenaTCPanel {
	private JPanel reviewPanel;

	public ECRPanel(Aegis aegis, TCComponentItem item) {
		super(aegis, item);
	}

	public void deploy() {
		mainPanel.setLayout(new VerticalLayout());
		mainPanel.add("top.bind", createECRPanel());
		mainPanel.add("top.bind", createReviewPanel());
	}

	private JPanel createECRPanel() {
		JPanel panel = new JPanel(new PropertyLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("ECRInformation")));
		ComponentService.deployComponent(hashtable, panel, new String[] {"a2ChangeReason", "a2ApplyDate", "a2Problem", "a2ChangeRequest"});
		return panel;
	}

	private JPanel createReviewPanel() {
		reviewPanel = new JPanel(new PropertyLayout());
		reviewPanel.setBorder(BorderFactory.createTitledBorder(getString("ECRReview")));
		ComponentService.deployComponent(hashtable, reviewPanel, new String[] {"a2ReviewResult", "a2ECODate", "a2ReviewComments"});
		return reviewPanel;
	}

	public boolean isEditable() {
		try {
			String userGroup = session.getUser().getLoginGroup().getLocalizedFullName();
			if (!userGroup.equals("EC_ReviewGroup")) {
				return false;
			}
			if (!item.getProperty("process_stage_list").equals("")) {
				TCComponentTask task = item.getCurrentJob().getRootTask();
				if (!task.getName().equals("ECR")) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void postAction() {
		if (!isEditable()) {
			ComponentService.setPanelEnable(reviewPanel, false);
		}
	}

	protected AthenaOperation createOperation() {
		return new ECRUpdateOperation(athena, item, hashtable, columns);
	}
}