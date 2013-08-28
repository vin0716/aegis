package aegis.origin;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.teamcenter.rac.util.Instancer;
import com.teamcenter.rac.util.viewer.ISubViewer;
import com.teamcenter.rac.util.viewer.IViewerFactory;
import com.teamcenter.rac.util.viewer.SubViewerListener;

public class AthenaViewerFactory implements IViewerFactory {
	private String viewerPanel;

	public AthenaViewerFactory() {
	}

	public void setParent(Object obj) {
	}

	public void loadViewer(final SubViewerListener listener) {
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					ISubViewer isubviewer = (ISubViewer)Instancer.newInstance(viewerPanel);
					if (isubviewer instanceof JPanel) {
						((JPanel)isubviewer).setVisible(true);
						((JPanel)isubviewer).setEnabled(true);
						((JPanel)isubviewer).setMinimumSize(new Dimension(0, 0));
					}
					listener.done(isubviewer);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
		};
		SwingUtilities.invokeLater(runnable);
	}

	public void setViewPanelClassName(String string) {
		viewerPanel = string;
	}
}
