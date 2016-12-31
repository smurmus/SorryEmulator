//Matt Carey - CSCI 201 Assignment 3 Solution
package sorryclient;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import help.Help;
import library.ImageLibrary;
import score.Score;

public class SorryClientWindow extends JFrame{
	private static final long serialVersionUID = 5147395078473323173L;
	
	private final static Dimension minSize = new Dimension(640,480);
	private final static Dimension maxSize = new Dimension(960,640);
	
	{
		setTitle("Sorry!");
		setSize(minSize);
		setMinimumSize(minSize);
		setMaximumSize(maxSize);
		add(new ClientPanel());
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		Toolkit toolkit = Toolkit.getDefaultToolkit();
    	Image cursorImg = ImageLibrary.getImage("images/cursors/cursorHand_grey.png");
    	Cursor cursor = toolkit.createCustomCursor(cursorImg,new Point(0,0),"hand");
    	
    	JMenuBar menuBar = new JMenuBar();
    	
    	JMenuItem help = new JMenuItem("Help");
    	help.setMnemonic('H');
    	help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,ActionEvent.CTRL_MASK));
    	help.addActionListener(new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent ae) {
    			Help.display();
    		}
    	});
    	menuBar.add(help);
    	
    	JMenuItem scores = new JMenuItem("Top Scores");
    	scores.setMnemonic('S');
    	scores.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
    	scores.addActionListener(new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent ae) {
    			new Score();
    		}
    	});
    	
    	menuBar.add(scores);
    	
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	SorryClientWindow scw = new SorryClientWindow();
		    	scw.setJMenuBar(menuBar);
		    	scw.setCursor(cursor);
		    	scw.setVisible(true);
		    }
		});
	}
}
