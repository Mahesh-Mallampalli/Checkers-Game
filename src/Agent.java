import java.util.*;
import java.awt.*;


public class Agent {
    private static int cutoffDepth = 5;
    static int nodeGenerated = 0;
    static int maxDepth = 0;


    public enum HardLevel{
        BEGINNER(5);
        private int code;
        private HardLevel(int code) { this.code = code; }
        public int getCode() { return code; }
    }


    public static void setHardLevel(HardLevel level){ cutoffDepth = level.getCode(); }

    public static boolean goalTest(CBStatus state, CheckerStatus color) {
        CheckerStatus other = color == CheckerStatus.WHITE ? CheckerStatus.BLACK : CheckerStatus.WHITE;
        for(int i = 0; i < state.rows(); i++)
            for(int j = 0; j < state.columns(); j++)
                if(state.get(i, j) == other && isMovable(state, new Location(i,j)))
                    return false;
        return true;
    }


    public static SearchResult bestMove(CBStatus state, CheckerStatus rep) {
        nodeGenerated = 0;
        maxDepth = 0;
        return maxValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
    }


    public static class SearchResult{
        public Action action;
        public int value;
        public int maxPruning;
        public int minPruning;
        public SearchResult(Action a, int v, int p1, int p2) {
            action = a;
            value = v;
            maxPruning = p1;
            minPruning = p2;
        }
        public SearchResult(){}


        public String toString(){
            StringBuilder sb = new StringBuilder("Action:\n");
            if(action != null)
                for(int i = 0; i < action.size(); i++) {
                    Step s = action.get(i);
                    if(s != null)
                        sb.append("\t" + s.toString() + "\n");
                }
            sb.append("value: " + value);
            sb.append("\nmaxPruning: " + maxPruning);
            sb.append("\nminPruning: " + minPruning);
            return sb.toString();
        }
    }


    private static SearchResult maxValue(CBStatus state, int alpha, int beta, int depth) {

        nodeGenerated++;
        if(cutoffTest(state, depth))
            return new SearchResult(null, evaluate(state), 0, 0);

        CheckerStatus rep = CheckersGame.getCurrentPlayer().getChess();
        SearchResult result = new SearchResult(null, Integer.MIN_VALUE, 0, 0);
        Action action = possibleActions(state, rep);
        try {
            for(int i = 0; i < action.size(); i++) {
                Step step = action.get(i);
                if(step == null) continue;
                CBStatus nextState = state.clone();
                Action path = findPath(nextState, step.from, step.to);
                // System.out.println(nextState);
                SearchResult minRes = minValue(nextState, alpha, beta, depth+1);
                result.maxPruning += minRes.maxPruning;
                result.minPruning += minRes.minPruning;
                if(result.value < minRes.value) {
                    result = minRes;
                    result.action = path;
                }
                if(result.value >= beta) {
                    result.maxPruning++;
                    return result;
                }
                alpha = alpha > result.value ? alpha : result.value;
            }
        } catch (CloneNotSupportedException e) {};
        return result;
    }


    private static SearchResult minValue(CBStatus state, int alpha, int beta, int depth) {

        nodeGenerated++;
        if(cutoffTest(state, depth))
            return new SearchResult(null, evaluate(state), 0, 0);

        CheckerStatus rep = CheckersGame.getCurrentPlayer().getChess();
        rep = rep == CheckerStatus.WHITE ? CheckerStatus.BLACK : CheckerStatus.WHITE;
        SearchResult result = new SearchResult(null, Integer.MAX_VALUE, 0, 0);
        Action action = possibleActions(state, rep);
        try {
            for(int i = 0; i < action.size(); i++) {
                Step step = action.get(i);
                if(step == null) continue;
                CBStatus nextState = state.clone();
                Action path = findPath(nextState, step.from, step.to);

                rep = rep == CheckerStatus.WHITE ? CheckerStatus.BLACK : CheckerStatus.WHITE;
                SearchResult maxRes = maxValue(nextState, alpha, beta, depth+1);
                result.maxPruning += maxRes.maxPruning;
                result.minPruning += maxRes.minPruning;
                if(result.value > maxRes.value) {
                    result = maxRes;
                    result.action = path;
                }
                if(result.value <= alpha) {
                    result.minPruning++;
                    return result;
                }
                beta = beta < result.value ? beta : result.value;
            }
        } catch (CloneNotSupportedException e) {};
        return result;
    }


    private static boolean cutoffTest(CBStatus state, int depth) {
        if(maxDepth < depth){ maxDepth = depth; }
        return (depth == cutoffDepth || goalTest(state, CheckerStatus.WHITE) || goalTest(state, CheckerStatus.BLACK));
    }


