package aegis.site.wiq.workflow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import aegis.origin.Aegis;
import aegis.origin.AthenaProcessViewDialog;
import aegis.utility.TemplateParser;

import com.teamcenter.rac.kernel.TCComponentProcess;

public class ProcessViewDialog extends AthenaProcessViewDialog {
	public ProcessViewDialog(Aegis aegis) {
		super(aegis);
	}

	protected void save(TCComponentProcess process, File file) {
		TemplateParser parser = new TemplateParser();
		String content = parser.parseWorkflow(process);
		download(file, content);
	}

	public void download(File file, String content) {
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
