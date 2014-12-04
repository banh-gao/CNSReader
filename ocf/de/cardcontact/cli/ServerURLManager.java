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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

/**
 * This class manages a list of approved server urls.
 * All approved urls will be stored in a file in the user's
 * home directory.
 * 
 * @author lew
 */
public class ServerURLManager {

	String fileName = ".card-update-servers";
	String defaultPath;
	HashSet<String> serverSet;



	public ServerURLManager() {
		String home = System.getProperty("user.home");
		defaultPath = home + "/" + fileName;
		serverSet = new HashSet<String>();
		try {
			loadServerList();
		} catch (IOException e) {
			// Ignore
		}
	}



	/**
	 * Read all approved server urls from the file
	 * card_update_server.txt from the user's home directory.
	 * If the file doesn't exist it will be created.
	 * 
	 * @throws IOException If an I/O error occurs during read/write operations
	 */
	private void loadServerList() throws IOException {

		File file = new File(defaultPath);
		try {
			FileReader in = new FileReader(file);
			BufferedReader reader = new BufferedReader(in);

			String server = reader.readLine();
			String newline = System.getProperty("line.separator");
			while (server != null) {
				if (!(server.equals(newline) || server.startsWith("#") || server.equals(""))) {
					serverSet.add(server);
				}
				server = reader.readLine();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("Create file " + file.getAbsolutePath() + " containing all authorized server urls.");

			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.write("# User authorized card update servers:");
			writer.close();
		}		
	}



	/**
	 * Check whether this server was approved by the user
	 * 
	 * @param url the server url
	 * @return true if the the server is approved, false otherwise
	 */
	public boolean isApproved(String url) {
		return serverSet.contains(url);
	}



	/**
	 * Approving this server and save the url to a file
	 * 
	 * @param url the server url
	 * @throws IOException
	 */
	public void approveServerURL(String url) throws IOException {
		if (serverSet.add(url)) {		
			File file = new File(defaultPath);
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.newLine();
			writer.append(url);
			writer.close();
		}
	}



	private void test() throws IOException {
		ServerURLManager sm = new ServerURLManager();
		String url = "http://test..";
		sm.loadServerList();
		boolean approved = sm.isApproved(url);
		sm.approveServerURL(url);
		approved = sm.isApproved(url);
		assert(approved == true);
	}
}
