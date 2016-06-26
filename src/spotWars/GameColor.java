// David Simmons (GitHub: davsim1)
// Date: 7/8/2014
package spotWars;

import java.awt.Color;

public enum GameColor {
	RED(230, 103, 114), BLUE(103, 154, 230), GREEN(103, 230, 122), GRAY(180, 180, 180), PLAYERSTART(70, 230, 230); // Cyan
 
	Color thisColor;
	float h;
	float s;
	float l;

	GameColor(int r, int g, int b) {
		float[] HSBcolors = new float[3];
		thisColor = new Color(r, g, b);
		Color.RGBtoHSB(r, g, b, HSBcolors);
		h = HSBcolors[0];
		s = HSBcolors[1];
		l = HSBcolors[2];
	}

	Color getColor() {
		return thisColor;
	}

}
