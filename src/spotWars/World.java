/* David Simmons (GitHub: davsim1)
 *  Date: 6/27/2016
 *	A world that represents a circle on the map.
 */
package spotWars;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.Comparator;
import java.util.LinkedList;

public class World {
	// Variables
	protected boolean beingAttacked;
	protected boolean attacking;
	protected Point coords;
	protected double power;
	protected Color color;
	// Occupied is if this world has an owner.
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
	protected LinkedList<World> transferringTo;
	protected boolean receivingTransfer;
	protected LinkedList<World> receivingTransferFrom;
	protected double range;

	public static final double maxPower = 100;
	public static final double maxTransferPower = 100;
	public static final int diameter = 50;
	public static final int radius = diameter / 2;
	public static final double transferSpeed = 2;
	// A comparator to rank enemies where best to attack come towards the front
	// of the list
	public static final Comparator<World> enemyComparator = new Comparator<World>() {
		@Override
		// Low return value means higher priority to attack
		public int compare(World arg0, World arg1) {
			// Compare powers
			int comp = (int) (arg0.getPower() - arg1.getPower());
			// If they're the same mode, prefer to attack lower power
			if (arg0.getMode() == arg1.getMode()) {
				return comp;
				// If the powers are close, prefer to attack explorative
			} else if (Math.abs(comp) <= 10 && arg0.getMode() == WorldMode.EXPLORATIVE) {
				return -1;
				// If the powers are close, prefer to attack offensive
			} else if (Math.abs(comp) <= 10 && arg0.getMode() == WorldMode.OFFENSIVE) {
				return -1;
			}
			return comp;
		}
	};

	// A comparator to rank allies where best to transfer to come towards the
	// front of the list
	public static final Comparator<World> allyComparator = new Comparator<World>() {
		@Override
		// Low return value means higher priority to transfer power to
		public int compare(World arg0, World arg1) {
			// Compare powers
			int comp = (int) (arg0.getPower() - arg1.getPower());
			// Prefer to transfer to ally that's being attacked
			if (Math.abs(comp) < 30) {
				if (arg0.isBeingAttacked() || arg0.isTransferring()) {
					return -1;
				}
				if (arg1.isBeingAttacked() || arg0.isTransferring()) {
					return 1;
				}
			}
			return comp;
		}
	};

	// Constructors
	public World() {
		// Set up initial values
		this.coords = new Point(0, 0);
		this.infoBox = null;
		this.center = null;
		this.resetVariables();
		// this.setOccupied(true); //Delete this line
		// ******************************************************
	}

	public World(Point coords) {
		this();
		this.coords = coords;
		this.infoBox = new WorldInfo(this);
		this.center = new Point((int) (this.coords.x + World.diameter / 2), (int) (this.coords.y + World.diameter / 2));
	}

	public World(Point coords, Player owner) {
		this(coords);
		setOwner(owner);
		this.occupied = true;
	}

	// General methods
	protected void resetVariables() {
		this.beingAttacked = false;
		this.attacking = false;
		this.power = Math.random() * 9 + 14;
		this.color = GameColor.GRAY.getColor();
		this.occupied = false;
		this.attackedBy = new LinkedList<World>();
		this.attackingWhom = new LinkedList<World>();
		// this.owner = new Player(this); //
		// *************************************************
		setOwner(null);
		this.mode = WorldMode.NEUTRAL;
		this.selected = false;
		this.transferring = false;
		this.transferringTo = new LinkedList<World>();
		this.receivingTransfer = false;
		this.receivingTransferFrom = new LinkedList<World>();
	}

