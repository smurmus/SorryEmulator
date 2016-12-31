package sorryclient;

import java.util.TimerTask;

public class ColorTask extends TimerTask{

	public void run(){
		//send disconnect message 
		OutMail.outBox.shutDown();
	}
}
