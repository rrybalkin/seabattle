package app.kmf.seabattle.controller;

import app.kmf.seabattle.core.datamodel.IFleet;
import app.kmf.seabattle.core.logic.strategy.IFleetCreatorStrategy;
import app.kmf.seabattle.core.logic.strategy.IShootingStrategy;
import app.kmf.seabattle.enums.PlayerType;

public class Game {
	protected int gameID;
	protected String playerID;
	protected PlayerType playerType;
	
	protected IFleet fleet;
	protected IFleetCreatorStrategy fleetCreator;
	protected IShootingStrategy shootStrategy;
	
	public Game( int gameID, String playerID, PlayerType playerType ) {
		this.gameID = gameID;
		this.playerID = playerID;
		this.playerType = playerType;
	}
	
	public int getGameID() {
		return gameID;
	}
	
	public String getPlayerID() {
		return playerID;
	}
	
	public PlayerType getPlayerType() {
		return playerType;
	}
	
	public void setFleet(IFleet fleet) {
		this.fleet = fleet;
	}
	
	public void setShootingStrategy(IShootingStrategy strategy) {
		this.shootStrategy = strategy;
	}
	
	public IFleet getFleet() {
		return fleet;
	}
	
	public IShootingStrategy getShootingStrategy() {
		return shootStrategy;
	}
	
	public void setFleetCreator(IFleetCreatorStrategy creator) {
		this.fleetCreator = creator;
	}
	
	public IFleetCreatorStrategy getFleetCreator() {
		return fleetCreator;
	}
}