	public void update(int ticks) {
		if (this.getMode() != WorldMode.NEUTRAL) {
			// Update range
			this.setRange(this.getDiameter() * this.getPower() / 10);

			// Perform each attack or transfer
			if (this.isAttacking()) {
				for (World victim : this.getAttackingWhom()) {
					World.attack(this, victim, ticks);
				}
			}
			if (this.isTransferring()) {
				for (World taker : transferringTo) {
					World.transfer(this, taker, ticks);
				}

			}
			if (!this.isBeingAttacked() && !this.isAttacking() && !this.isTransferring()) {
				// Increase power by 1 for each tick while not being attacked
				// and not attacking
				this.setPower(this.getPower() + (ticks - this.previousTicks));
			}

			// If attacking, cancel attack when power reaches 1
			if (this.isAttacking()) {
				if (this.getPower() <= 1) {
					int size = this.getAttackingWhom().size();
					for (int i = 0; i < size; i++) {
						World.cancelAttack(this, this.getAttackingWhom().get(0));
					}
				}
			}

			// If receiving transfer, cancel transfer when power reaches max
			if (this.isReceivingTransfer()) {
				if (this.getPower() >= World.maxTransferPower) {
					int size = this.getReceivingTransferFrom().size();
					for (int i = 0; i < size; i++) {
						World.cancelTransfer(this.getReceivingTransferFrom().get(0), this);
					}
				}
			}

			// If giving transfer, cancel when power reaches 1
			if (this.isTransferring()) {
				if (this.getPower() <= 1) {
					int size = this.getTransferringTo().size();
					for (int i = 0; i < size; i++) {
						World.cancelTransfer(this, this.getTransferringTo().get(0));
					}
				}
			}

		}

		// If being attacked, cancel attack when power reaches 0 and reset world
		if (this.isBeingAttacked()) {
			if (this.getPower() <= 0) {
				// Save first World to attack this one
				WorldMode attackerMode = this.getAttackedBy().getFirst().getMode();
				Player attackerOwner = this.getAttackedBy().getFirst().getOwner();
				// Cancel attack
				int length = this.getAttackedBy().size();
				for (int i = 0; i < length; i++) {
					World.cancelAttack(this.getAttackedBy().get(0), this);
				}
				// Cancel transfers
				length = this.getReceivingTransferFrom().size();
				for (int i = 0; i < length; i++) {
					World.cancelAttack(this.getReceivingTransferFrom().get(0), this);
				}
				/*
				 * System.out.println(this.getAttackedBy().size()); for(World
				 * attacker : this.getAttackedBy()){
				 * World.cancelAttack(attacker, this); }
				 */
				// Reset and unclaim this world
				this.resetVariables();
				// If the attacker that just defeated this world was explorative
				if (attackerMode == WorldMode.EXPLORATIVE) {
					// Claim this world in the name of the victor
					this.setOwner(attackerOwner);
					this.setOccupied(true);
				}
			}
		}

		// Update color (update saturation based off of power)
		this.setColor(new Color(Color.HSBtoRGB(this.getMode().getColor().h, (float) (this.power / World.maxPower),
				this.getMode().getColor().l)));

		// Crucially set current ticks to previous ticks for the next time
		this.previousTicks = ticks;
	}

	public static void attack(World attacker, World victim, int ticks) {
		if (attacker.isOccupied() && attacker.getOwner() != victim.getOwner()) {
			int ticksPassed = (ticks - attacker.getPreviousTicks());
			double attackMultiplier;
			double attackStrength;

			if (victim.getMode() == WorldMode.NEUTRAL) {
				// Set multiplier based on attacking gray
				attackMultiplier = attacker.getMode().getStrengthAgainstGray() * victim.getMode().getDefense();
			} else {
				// Set multiplier based on attacking other
				attackMultiplier = attacker.getMode().getStrength() * victim.getMode().getDefense();
			}
			// Attacker's power can not go to 0 from the attack
			attackStrength = attackMultiplier * ticksPassed;
			if (attacker.getPower() - ticksPassed > 0) {
				// Attacker loses power
				attacker.setPower(attacker.getPower() - ticksPassed);
			}
			// Victim loses power and can go to 0
			victim.setPower(victim.getPower() - attackStrength);
		}
	}

	public static void transfer(World giver, World taker, int ticks) {
		if (giver.getOwner() == taker.getOwner()) {
			if (giver.getMode() == taker.getMode()) {
				int ticksPassed = (ticks - giver.getPreviousTicks());
				giver.setPower(giver.getPower() - ticksPassed * World.transferSpeed);
				taker.setPower(taker.getPower() + ticksPassed * World.transferSpeed);
			}
		}
	}

	public static void initializeAttack(World attacker, World victim) {
		if (attacker.getOwner() != victim.getOwner() && attacker.canReach(victim)) {
			if (attacker.isOccupied() && !attacker.getAttackingWhom().contains(victim)) {
				attacker.setAttacking(true);
				attacker.getAttackingWhom().add(victim);
				victim.setBeingAttacked(true);
				victim.getAttackedBy().add(attacker);
			}
		}
	}

	public boolean canReach(World other) {
		return other.coords.distance(this.coords) <= range;
	}

	public static void cancelAttack(World attacker, World victim) {
		/*
		 * victim.setBeingAttacked(false); victim.setAttackedBy(null);
		 * attacker.setAttacking(false); attacker.setAttackingWhom(null);
		 */
		// Take victim out of attacker's list
		if (attacker.getAttackingWhom().contains(victim)) {
			if (!attacker.getAttackingWhom().remove(victim))
				System.out.println("error1");
		}
		// If that emptied the attacker's list, set the attacker to not
		// attacking
		if (attacker.getAttackingWhom().isEmpty()) {
			attacker.setAttacking(false);
		}
		// Take attacker out of the victim's list
		if (victim.getAttackedBy().contains(attacker)) {
			if (!victim.getAttackedBy().remove(attacker))
				System.out.println("error1");
		}
		if (victim.getAttackedBy().isEmpty()) {
			victim.setBeingAttacked(false);
		}

	}

