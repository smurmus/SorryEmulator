package sorryserver;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

@SuppressWarnings("serial")
public class SorryServerWindow extends JFrame{

	public static final String serverWindowTitle = "Authentication Server";
	
	public static final int maxChatLines = 50;
	public static final int maxChatServerLines = 100;
	public static final int maxChatLinesReduce = 10;
	
	private JTextArea msgBox;
	private DefaultCaret caret;
	private JScrollPane scrollPane;
	
	//instance constructor
	{
		setTitle(serverWindowTitle);
		msgBox = new JTextArea(4, 0);
		msgBox.setEditable(false);
		msgBox.setLineWrap(true);
		caret = (DefaultCaret)msgBox.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		scrollPane = new JScrollPane(msgBox);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane);
	}
	
	SorryServerWindow() {
		setSize(640,480);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}
	
	public void addMessage(String msg) {
		msgBox.append("\n"+msg);
		if(msgBox.getLineCount() == maxChatServerLines) {
			String messages[] = msgBox.getText().split("\n");
			msgBox.setText("");
			for(int i = 0; i < maxChatServerLines-maxChatLinesReduce; ++i) {
				msgBox.append("\n"+messages[i+maxChatLinesReduce]);
			}
		}
	}
}
