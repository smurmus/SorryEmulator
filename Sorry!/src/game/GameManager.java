package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JLabel;
import javax.swing.Timer;

import customUI.CardDialog;
import customUI.ClearPanel;
import game.CardDeck.Card;
import library.FontLibrary;
import networking.MessageConstants;
import sorryclient.GamePanel;
import sorryclient.OutMail;

/*
 * GameManager
 * Actual logic for the game play
 * */

public class GameManager {
	private final static String filePath = "src/game/board";
	private final GameBoard mGameBoard;
	
	private AudioInputStream audioInputStream;
	private AudioInputStream playersTurn;
	private AudioInputStream yourTurn; 
	
	

//	public MediaPlayer mp1;
	
	//The players (can be idle or not)
	private final static int numPlayers = 4;
	private final Player players[];
	
	//The card deck to be used.
	private final CardDeck cardDeck;
	
	//The actual players playing the game
	private Player livePlayers[];
	private int currentPlaying;
	
	//Main player information
	private int mainPlayer = 0;
	public Player getMainPlayer(){return livePlayers[mainPlayer];}
	
	//Info for two-step moves
	private Tile swapTile = null;
	private int sevenSplit = -1;
	private int sevenRemainder = -1;
	private int tenMoveChoice = -1;
	
	//for animating pawns
	private long starting, ending, time, pause; 
	boolean interrupt; 
	
	//The current drawn card, and helper
	private Card card;
	private boolean canDraw = true;
	
	//Winning information
	private boolean gameOver = false;
	public boolean isGameOver() {return gameOver;}
	private String winnerName = null;
	public String getWinner() {return winnerName;}
	
	//GamePanel to update
	private GamePanel mGamePanel;
	
	//Selected tile for movement
	Tile mSelectedTile;
	
	//Load in the players, board, and card deck used for play
	{
		players = new Player[numPlayers];
		currentPlaying = 0;
		for(int i = 0; i < numPlayers; ++i) {
			players[i] = new Player(GameHelpers.getColorFromIndex(i));
		}
		mGameBoard = new GameBoard(new File(filePath),players);
		cardDeck = new CardDeck();
		
		//timer GUI
//		timeLabel = new JLabel(timerString);
//		timeLabel.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, 28));
//						
//		timePanel = new ClearPanel();
//		timePanel.setOpaque(false);
////		timePanel.setBorder(new EmptyBorder(spacing));
//		timePanel.add(timeLabel);
	}
	
	//Set up once we know the players color and number selections
	public void setUp(Color playerColor, int numLivePlayers) {
		livePlayers = new Player[numLivePlayers];
		int mainPlayerIndex = GameHelpers.getIndexFromColor(playerColor);
		livePlayers[mainPlayer] = players[mainPlayerIndex];
		switch(numLivePlayers) {
			case 4:
				livePlayers[3] = players[(mainPlayerIndex+3)%numPlayers];
			case 3:
				livePlayers[1] = players[(mainPlayerIndex+1)%numPlayers];
				livePlayers[2] = players[(mainPlayerIndex+2)%numPlayers];
			break;
			case 2:
				livePlayers[1] = players[(mainPlayerIndex+(numPlayers/2))%numPlayers];
			break;
		}
		
	}
	
	public boolean isCurrentPlayer(){
		return currentPlaying == mainPlayer;
	}
	
	public void setUp(String playerColor, int numLivePlayers, String[] playingList) {
		livePlayers = new Player[numLivePlayers];
		for(int i = 0; i < numLivePlayers; i++) {
			livePlayers[i] = players[GameHelpers.getIndexFromColorName(playingList[i])];
			if(playingList[i].equals(playerColor)) {
				mainPlayer = i;
			}
		}
	}
	
	//Returns number of pawns at the given player's start
	public String getPlayerStartCount(int mPlayerNum) {
		int count = 0;
		for(Pawn p: players[mPlayerNum].getPawns()) {
			if(p.getCurrentTile().isStart()) count++;
		}
		return ""+count;
	}
	
	//Return the number of pawns at the given player's home
	public String getPlayerHomeCount(int mPlayerNum) {
		int count = 0;
		for(Pawn p: players[mPlayerNum].getPawns()) {
			if(p.getCurrentTile().isHome()) count++;
		}
		return ""+count;
	}
	
	//Gets a specific tile from the board, used for graphical elements
	public Tile getTile(int x, int y) {
		return mGameBoard.getTile(x, y);
	}
	