    private static int evaluate(CBStatus state) {
        CheckerStatus rep = CheckersGame.getCurrentPlayer().getChess();
        CheckerStatus other = rep == CheckerStatus.WHITE ? CheckerStatus.BLACK : CheckerStatus.WHITE;
        if(goalTest(state, rep)) return 1000;
        if(goalTest(state, other)) return -1000;

        int deltaChess = 0;
        int deltaMoves = 0;
        int deltaJumps =0;
        for(int i = 0; i < state.rows(); i++) {
            for(int j = 0; j < state.columns(); j++) {
                if(state.get(i,j) == rep){
                    deltaChess += 1;
                    Action moves = possibleMoves(state, new Location(i,j));
                    Action jumps = possibleJumps(state, new Location(i,j));
                    if(moves != null) deltaMoves += moves.size();
                    if(jumps != null) deltaJumps += jumps.size();
                }
                else if(state.get(i,j) == other){
                    deltaChess -= 1;
                    Action moves = possibleMoves(state, new Location(i,j));
                    Action jumps = possibleJumps(state, new Location(i,j));
                    if(moves != null) deltaMoves -= moves.size();
                    if(jumps != null) deltaJumps -= jumps.size();
                }
            }
        }

        return 8*deltaChess + deltaMoves + 10*deltaJumps;
    }


    public static Action findPath(CBStatus state, Location from, Location to) {

        HowToMove how = moveTo(state, from, to);
        if(how == HowToMove.NONE) return null;
        Step step = new Step(from, to, how);


        Action result = new Action();
        result.add(step);
        makeAMove(state, step);
        if(step.how == HowToMove.JUMP) {

            Action moves = possibleJumps(state, to);
            while(moves != null && moves.size() > 0) {
                step = moves.get(0);
                result.add(step);
                makeAMove(state, step);
                moves = possibleJumps(state, step.to);
            }
        }
        return result;
    }


    private static void makeAMove(CBStatus state, Step step){
        // //System.out.println(step);
        state.set(step.to, state.get(step.from));
        state.set(step.from, CheckerStatus.EMPTY);
        if(step.how == HowToMove.JUMP)
            state.set((step.from.x + step.to.x)/2, (step.from.y + step.to.y)/2, CheckerStatus.EMPTY);
    }


    private static boolean isMovable(CBStatus state, Location from) {
        return canMove(state, from) || canJump(state, from);
    }


    private static boolean canMove(CBStatus state, Location from) {
        if(!state.contains(from)) return false;
        CheckerStatus self = state.get(from);
        if(self == CheckerStatus.UNAVAILABLE || self == CheckerStatus.EMPTY) return false;
        /* can move forward only */
        int[][] moveDistance = self == CheckerStatus.BLACK ? new int[][]{{-1, -1}, {-1, 1}} : new int[][]{{1, -1}, {1, 1}};
        for(int[] m:moveDistance) {
            Location to = new Location(from.x + m[0], from.y + m[1]);
            if(state.contains(to) && state.get(to) == CheckerStatus.EMPTY)
                return true;
        }
        return false;
    }


