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

import java.awt.AWTException;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;


public class TrayView {

	private ImageIcon imageIcon;
	final JFrame logFrame = new JFrame("Log");;
	final JTextArea text = new JTextArea();
	private PrintStream out;



	public TrayView() {
		try {
			initTrayIcon();
		} catch (AWTException e) {
			// Ignore
		}
		Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		text.setFont(font);
		//startDeamon();
	}



	public void initTrayIcon() throws AWTException {	
		String ccicon = "cardcontact_8bit.gif";

		SystemTray systemTray = java.awt.SystemTray.getSystemTray();
		URL iconURL = TrayView.class.getResource(ccicon);
		imageIcon = new ImageIcon(iconURL);
		String tooltip = "Card Updater Deamon";
		TrayIcon icon = new TrayIcon(imageIcon.getImage(), tooltip, initPopUp());

		systemTray.add(icon);
		icon.setImageAutoSize(true);
		icon.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				logFrame.setVisible(true);
			}
		});
	}



	private PopupMenu initPopUp() {		
		MenuItem exit = new MenuItem("Exit");
		MenuItem log = new MenuItem("Show log");
		MenuItem clearLog = new MenuItem("Clear log");

		initLog();

		PopupMenu menu = new PopupMenu("Settings");
		menu.add(log);
		menu.add(clearLog);
		menu.addSeparator();
		menu.add(exit);		


		log.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logFrame.setVisible(true);
			}
		});

		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		clearLog.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					text.getDocument().remove(0, text.getDocument().getLength());
				} catch (BadLocationException e1) {
					System.out.println(e1.getMessage());
				}
			}
		});

		return menu;
	}



	private void initLog() {
		logFrame.setSize(600, 700);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(text);
		logFrame.getContentPane().add(scrollPane);

		changeStandardOutput();
	}



	private void changeStandardOutput() {
		out = new PrintStream(System.out) {
			@Override
			public void println(String s) {
				this.print(s);
				this.print("\n");
			}

			@Override
			public void print(String s) {
				text.append(s);
				text.setCaretPosition(text.getDocument().getLength());
			}
		};
		System.setOut(out);
	}
}