	public static void initializeTransfer(World giver, World taker) {
		if (giver.getMode() != WorldMode.NEUTRAL && taker.getMode() != WorldMode.NEUTRAL) {
			if (giver.getOwner() == taker.getOwner() && giver.canReach(taker)) {
				if (giver.getMode() == taker.getMode() && !giver.getTransferringTo().contains(taker)) {
					if (giver.getTransferringTo() == null || !giver.getTransferringTo().contains(taker)) {
						giver.setTransferring(true);
						giver.getTransferringTo().add(taker);
						taker.setReceivingTransfer(true);
						taker.getReceivingTransferFrom().add(giver);
					}
				}
			}
		}
	}

	public static void cancelTransfer(World giver, World taker) {
		/*
		 * giver.setTransferring(false); giver.setTransferringTo(null);
		 * taker.setReceivingTransfer(false);
		 * taker.setReceivingTransferFrom(null);
		 */
		// Take taker out of giver's list
		if (giver.getTransferringTo().contains(taker)) {
			if (!giver.getTransferringTo().remove(taker))
				System.out.println("error2");
		}
		// If that emptied the giver's list, set giver to not transferring
		if (giver.getTransferringTo().isEmpty()) {
			giver.setTransferring(false);
		}
		// Take giver out of taker's list
		if (taker.getReceivingTransferFrom().contains(giver)) {
			if (!taker.getReceivingTransferFrom().remove(giver))
				System.out.println("error2");
		}
		// If that emptied the taker's list, set the taker to not receiving
		// transfer
		if (taker.getReceivingTransferFrom().isEmpty()) {
			taker.setReceivingTransfer(false);
		}
	}

	public void paintL1(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		// TODO: paint player colors behind attack streams
		
		// If attacking, paint attack streams
		if (this.isAttacking() || this.isTransferring()) {
			
			for (World victim : this.getAttackingWhom()) {
				g2.setColor(this.getOwner().getColor());
				g2.setStroke(new BasicStroke(11));
				g2.draw(new Line2D.Float(this.getCenter().x, this.getCenter().y, victim.getCenter().x,
						victim.getCenter().y));
				
				g2.setColor(this.getColor());
				g2.setStroke(new BasicStroke(7));
				g2.draw(new Line2D.Float(this.getCenter().x, this.getCenter().y, victim.getCenter().x,
						victim.getCenter().y));
			}
			for (World taker : this.getTransferringTo()) {
				g2.setColor(this.getOwner().getColor());
				g2.setStroke(new BasicStroke(11));
				g2.draw(new Line2D.Float(this.getCenter().x, this.getCenter().y, taker.getCenter().x,
						taker.getCenter().y));
				
				g2.setColor(this.getColor());
				g2.setStroke(new BasicStroke(7));
				g2.draw(new Line2D.Float(this.getCenter().x, this.getCenter().y, taker.getCenter().x,
						taker.getCenter().y));
			}
			g2.setStroke(new BasicStroke(1));
		}

	}

	public void paintL2(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		// TODO: increase player color disk
		// If there is an owner, print that color behind this world
		if (this.isOccupied()) {
			g2.setStroke(new BasicStroke(7));
			g2.setColor(this.getOwner().getColor());
			g2.drawOval(this.getCoords().x, this.getCoords().y, (int) this.getDiameter(), (int) this.getDiameter());
			g2.setStroke(new BasicStroke(1));
		}

		// Paint this world
		g.setColor(this.getColor());
		g.fillOval(this.getCoords().x, this.getCoords().y, (int) this.getDiameter(), (int) this.getDiameter());
		// Paint indicator ring
		g.setColor(this.getMode().thisColor.getColor());
		g.drawOval(this.getCoords().x, this.getCoords().y, (int) this.getDiameter(), (int) this.getDiameter());

		// If selected, paint info
		if (this.isSelected()) {
			infoBox.paint(g);
		}

		// Paint labels
		if (Game.showLabels) {
			g.setColor(Color.BLACK);
			// Paint player name on world
			if (this.isOccupied()) {
				g.drawString(this.getOwner().getName(), (int) (this.getCoords().x + 0.35 * this.getDiameter()),
						(int) (this.getCoords().y + 0.25 * this.getDiameter()));
			}
			// Paint power value on the world
			g.drawString("" + (int) power, (int) (this.getCoords().x + 0.35 * this.getDiameter()),
					(int) (this.getCoords().y + 0.58 * this.getDiameter()));

			// Print mode letter for the color blind
			g.drawString(this.getMode().getLabel(), (int) (this.getCoords().x + 0.4 * this.getDiameter()),
					(int) (this.getCoords().y + 0.9 * this.getDiameter()));
		}

		// Paint range indicator
		if (this.isSelected()) {
			g.setColor(this.getOwner().getColor());
			g.drawOval((int) (this.getCenter().x - this.getRange()), (int) (this.getCenter().y - this.getRange()),
					(int) this.getRange() * 2, (int) this.getRange() * 2);
		}

	}

