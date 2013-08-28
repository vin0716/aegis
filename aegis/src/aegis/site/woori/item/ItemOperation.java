package aegis.site.woori.item;

import java.io.File;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

import athena.origin.Athena;
import athena.origin.AthenaOperation;
import athena.service.DatasetService;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTextService;

public class ItemOperation {
	private TCSession session;
	private String prefix;
	private Vector<String> vector;

	public ItemOperation(Athena athena, TCSession session, String prefix, Vector<String> vector) {
		this.session = session;
		this.prefix = prefix;
		this.vector = vector;
		execute();
	}

	public void execute() {
		try {
			TCComponent target = (TCComponent)AIFUtility.getCurrentApplication().getTargetComponent();
			TCComponentItemType itemType = (TCComponentItemType)session.getTypeComponent("Item");
			for (int i = 0; i < vector.size(); i++) {
				String item_id = getSerial();
				TCComponentItem item = itemType.create(item_id, "A", "Item", getFileName(vector.elementAt(i)), new String(), null);
				if (target instanceof TCComponentFolder) {
					target.add("contents", item);
				} else {
					session.getUser().getNewStuffFolder().add("contents", item);
				}
				DatasetService.createDataset(item.getLatestItemRevision(), "IMAN_specification", vector.elementAt(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getSerial() throws Exception {
		Calendar calendar = Calendar.getInstance();
		String year = String.valueOf(calendar.get(Calendar.YEAR));
		String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
		String date = String.valueOf(calendar.get(Calendar.DATE));
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (date.length() == 1) {
			date = "0" + date;
		}
		String pattern = year + month + date;
		TCTextService textService = session.getTextService();
		TCComponent[] components = session.search("Item...", new String[] {textService.getTextValue("Type"), textService.getTextValue("ItemID")}, new String[] {"Item", "*" + pattern + "-????"});
		String serial = "0001";
		int length = 4;
		for (int i = 0; i < components.length; i++) {
			TCComponentItem item = (TCComponentItem)components[i];
			String item_id = item.getProperty("item_id");
			String string = item_id.substring(item_id.lastIndexOf("-") + 1);
			if (string.compareTo(serial) > 0) {
				serial = string;
			}
		}
		components = session.search("Item...", new String[] {textService.getTextValue("Type"), textService.getTextValue("ItemID")}, new String[] {"Document", "*" + pattern + "-????"});
		for (int i = 0; i < components.length; i++) {
			TCComponentItem item = (TCComponentItem)components[i];
			String item_id = item.getProperty("item_id");
			String string = item_id.substring(item_id.lastIndexOf("-") + 1);
			if (string.compareTo(serial) > 0) {
				serial = string;
			}
		}
		serial = String.valueOf(Integer.parseInt(serial) + 1);
		String zero = new String();
		for (int i = serial.length(); i < length; i++) {
			zero += "0";
		}
		if (prefix.length() > 0) {
			return prefix + "-" + pattern + "-" + zero + serial;
		} else {
			return pattern + "-" + zero + serial;
		}
	}

	private String getFileName(String string) {
		File file = new File(string);
		String filename = file.getName();
		int i = filename.lastIndexOf(".");
		return filename.substring(0, i);
	}
}
