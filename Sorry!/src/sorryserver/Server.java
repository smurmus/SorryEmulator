package sorryserver;

import java.util.Random;

import networking.Mailbox;
import networking.Mailroom;
import networking.MessageConstants;

public class Server implements Runnable{

	private ServerGame serverGame;
	private Mailroom mailroom;
//	private SorryServerWindow sorryServerWindow;
	
	private volatile boolean running;
	
	public Server(int port){
		serverGame = new ServerGame();
		mailroom = new Mailroom(port);
		mailroom.start();
//		sorryServerWindow = new SorryServerWindow();
	}
	
	public void start() {
		running = true;
		new Thread(this).start();
//		sorryServerWindow.addMessage("Started server.");
	}
	
	@Override
	public void run() {
		while(running) {
			try {
				Thread.sleep(10);
				mailroom.clean();
				mailroom.lock();
				for(Mailbox m : mailroom.getMailboxList()) {
					processMail(m);
				}
				for(String s : mailroom.getNotificationList()) {
//					sorryServerWindow.addMessage(s);
					if(s.contains(MessageConstants.DISCONNECT)) {
						mailroom.broadCastToAll(s);
						serverGame.refresh();
					}
				}
				mailroom.unlock();
			} catch (InterruptedException e) {}
		}
	}
	
	private void processMail(Mailbox mb) {
		if(mb.hasMail()) {
			String msg = mb.getMail();
//			sorryServerWindow.addMessage("Recieved: " + msg);
			
			if (msg.startsWith(MessageConstants.REQUEST)) {
				String toSend = serverGame.processRequest(mb,msg);
//				sorryServerWindow.addMessage("Sending: " + toSend);
				if(toSend.equals(MessageConstants.NO_RETURN)) return;
				if(toSend.contains(MessageConstants.ALL_READY)) {
					for(Mailbox player : mailroom.getMailboxList()) 
						player.send(MessageConstants.NAME + player.getName());
					mailroom.broadCastToAll(MessageConstants.NUMBER + serverGame.getNumPlaying());
					mailroom.broadCastToAll(MessageConstants.RANDOM_SEED + String.valueOf(new Random().nextInt(100000)));
					mailroom.broadCastToAll(toSend);
				}
				else mailroom.send(mb, toSend);
			}
			else if(msg.startsWith(MessageConstants.BROADCAST)) {
				String toBroadcast = serverGame.processBroadcast(mb,msg);
//				sorryServerWindow.addMessage("Broadcasting: " + toBroadcast);
				mailroom.broadCastToAll(toBroadcast);
			}
		}
	}
	
}
