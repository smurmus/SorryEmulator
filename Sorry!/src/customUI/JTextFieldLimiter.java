package customUI;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class JTextFieldLimiter extends PlainDocument{
	private static final long serialVersionUID = -7217458816450223753L;
	private int limit;

	public JTextFieldLimiter(int limit) {
		super();
		this.limit = limit;
	}

	public void insertString( int offset, String  str, AttributeSet attr ) throws BadLocationException {
		if(str == null) return;
		if((getLength() + str.length()) <= limit) {
			//if(Integer.valueOf(this.getText(0, getLength())) > maxPort ) return;
			super.insertString(offset, str, attr);
		}
	}
}