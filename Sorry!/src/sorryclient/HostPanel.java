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

public class HostPanel extends PaintedPanel {
	private static final long serialVersionUID = 7432083141632844078L;

	private static final String portString = "Port:";
	private static final String connectString = "Start";
	private static final int portLength = 5;
	
	private JLabel portNumberLabel;
	private JTextField portNumberField;
	
	{
		portNumberLabel = new JLabel(portString);
		portNumberLabel.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 32));
		portNumberField = new JTextField(portLength-1);
		portNumberField.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 32));
		portNumberField.setDocument(new JTextFieldLimiter(portLength));
		portNumberField.setBackground(Color.GRAY);
		
		portNumberField.setText("3469");
	}
	
	public HostPanel(ActionListener hostAction, Image inImage){
		super(inImage,true);
		ClearPanel portInfoPanel = new ClearPanel();
		portInfoPanel.setLayout(new BoxLayout(portInfoPanel,BoxLayout.X_AXIS));
		portInfoPanel.add(portNumberLabel);
		portInfoPanel.add(Box.createHorizontalStrut(10));
		portInfoPanel.add(portNumberField);
		
		PaintedButton connect = new PaintedButton(
				connectString,
				ImageLibrary.getImage("images/buttons/grey_button00.png"),
				ImageLibrary.getImage("images/buttons/grey_button01.png"),
				22
				);
		connect.addActionListener(hostAction);
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(40,40,40,40);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 1;
		add(portInfoPanel,gbc);
		gbc.ipadx = 100;
		gbc.ipady = 25;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = 2;
		add(connect,gbc);
	}
	
	public int getPort() {
		return Integer.valueOf(portNumberField.getText());
	}

}