import java.awt.*;
import java.util.*;

public abstract class Player {

    private CheckerStatus chess;

    public Player(CheckerStatus chess) {
        this.chess = chess;
    }


    public void setChess(CheckerStatus chess) {
        this.chess = chess;
    }
    public CheckerStatus getChess() {
        return chess;
    }
    protected abstract void move();
}

class HumanPlayer extends Player {


    public HumanPlayer(CheckerStatus chessColor) {
        super(chessColor);
    }


    public void startFrom(Location from) {
        this.from = from;
    }


    public void goTo(Location to) {
        if(from != null) {
            this.to = to;
            move();
        }
    }

    protected void move() {
        Action action = Agent.findPath(CheckersGame.getCurrentState(), from, to);
        if(action != null){
            CheckersGame.showMove(action);
            from = null;
            to = null;
        }
    }

    private Location from;
    private Location to;
}




class ComputerPlayer extends Player {


    public ComputerPlayer(CheckerStatus chessColor) {
        super(chessColor);
    }


    protected void move() {
        CBStatus curState = CheckersGame.getCurrentState();
        // // System.out.println("computer starts calculate moving..............");
        // System.out.println(curState);
        Agent.SearchResult result = Agent.bestMove(curState, getChess());
        CheckersGame.showMove(result.action);
        System.out.println(String.format("max depth: %d, generated nodes: %d, %d prunings take place in maxValue and %d in minValue",
                Agent.maxDepth, Agent.nodeGenerated, result.maxPruning, result.minPruning));
    }

}
