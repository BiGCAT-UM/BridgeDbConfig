// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathvisio.plugins;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.plugin.Plugin;

/**
 * A tutorial implementation of a PathVisio plug-in
 */
public class AdvancedSynonymPlugin implements Plugin
{
	private PvDesktop desktop;
	
	public void init(PvDesktop desktop) 
	{
		// save the desktop reference so we can use it later
		this.desktop = desktop;
		
		// register our action in the "Help" menu.
		desktop.registerMenuAction ("Data", synDlgAction);
		
		// register more idmapper Drivers
		try
		{
			Class.forName("org.bridgedb.file.IDMapperText");
			Class.forName("org.bridgedb.webservice.bridgerest.BridgeRest");
			Class.forName("org.bridgedb.webservice.biomart.IDMapperBiomart");
			Class.forName("org.bridgedb.webservice.picr.IDMapperPicr");
			Class.forName("org.bridgedb.webservice.picr.IDMapperPicrRest");
			Class.forName("org.bridgedb.webservice.cronos.IDMapperCronos");
			Class.forName("org.bridgedb.webservice.synergizer.IDMapperSynergizer");
			Class.forName ("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException ex)
		{
			Logger.log.error ("Could not register IDMapper: ", ex);
		}
	}

	public void done() {}

	private final SynDlgAction synDlgAction = new SynDlgAction();
	
	/**
	 * Display a welcome message when this action is triggered. 
	 */
	private class SynDlgAction extends AbstractAction
	{
		SynDlgAction()
		{
			// The NAME property of an action is used as 
			// the label of the menu item
			putValue (NAME, "Synonym Database Settings...");
		}
		
		/**
		 *  called when the user selects the menu item
		 */
		public void actionPerformed(ActionEvent arg0) 
		{
			AdvancedSynonymDlg dlg = new AdvancedSynonymDlg(desktop);
			dlg.createAndShowGUI();
		}
	}
}
