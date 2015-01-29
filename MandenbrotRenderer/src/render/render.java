package render;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

//@author alex

    public final class render extends JFrame implements MouseListener {
        private final int AREAX = 700; // TODO: aspect ratio
        private final int AREAY = 700;
        private final int progressRefreshrateCurrent = 50;
        private final int progressRefreshrateBackground = 200;
        private final inout file = new inout();
        private final ArrayList<String> ColorIndex = new ArrayList<>();
        private final ArrayList<Integer> ClrVal = new ArrayList<>(); //indeholder værdien af de enkelte farver
        private int zoomLvl = 400;
        private double REEL_MIN = -2.0; // min reelle værdi
        private double REEL_MAX = 0.5; // max reelle værdi
        private double IMAG_MIN = -1.25; // min imaginære værdi
        private double IMAG_MAX = 1.25; // max imaginære værdi
        private double LOOP_LIMIT = 509;//grænseværdien for while loop iterationer
        private double Dx = (REEL_MAX - REEL_MIN) / AREAX;                              //Her er byttet om
        private double Dy = (IMAG_MAX - IMAG_MIN) / AREAY;//delta-x og delta-y          //Her er byttet om
        private double percnt;
        private boolean toggleComp;
        private Timer timer;  // TODO: rename this
        private Timer timer2; // TODO: rename this
        private Imagerenderer renderThread;
        private JTextField MaxColors;
        private JTextField Zoom;
        private JTextField FileName;
        private JTextField size;
        private JComboBox<Integer> clrnum;
        private JTextField clr;
        private JButton sav;
        private JButton restart;
        private JCheckBox retain;
        private JProgressBar renderProgress;
        private JProgressBar rendered;
        private JLayeredPane pane = new JLayeredPane();
        private render.GraphicsPanel mandelbrot;

        public render() {
            while (!loadConfig()) ;
            initUI();
        }

        public static void main(String[] args) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new render().setVisible(true);
                }
            });
        }

        void initUI() {
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setSize(AREAX+8, AREAY+38);
            setTitle("Fractals!!");
            setLocationRelativeTo(null);
            setLayout(null);
            addMouseListener(this);

            pane = new JLayeredPane();
            pane.setBounds(0, 0, AREAX, AREAY);
            add(pane);

            rendered = new JProgressBar(0,100);
            rendered.setBounds(2, AREAY-20, 100, 20);
            rendered.setValue(0);
            rendered.setStringPainted(true);
            pane.add(rendered,1);

            renderProgress = new JProgressBar(0,100);
            renderProgress.setBounds(104, AREAY-20, 100, 20);
            renderProgress.setValue(0);
            renderProgress.setStringPainted(true);
            pane.add(renderProgress,0);

            mandelbrot = new render.GraphicsPanel();
            mandelbrot.setBounds(0, 0, AREAX, AREAY);
            pane.add(mandelbrot, 2);

            retain = new JCheckBox();
            retain.setBounds(180, 80, 20, 20);

            MaxColors = new JTextField();
            MaxColors.setBounds(50, 50, 50, 20);

            Zoom = new JTextField();
            Zoom.setBounds(50, 80, 50, 20);

            clrnum = new JComboBox<>();
            clrnum.addItem(1);
            clrnum.setBounds(210, 50, 100, 20);

            clr = new JTextField();
            clr.setBounds(330, 50, 100, 20);

            sav = new JButton("Img");
            sav.setBounds(110, 50, 60, 20);

            size = new JTextField("4");
            size.setBounds(180, 50, 20, 20);

            restart = new JButton("←");
            restart.setBounds(110, 80, 60, 20);

            FileName = new JTextField("Filename");
            FileName.setBounds(210, 80, 100, 20);

            Zoom.setText(String.valueOf(zoomLvl));
            MaxColors.setText(String.valueOf(ColorIndex.size()));
            clrnum.removeAllItems();
            for (int i = 0; i < ColorIndex.size(); i++) {
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
                    pane.remove(MaxColors);
                    pane.remove(clrnum);
                    pane.remove(clr);
                    pane.remove(sav);
                    pane.remove(Zoom);
                    pane.remove(restart);
                    pane.remove(retain);
                    pane.remove(FileName);
                    pane.remove(size);
                    System.out.println("Closing Ui");
                    mandelbrot.rerender();
                    repaint();
                }
            });
