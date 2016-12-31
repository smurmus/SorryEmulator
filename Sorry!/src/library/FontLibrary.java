package library;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//A statically used class for loading image resources
//Ensures that images aren't loaded into memory more than necessary
public class FontLibrary {
	private static Map<String, Font> fontMap;
	
	static{
		fontMap = new HashMap<String,Font>();
	}
	
	private FontLibrary(){} //disable constructor
	
	//Gets the image if available already, otherwise the image is loaded and returned
	public static Font getFont(String directory, int style, int size) {
		Font font = fontMap.get(directory);
		if(font == null) {
			try {
				font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(directory));
			}
			catch (IOException | FontFormatException e) { System.out.println("Error reading image: " + e); return null; }
			fontMap.put(directory, font);
		}
		return font.deriveFont(style, size);
	}
	
	//Clears out all the images from the library
	public static void clearImages() {
		fontMap.clear();
	}
}