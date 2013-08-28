package aegis.site.wiq.client;

import aegis.origin.QueryService;
import athena.origin.Athena;
import athena.origin.AthenaOperation;

public class ClientOperation extends AthenaOperation {
	private String code, name, staff, address, type, phone, fax, description;

	public ClientOperation(Athena athena, String code, String name, String staff, String address, String type, String phone, String fax, String description) {
		super(athena);
		this.code = code;
		this.name = name;
		this.staff = staff;
		this.address = address;
		this.type = type;
		this.phone = phone;
		this.fax = fax;
		this.description = description;
	}

	public void execute() {
		aid = QueryService.wiq_newClient(code, name, staff, address, type, phone, fax, description);
		if (aid > 0) {
			isOperationDone = true;
		}
	}
}
