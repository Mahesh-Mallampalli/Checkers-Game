import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


enum CheckerStatus { UNAVAILABLE, EMPTY, BLACK,	WHITE };


public class Checker extends JComponent{

    public Checker(CheckerStatus status){
        this.status = status;
    }


    @Override protected void paintComponent(Graphics g){

        /* draw the background */
        Graphics2D g2 = (Graphics2D) g;
        Color boardColor = status == CheckerStatus.UNAVAILABLE ? Color.WHITE : Color.GRAY;
        g2.setColor(boardColor);
        g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));


        if(highlighted)
            setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.BLUE));
        else
            setBorder(null);


        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if(status == CheckerStatus.BLACK){
            g2.setColor(Color.BLACK);
            g2.fillOval(0+10, 0+10, getWidth()-20, getHeight()-20);
        }
        else if(status == CheckerStatus.WHITE){
            g2.setColor(Color.WHITE);
            g2.fillOval(0+10, 0+10, getWidth()-20, getHeight()-20);
        }
    }


    public void highlight(boolean highlighted) {
        this.highlighted = highlighted;
        repaint();
    }


    public CheckerStatus getStatus(){ return status; }


    public void changeStatus(CheckerStatus status){
        if(status == CheckerStatus.UNAVAILABLE) return;
        this.status = status;
        repaint();
    }


    private CheckerStatus status;

    private boolean highlighted = false;


}
