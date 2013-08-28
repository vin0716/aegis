package aegis.origin;

import java.sql.SQLException;

import athena.service.DatabaseService;

import com.teamcenter.rac.kernel.TCSession;

public class QueryService extends DatabaseService {
	private static QueryService service;

	public static void createService(TCSession session) {
		if (service == null) {
			service = new QueryService(session);
		}
	}

	private QueryService(TCSession session) {
		super(session);
	}

	public static long wiq_newClient(String code, String name, String staff, String address, String type, String phone, String fax, String description) {
		long aid;
		try {
			callableStatement = connection.prepareCall("{call AT_WIQ_NEW_CLIENT(?, ?, ?, ?, ?, ?, ?, ?, ?)}");
			callableStatement.setString(1, code);
			callableStatement.setString(2, name);
			callableStatement.setString(3, staff);
			callableStatement.setString(4, address);
			callableStatement.setString(5, type);
			callableStatement.setString(6, phone);
			callableStatement.setString(7, fax);
			callableStatement.setString(8, description);
			callableStatement.registerOutParameter(9, java.sql.Types.BIGINT);
			callableStatement.executeUpdate();
			aid = callableStatement.getLong(9);
			callableStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		return aid;
	}
	
	public static long isms_newdownloadHistory(String dataset_name, String item_id, String object_name, String user_id, String user_name) {
		long aid;
		try {
			callableStatement = connection.prepareCall("{call AT_ISMS_NEW_DOWNLOAD_HISTORY(?, ?, ?, ?, ?, ?)}");
			callableStatement.setString(1, dataset_name);
			callableStatement.setString(2, item_id);
			callableStatement.setString(3, object_name);
			callableStatement.setString(4, user_id);
			callableStatement.setString(5, user_name);
			callableStatement.registerOutParameter(6, java.sql.Types.BIGINT);
			callableStatement.executeUpdate();
			aid = callableStatement.getLong(6);
			callableStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		return aid;
	}
}