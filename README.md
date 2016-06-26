# SpotWars
Author: David Simmons, GitHub: davsim1 (Please preserve my name as the original author in all copies.)  
Status: Unfinished  

A simple game to take over all the spots on a battlefield made in a Java applet.  

The world colors represent:   
Gray: Neutral.  These have yet to be captured and controlled by players.  
Green: Explorative. These are the only ones that can capture gray worlds.  
Red: Offensive.  These have standard defense and attack.  It takes more than one red to defeat a blue.  
Blue: Defensive.  These have increased defense but decreased attack.  It takes more than one blue to defeat a red.  

Players select one of their worlds (spots) and choose red, green, or blue for it 
to start generating points.  If A attacks B, A spends points and B loses 
points, and at 0 points, the attacked world becomes unclaimed and gray.  A gray world
can be claimed by attacking it with a green one.  The objective is to take over gray
worlds to generate enough points to claim all the worlds.  When you have taken all 
of the worlds on the map, you win.  

Choose a world's color by clicking on it then clicking the color.  Select a world by 
clicking on it, and deselect it by clicking off of it.  Only one world can be selected 
at a time.  Attack another player's world (or a neutral one) by selecting a world and 
clicking on an enemy's.  One world can attack many worlds as long as they are in range. A 
player can transfer points between worlds of like colors by selecting a world to give 
then clicking the world to transfer to.

TODO:
* Find a way to handle input from multiple players.
* Make changes to make it multiplayer.
* Create opening, winning, and losing splash screens.
* Allow users to change settings in a file or on opening screen.
* Make the game accessible to play online.



