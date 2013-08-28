package aegis.site.wiq.search;

import java.awt.event.MouseEvent;

import aegis.origin.Aegis;
import athena.origin.search.AthenaSearchTCTableDialog;

import com.teamcenter.rac.aif.commands.open.OpenCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;

public class SearchECRDialog extends AthenaSearchTCTableDialog {
	public SearchECRDialog(Aegis aegis) {
		super(aegis);
	}

	protected void open() {
		if (table.getSelectedRow() == -1) {
			return;
		}
		String id = (String)table.getValueAt(table.getSelectedRow(), 0);
		try {
			TCComponent[] components = session.search("Item ID", new String[] {textService.getTextValue("ItemID")}, new String[] {id});
			if (components.length == 0) {
				return;
			}
			OpenCommand opencommand = new OpenCommand(AIFUtility.getActiveDesktop(), components[0]);
			opencommand.executeModeless();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void mouseDoubleClicked(MouseEvent mouseEvent) {
		if (mouseEvent.getSource().equals(table) && table.getSelectedRow() >= 0) {
			open();
		}
	}
}
