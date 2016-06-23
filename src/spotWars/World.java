/*
 *	A world that represents a circle on the map.
 */
package spotWars;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.LinkedList;

public class World {
	// Variables
	protected boolean beingAttacked;
	protected boolean attacking;
	protected Point coords;
	protected double power;
	protected double diameter;
	protected Color color;
	protected boolean occupied;
	protected World attackedBy;
	protected World attackingWhom;
	protected Player owner;
	protected WorldMode mode;
	protected boolean selected;
	protected WorldInfo infoBox;
	protected int previousTicks;
	protected Point center;

	public static LinkedList<World> worlds = new LinkedList<World>();
	public static final double maxPower = 100;
	public static final double startDiameter = 50;

	// Constructors
	public World(){
		// Set up initial values
		this.coords = new Point(0,0);
		this.infoBox = null;
		this.center = null;
		this.resetVariables();
		this.setOccupied(true); //Delete this line ******************************************************
		// Add this new world to the list of worlds
		World.worlds.add(this);
	}

	public World(Point coords){
		this();
		this.coords = coords;
		this.infoBox = new WorldInfo(this);
		this.center = new Point((int)(this.coords.x + this.diameter/2), (int)(this.coords.y + this.diameter/2));
	}

	public World(Point coords, int diameter){
		this(coords);
		this.diameter = diameter;
	}

	// General methods
	protected void resetVariables(){
		this.beingAttacked = false;
		this.attacking = false;
		this.power = Math.random()*10 + 10;
		this.diameter = World.startDiameter;
		this.color = GameColor.GRAY.getColor();
		this.occupied = false;
		this.attackedBy = null;
		this.attackingWhom = null;
		this.owner = new Player("test", this); // *************************************************
		this.mode = WorldMode.NEUTRAL;
		this.selected = false;
	}

	public void update(int ticks){
		if(this.getMode() != WorldMode.NEUTRAL){
			/*
			if(this.isBeingAttacked()){
				// Being attacked
				// CASES:    		attackee - power per tick
				 //  red v red   	1 
				//   red v green		1
				//   red v blue		.5
				//   red v gray		0
				 //  
				 //  green v red		0
				 //  green v green	0
				 //  green v blue	0 
				 //  green v gray	1
				 //  
				 // blue v red		.5
				 //  blue v green	.5
				  // blue v blue		.25
				  // blue v gray		0


				this.setPower(this.getPower() - (ticks - this.previousTicks));
			} else if(this.isAttacking()) {
				// Decrease power level while attacking, but not to 0
				if(this.getPower() - (ticks - this.previousTicks) > 0){
					this.setPower(this.getPower() - (ticks - this.previousTicks));
				} else {
					// Cancel attack when this runs out of power
					this.getAttackingWhom().setBeingAttacked(false);
					this.getAttackingWhom().setAttackedBy(null);
					this.setAttacking(false);
					this.setAttackingWhom(null);
				}
			} else {
				// Increase power by 1 for each tick while not being attacked
				this.setPower(this.getPower() + (ticks - this.previousTicks));
			}
			 */

			if(this.isAttacking()){
				World.attack(this, this.getAttackingWhom(), ticks);
			} else if(!this.isBeingAttacked()){
				// Increase power by 1 for each tick while not being attacked and not attacking
				this.setPower(this.getPower() + (ticks - this.previousTicks));
			}
		}

		// Update color (update saturation based off of power)
		this.setColor(new Color(Color.HSBtoRGB(this.getMode().getColor().h, (float)(this.power/World.maxPower), this.getMode().getColor().l)));

		// If being attacked, cancel attack when power reaches 0 and reset world
		if(this.isBeingAttacked()){
			if(this.getPower() <= 0){
				WorldMode attackerMode = this.getAttackedBy().getMode();
				Player attackerOwner = this.getAttackedBy().getOwner();
				// Cancel attack
				this.getAttackedBy().setAttackingWhom(null);
				this.getAttackedBy().setAttacking(false);
				this.setAttackedBy(null);
				// Reset and unclaim this world
				this.resetVariables();
				// If the attacker that just defeated this world was explorative
				if(attackerMode == WorldMode.EXPLORATIVE){
					this.setOwner(attackerOwner);
					this.setOccupied(true);
				}


				/*
				this.setOccupied(false);
				this.setMode(WorldMode.NEUTRAL);
				this.setColor(GameColor.GRAY.getColor());
				if(this.getAttackedBy() != null){
					this.getAttackedBy().setAttackingWhom(null);
					this.getAttackedBy().setAttacking(false);
					this.setAttackedBy(null);
				}
				this.setBeingAttacked(false);
				this.setAttackedBy(null);

				this.setAttacking(false);
				this.setAttackingWhom(null);
				 */
			}
		}

		// If attacking, cancel attack when power reaches 1
		if(this.isAttacking()){
			if(this.getPower() <= 1){
				this.getAttackingWhom().setBeingAttacked(false);
				this.getAttackingWhom().setAttackedBy(null);
				this.setAttacking(false);
				this.setAttackingWhom(null);
			}
		}

		// Crucially set current ticks to previous ticks for the next time
		this.previousTicks = ticks;
	}

