package org.pathvisio.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperCapabilities;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.pathvisio.gui.swing.PvDesktop;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class AdvancedSynonymDlg 
{
	final PvDesktop desktop;
	
	AdvancedSynonymDlg(PvDesktop desktop)
	{
		this.desktop = desktop;
	}

	private JList list;
	private JTextArea txtInfo;

	public void createAndShowGUI()
	{
		final JFrame aboutDlg = new JFrame();
		
		FormLayout layout = new FormLayout(
				"4dlu, 200dlu, 4dlu, 200dlu, 4dlu",
				"4dlu, pref:grow, 4dlu, pref, 4dlu, pref, 4dlu");
		
		CellConstraints cc = new CellConstraints();
		
		txtInfo = new JTextArea(40, 5);
		
		list = new JList(
				desktop.getSwingEngine().getGdbManager());
		
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
				addPressed();
			}
		});
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				aboutDlg.setVisible (false);
				aboutDlg.dispose();
			}
		});

		JButton btnRemove = new JButton("Remove");
		btnRemove.setEnabled(false);
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				//TODO
			}
		});

		dialogBox.add (new JScrollPane(list), cc.xy (2, 2));
		dialogBox.add (new JScrollPane(txtInfo), cc.xy (4, 2));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add (btnOk);
		buttonPanel.add (btnAdd);
		dialogBox.add (buttonPanel, cc.xyw (2, 6, 3));			
		
		aboutDlg.setTitle("Advanced Synonym Database Settings");
		aboutDlg.add (dialogBox);
		aboutDlg.pack();
		aboutDlg.setLocationRelativeTo(desktop.getFrame());
		aboutDlg.setVisible(true);
	}

	private void addPressed()
	{
		String result = JOptionPane.showInputDialog(desktop.getFrame(), "Please enter a BridgeDb connection String", 
				"Add connection", JOptionPane.QUESTION_MESSAGE);
		IDMapperStack stack = desktop.getSwingEngine().getGdbManager().getCurrentGdb();
		try
		{
			stack.addIDMapper(result);
		}
		catch (IDMapperException ex)
		{
			JOptionPane.showMessageDialog(desktop.getFrame(), "Could not connect: " + ex.getMessage());
		}
	}
	
	private void updateInfo()
	{
		int selected = list.getSelectedIndex();
		if (selected < 0)
		{
			txtInfo.setText("No Data");
		}
		else
		{
			txtInfo.setText ("");
			IDMapper mapper = (IDMapper)desktop.getSwingEngine().getGdbManager().getElementAt(selected);
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
			catch (IDMapperException ex) { /* ignore, just a litte less info */ }
			txtInfo.append ("Free search supported: " + caps.isFreeSearchSupported());
		}
	}	
}
