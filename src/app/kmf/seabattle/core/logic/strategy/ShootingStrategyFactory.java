package app.kmf.seabattle.core.logic.strategy;

import app.kmf.seabattle.core.logic.strategy.impl.AutoShootingStrategyHighComplexity;
import app.kmf.seabattle.core.logic.strategy.impl.AutoShootingStrategyLowComplexity;
import app.kmf.seabattle.core.logic.strategy.impl.AutoShootingStrategyMediumComplexity;

/**
 * This factory for creating shooting strategy objects
 */
public class ShootingStrategyFactory {

	public ShootingStrategyFactory() {}

	/**
	 * This method for getting ISootingStrategy object by complexity
	 * @param complexity - complexity of strategy
	 * @return shooting strategy object
	 */
	public IShootingStrategy getStrategyByComplexity(int complexity) {
		IShootingStrategy strategy = null;

		switch (complexity) {
		case 1:
			strategy = new AutoShootingStrategyLowComplexity();
			break;
		case 2:
			strategy = new AutoShootingStrategyMediumComplexity();
			break;
		case 3:
			strategy = new AutoShootingStrategyHighComplexity();
			break;
		}
		
		return strategy;
	}

}
