package sorryclient;

import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import customUI.ClearPanel;
import customUI.PaintedPanel;
import library.FontLibrary;

public class WaitPanel extends PaintedPanel{
	
	private static final long serialVersionUID = -8569142651678811723L;
	
	private final static Insets spacing = new Insets(60,80,60,80);
	private final static String waitingString = "Waiting for more players...";
	
	public WaitPanel(Image inImage) {
		super(inImage,true);
		JLabel selectColorLabel = new JLabel(waitingString);
		selectColorLabel.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, 28));
		
		ClearPanel topPanel = new ClearPanel();
		topPanel.setBorder(new EmptyBorder(spacing));
		topPanel.add(selectColorLabel);
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		add(topPanel);
		add(Box.createGlue());
	}
}
