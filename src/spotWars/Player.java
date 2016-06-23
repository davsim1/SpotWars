package spotWars;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;

public class Player {
	// Variables
	protected String name;
	protected Color color;
	protected World startingWorld;
	protected double score;
	protected int playerNum;
	
	public static LinkedList<Player> players = new LinkedList<Player>();
	
	// Constructors
	public Player(){
		float hue = ((GameColor.PLAYERSTART.h + Player.players.size() * 140) % 360) / 360 ;
		this.color = new Color(Color.HSBtoRGB(hue, GameColor.PLAYERSTART.s, GameColor.PLAYERSTART.l));
		this.startingWorld = null;
		
		Player.players.add(this);
		this.name = "P" + (players.size());
	}
	
	public Player(World startingWorld){
		this();
		this.startingWorld = startingWorld;
	}
	
	public Player(String name, World startingWorld) {
		this();
		this.name = name;
		this.startingWorld = startingWorld;
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
	
	
}