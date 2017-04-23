package app.kmf.test.core.strategy;

import app.kmf.seabattle.core.datamodel.IFleet;
import app.kmf.seabattle.core.logic.strategy.FleetCreatorStrategyFactory;
import app.kmf.seabattle.core.logic.strategy.IFleetCreatorStrategy;

public class TestAutoFleetCreatorStrategy {
	public static void main(String[] args) {
		FleetCreatorStrategyFactory factory = new FleetCreatorStrategyFactory();
		IFleetCreatorStrategy strategy = factory.getAutoStrategyByComplexity(3);
		
		for (int i = 0; i < 10; i++) {
			IFleet fleet = strategy.createFleet();

			System.out.println(fleet.toString());
			
			strategy.reset();
		}
	}
}
