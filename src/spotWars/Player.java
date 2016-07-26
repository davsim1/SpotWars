// David Simmons (GitHub: davsim1)
// Date: 6/27/2016
package spotWars;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Player {
	// Variables
	protected String name;
	protected Color color;
	protected World startingWorld;
	protected double score;
	protected int playerNum;
	protected boolean isHuman;
	
	public static float currColor = 0;
	public static LinkedList<Player> players = new LinkedList<Player>();
	public ArrayList<World> myWorlds = new ArrayList<World>();

	// Constructors
	public Player() {
		int i = 0;
		int infLoopGuard = 0;
		float hue = 0;
		do {
			if (infLoopGuard++ > 100) {
				System.out.println("test");
				break;
			}
			hue = ((GameColor.PLAYERSTART.h * 100 + (Player.currColor++) * 7) % 100);
		} while (Math.abs(hue - 99) < 14 || Math.abs(hue - 62) < 14 || Math.abs(hue - 35) < 14);

		this.color = new Color(Color.HSBtoRGB(hue / 100, GameColor.PLAYERSTART.s,
				GameColor.PLAYERSTART.l));
		this.startingWorld = null;

		Player.players.add(this);
		this.name = "P" + (players.size());
		this.isHuman = false;
	}

	public Player(Boolean isHuman) {
		this();
		this.isHuman = isHuman;
	}

	public Player(World startingWorld) {
		this();
		this.startingWorld = startingWorld;
	}

	public Player(String name, World startingWorld) {
		this();
		this.name = name;
		this.startingWorld = startingWorld;
	}

	public void addWorld(World w) {
		if (!myWorlds.contains(w)) {
			myWorlds.add(w);
		}
	}

	public void removeWorld(World w) {
		myWorlds.remove(w);
	}

	// Getters and Setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public World getStartingWorld() {
		return startingWorld;
	}

	public void setStartingWorld(World startingWorld) {
		this.startingWorld = startingWorld;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public int getPlayerNum() {
		return playerNum;
	}

	public void setPlayerNum(int playerNum) {
		this.playerNum = playerNum;
	}

	public List<World> getMyWorlds() {
		return myWorlds;
	}

}
