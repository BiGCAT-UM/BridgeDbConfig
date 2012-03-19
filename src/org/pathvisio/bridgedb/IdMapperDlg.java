package org.pathvisio.bridgedb;

import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import org.bridgedb.gui.BridgeDbParameterModel;
import org.bridgedb.gui.BridgeRestParameterModel;
import org.bridgedb.gui.ConnectionStringParameterModel;
import org.bridgedb.gui.FileParameterModel;
import org.bridgedb.gui.JdbcParameterModel;
import org.bridgedb.gui.ParameterPanel;
import org.bridgedb.gui.PgdbParameterModel;
import org.pathvisio.core.debug.Logger;

public class IdMapperDlg extends JDialog implements ActionListener, HyperlinkListener
{
	JPanel cardPanel;
	CardLayout cardLayout;
	JEditorPane helpLabel;
	
	public IdMapperDlg(JDialog parentDialog)
	{		
		super(parentDialog, "Configure new ID Mapper");

		setModal(true);
		setResizable(true);

		JPanel panel = new JPanel();
		FormLayout layout = new FormLayout(
				"3dlu, fill:100dlu, 3dlu, fill:300dlu:grow, 3dlu",
				"3dlu, fill:120dlu, 3dlu, fill:200dlu:grow, 3dlu, pref, 3dlu"
				);
		CellConstraints cc = new CellConstraints();
		panel.setLayout(layout);

        BridgeDbParameterModel models[] = new BridgeDbParameterModel[] {
		 		new PgdbParameterModel(),
		 		new FileParameterModel(),
		 		new BridgeRestParameterModel(),
		 		new JdbcParameterModel(),
		 		new ConnectionStringParameterModel()
        };

        cardLayout = new CardLayout();
		cardPanel = new JPanel();
		cardPanel.setLayout(cardLayout);
		
		helpLabel = new JEditorPane();
		helpLabel.setEditable(false);
		helpLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		helpLabel.addHyperlinkListener(this);
		helpLabel.setContentType( "text/html" );

		lMappers = new JList(models);
        lMappers.addListSelectionListener(new ListSelectionListener()
		{	
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				BridgeDbParameterModel item = (BridgeDbParameterModel)lMappers.getSelectedValue();
				if (item != null)
				{	
					cardLayout.show(cardPanel, item.getName());
					helpLabel.setText(item.getHelpHtml());
					helpLabel.setCaretPosition(0);
				}
			}
		});
		       
		panel.add (new JScrollPane(lMappers), cc.xywh(2,2,1,3));
		panel.add (new JScrollPane(helpLabel), cc.xy(4,2));
		panel.add (new JScrollPane(cardPanel), cc.xy(4,4));
		panel.add (ButtonBarFactory.buildOKCancelBar (btnOk, btnCancel), cc.xyw(2, 6, 3));
		
        for (BridgeDbParameterModel model : models)
        {
        	try
        	{
	        	JPanel card = new ParameterPanel(model);
	        	cardPanel.add(card, model.getName());
        	}
        	catch (Exception ex)
        	{
        		Logger.log.error ("Could not initialize model", ex);
        		cardPanel.add(new JLabel("Initialization error, see log for details"), model.getName());
        	}
        }
        cardPanel.add (new JPanel(), "no selection"); // empty initial panel
        cardLayout.show(cardPanel, "no selection");
        
		setContentPane (panel);
		btnOk.addActionListener(this);
		btnCancel.addActionListener(this);
		pack();
		setLocationRelativeTo(parentDialog);
	}

	JList lMappers;
	final JButton btnOk = new JButton("OK");
	final JButton btnCancel = new JButton("Cancel");

	private boolean isCancelled = true;

	public void actionPerformed(ActionEvent arg0) 
	{
		isCancelled = (arg0.getSource() == btnCancel);
		setVisible(false);
	}

	public String getConnectionString()
	{
		if (isCancelled)
			return null;
		else
			return ((BridgeDbParameterModel)lMappers.getSelectedValue()).getConnectionString();
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e)
	{
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
		{
			try
			{
				Desktop.getDesktop().browse(e.getURL().toURI());
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (URISyntaxException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}
