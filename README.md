# SpotWars
Author: David Simmons, GitHub: davsim1 (Please preserve my name as the original author in all copies.)
Status: Unfinished

A simple game to take over all the spots on a battlefield made in a Java applet.

Players select one of their worlds (spots) and choose red, green, or blue for it 
to start generating points.  If A attacks B, A spends points and B loses 
points, and at 0 points, the attacked world becomes unclaimed and gray.  The objective is 
to take over gray worlds to generate enough points to take the enemies' worlds.  When
you have taken all of the enemies' worlds, you win.  

The colors represent: 
Gray: Neutral.  These have yet to be captured and controlled by players. 
Green: Explorative. These are the only ones that can capture gray worlds. 
Red: Offensive.  These have standard defense and attack.  It takes two red to defeat a blue. 
Blue: Defensive.  These have increased defense but decreased attack. 

TODO:
* Decide on attack range limitation.
* Do a periodic check for a winner.
* Make usage guide.
* Randomize map generation.
* Find a way to handle input from multiple players.
* Make the game accessible to play online.



