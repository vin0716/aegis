package aegis.site.isms.search;

import aegis.origin.Aegis;
import athena.origin.AthenaDialog;
import athena.origin.search.AthenaSearchATTableDialog;

public class SearchDownloadHistoryDialog extends AthenaSearchATTableDialog {
	public SearchDownloadHistoryDialog(Aegis aegis) {
		super(aegis);
	}

	public SearchDownloadHistoryDialog(Aegis aegis, AthenaDialog dialog) {
		super(aegis, dialog);
	}
//
//	public void deploy() {
//		super.deploy();
//		ComponentService.deployComponent(hashtable, searchPanel, new String[] {"dataset_name", "item_id", "object_name", "user_id", "user_name"});
//		searchPanel.add("7.3.left", createFindTableDialog((JTextField)getComponent("user_id")));
//	}
//
//	protected void findTable(CoupledButton button) {
//		athena.launchDialog("FindUserDialog", new Object[] {this, button.getTextField(), button});
//	}
//
//	protected void arrangeResult() {
//		clearTable();
//		String[] fields = getStrings("TableColumns");
//		String[] information = new String[fields.length];
//		for (int i = 0; i < components.length; i++) {
//			TCComponentForm form = (TCComponentForm)components[i];
//			try {
//				for (int j = 0; j < information.length; j++) {
//					if (fields[j].startsWith("creation_date")) {
//						information[j] = dateFormat.format(form.getDateProperty(fields[j]));
//					} else if (fields[j].equals("user_id") || fields[j].equals("user_name")) {
//						TCComponentUser user = (TCComponentUser)form.getRelatedComponent("owning_user");
//						information[j] = user.getProperty(fields[j]);
//					} else {
//						information[j] = form.getProperty(fields[j]);
//					}
//				}
//			} catch (TCException e) {
//				e.printStackTrace();
//			}
//			model.addRow(information);
//		}
//	}
//
//	protected void open() {
//		if (table.getSelectedRow() == -1) {
//			return;
//		}
//		String id = (String)table.getValueAt(table.getSelectedRow(), 1);
//		try {
//			TCComponent[] components = session.search("Item ID", new String[] {textService.getTextValue("ItemID")}, new String[] {id});
//			if (components.length == 0) {
//				return;
//			}
//			OpenCommand opencommand = new OpenCommand(AIFUtility.getActiveDesktop(), components[0]);
//			opencommand.executeModeless();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
