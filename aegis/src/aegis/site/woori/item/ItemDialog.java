package aegis.site.woori.item;

import java.io.File;
import java.util.Vector;

import javax.swing.JPanel;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;

import aegis.origin.Aegis;
import aegis.site.wiq.part.PartOperation;
import aegis.Activator;
import athena.origin.AthenaDialogSWT;
import athena.origin.AthenaOperation;
import athena.service.ResourceService;

public class ItemDialog extends AthenaDialogSWT {
	private Vector<String> vector;
	private ListViewer listViewer;
	private List list;
	private Button addButton, removeButton;

	public ItemDialog(Aegis aegis, Shell shell) {
		super(aegis, shell);
	}

	protected Control createDialogArea(Composite parent) {// Dialog Composite
		Composite composite = (Composite)super.createDialogArea(parent);// main Composite
		createPrefixComposite(composite);
		createReferenceComposite(composite);
		return composite;
	}

	private void createPrefixComposite(Composite parent) {
		Group group = new Group(parent, SWT.NULL);
		group.setText(getString("Information"));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout(2, false);
		group.setLayout(layout);
		getLabel("prefix").setParent(group);
		Control prefix = getControl("prefix");
		prefix.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		prefix.setParent(group);
	}

	private void createReferenceComposite(Composite parent) {
		Group group = new Group(parent, SWT.NULL);
		group.setText(getString("Reference"));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		FormLayout layout = new FormLayout();
		group.setLayout(layout);
		list.setParent(group);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		// formData.bottom = new FormAttachment(cancelButton, -5);
		formData.left = new FormAttachment(0, 5);
		formData.right = new FormAttachment(100, -5);
		list.setLayoutData(formData);
		removeButton = new Button(group, SWT.PUSH);
		removeButton.setData(new String("Remove"));
		Image image = new Image(shell.getDisplay(), ResourceService.getImageStream(getBundle(), "Remove"));
		removeButton.setImage(image);
		formData = new FormData();
		// formData.left = new FormAttachment(addButton,5);
		formData.right = new FormAttachment(100, -5);
		formData.bottom = new FormAttachment(100, -5);
		removeButton.setLayoutData(formData);
		addButton = new Button(group, SWT.PUSH);
		addButton.setData(new String("Add"));
		image = new Image(shell.getDisplay(), ResourceService.getImageStream(getBundle(), "Add"));
		addButton.setImage(image);
		formData = new FormData();
		formData.top = new FormAttachment(list, 5);
		formData.right = new FormAttachment(removeButton, -5);
		formData.bottom = new FormAttachment(100, -5);
		addButton.setLayoutData(formData);
	}

	public void createControls(Composite composite) {
		listViewer = new ListViewer(composite, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		list = listViewer.getList();
		listViewer.setLabelProvider(new LabelProvider());
		listViewer.setContentProvider(new ArrayContentProvider());
		vector = new Vector();
		listViewer.setInput(vector);
	}

	public void setListener() {
		removeButton.addSelectionListener(this);
		addButton.addSelectionListener(this);
	}

	protected void okPressed() {
		String prefix = ((Text)getControl("prefix")).getText().trim();
		new ItemOperation(athena, session, prefix, vector);
		super.okPressed();
	}

	protected boolean isPrepared() {
		if (vector.size() == 0) {
			MessageDialog dialog = new MessageDialog(shell, getString("AttachFile"), null, getString("AttachFile"), MessageDialog.WARNING, new String[] {"Close"}, 0);
			dialog.open();
			return false;
		}
		return true;
	}

	public void widgetSelected(SelectionEvent event) {
		Object object = event.getSource();
		if (object.equals(addButton)) {
			FileDialog dialog = new FileDialog(shell, SWT.MULTI);
			dialog.open();
			String path = dialog.getFilterPath();
			String[] files = dialog.getFileNames();
			for (int i = 0; i < files.length; i++) {
				String filePath = path + File.separator + files[i];
				boolean exist = false;
				for (int j = 0; j < vector.size(); j++) {
					if ((vector.elementAt(j)).equals(filePath)) {
						exist = true;
					}
				}
				if (!exist) {
					vector.addElement(filePath);
				}
			}
			listViewer.setInput(vector);
		} else if (object.equals(removeButton)) {
			int i = list.getSelectionIndex();
			if (i == -1) {
				return;
			}
			vector.removeElementAt(i);
			listViewer.setInput(vector);
		}
	}

	public Bundle getBundle() {
		return Activator.getDefault().getBundle();
	}
}