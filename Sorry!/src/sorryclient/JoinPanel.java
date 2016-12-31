package sorryclient;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;

import customUI.ClearPanel;
import customUI.JTextFieldLimiter;
import customUI.PaintedButton;
import customUI.PaintedPanel;
import library.FontLibrary;
import library.ImageLibrary;

public class JoinPanel extends PaintedPanel {
	private static final long serialVersionUID = 7432083141632844078L;

	private static final String IPString = "IP:";
	private static final int IPLength = 20;
	private static final String portString = "Port:";
	private static final int portLength = 5;
	private static final String connectString = "Connect";
	
	private JLabel IPNumberLabel;
	private JTextField IPNumberField;
	
	private JLabel portNumberLabel;
	private JTextField portNumberField;
	
	{
		portNumberLabel = new JLabel(portString);
		portNumberLabel.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 32));
		portNumberField = new JTextField(portLength-1);
		portNumberField.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 32));
		portNumberField.setDocument(new JTextFieldLimiter(portLength));
		portNumberField.setBackground(Color.GRAY);
		
		IPNumberLabel = new JLabel(IPString);
		IPNumberLabel.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 32));
		IPNumberField = new JTextField(IPLength-1);
		IPNumberField.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 32));
		IPNumberField.setDocument(new JTextFieldLimiter(IPLength));
		IPNumberField.setBackground(Color.GRAY);

		portNumberField.setText("3469");
		IPNumberField.setText("localhost");
	}
	
	public JoinPanel(ActionListener hostAction, Image inImage){
		super(inImage,true);
		ClearPanel portInfoPanel = new ClearPanel();
		portInfoPanel.setLayout(new BoxLayout(portInfoPanel,BoxLayout.X_AXIS));
		portInfoPanel.add(portNumberLabel);
		portInfoPanel.add(Box.createHorizontalStrut(10));
		portInfoPanel.add(portNumberField);
		
		ClearPanel IPInfoPanel = new ClearPanel();
		IPInfoPanel.setLayout(new BoxLayout(IPInfoPanel,BoxLayout.X_AXIS));
		IPInfoPanel.add(IPNumberLabel);
		IPInfoPanel.add(Box.createHorizontalStrut(10));
		IPInfoPanel.add(IPNumberField);
		
		PaintedButton connect = new PaintedButton(
				connectString,
				ImageLibrary.getImage("images/buttons/grey_button00.png"),
				ImageLibrary.getImage("images/buttons/grey_button01.png"),
				22
				);
		connect.addActionListener(hostAction);
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.ipadx = 50;
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.gridwidth = 5;
		add(IPInfoPanel,gbc);
		
		gbc.ipadx = 75;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		add(new ClearPanel(), gbc);
		gbc.gridwidth = 2;
		gbc.gridx = 2;
		add(portInfoPanel,gbc);
		gbc.gridwidth = 1;
		gbc.gridx = 4;
		add(new ClearPanel(), gbc);

		gbc.gridy = 2;
		gbc.insets = new Insets(40,40,40,40);
		gbc.ipady = 25;
		gbc.ipadx = 100;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridwidth = 1;
		gbc.gridx = 3;
		add(connect,gbc);
	}
	
	public String getIP() {
		return IPNumberField.getText();
	}
	
	public int getPort() {
		return Integer.valueOf(portNumberField.getText());
	}

}