	public void clicked(MouseEvent e, World selectedWorld, Player p) {
		Boolean anotherSelected = true;
		if (selectedWorld == null || selectedWorld == this) {
			anotherSelected = false;
		}

		if (anotherSelected) {
			if (selectedWorld.isAttacking() && selectedWorld.getAttackingWhom().contains(this)) {
				// Cancel attack against this
				World.cancelAttack(selectedWorld, this);
			} else {
				// Attack with this as the victim
				// THIS IS PREVENTING MULTI ATTACK BECAUSE OF CONDITIONAL (ONLY
				// HAPPENS IF SELECTED IS NOT ATTACKING)
				World.initializeAttack(selectedWorld, this);
			}
			if (getOwner() == p) {
				if (selectedWorld.isTransferring() && selectedWorld.getTransferringTo().contains(this)) {
					// Cancel transfer to this
					World.cancelTransfer(selectedWorld, this);
				} else {
					// Start transfer to this
					World.initializeTransfer(selectedWorld, this);
				}
			}
		} else {
			// Reverse selected
			if (getOwner() == p) {
				if (this.isOccupied()) {
					this.selected = !this.selected;
					// Reverse open of info box
					this.infoBox.reverseOpen();
				}
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
		if (power >= 0 && power <= World.maxPower) {
			this.power = power;
		} else if (power < 0) {
			this.power = 0;
		} else if (power > World.maxPower) {
			this.power = World.maxPower;
		}
	}

	public double getDiameter() {
		return diameter;
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
		cancelAllTransfers();
		cancelAllAttacks();
		if (owner != this.owner) {
			this.setSelected(false);
		}

		if (this.owner != null) {
			this.owner.removeWorld(this);
		}
		this.owner = owner;
		if (owner != null) {
			owner.addWorld(this);
			setOccupied(true);
		}
	}

	public WorldMode getMode() {
		return mode;
	}

	public void setMode(WorldMode mode) {
		cancelAllTransfers();
		cancelOutgoingAttacks();
		if (this.mode == WorldMode.NEUTRAL) {
			this.setPower(this.getPower() / 2);
		} else {
			this.setPower(this.getPower() / 5);
		}
		this.mode = mode;

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
		this.center = new Point((int) (this.coords.x + World.diameter / 2), (int) (this.coords.y + World.diameter / 2));
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

	public LinkedList<World> getTransferringTo() {
		return transferringTo;
	}

	public void setTransferringTo(LinkedList<World> transferringTo) {
		this.transferringTo = transferringTo;
	}

	public boolean isReceivingTransfer() {
		return receivingTransfer;
	}

	public void setReceivingTransfer(boolean receivingTransfer) {
		this.receivingTransfer = receivingTransfer;
	}

	public LinkedList<World> getReceivingTransferFrom() {
		return receivingTransferFrom;
	}

	public void setReceivingTransferFrom(LinkedList<World> receivingTransferFrom) {
		this.receivingTransferFrom = receivingTransferFrom;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public boolean isIdle() {
		return !isTransferring() && !isBeingAttacked() && !isAttacking();
	}

	// Cancel all income and out going transfers
	public void cancelAllTransfers() {
		if (getTransferringTo() != null) {
			for (World w : getTransferringTo()) {
				World.cancelTransfer(this, w);
			}
		}
		if (getReceivingTransferFrom() != null) {
			for (World w : getReceivingTransferFrom()) {
				World.cancelTransfer(w, this);
			}
		}
	}

	// Cancel all attacks
	public void cancelAllAttacks() {
		cancelOutgoingAttacks();
		if (getAttackedBy() != null) {
			for (World w : getAttackedBy()) {
				World.cancelAttack(w, this);
			}
		}
	}

	// Cancel all outgoing attacks
	public void cancelOutgoingAttacks() {
		if (getAttackingWhom() != null) {
			for (World w : getAttackingWhom()) {
				World.cancelAttack(this, w);
			}
		}
	}
}
