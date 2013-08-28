package aegis.site.wiq.ecn;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;

import aegis.origin.Aegis;
import athena.origin.AthenaOperation;
import athena.origin.AthenaTCPanel;
import athena.service.ComponentService;
import athena.service.DatabaseService;

import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.VerticalLayout;

public class ECNPanel extends AthenaTCPanel {
	private JPanel EffectivityPanel;

	public ECNPanel(Aegis aegis, TCComponentItem item) {
		super(aegis, item);
	}

	public void deploy() {
		mainPanel.setLayout(new VerticalLayout());
		mainPanel.add("top.bind", createInformationPanel());
		mainPanel.add("top.bind", createChangePanel());
		mainPanel.add("top.bind", createEffectivityPanel());
	}

	private JPanel createInformationPanel() {
		JPanel panel = new JPanel(new PropertyLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("ECNInformation")));
		ComponentService.deployComponent(hashtable, panel, new String[] {"a2DevelopType", "a2Inventory", "a2ChangeReason", "a2PartClassification_", "a2ChangeType", "a2ApplyDate"});
		return panel;
	}

	private JPanel createChangePanel() {
		JPanel panel = new JPanel(new PropertyLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("ChangeInformation")));
		ComponentService.deployComponent(hashtable, panel, new String[] {"a2ChangeBefore", "a2ChangeAfter"});
		return panel;
	}

	private JPanel createEffectivityPanel() {
		EffectivityPanel = new JPanel(new PropertyLayout());
		EffectivityPanel.setBorder(BorderFactory.createTitledBorder(getString("Effectivity")));
		ComponentService.deployComponent(hashtable, EffectivityPanel, new String[] {"a2ApplyWhether", "a2RealApplyDate"});
		return EffectivityPanel;
	}

	public void setComponents() {
		JTextField a2PartClassification = (JTextField)getComponent("a2PartClassification");
		String value = DatabaseService.getValuesDescription("PartClassification", a2PartClassification.getText());
		((JTextField)getComponent("a2PartClassification_")).setText(value);
	}

	public boolean isEditable() {
		try {
			String userGroup = session.getUser().getLoginGroup().getLocalizedFullName();
			if (!userGroup.equals("EC_ReviewGroup")) {
				return false;
			}
			if (!item.getProperty("process_stage_list").equals("")) {
				TCComponentTask task = item.getCurrentJob().getRootTask();
				if (!task.getName().equals("ECN")) {
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
			ComponentService.setPanelEnable(EffectivityPanel, false);
		}
	}

	protected AthenaOperation createOperation() {
		return new ECNUpdateOperation(athena, item, hashtable, columns);
	}
}