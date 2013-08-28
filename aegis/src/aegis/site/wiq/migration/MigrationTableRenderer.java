package aegis.site.wiq.migration;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MigrationTableRenderer extends DefaultTableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		String status = (String)table.getValueAt(row, table.getColumnCount() - 2);
		if (status != null) {
			if (status.equals("OK")) {
				if (isSelected) {
					setForeground(table.getSelectionForeground());
				} else {
					setForeground(table.getForeground());
				}
				setBackground(new Color(0, 0, 255));
			} else if (status.equals("Fail")) {
				if (isSelected) {
					setForeground(table.getSelectionForeground());
				} else {
					setForeground(table.getForeground());
				}
				setBackground(new Color(255, 0, 0));
			}
		} else {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
		}
		setText((value == null) ? "" : value.toString());
		return this;
	}
}
