package customUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class PaintedPanel extends JPanel{
	private static final long serialVersionUID = -1560754474960195264L;

	private Image mImage;
	protected boolean mDrawBack = false;
	protected Color mBackColor = Color.WHITE;
	
	public PaintedPanel(Image inImage) {
		mImage = inImage;
		setBackground(new Color(0,0,0,0));
		this.setOpaque(true);
	}
	
	public PaintedPanel(Image inImage, boolean inDrawBack) {
		mDrawBack = inDrawBack;
		mImage = inImage;
		setBackground(new Color(0,0,0,0));
		this.setOpaque(true);
		repaint();
	}
	
	public void setImage(Image inImage) {
		mImage = inImage;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(mDrawBack)g.drawImage(mImage, 0, 0, getWidth(), getHeight(), Color.WHITE, null);
		else g.drawImage(mImage, 0, 0, getWidth(), getHeight(), null);
			
	}
	
}
