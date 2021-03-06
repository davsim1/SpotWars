/* David Simmons (GitHub: davsim1)
 * Date: 7/8/2014
 * A WorldInfo is the box to select color that appears next to a selected world.
 */
package spotWars;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class WorldInfo { 
	protected String label1;
	protected String label2;
	protected String label3;

	protected Point coords;
	protected double width;
	protected double height;
	protected int borderSize;

	protected int buttonWidth;
	protected int buttonHeight;
	protected Point buttonCoords1;
	protected Point buttonCoords2;
	protected Point buttonCoords3;

	protected World thisWorld;

	protected boolean open;

	public static LinkedList<WorldInfo> worldInfos = new LinkedList<WorldInfo>();

	// Constructors
	public WorldInfo() {
		this.coords = new Point(0, 0);
		this.width = 66;
		this.height = 41;
		this.borderSize = 3;

		this.buttonCoords1 = new Point(0, 0);
		this.buttonCoords2 = new Point(0, 0);
		this.buttonCoords3 = new Point(0, 0);
		this.thisWorld = null;

		this.open = false;
		worldInfos.add(this);
	}

	public WorldInfo(World world) {
		this();
		this.thisWorld = world;

		// Set dimensions
		this.width = World.diameter;
		this.height = World.diameter * 0.61;

		// Set coordinates
		// potential x, y
		int px = (int) (thisWorld.coords.x + World.diameter);
		int py = (int) (thisWorld.coords.y + World.diameter);

		// Set coordinates making sure it won't appear off screen
		if (px + this.getWidth() >= Game.width) {
			this.coords.x = (int) (thisWorld.coords.x - thisWorld.getDiameter());
		} else {
			this.coords.x = px;
		}

		if (py + this.getHeight() >= Game.height) {
			this.coords.y = (int) (thisWorld.coords.y - this.getHeight());
		} else {
			this.coords.y = py;
		}
		this.buttonWidth = (int) ((width - borderSize * 3) / 3);
		this.buttonHeight = (int) ((height - borderSize * 2));
		this.buttonCoords1 = new Point(coords.x + borderSize, coords.y + borderSize);
		this.buttonCoords2 = new Point(buttonCoords1.x + buttonWidth + borderSize, buttonCoords1.y);
		this.buttonCoords3 = new Point(buttonCoords2.x + buttonWidth + borderSize, buttonCoords1.y);
	}

	// General methods
	public void paint(Graphics g) {
		// Paint background
		g.setColor(GameColor.GRAY.getColor());
		g.fillRect(coords.x, coords.y, (int) width + 1, (int) height);

		// Paint buttons
		// offensive
		g.setColor(GameColor.RED.getColor());
		g.fillRect(buttonCoords1.x, buttonCoords1.y, buttonWidth, buttonHeight);

		// explorative
		g.setColor(GameColor.GREEN.getColor());
		g.fillRect(buttonCoords2.x, buttonCoords2.y, buttonWidth, buttonHeight);

		// defensive
		g.setColor(GameColor.BLUE.getColor());
		g.fillRect(buttonCoords3.x, buttonCoords3.y, buttonWidth, buttonHeight);
	}

	public void reverseOpen() {
		this.open = !this.open;
	}

	public void clicked(MouseEvent e) {
		// If clicked on red
		if (e.getX() >= buttonCoords1.getX() && e.getX() <= buttonCoords1.getX() + buttonWidth) {
			thisWorld.setMode(WorldMode.OFFENSIVE);
		}

		// If clicked on green
		if (e.getX() >= buttonCoords2.getX() && e.getX() <= buttonCoords2.getX() + buttonWidth) {
			thisWorld.setMode(WorldMode.EXPLORATIVE);
		}

		// If clicked on blue
		if (e.getX() >= buttonCoords3.getX() && e.getX() <= buttonCoords3.getX() + buttonWidth) {
			thisWorld.setMode(WorldMode.DEFENSIVE);
		}

	}

	// Getters and Setters
	public Point getCoords() {
		return coords;
	}

	public void setCoords(Point coords) {
		this.coords = coords;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
}
