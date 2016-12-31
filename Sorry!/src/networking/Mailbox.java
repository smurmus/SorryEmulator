package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Mailbox implements Runnable{
	
	private PrintWriter pw;
	private BufferedReader br;
	private Socket s;
	
	private Queue<String> mail;
	private Lock mLock;
	private Condition mCondition;
	
	private volatile boolean running;
	private boolean delete;
	
	private String mName;
	
	{
		mail = new LinkedList<String>();
		mLock = new ReentrantLock();
		mCondition = mLock.newCondition();
		mName = " ";
	}
	
	public Mailbox(String ip, int port) {
		try{
			s = new Socket(ip,port);
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(s.getOutputStream());
		} catch(IOException ioe) {
			ioe.printStackTrace();
			running = false;
		}
	}
	
	public Mailbox(Socket s) {
		this.s = s;
		try {
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(s.getOutputStream());
		} catch (IOException e) {running = false;}
	}
	
	public void start() {
		running = true;
		new Thread(this).start();
	}
	
	public void add(String toAdd) {
		mail.add(toAdd);
	}
	
	public void send(String msg) {
		pw.print(msg+"\n");
		pw.flush();
	}
	
	public boolean hasMail() {
		return !mail.isEmpty();
	}
	
	public String getMail() {
		return mail.remove();
	}
	
	public boolean toDelete() {
		return delete;
	}
	
	private void signal() {
		mLock.lock();
		mCondition.signal();
		mLock.unlock();
	}
	
	public void waitForMail() throws InterruptedException {
		mLock.lock();
		mCondition.await();
		mLock.unlock();
	}

	public void shutDown() {
		delete = true;
		running = false;
		add(MessageConstants.DISCONNECT);
		signal();
	}
	
	@Override
	public void run() {
		while(running) {
			try {
				String msg = br.readLine();
				if(msg == null) shutDown();
				else add(msg);
				signal();
			} catch (Exception e) {
				shutDown();
				signal();
			}
		}
	}
	
	public String toString() {
		return "Mailbox: "+s;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setName(String inName) {
		mName = inName;
	}
}