    private static boolean canJump(CBStatus state, Location from) {
        if(!state.contains(from)) return false;
        CheckerStatus self = state.get(from);
        if(self == CheckerStatus.UNAVAILABLE || self == CheckerStatus.EMPTY) return false;
        /* can jump forward or backword */
        CheckerStatus other = self == CheckerStatus.BLACK ? CheckerStatus.WHITE : CheckerStatus.BLACK;
        int[][] jumpDistance = new int[][]{{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
        for(int[] j:jumpDistance) {
            Location to = new Location(from.x + j[0], from.y + j[1]);
            Location mid = new Location(from.x + j[0]/2, from.y + j[1]/2);
            if(state.contains(to) && state.get(to) == CheckerStatus.EMPTY && state.get(mid) == other)
                return true;
        }
        return false;
    }


    private static Action possibleJumps(CBStatus state, Location from) {
        if(!state.contains(from)) return null;
        CheckerStatus self = state.get(from);
        if(self == CheckerStatus.UNAVAILABLE || self == CheckerStatus.EMPTY) return null;

        /* can jump forward or backword */
        Action steps = new Action();
        CheckerStatus other = self == CheckerStatus.BLACK ? CheckerStatus.WHITE : CheckerStatus.BLACK;
        int[][] jumpDistance = new int[][]{{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
        for(int[] j:jumpDistance) {
            Location to = new Location(from.x + j[0], from.y + j[1]);
            Location mid = new Location(from.x + j[0]/2, from.y + j[1]/2);
            if(state.contains(to) && state.get(to) == CheckerStatus.EMPTY && state.get(mid) == other)
                steps.add(new Step(from, to, HowToMove.JUMP));
        }
        return steps;
    }


    private static Action possibleMoves(CBStatus state, Location from) {
        if(!state.contains(from)) return null;
        CheckerStatus self = state.get(from);
        if(self == CheckerStatus.UNAVAILABLE || self == CheckerStatus.EMPTY) return null;

        /* can move forward only */
        Action steps = new Action();
        int[][] directions = self == CheckerStatus.BLACK ? new int[][]{{-1, -1}, {-1, 1}} : new int[][]{{1, -1}, {1, 1}};
        for(int[] d:directions) {
            Location to = new Location(from.x + d[0], from.y + d[1]);
            if(state.contains(to) && state.get(to) == CheckerStatus.EMPTY)
                steps.add(new Step(from, to, HowToMove.MOVE));
        }
        return steps;
    }


    private static HowToMove moveTo(CBStatus state, Location from, Location to) {
        if(state.get(to) != CheckerStatus.EMPTY) return HowToMove.NONE;

        CheckerStatus self = state.get(from);
        CheckerStatus other = self == CheckerStatus.WHITE ? CheckerStatus.BLACK : CheckerStatus.WHITE;
        int x = to.x - from.x;
        int y = to.y - from.y;
        int direction = self == CheckerStatus.BLACK ? -1 : 1;
        if(x == direction && y*y == 1)
            return HowToMove.MOVE;
        else if(x*x == 4 && y*y == 4 && state.get((from.x+to.x)/2, (from.y+to.y)/2) == other)
            return HowToMove.JUMP;
        else
            return HowToMove.NONE;
    }


    private static Action possibleActions(CBStatus state, CheckerStatus rep) {
        Action action = new Action();
        for(int i = 0; i < state.rows(); i++) {
            for(int j = 0; j < state.columns(); j++) {
                if(state.get(i,j) == rep){
                    action.combine(possibleMoves(state, new Location(i,j)));
                    action.combine(possibleJumps(state, new Location(i,j)));
                }
            }
        }
        return action;
    }
}


class Location {
    public int x;
    public int y;
    public Location(int x, int y) { this.x = x; this.y = y;}

    @Override public boolean equals(Object otherObj) {
        if(otherObj == this) return true;
        if(otherObj == null) return false;
        if(getClass() != otherObj.getClass()) return false;
        Location other = (Location) otherObj;
        return x == other.x && y == other.y;
    }

    public String toString(){ return String.format("(%d, %d)", x, y);}
}


enum HowToMove { NONE, MOVE, JUMP }

class Step {
    public HowToMove how;
    public Location from;
    public Location to;
    public Step(Location from, Location to, HowToMove how) {
        this.from = from;
        this.to = to;
        this.how = how;
    }


    public String toString() {
        return String.format(how.toString() + " from (" + from.x + ", " + from.y + ") to (" + to.x + ", " + to.y + ")");
    }
}


class CBStatus implements Cloneable{
    private CheckerStatus[][] CB;

    public CBStatus(int x, int y) {
        CB = new CheckerStatus[x][y];
    }

    public CBStatus(int[][] array) {
        int x = array.length;
        int y = array[0].length;
        CB = new CheckerStatus[x][y];
        for(int i = 0; i < x; i++)
            for(int j = 0; j < y; j++)
                CB[i][j] = CheckerStatus.values()[array[i][j]];
    }

    @Override public CBStatus clone() throws CloneNotSupportedException {
        CBStatus cb = (CBStatus)super.clone();
        cb.CB = CB.clone();
        for(int i = 0; i < cb.CB.length; i++)
            cb.CB[i] = CB[i].clone();
        return cb;
    }


    public String toString(){
        StringBuilder sb = new StringBuilder("------------------------\n");
        for(CheckerStatus[] row:CB) {
            sb.append("|");
            for(CheckerStatus cell:row){
                if(cell == CheckerStatus.WHITE)
                    sb.append("  O");
                else if(cell == CheckerStatus.BLACK)
                    sb.append("  @");
                else
                    sb.append("  -");
            }
            sb.append("  |\n");
        }
        return sb.toString();
    }

    public int rows() {return (CB!=null) ? CB.length : 0;}
    public int columns() {return ( CB!=null && CB.length != 0 )? CB[0].length : 0;}

    public CheckerStatus get(Location loc) { return CB[loc.x][loc.y]; }
    public CheckerStatus get(int x, int y) { return CB[x][y]; }
    public void set(Location loc, CheckerStatus s) { CB[loc.x][loc.y] = s; }
    public void set(int x, int y, CheckerStatus s) { CB[x][y] = s; }

    public boolean contains(Location loc) { return loc.x >= 0 && loc.x < rows() && loc.y >= 0 && loc.y < columns(); }
}


class Action {
    private ArrayList<Step> path = new ArrayList<Step>();
    public void add(Step s){ path.add(s); }
    public int size(){ return path.size(); }
    public Step get(int i){ return i<path.size() && i>=0 ? path.get(i) : null;}


    public void combine(Action another){
        if(another == null) return;
        for(int i = 0; i < another.size(); i++)
            path.add(another.get(i));
    }

    public String toString(){
        if(path.size()==0) return "";
        String str = "";
        for(Step s:path)
            str = str+s.toString();
        return str;
    }
}


