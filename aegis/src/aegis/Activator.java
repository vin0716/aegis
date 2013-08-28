package aegis;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import aegis.origin.Aegis;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;

public class Activator extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "aegis";
	private static Activator plugin;
	private static Aegis aegis;

	public Activator() {
		super();
		int i = 0;
		int j = 0;
		int k = 0;
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		TCSession session = (TCSession)AIFUtility.getDefaultSession();
		aegis = new Aegis(session);
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static Aegis getAegis() {
		return aegis;
	}
}
