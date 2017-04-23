package app.kmf.seabattle.core.logic.strategy;

import app.kmf.seabattle.core.logic.strategy.impl.AutoFleetCreatorStrategyHighComplexity;
import app.kmf.seabattle.core.logic.strategy.impl.AutoFleetCreatorStrategyLowComplexity;
import app.kmf.seabattle.core.logic.strategy.impl.ManualFleetCreatingStrategy;

public class FleetCreatorStrategyFactory {

	public FleetCreatorStrategyFactory() {}

	/**
	 * Creates strategy for auto creating fleet objects by complexity
	 * @param complexity complexity of needed strategy
	 * @return created strategy object
	 */
	public IFleetCreatorStrategy getAutoStrategyByComplexity(int complexity) {
		IFleetCreatorStrategy strategy = null;

		switch (complexity) {
		case 1:
			strategy = new AutoFleetCreatorStrategyLowComplexity();
			break;

		case 2:
			strategy = new AutoFleetCreatorStrategyLowComplexity();
			break;

		case 3:
			strategy = new AutoFleetCreatorStrategyHighComplexity();
			break;
		}

		return strategy;
	}
	
	/**
	 * Creates strategy for manual creating fleet objects by using fleet descriptors
	 * @return created strategy object
	 */
	public IFleetCreatorStrategy getManualStrategy() {
		IFleetCreatorStrategy strategy = new ManualFleetCreatingStrategy();
		
		return strategy;
	}
}
