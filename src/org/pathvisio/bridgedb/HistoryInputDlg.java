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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class HistoryInputDlg extends JDialog implements ActionListener
{
	/** private constructor, use one of the static creation methods */
	private HistoryInputDlg(JFrame parent, String message, String title)
	{		
		super(parent, title);

		FormLayout layout = new FormLayout(
				"4dlu, 200dlu, 4dlu", 
				"4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu");
		
		CellConstraints cc = new CellConstraints();
		lblMessage.setText(message);
		setModal(true);
		setResizable(false);
		
		JPanel panel = new JPanel();
		panel.setLayout(layout);
		panel.add (lblMessage, cc.xy(2,2));
		panel.add (txtInput, cc.xy(2,4));		
		panel.add (ButtonBarFactory.buildOKCancelBar (btnOk, btnCancel), cc.xy(2,6));

		setContentPane (panel);
		btnOk.addActionListener(this);
		btnCancel.addActionListener(this);
		pack();
		setLocationRelativeTo(parent);
	}

	JLabel lblMessage = new JLabel();
	HistoryCombo txtInput = new HistoryCombo();
	final JButton btnOk = new JButton("OK");
	final JButton btnCancel = new JButton("Cancel");

	private boolean isCancelled;

	public void actionPerformed(ActionEvent arg0) 
	{
		isCancelled = (arg0.getSource() == btnCancel);
		setVisible(false);
	}

	public static String getInputWithHistory(JFrame parent,
			String message, String title)
	{
		HistoryInputDlg dlg = new HistoryInputDlg(parent, message, title);
				
		dlg.setVisible(true);
		
		if (!dlg.isCancelled) return (String)dlg.txtInput.getSelectedItem();
		else return null;
	}

}
