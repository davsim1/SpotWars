package spotWars;

import java.applet.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Calendar;

public class Game extends Applet implements Runnable, MouseListener{

	private World world1;
	private World world2;
	private World world3;
	private World world4;
	private World world5;
	private World world6;

	private Image img;
	private Graphics doubleG;

	// Number of milliseconds that must pass to send a tick
	public static final int gameSpeed = 200;
	public static final long startTime = Calendar.getInstance().getTimeInMillis();
	public static final boolean showLabels = true;
	public static final int width = 800;
	public static final int height = 600;
	private int ticks;

	private static final long serialVersionUID = 2009818433704815839L;

	public void init(){
		setSize(Game.width, Game.height);
	}

	public void start(){
		int radius = (int)World.startDiameter/2;
		int diameter = (int)World.startDiameter;
		this.world1 = new World(new Point(10, this.getHeight()/2 - radius));
		this.world2 = new World(new Point(this.getWidth()/4 + diameter, this.getHeight()/3 - radius));
		this.world3 = new World(new Point(this.getWidth()/4 + diameter, 2*this.getHeight()/3 - radius));
		this.world4 = new World(new Point(this.getWidth()/2 + diameter, this.getHeight()/3 - radius));
		this.world5 = new World(new Point(this.getWidth()/2 + diameter, 2*this.getHeight()/3 - radius));
		this.world6 = new World(new Point(this.getWidth() - diameter - 10, this.getHeight()/2 - radius));
		
		addMouseListener(this);
		Thread thread = new Thread(this);
		thread.start();
	}

	public void stop(){

	}

	public void destroy(){

	}

	@Override
	public void update(Graphics g) {
		// Update each world
		long currentTime = Calendar.getInstance().getTimeInMillis();
		// integer amount of gameSpeed ms that have passed since game start
		ticks = (int)(currentTime - startTime) / gameSpeed;
		for(World w : World.worlds){
			w.update(ticks);
		}

		// Set up double buffering
		if(img == null){
			img = createImage(this.getSize().width, this.getSize().height);
			doubleG = img.getGraphics();
		}

		doubleG.setColor(getBackground());
		doubleG.fillRect(0, 0, this.getSize().width, this.getSize().height);

		doubleG.setColor(getForeground());
		paint(doubleG);

		g.drawImage(img,0,0,this);
	}

	public void paint (Graphics g)
	{
		// Loop through painting layer 1 of each world
		for(World w : World.worlds){
			w.paintL1(g);
		}

		// Loop through painting layer 2 of each world
		for(World w : World.worlds){
			w.paintL2(g);
		}

	}

	@Override
	public void run() {
		boolean error = false;
		while(!error){
			repaint();
			try {
				Thread.sleep(17);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				error=true;
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

		// Loop through the worlds to see if they were clicked on
		for(World w : World.worlds){
			wx = w.getCoords().x;
			wy = w.getCoords().y;
			height = (int)w.getDiameter();
			width = (int)w.getDiameter();

			if(ex > wx && ex < wx+width){
				if(ey > wy && ey < wy+height){
					// if it was clicked on, call it's clicked method
					w.clicked(e);
				}
			}
		}

		// Loop through the world infos to see if they were clicked on
		for(WorldInfo i : WorldInfo.worldInfos){
			if(i.isOpen()){
				if(ex > i.getCoords().x && ex < i.getCoords().x + i.getWidth()){
					if(ey > i.getCoords().y && ey < i.getCoords().y + i.getHeight()){
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
		// TODO Auto-generated method stub
		Point loc = e.getLocationOnScreen();
	}
}
