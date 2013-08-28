package aegis.origin;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import aegis.Activator;
import athena.origin.AthenaTask;
import athena.service.ResourceService;

public class AthenaTaskHandler extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String commandID = event.getCommand().getId();
		String string = commandID.substring(commandID.lastIndexOf(".") + 1);
		// ((AthenaTask)Activator.newInstance(string + "Task", new Object[] {session})).execute();
		Aegis aegis = Activator.getAegis();
		AthenaTask task = (AthenaTask)aegis.newInstance(ResourceService.getString(string + "Task", "Class"), new Object[] {});
		task.execute();
		return null;
	}
}