	//Draw a card from the deck
	public void drawCard() {
		
		if(!canDraw) return;
		//card draw
				try {
					audioInputStream = AudioSystem.getAudioInputStream(new File("Sounds/draw_card.wav").getAbsoluteFile());
					Clip clip = AudioSystem.getClip();
			        clip.open(audioInputStream);
			        clip.start();
				} catch (UnsupportedAudioFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		card = cardDeck.drawCard();
		canDraw = false;
		CardDialog.popup(card.getType());
		
		//If the player cannot make a move, just skip
		if(!canMakeMove()){
			System.out.println("is this happening?");
			endTurn();
		}
	}
	
	public void requestCardDrawn() {
		if(mainPlayer != currentPlaying) return;
		OutMail.send(MessageConstants.BROADCAST, MessageConstants.DRAW_CARD);
	}
	
	public void requestClicked(Tile t, Player p) {
		String toSend = MessageConstants.CLICK + t.getX() + "|" + t.getY();
		OutMail.send(MessageConstants.BROADCAST, toSend);
	}
	
	public void simulateClick(String playerName, int x, int y) {
		Tile t = mGameBoard.getTile(x, y);
		Player p = players[GameHelpers.getIndexFromColorName(playerName)];
		tileClicked(t,p);
	}
	
	//Test if the current player can make a move or not.
	private boolean canMakeMove() {
		if(card.getType() == CardDeck.ONE || card.getType() == CardDeck.TWO) return true;
		else if(card.getType() == CardDeck.SORRY) {
			if(livePlayers[currentPlaying].hasPawnsAtStart()) {
				for(Player opponent : livePlayers) {
					if(opponent != livePlayers[currentPlaying]) {
						for(Pawn pawn : opponent.getPawns()) {
							if(!pawn.getCurrentTile().isStart() && !pawn.getCurrentTile().isHome() && !pawn.getCurrentTile().isSafeZone()) return true;
						}
					}
				}
			}
		} else {
			for(Pawn pawn : livePlayers[currentPlaying].getPawns()) {
				Tile currentTile = pawn.getCurrentTile();
				if(!currentTile.isStart() && !currentTile.isHome()) return true;
			}
		}
		return false;
	}
	
	//End the player turn
	public void endTurn() {
		System.out.println("STOP THE TIMER");
		mGamePanel.stopTimer();
		if(gameOver) return;
		//Update the board graphically
		mSelectedTile = null;
		mGameBoard.resetSelections();
		mGamePanel.redraw();
		canDraw = true;
		
		//reset temp variables
		swapTile = null;
		sevenSplit = -1;
		sevenRemainder = -1;
		tenMoveChoice = -1;
		
		//Check if the game should end
		boolean allHome = true;
		for(Pawn pawn : livePlayers[currentPlaying].getPawns()) {
			if(!pawn.getCurrentTile().isHome()) allHome = false;
		}
		if(allHome) {
			Color player = livePlayers[currentPlaying].getColor();
			if(player.equals(Color.RED)) winnerName = "Red";
			else if(player.equals(Color.GREEN)) winnerName = "Green";
			else if(player.equals(Color.YELLOW)) winnerName = "Yellow";
			else if(player.equals(Color.BLUE)) winnerName = "Blue";
			gameOver = true;
			int score = 0;
			for(Pawn playerPawn : livePlayers[mainPlayer].getPawns()) {
				if(playerPawn.getCurrentTile().isHome()) score += 5;
			}
			for(Player p : livePlayers) {
				if(p == livePlayers[mainPlayer]) continue;
				for(Pawn opponentPawn : p.getPawns()) {
					if(!opponentPawn.getCurrentTile().isHome()) score += 3;
					if(opponentPawn.getCurrentTile().isStart()) score += 1;
				}
			}
			//check if the winning player is the mainplayer
			if(winnerName == GameHelpers.getNameFromColor(livePlayers[mainPlayer].getColor())){
				//then play the winning noise?
				try {
					playersTurn = AudioSystem.getAudioInputStream(new File("Sounds/victory.wav").getAbsoluteFile());
					Clip clip = AudioSystem.getClip();
			        clip.open(playersTurn);
			        clip.start();
				} catch (UnsupportedAudioFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				//then play the losing noise?
				try {
					playersTurn = AudioSystem.getAudioInputStream(new File("Sounds/lose.wav").getAbsoluteFile());
					Clip clip = AudioSystem.getClip();
			        clip.open(playersTurn);
			        clip.start();
				} catch (UnsupportedAudioFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			mGamePanel.endGame(winnerName, score);
			return;
		}
		
		//If the card was a two, let the player have another turn
		//Otherwise advance
		if(card.getType() != CardDeck.TWO) {
			//tell the server we are done
			if(currentPlaying == mainPlayer) {
				OutMail.send(MessageConstants.BROADCAST, MessageConstants.END_TURN);
			} else if (!livePlayers[currentPlaying].isActive()) {
				nextPlayer();
			}
		} else if(!livePlayers[currentPlaying].isActive()) {
			if(!gameOver)
			simulatePlayer(livePlayers[currentPlaying]);
		}
		
		//Clean up for next player
		clean();
		set();
		card = null;
	}
	
	public void nextPlayer() {
		currentPlaying++;
		currentPlaying %= livePlayers.length;
		if(currentPlaying == mainPlayer){
			
			try {
				yourTurn = AudioSystem.getAudioInputStream(new File("Sounds/your_turn.wav").getAbsoluteFile());
				Clip clip = AudioSystem.getClip();
		        clip.open(yourTurn);
		        clip.start();
			} catch (UnsupportedAudioFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Simulate the other players if they are a robot
		//System.out.println("Checking if " + livePlayers[currentPlaying].getColor() + "is active");
		if(!livePlayers[currentPlaying].isActive()) {
			if(!gameOver)
			simulatePlayer(livePlayers[currentPlaying]);
		}
	}

	//Will draw the card for the robot, and make their moves.
	private void simulatePlayer(Player player) {
		
		drawCard();
		// open the sound file as a Java input stream
//	    String gongFile = "/Users/al/DevDaily/Projects/MeditationApp/resources/gong.au";
//	    InputStream in = new FileInputStream(gongFile);
//	 
//	    // create an audiostream from the inputstream
//	    AudioInputStream audioStream = new AudioInputStream(in);
//	 
//	    // play the audio clip with the audioplayer class
//	    AudioPlayer.player.start(audioStream);
//	    
		for(Pawn pawn : player.getPawns()) {
			if(tileClicked(pawn.getCurrentTile(),player)) break;
		} 
		if(sevenRemainder != -1)
			for(Pawn pawn : player.getPawns()) {
				if(tileClicked(pawn.getCurrentTile(),player))break;
			}
		if(card != null) endTurn();
	}
	
	//The logic for a tile being clicked
	public boolean tileClicked(Tile tile, Player player) {
		mGamePanel.stopTimer();
//		if(!canMakeMove()) return false; 
		//Check basic impossibilities
		if(card == null) return false;
		if(tile == null) return false;
		if(!livePlayers[currentPlaying].equals(player)) return false;

		//If we are dealing with a sorry card
		if(card.getType() == CardDeck.SORRY) {
			//For the robots, find another pawn and replace it
			//if(currentPlaying != mainPlayer) {
			if(!livePlayers[currentPlaying].isActive()) {
				Pawn playerPawn = player.getAvailablePawn();
				Pawn opponentPawn = null;
				for(Player opponent : livePlayers) {
					if(opponent != livePlayers[currentPlaying]) {
						for(Pawn pawn : opponent.getPawns()) {
							if(!pawn.getCurrentTile().isStart() && !pawn.getCurrentTile().isHome() && !pawn.getCurrentTile().isSafeZone()){
								opponentPawn = pawn;
								break;
							}
						}
						if(opponentPawn != null) break;
					}
				}
				Tile toSet = opponentPawn.getCurrentTile();
				players[GameHelpers.getIndexFromColor(opponentPawn.getColor())].returnPawn(opponentPawn);
				toSet.setPawn(playerPawn);
				endTurn();
				return true;
			} else {
				//If human, check if the selection of the opponent is valid
				if(tile.isOccupied()) {
					if(!tile.isOccupiedByColor(player.getColor()) && !tile.isSafeZone()) {
						Pawn playerPawn = player.getAvailablePawn();
						Pawn opponentPawn = tile.getPawn();
						players[GameHelpers.getIndexFromColor(opponentPawn.getColor())].returnPawn(opponentPawn);
						tile.setPawn(playerPawn);
						endTurn();
						return true;
					}
				}
				return false;
			}
		}
		
		//If we are making a swap with 11
		if(swapTile != null) {
			//Swap the pawns
			if(card.getType() == CardDeck.ELEVEN) {
				if(tile.isOccupied()) {
					if(!tile.isOccupiedByColor(swapTile.getPawnColor())) {
						Pawn playerPawn = swapTile.getPawn();
						Pawn opponentPawn = tile.getPawn();
						tile.setPawn(playerPawn);
						swapTile.setPawn(opponentPawn);
						swapTile = null;
					}
				}
			}
			if(swapTile == null) {
				endTurn();
				return true;
			} else return false;
		}
		
		//Get conditions that must be true
		boolean start = tile.isStart() && tile.isColor(player.getColor()) && player.hasPawnsAtStart() && (card.getType() == CardDeck.ONE || card.getType() == CardDeck.TWO);
		boolean owned = tile.isOccupiedByColor(player.getColor());
		
		if(!start && !owned) return false;
		
		//Check to see if we have a valid pawn to work with
		Pawn pawn;
		if(!start) pawn = tile.getPawn();
		else pawn = player.getAvailablePawn();
		if(pawn == null) return false;
		
		//If the card is a seven, and we have remaining moves, use the remainder on the pawn
		if(card.getType() == CardDeck.SEVEN && sevenRemainder != -1) {
			
			pause = 250; 
			starting = 0;
			interrupt = false; 
			Tile endTile = null; 
			Tile temp = tile; 
			for(int i = 0; i < sevenRemainder; i++){
//				mGamePanel.redraw();

				System.out.println("move #: " + i);
				endTile = travel(temp, pawn, 1);
				pawn.setCurrentTile(endTile);
				endTile.setPawn(pawn);
				temp.removePawn();
				temp = pawn.getCurrentTile();
				
				mGamePanel.redraw();
				
				try {
					starting = System.currentTimeMillis();
					Thread.sleep(pause);
				} catch (InterruptedException e) {
					interrupt = true; 

					ending = System.currentTimeMillis();
					time = ending - starting;
					pause -= time; 
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(interrupt){
					Thread.currentThread().interrupt();
				}
			}
			
			
//			Tile endTile = travel(tile, pawn, sevenRemainder);
			if(endTile == null) return false;
			endTile = slide(endTile,pawn);
			if(endTile.isOccupiedByColor(pawn.getColor())) {
				if(endTile.getPawn() != pawn) return false;
			}
			if(endTile.isOccupied()) players[GameHelpers.getIndexFromColor(endTile.getPawnColor())].returnPawn(endTile.getPawn());
			tile.removePawn();
			if(!endTile.isHome()){
				endTile.setPawn(pawn);
			} else pawn.setCurrentTile(endTile);
			sevenRemainder = -1;
			endTurn();
			return true;
		}//If a normal forward move, simply move the pawn forward
		else if(card.getType() == CardDeck.ONE || card.getType() == CardDeck.TWO ||
			card.getType() == CardDeck.THREE || card.getType() == CardDeck.FIVE ||
			card.getType() == CardDeck.EIGHT || card.getType() == CardDeck.TWELVE ) {
			
			Tile endTile = animate(CardDeck.getValue(card), tile, pawn);
			
//			pause = 250; 
//			starting = 0;
//			interrupt = false; 
//			Tile endTile = null; 
//			Tile temp = tile; 
//			
//			int max = card.getType();
//			
//			if(card.getType() == 8){
//				max++;
//			}
//			else if(card.getType() == 12){
//				max += 2;
//			}
//			
//			for(int i = 0; i <= max; i++){
////				mGamePanel.redraw();
//
//				System.out.println("move #: " + i);
//				endTile = travel(temp, pawn, 1);
//				pawn.setCurrentTile(endTile);
//				endTile.setPawn(pawn);
//				temp.removePawn();
//				temp = pawn.getCurrentTile();
//				
//				mGamePanel.redraw();
//				
//				try {
//					starting = System.currentTimeMillis();
//					Thread.sleep(pause);
//				} catch (InterruptedException e) {
//					interrupt = true; 
//
//					ending = System.currentTimeMillis();
//					time = ending - starting;
//					pause -= time; 
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				if(interrupt){
//					Thread.currentThread().interrupt();
//				}
//			}
			
//			Tile endTile = travel(tile, pawn, CardDeck.getValue(card));
			if(endTile == null) return false;
			Tile beforeTile = endTile;
			endTile = slide(endTile,pawn);
			if(endTile.isOccupiedByColor(pawn.getColor())) {
				if(endTile.getPawn() != pawn) return false;
			}
			if(endTile.isOccupied()) players[GameHelpers.getIndexFromColor(endTile.getPawnColor())].returnPawn(endTile.getPawn());
			tile.removePawn();
			if(!endTile.isHome()){
				endTile.setPawn(pawn);
			} else pawn.setCurrentTile(endTile);
			if(beforeTile != endTile) slideRemove(beforeTile, pawn.getColor());
		}//If the card is four, do as before, but negate the travel amount
		else if (card.getType() == CardDeck.FOUR){
			
			Tile endTile = animate(-CardDeck.getValue(card), tile, pawn);
			
//			pause = 250; 
//			starting = 0;
//			interrupt = false; 
//			Tile endTile = null; 
//			Tile temp = tile; 
//			for(int i = 0; i <= card.getType(); i++){
////				mGamePanel.redraw();
//
//				System.out.println("move #: " + i);
//				endTile = travel(temp, pawn, -1);
//				pawn.setCurrentTile(endTile);
//				endTile.setPawn(pawn);
//				temp.removePawn();
//				temp = pawn.getCurrentTile();
//				
//				mGamePanel.redraw();
//				
//				try {
//					starting = System.currentTimeMillis();
//					Thread.sleep(pause);
//				} catch (InterruptedException e) {
//					interrupt = true; 
//
//					ending = System.currentTimeMillis();
//					time = ending - starting;
//					pause -= time; 
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				if(interrupt){
//					Thread.currentThread().interrupt();
//				}
//			}
//			Tile endTile = travel(tile, pawn, -CardDeck.getValue(card));
			if(endTile == null) return false;
			Tile beforeTile = endTile;
			endTile = slide(endTile,pawn);
			if(endTile.isOccupiedByColor(pawn.getColor())) {
				if(endTile.getPawn() != pawn) return false;
			}
			if(endTile.isOccupied()) players[GameHelpers.getIndexFromColor(endTile.getPawnColor())].returnPawn(endTile.getPawn());
			tile.removePawn();
			if(!endTile.isHome()){
				endTile.setPawn(pawn);
			} else pawn.setCurrentTile(endTile);
			if(beforeTile != endTile) slideRemove(beforeTile, pawn.getColor());
		}//Card seven
		else if (card.getType() == CardDeck.SEVEN){
			//assume you can move all 1-7
			boolean moves[] = {false,true,true,true,true,true,true,true};
			//check if you can make the move normally
			for(int i = 0; i <= 7; i++) {
				int main = 7 - i;
				int secondary = i;
				Tile endTileMain = travel(tile,pawn, main);
				if(endTileMain == null) {
					moves[main] = false;
					continue;
				}
				endTileMain = slide(endTileMain,pawn);
				if(endTileMain.isOccupiedByColor(pawn.getColor())) {
					if(endTileMain.getPawn() != pawn) {
						moves[main] = false;
					}
				}
				if(main == 7) continue;//if 7, we don'e need a compliment
				
				//Prove that the compliment to each valid move is also a valid move
				boolean hasFriend = false;
				for(Pawn other : livePlayers[currentPlaying].getPawns()) {
					if(other == pawn) continue;
					Tile endTileSecondary = other.getCurrentTile();
					if(endTileSecondary.isHome()) continue;
					if(endTileSecondary.isStart()) continue;
					
					//Check if the move would be blocked
					boolean blocked = false;
					for(int j = 0; j < secondary; j++) {
						endTileSecondary = endTileSecondary.getNext(other.getColor());
						if(endTileSecondary.isOccupiedByColor(other.getColor())) {
							if(endTileSecondary != tile) blocked = true;
						}
						if(endTileSecondary == endTileMain) blocked = true;
					}
					if(blocked) continue;
					
					endTileSecondary = slide(endTileSecondary,other);
					if(endTileSecondary.isOccupiedByColor(other.getColor())) {
						if(endTileSecondary != tile) continue;
					}
					if(endTileSecondary == endTileMain) continue;
					hasFriend = true;
					break;
				}
				//The final verdict if the initial move has a valid compliment
				moves[main] = moves[main] && hasFriend;
			}
			//count our options
			int options = 0;
			for(boolean move : moves) if(move) options++;
			//If no options, the move is not valid, otherwise offer to move
			if(options == 0) return false;
			else {
				String availableMoves[] = new String[options];
				int counter = 0;
				for(int i = 0; i < moves.length; i++) {
					if(moves[i])availableMoves[counter++] = ""+i;
				}
				int n;
				//Allow the human player to pick
				//OVERRIDE - Let each work the same - IF THEY ARE A REAL PERSON
				//if(currentPlaying == mainPlayer) {
				if(livePlayers[currentPlaying].isActive()) {
				/*String selection = (String)JOptionPane.showInputDialog(
						null,
						"What would you like to do?",
					    "???",
				        JOptionPane.QUESTION_MESSAGE,
				        null,
				        availableMoves,
				        availableMoves[0]);
						n = Integer.valueOf(selection);*/
					n = sevenSplit;
				} else n = Integer.valueOf(availableMoves[0]); //Robot will just use first valid move
				//System.out.println(n);
				//if moving 7, we won't move another pawn, otherwise, keep track of the remainder
				//Note: we proved earlier that this remainder will be valid for at least one pawn later
				if(n == 7) sevenRemainder = -1;
				else sevenRemainder = 7 - n;
				
				pause = 250; 
				starting = 0;
				interrupt = false; 
				Tile endTile = null; 
				Tile temp = tile; 
				for(int i = 0; i < n; i++){
//					mGamePanel.redraw();

					System.out.println("move #: " + i);
					endTile = travel(temp, pawn, 1);
					pawn.setCurrentTile(endTile);
					endTile.setPawn(pawn);
					temp.removePawn();
					temp = pawn.getCurrentTile();
					
					mGamePanel.redraw();
					
					try {
						starting = System.currentTimeMillis();
						Thread.sleep(pause);
					} catch (InterruptedException e) {
						interrupt = true; 

						ending = System.currentTimeMillis();
						time = ending - starting;
						pause -= time; 
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(interrupt){
						Thread.currentThread().interrupt();
					}
				}
				
//				Tile endTile = travel(tile, pawn, n);
				if(endTile == null) return false;
				Tile beforeTile = endTile;
				endTile = slide(endTile,pawn);
				if(endTile.isOccupiedByColor(pawn.getColor())) {
					if(endTile.getPawn() != pawn) return false;
				}
				if(endTile.isOccupied()) players[GameHelpers.getIndexFromColor(endTile.getPawnColor())].returnPawn(endTile.getPawn());
				tile.removePawn();
				if(!endTile.isHome()){
					endTile.setPawn(pawn);
				} else pawn.setCurrentTile(endTile);
				if(beforeTile != endTile) slideRemove(beforeTile, pawn.getColor());
				mSelectedTile = null;
				mGameBoard.resetSelections();
				mGamePanel.redraw();
				boolean canMove = false;
				for(Pawn p : livePlayers[currentPlaying].getPawns()) {
					if(!p.getCurrentTile().isHome() && !p.getCurrentTile().isStart()) {
						canMove = true;
					}
				}
				if(!canMove) sevenRemainder = -1;
				if(sevenRemainder != -1) return true;
			}
		}
		else if (card.getType() == CardDeck.TEN){
			//Check to see if the resulting forward 10 move is valid
			boolean canMoveForward = true;
			
			Tile endTileForward = animate(CardDeck.getValue(card), tile, pawn);
			
//			pause = 250; 
//			starting = 0;
//			interrupt = false; 
//			Tile endTileForward = null; 
//			Tile temp = tile; 
//			for(int i = 0; i <= card.getType() + 2; i++){
////				mGamePanel.redraw();
//
//				System.out.println("move #: " + i);
//				endTileForward = travel(temp, pawn, 1);
//				pawn.setCurrentTile(endTileForward);
//				endTileForward.setPawn(pawn);
//				temp.removePawn();
//				temp = pawn.getCurrentTile();
//				
//				mGamePanel.redraw();
//				
//				try {
//					starting = System.currentTimeMillis();
//					Thread.sleep(pause);
//				} catch (InterruptedException e) {
//					interrupt = true; 
//
//					ending = System.currentTimeMillis();
//					time = ending - starting;
//					pause -= time; 
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				if(interrupt){
//					Thread.currentThread().interrupt();
//				}
//			}
			
			
//			Tile endTileForward = travel(tile, pawn, CardDeck.getValue(card));
			Tile beforeTileForward = endTileForward;
			if(endTileForward == null) canMoveForward = false;
			if(canMoveForward) {
				endTileForward = slide(endTileForward,pawn);
				if(endTileForward.isOccupiedByColor(pawn.getColor())) {
					if(endTileForward.getPawn() != pawn) canMoveForward = false;
				}
			}
			//Check to see if the resulting backward 1 move is valid
			boolean canMoveBackwards = true;
//			Tile endTileBackward = travel(tile, pawn, -1);
			Tile endTileBackward = animate(-1, tile, pawn);
			Tile beforeTileBackward = endTileBackward;
			if(endTileBackward == null) canMoveBackwards = false;
			if(canMoveBackwards) {
				endTileBackward = slide(endTileBackward,pawn);
				if(endTileBackward.isOccupiedByColor(pawn.getColor())) {
					if(endTileBackward.getPawn() != pawn) canMoveBackwards = false;
				}
			}
			//if(currentPlaying != mainPlayer) {
			if(!livePlayers[currentPlaying].isActive()){
				//Robots will move forward if possible
				if(canMoveForward) {
					if(endTileForward.isOccupied()) players[GameHelpers.getIndexFromColor(endTileForward.getPawnColor())].returnPawn(endTileForward.getPawn());
					tile.removePawn();
					if(!endTileForward.isHome()){
						endTileForward.setPawn(pawn);
					}else pawn.setCurrentTile(endTileForward);
					if(beforeTileForward != endTileForward) slideRemove(beforeTileForward, pawn.getColor());
				} else {
					if(endTileBackward.isOccupied()) players[GameHelpers.getIndexFromColor(endTileBackward.getPawnColor())].returnPawn(endTileBackward.getPawn());
					tile.removePawn();
					if(!endTileBackward.isHome()){
						endTileBackward.setPawn(pawn);
					}else pawn.setCurrentTile(endTileBackward);
					if(beforeTileBackward != endTileBackward) slideRemove(beforeTileBackward, pawn.getColor());
				}
			} else {
				int n = -1;
				//Humans will be able to choose if they can
				if(canMoveForward && canMoveBackwards) {
					/*Object[] options = {"Forward 10","Backward 1"};
					n = JOptionPane.showOptionDialog(null,
					    "What would you like to do?",
					    "???",
					    JOptionPane.YES_NO_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,
					    options,
					    options[1]);*/
					if(tenMoveChoice == 10) n = 0;
					if(tenMoveChoice == -1) n = -1;
				}
				else if(canMoveForward) n = 0;
				else if(canMoveBackwards) n = -1;
				//Human selected forward (or is forced to)
				if(n == 0) {
					if(endTileForward.isOccupied()) players[GameHelpers.getIndexFromColor(endTileForward.getPawnColor())].returnPawn(endTileForward.getPawn());
					tile.removePawn();
					if(!endTileForward.isHome()){
						endTileForward.setPawn(pawn);
					}else pawn.setCurrentTile(endTileForward);
					if(beforeTileForward != endTileForward) slideRemove(beforeTileForward, pawn.getColor());
				} else { //Human selected backward (or is forced to)
					if(endTileBackward.isOccupied()) players[GameHelpers.getIndexFromColor(endTileBackward.getPawnColor())].returnPawn(endTileBackward.getPawn());
					tile.removePawn();
					if(!endTileBackward.isHome()){
						endTileBackward.setPawn(pawn);
					}else pawn.setCurrentTile(endTileBackward);
					if(beforeTileBackward != endTileBackward) slideRemove(beforeTileBackward, pawn.getColor());
				}
			}
		}
		else if (card.getType() == CardDeck.ELEVEN) {
			//Check to see if the pawn can move forward 11
			boolean canMoveForward = true;
			
			Tile endTile = animate(CardDeck.getValue(card), tile, pawn);
			
//			pause = 250; 
//			starting = 0;
//			interrupt = false; 
//			Tile endTile = null; 
//			Tile temp = tile; 
//			for(int i = 0; i <= card.getType(); i++){
////				mGamePanel.redraw();
//
//				System.out.println("move #: " + i);
//				endTile = travel(temp, pawn, 1);
//				pawn.setCurrentTile(endTile);
//				endTile.setPawn(pawn);
//				temp.removePawn();
//				temp = pawn.getCurrentTile();
//				
//				mGamePanel.redraw();
//				
//				try {
//					starting = System.currentTimeMillis();
//					Thread.sleep(pause);
//				} catch (InterruptedException e) {
//					interrupt = true; 
//
//					ending = System.currentTimeMillis();
//					time = ending - starting;
//					pause -= time; 
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				if(interrupt){
//					Thread.currentThread().interrupt();
//				}
//			}
					
//			Tile endTile = travel(tile, pawn, CardDeck.getValue(card));
			
			Tile beforeTile = endTile;
			if(endTile == null) canMoveForward = false;
			if(canMoveForward) {
				endTile = slide(endTile,pawn);
				if(endTile.isOccupiedByColor(pawn.getColor())) {
					if(endTile.getPawn() != pawn) canMoveForward = false;
				}
			}
			//Check to see if the pawn can swap with another
			boolean canSwap = false;
			for(Player playing : livePlayers) {
				if(playing != livePlayers[currentPlaying]) {
					for(Pawn opponentPawn : playing.getPawns()) {
						Tile t = opponentPawn.getCurrentTile();
						if(!t.isHome() && !t.isStart() && !t.isSafeZone()) {
							canSwap = true;
							break;
						}
					}
					if(canSwap) break;
				}
			}
			if(canMoveForward && !canSwap) {
				//If the pawn can move forward, but can't swap just move it forward
				if(endTile.isOccupied()) players[GameHelpers.getIndexFromColor(endTile.getPawnColor())].returnPawn(endTile.getPawn());
				tile.removePawn();
				if(!endTile.isHome()){
					endTile.setPawn(pawn);
				} else pawn.setCurrentTile(endTile);
				if(beforeTile != endTile) slideRemove(beforeTile, pawn.getColor());
			} else {
				//If it is a robot, just move forward
				//if(currentPlaying != mainPlayer) {
				if(!livePlayers[currentPlaying].isActive()) {
					if(canMoveForward) {
						if(endTile.isOccupied()) players[GameHelpers.getIndexFromColor(endTile.getPawnColor())].returnPawn(endTile.getPawn());
						tile.removePawn();
						if(!endTile.isHome()){
							endTile.setPawn(pawn);
						} else pawn.setCurrentTile(endTile);
						if(beforeTile != endTile) slideRemove(beforeTile, pawn.getColor());
					} else {
						//Remember: The robot will attempt with all pawns,
						//Eventually, the robot will move forward
						return false;
					}
				} else {
					//If the player can choose, let them.
					/*
					Object[] options = {"Move Forward","Swap"};
					int n = JOptionPane.showOptionDialog(null,
					    "What would you like to do?",
					    "???",
					    JOptionPane.YES_NO_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,
					    options,
					    options[1]);
					if(n == 1) {//Chose to swap, remember this tile for later
						swapTile = tile;
						return true;
					} else {
					*/
						//Chose to move, simply move forward 11
						if(endTile.isOccupied()) {
							players[GameHelpers.getIndexFromColor(endTile.getPawnColor())].returnPawn(endTile.getPawn());
						}
						tile.removePawn();
						if(!endTile.isHome()){
							endTile.setPawn(pawn);
						} else pawn.setCurrentTile(endTile);
						if(beforeTile != endTile) slideRemove(beforeTile, pawn.getColor());
					//}
				}
			}
		}
		//Move was successful if this is reached.
		endTurn();
		return true;
	}
	
	//Clears the slide of all non-color colored pawns
	private void slideRemove(Tile temp, Color color) {
		if(temp.doesSlide() && !temp.getPrevious().doesSlide()) {
			while(temp.getNext(color).doesSlide()) {
				temp = temp.getNext(color);
				if(temp.isOccupied())
				if(!temp.getPawnColor().equals(color)) {
					players[GameHelpers.getIndexFromColor(temp.getPawnColor())].returnPawn(temp.getPawn());
					temp.removePawn();
				}
			}
		}
	}

	public Tile animate(int moves, Tile tile, Pawn pawn){
//		long starting, ending, time; 
		long pause = 250; 
		starting = 0;
		boolean interrupt = false; 
		
		int absMove = Math.abs(moves);
		
//		Tile endTile = null; 
		Tile temp = tile; 
		for(int i = 0; i < absMove; i++){
//			mGamePanel.redraw();

			System.out.println("move #: " + i);
			
			if(moves > 0) tile = travel(temp, pawn, 1);
			else tile = travel(temp, pawn, -1);
			
			pawn.setCurrentTile(tile);
			tile.setPawn(pawn);
			temp.removePawn();
			temp = pawn.getCurrentTile();
			
			mGamePanel.redraw();
			
			try {
//				starting = System.currentTimeMillis();
				Thread.sleep(pause);
			} catch (InterruptedException e) {
//				interrupt = true; 
//
//				ending = System.currentTimeMillis();
//				time = ending - starting;
//				pause = time; 
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(interrupt){
				Thread.currentThread().interrupt();
			}
		}
		
		return tile; 
	}
	
	//Movement logic to move pawn
	public Tile travel(Tile start, Pawn pawn, int numSpaces) {
		boolean backwards = numSpaces < 0;
		numSpaces = Math.abs(numSpaces);
//		Tile temp = start; 
		for(int i = 0; i < numSpaces; i++) {
			if(backwards) start = start.getPrevious();
			else{
				start = start.getNext(pawn.getColor());
//				temp = start; 
//				pawn.setCurrentTile(start);
//				temp.setPawn(pawn);
//				mGamePanel.redraw();
			}
//			try {
//				Thread.sleep(250);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			mGamePanel.redraw();
//			temp.removePawn();
			
			if(start.isOccupiedByColor(pawn.getColor())) return null;
		}
		return start;
	}
	
	public Tile travel(Tile start, Color color, int numSpaces) {
		boolean backwards = numSpaces < 0;
		numSpaces = Math.abs(numSpaces);
		
//		Tile temp = start; 
		
		for(int i = 0; i < numSpaces; i++) {
			if(backwards){ 
				start = start.getPrevious();
			}
			else{ 
				start = start.getNext(color);
//				temp = start; 
//				(start.getPawn()).setCurrentTile(start);
//				temp.setPawn(start.getPawn());
//				mGamePanel.redraw();
			}
//			try {
//				Thread.sleep(250);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			mGamePanel.redraw();
//			temp.removePawn();
//			mGamePanel.redraw();

			if(start.isOccupiedByColor(color)) return null;
		}
		return start;
	}
	
	//Movement logic to slide pawn (does not remove)
	public Tile slide(Tile temp, Pawn pawn) {
		if(temp.doesSlide() && !temp.getPrevious().doesSlide()) {
			while(temp.getNext(pawn.getColor()).doesSlide()) {
				temp = temp.getNext(pawn.getColor());
			}
			temp = temp.getNext(pawn.getColor());
		}
		return temp;
	}
	
	public Tile slide(Tile temp, Color color) {
		if(temp.doesSlide() && !temp.getPrevious().doesSlide()) {
			while(temp.getNext(color).doesSlide()) {
				temp = temp.getNext(color);
			}
			temp = temp.getNext(color);
		}
		return temp;
	}
	
	//clears the tiles of the game board
	private void clean() {
		mGameBoard.clearTiles();
	}
	
	//Sets the game board
	private void set() {
		for(Player player : players) {
			player.resetStartPawns();
			for(Pawn pawn : player.getPawns()) {
				Tile toSet = pawn.getCurrentTile();
				if(!toSet.isHome() && !toSet.isStart())
					pawn.getCurrentTile().setPawn(pawn);
				else if(toSet.isStart()) {
					player.returnPawn(pawn);
				}
			}
		}
	}

	//Set the render target
	public void setGamePanel(GamePanel inGamePanel) {
		mGamePanel = inGamePanel;
	}
	
	public void setSelectedTile(Player player, Tile tile, boolean applySelection) {
		int cardType = card.getType();
		
		if(cardType == CardDeck.SORRY) {
			if(tile.isOccupied()) {
				if(!tile.isOccupiedByColor(player.getColor()) && !tile.isSafeZone()) {
					setSelected(tile,applySelection);
					if(applySelection) {
						tileClicked(tile,player);
						return;
					}
				}
			}
		}
		
		boolean start = tile.isStart() && tile.isColor(player.getColor()) && player.hasPawnsAtStart() && (card.getType() == CardDeck.ONE || card.getType() == CardDeck.TWO);
		boolean owned = tile.isOccupiedByColor(player.getColor());
		if(!start && !owned) return;
		
		if(
			cardType == CardDeck.ONE ||
			cardType == CardDeck.TWO ||
			cardType == CardDeck.THREE ||
			cardType == CardDeck.FIVE ||
			cardType == CardDeck.EIGHT ||
			cardType == CardDeck.TWELVE
		) {
			Tile endTile = travel(tile, player.getColor(), CardDeck.getValue(card));
			if(endTile == null) return;
			endTile = slide(endTile,player.getColor());
			if(endTile.isOccupiedByColor(player.getColor())) {
				if(endTile.getPawn() != tile.getPawn()) return;
			}
			setSelected(tile,applySelection);
		}
		else if (card.getType() == CardDeck.FOUR){
			Tile endTile = travel(tile, player.getColor(), -CardDeck.getValue(card));
			if(endTile == null) return;
			endTile = slide(endTile,player.getColor());
			if(endTile.isOccupiedByColor(player.getColor())) {
				if(endTile.getPawn() != tile.getPawn()) return;
			}
			setSelected(tile,applySelection);
		}
		else if (card.getType() == CardDeck.SEVEN){
			//assume you can move all 1-7
			boolean moves[] = {false,true,true,true,true,true,true,true};
			//check if you can make the move normally
			for(int i = 0; i <= 7; i++) {
				int main = 7 - i;
				int secondary = i;
				Tile endTileMain = travel(tile,player.getColor(), main);
				if(endTileMain == null) {
					moves[main] = false;
					continue;
				}
				endTileMain = slide(endTileMain,player.getColor());
				if(endTileMain.isOccupiedByColor(player.getColor())) {
					if(endTileMain.getPawn() != tile.getPawn()) {
						moves[main] = false;
					}
				}
				if(main == 7) continue;//if 7, we don'e need a compliment
				
				//Prove that the compliment to each valid move is also a valid move
				boolean hasFriend = false;
				for(Pawn other : livePlayers[currentPlaying].getPawns()) {
					if(other == tile.getPawn()) continue;
					Tile endTileSecondary = other.getCurrentTile();
					if(endTileSecondary.isHome()) continue;
					if(endTileSecondary.isStart()) continue;
					
					//Check if the move would be blocked
					boolean blocked = false;
					for(int j = 0; j < secondary; j++) {
						endTileSecondary = endTileSecondary.getNext(other.getColor());
						if(endTileSecondary.isOccupiedByColor(other.getColor())) {
							if(endTileSecondary != tile) blocked = true;
						}
						if(endTileSecondary == endTileMain) blocked = true;
					}
					if(blocked) continue;
					
					endTileSecondary = slide(endTileSecondary,other);
					if(endTileSecondary.isOccupiedByColor(other.getColor())) {
						if(endTileSecondary != tile) continue;
					}
					if(endTileSecondary == endTileMain) continue;
					hasFriend = true;
					break;
				}
				//The final verdict if the initial move has a valid compliment
				moves[main] = moves[main] && hasFriend;
			}
			//count our options
			int options = 0;
			for(boolean move : moves) if(move) options++;
			//If no options, the move is not valid, otherwise offer to move
			if(options == 0) return;
			setSelected(tile,applySelection);
		}
		else if (card.getType() == CardDeck.TEN){
			//Check to see if the resulting forward 10 move is valid
			boolean canMoveForward = true;
			Tile endTileForward = travel(tile, player.getColor(), CardDeck.getValue(card));
			if(endTileForward == null) canMoveForward = false;
			if(canMoveForward) {
				endTileForward = slide(endTileForward,player.getColor());
				if(endTileForward.isOccupiedByColor(player.getColor())) {
					if(endTileForward.getPawn() != tile.getPawn()) canMoveForward = false;
				}
			}
			//Check to see if the resulting backward 1 move is valid
			boolean canMoveBackwards = true;
			Tile endTileBackward = travel(tile, player.getColor(), -1);
			if(endTileBackward == null) canMoveBackwards = false;
			if(canMoveBackwards) {
				endTileBackward = slide(endTileBackward,player.getColor());
				if(endTileBackward.isOccupiedByColor(player.getColor())) {
					if(endTileBackward.getPawn() != tile.getPawn()) canMoveBackwards = false;
				}
			}
			if(!canMoveForward && !canMoveBackwards) return;
			setSelected(tile,applySelection);
		}
		else if (card.getType() == CardDeck.ELEVEN) {
			//Check to see if the pawn can move forward 11
			boolean canMoveForward = true;
			Tile endTile = travel(tile, player.getColor(), CardDeck.getValue(card));
			if(endTile == null) canMoveForward = false;
			if(canMoveForward) {
				endTile = slide(endTile,player.getColor());
				if(endTile.isOccupiedByColor(player.getColor())) {
					if(endTile.getPawn() != tile.getPawn()) canMoveForward = false;
				}
			}
			//Check to see if the pawn can swap with another
			boolean canSwap = false;
			for(Player playing : livePlayers) {
				if(playing != livePlayers[currentPlaying]) {
					for(Pawn opponentPawn : playing.getPawns()) {
						Tile t = opponentPawn.getCurrentTile();
						if(!t.isHome() && !t.isStart() && !t.isSafeZone()) {
							canSwap = true;
							break;
						}
					}
					if(canSwap) break;
				}
			}
			if(!canMoveForward && !canSwap) return;
			setSelected(tile,applySelection);
		}
	}
	
	private void setSelected(Tile toSelect, boolean select) {
		if(select) {
			mSelectedTile = toSelect;
			mSelectedTile.selected = true;
		}
		toSelect.highlighted = true;
	}

	public Tile getSelectedTile() {
		return mSelectedTile;
	}

	public boolean hasCard() {
		return(card != null);
	}

	public boolean highlightPath(Tile destination, Color color) {
		if(mSelectedTile == destination) return true;
		int cardType = card.getType();
		int cardValue = CardDeck.getValue(card);
		
		if(cardType == CardDeck.SORRY) {
			//If human, check if the selection of the opponent is valid
			if(destination.isOccupied()) {
				if(!destination.isOccupiedByColor(color) && !destination.isSafeZone()) {
					destination.highlighted = true;
				}
			}
		}
		if(
		cardType == CardDeck.ONE ||
		cardType == CardDeck.TWO ||
		cardType == CardDeck.THREE ||
		cardType == CardDeck.FIVE ||
		cardType == CardDeck.EIGHT ||
		cardType == CardDeck.TWELVE
		) {
			Tile toCheck = travel(mSelectedTile, color, cardValue);
			if(toCheck == destination) {
				highlightTilesForward(mSelectedTile, destination, color);
			}
		}
		else if( cardType == CardDeck.FOUR) {
			Tile toCheck = travel(mSelectedTile, color, -cardValue);
			if(toCheck == destination) {
				highlightTilesBackward(mSelectedTile, destination, color);
			}
		}
		else if (cardType == CardDeck.SEVEN && sevenRemainder == -1) {
			int distance = getDistance(mSelectedTile, destination, color);
			if(distance > 7 || distance <= 0) return false;
			//sevenSplit = distance;
			OutMail.send(MessageConstants.BROADCAST, MessageConstants.SEVEN_SPLIT + distance);
			/*if(distance == -1) return false;
			if(distance > 7) return false;
			Tile toCheck = travel(mSelectedTile, color, distance);
			boolean hasCompliment = false;
			if(toCheck != null) {
				if(toCheck == destination) {
					if(distance == 7) hasCompliment = true;
					if(!hasCompliment)
					for(Pawn others : livePlayers[currentPlaying].getPawns()) {
						if(others == mSelectedTile.getPawn()) continue;
						if(others.getCurrentTile().isHome()) continue;
						if(others.getCurrentTile().isStart()) continue;
						if(travel(others.getCurrentTile(), color, 7-distance) != null) hasCompliment = true;
					}
					if(hasCompliment) {
						sevenSplit = distance;
						highlightTilesForward(mSelectedTile, destination, color);
					}
				}
			}*/
			//assume you can move all 1-7
			boolean moves[] = {false,true,true,true,true,true,true,true};
			//check if you can make the move normally
			for(int i = 0; i <= 7; i++) {
				int main = 7 - i;
				int secondary = i;
				Tile endTileMain = travel(mSelectedTile,color, main);
				if(endTileMain == null) {
					moves[main] = false;
					continue;
				}
				endTileMain = slide(endTileMain,color);
				if(endTileMain.isOccupiedByColor(color)) {
					if(endTileMain.getPawn() != mSelectedTile.getPawn()) {
						moves[main] = false;
					}
				}
				if(main == 7) continue;//if 7, we don'e need a compliment
				
				//Prove that the compliment to each valid move is also a valid move
				boolean hasFriend = false;
				for(Pawn other : livePlayers[currentPlaying].getPawns()) {
					if(other == mSelectedTile.getPawn()) continue;
					Tile endTileSecondary = other.getCurrentTile();
					if(endTileSecondary.isHome()) continue;
					if(endTileSecondary.isStart()) continue;
					
					//Check if the move would be blocked
					boolean blocked = false;
					for(int j = 0; j < secondary; j++) {
						endTileSecondary = endTileSecondary.getNext(other.getColor());
						if(endTileSecondary.isOccupiedByColor(other.getColor())) {
							if(endTileSecondary != mSelectedTile) blocked = true;
						}
						if(endTileSecondary == endTileMain) blocked = true;
					}
					if(blocked) continue;
					
					endTileSecondary = slide(endTileSecondary,other);
					if(endTileSecondary.isOccupiedByColor(other.getColor())) {
						if(endTileSecondary != mSelectedTile) continue;
					}
					if(endTileSecondary == endTileMain) continue;
					hasFriend = true;
					break;
				}
				//The final verdict if the initial move has a valid compliment
				moves[main] = moves[main] && hasFriend;
			}
			if(moves[distance] || destination.isHome()) {
				highlightTilesForward(mSelectedTile, destination, color);
			}
		}
		else if (cardType == CardDeck.SEVEN && sevenRemainder != -1) {
			Tile toCheck = travel(mSelectedTile, color, sevenRemainder);
			if(toCheck == destination) {
				highlightTilesForward(mSelectedTile, destination, color);
			}
		}
		else if (cardType == CardDeck.TEN) {
			Tile toCheckForward = travel(mSelectedTile, color, 10);
			if(toCheckForward == destination) {
				//tenMoveChoice = 10;
				OutMail.send(MessageConstants.BROADCAST, MessageConstants.TEN_MOVE_CHOICE + 10);
				highlightTilesForward(mSelectedTile, destination, color);
			}
			Tile toCheckBackward = travel(mSelectedTile, color, -1);
			if(toCheckBackward == destination) {
				//tenMoveChoice = -1;
				OutMail.send(MessageConstants.BROADCAST, MessageConstants.TEN_MOVE_CHOICE + -1);
				highlightTilesBackward(mSelectedTile, destination, color);
			}
		}
		else if (cardType == CardDeck.ELEVEN) {
			boolean moveForward = false;
			swapTile = null;
			Tile toCheckForward = travel(mSelectedTile, color, 11);
			if(toCheckForward == destination) {
				moveForward = true;
				highlightTilesForward(mSelectedTile, destination, color);
			}
			if(!moveForward) {
				if(destination.getPawn() != null) {
					if(destination.getPawnColor() != color) {
						destination.highlighted = true;
						OutMail.send(MessageConstants.BROADCAST, MessageConstants.SWAP_TILE + destination.getX() + "|" + destination.getY());
						//swapTile = destination;
					}
				}
			}
		}
		return false;
	}

	private int getDistance(Tile from, Tile to, Color color) {
		int distance = 0;
		while(from != to) {
			distance++;
			from = from.getNext(color);
			if(distance > 12) return -1;
		}
		return distance;
	}

	private void highlightTilesForward(Tile from, Tile to, Color color) {
		Tile tempTile = from;
		while(tempTile != to){
			tempTile.highlighted = true;
			tempTile = tempTile.getNext(color);
		} tempTile.highlighted = true;
	}
	
	private void highlightTilesBackward(Tile from, Tile to, Color color) {
		Tile tempTile = from;
		while(tempTile != to){
			tempTile.highlighted = true;
			tempTile = tempTile.getPrevious();
		} tempTile.highlighted = true;
	}

	public void clearHighlight() {
		mGameBoard.unhighlightTiles();
	}

	public void resetSelection() {
		mSelectedTile = null;
		mGameBoard.resetSelections();
	}

	public void setCardDeckSeed(int seed) {
		cardDeck.setSeed(seed);
	}

	public Player getPlayerByName(String name) {
		return players[GameHelpers.getIndexFromColorName(name)];
	}
	
	public void setSevenSplit(int distance) {
		sevenSplit = distance;
	}
	
	public void setTenMoveChoice(int choice) {
		tenMoveChoice = choice;
	}
	
	public void setSwapTile(int x, int y) {
		swapTile = mGameBoard.getTile(x, y);
	}

	public void deactivatePlayer(String name) {
		int index = GameHelpers.getIndexFromColorName(name);
		players[index].deactivate();
		//System.out.println(players[index].getColor() + "has been deactivated");
		if(players[index] == livePlayers[currentPlaying]) {
			//System.out.println("The player currently playing d/ced!");
			simulatePlayer(players[index]);
		}
	}

	public boolean isSorry() {
		return card.getType() == CardDeck.SORRY;
	}
	
}
