package analyse_installer;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import resources.IImagesKeys;
import resources.ImagesUtils;

public class EULAPage extends WizardPage {
	
	private static String licence = 
							"AUTOMATIC DATA PROCESSING End User License Agreement (EULA)\n" +
							"END-USER LICENSE AGREEMENT FOR AUTOMATIC DATA PROCESSING.\n" + 
							"\n" +
							"IMPORTANT: PLEASE READ THE TERMS AND CONDITIONS OF THIS LICENSE\n" +
							"AGREEMENT CAREFULLY BEFORE CONTINUING WITH THIS PROGRAM INSTALL. BY\n" +
							"CLICKING ON THE \"ACCEPT\" BUTTON, IT IS ASSUMED THAT YOU UNDERSTAND AND\n" +
							"AGREE THE FOLLOWING LICENCE AGREEMENT.\n" +
							"\n" +
							"End-User License Agreement (\"EULA\") of Université d'Aix-Marseille\n" +
							"(AMU - French University of Aix-Marseille) and\n" +
							"Centre National de la recherche Scientifique (CNRS - French \"National\n" +
							"Center for Scientific Research\") is a legal agreement between you\n" +
							"(either an individual or a single entity) and the AMU/CNRS, for the\n" +
							"AUTOMATIC DATA PROCESSING software product identified above which may \n" +
							"include associated software components, media, printed materials, and\n" +
							"\"online\" or electronic documentation (\"SOFTWARE PRODUCT\"). By installing,\n" +
							"copying, or otherwise using the SOFTWARE PRODUCT, you agree to be\n" +
							"bound by the terms of this EULA. This license agreement represents the\n" +
							"entire agreement concerning the program between you and AMU/CNRS,\n" +
							"(referred to as \"licenser\"), and it supersedes any prior proposal,\n" +
							"representation, or understanding between the parties. If you do not\n" +
							"agree to the terms of this EULA, do not install or use the SOFTWARE\n" +
							"PRODUCT.\n" +
							"\n" +
							"The SOFTWARE PRODUCT is protected by copyright laws and international\n" +
							"copyright treaties, as well as other intellectual property laws and\n" +
							"treaties. The SOFTWARE PRODUCT is licensed, not sold.\n" +
							"\n" +
							"1) GRANT OF LICENSE. \n" +
							"The SOFTWARE PRODUCT is licensed as follows: \n" +
							"\n" +
							"1.1) Installation and Use.\n" +
							"AMU and CNRS grants you the right to install and use copies of the\n" +
							"SOFTWARE PRODUCT on your computer running a validly licensed copy of\n" +
							"the operating system for which the SOFTWARE PRODUCT was designed\n" +
							"[Windows XP or Windows 7, Mac OS 10.7 et Linux GTK].\n" +
							"\n" +
							"1.2) Backup Copies.\n" +
							"You may also make copies of the SOFTWARE PRODUCT as may be necessary\n" +
							"for backup and archival purposes.\n" +
							"\n" +
							"1.3) Advertizing clause.\n" +
							"All advertising materials mentioning features or use of this software\n" +
							"must display the following acknowledgement: \"This product has been\n" +
							"developed with the contribution of Institut des Sciences du Mouvement, \n" +
							"University of Aix-Marseille and the French National Center for Scientific\n" +
							"Research\".\n" +
							"\n" +
							"2) DESCRIPTION OF OTHER RIGHTS AND LIMITATIONS.\n" +
							"\n" +
							"2.1) Maintenance of Copyright Notices.\n" +
							"You must not remove or alter any copyright notices on any and all\n" +
							"copies of the SOFTWARE PRODUCT.\n" +
							"\n" +
							"2.2) Distribution.\n" +
							"You may not distribute registered copies of the SOFTWARE PRODUCT to\n" +
							"third parties.\n" +
							"\n" +
							"2.3) Prohibition on Reverse Engineering, Decompilation, and Disassembly.\n" +
							"You may not reverse engineer, decompile, or disassemble the SOFTWARE\n" +
							"PRODUCT, except and only to the extent that such activity is expressly\n" +
							"permitted by applicable law not withstanding this limitation.\n" +
							"\n" +
							"2.4) Rental.\n" +
							"You may not rent, lease, or lend the SOFTWARE PRODUCT.\n" +
							"\n" +
							"2.5) Support Services.\n" +
							"AMU and CNRS do not provide any support services related to the\n" +
							"SOFTWARE PRODUCT (\"Support Services\"). Any supplemental software code\n" +
							"provided to you as part of the Support Services shall be considered\n" +
							"part of the SOFTWARE PRODUCT and subject to the terms and conditions\n" +
							"of this EULA.\n" +
							"\n" +
							"2.6) Compliance with Applicable Laws.\n" +
							"You must comply with all applicable laws regarding use of the SOFTWARE\n" +
							"PRODUCT.\n" +
							"\n" +
							"3) TERMINATION \n" +
							"\n" +
							"Without prejudice to any other rights, AMU and CNRS may terminate this\n" +
							"EULA if you fail to comply with the terms and conditions of this\n" +
							"EULA. In such event, you must destroy all copies of the SOFTWARE\n" +
							"PRODUCT in your possession.\n" +
							"\n" +
							"4) COPYRIGHT\n" +
							"\n" +
							"All title, including but not limited to copyrights, in and to the\n" +
							"SOFTWARE PRODUCT and any copies thereof are owned by AMU and CNRS or\n" +
							"its suppliers. All title and intellectual property rights in and to\n" +
							"the content which may be accessed through use of the SOFTWARE PRODUCT\n" +
							"is the property of the respective content owner and may be protected\n" +
							"by applicable copyright or other intellectual property laws and\n" +
							"treaties. This EULA grants you no rights to use such content. All\n" +
							"rights not expressly granted are reserved by AMU and CNRS.\n" +
							"\n" +
							"5) NO WARRANTIES\n" +
							"\n" +
							"AMU and CNRS expressly disclaim any warranty for the SOFTWARE\n" +
							"PRODUCT. The SOFTWARE PRODUCT is provided \"As Is\" without any express\n" +
							"or implied warranty of any kind, including but not limited to any\n" +
							"warranties of merchantability, no infringement, or fitness of a\n" +
							"particular purpose. AMU and CNRS do not warrant or assume\n" +
							"responsibility for the accuracy or completeness of any information,\n" +
							"text, graphics, links or other items contained within the SOFTWARE\n" +
							"PRODUCT. AMU and CNRS make no warranties respecting any harm that may\n" +
							"be caused by the transmission of a computer virus, worm, time bomb,\n" +
							"logic bomb, or other such computer program. AMU and CNRS further\n" +
							"expressly disclaim any warranty or representation to Authorized Users\n" +
							"or to any third party.\n" +
							"\n" +
							"6) LIMITATION OF LIABILITY\n" +
							"\n" +
							"In no event shall AMU and CNRS be liable for any damages (including,\n" +
							"without limitation, lost profits, business interruption, or lost\n" +
							"information) rising out of 'Authorized Users' use of or inability to\n" +
							"use the SOFTWARE PRODUCT, even if AMU and CNRS have been advised of\n" +
							"the possibility of such damages. In no event will AMU and CNRS be\n" +
							"liable for loss of data or for indirect, special, incidental,\n" +
							"consequential (including lost profit), or other damages based in\n" +
							"contract, tort or otherwise. AMU and CNRS shall have no liability with\n" +
							"respect to the content of the SOFTWARE PRODUCT or any part thereof,\n" +
							"including but not limited to errors or omissions contained therein,\n" +
							"libel, infringements of rights of publicity, privacy, trademark\n" +
							"rights, business interruption, personal injury, loss of privacy, moral\n" +
							"rights or the disclosure of confidential information.\n" +
							"\n" +
							"---------------------------------------------------------------------\n" +
							"                         DISCLAIMER\n" +
							"---------------------------------------------------------------------\n" +
							"\n" +
							"This software is provided by the copyright holders and contributors\n" +
							"\"as is\" and any express or implied warranties, including, but not\n" +
							"limited to, the implied warranties of merchantability and fitness for\n" +
							"a particular purpose are disclaimed. In no event shall the copyright\n" +
							"owner or contributors be liable for any direct, indirect, incidental,\n" +
							"special, exemplary, or consequential damages (including, but not\n" +
							"limited to, procurement of substitute goods or services; loss of use,\n" +
							"data, or profits; or business interruption) however caused and on any\n" +
							"theory of liability, whether in contract, strict liability, or tort\n" +
							"(including negligence or otherwise) arising in any way out of the use\n" +
							"of this software, even if advised of the possibility of such damage.";
	
	public static final String PAGE_NAME = "EULA_PAGE";
	private static String titleMessage = "Licence Agreement";
	private static String message1 = "Please read licence and check button if you agree the terms.";
	private static String agree = "I agree";

	private Button checkButton;

	protected EULAPage() {
		super(PAGE_NAME,"",ImagesUtils.getImageDescriptor(IImagesKeys.UPDATE_BANNER));
		setTitle(titleMessage);
		setMessage(message1,IMessageProvider.INFORMATION);
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent,SWT.NONE);
		container.setLayout(new GridLayout());
		
		Text text = new Text(container, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		text.setText(licence);
		
		checkButton = new Button(container, SWT.CHECK);
		checkButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		checkButton.setText(agree);
		
		checkButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getWizard().getContainer().updateButtons();
				setPageComplete(checkButton.getSelection());
			}
		});
		
		setControl(container);

	}
	
	@Override
	public boolean canFlipToNextPage() {
		return checkButton.getSelection();
	}
	
	

}
