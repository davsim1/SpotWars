// David Simmons (GitHub: davsim1)
// Date: 7/8/2014
// TODO: Win status
package spotWars;

import java.applet.*;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Calendar;
import java.util.LinkedList;

public class Game extends Applet implements Runnable, MouseListener {
	private Player p1;
	private Player p2;

	private LinkedList<World> worlds;

	private Image img;
	private Graphics doubleG;

	// Number of milliseconds that must pass to send a tick
	public static final int gameSpeed = 300;
	public static final long startTime = Calendar.getInstance().getTimeInMillis();
	public static final boolean showLabels = true;
	public static final int width = 800;
	public static final int height = 600;
	// How often to check for a win in milliseconds
	public static final int winCheckInterval = 1000;
	private long lastWinCheck;
	private int ticks;

	private static final long serialVersionUID = 2009818433704815839L;

	private World selectedWorld;

	public void init() {
		setSize(Game.width, Game.height);
	}

	public void start() {
		this.p1 = new Player();
		this.p2 = new Player();
		int radius = (int) World.startDiameter / 2;
		int diameter = (int) World.startDiameter;
		this.lastWinCheck = Calendar.getInstance().getTimeInMillis();
		this.worlds = new LinkedList<World>();
		worlds.add(new World(new Point(10, this.getHeight() / 2 - radius), p1));
		worlds.add(new World(new Point(this.getWidth() / 4 + diameter, this.getHeight() / 3 - radius)));
		worlds.add(new World(new Point(this.getWidth() / 4 + diameter, 2 * this.getHeight() / 3 - radius)));
		worlds.add(new World(new Point(this.getWidth() / 2 + diameter, this.getHeight() / 3 - radius)));
		worlds.add(new World(new Point(this.getWidth() / 2 + diameter, 2 * this.getHeight() / 3 - radius)));
		worlds.add(new World(new Point(this.getWidth() / 4 + diameter, 10)));
		worlds.add(new World(new Point(this.getWidth() / 2 + diameter, this.getHeight() - diameter - 10)));
		worlds.add(new World(new Point(this.getWidth() - diameter - 10, this.getHeight() / 2 - radius), p2));

		addMouseListener(this);
		Thread thread = new Thread(this);
		thread.start();
	}

	public void stop() {

	}

	public void destroy() {

	}

	@Override
	public void update(Graphics g) {
		Player winner = null;
		// Update each world
		// TODO: check efficiency of this
		long currentTime = Calendar.getInstance().getTimeInMillis();

		// integer amount of gameSpeed ms that have passed since game start
		ticks = (int) (currentTime - startTime) / gameSpeed;
		for (World w : worlds) {
			w.update(ticks);
		}

		// Set up double buffering
		if (img == null) {
			img = createImage(this.getSize().width, this.getSize().height);
			doubleG = img.getGraphics();
		}

		doubleG.setColor(getBackground());
		doubleG.fillRect(0, 0, this.getSize().width, this.getSize().height);

		doubleG.setColor(getForeground());
		paint(doubleG);

		g.drawImage(img, 0, 0, this);
		// Determine if it's time to check for a victory
		if (currentTime - lastWinCheck >= winCheckInterval) {
			lastWinCheck = currentTime;
			if ((winner = getWinner()) != null) {
				g.drawString(winner.getName() + " WINS!!!", (int)(this.getSize().getWidth() *.45), (int)(this.getSize().getHeight() / 15));
				try {
					Thread.sleep(10000);
					System.exit(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void paint(Graphics g) {
		// Loop through painting layer 1 of each world
		for (World w : worlds) {
			w.paintL1(g);
		}

		// Loop through painting layer 2 of each world
		for (World w : worlds) {
			w.paintL2(g);
		}

	}

	@Override
	public void run() {
		boolean error = false;
		while (!error) {
			repaint();
			try {
				Thread.sleep(17);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				error = true;
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// world x, y coordinates
		int wx;
		int wy;
		// world width, height
		int width;
		int height;
		// click even x, y coordinates
		int ex = e.getX();
		int ey = e.getY();
		boolean wasSelected;

		// Loop through the worlds to see if they were clicked on
		// TODO: Tree or hash table to improve from O(n)
		for (World w : worlds) {
			wx = w.getCoords().x;
			wy = w.getCoords().y;
			height = (int) w.getDiameter();
			width = (int) w.getDiameter();

			if (ex > wx && ex < wx + width) {
				if (ey > wy && ey < wy + height) {
					wasSelected = w.isSelected();
					// if it was clicked on, call its clicked method
					w.clicked(e, selectedWorld);
					// Save if it was selected or unselected
					if (w.isSelected()) {
						selectedWorld = w;
					} else if (wasSelected) {
						selectedWorld = null;
					}
				}
			}
		}

		// Loop through the world infos to see if they were clicked on
		// TODO: Tree or hash table to improve from O(n)
		for (WorldInfo i : WorldInfo.worldInfos) {
			if (i.isOpen()) {
				if (ex > i.getCoords().x && ex < i.getCoords().x + i.getWidth()) {
					if (ey > i.getCoords().y && ey < i.getCoords().y + i.getHeight()) {
						// if it was clicked on, call it's clicked method
						i.clicked(e);
					}
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	public Player getWinner() {
		Player winner = worlds.get(0).getOwner();
		for (World w : worlds) {
			if (w.getOwner() != winner) {
				winner = null;
				break;
			}
		}
		return winner;
	}
}
