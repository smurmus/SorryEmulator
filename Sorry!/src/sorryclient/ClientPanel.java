package sorryclient;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;

import javax.swing.JPanel;

import game.GameManager;
import library.ImageLibrary;
import networking.MessageConstants;
import sorryserver.Server;

public class ClientPanel extends JPanel implements Listener {
	private static final long serialVersionUID = 6415716059554739910L;
	
	private MainMenu mainMenu;
	private HostPanel hostPanel;
	private JoinPanel joinPanel;
	private NumPlayerSelector numPlayerSelect;
	private ColorSelector colorSelect;
	private WaitPanel waitPanel;
	private GamePanel gamePanel;
	private ChatBox chatBox;
	
//	public Timer colorTimer; 
	
	private GameManager gameManager;
	
	private String mName;
	private int numPlaying;
	
	{
		mainMenu = new MainMenu(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				ClientPanel.this.removeAll();
				ClientPanel.this.add(hostPanel);
				ClientPanel.this.revalidate();
				ClientPanel.this.repaint();
			}
		}, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ClientPanel.this.removeAll();
				ClientPanel.this.add(joinPanel);
				ClientPanel.this.revalidate();
				ClientPanel.this.repaint();
			}	
		}, ImageLibrary.getImage("images/panels/grey_panel.png"));
		
		refreshComponents();
		setLayout(new BorderLayout());
		add(mainMenu);
	}
	
	private void refreshComponents() {
				
		hostPanel = new HostPanel(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int port = hostPanel.getPort();
				new Server(port).start();
				OutMail.init("localhost",port);
				OutMail.setListener(ClientPanel.this);
				OutMail.send(MessageConstants.REQUEST,MessageConstants.HOST);
			}
		},ImageLibrary.getImage("images/panels/grey_panel.png"));
		
		joinPanel = new JoinPanel(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int port = joinPanel.getPort();
				String ip = joinPanel.getIP();
				OutMail.init(ip,port);
				OutMail.setListener(ClientPanel.this);
				OutMail.send(MessageConstants.REQUEST, MessageConstants.JOIN);
				colorSelect.startTimer();
			}
		},ImageLibrary.getImage("images/panels/grey_panel.png"));
		
		numPlayerSelect = new NumPlayerSelector(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int numPlayers = numPlayerSelect.getNumberOfPlayers();
				OutMail.send(MessageConstants.REQUEST,MessageConstants.NUM_PLAYER_SELECT+numPlayers);
				colorSelect.startTimer();
			}
		},ImageLibrary.getImage("images/panels/grey_panel.png"));
		
		colorSelect = new ColorSelector(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				swap(waitPanel);
				OutMail.send(MessageConstants.REQUEST, MessageConstants.READY);
			}
		},ImageLibrary.getImage("images/panels/grey_panel.png"));
		
		waitPanel = new WaitPanel(ImageLibrary.getImage("images/panels/grey_panel.png"));
		
		gameManager = new GameManager();
		gamePanel = new GamePanel(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				swap(mainMenu);
				refreshComponents();
			}
		}, gameManager, ImageLibrary.getImage("images/sorry.png"));
	}

	@Override
	public void listen(String msg) {
		String split[] = msg.split(MessageConstants.SPLIT);
		if(msg.contains(MessageConstants.JOIN+MessageConstants.SUCCESS)) {
			swap(colorSelect);
			colorSelect.colorReserve(split[2]);
		}
		if(msg.equals(MessageConstants.HOST+MessageConstants.SUCCESS)) {
			swap(numPlayerSelect);
		}
		if(msg.equals(MessageConstants.NUM_PLAYER_SELECT+MessageConstants.SUCCESS)) {
			swap(colorSelect);
		}
		if(msg.contains(MessageConstants.DISCONNECT)) {
			if(!OutMail.isAlive()) {
				colorSelect.free(split[1]);
				if(chatBox != null) chatBox.addMessage(split[1], "has left the game.", true);
				gameManager.deactivatePlayer(split[1]);
			} else  {
				OutMail.reset();
				refreshComponents();
				swap(mainMenu);
			}
		}
		if(msg.contains(MessageConstants.COLOR_SELECT)) {
			colorSelect.colorSwap(msg);
		}
		if(msg.contains(MessageConstants.NAME)) {
			mName = msg.split(MessageConstants.SPLIT)[1];
		}
		if(msg.contains(MessageConstants.NUMBER)) {
			numPlaying = Integer.valueOf(msg.split(MessageConstants.SPLIT)[1]);
		}
		if(msg.contains(MessageConstants.RANDOM_SEED)) {
			gameManager.setCardDeckSeed(Integer.valueOf(msg.split(MessageConstants.SPLIT)[1]));
		}
		if(msg.contains(MessageConstants.ALL_READY)) {
			gameManager.setUp(
					mName, 
					numPlaying,
					msg.split(MessageConstants.SPLIT)[1].split("(?=[A-Z])")
				);
			swap(gamePanel);
			chatBox = new ChatBox();
			add(chatBox,BorderLayout.SOUTH);
			revalidate();
			repaint();
		}
		if(msg.contains(MessageConstants.CHAT_MESSAGE)) {
			chatBox.addMessage(split[1].replace("|", ""),split[2].replace("|", ""),false);
		}
		if(msg.contains(MessageConstants.DRAW_CARD)) {
			gameManager.drawCard();
		}
		if(msg.contains(MessageConstants.CLICK)) {
			gameManager.simulateClick(split[1].replace("|", ""), Integer.valueOf(split[2].replace("|", "")), Integer.valueOf(split[3].replace("|", "")));
		}
		if(msg.contains(MessageConstants.END_TURN)) {
			gameManager.nextPlayer();
		}
		if(msg.contains(MessageConstants.SEVEN_SPLIT)) {
			gameManager.setSevenSplit(Integer.valueOf(split[1].replace("|","")));
		}
		if(msg.contains(MessageConstants.TEN_MOVE_CHOICE)) {
			gameManager.setTenMoveChoice(Integer.valueOf(split[1].replace("|", "")));
		}
		if(msg.contains(MessageConstants.SWAP_TILE)) {
			System.out.println(msg);
			gameManager.setSwapTile(Integer.valueOf(split[1].replace("|", "")), Integer.valueOf(split[2].replace("|", "")));
		}
	}

	private void swap(JPanel c) {
		removeAll();
		add(c);
		revalidate();
		repaint();
	}
	
}
