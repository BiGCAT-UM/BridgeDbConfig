package org.pathvisio.plugins;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.pathvisio.data.GdbManager;
import org.pathvisio.debug.Logger;
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
	private JButton btnRemove;

	public void createAndShowGUI()
	{
		final JFrame aboutDlg = new JFrame();
		
		FormLayout layout = new FormLayout(
				"4dlu, 50dlu:grow, 4dlu, 50dlu:grow, 4dlu",
				"4dlu, fill:50dlu:grow, 4dlu, pref, 4dlu, pref, 4dlu");
		layout.setColumnGroups   (new int[][]{ {2, 4} });

		CellConstraints cc = new CellConstraints();
		
		txtInfo = new JTextArea(40, 5);
		
		list = new JList(
				desktop.getSwingEngine().getGdbManager());
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
				aboutDlg.setVisible(false);
				aboutDlg.dispose();
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

		dialogBox.add (new JScrollPane(list), cc.xy (2, 2));
		dialogBox.add (new JScrollPane(txtInfo), cc.xy (4, 2));

		boolean isTransitive = desktop.getSwingEngine().getGdbManager().getCurrentGdb().getTransitive();
		final JCheckBox ckTransitive = new JCheckBox("Transitive", isTransitive);
		
		dialogBox.add (ckTransitive, cc.xyw (2,4,3));
		ckTransitive.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent arg0) 
			{
				desktop.getSwingEngine().getGdbManager().getCurrentGdb().
					setTransitive(ckTransitive.isSelected());
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add (btnOk);
		buttonPanel.add (btnAdd);
		buttonPanel.add (btnRemove);
		dialogBox.add (buttonPanel, cc.xyw (2, 6, 3));			
		
		aboutDlg.setTitle("Advanced Synonym Database Settings");
		aboutDlg.add (dialogBox);
		aboutDlg.pack();
		aboutDlg.setSize(600, 400);
		aboutDlg.setLocationRelativeTo(desktop.getFrame());
		aboutDlg.setVisible(true);
	}

	private void removePressed()
	{
		int selected = list.getSelectedIndex();
		if (selected >= 0)
		{
			GdbManager manager = desktop.getSwingEngine().getGdbManager();
			IDMapper mapper = manager.getCurrentGdb().getIDMapperAt(selected);
			try {
				manager.removeMapper(mapper);
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
		String connectString = JOptionPane.showInputDialog(desktop.getFrame(), "Please enter a BridgeDb connection String", 
				"Add connection", JOptionPane.QUESTION_MESSAGE);
		if (connectString != null)
		{
			GdbManager manager = desktop.getSwingEngine().getGdbManager();
			try
			{
				manager.addMapper(connectString);
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
