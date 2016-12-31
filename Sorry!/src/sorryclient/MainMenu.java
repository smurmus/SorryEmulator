package sorryclient;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import customUI.ClearPanel;
import customUI.PaintedButton;
import customUI.PaintedPanel;
import library.ImageLibrary;

public class MainMenu extends PaintedPanel{
	private static final long serialVersionUID = 3609831945869059312L;
	
	private final JPanel buttonPanel;
	private final JButton host;
	private final JButton join;
	
	public MainMenu(ActionListener hostAction, ActionListener joinAction, Image inImage){
		super(inImage,true);
		
		buttonPanel = new JPanel(new GridLayout(0,3));
		
		host = new PaintedButton(
				"Host",
				ImageLibrary.getImage("images/buttons/grey_button00.png"),
				ImageLibrary.getImage("images/buttons/grey_button01.png"),
				16
				);
		host.addActionListener(hostAction);
		join = new PaintedButton(
				"Join",
				ImageLibrary.getImage("images/buttons/grey_button00.png"),
				ImageLibrary.getImage("images/buttons/grey_button01.png"),
				16
				);
		join.addActionListener(joinAction);
		buttonPanel.add(host);
		buttonPanel.add(new ClearPanel());
		buttonPanel.add(join);
		
		Image titleImage = ImageLibrary.getImage("images/sorry.png");
		PaintedPanel titlePanel = new PaintedPanel(titleImage);
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.ipadx = titleImage.getWidth(null);
		gbc.ipady = titleImage.getHeight(null);
		gbc.insets = new Insets(40,40,40,40);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 1;
		add(titlePanel,gbc);
		gbc.ipadx = 100;
		gbc.ipady = 25;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = 2;
		add(buttonPanel,gbc);
	}
	
}