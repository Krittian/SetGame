/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author EliFriedman
 */
public class Game {

	private String name;
	private String ID;
	private final int INIT_SIZE = 12;
	private int stateNum =0;
	// Each card is a 4 digit String where each digit is from 0 - 2 
	// and corresponds to a feature of the Set card
	// "Shape Color Number Fill"
	private HashMap<Integer, String> deck;

	// Each player is represented by the cards they won.
	private Map<String, ArrayList<String>> players;
	private ArrayList<String> outCards;

	public Game(String ID, String name) {
		this.ID = ID;
		this.name = name;
		stateNum = 0;
		players = new HashMap<>();
		deck = new HashMap<>(81);
		outCards = new ArrayList<String>();//new HashMap<>(21);

		for (int i = 0; i < 81; i++) {
			String res = getCardString(i);
			deck.put(i, res);
		}
		fillOutCards();
	}
	public int getStateNum() {
		return stateNum;
	}
	public void incrementStateNum() {
		System.out.println("Updatiing state num " + (stateNum +1));
		stateNum++;
	}
	/**
	 * Initializes the cards that are out, making sure that there is at least
	 * one set on the board
	 *//*
	private void initializeOutCards() {
		for (int i = 0; i < (INIT_SIZE / 3.0 - 1); i++) {
			add3Cards();
		}
		add3Cards(); // ensure outCards contains a set
	}*/

	private void fillOutCards(){
		fillOutCards(-1,-1,-1);
	}
	
	private void fillOutCards(int c1, int c2, int c3){
		int sub = 0;
		if(c1 != -1) sub++;
		if(c2 != -1) sub++;
		if(c3 != -1) sub++;
		if(sub != 0 && sub != 3)
			System.out.println("NEVER SHOULD HAPPEN");
		
		if(deck.size() <3){
			try{
				outCards.remove(c1);
				outCards.remove(c2);
				outCards.remove(c3);
			}catch(Exception e){
				System.err.println("delete failed");
				;
			}
			System.out.println("THE GAME IS OVER MAN!!!! GIVE UP.");
			return;
		}
		while(outCards.size() - sub <12 || !outContainsSet()){
			System.out.println("party!!");
			add3Cards(c1, c2, c3);
			c1 = -1;
			c2 = -1;
			c3 = -1;
			sub = 0;
		}
	}
	
	private void fillOutCards(String card1s, String card2s, String card3s){
		fillOutCards(outCards.lastIndexOf(card1s),outCards.lastIndexOf(card2s),outCards.lastIndexOf(card3s));
	}
	private boolean outContainsSet() {
		//String[] cards = new String[outCards.size()];
		//outCards.toArray(cards);
		for (int i = 0; i < outCards.size(); i++) {
			for (int j = 0; j < outCards.size(); j++) {
				if (i != j) {
					String setCard = getSetCard(outCards.get(i), outCards.get(j));
					if (outCards.contains(setCard)) {
						System.out.println("NEXT SET: " + i + " " + j + " " + outCards.lastIndexOf(setCard));
						return true;
					}
				}
			}
		}
		return false;
	}


	/**
	 *
	 * @param card1
	 * @param card2
	 * @param card3
	 * @return given three cards, check to see if they're on the table and they
	 * form a set
	 */
	private boolean checkValidSet(int card1, int card2, int card3) {
		boolean validSet = card3 == getSetCard(card1,card2);            
		System.out.println("valid: " + validSet);
		return validSet;
	}

	/**
	 *
	 * @param playerID the ID of a player
	 * @param card1
	 * @param card2
	 * @param card3
	 * @return
	 */
	public boolean checkSet(String playerID, int card1, int card2, int card3) {
		String card1s = getCardString(card1);
		String card2s = getCardString(card2);
		String card3s = getCardString(card3);
		if (players.containsKey(playerID) 
				&& outCards.contains(card1s)  
				&& outCards.contains(card2s)  
				&& outCards.contains(card3s)
				&& checkValidSet(card1, card2, card3)) 
		{
			ArrayList<String> ownedcards = players.get(playerID);
			ownedcards.add(card1s);
			ownedcards.add(card2s);
			ownedcards.add(card3s);
			players.put(playerID, ownedcards);

			System.out.println(outCards.toString());

			fillOutCards(card1s,card2s,card3s);
			
			System.out.println(outCards.toString());

			return true;
		}
		return false;
	}

