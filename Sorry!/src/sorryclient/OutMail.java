package sorryclient;

import networking.Mailbox;

public class OutMail implements Runnable{
	static public Mailbox outBox;
	static private Listener mListener;

	private static volatile boolean running;
	
	public static boolean init(String ip, int port) {
		if(running) return false;
		outBox = new Mailbox(ip, port);
		outBox.start();
		running = true;
		new Thread(new OutMail()).start();
		return true;
	}
	
	public static void send(String type, String msg) {
		outBox.send(type+msg);
	}

	public static void setListener(Listener inListener) {
		mListener = inListener;
	}

	@Override
	public void run() {
		while(running) {
			try {outBox.waitForMail();} catch (InterruptedException e) {}
			while(outBox.hasMail()) {
				mListener.listen(outBox.getMail());
			}
		}
	}

	public static boolean isAlive() {
		return outBox.toDelete();
	}

	public static void reset() {
		running = false;
	}
	
}
