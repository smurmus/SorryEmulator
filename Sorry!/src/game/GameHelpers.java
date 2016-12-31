package game;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class GameHelpers {
	private static final Color colors[] = {Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN};
	
	private static final Map<String,Color> colorLookup = new HashMap<String,Color>();
	private static final Map<Color,String> slideLabelLookup = new HashMap<Color,String>();
	private static final Map<Color,Integer> colorIndexLookup = new HashMap<Color,Integer>();
	private static final Map<Color,String> colorNameLookup = new HashMap<Color,String>();
	private static final Map<String,Integer> colorNameIndexLookup = new HashMap<String,Integer>();
	
	static {
		colorLookup.put("-", Color.BLACK);
		colorLookup.put("r", colors[0]);
		colorLookup.put("b", colors[1]);
		colorLookup.put("y", colors[2]);
		colorLookup.put("g", colors[3]);
		
		slideLabelLookup.put(colors[0], "<");
		slideLabelLookup.put(colors[1], "^");
		slideLabelLookup.put(colors[2], ">");
		slideLabelLookup.put(colors[3], "v");
		
		colorIndexLookup.put(colors[0], 0);
		colorIndexLookup.put(colors[1], 1);
		colorIndexLookup.put(colors[2], 2);
		colorIndexLookup.put(colors[3], 3);
		
		colorNameLookup.put(colors[0], "red");
		colorNameLookup.put(colors[1], "blue");
		colorNameLookup.put(colors[2], "yellow");
		colorNameLookup.put(colors[3], "green");
		
		colorNameIndexLookup.put("Red", 0);
		colorNameIndexLookup.put("Blue", 1);
		colorNameIndexLookup.put("Yellow", 2);
		colorNameIndexLookup.put("Green", 3);
	}

	public static Color getColorFromIndex(int index) {
		return colors[index];
	}
	
	public static Integer getIndexFromColor(Color color) {
		return colorIndexLookup.get(color);
	}
	
	public static Color getColorFromString(String color) {
		return colorLookup.get(color);
	}
	
	public static String getSlideLabelFromColor(Color color) {
		return slideLabelLookup.get(color);
	}

	public static String getNameFromColor(Color color) {
		String toReturn = colorNameLookup.get(color);
		if (toReturn == null) return "grey";
		else return toReturn;
	}
	
	public static Integer getIndexFromColorName(String name) {
		return colorNameIndexLookup.get(name);
	}

	public static boolean containsWhitespace(String str) {
	    if (!hasLength(str)) {
	      return false;
	    }
	    int strLen = str.length();
	    for (int i = 0; i < strLen; i++) {
	      if (Character.isWhitespace(str.charAt(i))) {
	        return true;
	      }
	    }
	    return false;
	  }
	
	 public static boolean hasLength(String str) {
		    return (str != null && str.length() > 0);
		  }
	
}
