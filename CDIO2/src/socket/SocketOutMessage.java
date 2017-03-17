package socket;

public class SocketOutMessage {

	private String message;
	private boolean commandStatus;

	public SocketOutMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	public boolean commandStatus(){
		return commandStatus;
	}


}
