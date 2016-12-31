package score;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ScoreServer implements Runnable{
	
	private static final String filePath = "src/score/scores";
	
	private JTable scoreTable;
	private DefaultTableModel tableModel;
	private final JFrame scoreDisplay;
	private static final int NAME_COLUMN = 0;
	private static final int SCORE_COLUMN = 1;
	
	private static final String NAME = "NAME";
	private static final String SCORE = "SCORE";
	
	private static final String DOCTYPE = "<!DOCTYPE html>";
	private static final String HTML_HEADER = "<html>";
	private static final String HTML_FOOTER = "</html>";
	private static final String BODY_HEADER = "<body>";
	private static final String BODY_FOOTER = "</body>";
	
	private static final int BORDER_TYPE = 1;
	private static final String TABLE_HEADER = "<table border=" + "\"\"" + BORDER_TYPE + ">";
	private static final String TABLE_FOOTER = "</table>";
	private static final String TABLE_ROW_HEADER = "<tr>";
	private static final String TABLE_ROW_FOOTER = "</tr>";
	private static final String TABLE_DATA_HEADER = "<td>";
	private static final String TABLE_DATA_FOOTER = "</td>";
	
	private static final String PAGE_HEADER = "Sorry! Top Score List";
	
	private volatile boolean running;
	
	{
		Object[] tableHeaders = new Object[] {"Name", "Score"};
		tableModel = new DefaultTableModel(tableHeaders,0) {
			private static final long serialVersionUID = -2100979802046466684L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		scoreTable = new JTable(tableModel);
		try {
			Scanner sc = new Scanner(new File(filePath));
			while(sc.hasNext()) {
				Object[] row = {sc.next(),sc.nextInt()};
				tableModel.addRow(row);
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		scoreDisplay = new JFrame();
		scoreDisplay.setTitle("Scores");
		scoreDisplay.setSize(250, 300);
		JScrollPane jsp = new JScrollPane(scoreTable);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scoreDisplay.add(jsp);
		scoreDisplay.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void start() {
		running = true;
		
		ServerSocket ss;
		
		try {
			ss = new ServerSocket(6789);
		} catch (Exception e) {
			System.out.println("Error: " + e);
			return;
		}
		
		while (running) {
			try {
				Socket s = ss.accept();
				PrintWriter out = new PrintWriter(s.getOutputStream());

				StringBuilder sb = new StringBuilder();
				sb.append(DOCTYPE);
				sb.append(HTML_HEADER);
				sb.append(BODY_HEADER);
				sb.append(PAGE_HEADER);
				sb.append(TABLE_HEADER);
				{//Name and score row
					sb.append(TABLE_ROW_HEADER);
					sb.append(TABLE_DATA_HEADER);
					sb.append(NAME);
					sb.append(TABLE_DATA_FOOTER);
					sb.append(TABLE_DATA_HEADER);
					sb.append(SCORE);
					sb.append(TABLE_DATA_FOOTER);
					sb.append(TABLE_ROW_FOOTER);
				}
				//Rows of data from table
				for(int i = 0; i < tableModel.getRowCount(); i++) {
					sb.append(TABLE_ROW_HEADER);
					for(int j = 0; j < tableModel.getColumnCount(); j++) {
						sb.append(TABLE_DATA_HEADER);
						sb.append(tableModel.getValueAt(i, j));
						sb.append(TABLE_DATA_FOOTER);
					}
					sb.append(TABLE_ROW_FOOTER);
				}
				sb.append(TABLE_FOOTER);
				sb.append(BODY_FOOTER);
				sb.append(HTML_FOOTER);
				out.println(sb);
				out.flush();
				s.close();
			} catch (Exception e) {
				System.out.println("Error: " + e);
			}
		}
		try {
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void add(String name, int score) {
		boolean placed = false;
		for(int i = 0; i < tableModel.getRowCount(); i++) {
			if ((int)tableModel.getValueAt(i, SCORE_COLUMN) < score) {
				Object[] row = {name,score};
				tableModel.insertRow(i, row);
				placed = true;
				break;
			}
		}
		if(!placed) {
			Object[] row = {name,score};
			tableModel.addRow(row);
		}
		System.out.println("SAVING");
		save();
	}
	
	private void save() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filePath, "UTF-8");
			for(int i = 0; i < tableModel.getRowCount(); i++) {
				writer.print(tableModel.getValueAt(i, NAME_COLUMN));
				writer.print(" ");
				writer.println(tableModel.getValueAt(i, SCORE_COLUMN));
				System.out.println("PRINTING");
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.out.println("File not found: " + filePath);
			System.out.println("Could not save scores!");
		} finally {
			if(writer != null) writer.close();
		}
	}
	
	public void display() {
		scoreDisplay.setLocationRelativeTo(null);
		scoreDisplay.setVisible(true);
	}
	
	public static void main(String args[]) {
		ScoreServer scoreServer = new ScoreServer();
		scoreServer.display();
		new Thread(scoreServer).start();
		scoreServer.start();
	}

	@Override
	public void run() {
		ServerSocket ss;
		
		try {
			ss = new ServerSocket(8000);
		} catch (Exception e) {
			System.out.println("Error: " + e);
			return;
		}
		
		while (true) {
			try {
				Socket s = ss.accept();
				Scanner scan = new Scanner(s.getInputStream());
				String name = scan.next();
				System.out.println(name);
				int score = scan.nextInt();
				System.out.println(score);
				scan.close();
				add(name,score);
				scoreDisplay.getContentPane().revalidate();
				scoreDisplay.getContentPane().repaint();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
