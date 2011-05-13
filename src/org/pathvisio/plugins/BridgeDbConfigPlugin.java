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

import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.pathvisio.core.data.GdbManager;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.Preference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;

/**
 * A tutorial implementation of a PathVisio plug-in
 */
public class BridgeDbConfigPlugin implements Plugin
{
	private PvDesktop desktop;
	
	enum AdvancedSynonymPreferences implements Preference
	{
		BRIDGEDB_CONNECTION_1,
		BRIDGEDB_CONNECTION_2,
		BRIDGEDB_CONNECTION_3,
		BRIDGEDB_CONNECTION_4,
		BRIDGEDB_CONNECTION_5,
		BRIDGEDB_CONNECTION_6,
		BRIDGEDB_CONNECTION_7,
		BRIDGEDB_CONNECTION_8,
		BRIDGEDB_CONNECTION_9,
		BRIDGEDB_CONNECTION_10,
		
		BRIDGEDB_TRANSITIVE("" + false);
		
		private final String defaultValue;
		
		private AdvancedSynonymPreferences()
		{
			defaultValue = null;
		}
		
		private AdvancedSynonymPreferences(String defaultValue)
		{
			this.defaultValue = defaultValue;
		}
		
		@Override
		public String getDefault()
		{
			return defaultValue;
		}
		
		static Preference[] connectionStrings = new Preference[] {
			BRIDGEDB_CONNECTION_1, BRIDGEDB_CONNECTION_2, BRIDGEDB_CONNECTION_3, BRIDGEDB_CONNECTION_4,
			BRIDGEDB_CONNECTION_5 };
		
	}
	
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
		
		PreferenceManager pm = PreferenceManager.getCurrent();
		GdbManager mgr = desktop.getSwingEngine().getGdbManager();
		for (int i = 0; i < AdvancedSynonymPreferences.connectionStrings.length; ++i)
		{
			String s = pm.get(AdvancedSynonymPreferences.connectionStrings[i]);
			if (!(s == null || "".equals(s)))
			{
				try
				{
					mgr.addMapper(s);
					Logger.log.trace ("Added mapper: " + s); 
				}
				catch (IDMapperException ex)
				{
					Logger.log.error ("Could not restore mapping service from preferences: " + s, ex);
				}
			}
		}

		mgr.getCurrentGdb().setTransitive(pm.getBoolean(AdvancedSynonymPreferences.BRIDGEDB_TRANSITIVE));
	}

	public void done() 
	{
	}

	private final SynDlgAction synDlgAction = new SynDlgAction();
	
	/**
	 * Open the synonym database settings dialog 
	 */
	private class SynDlgAction extends AbstractAction
	{
		SynDlgAction()
		{
			// The NAME property of an action is used as 
			// the label of the menu item
			putValue (NAME, "Identifier mapping setup...");
		}
		
		/**
		 *  called when the user selects the menu item
		 */
		public void actionPerformed(ActionEvent arg0) 
		{
			BridgeDbConfigDlg dlg = new BridgeDbConfigDlg(desktop);
			dlg.createAndShowGUI();
		}
	}
}
