package render;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Mini on 27-08-2015.
 */
public class Flasher implements ActionListener {
    double dr;
    double dg;
    double db;
    private Color col1;
    private Color col2;
    private String action;
    private Component cmp;
    private Timer foo;
    private int spd;
    private boolean reset;
    private boolean resetting;
    private int i = 0;

    public void Fade(Color start, Color end, Component cmpt, int speed, boolean reset) {
        action = "fade";
        col1 = start;
        col2 = end;
        dr = (end.getRed() - start.getRed()) / 100.0;
        dg = (end.getGreen() - start.getGreen()) / 100.0;
        db = (end.getBlue() - start.getBlue()) / 100.0;
        cmp = cmpt;
        spd = speed;
        this.reset = reset;
        foo = new Timer(spd, this);
        foo.start();
    }

    public void FadeBackground(Color start, Color end, Component cmpt, int speed, boolean reset) {
        action = "fadeBackground";
        col1 = start;
        col2 = end;
        dr = (end.getRed() - start.getRed()) / 100.0;
        dg = (end.getGreen() - start.getGreen()) / 100.0;
        db = (end.getBlue() - start.getBlue()) / 100.0;
        cmp = cmpt;
        spd = speed;
        this.reset = reset;
        foo = new Timer(spd, this);
        foo.start();
    }

    public void Flash(Color color, Component cmpt, int flashes, int speed) {
        action = "flash";
        col1 = color;
        cmp = cmpt;
        i = flashes * 2;
        foo = new Timer(speed, this);
        foo.start();
    }

    public void FlashBackground(Color color, Component cmpt, int flashes, int speed) {
        action = "flashBackground";
        col1 = color;
        cmp = cmpt;
        i = flashes * 2;
        foo = new Timer(speed, this);
        foo.start();
    }

    public void actionPerformed(ActionEvent e) {
        if (action.equals("fade")) {
            if (!resetting) {
                i++;
                cmp.setForeground(new Color((int) (col1.getRed() + (i * dr)), (int) (col1.getGreen() + (i * dg)), (int) (col1.getBlue() + (i * db))));
            }
            if (reset && resetting) {
                i++;
                cmp.setForeground(new Color((int) (col2.getRed() - (i * dr)), (int) (col2.getGreen() - (i * dg)), (int) (col2.getBlue() - (i * db))));
            }
            if (i >= 100 && reset && !resetting) {
                resetting = true;
                i = 0;
            }
            if (i >= 100 && !reset) foo.stop();
            if (i >= 100 && reset && resetting) foo.stop();

        } else if (action.equals("fadeBackground")) {
            if (!resetting) {
                i++;
                cmp.setBackground(new Color((int) (col1.getRed() + (i * dr)), (int) (col1.getGreen() + (i * dg)), (int) (col1.getBlue() + (i * db))));
            }
            if (reset && resetting) {
                i++;
                cmp.setBackground(new Color((int) (col2.getRed() - (i * dr)), (int) (col2.getGreen() - (i * dg)), (int) (col2.getBlue() - (i * db))));
            }
            if (i >= 100 && reset && !resetting) {
                resetting = true;
                i = 0;
            }
            if (i >= 100 && !reset) foo.stop();
            if (i >= 100 && reset && resetting) foo.stop();

        } else if (action.equals("flash")) {
            if (i % 2 == 0) {
                cmp.setForeground(col1);
            } else {
                cmp.setForeground(null);
            }
            i--;
            if (i == 0) foo.stop();

        } else if (action.equals("flashBackground")) {
            if (i % 2 == 0) {
                cmp.setBackground(col1);
            } else {
                cmp.setBackground(null);
            }
            i--;
            if (i == 0) foo.stop();
        }
    }
}
