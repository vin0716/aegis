package aegis.origin;

import java.lang.reflect.Constructor;
import java.util.Locale;

import athena.origin.Athena;
import athena.service.ResourceService;

import com.teamcenter.rac.kernel.TCSession;

public class Aegis extends Athena {
	public Aegis(TCSession session) {
		super(session);
		QueryService.createService(session);
		ResourceService.createService(Locale.getDefault().toString());
	}

	public Object newInstance(String string, Object[] objects) {
		try {
			Class[] classes = new Class[objects.length + 1];
			classes[0] = this.getClass();
			for (int i = 1; i <= objects.length; i++) {
				classes[i] = objects[i - 1].getClass();
			}
			Constructor constructor = Class.forName(string).getConstructor(classes);
			Object[] parameters = new Object[objects.length + 1];
			parameters[0] = this;
			for (int i = 1; i < parameters.length; i++) {
				parameters[i] = objects[i - 1];
			}
			return constructor.newInstance(parameters);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
