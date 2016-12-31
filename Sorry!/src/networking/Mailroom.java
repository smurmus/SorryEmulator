package networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Mailroom implements Runnable{
	private Lock mailLock;
	private ArrayList<Mailbox> mailboxList;
	private Queue<String> notifications;
	private ServerSocket ss;
	
	private volatile boolean running;
	
	{
		mailLock = new ReentrantLock();
		mailboxList = new ArrayList<Mailbox>();
		notifications = new LinkedList<String>();
	}
	
	public Mailroom(int port){
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			running = false;
			e.printStackTrace();
		}
	}
	
	public void start() {
		running = true;
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		while(running) {
			try {
				Thread.sleep(10);
				Socket s = ss.accept();
				Mailbox mb = new Mailbox(s);
				mb.start();
				lock();
				mailboxList.add(mb);
				notifications.add("New Connection: " + mb);
				unlock();
				new Thread(mb).start();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void broadCast(Mailbox m, String msg) {
		lock();
		for(Mailbox mb : mailboxList) {
			if(mb != m) mb.send(msg);
		}
		unlock();
	}
	
	public void send(Mailbox mb, String msg) {
		lock();
		mb.send(msg);
		unlock();
	}

	public void broadCastToAll(String msg) {
		lock();
		for(Mailbox mb : mailboxList) {
			mb.send(msg);
		}
		unlock();
	}
	
	public void lock() {mailLock.lock();}
	public void unlock() {mailLock.unlock();}

	public ArrayList<Mailbox> getMailboxList() {
		return mailboxList;
	}
	
	public ArrayList<String> getNotificationList() {
		ArrayList<String> toReturn = new ArrayList<String>();
		while(!notifications.isEmpty()) toReturn.add(notifications.remove());
		return toReturn;
	}

	public void clean() {
		lock();
		Iterator<Mailbox> mbi = mailboxList.iterator();
		while (mbi.hasNext()) {
			Mailbox mb = mbi.next();
			if(mb.toDelete()) {
				notifications.add(MessageConstants.DISCONNECT + mb.getName());
				mbi.remove();
			}
		}
		unlock();
	}
	
}