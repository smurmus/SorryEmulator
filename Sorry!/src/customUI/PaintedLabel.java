package customUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JLabel;

public class PaintedLabel extends JLabel {
	private static final long serialVersionUID = -6437618397404609256L;
	
	private Image mImage;
	
	public PaintedLabel(Image inImage) {
		mImage = inImage;
		setBackground(new Color(0,0,0,0));
		this.setOpaque(true);
	}
	
	public void setImage(Image inImage) {
		mImage = inImage;
		revalidate();
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(mImage, 0, 0, getWidth(), getHeight(), null);
	}
}
