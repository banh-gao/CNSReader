/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2013 CardContact Software & System Consulting
 * |'##> <##'|  Andreas Schwier, 32429 Minden, Germany (www.cardcontact.de)
 *  --------- 
 *
 *  This file is part of OpenSCDP.
 *
 *  OpenSCDP is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *
 *  OpenSCDP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSCDP; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.cardcontact.cli;

import java.awt.Dialog;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;


public class URLVerifier {

	private ServerURLManager manager;
	private static int OPTION_ALWAYS = 0;
	private static int OPTION_ONCE = 1;
	private static int OPTION_NO = 2;
	public URLVerifier() {
		manager = new ServerURLManager();
	}



	public boolean verifyURL(String url) {
		if (manager.isApproved(url)) {
			return true;
		}

		Object[] options = { 
				"Yes, always",
				"Yes, only once",
				"No"};

		String message = "The server "
			+"\n" + url 
			+ "\nis trying to connect to your smart card."
			+ "\n\nDo you wish to allow the connection ?";

		String title = "Incoming Connection";

		JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, null);
		JDialog dialog = pane.createDialog(title);
		dialog.setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
		Dimension dim = dialog.getToolkit().getScreenSize();
		dialog.setLocation(dim.width - dialog.getWidth(), 0);
		dialog.setVisible(true);
		
		Object value = pane.getValue();
		if (value == null) {
			return false;
		}

		int n = 0;
		for (; n < options.length && !value.equals(options[n]); n++);
		
		if (n == OPTION_ALWAYS) {
			try {
				manager.approveServerURL(url);
			} catch (IOException e) {
				// Ignore
			}
			return true;
		} else if (n == OPTION_ONCE) {
			return true;
		}

		return false;
	}
}
