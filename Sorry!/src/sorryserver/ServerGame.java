package sorryserver;

import java.util.ArrayList;
import java.util.Iterator;

import networking.Mailbox;
import networking.MessageConstants;

public class ServerGame {
	public static final int NONE = 0;
	public static final int NUM_PLAYER_SELECT = 1;
	public static final int COLOR_SELECT = 2;
	public static final int GAME = 3;
	private int gameState;
	
	private Mailbox host;
	
	private int numPlayers;
	private ArrayList<Mailbox> players;
	private ArrayList<Mailbox> readyPlayers;
	
	{
		gameState = NONE;
		players = new ArrayList<Mailbox>();
		readyPlayers = new ArrayList<Mailbox>();
	}
	
	public String processRequest(Mailbox player, String msg) {
		String[] split = msg.split(MessageConstants.SPLIT);
		if(split.length < 2) return "";
		if(split[1].equals(MessageConstants.HOST)) {
			if(gameState != NONE) return MessageConstants.FAILED;
			if(host == null) {
				host = player;
				players.add(host);
				gameState = NUM_PLAYER_SELECT;
				return MessageConstants.HOST + MessageConstants.SUCCESS;
			} else return MessageConstants.FAILED;
		} else if (split[1].equals(MessageConstants.JOIN)) {
			if(gameState != COLOR_SELECT) return MessageConstants.FAILED;
			if(players.size() < numPlayers) {
				players.add(player);
				String colors = "";
				for(Mailbox p : players) colors += p.getName();
				return MessageConstants.JOIN + MessageConstants.SUCCESS + colors;
			} else return MessageConstants.FAILED;
		} else if (split[1].equals(MessageConstants.NUM_PLAYER_SELECT)){
			if(gameState != NUM_PLAYER_SELECT) return MessageConstants.FAILED;
			if(host != player) return MessageConstants.FAILED;
			numPlayers = Integer.valueOf(split[2]);
			gameState = COLOR_SELECT;
			return MessageConstants.NUM_PLAYER_SELECT + MessageConstants.SUCCESS;
		} else if (split[1].equals(MessageConstants.READY)) {
			if(gameState != COLOR_SELECT) return MessageConstants.FAILED;
			readyPlayers.add(player);
			if(readyPlayers.size() == numPlayers) {
				String toReturn = MessageConstants.ALL_READY;
				for(Mailbox mb : readyPlayers) {
					toReturn += mb.getName();
				}
				return toReturn;
			}
			else return MessageConstants.NO_RETURN;
		}
		return MessageConstants.FAILED;
	}
	
	public String processBroadcast(Mailbox player, String msg) {
		String[] split = msg.split(MessageConstants.SPLIT);
		if(split[1].equals(MessageConstants.COLOR_SELECT)) {
			for(Mailbox mb : players) {
				if(mb.getName().equals(split[2])) return MessageConstants.NO_RETURN;
			}
			String toReturn = MessageConstants.COLOR_SELECT + player.getName() + split[2];
			player.setName(split[2]);
			player.send(MessageConstants.COLOR_SELECT+MessageConstants.SUCCESS);
			return toReturn;
		}
		if(split[1].equals(MessageConstants.CHAT_MESSAGE)) {
			return MessageConstants.CHAT_MESSAGE + player.getName() + "|" + split[2];
		}
		if(split[1].equals(MessageConstants.CLICK)) {
			return MessageConstants.CLICK + player.getName() + "|" + split[2] + split[3];
		}
		if(split[1].equals(MessageConstants.DRAW_CARD)) {
			return MessageConstants.DRAW_CARD;
		}
		if(split[1].equals(MessageConstants.END_TURN)) {
			return MessageConstants.END_TURN;
		}
		if(split[1].equals(MessageConstants.SEVEN_SPLIT)) {
			return MessageConstants.SEVEN_SPLIT + split[2];
		}
		if(split[1].equals(MessageConstants.TEN_MOVE_CHOICE)) {
			return MessageConstants.TEN_MOVE_CHOICE + split[2];
		}
		if(split[1].equals(MessageConstants.SWAP_TILE)) {
			return MessageConstants.SWAP_TILE + split[2] + split[3];
		}
		return "";
	}

	public String getNumPlaying() {
		return ""+numPlayers;
	}
	
	public void refresh() {
		Iterator<Mailbox> mbi = players.iterator();
		while (mbi.hasNext()) {
			Mailbox mb = mbi.next();
			if(mb.toDelete()) {
				mbi.remove();
			}
		}
		Iterator<Mailbox> mbiReady = readyPlayers.iterator();
		while (mbiReady.hasNext()) {
			Mailbox mb = mbiReady.next();
			if(mb.toDelete()) {
				mbiReady.remove();
			}
		}
	}
	
}