//TODO: Clean up mess from here on down! make comments to get an overview

            Zoom.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    zoomLvl = Integer.valueOf(Zoom.getText());
                }
            });

            sav.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    sendToPrint();
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
                    if (clrnum.getSelectedIndex() >= 0 && clrnum.getSelectedIndex() < ColorIndex.size()) {
                        System.out.println(ColorIndex.get(clrnum.getSelectedIndex()));
                        clr.setText(ColorIndex.get(clrnum.getSelectedIndex()));
                    }
                }
            });

            clr.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent event) {
                    Flasher flasher = new Flasher();
                    if (IsNumber(clr.getText())) {
                        if (clr.getText().length() == 9) {
                            if (Integer.valueOf(clr.getText().substring(0, 3)) <= 255 &&
                                    (Integer.valueOf(clr.getText().substring(3, 6)) <= 255) &&
                                    (Integer.valueOf(clr.getText().substring(6, 9)) <= 255) &&
                                    (Integer.valueOf(clr.getText().substring(0, 3)) >= 0) &&
                                    (Integer.valueOf(clr.getText().substring(3, 6)) >= 0) &&
                                    (Integer.valueOf(clr.getText().substring(6, 9)) >= 0)) {
                                clr.setOpaque(true);
                                flasher.Fade(Color.black, Color.green, clr, 4, true);
                                if (ColorIndex.size() <= clrnum.getSelectedIndex()) {
                                    ColorIndex.add(clr.getText());
                                } else {
                                    ColorIndex.set(clrnum.getSelectedIndex(), clr.getText());
                                }
                                LOOP_LIMIT = 255 * ColorIndex.size();
                            } else {
                                flasher.Fade(Color.black, Color.red, clr, 4, false);
                            }
                        }
                    } else flasher.Flash(Color.red, clr, 5, 100);
                }
            });
        }

        boolean IsNumber(String test) {
            try {
                Integer.valueOf(test);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        Color AdvColorPix(int count) {
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
            ClrVal.clear();
            for (int i = 0; i <= (count / 255) - 1; i++) ClrVal.add(255);
            if (ClrVal.size() < ColorIndex.size()) ClrVal.add(count % 255);

            if (ClrVal.size() >= 2) {
                fg = String.valueOf(ColorIndex.get(ClrVal.size() - 2));
                or = Integer.valueOf(fg.substring(0, 3));
                og = Integer.valueOf(fg.substring(3, 6));
                ob = Integer.valueOf(fg.substring(6, 9));
                c2 = Integer.valueOf(String.valueOf(ClrVal.get(ClrVal.size() - 1)));
                bg = String.valueOf(ColorIndex.get(ClrVal.size() - 1));

                dr = ((Integer.valueOf(bg.substring(0, 3)) - Integer.valueOf(fg.substring(0, 3))) / 256.0);
                dg = ((Integer.valueOf(bg.substring(3, 6)) - Integer.valueOf(fg.substring(3, 6))) / 256.0);
                db = ((Integer.valueOf(bg.substring(6, 9)) - Integer.valueOf(fg.substring(6, 9))) / 256.0);
                r = (int) ((or) + (c2 * dr));
                g = (int) ((og) + (c2 * dg));
                b = (int) ((ob) + (c2 * db));

            } else {
                c1 = Integer.valueOf(String.valueOf(ClrVal.get(ClrVal.size() - 1)));
                fg = String.valueOf(ColorIndex.get(ClrVal.size() - 1));

                dr = (Integer.valueOf(fg.substring(0,3))/256.0);
                dg = (Integer.valueOf(fg.substring(3,6))/256.0);
                db = (Integer.valueOf(fg.substring(6,9))/256.0);
                r =(int)(c1 * dr);
                g =(int)(c1 * dg);
                b =(int)(c1 * db);
            }
            if (r > 255 || g > 255 || b > 255 || r < 0 || g < 0 || b < 0) {
                System.out.println(r + "," + g + "," + b);
                return Color.black;
            }else {
                return new Color(r, g, b);
            }
        }

        boolean loadConfig() { //sætter alle variabler udfra config fil, hvis filen ikke er der, lav en
            //ColorIndex.add("180090000");
            //ColorIndex.add("255255000");
            String settings = file.readFile("settings");
            if (settings.equals("false")) {
                SetConfig();
                return false;
            }
            int cNum = Integer.valueOf(settings.substring(settings.indexOf("C?") + 3, settings.indexOf("#", settings.indexOf("C?"))));
            LOOP_LIMIT = 255 * cNum;
            ColorIndex.clear();
            for (int i = 0; i <= cNum - 1; i++) {
                ColorIndex.add(settings.substring(settings.indexOf("C" + String.valueOf(i + 1)) + 2 + String.valueOf(i).length(), settings.indexOf("#", settings.indexOf("C" + String.valueOf(i + 1)))));
            }
            return true;
        }

        void SetConfig() { //kode der (finder ud ad hvornår user interfacet skal loades og) sætter en configfil op med standard-værdier
            String output;
            output = "C? 2#" + "\n" + "C1 000000180#" + "\n" + "C2 255255050#";
            file.writeFile(output, "settings");
        }

// --Commented out by Inspection START (28-01-2015 23:28):
//        void setSave(){
//            String output;
//            output = "C? "+ String.valueOf(clrnum.getItemCount()) + "#" + "\n";
//            for (int i =1; i<clrnum.getItemCount(); i++) {
//                output = output + "C" + String.valueOf(i) + Coloring.get(i-1) + "#" + "\n";
//            }
//            file.writeFile(output, "savefile");
//        }
// --Commented out by Inspection STOP (28-01-2015 23:28)

        void sendToPrint(){
            renderThread = new Imagerenderer();
            System.out.println("image away");
            renderThread.params(Integer.valueOf(size.getText()), REEL_MAX, REEL_MIN, IMAG_MAX, IMAG_MIN, ColorIndex.toArray(new String[ColorIndex.size()]), FileName.getText());
            renderThread.start();
            render.RenderProgress pgbar = new render.RenderProgress();
            timer = new Timer(progressRefreshrateBackground, pgbar);
            timer.start();
            System.out.println("you should have regained control");
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
                    pane.add(MaxColors,0);
                    pane.add(clrnum,0);
                    pane.add(clr,0);
                    pane.add(sav,0);
                    pane.add(Zoom, 0);
                    pane.add(restart,0);
                    pane.add(retain, 0);
                    pane.add(FileName,0);
                    pane.add(size,0);
                    System.out.println("Opening Ui...");
                } else {
                    pane.remove(MaxColors);
                    pane.remove(clrnum);
                    pane.remove(clr);
                    pane.remove(sav);
                    pane.remove(Zoom);
                    pane.remove(restart);
                    pane.remove(retain);
                    pane.remove(FileName);
                    pane.remove(size);
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
            mandelbrot.rerender();
            repaint();
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

        public interface ImageConsumer {
            public void imageLoaded(BufferedImage img);
        }

        public class GraphicsPanel extends JPanel implements ImageConsumer {
            private BufferedImage img;

            GraphicsPanel() {
                new RenderWorker(this).execute();
            }

            public void imageLoaded(BufferedImage pic) {
                this.img = pic;
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (img != null) {
                    g.drawImage(img, 0, 0, this);
                    pane.repaint();
                }
            }

            void rerender() {
                CurrentProgress pgbar1 = new CurrentProgress();
                timer2 = new Timer(progressRefreshrateCurrent, pgbar1);
                timer2.start();
                new RenderWorker(this).execute();
            }

            class RenderWorker extends SwingWorker<BufferedImage, Integer> {
                private final ImageConsumer consumer;

                public RenderWorker(ImageConsumer consumer) {
                    this.consumer = consumer;
                }

                @Override
                protected BufferedImage doInBackground() throws Exception {
                    BufferedImage image = new BufferedImage(AREAX, AREAY, BufferedImage.TYPE_INT_RGB);
                    Graphics g = image.createGraphics();
                    double dpcnt; //Delta percent
                    dpcnt = 99.99 / AREAY;
                    percnt = 0;
                    Dx = (REEL_MAX - REEL_MIN) / AREAX;                 //Her er byttet om
                    Dy = (IMAG_MAX - IMAG_MIN) / AREAY;                 //Her er byttet om
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

                protected void done() {
                    try {
                        BufferedImage img = get();
                        consumer.imageLoaded(img);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private class RenderProgress implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                System.out.println(renderThread.prcnt);
                if (renderThread.prcnt == 100) {
                    renderProgress.setValue(100);
                    renderProgress.setIndeterminate(false);
                    renderProgress.setString(null);
                    timer.stop();
                } else if (renderThread.prcnt == -1) {
                    renderProgress.setIndeterminate(true);
                    renderProgress.setString("saving");
                } else {
                    renderProgress.setValue((int) renderThread.prcnt);
                }
            }
        }
        private class CurrentProgress implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                rendered.setValue((int) percnt);
                if ((int) percnt == 100) {
                    timer2.stop();
                }
            }
        }

        private class Flasher implements ActionListener {
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

            public void Flash(Color color, Component cmpt, int flashes, int speed) {
                action = "flash";
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
                } else if (action.equals("flash")) {
                    if (i % 2 == 0) {
                        cmp.setForeground(col1);
                    } else {
                        cmp.setForeground(null);
                    }
                    i--;
                    if (i == 0) foo.stop();
                }
            }
        }
}

/*
UI:
Optimise UI<
Improve UI load time (multi threading helped a bit)
saves, loaded from the UI in a combobox, stored in multiple text files. the amount of saves stored in a main file + names
UI descriptions
Add a "give me a random colour" button

Metadata for the images?
watermark? optional?
make tray  flash when rendering is complete, include a sound notification
add orange turquoise violet
revise colour engine delta in individual colours, this makes for custom colours
optimise (there is always optimisation)
multiple ways to optimise (rendering time vs. memory vs. space used.. etc)
remove/translate old comments

COMPLETED:
Creating a huge image from a zoomed in part, --specify the dimension in the UI >filename in UI
Animations? like a spinner thing while UI is loading COMPLETED: progress bar
add a setting to a save/reset from the UI< COMPLETED: put op two buttons for saving an image and resetting
remove colors from the UI COMPLETED: reset added
*/
