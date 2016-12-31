package customUI;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JRadioButton;

public class PaintedRadioButton extends JRadioButton{
	private static final long serialVersionUID = -2379744418193606172L;

	private Image mImage;
	
	private final Image mSelected;
	private final Image mNotSelected;
	
	public PaintedRadioButton(Image inSelected, Image inNotSelected) {
		mSelected = inSelected;
		mImage = mNotSelected  = inNotSelected;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(isSelected()) mImage = mSelected;
		else mImage = mNotSelected;
		g.drawImage(mImage, 0, 0, getWidth(), getHeight(), null);
	}
}
