# SpotWars
Author: David Simmons, GitHub: davsim1 (Please preserve my name as the original author in all copies.)  
Status: Unfinished  

A simple game to take over all the spots on a battlefield made in a Java applet.  

The world colors represent:   
Gray: Neutral.  These have yet to be captured or are not generating points for players.  
Green: Explorative. These are the only ones that can capture gray worlds.  
Red: Offensive.  These have standard defense and attack.  It takes more than one red to defeat a blue.  
Blue: Defensive.  These have increased defense but decreased attack.  It takes more than one blue to defeat a red.  

Players select one of their worlds (spots) and choose red, green, or blue for it 
to start generating points.  If A attacks B, A spends points and B loses 
points, and at 0 points, the attacked world becomes unclaimed and gray.  A gray world 
can be claimed by attacking it with a green one.  The objective is to take over gray 
worlds to generate enough points to claim all the worlds.  When you are the only player 
left with worlds, you win.  

Choose a world's color by clicking on it then clicking the color.  Select a world by 
clicking on it, and deselect it by clicking on it or the background.  Only one world per 
player can be selected at a time.  Attack another player's world (or a neutral one) by selecting 
a world and clicking on an enemy's.  One world can attack many worlds as long as they are in range. A 
player can transfer points between worlds of like colors by selecting a world to give points 
then clicking the world to transfer to.  

There can be up to one human player (player 1) and many computer players.  There are 2 levels of computer 
with level 2 being smarter.  Add a new Player() first if you want to play, and add/remove computer 
players (AIPlayerL1 and AIPlayerL2) in the start() method of Game.java.  You can change the number 
of starting worlds by changing makeRandomMap(int) in the start() method().  

To run the game:
In your browser:
1. Clone the repository (all you need is SpotWars.jar and SpotWars.html).
2. Open Internet Explorer and go to java.com. (If you don't have Java for IE)
3. Install Java. (If you don't have Java for IE)
4. Add "file:\\\C:\...SpotWars.html" to your Java security exceptions list (where ... is the full path 
to the file after C:\).
  a. Press the Windows Key.
  b. Search for and open "Configure Java".
  c. Go to the Security tab.
  d. Click "Edit Site List...".
  e. Add the fully qualified path to the html file with "file:\\\" in front.
  f. Hit Ok.
  g. Hit Apply.
5. Run SpotWars.html in IE
6. Click on prompts to allow the application to run.  

OR

Import the project into Eclipse and run Game.java as an applet.

TODO:
* Make more AI levels.
* Find a way to handle input from multiple players.
* Make changes to make it multiplayer.
* Create opening, winning, and losing splash screens.
* Allow users to change settings in a file or on opening screen.
* Make the game accessible to play online.



