package spotWars;

import java.util.ArrayList;
import java.util.TreeSet;

public class AIPlayerL2 extends AIPlayerL1 implements AIPlayer {

	public AIPlayerL2(Game game) {
		super(game);
	}

	protected void processTransfers(World myWorld, TreeSet<World> allies) {
		// Initiate transfer to ally that's being attacked or has the lowest
		// power if its power is less than half of this world's
		if (!allies.isEmpty()
				&& allies.first().getPower() < myWorld.getPower() / 2) {
			World.initializeTransfer(myWorld, allies.first());
		}

		// Cancel transfer to any allies with high enough power
		if (myWorld.isTransferring()) {
			for (World w : myWorld.getTransferringTo()) {
				if (w.getPower() > myWorld.getPower() * (0.75)) {
					World.cancelTransfer(myWorld, w);
				}
			}
		}
	}

	// TODO: add ability for AI to do more than one action per world
	// TODO: add ability for AI to initiate transfers

	// Decide action for an offensive world
	protected void processOffensive(World myWorld,
			ArrayList<World> worldsInRange) {
		TreeSet<World> enemies = new TreeSet<World>(World.enemyComparator);
		TreeSet<World> allies = new TreeSet<World>(World.allyComparator);
		World victim = null;
		int neutralCount = 0;

		for (World w : worldsInRange) {
			if (w.getPower() <= myWorld.getPower()) {
				switch (w.getMode()) {
				case OFFENSIVE:
					if (w.getOwner() == this) {
						allies.add(w);
					} else {
						enemies.add(w);
					}
					break;
				case DEFENSIVE:
					// Offensive avoids attacking defensive without
					// advantage
					if (w.getOwner() != this
							&& (w.getPower() <= 0.5 * myWorld.getPower() || myWorld
									.getPower() == 100)) {
						enemies.add(w);
					}
					break;
				case EXPLORATIVE:
					if (w.getOwner() != this) {
						enemies.add(w);
					}
					break;
				case NEUTRAL:
					if (w.getOwner() != this) {
						neutralCount++;
					}
					break;
				}
			}
		}

		// Attack the highest priority victim
		if (!enemies.isEmpty()) {
			victim = enemies.first();
			// One Offensive can't defeat a Defensive, so prefer to explore
			// rather than attack defensive 50% of the time
			if (neutralCount > 0 && victim.getMode() == WorldMode.DEFENSIVE
					&& rand.nextInt(100) < 50) {
				myWorld.setMode(WorldMode.EXPLORATIVE);
			} else {
				World.initializeAttack(myWorld, victim);
			}

		} else if (myWorld.getPower() == 100 && neutralCount > 0
				&& rand.nextInt(100) < 25) {
			// If there's nothing to attack but worlds to claim, change to
			// explorative, but leave chance to stay offensive for transfers
			myWorld.setMode(WorldMode.EXPLORATIVE);
		}

		processTransfers(myWorld, allies);
	}

	// Decide action for an defensive world
	protected void processDefensive(World myWorld,
			ArrayList<World> worldsInRange) {
		TreeSet<World> enemies = new TreeSet<World>(World.enemyComparator);
		TreeSet<World> allies = new TreeSet<World>(World.allyComparator);
		int neutralCount = 0;

		for (World w : worldsInRange) {
			if (w.getPower() <= myWorld.getPower()) {
				switch (w.getMode()) {
				case OFFENSIVE:
					// Defensive avoids attacking offensive without
					// advantage
					if (w.getOwner() != this
							&& (w.getPower() <= 0.5 * myWorld.getPower() || myWorld
									.getPower() == 100)) {
						enemies.add(w);
					}
					break;
				case DEFENSIVE:
					if (w.getOwner() != this) {
						enemies.add(w);
					} else {
						allies.add(w);
					}
					break;
				case EXPLORATIVE:
					// Defensive avoids attacking explorative without
					// advantage
					if (w.getOwner() != this
							&& (w.getPower() <= 0.5 * myWorld.getPower() || myWorld
									.getPower() == 100)) {
						enemies.add(w);
					}
					break;
				case NEUTRAL:
					if (w.getOwner() != this) {
						neutralCount++;
					}
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
		
		processTransfers(myWorld, allies);
	}

	// Decide action for an explorative world
	protected void processExplorative(World myWorld,
			ArrayList<World> worldsInRange) {
		TreeSet<World> enemies = new TreeSet<World>(World.enemyComparator);
		TreeSet<World> allies = new TreeSet<World>(World.allyComparator);
		int offensiveCount = 0;
		int defensiveCount = 0;
		int explorativeCount = 0;
		int size = 0;

		for (World w : worldsInRange) {

			switch (w.getMode()) {
			case OFFENSIVE:
				if (w.getOwner() != this) {
					offensiveCount++;
				}
				break;
			case DEFENSIVE:
				if (w.getOwner() != this) {
					defensiveCount++;
				}
				break;
			case EXPLORATIVE:
				if (w.getOwner() != this) {
					explorativeCount++;
				} else {
					allies.add(w);
				}
				break;
			case NEUTRAL:
				if (w.getPower() <= myWorld.getPower()) {
					enemies.add(w);
				}
				break;
			}
		}

		// Attack up to 3 unclaimed neutral worlds
		if (!enemies.isEmpty()) {
			size = enemies.size();
			for (int i = 0; i < Math.min(size, 3); i++) {
				World.initializeAttack(myWorld, enemies.pollFirst());
			}
		} else {
			if (offensiveCount + defensiveCount + explorativeCount == 0
					&& myWorld.getPower() == 100) {
				// If only surrounded by friendlies with nothing to take in
				// range, change to defensive (bunker)
				myWorld.setMode(WorldMode.DEFENSIVE);
			} else if (1.25 * explorativeCount > offensiveCount
					|| (defensiveCount > offensiveCount && myWorld.getPower() == 100)) {
				// If there are explorative or defensive, change to
				// offensive with preference for destroying explorative
				myWorld.setMode(WorldMode.OFFENSIVE);
			} else if (offensiveCount > 0) {
				// If there is an offensive enemy, go defensive
				myWorld.setMode(WorldMode.DEFENSIVE);
			}
		}

		processTransfers(myWorld, allies);
	}

}
