package sorryclient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import customUI.PaintedButton;
import library.FontLibrary;
import library.ImageLibrary;
import networking.MessageConstants;

public class ChatBox extends JPanel{
	private static final long serialVersionUID = -8733618559215673231L;
	public final ChatLog chatLog;
	{
		setLayout(new BorderLayout());
		chatLog = new ChatLog();
		add(chatLog,BorderLayout.NORTH);
		Box southBox = Box.createHorizontalBox();
		JTextField toSendText = new JTextField();
		toSendText.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 12));
		toSendText.setForeground(Color.WHITE);
		toSendText.setBackground(Color.BLACK);
		southBox.add(toSendText);
		PaintedButton connect = new PaintedButton(
				"Send",
				ImageLibrary.getImage("images/buttons/grey_button00.png"),
				ImageLibrary.getImage("images/buttons/grey_button01.png"),
				10
				);
		connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String toSend = toSendText.getText().replace("|", "(?)");
				OutMail.send(MessageConstants.BROADCAST, MessageConstants.CHAT_MESSAGE + toSend);
				toSendText.setText("");
			}
		});
		southBox.add(connect);
		add(southBox,BorderLayout.SOUTH);
		setPreferredSize(new Dimension(0,southBox.getPreferredSize().height + chatLog.getPreferredSize().height));
	}
	
	public void addMessage(String name, String message, boolean system) {
		chatLog.addMessage(name, message, system);
	}
}

class ChatLog extends JPanel {
	private static final long serialVersionUID = -7750375626632126527L;
	private static final int numMessagesDisplayed = 4;
	private final JScrollPane jsp;
	private final JPanel content;
	private static final Color blue = new Color(37,164,219);
	private static final Color red = new Color(230,106,26);
	private static final Color green = new Color(113,206,71);
	private static final Color yellow = new Color(255,203,2);
	{
		setLayout(new BorderLayout());
		ChatMessage sample = new ChatMessage("Name","Message",Color.BLACK,false);
		setPreferredSize(new Dimension(0,sample.getPreferredSize().height*numMessagesDisplayed+5));
		content = new JPanel(new GridLayout(0,1));
		content.setBackground(Color.BLACK);
		jsp = new JScrollPane(content);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(jsp);
		
	}
	public void addMessage(String name, String message, boolean system) {
		Color c = Color.WHITE;
		if(name.equals("Blue")) c = blue;
		if(name.equals("Red")) c = red;
		if(name.equals("Green")) c = green;
		if(name.equals("Yellow")) c = yellow;
		content.add(new ChatMessage(name,message,c,system));
		revalidate();
		repaint();
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
				jsp.getVerticalScrollBar().setValue(jsp.getVerticalScrollBar().getMaximum());
		    }
		});
	}
}

class ChatMessage extends JPanel {
	private static final long serialVersionUID = 5192624033887020338L;
	private final JLabel name;
	private final JLabel text;
	private static final String namePostFix = ":  ";
	
	{
		setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		setBackground(Color.BLACK);
		name = new JLabel();
		name.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 12));
		text = new JLabel();
		text.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 12));
		add(name);
		add(text);
	}
	
	ChatMessage(String inName, String inText, Color color, boolean system) {
		name.setForeground(color);
		name.setText(inName + namePostFix);
		if(system) text.setForeground(color);
		else text.setForeground(Color.WHITE);
		text.setText(inText);
	}
}