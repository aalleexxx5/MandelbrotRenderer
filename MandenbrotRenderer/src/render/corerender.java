package render;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

// Created on 03-02-2015.
public class corerender implements ActionListener {
    double percnt;

    BufferedImage render(int AREAX, int AREAY, int LOOP_LIMIT, int REEL_MAX, int REEL_MIN, int IMAG_MAX, int IMAG_MIN) {
        BufferedImage image = new BufferedImage(AREAX, AREAY, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.createGraphics();
        double dpcnt; //Delta percent
        dpcnt = 99.99 / AREAY;
        percnt = 0;
        double Dx = (REEL_MAX - REEL_MIN) / AREAX;                 //Her er byttet om
        double Dy = (IMAG_MAX - IMAG_MIN) / AREAY;                 //Her er byttet om
        //Dx = -0.00357142857142857142857142857143;
        //Dy = -0.00357142857142857142857142857143;
        double x = REEL_MIN;
        double y = IMAG_MIN;
        for (int i = 0; i < AREAY; i++) {
            percnt = percnt + dpcnt;
            for (int j = 0; j < AREAX; j++) {
                int count = 0;
                double p0 = x;
                double q0 = y;
                double LIMIT = 20.0;
                for (int k = 0; Math.abs(p0) <= LIMIT && Math.abs(q0) <= LIMIT && k < LOOP_LIMIT; k++) {
                    double p1 = p0 * p0 - q0 * q0 + x;
                    double q1 = 2 * p0 * q0 + y;
                    p0 = p1;
                    q0 = q1;
                    count++;
                }
                if (Math.abs(p0) < LIMIT && Math.abs(q0) < LIMIT) {
                    g.setColor(Color.black);
                } else {
                    //g.setColor(farve);
                    g.setColor(AdvColorPix(count));
                }
                g.drawLine(j, i, j, i);
                x = x + Dx;
            }
            x = REEL_MIN;
            y = y + Dy;
        }
        percnt = 100.0;
        return image;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
