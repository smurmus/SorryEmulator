package customUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import library.FontLibrary;

public class PaintedButton extends JButton{
	private static final long serialVersionUID = 7074393176317490987L;

	private Image toDraw;
	private final Image mUp;
	private final Image mDown;
	
	private final int mFontSize;
	
	public PaintedButton(String name, Image inUp, Image inDown, int inFontSize) {
		super(name);
		toDraw = mUp = inUp;
		mDown = inDown;
		mFontSize = inFontSize;
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				toDraw = mDown;
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
				toDraw = mUp;
			}
		});
		
		setOpaque(true);
		setBackground(new Color(0,0,0,0));
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(toDraw, 0, 0, getWidth(), getHeight(), null);
		g.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, mFontSize));
		super.paintComponent(g);
	}
	
	@Override
	protected void paintBorder(Graphics g) {
		//paint no border
	}
	
}
