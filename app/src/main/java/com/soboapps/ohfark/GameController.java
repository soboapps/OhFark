package com.soboapps.ohfark;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.style.StyleSpan;
import android.view.View;

import java.util.ArrayList;


public class GameController extends PreferenceActivity {

		private ArrayList<Player> players = new ArrayList<Player>();

        private int round = 0;
        public boolean isPlayerPastWinScore = false;
        public boolean didPlayerGetGOB = false;
        private boolean negateFarklePenalty = false;

        private DieManager dM;
        private OhFarkActivity UI;

        private Player currPlayer;
        private Player lastPlayer;

        private int WINNING_SCORE;
        private int GOB_SCORE;
        private boolean isRoundEnded = true;
        boolean showOverlay;

        public GameController(OhFarkActivity ui) {

        		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ui);

                WINNING_SCORE = Integer.valueOf(prefs.getString("winScorePref", "10000"));
                
                GOB_SCORE = Integer.valueOf(prefs.getString("gobScorePref", "0"));
                
               
                showOverlay = prefs.getBoolean("overlaypref", true);

                setupPlayers(ui);

                dM = new DieManager();
                UI = ui;
                updateUIScore();

                currPlayer = players.get(round % players.size());
                lastPlayer = null;

                UI.rollButtonState(true);
                UI.scoreButtonState(false);
                UI.finalRoundButtonState(false);
                UI.finalRoundLayout(false);
                UI.winnerLayout(false);
        }



        // Load the number of players from Preferences
        // and make the right number of Player Objects
        private void setupPlayers(Context c) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
                
                int numOfPlayers = Integer.valueOf(prefs.getString("playerNumPref", "2"));

                for (int i = 1; i <= numOfPlayers; i++) {
                        String temp = prefs.getString("Player " + i + "NamePref", "Player " + i);
                        players.add(new Player(temp));
                }

        }

        // Always called from onRoll method
        private void startRound() {
                isRoundEnded = false;
                currPlayer = players.get(round % players.size());
                currPlayer.resetInRoundScore();
                updateUIScore();
                
             // Show the Current PLayers Number of Farkles
                String fmessage = "";    
                fmessage += UI.getString(R.string.stYouHave) + " " + currPlayer.getNumOfFarkles() + " " +  UI.getString(R.string.stFarkles);
                UI.updatenumOfFarkles(UTILS.setSpanBetweenTokens(fmessage, "$$", new StyleSpan(Typeface.NORMAL)));
                

                // TODO Check to see if @currPlayer is AI    
        }

        // Can be called from onRoll or onScore
        private void endRound(boolean isFarkle) {

                // If the player rolled a farkle then the roll button will get enabled
                // after the animation ends in GameScreen
        		
                if (!isFarkle)
                		
                        UI.rollButtonState(true);
                		UI.scoreButtonState(false);
                		
                		String pscore = UI.getString(R.string.stScore);
                        UI.updatepPoints(UTILS.setSpanBetweenTokens(pscore, "$$", new StyleSpan(Typeface.BOLD)));
                		
                // If the player has rolled his 3rd farkle and the option is enabled, show penalty
                SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(UI);
                int farklePenaltyScore = Integer.valueOf(pref.getString("farklePenaltyPref", "-1000"));
                
                // Checks to see if Farkle Penalty is on
        	    if (pref.getBoolean("farklePenalty", true)) {
        	    	if (isFarkle && currPlayer.getNumOfFarkles() == 3) {
        	    		currPlayer.setInRoundScore(farklePenaltyScore);
                        // Show a -1000 next to his name
                        updateUIScore();
                        // There is a penalty *See animationsEnded()
                        negateFarklePenalty = true;
        	    	}
        	    }
        	    
                // Alert the UI a farkle is rolled
                UI.updateImages(false, isFarkle);
                
                // If its not a farkle then just clear the table and Update the images
                if (!isFarkle) {
                        dM.clearTable();
                        UI.updateImages(false, false);
                }

                isRoundEnded = true;

                round++;
                lastPlayer = currPlayer;
                currPlayer = players.get(round % players.size());

                // Usually updateUIScore() is called to erase the "+Score" but it
                // shouldn't be called if the last player farkled three times because it
                // erases the -1000
                if (lastPlayer.getNumOfFarkles() != 3)
                        updateUIScore();

                if (!isFarkle && currPlayer.hasHighestScore()) {
                    alertOfWinner();
                    
                }
                
        }

        // Makes an Button shows who won an loads GameOver Layout
        private void alertOfWinner() {
                String wintext = UI.getString(R.string.stWinnerButton);


                for (Player p : players) {
                        p.getName();
                        players.indexOf(p);
                }

                if ((currPlayer.getScore()) > (lastPlayer.getScore())) {
                        String setWinnerText = currPlayer.getName() + " ";
                        UI.updateWinnerButtonText(setWinnerText + wintext);

                } else {
                        String setWinnerText = lastPlayer.getName() + " ";
                        UI.updateWinnerButtonText(setWinnerText + wintext);
                }

                // Play Sound
                SharedPreferences mySoundPref=PreferenceManager.getDefaultSharedPreferences(UI);
                if (mySoundPref.getBoolean("soundPrefCheck", true)) {
                        SoundManager.playSound(7, 1);  //Cheer
                        UI.winnerLayout.setVisibility(View.VISIBLE);
                        UI.winnerButtonState(true);

                }

                showGameOver();
        }

        // Finds the player with the Highest score
        private int findHighestScore() {
                for (Player p : players) {
                        if (p.hasHighestScore())
                                return players.indexOf(p);
                }
                return -1;
        }

        // Updates the ScoreBox
        private void updateUIScore() {
        	
                String message = "";

                for (Player p : players) {
                        if (p.equals(currPlayer)) {

                                int currInRoundScore = p.getInRoundScore();
                                String temp = "$$            ";
                                String sign = (currInRoundScore > 0) ? "+" : "";

                                if (currInRoundScore != 0 && !isRoundEnded)
                                        temp = "$$   " + sign + currInRoundScore + "    ";

                                message += "$$" + p.getName() + temp + p.getScore() + "\n";

                        } else {
                                message += p.getName() + "             " + p.getScore() + "\n";

                        }

                }

                // The $$ tell setSpanBetweenTokens() what to make bold
                UI.updateScoreBox(UTILS.setSpanBetweenTokens(message, "$$", new StyleSpan(Typeface.BOLD)));

        }
        
        // Number of Current Die remaining on the table
        public int numOnTable() {
            return dM.numOnTable();
        }

        // Called when the roll button is clicked or can be called from the AI
        public void onRoll() {

                UI.finalRoundLayout.setVisibility(View.GONE);
     
	        	// Play Sound
	        	SharedPreferences mySoundPref=PreferenceManager.getDefaultSharedPreferences(UI);
	        	if (mySoundPref.getBoolean("soundPrefCheck", true)) {
	        		
	        		if (dM.numDiceRemain() == 5){
	        			SoundManager.playSound(5, 1);  //5 DiceRoll
	        		}
	        		if (dM.numDiceRemain() == 4){
	        			SoundManager.playSound(4, 1);  //5 DiceRoll
	        		}
	        		if (dM.numDiceRemain() == 3){
	        			SoundManager.playSound(3, 1);  //5 DiceRoll
	        		}
	        		if (dM.numDiceRemain() == 2){
	        			SoundManager.playSound(2, 1);  //5 DiceRoll
	        		}
	        		if (dM.numDiceRemain() == 1){
	        			SoundManager.playSound(1, 1);  //5 DiceRoll
	        		}
	        		if (dM.numDiceRemain() == 0){
	        			SoundManager.playSound(6, 1);  //6 DiceRoll
		        	}
	            }
	        	
        		// Reset the Current Dice Score and Pass Buttondisplay
        		String message = "";    
                //message += "Dice Score: 0";
                message += (UI.getString(R.string.stDiceScore)) + ": 0";
                UI.updateCurrentScore(UTILS.setSpanBetweenTokens(message, "$$", new StyleSpan(Typeface.BOLD)));
                
                // onRoll
                if (isRoundEnded)
                        startRound();

                UI.rollButtonState(false);
                UI.scoreButtonState(false);

                // If there are dice highlighted then its not the first roll of the
                // round
                if (dM.getHighlighted(DieManager.INDEX_FLAG).length > 0) {

                        int scoreOnTable = Scorer.calculate(
                                        dM.getHighlighted(DieManager.ABS_VALUE_FLAG), true, UI);

                        currPlayer.incrementInRoundScore(scoreOnTable);
                        // Show how much was picked up
                        updateUIScore();

                        dM.pickUp(dM.getHighlighted(DieManager.INDEX_FLAG));
                }

                dM.rollDice();
                UI.updateImages(true, false);

                // If the highest score possible on the table is 0 then its a farkle
                if (Scorer.calculate(dM.diceOnTable(DieManager.ABS_VALUE_FLAG), true,
                                UI) == 0) {
                        currPlayer.setInRoundScore(0);
                        currPlayer.incrementNumOfFarkles();
                        // True because its a Farkle
                        endRound(true);
                        // Else just show what the player rolled
                } else
                        UI.updateImages(true, false);
        }

        // When a dice is clicked the GameController determines which dice to
        // highlight and whether to enable the buttons
        public void onClickDice(int index, boolean isFarkle) {
        	
	        	// Play Select Sound
	        	SharedPreferences mySoundPref=PreferenceManager.getDefaultSharedPreferences(UI);
	        	if (mySoundPref.getBoolean("soundPrefCheck", true)) {
		    			SoundManager.playSound(10, 1);  // Select Sound
	        	}	        	
	        	
        		String message = "";
        		String pscore = "";
        	
                if (isRoundEnded)
                        return;

                int value = dM.getValue(index, DieManager.ABS_VALUE_FLAG);

                // A letter was clicked
                if (value == 0)
                        return;

                // Always highlight 5's and 1's
                if (value == 5 || value == 1) {

                        dM.toggleHighlight(index);

                } else {

                        // This method determines if the dice should be highlighted
                        if (shouldHighlight(index)) {

                                // if true then get the pairs of this dice
                                int[] pairs = dM.findPairs(index, DieManager.INDEX_FLAG);

                                // Highlight them
                                for (int i : pairs)
                                        dM.toggleHighlight(i);

                                // And highlight the original dice
                                dM.toggleHighlight(index);
                        }
                }

                // The score of the highlighted dice
                int highlightedScore = Scorer.calculate(dM.getHighlighted(DieManager.ABS_VALUE_FLAG), false, UI);
                // Second parameter is false to not ignore any extra dice

                // The score the player picked up
                int possibleScore = currPlayer.getInRoundScore() + highlightedScore + currPlayer.getScore();
                
                // Display the Current Score of the selected Dice
                //message += "Dice Score: " + highlightedScore;
                message += (UI.getString(R.string.stDiceScore)) + ": " + highlightedScore;
                UI.updateCurrentScore(UTILS.setSpanBetweenTokens(message, "$$", new StyleSpan(Typeface.BOLD)));
                
                // Display the Potential Points in the Score Button
                int ppossibleScore = currPlayer.getInRoundScore() + highlightedScore;
                
                pscore = "+ " + ppossibleScore;
                UI.updatepPoints(UTILS.setSpanBetweenTokens(pscore, "$$", new StyleSpan(Typeface.BOLD)));
       
                if (highlightedScore != 0) {
                        UI.rollButtonState(true);

                        // Play Hot Dice sound
        	        	if (mySoundPref.getBoolean("soundPrefCheck", true)) {
	    	        		if (dM.numDiceRemain() == 0){
	    	        			SoundManager.playSound(8, 1);  // HotDice Sound
	    	        		}
        	        	}
                        
                        // Tells it when to enable the Score Button
                        // based on the Get-On-Board Value
                        if (possibleScore >= GOB_SCORE) {
                    		UI.scoreButtonState(true);
                        }

                        // If a player is past the winning score
                        if (isPlayerPastWinScore) {

                                // Disable the score button
                                UI.scoreButtonState(false);

                                // The highest score so far
                                int scoreOfWinnerToBe = players.get(findHighestScore())
                                                .getScore();

                                // If the player can get a higher score then enable the score
                                // button
                                if (possibleScore > scoreOfWinnerToBe) {
                                        UI.scoreButtonState(true);
                                }
                        }

                        // Highlighted score = 0
                } else {
                        UI.rollButtonState(false);
                        UI.scoreButtonState(false);
                }

                UI.updateImages(false, false);
        }

        // Current Score that is possible if banked
        public int CurrScoreOnTable() {
            int highlightedScore = Scorer.calculate(dM.getHighlighted(DieManager.ABS_VALUE_FLAG), false, UI);
            int possibleScore = lastPlayer.getInRoundScore() + highlightedScore;
			return possibleScore;
        }
        
        // Determines whether to highlight the dice clicked
        private boolean shouldHighlight(int index) {

                // Gets the pairs (if any)
                int[] pairs = dM.findPairs(index, DieManager.INDEX_FLAG);

                int[] valuesOfPairs = new int[pairs.length + 1];
                
                // Takes the value of the pairs and puts them into the valueOfPairs
                // array
                System.arraycopy(dM.findPairs(index, DieManager.ABS_VALUE_FLAG), 0, valuesOfPairs, 0, pairs.length);
                
                // Adds the dice that was clicked to the array
                valuesOfPairs[pairs.length] = dM.getValue(index, DieManager.ABS_VALUE_FLAG);
                
                // True if the value of the pairs and the dice clicked is 0
                boolean isZero = Scorer.calculate(valuesOfPairs, false, UI) == 0;

                int[] diceOnTable = dM.diceOnTable(DieManager.ABS_VALUE_FLAG);
                
                // True is there are three pairs on the table
                boolean isThreePair = Scorer.isThreePair(diceOnTable, UI, true) != 0;
                
                // True if there is a straight on the table
                boolean isStraight = Scorer.isStraight(diceOnTable, UI, true) != 0;

                return (!isZero || isThreePair || isStraight);
        }

        // Called when the score button is clicked. Can be called from the AI
        public void onScore() {
        	
	    		// Reset the Current Dice Score and Pass Button display
	    		String message = "";    
	           // message += "Dice Score: 0";
	            
	            message += (UI.getString(R.string.stDiceScore)) + ": 0";
	            
	            UI.updateCurrentScore(UTILS.setSpanBetweenTokens(message, "$$", new StyleSpan(Typeface.BOLD)));
	            
                String pscore = UI.getString(R.string.stScore);
                UI.updatepPoints(UTILS.setSpanBetweenTokens(pscore, "$$", new StyleSpan(Typeface.BOLD)));

                // Player Scored! Reset his farkles
                currPlayer.resetNumOfFarkles();
                // Values of highlighted dice on table
                int[] highlighted = dM.getHighlighted(DieManager.ABS_VALUE_FLAG);
                // Score of the highlighted dice
                int onTable = Scorer.calculate(highlighted, false, UI);
                // The players new Score
                int newScore = currPlayer.getScore() + onTable + currPlayer.getInRoundScore();
                currPlayer.setScore(newScore);
                
                if (newScore >= WINNING_SCORE) {
                	//SharedPreferences myPrefs=PreferenceManager.getDefaultSharedPreferences(this);
                        isPlayerPastWinScore = true;
                        // If no one is over the WINNING_SCORE findWinnerToBe() returns -1
                        if (findHighestScore() == -1) {
                                // Player is the first one to pass the score
                                currPlayer.setOriginalWinner(true);
                                currPlayer.setHasHighestScore(true);
                                
                            	// Play Sound
                            	SharedPreferences mySoundPref=PreferenceManager.getDefaultSharedPreferences(UI);
                            	if (mySoundPref.getBoolean("soundPrefCheck", true)) {
                            		SoundManager.playSound(12, 1);  //Final Round
                                        //Display the Final Round Button
                                    UI.finalRoundLayout.setVisibility(View.VISIBLE);
                                    UI.finalRoundButtonState(true);
                            	}

                        } else {
                                currPlayer.setHasHighestScore(true);
                        }
                }

                currPlayer.resetInRoundScore();
                // False because its not a Farkle
                
                endRound(false);
                
             // Show the Current PLayers Number of Farkles
                String fmessage = "";    
                fmessage += UI.getString(R.string.stYouHave) + " " + currPlayer.getNumOfFarkles() + " " + UI.getString(R.string.stFarkles);
                UI.updatenumOfFarkles(UTILS.setSpanBetweenTokens(fmessage, "$$", new StyleSpan(Typeface.NORMAL)));
                
                
        }

        // Hide Final Round Button
        public void lastRound() {
                UI.finalRoundLayout.setVisibility(View.INVISIBLE);
                UI.finalRoundButtonState(false);

        }

        // Used to call GameOver.class
        private void showGameOver() {
            
            Intent intent=new Intent();
            intent.setClass(UI, GameOver.class);
            finish();
            UI.startActivity(intent);
        	
		}

		public int getImage(int index) {
                return dM.getImage(index);
        }

        public void newUI(OhFarkActivity newUI) {
                UI = newUI;
        }

        // Should not animate if value of dice is 0 ie its a letter
        public boolean shouldAnimate(int index) {
                return (dM.getValue(index, DieManager.VALUE_FLAG) > 0);
        }

        public void clearTable() {
                dM.clearTable();
        }

        // Called dice roll animation is ended. If a player rolled a Farkle this
        // gets called at the end of the Farkle Animation instead
        public void animationsEnded(boolean isFarkle) {
                // If the player up is the original winner its time to announce who won
	            if (isFarkle && currPlayer.hasHighestScore()) {
	                alertOfWinner();
	            }
	            
	         // If negateFarklePenalty is true then there is a Negative Penalty Score
             // next to a player's name. Its time to take that off and negate the score
	         // from the player
	        	SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(UI);
	        	int farklePenaltyScore = Integer.valueOf(pref.getString("farklePenaltyPref", "-1000"));
	        	
	        	// Checks to see if Farkle Penalty is on
	        	if (pref.getBoolean("farklePenalty", true)) {
	        		if (isFarkle && negateFarklePenalty && lastPlayer.getNumOfFarkles() == 3) {
	        			
                    	lastPlayer.incrementScore(farklePenaltyScore);
                    	
                    	// Checks to see if a Negative Score is allowed after a Farkle
                    	// Penalty has occurred
                        if (pref.getBoolean("allowNegScore", false)){
						if (lastPlayer.getScore() <= 0)
							lastPlayer.setScore(0);
                        }
                        
                        //lastPlayer.resetNumOfFarkles();
                        updateUIScore();
	        		}
	        	}
	        	// Show the Current PLayers Number of Farkles
                String fmessage = "";    
                fmessage += UI.getString(R.string.stYouHave) + " " + currPlayer.getNumOfFarkles() + " " +  UI.getString(R.string.stFarkles);
                UI.updatenumOfFarkles(UTILS.setSpanBetweenTokens(fmessage, "$$", new StyleSpan(Typeface.NORMAL)));
	        	// TODO Alert AI that animations ended
        }

}