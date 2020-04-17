package analyse.gui.dialogs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import analyse.resources.Messages;

public class ModifySampleFrequencyDialog extends Dialog {
	
	private double sf;
	private String signalName;

	public ModifySampleFrequencyDialog(Shell parentShell, double sf, String signalName) {
		super(parentShell);
		this.sf = sf;
		this.signalName = signalName;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("ModifySampleFrequencyDialog.Title"));
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		
		dialogArea.setLayout(new GridLayout(2, false));
		
		Label sfLabel = new Label(dialogArea, SWT.NORMAL);
		sfLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		sfLabel.setText(Messages.getString("ChannelsView.SignalsItemFrequencyLabelTitle"));
		
		Text sfText = new Text(dialogArea, SWT.BORDER);
		sfText.setText(String.valueOf(sf));
		sfText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		sfText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				Pattern pattern = Pattern.compile("^\\d+(\\.\\d+)?$");
		        Matcher matcher = pattern.matcher(sfText.getText());
		        getButton(IDialogConstants.OK_ID).setEnabled(matcher.matches());
		        if(matcher.matches()) {
		        	sf = Double.parseDouble(sfText.getText());
		        }
			}
		});
		
		Label infoLabel = new Label(dialogArea, SWT.NORMAL);
		infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		infoLabel.setText(Messages.getString("ModifySampleFrequencyDialog.For") + " : " + signalName);
		
		return dialogArea;
	}
	
 
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	
	public double getSampleFrequency() {
		return sf;
	}
	

}
