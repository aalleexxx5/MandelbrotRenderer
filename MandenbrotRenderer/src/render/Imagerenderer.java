package render;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

// Created on 25-01-2015.
public class Imagerenderer implements Runnable {
    private Thread t;
    int size;
    double reel_max;
    double reel_min;
    double imag_max;
    double imag_min;
    String[] coloring;
    String filename;
    public void params(int Size, double Reel_max, double Reel_min, double Imag_max, double Imag_min, String[] Coloring, String Filename) {
        this.size = Size;
        this.reel_max = Reel_max;
        this.reel_min = Reel_min;
        this.imag_max = Imag_max;
        this.imag_min = Imag_min;
        this.coloring = Coloring;
        this.filename = Filename;
    }

    public void start ()
    {
        System.out.println("Starting " +  "renderThread" );
        if (t == null)
        {
            t = new Thread (this, "renderThread");
            t.start ();
        }
    }

    public void run() {
        System.out.println("renderThread is rendering");
        int picareax =1024;
        int picareay =1024;
        if (size > 0) picareax = picareay = 1024 * size;
        BufferedImage image = new BufferedImage(picareax, picareay, BufferedImage.TYPE_INT_RGB);
        Graphics w = image.createGraphics();
            double Dx = (reel_max - reel_min) / picareax;
            double Dy = (imag_max - imag_min) / picareay;
            int currentAreaY = picareay;
            int currentAreaX = picareax;
        //Dx = -0.00357142857142857142857142857143;
        //Dy = -0.00357142857142857142857142857143;
        double p0;
        double q0;
        double x = reel_min;
        double y = imag_min;
        double p1 = 0;
        double q1 = 0;
        double LIMIT = 20.0;
        int LOOP_LIMIT = coloring.length*255;
        int count;

        for (int i = 0; i < currentAreaY; i++) {
            for (int j = 0; j < currentAreaX; j++) {
                count = 0;
                p0 = x;
                q0 = y;
                for (int k = 0; Math.abs(p0) <= LIMIT && Math.abs(q0) <= LIMIT && k < LOOP_LIMIT; k++) {
                    p1 = p0 * p0 - q0 * q0 + x;
                    q1 = 2 * p0 * q0 + y;
                    p0 = p1;
                    q0 = q1;
                    count++;
                }
                if (Math.abs(p0) < LIMIT && Math.abs(q0) < LIMIT) {
                    w.setColor(Color.black);
                } else {
                    w.setColor(ColorPix(count, coloring));
                }
                w.drawLine(j, i, j, i);
                x = x + Dx;
            }
            x = imag_max;
            y = y + Dy;
        }
        print(image, filename);
        System.out.println("renderThread completed");
    }
    private Color ColorPix(int count, String[] coloring){
        int c1;
        int c2 = 0;
        int c3;
        int r = 0;
        int g = 0;
        int b = 0;
        int cNum = coloring.length;
        int mod = count % 255;
        String fg;
        String bg = "Yellow";

        ArrayList ClrVal = new ArrayList();
        for (int i = 0; i <= (count / 255) - 1; i++) ClrVal.add(255);
        if (ClrVal.size() < cNum) ClrVal.add(count % 255);

        if (ClrVal.size() >= 2) {
            c1 = Integer.valueOf(String.valueOf(ClrVal.get(ClrVal.size() - 2)));
            fg = String.valueOf(coloring[ClrVal.size() - 2]);
            c2 = Integer.valueOf(String.valueOf(ClrVal.get(ClrVal.size() - 1)));
            bg = String.valueOf(coloring[ClrVal.size() - 1]);
        } else {
            c1 = Integer.valueOf(String.valueOf(ClrVal.get(ClrVal.size() - 1)));
            fg = String.valueOf(coloring[ClrVal.size() - 1]);
        }
        switch (fg) {
            case "Red":
                r = c1;
                break;
            case "Green":
                g = c1;
                break;
            case "Blue":
                b = c1;
                break;
            case "Cyan":
                g = b = c1;
                break;
            case "Magenta":
                r = b = c1;
                break;
            case "Yellow":
                r = g = c1;
                break;
            case "Orange":
                r = c1;
                g = c1/2;
                break;
            case "Forest":
                r = c1/2;
                g = c1;
                break;
            case "Turquoise":
                g = c1;
                b = c1/2;
                break;
            case "Sea":
                g = c1/2;
                b = c1;
                break;
            case "Violet":
                b = c1;
                r = c1/2;
                break;
            case "Lavender":
                b = c1/2;
                r = c1;
                break;
            case "White":
                r = g = b = c1;
                break;
            case "Black":
                r = g = b = 255 - c1;
                break;
            default:
                b = c1;
                break;
        }
        if (count > mod) {
            switch (bg) {
                case "Red":
                    if (r != 255) r = c2;
                    if (g != 0) g = c1 - c2;
                    if (b != 0) b = c1 - c2;
                    break;
                case "Green":
                    if (g != 255) g = c2;
                    if (r != 0) r = c1 - c2;
                    if (b != 0) b = c1 - c2;
                    break;
                case "Blue":
                    if (b != 255) b = c2;
                    if (r != 0) r = c1 - c2;
                    if (g != 0) g = c1 - c2;
                    break;
                case "Cyan":
                    if (g != 255) g = c2;
                    if (b != 255) b = c2;
                    if (r != 0) r = c1 - c2;
                    break;
                case "Magenta":
                    if (r != 255) r = c2;
                    if (b != 255) b = c2;
                    if (g != 0) g = c1 - c2;
                    break;
                case "Yellow":
                    if (r != 255) r = c2;
                    if (g != 255) g = c2;
                    if (b != 0) b = c1 - c2;
                    break;
                case "Orange":
                    if (r != 255) r = c2;
                    if (g != 127){
                        if (g < 255) g = c2/2;
                        if (g > 128) g=c1-c2/2;}
                    if (b != 0) b = c1 - c2;
                    break;
                case "Sea":
                    if (r != 0) r = c1 - c2;
                    if (g != 127){
                        if (g < 255) g = c2/2;
                        if (g > 128) g=c1-c2/2;}
                    if (b != 255) b = c2;
                    break;
                case "Violet":
                    if (r!= 127){
                        if (r < 255) r = c2/2;
                        if (r > 128) r=c1-c2/2;}
                    if (g != 0) g = c1 - c2;
                    if (b != 255) b = c2;
                    break;
                case "Lavender":
                    if (r != 255) r = c2;
                    if (g != 0) g = c1-c2;
                    if (b!=127){
                        if (b < 255) b = c2/2;
                        if (b > 128) b=c1-c2/2;}
                    break;
                case "Forest":
                    if (r != 127){
                        if (r < 255) r = c2/2;
                        if (r > 128) r=c1-c2/2;}
                    if (g != 255) g = c2;
                    if (b != 0) b = c1-c2;
                    break;
                case "Turquoise":
                    if (r != 0) r = c1 - c2;
                    if (g != 255) g = c2;
                    if (b!=127){
                        if (b < 255) b = c2/2;
                        if (b > 128) b=c1/2;}
                    break;
                case "White":
                    if (r != 255) r = c2;
                    if (g != 255) g = c2;
                    if (b != 255) b = c2;
                    break;
                case "Black":
                    if (r != 0) r = c1 - c2;
                    if (g != 0) g = c1 - c2;
                    if (b != 0) b = c1 - c2;
                    break;
                default:
                    r = g = c2;
                    b = c1 - c2;
                    break;
            }
        }
        return new Color(r, g, b);
    }
    private void print(BufferedImage bi, String filename) {
        try {
            // retrieve image
            if (!filename.isEmpty()) {
                File outputfile = new File(filename + ".png");
                ImageIO.write(bi, "png", outputfile);
            }
        } catch (IOException e){
        }
    }
}
