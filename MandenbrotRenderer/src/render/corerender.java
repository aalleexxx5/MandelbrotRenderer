package render;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

// Created on 03-02-2015.
public class corerender implements ActionListener {
    double percnt;

    BufferedImage render(int AREAX, int AREAY, int LOOP_LIMIT, int REEL_MAX, int REEL_MIN, int IMAG_MAX, int IMAG_MIN, String[] coloring) {
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
                    g.setColor(AdvColorPix(count, coloring));
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

    Color AdvColorPix(int count, String[] ColorIndex) {
        int c1;
        int c2;
        int r;
        int g;
        int b;
        int or; //old red
        int og;
        int ob;
        double dr;
        double dg;
        double db;
        String fg; //foreground colour
        String bg; //background colour
        ArrayList<Integer> ClrVal = new ArrayList<>();
        for (int i = 0; i <= (count / 255) - 1; i++) ClrVal.add(255);
        if (ClrVal.size() < ColorIndex.length) ClrVal.add(count % 255);
        if (ClrVal.size() >= 2) {
            fg = String.valueOf(ColorIndex[ClrVal.size() - 2]);
            or = Integer.valueOf(fg.substring(0, 3));
            og = Integer.valueOf(fg.substring(3, 6));
            ob = Integer.valueOf(fg.substring(6, 9));
            c2 = Integer.valueOf(String.valueOf(ClrVal.get(ClrVal.size() - 1)));
            bg = String.valueOf(ColorIndex[ClrVal.size() - 1]);

            dr = ((Integer.valueOf(bg.substring(0, 3)) - Integer.valueOf(fg.substring(0, 3))) / 256.0);
            dg = ((Integer.valueOf(bg.substring(3, 6)) - Integer.valueOf(fg.substring(3, 6))) / 256.0);
            db = ((Integer.valueOf(bg.substring(6, 9)) - Integer.valueOf(fg.substring(6, 9))) / 256.0);
            r = (int) ((or) + (c2 * dr));
            g = (int) ((og) + (c2 * dg));
            b = (int) ((ob) + (c2 * db));

        } else {
            c1 = Integer.valueOf(String.valueOf(ClrVal.get(ClrVal.size() - 1)));
            fg = String.valueOf(ColorIndex[ClrVal.size() - 1]);

            dr = (Integer.valueOf(fg.substring(0, 3)) / 256.0);
            dg = (Integer.valueOf(fg.substring(3, 6)) / 256.0);
            db = (Integer.valueOf(fg.substring(6, 9)) / 256.0);
            r = (int) (c1 * dr);
            g = (int) (c1 * dg);
            b = (int) (c1 * db);
        }
        if (r > 255 || g > 255 || b > 255 || r < 0 || g < 0 || b < 0) {
            System.out.println(r + "," + g + "," + b);
            return Color.black;
        } else {
            return new Color(r, g, b);
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