	public String[] getOutCards(boolean shuffle) {
		String[] cards = new String[outCards.size()];
		outCards.toArray(cards);

		if (shuffle) {
			int index;
			String temp;
			Random random = new Random();
			for (int i = cards.length - 1; i >= 0; i--) {
				index = random.nextInt(i + 1);
				temp = cards[index];
				cards[index] = cards[i];
				cards[i] = temp;
			}
		}

		//for(int i = 0; i< cards.length; i++) System.out.println("" + i + "." + cards[i]);

		return cards;
	}


	private void add3Cards(int c1,int c2,int c3){
		System.out.println("DESK SIZE: " + deck.size());
		incrementStateNum();
		Integer [] c = new Integer []{ c1, c2, c3}; 
		Random rnd = new Random();
		for (int i = 0; i < 3; i++) {
			int index = rnd.nextInt(81);
			while (!deck.containsKey(index)) {
				index = (index + 1) % 81;
				System.out.println("index: " + index);
			}
			String card = deck.remove(index);
			
			System.out.println(c[i] + " __ " + card);

			if(c[i] == -1) outCards.add(card);
			else outCards.set(c[i], card);
		}
	}

	/**
	 * Given any two cards returns the unique third card that forms a set with
	 * the first two.
	 *
	 * @param card1
	 * @param card2
	 * @return
	 */
	private int getSetCard(int c1, int c2) {
		String card1 = getCardString(c1);
		String card2 = getCardString(c2);
		String card3 = "";
		for (int i = 0; i < 4; i++) {
			if (card1.charAt(i) == card2.charAt(i)) {
				card3 += card1.charAt(i);
			} else {
				// gets the third value and converts to char
				card3 += (char) '0' + '3' - (card1.charAt(i) + card2.charAt(i));
			}
		}
		return getCardIndex(card3);
	}

	/**
	 * Given any two cards returns the unique third card that forms a set with
	 * the first two.
	 *
	 * @param card1
	 * @param card2
	 * @return
	 */
	private String getSetCard(String card1, String card2) {
		String card3 = "";
		for (int i = 0; i < 4; i++) {
			if (card1.charAt(i) == card2.charAt(i)) {
				card3 += card1.charAt(i);
			} else {
				// gets the third value and converts to char
				card3 += (char) '0' + '3' - (card1.charAt(i) + card2.charAt(i));
			}
		}
		return card3;
	}

	/**
	 * Returns the string form (ex. 1010) of the card given a number from 0 to 80
	 *
	 * @param index
	 * @return
	 */
	private String getCardString(int index) {
		String res = "";
		for (int threeNum = index; threeNum > 0; threeNum /= 3) {
			res = (threeNum % 3) + res;
		}
		if (res.length() < 4) {
			res = "0000" + res;
			res = res.substring(res.length() - 4);
		}
		return res;
	}

	private String getCardString(String index) {
		return getCardString(Integer.parseInt(index));
	}

	private int getCardIndex(String card) {
		return Integer.valueOf(card, 3);
	}

	public String[] getPlayers() {
		String[] p = new String[players.keySet().size()];
		players.keySet().toArray(p);
		return p;
	}

	/***
	 * @return true if adding new player, false if player was already there.
	 */
	public boolean addPlayer(String uid){

		if(players.containsKey(uid))
			return false;
		players.put(uid, new ArrayList<String>());
		incrementStateNum();
		return true;
	}

	/***
	 * @return false if the player didn't exist, and true if it removed it
	 */
	public boolean removePlayer(String uid){
		if(!players.containsKey(uid))
			return false;
		players.remove(uid);
		incrementStateNum();
		System.out.println(getPlayers().toString());
		return true;
	}

	public String getID() {
		return ID;
	}
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
