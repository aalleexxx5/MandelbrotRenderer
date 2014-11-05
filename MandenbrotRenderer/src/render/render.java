package render;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;

//@author alex

    public final class render extends JFrame implements MouseListener {
        public render() {
            loadConfig();
            initUI();
        }

        int AREAX = 700; // TODO: aspect ratio
        int AREAY = 700;
        int picareax = 4096;
        int picareay = 4096;
        int count;
        int cNum;
        int currentAreaX;
        int currentAreaY;
        int zoomLvl = 400;
        double REEL_MIN = -2.0; // min reelle værdi
        double REEL_MAX = 0.5; // max reelle værdi
        double IMAG_MIN = -1.25; // min imaginære værdi
        double IMAG_MAX = 1.25; // max imaginære værdi
        double LIMIT = 20.0; //grænseværdien før loopen stopper
        double LOOP_LIMIT = 509;//grænseværdien for while loop iterationer
        double p0, q0, p1, q1, x, y;
        double Dx = (REEL_MAX - REEL_MIN) / AREAX;                              //Her er byttet om
        double Dy = (IMAG_MAX - IMAG_MIN) / AREAY;//delta-x og delta-y          //Her er byttet om
        boolean toggleUI = false;
        boolean toggleComp;
        boolean printImage = false;
        Color farve;
        inout file = new inout();
        ArrayList Coloring = new ArrayList(); // lagre rekkefølgen af farver
        ArrayList ClrVal = new ArrayList(); //indeholder værdien af de enkelte farver

        JTextField MaxColors;
        JTextField Zoom;
        JComboBox clrnum;
        JComboBox clr;
        JButton sav;
        JButton restart;
        JCheckBox retain;

        public void initUI() {
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(AREAX, AREAY);
            setTitle("Fractals!!");
            setLocationRelativeTo(null);
            setLayout(null);
            addMouseListener(this);

            retain = new JCheckBox();
            retain.setBounds(180, 80, 20, 20);

            MaxColors = new JTextField();
            MaxColors.setBounds(50, 50, 50, 20);

            Zoom = new JTextField();
            Zoom.setBounds(50, 80, 50, 20);

            clrnum = new JComboBox();
            clrnum.addItem(1);
            clrnum.setBounds(200, 50, 100, 20);

            String[] colors = {"Red", "Green", "Blue", "Cyan", "Magenta", "Yellow", "White", "Black"};
            clr = new JComboBox(colors);
            clr.setBounds(330, 50, 100, 20);

            sav = new JButton("Img");
            sav.setBounds(110, 50, 60, 20);

            restart = new JButton("←");
            restart.setBounds(110, 80, 60, 20);

            Zoom.setText(String.valueOf(zoomLvl));
            MaxColors.setText(String.valueOf(Coloring.size()));
            clrnum.removeAllItems();
            for (int i = 0; i < Coloring.size(); i++) {
                clrnum.addItem(i);
            }

            restart.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    REEL_MIN = -2.0; // min reelle værdi
                    REEL_MAX = 0.5; // max reelle værdi
                    IMAG_MIN = -1.25; // min imaginære værdi
                    IMAG_MAX = 1.25; // max imaginære værdi
                    if (!retain.isSelected()) loadConfig();
                    toggleComp = false;
                    remove(MaxColors);
                    remove(clrnum);
                    remove(clr);
                    remove(sav);
                    remove(Zoom);
                    remove(restart);
                    remove(retain);
                    System.out.println("Closing Ui");
                    repaint();
                }
            });

            Zoom.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    zoomLvl = Integer.valueOf(Zoom.getText());
                }
            });

            sav.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    printImage = (!printImage);
                    if (printImage) System.out.println("creating image");
                    repaint();
                }
            });


            MaxColors.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    if (Integer.valueOf(MaxColors.getText()) > 0) {
                        clrnum.removeAllItems();
                        for (int i = 0; i < Integer.valueOf(MaxColors.getText()); i++) {
                            clrnum.addItem(i);
                        }
                    }
                }
            });

            clrnum.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    System.out.println("Ping!");
                    if (clrnum.getSelectedIndex() >= 0 && clrnum.getSelectedIndex() < Coloring.size()) {
                        System.out.println(Coloring.get(clrnum.getSelectedIndex()));
                        clr.setSelectedItem(Coloring.get(clrnum.getSelectedIndex()));
                    }
                }
            });

            clr.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    cNum = Coloring.size();
                    LOOP_LIMIT = 255 * cNum;
                    if (Coloring.size() <= clrnum.getSelectedIndex()) {
                        Coloring.add(clrnum.getSelectedIndex(), clr.getSelectedItem());
                    } else {
                        Coloring.set(clrnum.getSelectedIndex(), clr.getSelectedItem());
                    }
                }
            });
        }

        @Override
        public void paint(Graphics g) // har laves selve mandenbrot, der er her selve matematikken sker
        {
            BufferedImage image = new BufferedImage(picareax, picareay, BufferedImage.TYPE_INT_RGB);
            Graphics w = image.createGraphics();
            if (printImage) {
                Dx = (REEL_MAX - REEL_MIN) / picareax;
                Dy = (IMAG_MAX - IMAG_MIN) / picareay;
                currentAreaY = picareay;
                currentAreaX = picareax;
            } else {
                Dx = (REEL_MAX - REEL_MIN) / AREAX;                 //Her er byttet om
                Dy = (IMAG_MAX - IMAG_MIN) / AREAY;                 //Her er byttet om
                currentAreaY = AREAY;
                currentAreaX = AREAX;
            }
            //Dx = -0.00357142857142857142857142857143;
            //Dy = -0.00357142857142857142857142857143;
            p0 = REEL_MIN;
            q0 = IMAG_MIN;
            x = REEL_MIN;
            y = IMAG_MIN;
            p1 = 0;
            q1 = 0;
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
                        if (printImage) w.setColor(Color.black);
                        else g.setColor(Color.black);
                    } else {
                        colorPix();
                        if (printImage) w.setColor(farve);
                        else g.setColor(farve);
                    }
                    if (printImage) w.drawLine(j, i, j, i);
                    else g.drawLine(j, i, j, i);
                    x = x + Dx;
                }
                x = REEL_MIN;
                y = y + Dy;
            }
            if (toggleComp) {
                MaxColors.grabFocus();
                clrnum.grabFocus();
                clr.grabFocus();
                sav.grabFocus();
                Zoom.grabFocus();
                restart.grabFocus();
                retain.grabFocus();
            }
            if (printImage) print(image);
        }


        void colorPix() { //giver variablen "farve" en farve, der tildeles til en pixel, dette gøres for hvær pixel
            int c1;
            int c2 = 0;
            int c3;
            int r = 0;
            int g = 0;
            int b = 0;
            int mod = count % 255;
            String fg;
            String bg = "Yellow";

            ClrVal.clear();
            for (int i = 0; i <= (count / 255) - 1; i++) ClrVal.add(255);
            if (ClrVal.size() < cNum) ClrVal.add(count % 255);

            if (ClrVal.size() >= 2) {
                c1 = Integer.valueOf(String.valueOf(ClrVal.get(ClrVal.size() - 2)));
                fg = String.valueOf(Coloring.get(ClrVal.size() - 2));
                c2 = Integer.valueOf(String.valueOf(ClrVal.get(ClrVal.size() - 1)));
                bg = String.valueOf(Coloring.get(ClrVal.size() - 1));
            } else {
                c1 = Integer.valueOf(String.valueOf(ClrVal.get(ClrVal.size() - 1)));
                fg = String.valueOf(Coloring.get(ClrVal.size() - 1));
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
            farve = new Color(r, g, b);
        }

        void loadConfig() { //sætter alle variabler udfra config fil, hvis filen ikke er der, lav en
            String settings = file.readFile("settings");
            if (settings.equals("false")) SetConfig();
            cNum = Integer.valueOf(settings.substring(settings.indexOf("C?") + 3, settings.indexOf("#", settings.indexOf("C?"))));
            LOOP_LIMIT = 255 * cNum;
            Coloring.clear();
            for (int i = 0; i <= cNum - 1; i++) {
                Coloring.add(settings.substring(settings.indexOf("C" + String.valueOf(i + 1)) + 2 + String.valueOf(i).length(), settings.indexOf("#", settings.indexOf("C" + String.valueOf(i + 1)))));
            }
        }


        void SetConfig() { //kode der (finder ud ad hvornår user interfacet skal loades og) sætter en configfil op med standard-værdier
            String output;
            output = "C? 2#" + "\n" + "C1 Blue#" + "\n" + "C2 Yellow#";
            file.writeFile(output, "settings");
        }


        @Override
        public void mouseClicked(MouseEvent e) { //finder hvor du trykker og ændre på værdierne REEL og IMAG
            int MouseX = e.getX() - (zoomLvl / 2);
            int MouseY = e.getY() - (zoomLvl / 2);
            if (zoomLvl < 0) {
                MouseX = e.getX() - (zoomLvl / 2);
                MouseY = e.getY() - (zoomLvl / 2);
            }
            if (e.getX() > AREAX - 20 && e.getY() > AREAY - 20) {
                toggleComp = (!toggleComp);
                if (toggleComp) {
                    add(MaxColors);
                    add(clrnum);
                    add(clr);
                    add(sav);
                    add(Zoom);
                    add(restart);
                    add(retain);
                    System.out.println("Opening Ui...");
                } else {
                    remove(MaxColors);
                    remove(clrnum);
                    remove(clr);
                    remove(sav);
                    remove(Zoom);
                    remove(restart);
                    remove(retain);
                    System.out.println("Closing Ui");
                }
            } else if (!toggleComp && zoomLvl > 0) {
                REEL_MIN = REEL_MIN + MouseX * Dx;
                REEL_MAX = REEL_MIN + zoomLvl * Dx;

                IMAG_MIN = IMAG_MIN + MouseY * Dy;
                IMAG_MAX = IMAG_MIN + zoomLvl * Dy;
            } else if (!toggleComp && zoomLvl < 0) {
                REEL_MAX = REEL_MIN + (MouseX * 2) * Dx;
                REEL_MIN = REEL_MIN + (zoomLvl) * Dx;

                IMAG_MAX = IMAG_MIN + (MouseY * 2) * Dy;
                IMAG_MIN = IMAG_MIN + (zoomLvl) * Dy;
            }
            repaint();
        }

        void print(BufferedImage bi) {
            try {
                // retrieve image
                File outputfile = new File("saved.png");
                ImageIO.write(bi, "png", outputfile);
            } catch (IOException e) {
            }
            printImage = false;
            repaint();
            System.out.println("image saved successfully");
        }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                new render().setVisible(true);
            }
        });
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {

    }

}
/* TODO:
saves, loaded from the UI in a combobox, stored in multiple txtfiles. the amount of saves stored in a main file + names
UI:
add a setting to a save/reset from the UI<
Optimise UI<
Improove UI load time
remove colors

Animations? like a spinner thing while UI is loading
Creating a huge image from a zoomed in part, --specify the dimension in the UI >filename in UI
Metadata for the images?
make tray  flash when rendering is complete include a sound notification
add orange turquoise violet
*/