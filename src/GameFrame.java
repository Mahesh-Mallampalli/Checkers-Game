import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class GameFrame extends JFrame {

    public GameFrame(CBStatus CB) {
        setTitle("IIT2018137");
        cfgPanel = new ConfigPanel(this);
        add(cfgPanel, BorderLayout.EAST);
        checkerboard = new Checkerboard(CB);
        add(checkerboard, BorderLayout.CENTER);
    }

    public void setCheckerBoard(CBStatus CB) {
        checkerboard.resetCheckers(CB);
    }



    public CBStatus getCurrentState() {
        return checkerboard.getCurrentState();
    }


    public void showMove(Action path) {
        checkerboard.showMove(path);
    }


    public boolean isMoving(){return checkerboard.isMoving();}

    private Checkerboard checkerboard;
    private ConfigPanel cfgPanel;

}



class ConfigPanel extends JPanel{

    public ConfigPanel(final GameFrame owner){

        setLayout(new GridLayout(10, 1));

        ButtonGroup g1 = new ButtonGroup();
        final JRadioButton black = new JRadioButton("Black", true);

        g1.add(black);



        ButtonGroup g2 = new ButtonGroup();
        final JRadioButton beginner = new JRadioButton("beginner", true);

        g2.add(beginner);

        add(new JLabel());


        final JButton start = new JButton("Start");
        start.setPreferredSize(new Dimension(100, 30));
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(CheckersGame.isMoving())
                    return;
                CheckerStatus human = black.isSelected() ? CheckerStatus.BLACK : CheckerStatus.WHITE;
                CheckersGame.resetPlayer(human);

                if(beginner.isSelected()) Agent.setHardLevel(Agent.HardLevel.BEGINNER); // to be done

                owner.setCheckerBoard(CheckersGame.initCBS);
            }
        });
        JPanel p = new JPanel();
        p.add(start);
        add(p);
    }
}
