/*
 * Copyright � 1997 - 1999 IBM Corporation.
 * 
 * Redistribution and use in source (source code) and binary (object code)
 * forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 1. Redistributed source code must retain the above copyright notice, this
 * list of conditions and the disclaimer below.
 * 2. Redistributed object code must reproduce the above copyright notice,
 * this list of conditions and the disclaimer below in the documentation
 * and/or other materials provided with the distribution.
 * 3. The name of IBM may not be used to endorse or promote products derived
 * from this software or in any other form without specific prior written
 * permission from IBM.
 * 4. Redistribution of any modified code must be labeled "Code derived from
 * the original OpenCard Framework".
 * 
 * THIS SOFTWARE IS PROVIDED BY IBM "AS IS" FREE OF CHARGE. IBM SHALL NOT BE
 * LIABLE FOR INFRINGEMENTS OF THIRD PARTIES RIGHTS BASED ON THIS SOFTWARE.  ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IBM DOES NOT WARRANT THAT THE FUNCTIONS CONTAINED IN THIS
 * SOFTWARE WILL MEET THE USER'S REQUIREMENTS OR THAT THE OPERATION OF IT WILL
 * BE UNINTERRUPTED OR ERROR-FREE.  IN NO EVENT, UNLESS REQUIRED BY APPLICABLE
 * LAW, SHALL IBM BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  ALSO, IBM IS UNDER NO OBLIGATION
 * TO MAINTAIN, CORRECT, UPDATE, CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS
 * SOFTWARE.
 */

package opencard.core.service;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/******************************************************************************
* The default dialog used by card services.
*
* @author  Thomas Schaeck (schaeck@de.ibm.com)
* @version $Id: DefaultCHVDialog.java,v 1.1.1.1 1999/10/05 15:34:31 damke Exp $
*
* @see opencard.core.service.CHVDialog
******************************************************************************/
public class DefaultCHVDialog implements CHVDialog
{
  /******************************************************************************
  * IDDialog is used to request the PIN from the user.
  ******************************************************************************/
  public class IDDialog extends Dialog implements ActionListener
  {
    protected Button	     okButton;
    protected Button	     cancelButton;
    protected Label	     messageLabel;
    protected boolean	     finished;
    protected Object	     objectToNotify;
    public TextField     textField;
    protected String	     chv;

    /****************************************************************************
    * Create a new dialog.
    * @param parent    the frame to which the dialog shall belong
    * @param title     the title to be shown in the title bar
    * @param prompt    the text to be printed above the text entry field
    ****************************************************************************/
    public IDDialog(Frame parent, String title, String prompt)
    {
      super(parent, title, true);
      GridBagLayout gridBag = new GridBagLayout();
      setLayout(gridBag);
      GridBagConstraints c = new GridBagConstraints();
      setLayout(gridBag);
      c.weightx = 1.0;
      c.weighty = 1.0;
      messageLabel = new Label(prompt);
      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 2;
      gridBag.setConstraints(messageLabel, c);
      add(messageLabel);
      c.gridx = 0;
      c.gridy = 1;
      textField = new TextField("", 10);
      textField.addActionListener(this);
      textField.setEchoChar('*');
      gridBag.setConstraints(textField, c);
      add(textField);
//      this.objectToNotify = objectToNotify;
      c.gridx = 0;
      c.gridy = 2;
      c.gridwidth = 1;
      okButton = new Button(" OK ");
      okButton.addActionListener(this);
      gridBag.setConstraints(okButton, c);
      add(okButton);
      c.gridx = 1;
      c.gridy = 2;
      cancelButton = new Button("Cancel");
      cancelButton.addActionListener(this);
      gridBag.setConstraints(cancelButton, c);
      add(cancelButton);
      this.setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
      this.pack();
    }

    /****************************************************************************
    * Handle action events.<p>
    * Close the dialog on buttons and RETURN in the text field.
    * @param e    The <tt>ActionEvent</tt> to be handeled.
    ****************************************************************************/
    public void actionPerformed(ActionEvent e)
    {
      Object source = e.getSource();

      if (source == okButton || source == textField) {
	chv = textField.getText();
      } else if (source == cancelButton) {
	chv = null;
      }
      setVisible(false);
    }

    /****************************************************************************
    * Get the CHV previously entered.
    * @return the CHV
    ****************************************************************************/
    public String chv() { return chv; }
    
    public void showDialog() {
        Thread t = new Thread() {
            public void run() {

            try { Thread.sleep(500); } 
            catch (InterruptedException e) { };
            textField.requestFocus();
            }
    	};

        t.start();
        setVisible(true);
    }
  }

  /****************************************************************************
  * Get the CHV with the given number from the user.
  * @return the CHV
  ****************************************************************************/
  public String getCHV(int chvNumber)
  {
    ResourceBundle rb = ResourceBundle.getBundle("opencard.core.service.DefaultCHVDialogResourceBundle");
    String dialogTitle = rb.getString("chv.title");
    String dialogPrompt = rb.getString("chv.prompt.prefix") + chvNumber +
                          rb.getString("chv.prompt.postfix");
    Frame    frame  = new Frame("");
    IDDialog dialog = new IDDialog(frame, dialogTitle, dialogPrompt);
    frame.setVisible(false);
    Dimension dim=frame.getToolkit().getScreenSize();

//    dialog.setSize(dim.width/3, dim.height/3);
    dialog.setSize(220, 150);
//    dialog.setLocation(dim.width/2-dim.width/6, dim.height/2-dim.height/6);
    dialog.setLocation(dim.width-220, 0);
    dialog.showDialog();
    frame.dispose();		 // kill the frame
    String chv = dialog.chv();
    if (chv != null && chv.length() > 0) {
      return chv;
    } else {
      return null;
    }
  }
}

// $Log: DefaultCHVDialog.java,v $
// Revision 1.1.1.1  1999/10/05 15:34:31  damke
// Import OCF1.1.1 from Zurich
//
// Revision 1.2  1999/08/09 11:18:29  ocfadmin
// replacing OCF 1.1 with updates of OCF 1.1.1 (aka Hudson) as of Mai 1999 (by J.Damke)
//
// Revision 1.4  1998/04/15 12:39:46  schaeck
// Added NLS
//
// Revision 1.3  1998/04/14 14:22:18  breid
// CVS Log-Keyword added
//