	public static void attack(World attacker, World victim, int ticks){
		if(attacker.isOccupied()){
			int ticksPassed = (ticks - attacker.getPreviousTicks());
			double attackMultiplier;
			double attackStrength;

			if(victim.getMode() == WorldMode.NEUTRAL){
				// Set multiplier based on attacking gray
				attackMultiplier = attacker.getMode().getStrengthAgainstGray() * victim.getMode().getDefense();
			} else {
				// Set multiplier based on attacking other
				attackMultiplier = attacker.getMode().getStrength() * victim.getMode().getDefense();
			}
			// Attacker's power can not go to 0 from the attack
			attackStrength = attackMultiplier * ticksPassed;
			if(attacker.getPower() - ticksPassed > 0){
				// Attacker loses power
				attacker.setPower(attacker.getPower() - ticksPassed);
			}
			// Victim loses power and can go to 0
			victim.setPower(victim.getPower() - attackStrength);
		}
	}

	public static void initializeAttack(World attacker, World victim){
		attacker.setAttacking(true);
		attacker.setAttackingWhom(victim);
		victim.setBeingAttacked(true);
		victim.setAttackedBy(attacker);
	}

	public static void cancelAttack(World attacker, World victim){
		victim.setBeingAttacked(false);
		victim.setAttackedBy(null);
		attacker.setAttacking(false);
		attacker.setAttackingWhom(null);
	}

	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D)g;

		// If there is an owner, print that color behind this world
		if(this.getOwner() != null){
			g2.setStroke(new BasicStroke(6));
			g2.setColor(this.getOwner().getColor());
			g2.drawOval(this.getCoords().x, this.getCoords().y, (int)this.getDiameter(), (int)this.getDiameter());
			g2.setStroke(new BasicStroke(1));
		}

		// If attacking, paint attack stream
		if(this.isAttacking()){
			g2.setColor(this.getColor());
			g2.setStroke(new BasicStroke(7));
			g2.draw(new Line2D.Float(this.getCenter().x, this.getCenter().y, getAttackingWhom().getCenter().x, getAttackingWhom().getCenter().y));
			g2.setStroke(new BasicStroke(1));
		}

		// Paint this world
		g.setColor(this.getColor());
		g.fillOval(this.getCoords().x, this.getCoords().y, (int)this.getDiameter(), (int)this.getDiameter());
		// Paint indicator ring
		g.setColor(this.getMode().thisColor.getColor());
		g.drawOval(this.getCoords().x, this.getCoords().y, (int)this.getDiameter(), (int)this.getDiameter());

		// If selected, paint info
		if(this.isSelected()){
			infoBox.paint(g);
		}

		// Paint labels
		if(Game.showLabels){
			// Paint power value on the world
			g.setColor(Color.BLACK);
			g.drawString(""+(int)power,(int)( this.getCoords().x + 0.35 * this.getDiameter()), (int)(this.getCoords().y + 0.58 * this.getDiameter()));

			// Print mode letter for the color blind
			g.drawString(this.getMode().getLabel(), (int)(this.getCoords().x + 0.4 * this.getDiameter()), (int)(this.getCoords().y + 0.9 * this.getDiameter()));
		}

	}

	public void clicked(MouseEvent e){
		boolean anotherSelected = false;
		World selectedWorld = null;
		// If this is going to be selected, deselect all others
		if(!this.isSelected()){
			for(World w : World.worlds){
				if(w.isSelected()){
					anotherSelected = true;
					selectedWorld = w; // this might cause an issue because of the way a for each works
				}
			}
		}

		if(anotherSelected){
			if(selectedWorld.isAttacking()){
				// Cancel attack
				World.cancelAttack(selectedWorld, selectedWorld.getAttackingWhom());
			} else {
				// Attack with this as the victim
				World.initializeAttack(selectedWorld, this);
			}
		} else {
			// Reverse selected
			this.selected = !this.selected;
			// Reverse open of info box
			this.infoBox.reverseOpen();
		}


	}

	// Getters and setters
	public boolean isBeingAttacked() {
		return beingAttacked;
	}

	public void setBeingAttacked(boolean beingAttacked) {
		this.beingAttacked = beingAttacked;
	}

	public boolean isAttacking() {
		return attacking;
	}

	public void setAttacking(boolean attacking) {
		this.attacking = attacking;
	}

	public Point getCoords() {
		return coords;
	}

	public void setCoords(Point coords) {
		this.coords = coords;
	}

	public double getPower() {
		return power;
	}

	public void setPower(double power) {
		if(power >= 0 && power <= World.maxPower){
			this.power = power;
		} else if (power < 0){
			this.power = 0;
		} else if (power > World.maxPower){
			this.power = World.maxPower;
		}
	}

	public double getDiameter() {
		return diameter;
	}

	public void setDiameter(double diameter) {
		this.diameter = diameter;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public boolean isOccupied() {
		return occupied;
	}

	public void setOccupied(boolean occupied) {
		this.occupied = occupied;
	}

	public World getAttackedBy() {
		return attackedBy;
	}

	public void setAttackedBy(World attackedBy) {
		this.attackedBy = attackedBy;
	}

	public World getAttackingWhom() {
		return attackingWhom;
	}

	public void setAttackingWhom(World attackingWhom) {
		this.attackingWhom = attackingWhom;
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public WorldMode getMode() {
		return mode;
	}

	public void setMode(WorldMode mode) {
		this.mode = mode;
		this.setPower(0);
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public WorldInfo getInfoBox() {
		return infoBox;
	}

	public void setInfoBox(WorldInfo infoBox) {
		this.infoBox = infoBox;
	}


	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	public int getPreviousTicks() {
		return previousTicks;
	}

	public void setPreviousTicks(int previousTicks) {
		this.previousTicks = previousTicks;
	}

}
