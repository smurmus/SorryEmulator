package score;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

public class Score {

	private static final String FAILED_TEXT = "SCORES FAILED TO LOAD!";
	
	private static JDialog scoreDisplay;
	private JEditorPane jEditorPane;
	
	{
		jEditorPane = new JEditorPane();
        jEditorPane.setEditable(false);
        HTMLEditorKit kit = new HTMLEditorKit();
        jEditorPane.setEditorKit(kit);
        
		scoreDisplay = new JDialog();
		scoreDisplay.setTitle("Scores");
		scoreDisplay.setModal(true);
		scoreDisplay.setSize(200, 300);
		JScrollPane jsp = new JScrollPane(jEditorPane);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scoreDisplay.add(jsp);
		
		try {
			Socket s = new Socket("localhost",80);
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while((line = br.readLine()) != null) {
				sb.append(line);
			}
			jEditorPane.setText(sb.toString());
			s.close();
		}catch ( Exception ex ) {
			jEditorPane.setText(FAILED_TEXT);
		}
		
		scoreDisplay.setLocationRelativeTo(null);
		scoreDisplay.setVisible(true);
		
	}

}
