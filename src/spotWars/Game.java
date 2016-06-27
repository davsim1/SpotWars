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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class Game extends Applet implements Runnable, MouseListener {
	private LinkedList<Player> players;
	private LinkedList<World> worlds;

	private Image img;
	private Graphics doubleG;

	// Number of milliseconds that must pass to send a tick
	public static final int gameSpeed = 300;
	public static final long startTime = Calendar.getInstance().getTimeInMillis();
	public static final boolean showLabels = true;
	public static final int width = 1000;
	public static final int height = 800;
	// Interval for the AI to plan and make moves in ticks
	public static final int aIUpdateRate = 13;
	// How often to check for a win in milliseconds
	public static final int winCheckInterval = 2500;

	private long lastWinCheck;
	private int ticks;
	private int prevTicks;

	private static final long serialVersionUID = 2009818433704815839L;

	private World selectedWorld;
	private Player selectedOriginalOwner;

	private Random rand = new Random(System.currentTimeMillis());

	public void init() {
		setSize(Game.width, Game.height);
	}

	public void start() {
		this.players = new LinkedList<Player>();
		players.add(new AIPlayerL1(this));
		players.add(new AIPlayerL2(this));

		this.lastWinCheck = Calendar.getInstance().getTimeInMillis();
		this.worlds = new LinkedList<World>();

		makeRandomMap(14);
		addPlayersToMap(players);

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

		if (ticks - prevTicks >= aIUpdateRate) {
			for (Player p : players) {
				if (p instanceof AIPlayerL1) {
					((AIPlayerL1) p).makeMoves();
				}
			}
			prevTicks = ticks;
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
				g.drawString(winner.getName() + " WINS!!!", (int) (this.getSize().getWidth() * .45),
						(int) (this.getSize().getHeight() / 15));
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
		boolean worldClicked = false;

		// If the selected world gets taken, reset it
		if (selectedWorld != null && selectedWorld.getOwner() != selectedOriginalOwner) {
			selectedWorld = null;
		}

		// Loop through the worlds to see if they were clicked on
		// TODO: Tree or hash table to improve from O(n)
		for (World w : worlds) {
			wx = w.getCoords().x;
			wy = w.getCoords().y;
			height = (int) w.getDiameter();
			width = (int) w.getDiameter();

			if (ex > wx && ex < wx + width) {
				if (ey > wy && ey < wy + height) {
					worldClicked = true;
					wasSelected = w.isSelected();
					// if it was clicked on, call its clicked method
					w.clicked(e, selectedWorld, players.getFirst());
					// Save if it was selected or unselected
					if (w.getOwner() == players.getFirst()) {
						if (w.isSelected()) {
							selectedWorld = w;
							selectedOriginalOwner = w.getOwner();
						} else if (wasSelected) {
							selectedWorld = null;
							selectedOriginalOwner = null;
						}
					}
				}
			}
		}

		if (!worldClicked && selectedWorld != null) {
			selectedWorld.setSelected(false);
			selectedWorld = null;
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

	public void pruneLosers() {
		if (players != null) {
			Iterator<Player> iter = players.iterator();
			while (iter.hasNext()) {
				if (iter.next().getMyWorlds().isEmpty()) {
					iter.remove();
				}
			}
		}
	}

	public Player getWinner() {
		pruneLosers();
		Player winner = players.getFirst();
		
		for (World w : worlds) {
			if (w.getOwner() != null && w.getOwner() != winner) {
				winner = null;
				break;
			}
		}
		return winner;
	}

	public ArrayList<World> worldsInRangeOf(World origin) {
		ArrayList<World> result = new ArrayList<World>();

		for (World w : worlds) {
			if (origin.getCoords().distance(w.getCoords()) <= origin.range) {
				result.add(w);
			}
		}

		return result;
	}

	public void makeRandomMap(int numWorlds) {
		Point trialLocation = new Point();
		int infLoopGuard = 0;
		for (int i = 0; i < numWorlds; i++) {
			do {
				trialLocation.x = rand.nextInt(getWidth() - 2 * World.diameter) + World.diameter;
				trialLocation.y = rand.nextInt(getHeight() - 2 * World.diameter) + World.diameter;
				if (infLoopGuard++ > 1000) {
					throw new IllegalStateException("infinite loop while making random map");
				}
			} while (tooCloseToNeighbor(trialLocation));

			worlds.add(new World(new Point(trialLocation)));
		}
	}

	public boolean tooCloseToNeighbor(Point p) {
		for (World w : worlds) {
			if (p.distance(w.getCoords()) < 3 * World.diameter) {
				return true;
			}
		}
		return false;
	}

	public void addPlayersToMap(Collection<Player> players) {
		World trialHome;
		int infLoopGuard = 0;
		for (Player p : players) {
			do {
				trialHome = worlds.get(rand.nextInt(worlds.size()));
				if (infLoopGuard++ > 1000) {
					throw new IllegalStateException("infinite loop while making random map");
				}
			} while (trialHome.getOwner() != null || adjacentToAnotherPlayer(trialHome));
			trialHome.setOwner(p);
		}
	}

	public World nearest(World origin) {
		World neighbor = null;
		for (World w : worlds) {
			if (w != origin && (neighbor == null || origin.getCoords().distance(w.getCoords()) < origin.getCoords()
					.distance(neighbor.getCoords()))) {
				neighbor = w;
			}
		}

		return neighbor;
	}

	public boolean adjacentToAnotherPlayer(World origin) {
		if (worlds.size() >= 2 * players.size()) {
			World neighbor = nearest(origin);
			if (neighbor != null && neighbor.getOwner() != origin.getOwner()) {
				return true;
			}
		}
		return false;
	}

	public void generateWorldSetup1() {
		worlds.add(new World(new Point(10, this.getHeight() / 2 - World.radius), players.get(0)));
		worlds.add(new World(
				new Point((int) (this.getWidth() / 4 + World.diameter * 1.5), this.getHeight() / 3 - World.radius)));
		worlds.add(new World(new Point((int) (this.getWidth() / 4 + World.diameter * 1.5),
				2 * this.getHeight() / 3 - World.radius)));
		worlds.add(new World(
				new Point((int) (this.getWidth() / 2 + World.diameter * 1.5), this.getHeight() / 3 - World.radius)));
		worlds.add(new World(new Point((int) (this.getWidth() / 2 + World.diameter * 1.5),
				2 * this.getHeight() / 3 - World.radius)));
		worlds.add(new World(new Point((int) (this.getWidth() / 4 + World.diameter * 1.5), 10)));
		worlds.add(new World(
				new Point((int) (this.getWidth() / 2 + World.diameter * 1.5), this.getHeight() - World.diameter - 10)));
		worlds.add(new World(new Point(this.getWidth() - World.diameter - 10, this.getHeight() / 2 - World.radius),
				players.get(1)));
	}
}
