import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class CheckersGame {

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                gameFrame.pack();
                gameFrame.setVisible(true);
            }
        });
    }


    public static void resetPlayer(CheckerStatus status) {
        human.setChess(status);
        if(status == CheckerStatus.BLACK) {
            currentPlayer = human;
            computer.setChess(CheckerStatus.WHITE);
        } else {
            currentPlayer = computer;
            computer.setChess(CheckerStatus.BLACK);
        }
    }


    public static void humanChooseChecker(Location cell, CheckerStatus status) {
        if(status == CheckerStatus.EMPTY)
            human.goTo(cell);
        else if(status == human.getChess())
            human.startFrom(cell);
    }


    public static Player getCurrentPlayer() { return currentPlayer; }


    public static CBStatus getCurrentState() { return gameFrame.getCurrentState(); }


    public static void showMove(Action path) {
        gameFrame.showMove(path);
    }


    public static void updateState() {
        CBStatus state = gameFrame.getCurrentState();
        if(Agent.goalTest(state, currentPlayer.getChess())) {
            String msg = currentPlayer == human ? "Congratulations! You win!" : "Sorry, you lose.";
            JOptionPane.showMessageDialog(gameFrame, msg);
            gameFrame.setCheckerBoard(initCBS);
            resetPlayer(human.getChess());
        }
        else{
            currentPlayer = ((currentPlayer == human) ? computer : human);
            if(currentPlayer == computer)
                computer.move();
        }
    }

    public static boolean isMoving(){return gameFrame.isMoving();}


    private static HumanPlayer human = new HumanPlayer(CheckerStatus.BLACK);
    private static ComputerPlayer computer = new ComputerPlayer(CheckerStatus.WHITE);
    //private static HumanPlayer computer = new HumanPlayer(CheckerStatus.WHITE);
    private static Player currentPlayer = human;


    public static final CBStatus initCBS = new CBStatus( new int[][]{
            {3,0,3, 0, 3, 0, 3, 0},
            {0,3,0, 3, 0, 3, 0, 3},
            {3,0,3, 0, 3, 0, 3, 0},
            {0,1,0, 1, 0, 1, 0, 1},
            {1,0,1, 0, 1, 0, 1, 0},
            {0,2,0, 2, 0, 2, 0, 2},
            {2,0,2, 0, 2, 0, 2, 0},
            {0,2,0, 2, 0, 2, 0, 2}});

    private static GameFrame gameFrame = new GameFrame(initCBS);
}






