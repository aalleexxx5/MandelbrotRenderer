package render;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

//@author alex

    public final class render extends JFrame implements MouseListener {
        private final int AREAX = 700; // TODO: aspect ratio
        private final int AREAY = 700;
        private final int progressRefreshrateCurrent = 50;
        private final int progressRefreshrateBackground = 200;
        private final inout file = new inout();
        private final ArrayList<String> ColorIndex = new ArrayList<>();//index for collour values
        private final ArrayList<Integer> ClrVal = new ArrayList<>(); //used to store a numbers to create colours (if in doubt alt + f7)
        private int zoomLvl = 400;
        private double REEL_MIN = -2.0; // min real value
        private double REEL_MAX = 0.5; // max real value
        private double IMAG_MIN = -1.25; // min imaginarry value
        private double IMAG_MAX = 1.25; // max imaginarry value
        private double LOOP_LIMIT = 509;//limit for loop iterations
        private double Dx = (REEL_MAX - REEL_MIN) / AREAX;
        private double Dy = (IMAG_MAX - IMAG_MIN) / AREAY;//delta-x and delta-y
        private double percnt;
        private boolean toggleComp;
        private boolean isHelp;
        private Timer timer;  // TODO: rename this
        private Timer timer2; // TODO: rename this
        private Imagerenderer renderThread;
        private JSpinner MaxColors;
        private JTextField Zoom;
        private JTextField FileName;
        private JTextField size;
        private JComboBox<Integer> clrnum;
        private JTextField clr;
        private JButton sav;
        private JButton restart;
        private JButton random;
        private JToggleButton help;
        private JCheckBox retain;
        private JProgressBar renderProgress;
        private JProgressBar rendered;
        private JLayeredPane pane = new JLayeredPane();
        private JTextArea desc;
        private render.GraphicsPanel mandelbrot;
        private JLabel pageTurnContainer;

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
            setSize(AREAX + 8, AREAY + 38);
            setTitle("Fractals!!");
            setLocationRelativeTo(null);
            setLayout(null);
            addMouseListener(this);

            pane = new JLayeredPane();
            pane.setBounds(0, 0, AREAX, AREAY);
            add(pane);

            rendered = new JProgressBar(0,100);
            rendered.setBounds(2, AREAY - 20, 100, 20);
            rendered.setValue(100);
            rendered.setStringPainted(true);
            pane.add(rendered, 1);

            renderProgress = new JProgressBar(0,100);
            renderProgress.setBounds(104, AREAY - 20, 100, 20);
            renderProgress.setValue(0);
            renderProgress.setStringPainted(true);
            pane.add(renderProgress, 0);

            mandelbrot = new render.GraphicsPanel();
            mandelbrot.setBounds(0, 0, AREAX, AREAY);
            pane.add(mandelbrot, 2);

            Zoom = new JTextField();
            Zoom.setBounds(20, 50, 35, 20);

            MaxColors = new JSpinner(new SpinnerNumberModel(2, null, null, 1));
            MaxColors.setBounds(20, 20, 40, 20);

            clrnum = new JComboBox<>();
            clrnum.addItem(1);
            clrnum.setBounds(62, 20, 60, 20);

            clr = new JTextField();
            clr.setBounds(127, 20, 100, 20);
            clr.setOpaque(true);

            random = new JButton("RND");
            random.setBounds(230, 20, 60, 20);

            sav = new JButton("Img");
            sav.setBounds(57, 80, 60, 20);

            size = new JTextField("4");
            size.setBounds(20, 80, 35, 20);

            FileName = new JTextField("Filename");
            FileName.setBounds(20, 102, 100, 20);

            restart = new JButton("←");
            restart.setBounds(62, 50, 60, 20);

            retain = new JCheckBox();
            retain.setBounds(124, 50, 20, 20);

            help = new JToggleButton("?");
            help.setMargin(new Insets(0, 0, 0, 0));
            help.setBounds(124, 80, 20, 20);

            desc = new JTextArea();
            desc.setBounds(149, 50, 227, 72);
            desc.setLineWrap(true);
            desc.setWrapStyleWord(true);

            java.net.URL imgURL = getClass().getResource("blue-corner-symbol-small.png");
            ImageIcon turnIcon = new ImageIcon(imgURL);
            pageTurnContainer = new JLabel(turnIcon);
            pageTurnContainer.setBounds(AREAX - 40, AREAY - 32, 32, 32);
            pane.add(pageTurnContainer, 0);

            Zoom.setText(String.valueOf(zoomLvl));
            MaxColors.setValue(ColorIndex.size());
            clrnum.removeAllItems();
            for (int i = 0; i < ColorIndex.size(); i++) {
                clrnum.addItem(i);
            }

            restart.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    REEL_MIN = -2.0; // min real value
                    REEL_MAX = 0.5; // max real value
                    IMAG_MIN = -1.25; // min imaginarry value
                    IMAG_MAX = 1.25; // max imaginarry value
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
                    pane.remove(random);
                    pane.remove(help);
                    pane.remove(desc);
                    mandelbrot.rerender();
                    repaint();
                }
            });
