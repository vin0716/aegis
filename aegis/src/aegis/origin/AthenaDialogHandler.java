package aegis.origin;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import aegis.Activator;
import athena.service.ResourceService;

public class AthenaDialogHandler extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String commandID = event.getCommand().getId();
		String string = commandID.substring(commandID.lastIndexOf(".") + 1);
		Aegis aegis = Activator.getAegis();
		aegis.launchDialog(string + "Dialog", new Object[] {HandlerUtil.getActiveShell(event)});
		return null;
	}
}
