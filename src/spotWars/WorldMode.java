// David Simmons (GitHub: davsim1)
// Date: 7/8/2014
package spotWars;

public enum WorldMode {
	OFFENSIVE(1), EXPLORATIVE(2), DEFENSIVE(3), NEUTRAL(4);
 
	GameColor thisColor;
	// These are multipliers for what is to be subtracted when attacking
	// ex: red attacking blue
	// blue power -= red power * blue defense (= 0.5)
	double strength;
	// Defense has 05% more so in an even match the attacker wins
	double defense;
	double strengthAgainstGray;
	String label;

	WorldMode(int num) {
		if (num == 1) {
			thisColor = GameColor.RED;
			strength = 1;
			defense = 1.05;
			strengthAgainstGray = 0;
			label = "O";
		} else if (num == 2) {
			thisColor = GameColor.GREEN;
			strength = 0;
			defense = 1.05;
			strengthAgainstGray = 1;
			label = "E";
		} else if (num == 3) {
			thisColor = GameColor.BLUE;
			strength = 0.5; // BLUE WINS AGAINST RED
			defense = 0.55;
			strengthAgainstGray = 0;
			label = "D";
		} else if (num == 4) {
			thisColor = GameColor.GRAY;
			strength = 0;
			defense = 1;
			strengthAgainstGray = 0;
			label = "N";
		}
	}

	public double getStrength() {
		return strength;
	}

	public double getDefense() {
		return defense;
	}

	public double getStrengthAgainstGray() {
		return strengthAgainstGray;
	}

	public String getLabel() {
		return label;
	}

	GameColor getColor() {
		return thisColor;
	}
}