//TODO: Clean up mess from here on down! make comments to get an overview

            help.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (help.isSelected()) {
                        pane.add(desc, 0);
                        if (!isHelp) {
                            isHelp = true;
                            helpDisplay display = new helpDisplay();
                            display.startDisplaying();
                        }
                    } else {
                        pane.remove(desc);
                        isHelp = false;
                    }
                }
            });

            Zoom.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent event) {
                    Flasher flasher = new Flasher();
                    if (IsNumber(Zoom.getText())) {
                        if (Integer.valueOf(Zoom.getText()) != zoomLvl) {
                            zoomLvl = Integer.valueOf(Zoom.getText());
                            //Zoom.setBackground(Color.green);
                            //flasher.FadeBackground(Color.green, Color.white, Zoom, 5,false);
                            flasher.FlashBackground(Color.green, Zoom, 1, 100);
                        }
                    } else if (!Zoom.getText().equals("")) {
                        flasher.FadeBackground(Color.white, Color.red, Zoom, 4, false);
                    }
                }
            });

            sav.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    sendToPrint();
                }
            });

            random.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    DecimalFormat format = new DecimalFormat("000");
                    clr.setText(format.format(Math.random() * 255) + format.format(Math.random() * 255) + format.format(Math.random() * 255));
                }
            });

            MaxColors.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (Integer.valueOf(MaxColors.getValue().toString()) > 0) {
                        clrnum.removeAllItems();
                        System.out.println("removed all items");
                        for (int i = 0; i < Integer.valueOf(MaxColors.getValue().toString()); i++) {
                            clrnum.addItem(i+1);
                        }
                        if (ColorIndex.size()>Integer.valueOf(MaxColors.getValue().toString())){
                            ColorIndex.remove(ColorIndex.size()-1);
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
                                flasher.FadeBackground(Color.white, Color.green, clr, 4, true);
                                if (ColorIndex.size() <= clrnum.getSelectedIndex()) {
                                    ColorIndex.add(clr.getText());
                                } else {
                                    ColorIndex.set(clrnum.getSelectedIndex(), clr.getText());
                                }
                                LOOP_LIMIT = 255 * ColorIndex.size();
                            } else {
                                flasher.FadeBackground(Color.white, Color.red, clr, 4, true);
                            }
                        }
                    } else if (clr.getText().length() > 0){
                        flasher.FlashBackground(Color.red, clr, 5, 100);
                    }
                }
            });
        }

        boolean IsNumber(String test) { //utility to check for number
            try {
                Integer.valueOf(test);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        Color AdvColorPix(int count) { // all new and improved colour engine
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

        boolean loadConfig() { //load variables from a configfile, a config is not presant, create one and return false
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

        void SetConfig() { //create a stndard config-file
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
        public void mouseClicked(MouseEvent e) { //locates where a mouse click happened and sets values accordingly
            int MouseX = e.getX() - (zoomLvl / 2);
            int MouseY = e.getY() - (zoomLvl / 2);
            if (zoomLvl < 0) {
                MouseX = e.getX() - (zoomLvl / 2);
                MouseY = e.getY() - (zoomLvl / 2);
            }
            if (rendered.getValue()>99) {
                if (e.getX() > AREAX - 20 && e.getY() > AREAY - 20) {
                    toggleComp = (!toggleComp);
                    if (toggleComp) {
                        pane.add(MaxColors, 0);
                        pane.add(clrnum, 0);
                        pane.add(clr, 0);
                        pane.add(sav, 0);
                        pane.add(Zoom, 0);
                        pane.add(restart, 0);
                        pane.add(retain, 0);
                        pane.add(FileName, 0);
                        pane.add(size, 0);
                        pane.add(random, 0);
                        pane.add(help, 0);
                        if (help.isSelected()) pane.add(desc, 0);
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
                        pane.remove(random);
                        pane.remove(help);
                        pane.remove(desc);
                    }
                    repaint();
                } else if (!toggleComp && zoomLvl > 0) {
                    REEL_MIN = REEL_MIN + MouseX * Dx;
                    REEL_MAX = REEL_MIN + zoomLvl * Dx;

                    IMAG_MIN = IMAG_MIN + MouseY * Dy;
                    IMAG_MAX = IMAG_MIN + zoomLvl * Dy;
                    mandelbrot.rerender();
                } else if (!toggleComp && zoomLvl < 0) {
                    REEL_MAX = REEL_MIN + (MouseX * 2) * Dx;
                    REEL_MIN = REEL_MIN + (zoomLvl) * Dx;

                    IMAG_MAX = IMAG_MIN + (MouseY * 2) * Dy;
                    IMAG_MIN = IMAG_MIN + (zoomLvl) * Dy;
                    mandelbrot.rerender();
                } else mandelbrot.rerender();
            }
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
            void imageLoaded(BufferedImage img);
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
                    long starttime = System.nanoTime();
                    BufferedImage image = new BufferedImage(AREAX, AREAY, BufferedImage.TYPE_INT_RGB);
                    Graphics g = image.createGraphics();
                    double dpcnt; //Delta percent
                    dpcnt = 99.99 / AREAY;
                    percnt = 0;
                    Dx = (REEL_MAX - REEL_MIN) / AREAX;
                    Dy = (IMAG_MAX - IMAG_MIN) / AREAY;
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
                    System.out.println("Time to render: " + ((System.nanoTime() - starttime) / 1000) + " μs");
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
                    timer.stop();
                    renderProgress.setValue(100);
                    renderProgress.setIndeterminate(false);
                    renderProgress.setString(null);
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

        class helpDisplay implements ActionListener {
            Timer ding;
            String foo = "The corner of the screen toggles this UI in a 20x20 area (as large as the '?' button)";

            @Override
            public void actionPerformed(ActionEvent e) {
                if (MaxColors.getMousePosition() != null)
                    foo = "Amount of colours to be rendered, more colours takes longer and if you add too many, it looks confusing. \n -I didn't set a limit though";
                else if (clrnum.getMousePosition() != null)
                    foo = "In this box you select which colour to give which value";
                else if (clr.getMousePosition() != null)
                    foo = "This is where you specify the colour value to be rendered. In dec, eg. '000000000' is black and 255255255 is white. The box flashes green when the value is set";
                else if (sav.getMousePosition() != null)
                    foo = "Press to save what you see as an image (no, the buttons are not seen in the image silly)";
                else if (Zoom.getMousePosition() != null)
                    foo = "Here goes the amount of pixels you want to zoom in on, If the value is low you zoom a lot, if the value exceeds the size of the area: " + AREAY + "x" + AREAY + " you zoom out";
                else if (restart.getMousePosition() != null)
                    foo = "Pressing this will reset the application, unless the checkbox is marked everything returns to how it was when you started the application";
                else if (retain.getMousePosition() != null)
                    foo = "If this is checked, the colours will be retained when resetting the application (the button with the arrow))";
                else if (FileName.getMousePosition() != null)
                    foo = "Here is where the filename goes, If the name is in use, the file gets overwritten without any prompting";
                else if (size.getMousePosition() != null)
                    foo = "Size of the image you want to render in thousands of pixels, setting this to '1' gives an image of 1024x1024";
                else if (random.getMousePosition() != null)
                    foo = "Press this to add a random colour value, y'know because randomness is awesome!";
                else if (help.getMousePosition() != null) foo = "this is the button you press to hide this help box.";
                desc.setText(foo);
                if (!isHelp) ding.stop();
            }

            void startDisplaying() {
                ding = new Timer(200, this);
                ding.start();
            }
        }
}

/*
UI:
saves, loaded from the UI in a combobox, stored in multiple text files. the amount of saves stored in a main file + names

When render thread is completed, cancel percent calculation
Metadata for the images?
watermark? optional?
make tray  flash when rendering is complete, include a sound notification
optimise (there is always optimisation)
multiple ways to optimise (rendering time vs. memory vs. space used.. etc)
render queue
Guided tour of UI and Mandelbrot (use double buffering and a lot of randomness){
render mandelbrot, Choice: render or change colour layout
render: find a place in the image where there is a high colour value
Colour layout: cycle through the colours and assign a random colours
}

COMPLETED:
remove/translate old comments
Optimise UI<
Improve UI load time (multi threading helped a bit)
UI descriptions
Add a "give me a random colour" button
add orange turquoise violet
Creating a huge image from a zoomed in part, --specify the dimension in the UI >filename in UI
Animations? like a spinner thing while UI is loading COMPLETED: progress bar
add a setting to a save/reset from the UI< COMPLETED: put op two buttons for saving an image and resetting
remove colors from the UI COMPLETED: reset added
revise colour engine delta in individual colours, this makes for custom colours COMPLETED!
*/
