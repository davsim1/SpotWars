package spotWars;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeSet;

public class AIPlayer extends Player {

	protected Game game;

	public AIPlayer(Game game) {
		super();
		this.game = game;
	}

	public void makeMoves(int ticks) {
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
				processOffensive(w, worldsInRange, ticks);
				break;
			case DEFENSIVE:
				processDefensive(w, worldsInRange, ticks);
				break;
			case EXPLORATIVE:
				processExplorative(w, worldsInRange, ticks);
				break;
			}
		}
	}

	// TODO: add ability for AI to do more than one action per world
	// TODO: add ability for AI to initiate transfers
	// TODO: add ability for AI to change world mode

	// Decide action for an offensive world
	private void processOffensive(World myWorld, ArrayList<World> worldsInRange, int ticks) {
		// Continue if myWorld is not doing anything (to keep it simple)
		if (myWorld.isIdle()) {
			TreeSet<World> potentialVictims = new TreeSet<World>(World.worldStrengthComparator);
			int neutralCount = 0;

			for (World w : worldsInRange) {
				if (w.getOwner() != this && w.getPower() <= myWorld.getPower()) {
					switch (w.getMode()) {
					case OFFENSIVE:
						potentialVictims.add(w);
						break;
					case DEFENSIVE:
						// Offensive avoids attacking defensive without
						// advantage
						if (w.getPower() <= 0.5 * myWorld.getPower() || myWorld.getPower() == 100) {
							potentialVictims.add(w);
						}
						break;
					case EXPLORATIVE:
						potentialVictims.add(w);
						break;
					case NEUTRAL:
						neutralCount++;
						break;
					}
				}
			}

			// Attack the highest priority victim
			if (!potentialVictims.isEmpty()) {
				World.initializeAttack(myWorld, potentialVictims.first());
			} else if (myWorld.getPower() == 100 && neutralCount > 0) {
				// If there's nothing to attack but worlds to claim, change to
				// explorative
				myWorld.setMode(WorldMode.EXPLORATIVE);
			}
		}
	}

	// Decide action for an defensive world
	private void processDefensive(World myWorld, ArrayList<World> worldsInRange, int ticks) {
		// Continue if myWorld is not doing anything (to keep it simple)
		if (myWorld.isIdle()) {
			TreeSet<World> potentialVictims = new TreeSet<World>(World.worldStrengthComparator);
			int neutralCount = 0;

			for (World w : worldsInRange) {
				if (w.getOwner() != this && w.getPower() <= myWorld.getPower()) {
					switch (w.getMode()) {
					case OFFENSIVE:
						// Defensive avoids attacking offensive without
						// advantage
						if (w.getPower() <= 0.5 * myWorld.getPower() || myWorld.getPower() == 100) {
							potentialVictims.add(w);
						}
						break;
					case DEFENSIVE:
						potentialVictims.add(w);
						break;
					case EXPLORATIVE:
						// Defensive avoids attacking explorative without
						// advantage
						if (w.getPower() <= 0.5 * myWorld.getPower() || myWorld.getPower() == 100) {
							potentialVictims.add(w);
						}
						break;
					case NEUTRAL:
						neutralCount++;
						break;
					}
				}
			}

			// Attack the highest priority victim
			if (!potentialVictims.isEmpty()) {
				World.initializeAttack(myWorld, potentialVictims.first());
			} else if (myWorld.getPower() == 100 && neutralCount > 0) {
				// If there's nothing to attack but worlds to claim, change to
				// explorative
				myWorld.setMode(WorldMode.EXPLORATIVE);
			}
		}
	}

	// Decide action for an explorative world
	private void processExplorative(World myWorld, ArrayList<World> worldsInRange, int ticks) {
		// Continue if myWorld is not doing anything (to keep it simple)
		if (myWorld.isIdle()) {
			TreeSet<World> potentialVictims = new TreeSet<World>(World.worldStrengthComparator);
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
							potentialVictims.add(w);
						}
						break;
					}
				}
			}

			// Attack the highest priority victim (neutral with lowest points)
			if (!potentialVictims.isEmpty()) {
				World.initializeAttack(myWorld, potentialVictims.first());
			} else {
				if (offensiveCount + defensiveCount + explorativeCount == 0 && myWorld.getPower() == 100) {
					// If only surrounded by friendlies with nothing to take in
					// range, change to defensive (bunker)
					myWorld.setMode(WorldMode.DEFENSIVE);
				} else if (1.25 * explorativeCount > offensiveCount || defensiveCount > offensiveCount) {
					// If there are explorative or defensive enemies, change to
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
