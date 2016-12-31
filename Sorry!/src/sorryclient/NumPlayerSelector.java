package sorryclient;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import customUI.ClearPanel;
import customUI.PaintedButton;
import customUI.PaintedPanel;
import customUI.PaintedRadioButton;
import library.FontLibrary;
import library.ImageLibrary;

public class NumPlayerSelector extends PaintedPanel {
	private static final long serialVersionUID = -4510696620583889943L;
	
	private int selection = -1;
	private final int numOptions = 3;
	private final PaintedRadioButton[] optionButtons;
	private final ButtonGroup buttonGroup;
	
	private JButton confirmButton;
	
	private final String selectNumPlayerString = "Select the number of players";
	
	private static final Insets spacing = new Insets(60,80,60,80);
	
	public int getNumberOfPlayers() {
		return selection;
	}
	
	public NumPlayerSelector(ActionListener confirmAction, Image inImage){
		super(inImage,true);
		confirmButton = new PaintedButton(
				"Confirm",
				ImageLibrary.getImage("images/buttons/grey_button00.png"),
				ImageLibrary.getImage("images/buttons/grey_button01.png"),
				22
				);
		confirmButton.addActionListener(confirmAction);
		confirmButton.setEnabled(false);
		
		JLabel selectPlayerLabel = new JLabel(selectNumPlayerString);
		selectPlayerLabel.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, 28));
		
		ClearPanel topPanel = new ClearPanel();
		topPanel.setBorder(new EmptyBorder(spacing));
		topPanel.add(selectPlayerLabel);
		
		ClearPanel centerPanel = new ClearPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		
		ClearPanel buttonPanel = new ClearPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		buttonGroup = new ButtonGroup();
		optionButtons = new PaintedRadioButton[numOptions];
		for(int i = 0; i < numOptions; ++i) {
			ClearPanel numPanel = new ClearPanel();
			optionButtons[i] = new PaintedRadioButton(
					ImageLibrary.getImage("images/checkboxes/grey_boxTick.png"),
					ImageLibrary.getImage("images/checkboxes/grey_circle.png")
					);
			final int buttonSelection = i+2;
			optionButtons[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					selection = buttonSelection;
					confirmButton.setEnabled(true);
				}
			});
			buttonGroup.add(optionButtons[i]);
			numPanel.add(optionButtons[i]);
			JLabel numLabel = new JLabel(""+buttonSelection);
			numLabel.setBackground(new Color(0,0,0,0));
			numLabel.setOpaque(true);
			numLabel.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, 28));
			numPanel.add(numLabel);
			
			buttonPanel.add(numPanel);
		}
		
		centerPanel.add(buttonPanel);
		
		ClearPanel bottomPanel = new ClearPanel();
		bottomPanel.setLayout(new GridLayout(1,3));
		bottomPanel.setBorder(new EmptyBorder(spacing));
		bottomPanel.add(Box.createGlue());
		bottomPanel.add(Box.createGlue());
		bottomPanel.add(confirmButton);
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		add(topPanel);
		add(centerPanel);
		add(bottomPanel);
	}
}
