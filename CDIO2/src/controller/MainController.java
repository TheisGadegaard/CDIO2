package controller;

import java.util.ArrayList;
import java.util.List;

import socket.ISocketController;
import socket.ISocketObserver;
import socket.SocketInMessage;
import socket.SocketOutMessage;
import weight.IWeightInterfaceController;
import weight.IWeightInterfaceObserver;
import weight.KeyPress;
/**
 * MainController - integrating input from socket and ui. Implements ISocketObserver and IUIObserver to handle this.
 * @author Christian Budtz
 * @version 0.1 2017-01-24
 *
 */
public class MainController implements IMainController, ISocketObserver, IWeightInterfaceObserver {

	private ISocketController socketHandler;
	private IWeightInterfaceController weightController;
	private KeyState keyState = KeyState.K1;
	
	private double weight = 0.0;
	private double tarWeight = 0.0;
	private Double total = 0.0;
	private List<Character> numbers = new ArrayList<Character>();
	private int numbersPointer = 0;
	private String numberMessage;
	
	public MainController(ISocketController socketHandler, IWeightInterfaceController weightInterfaceController) {
		this.init(socketHandler, weightInterfaceController);
	}

	@Override
	public void init(ISocketController socketHandler, IWeightInterfaceController weightInterfaceController) {
		this.socketHandler = socketHandler;
		this.weightController=weightInterfaceController;
	}

	@Override
	public void start() {
		if (socketHandler!=null && weightController!=null){
			//Makes this controller interested in messages from the socket
			socketHandler.registerObserver(this);
			//Starts socketHandler in it's own thread
			new Thread(socketHandler).start();
			//weightController setup
			weightController.registerObserver(this);
			//Starts weightController in it's own thread
			new Thread(weightController).start();
		} else {
			System.err.println("No controllers injected!");
		}
	}

	//Listening for socket input
	//When we notify observers, this is the controller that gets the input
	@Override
	public void notify(SocketInMessage message) {
		switch (message.getType()) {
		case B:
			try{
				if (Double.parseDouble(message.getMessage()) < -weight){
					weightController.showMessageSecondaryDisplay("Cant withdraw more weight than currently on weight");
				} else{
					weight = weight + Double.parseDouble(message.getMessage());
					weightController.showMessagePrimaryDisplay(weight+"kg");
					weightController.showMessageSecondaryDisplay("Unmodified total weight:");
				} 
				break;
			}
			catch(NumberFormatException e){
				weightController.showMessageSecondaryDisplay("Error: Wrong format " + e.getMessage());
				break;
			}
		case D:
			weightController.showMessagePrimaryDisplay(message.getMessage()); 
			break;
		case Q:
			quit();
			break;
		case RM204: //Expects an integer reply and does not have to be implemented
			break;
		case RM208: //Expects a string as a reply 
			weightController.showMessageSecondaryDisplay("Enter your operator ID: ");
			break;
		case S:
			total = weight - tarWeight;
			weightController.showMessageSecondaryDisplay("The current weight is:");
			weightController.showMessagePrimaryDisplay(total.toString());
			break;
		case T:
			tara();
			break;
		case DW:
			weightController.showMessagePrimaryDisplay(message.getMessage());
			break;
		case K:
			handleKMessage(message);
			break;
		case P111:
			weightController.showMessageSecondaryDisplay(message.getMessage());
			break;
		}

	}

	private void handleKMessage(SocketInMessage message) {
		switch (message.getMessage()) {
		case "1" :
			this.keyState = KeyState.K1;
			break;
		case "2" :
			this.keyState = KeyState.K2;
			break;
		case "3" :
			this.keyState = KeyState.K3;
			break;
		case "4" :
			this.keyState = KeyState.K4;
			break;
		default:
			socketHandler.sendMessage(new SocketOutMessage("ES"));
			break;
		}
	}
	//Listening for UI input
	@Override
	public void notifyKeyPress(KeyPress keyPress) {
		//TODO implement logic for handling input from ui
		switch (keyPress.getType()) {
		case SOFTBUTTON:
			break;
		case TARA:
			tara();
			break;
		case TEXT:
			numbers.add(keyPress.getCharacter());
			 numbersPointer++;
			 numberMessage = "";
			 for(int i = 0; i<numbersPointer; i++){
				 numberMessage += numbers.get(i);
			 }
			 weightController.showMessageSecondaryDisplay(numberMessage);
			break;
		case ZERO:
			weight = 0.0;
			tarWeight = 0.0;
			total = 0.0;
			weightController.showMessagePrimaryDisplay(total.toString());
			weightController.showMessageSecondaryDisplay(""); 
			break;
		case C:
			numbers = new ArrayList<Character>();
			 numbersPointer = 0;
			 System.out.println("C");
			break;
		case EXIT:
			quit();
			break;
		case SEND:
			if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3) ){
				socketHandler.sendMessage(new SocketOutMessage("K A 3"));
			}
			//Code for sending/resetting numbers
			 numbers = new ArrayList<Character>();
			 numbersPointer = 0;
			 weightController.showMessageSecondaryDisplay("");
			break;
		}

	}

	@Override
	public void notifyWeightChange(double newWeight) {
		this.weight = newWeight; //Set the weight to be equal to the new weight
		weightController.showMessagePrimaryDisplay(weight-tarWeight+"kg"); //Print this to the GUI

	}

	public void tara(){
		tarWeight = weight;
		weightController.showMessagePrimaryDisplay(total.toString());
	}
	
	public void quit(){
		System.exit(0);
	}
	
}
