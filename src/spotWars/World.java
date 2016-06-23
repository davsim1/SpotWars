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
	protected LinkedList<World> attackedBy;
	protected LinkedList<World> attackingWhom;
	protected Player owner;
	protected WorldMode mode;
	protected boolean selected;
	protected WorldInfo infoBox;
	protected int previousTicks;
	protected Point center;
	protected boolean transferring;
	protected World transferringTo;
	protected boolean receivingTransfer;
	protected World receivingTransferFrom;

	public static LinkedList<World> worlds = new LinkedList<World>();
	public static final double maxPower = 100;
	public static final double maxTransferPower = 100;
	public static final double startDiameter = 50;
	public static final double transferSpeed = 2;

	// Constructors
	public World(){
		// Set up initial values
		this.coords = new Point(0,0);
		this.infoBox = null;
		this.center = null;
		this.resetVariables();
		//this.setOccupied(true); //Delete this line ******************************************************
		// Add this new world to the list of worlds
		World.worlds.add(this);
	}

	public World(Point coords){
		this();
		this.coords = coords;
		this.infoBox = new WorldInfo(this);
		this.center = new Point((int)(this.coords.x + this.diameter/2), (int)(this.coords.y + this.diameter/2));
	}

	public World(Point coords, Player owner){
		this(coords);
		this.owner = owner;
		this.occupied = true;
	}

	// General methods
	protected void resetVariables(){
		this.beingAttacked = false;
		this.attacking = false;
		this.power = Math.random()*10 + 10;
		this.diameter = World.startDiameter;
		this.color = GameColor.GRAY.getColor();
		this.occupied = false;
		this.attackedBy = new LinkedList<World>();
		this.attackingWhom = new LinkedList<World>();
		//this.owner = new Player(this); // *************************************************
		this.owner = null;
		this.mode = WorldMode.NEUTRAL;
		this.selected = false;
		this.transferring = false;
		this.transferringTo = null;
		this.receivingTransfer = false;
		this.receivingTransferFrom = null;
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
				for(World victim : this.getAttackingWhom()){
					World.attack(this, victim, ticks);
				}
			} else if(!this.isBeingAttacked()){
				// Increase power by 1 for each tick while not being attacked and not attacking
				this.setPower(this.getPower() + (ticks - this.previousTicks));
			}

			if(this.isTransferring()){
				World.transfer(this, this.transferringTo, ticks);
			}
		}

		// Update color (update saturation based off of power)
		this.setColor(new Color(Color.HSBtoRGB(this.getMode().getColor().h, (float)(this.power/World.maxPower), this.getMode().getColor().l)));

		// If being attacked, cancel attack when power reaches 0 and reset world
		if(this.isBeingAttacked()){
			if(this.getPower() <= 0){
				// Save first World to attack this one
				WorldMode attackerMode = this.getAttackedBy().getFirst().getMode();
				Player attackerOwner = this.getAttackedBy().getFirst().getOwner();
				// Cancel attack
				int length = this.getAttackedBy().size();
				for(int i = 0; i < length; i++){
					World.cancelAttack(this.getAttackedBy().get(0), this);
				}
				/*
				System.out.println(this.getAttackedBy().size());
				for(World attacker : this.getAttackedBy()){
					World.cancelAttack(attacker, this);
				}*/
				// Reset and unclaim this world
				this.resetVariables();
				// If the attacker that just defeated this world was explorative
				if(attackerMode == WorldMode.EXPLORATIVE){
					// Claim this world in the name of the victor
					this.setOwner(attackerOwner);
					this.setOccupied(true);
				}
			}
		}

		// If attacking, cancel attack when power reaches 1
		if(this.isAttacking()){
			if(this.getPower() <= 1){
				for(World victim : this.getAttackingWhom()){
					World.cancelAttack(this, victim);
				}
			}
		}

		// If receiving transfer, cancel transfer when power reaches max
		if(this.isReceivingTransfer()){
			if(this.getPower() >= World.maxTransferPower){
				World.cancelTransfer(this.getReceivingTransferFrom(), this);
			}
		}

		// If giving transfer, cancel when power reaches 1
		if(this.isTransferring()){
			if(this.getPower() <= 1){
				World.cancelTransfer(this, this.getTransferringTo());
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

	public static void transfer(World giver, World taker, int ticks){
		if(giver.getOwner() == taker.getOwner()){
			if(giver.getMode() == taker.getMode()){
				int ticksPassed = (ticks - giver.getPreviousTicks());
				giver.setPower(giver.getPower() - ticksPassed * World.transferSpeed);
				taker.setPower(taker.getPower() + ticksPassed * World.transferSpeed);
			}
		}
	}

	public static void initializeAttack(World attacker, World victim){
		if(attacker.getOwner() != victim.getOwner()){
			if(attacker.isOccupied()){
				attacker.setAttacking(true);
				//attacker.setAttackingWhom(victim);
				attacker.getAttackingWhom().add(victim);
				victim.setBeingAttacked(true);
				//victim.setAttackedBy(attacker);
				victim.getAttackedBy().add(attacker);
			}
		}
	}

	public static void cancelAttack(World attacker, World victim){
		/*
		victim.setBeingAttacked(false);
		victim.setAttackedBy(null);
		attacker.setAttacking(false);
		attacker.setAttackingWhom(null);
		*/
		// Take victim out of attacker's list
		if(attacker.getAttackingWhom().contains(victim)){
			if(!attacker.getAttackingWhom().remove(victim))
				System.out.println("error1");
		}
		// If that emptied the attacker's list, set the attacker to not attacking
		if(attacker.getAttackingWhom().isEmpty()){
			attacker.setAttacking(false);
		}
		// Take attacker out of the victim's list
		if(victim.getAttackedBy().contains(attacker)){
			if(!victim.getAttackedBy().remove(attacker))
			System.out.println("error1");
		}
		if(victim.getAttackedBy().isEmpty()){
			victim.setBeingAttacked(false);
		}
		
	}

	public static void initializeTransfer(World giver, World taker){
		if(giver.getOwner() == taker.getOwner()){
		giver.setTransferring(true);
		giver.setTransferringTo(taker);
		taker.setReceivingTransfer(true);
		taker.setReceivingTransferFrom(giver);
		}
	}

	public static void cancelTransfer(World giver, World taker){
		giver.setTransferring(false);
		giver.setTransferringTo(null);
		taker.setReceivingTransfer(false);
		taker.setReceivingTransferFrom(null);
	}

	public void paintL1(Graphics g){
		Graphics2D g2 = (Graphics2D)g;

		// If attacking, paint attack streams
		if(this.isAttacking()){
			g2.setColor(this.getColor());
			g2.setStroke(new BasicStroke(7));
			for(World victim : this.getAttackingWhom()){
				g2.draw(new Line2D.Float(this.getCenter().x, this.getCenter().y, victim.getCenter().x, victim.getCenter().y));
			}
			g2.setStroke(new BasicStroke(1));
		}

		// If transferring, paint attack stream
		if(this.isTransferring()){
			g2.setColor(this.getColor());
			g2.setStroke(new BasicStroke(7));
			g2.draw(new Line2D.Float(this.getCenter().x, this.getCenter().y, getTransferringTo().getCenter().x, getTransferringTo().getCenter().y));
			g2.setStroke(new BasicStroke(1));
		}

	}

	public void paintL2(Graphics g){
		Graphics2D g2 = (Graphics2D)g;

		// If there is an owner, print that color behind this world
		if(this.isOccupied()){
			g2.setStroke(new BasicStroke(6));
			g2.setColor(this.getOwner().getColor());
			g2.drawOval(this.getCoords().x, this.getCoords().y, (int)this.getDiameter(), (int)this.getDiameter());
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
			g.setColor(Color.BLACK);
			// Paint player name on world
			if(this.isOccupied()){
				g.drawString(this.getOwner().getName(), (int)(this.getCoords().x + 0.35 * this.getDiameter()), (int)(this.getCoords().y + 0.25 * this.getDiameter()));
			}
			// Paint power value on the world
			g.drawString(""+(int)power,(int)(this.getCoords().x + 0.35 * this.getDiameter()), (int)(this.getCoords().y + 0.58 * this.getDiameter()));

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
				if(selectedWorld.getAttackingWhom().contains(this)){
					// Cancel attack
					World.cancelAttack(selectedWorld, this);
				}
			} else {
				// Attack with this as the victim
				World.initializeAttack(selectedWorld, this); // THIS IS PREVENTING MULTI ATTACK BECAUSE OF CONDITIONAL (ONLY HAPPENS IF SELECTED IS NOT ATTACKING)
			}
			if(selectedWorld.isTransferring()){
				if(selectedWorld.getTransferringTo() == this){
					World.cancelTransfer(selectedWorld, this);
				}
			} else {
				World.initializeTransfer(selectedWorld, this);
			}
			//if(selectedWorld.getTransferringTo().contains(this)){
			//	World.cancelTransfer(selectedWorld, this);
			//}
		} else {
			// Reverse selected
			if(this.isOccupied()){
				this.selected = !this.selected;
				// Reverse open of info box
				this.infoBox.reverseOpen();
			}
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

	public LinkedList<World> getAttackedBy() {
		return attackedBy;
	}

	public void setAttackedBy(LinkedList<World> attackedBy) {
		this.attackedBy = attackedBy;
	}

	public LinkedList<World> getAttackingWhom() {
		return attackingWhom;
	}

	public void setAttackingWhom(LinkedList<World> attackingWhom) {
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

	public boolean isTransferring() {
		return transferring;
	}

	public void setTransferring(boolean transferring) {
		this.transferring = transferring;
	}

	public World getTransferringTo() {
		return transferringTo;
	}

	public void setTransferringTo(World transferringTo) {
		this.transferringTo = transferringTo;
	}

	public boolean isReceivingTransfer() {
		return receivingTransfer;
	}

	public void setReceivingTransfer(boolean receivingTransfer) {
		this.receivingTransfer = receivingTransfer;
	}

	public World getReceivingTransferFrom() {
		return receivingTransferFrom;
	}

	public void setReceivingTransferFrom(World receivingTransferFrom) {
		this.receivingTransferFrom = receivingTransferFrom;
	}

}
