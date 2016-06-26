package spotWars;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

public class AIPlayerL1 extends Player implements AIPlayer {

	protected Game game;
	protected Random rand = new Random();

	public AIPlayerL1(Game game) {
		super();
		this.game = game;
	}

	public void makeMoves() {
		ArrayList<World> worldsInRange;
		// For all of this player's worlds
		for (World w : myWorlds) {
			// Find worlds in range
			worldsInRange = game.worldsInRangeOf(w);
			// Decide action based on world's type
			switch (w.getMode()) {
			case NEUTRAL:
				w.setMode(WorldMode.EXPLORATIVE);
				break;
			case OFFENSIVE:
				processOffensive(w, worldsInRange);
				break;
			case DEFENSIVE:
				processDefensive(w, worldsInRange);
				break;
			case EXPLORATIVE:
				processExplorative(w, worldsInRange);
				break;
			}
		}
	}

	// Decide action for an offensive world
	protected void processOffensive(World myWorld,
			ArrayList<World> worldsInRange) {
		// Continue if myWorld is not doing anything (to keep it simple)
		if (myWorld.isIdle()) {
			TreeSet<World> enemies = new TreeSet<World>(World.enemyComparator);
			World victim = null;
			int neutralCount = 0;

			for (World w : worldsInRange) {
				if (w.getOwner() != this && w.getPower() <= myWorld.getPower()) {
					switch (w.getMode()) {
					case OFFENSIVE:
						enemies.add(w);
						break;
					case DEFENSIVE:
						// Offensive avoids attacking defensive without
						// advantage
						if (w.getPower() <= 0.5 * myWorld.getPower()
								|| myWorld.getPower() == 100) {
							enemies.add(w);
						}
						break;
					case EXPLORATIVE:
						enemies.add(w);
						break;
					case NEUTRAL:
						neutralCount++;
						break;
					}
				}
			}

			// Attack the highest priority victim
			if (!enemies.isEmpty()) {
				victim = enemies.first();
				// One Offensive can't defeat a Defensive, so prefer to explore
				// rather than attack defensive sometimes
				if (neutralCount > 0 && victim.getMode() == WorldMode.DEFENSIVE
						&& rand.nextInt(100) < 40) {
					myWorld.setMode(WorldMode.EXPLORATIVE);
				} else {
					World.initializeAttack(myWorld, victim);
				}

			} else if (myWorld.getPower() == 100 && neutralCount > 0) {
				// If there's nothing to attack but worlds to claim, change to
				// explorative
				myWorld.setMode(WorldMode.EXPLORATIVE);
			}
		}
	}

	// Decide action for an defensive world
	protected void processDefensive(World myWorld,
			ArrayList<World> worldsInRange) {
		// Continue if myWorld is not doing anything (to keep it simple)
		if (myWorld.isIdle()) {
			TreeSet<World> enemies = new TreeSet<World>(World.enemyComparator);
			int neutralCount = 0;

			for (World w : worldsInRange) {
				if (w.getOwner() != this && w.getPower() <= myWorld.getPower()) {
					switch (w.getMode()) {
					case OFFENSIVE:
						// Defensive avoids attacking offensive without
						// advantage
						if (w.getPower() <= 0.5 * myWorld.getPower()
								|| myWorld.getPower() == 100) {
							enemies.add(w);
						}
						break;
					case DEFENSIVE:
						enemies.add(w);
						break;
					case EXPLORATIVE:
						// Defensive avoids attacking explorative without
						// advantage
						if (w.getPower() <= 0.5 * myWorld.getPower()
								|| myWorld.getPower() == 100) {
							enemies.add(w);
						}
						break;
					case NEUTRAL:
						neutralCount++;
						break;
					}
				}
			}

			// Attack the highest priority victim
			if (!enemies.isEmpty()) {
				if (enemies.first().getMode() == WorldMode.DEFENSIVE
						&& rand.nextInt(100) < 75) {
					// If defensive encounters enemy defensive, give chance to
					// change to offensive
					myWorld.setMode(WorldMode.OFFENSIVE);
				} else {
					World.initializeAttack(myWorld, enemies.first());
				}
			} else if (myWorld.getPower() == 100 && neutralCount > 0) {
				// If there's nothing to attack but worlds to claim, change to
				// explorative
				myWorld.setMode(WorldMode.EXPLORATIVE);
			}
		}
	}

	// Decide action for an explorative world
	protected void processExplorative(World myWorld,
			ArrayList<World> worldsInRange) {
		// Continue if myWorld is not doing anything (to keep it simple)
		if (myWorld.isIdle()) {
			TreeSet<World> enemies = new TreeSet<World>(World.enemyComparator);
			int offensiveCount = 0;
			int defensiveCount = 0;
			int explorativeCount = 0;

			for (World w : worldsInRange) {
				if (w.getOwner() != this) {
					switch (w.getMode()) {
					case OFFENSIVE:
						offensiveCount++;
						break;
					case DEFENSIVE:
						defensiveCount++;
						break;
					case EXPLORATIVE:
						explorativeCount++;
						break;
					case NEUTRAL:
						if (w.getPower() <= myWorld.getPower()) {
							enemies.add(w);
						}
						break;
					}
				}
			}

			// Attack the highest priority victim (neutral with lowest points)
			if (!enemies.isEmpty()
					&& enemies.first().getMode() == WorldMode.NEUTRAL) {
				World.initializeAttack(myWorld, enemies.first());
			} else {
				if (offensiveCount + defensiveCount + explorativeCount == 0
						&& myWorld.getPower() == 100) {
					if (rand.nextInt(100) < 85) {
						// If only surrounded by friendlies with nothing to take
						// in
						// range, change to defensive (bunker)
						myWorld.setMode(WorldMode.DEFENSIVE);
					} else if (rand.nextInt(100) < 50) {
						// Or chance to go offensive
						myWorld.setMode(WorldMode.OFFENSIVE);
					}
					// Or chance to stay explorative
				} else if (1.25 * explorativeCount > offensiveCount
						|| (defensiveCount > offensiveCount && myWorld
								.getPower() == 100)) {
					// If there are explorative or defensive, change to
					// offensive with preference for destroying explorative
					myWorld.setMode(WorldMode.OFFENSIVE);
				} else if (offensiveCount > 0) {
					// If there is an offensive enemy, go defensive
					myWorld.setMode(WorldMode.DEFENSIVE);
				}
			}
		}
	}
}
