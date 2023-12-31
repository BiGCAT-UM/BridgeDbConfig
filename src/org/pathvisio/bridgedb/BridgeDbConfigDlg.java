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
package org.pathvisio.bridgedb;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperCapabilities;
import org.bridgedb.IDMapperException;
import org.bridgedb.gui.BridgeDbParameterModel;
import org.pathvisio.bridgedb.BridgeDbConfigPlugin.AdvancedSynonymPreferences;
import org.pathvisio.core.data.GdbManager;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.PreferenceManager;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class BridgeDbConfigDlg 
{
	private final GdbManager gdbManager;
	private final PreferenceManager pm;
	private final JFrame parentFrame;
	private final BridgeDbParameterModel[] models;
	
	/**
	 * @pm may be null
	 */
	public BridgeDbConfigDlg(JFrame frame, PreferenceManager pm, GdbManager gdbManager, BridgeDbParameterModel[] models)
	{
		this.gdbManager = gdbManager;
		this.pm = pm;
		this.parentFrame = frame;
		this.models = models;
	}

	private JList list;
	private JTextArea txtInfo;
	private JButton btnRemove;
	private JDialog mappersDlg;

	public void createAndShowGUI()
	{
		mappersDlg = new JDialog(parentFrame);
		
		FormLayout layout = new FormLayout(
				"4dlu, 50dlu:grow, 4dlu, 50dlu:grow, 4dlu",
				"4dlu, fill:50dlu:grow, 4dlu, pref, 4dlu, pref, 4dlu");
		layout.setColumnGroups   (new int[][]{ {2, 4} });

		CellConstraints cc = new CellConstraints();
		
		txtInfo = new JTextArea(40, 5);
		
		list = new JList(gdbManager);
		list.setCellRenderer(new BridgeRenderer());
		list.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent arg0) 
			{
				updateInfo();
			}
		});
		
		JPanel dialogBox = new JPanel();
		dialogBox.setLayout (layout);
		
		JButton btnOk = new JButton();
		btnOk.setText("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				mappersDlg.setVisible(false);
				mappersDlg.dispose();
				
				writeMapperPreferences();
			}
		});
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				addPressed();
			}
		});

		btnRemove = new JButton("Remove");
		btnRemove.setEnabled(false);
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				removePressed();
			}
		});
		
		JButton btnHelp = new JButton("Help");
		btnHelp.setToolTipText("Help about this dialog");
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					Desktop.getDesktop().browse(new URI("http://www.pathvisio.org/wiki/BridgeDbConfigPluginHelp"));
				}
				catch (IOException e1)
				{
					Logger.log.error ("Couldn't open Browser", e1);
				}
				catch (URISyntaxException e1)
				{
					Logger.log.error ("Couldn't open Browser", e1);
				}
			}
		});
		
		dialogBox.add (new JScrollPane(list), cc.xy (2, 2));
		dialogBox.add (new JScrollPane(txtInfo), cc.xy (4, 2));

		boolean isTransitive = gdbManager.getCurrentGdb().getTransitive();
		final JCheckBox ckTransitive = new JCheckBox("Transitive", isTransitive);
		
		dialogBox.add (ckTransitive, cc.xyw (2,4,3));
		ckTransitive.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent arg0) 
			{
				gdbManager.getCurrentGdb().
					setTransitive(ckTransitive.isSelected());
				pm.setBoolean(AdvancedSynonymPreferences.BRIDGEDB_TRANSITIVE, ckTransitive.isSelected());
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add (btnOk);
		buttonPanel.add (btnAdd);
		buttonPanel.add (btnRemove);
		buttonPanel.add (btnHelp);
		dialogBox.add (buttonPanel, cc.xyw (2, 6, 3));			
		
		mappersDlg.setTitle("BridgeDb Configuration");
		mappersDlg.add (dialogBox);
		mappersDlg.pack();
		mappersDlg.setSize(600, 400);
		mappersDlg.setLocationRelativeTo(parentFrame);
		mappersDlg.setVisible(true);
	}

	protected void writeMapperPreferences()
	{
		if (pm == null) return;
		
		// clear all old preferences
		for (int i = 0; i < AdvancedSynonymPreferences.connectionStrings.length; ++i)
		{
			pm.set(AdvancedSynonymPreferences.connectionStrings[i], null);
		}
		
		int pos = 0;
		for (int i = 0; i < gdbManager.getSize(); ++i)
		{
			IDMapper mapper = (IDMapper)gdbManager.getElementAt(i);
			if (mapper == gdbManager.getGeneDb() || mapper == gdbManager.getMetaboliteDb()) continue;
			String s = gdbManager.getConnectionStringAt(i);
			pm.set(AdvancedSynonymPreferences.connectionStrings[pos], s);
			pos++;
		}
	}

	private void removePressed()
	{
		int selected = list.getSelectedIndex();
		if (selected >= 0)
		{
			IDMapper mapper = gdbManager.getCurrentGdb().getIDMapperAt(selected);
			try {
				gdbManager.removeMapper(mapper);
			} 
			catch (IDMapperException e1) 
			{
				//Problem during close can be ignored. Just log exception.
				Logger.log.error ("Could not close connection ", e1);
			}
		}
	}
	
	private void addPressed()
	{
		IdMapperDlg dlg = new IdMapperDlg(mappersDlg, models);
		dlg.setVisible(true);

		String connectString = dlg.getConnectionString();
		if (connectString != null)
		{
			Logger.log.info ("Using connection string [" + connectString + "]");
			try
			{
				gdbManager.addMapper(connectString);
			}
			catch (IDMapperException ex)
			{
				//TODO: redundant with SwingEngine.selectGdb
				String msg = "Failed to open database; " + ex.getMessage();
				JOptionPane.showMessageDialog(null, 
						"Error: " + msg + "\n\n" + "See the error log for details.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				Logger.log.error(msg, ex);
			}
		}
	}
	
	private void updateInfo()
	{
		int selected = list.getSelectedIndex();
		btnRemove.setEnabled(selected >= 0);
		if (selected < 0)
		{
			txtInfo.setText("No Data");
		}
		else
		{
			txtInfo.setText ("");
			IDMapper mapper = (IDMapper)gdbManager.getElementAt(selected);
			IDMapperCapabilities caps = mapper.getCapabilities(); 
			List<String> sortedKeys = new ArrayList<String>(caps.getKeys());
			Collections.sort(sortedKeys);
			for (String key : sortedKeys)
			{
				txtInfo.append(key + " = " + caps.getProperty(key) + "\n");
			}
			try
			{
				txtInfo.append("Supported sources: " + caps.getSupportedSrcDataSources() + "\n");
				txtInfo.append("Supported targets: " + caps.getSupportedTgtDataSources() + "\n");
			}
			catch (IDMapperException ex) { /* ignore, just a little less info */ }
			txtInfo.append ("Free search supported: " + caps.isFreeSearchSupported());
		}
	}
	
	private static class BridgeRenderer extends JLabel implements ListCellRenderer 
	{
	     public BridgeRenderer() {
	         setOpaque(true);
	     }
	     public Component getListCellRendererComponent(
	         JList list,
	         Object value,
	         int index,
	         boolean isSelected,
	         boolean cellHasFocus)
	     {
	    	 String text = value.toString();
	    	 if (text == null || "".equals(text))
	    		 text = "<unnamed idmapper>";
	    	 // shorten long names
	    	 if (text.length() > 50) text = text.substring(0,10) + "..." + 
	    	 	text.substring(text.length()-30);
	         setText(text);
	         if (isSelected) {
	             setBackground(list.getSelectionBackground());
	             setForeground(list.getSelectionForeground());
	         } else {
	             setBackground(list.getBackground());
	             setForeground(list.getForeground());
	         }

	         return this;
	     }
	 }

}
