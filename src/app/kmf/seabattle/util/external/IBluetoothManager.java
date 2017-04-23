package app.kmf.seabattle.util.external;

import java.util.Map;

import app.kmf.seabattle.enums.BluetoothState;
/**
 * This class contains methods for working with Bluetooth adapter of phone.
 * @author Ramsik
 */
public interface IBluetoothManager {
	/**
	 * @return current state of Bluetooth module on phone.
	 */
	public BluetoothState getBluetoothStatus();
	/**
	 * Method for switching ON bluetooth adapter
	 * @return true - if bluetooth was swithed on, false - in another case.
	 */
	public boolean switchON();
	/**
	 * Method for switching OFF bluetooth adapter
	 * @return true - if bluetooth was swithed off, false - in another case.
	 */
	public boolean switchOFF();
	/**
	 * @return list of bounded devices for this phone in type: Name of Device - MAC Adress.
	 */
	public Map<String, String> getListBondedDevices();
	/**
	 * @return list of founded devices in type: Name of Device : MAC Address.
	 */
	public Map<String, String> findAvailableDevices();
	/**
	 * Method for enabling "Server Mode" of this phone.
	 * @return true - if "Server Mode" was enabled, false - in another case.
	 */
	public boolean enableServerMode();
	/**
	 * Method for connecting to another device by bluetooth.
	 * @param adress - adress of cennecting device;
	 * @return true - if connection was set, false - in another case.
	 */
	public boolean connectToDevice(String adress);
	/**
	 * Method for sending message to another device by bluetooth.
	 * @param message - sending message;
	 * @return true - if message was sended, false - in another case.
	 */
	public boolean sendMessage(String message);
	/**
	 * Method for setting handler of messages.
	 * @param handler - object of Handler class.
	 */
	public void setHandler(Object handler);
}
