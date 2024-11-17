
import java.util.SplittableRandom;
import java.util.Scanner;

import static java.lang.System.*;

/*
 *  The Mexico dice game
 *  See https://en.wikipedia.org/wiki/Mexico_(game)
 *
 */
public class Mexico {

	public static void main(String[] args) {
		new Mexico().program();
	}

	final SplittableRandom rand = new SplittableRandom();
	final Scanner sc = new Scanner(in);
	final int maxRolls = 3; // No player may exceed this
	final int startAmount = 3; // Money for a player. Select any
	final int mexico = 1000; // A value greater than any other

	void program() {
		//test(); // <----------------- UNCOMMENT to test

		int pot = 0; // What the winner will get
		Player[] players; // The players (array of Player objects)
		Player current; // Current player for round
		Player leader; // Player starting the round

		players = getPlayers();
		current = getRandomPlayer(players);
		leader = current;

		out.println("Mexico Game Started");
		statusMsg(players);

		while (players.length > 1) { // Game over when only one player left
			// ----- In ----------
			String choice = getPlayerChoice(current); //Get an input from the player
			if ("r".equals(choice)) {//If the player rolls
				// --- Process ------
				//If the player is the leader, he is allowed to roll at max 3 times
				if(current.name == leader.name && current.nRolls < maxRolls){ 
					rollDice(current);
					roundMsg(current);

					if(current.nRolls == maxRolls){ // Automatically switch when the limit is reached
						current = next(players, current);
					}
				}
				//If you are not the leader then you are allowed to roll less than or equal to the leader
				else if(current.nRolls < leader.nRolls){
					rollDice(current);
					roundMsg(current);

					if(current.nRolls == leader.nRolls){ // Automatically switch when the limit is reached
						current = next(players, current);
					}
				}else{
					//If you don't have rolls left then skip to the next person
					current = next(players, current);
				}
			} else 
			if ("n".equals(choice)) { //Skip to the next person
				// Process
				current = next(players, current);
			} else {
				out.println("?");
			}

			//If all players have rolled and we are back at the start
			if (allRolled(players) && current.name == leader.name) {
				// --- Process -----
			    Player loser = getLoser(players);
				loser.amount -= 1;
				pot += 1;

				out.println("Round done "+ loser.name +" lost!");
				
				current = loser;
				if (loser.amount <= 0){
					out.println(loser.name +" has no resources, will leave game");
					players = removeLoser(players, loser);

					current = getRandomPlayer(players);
				}

				clearRoundResults(players);

				leader = current;
				// ----- Out --------------------
				out.println("Next to roll is " + current.name);

				statusMsg(players);

				out.print("Press enter to continue >");
				sc.nextLine();
				out.print("\033[H\033[2J");
				out.flush();
			}
		}
		out.println("Game Over, winner is " + players[0].name + ". Will get " + pot + " from pot");
	}

	// ---- Game logic methods --------------
	void clearRoundResults(Player[] players){
		for(Player player: players){
			player.fstDice = 0;
			player.secDice = 0;
			player.nRolls = 0;
		}
	}

	Player getLoser (Player[] players){
		int lowest = Integer.MAX_VALUE;
		Player lowest_player = null;

		for (Player player : players) {
			if (getScore(player) < lowest) {
				lowest = getScore(player);
				lowest_player = player;
			}
		}
		return lowest_player;
	}

	int getScore(Player player){
		int higher = 0;
		int lower = 0;

		if (player.fstDice > player.secDice){
			higher = player.fstDice;
			lower = player.secDice;
		}else{
			lower = player.fstDice;
			higher = player.secDice;
		}

		int score = higher*10 + lower;

		if (score == 21){
			return mexico;
		}
		return score;
	}

	Player[] removeLoser(Player[] players, Player loser){
		Player[] result = new Player[players.length -1];
		int i = 0;
		for(Player player : players){
			if(player.name != loser.name){
				result[i] = player;
				i++;
			}
		}
		return result;
	}

	boolean allRolled(Player[] players){
		for (int i = 0; i < players.length; i++) {
			if (players[i].fstDice == 0 || players[i].secDice == 0) {
				return false;
			}
		}
		return true;
	}

	void rollDice(Player player){
		int fstRoll = rand.nextInt(1, 7);
		player.fstDice = fstRoll;
		int scndRoll = rand.nextInt(1, 7);
		player.secDice = scndRoll;

		player.nRolls += 1;
	}

	Player next(Player[] players, Player player){
		int indexOfCurrent = indexOf(players, player);
		int idx = indexOfCurrent + 1;

		if(idx >= players.length){
			idx = 0;
		}

		return players[idx];
	}

	// TODO implement and test methods (one at the time)

	int indexOf(Player[] players, Player player) {
		for (int i = 0; i < players.length; i++) {
			if (players[i] == player) {
				return i;
			}
		}
		return -1;
	}

	Player getRandomPlayer(Player[] players) {
		int index = rand.nextInt(players.length);
		return players[index];
	}

	// ---------- IO methods (nothing to do here) -----------------------

	Player[] getPlayers() {
		// Ugly for now. If using a constructor this may
		// be cleaned up.
		Player[] players = new Player[3];
		Player p1 = new Player("Shariq", startAmount);
		Player p2 = new Player("Qasim", startAmount);
		Player p3 = new Player("Yousef", startAmount);
		players[0] = p1;
		players[1] = p2;
		players[2] = p3;
		
		return players;
	}

	void statusMsg(Player[] players) {
		out.print("Status: ");
		for (int i = 0; i < players.length; i++) {
			out.print(players[i].name + " " + players[i].amount + " ");
		}
		out.println();
	}

	void roundMsg(Player current) {
		out.println(current.name + " got " + current.fstDice + " and " + current.secDice);
	}

	String getPlayerChoice(Player player) {
		out.print("Player is " + player.name + " > ");
		return sc.nextLine();
	}

	// Possibly useful utility during development
	String toString(Player p) {
		return p.name + ", " + p.amount + ", " + p.fstDice + ", " + p.secDice + ", " + p.nRolls;
	}

	// Class for a player
	class Player {
		String name;
		int amount; // Start amount (money)
		int fstDice; // Result of first dice
		int secDice; // Result of second dice
		int nRolls; // Current number of rolls

		public Player(String name, int amount){
			this.name = name;
			this.amount = amount;
		}
	}

	/**************************************************
	 * Testing
	 *
	 * Test are logical expressions that should evaluate to true (and then be
	 * written out) No testing of IO methods Uncomment in program() to run test
	 * (only)
	 ***************************************************/
	void test() {
		// A few hard coded player to use for test
		// NOTE: Possible to debug tests from here, very efficient!
		Player[] ps = { new Player("",0), new Player("",0), new Player("",0) };
		ps[0].fstDice = 2;
		ps[0].secDice = 6;
		ps[1].fstDice = 6;
		ps[1].secDice = 5;
		ps[2].fstDice = 1;
		ps[2].secDice = 1;

		out.println(getScore(ps[0]) == 62);
		out.println(getScore(ps[1]) == 65);
		out.println(next(ps, ps[0]) == ps[1]);
		out.println(getLoser(ps) == ps[0]);

		exit(0);
	}

}
