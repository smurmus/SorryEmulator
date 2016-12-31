package customUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import game.CardDeck;
import library.FontLibrary;
import library.ImageLibrary;

public class CardDialog extends JDialog {
	private static final long serialVersionUID = 1421966997677854733L;

	private static final Map<Integer,CardDialog> cardDialogs;
	private static final Image background;
	
	private final JPanel content;
	private final JLabel name;
	private final JTextArea text;
	
	static {
		cardDialogs = new HashMap<Integer,CardDialog>();
		background = ImageLibrary.getImage("images/cards/card_brown.png");
		for(int i = 0; i < CardDeck.cardNames.length; ++i) {
			String[] split = CardDeck.cardNames[i].split("-");
			cardDialogs.put(i, new CardDialog(split[0].trim(),split[1].trim()));
		}
	}
	
	{
		setSize(200,300);
		setModal(true);
		content = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = 7847772867160557058L;
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
			}
		};
		content.setBorder(new EmptyBorder(25,25,25,25));
		add(content);
		
		name = new JLabel();
		name.setBackground(new Color(0,0,0,0));
		name.setOpaque(true);
		name.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, 24));
		
		ClearPanel center = new ClearPanel();
		center.add(name);
		content.add(center, BorderLayout.NORTH);
		
		text = new JTextArea();
		text.setBackground(new Color(0,0,0,0));
		text.setEditable(false);
		text.setHighlighter(null);
		text.setOpaque(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, 12));
		
		content.add(text, BorderLayout.CENTER);
		setTitle("Sorry!");
	}
	
	public static void popup(int type) {
		CardDialog toShow = cardDialogs.get(type);
		toShow.setLocationRelativeTo(null);
		toShow.setVisible(true);
	}
	
	CardDialog(String inName, String inText) {
		name.setText(inName);
		text.setText(inText);
	}
	
	
}
