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
        private final ArrayList<String> Coloring = new ArrayList<>(); // lagre rekkefølgen af farver
        private final ArrayList<String> ColorIndex = new ArrayList<>();
        private final ArrayList<Integer> ClrVal = new ArrayList<>(); //indeholder værdien af de enkelte farver
        private int count;
        private int cNum;
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
        private Color farve;
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
            loadConfig();
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
                    if (clrnum.getSelectedIndex() >= 0 && clrnum.getSelectedIndex() < Coloring.size()) {
                        System.out.println(ColorIndex.get(clrnum.getSelectedIndex()));
                        clr.setText(ColorIndex.get(clrnum.getSelectedIndex()));
                    }
                }
            });

            clr.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent event) {
                    if (clr.getText().length() == 9) {
                        if (ColorIndex.size() <= clrnum.getSelectedIndex()) {
                            ColorIndex.add(clr.getText());
                        } else {
                            ColorIndex.set(clrnum.getSelectedIndex(), clr.getText());
                        }
                        cNum = ColorIndex.size();
                        LOOP_LIMIT = 255 * ColorIndex.size();
                    }
                }
            });
        }

        /*void colorPix() { //giver variablen "farve" en farve, der tildeles til en pixel, dette gøres for hvær pixel
            int c1;
            int c2 = 0;
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
                case "Orange":
                    r = c1;
                    g = c1 / 2;
                    break;
                case "Forest":
                    r = c1 / 2;
                    g = c1;
                    break;
                case "Turquoise":
                    g = c1;
                    b = c1 / 2;
                    break;
                case "Sea":
                    g = c1 / 2;
                    b = c1;
                    break;
                case "Violet":
                    b = c1;
                    r = c1 / 2;
                    break;
                case "Lavender":
                    b = c1 / 2;
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
                        if (g != 127) {
                            if (g < 255) g = c2 / 2;
                            if (g > 128) g = c1 - c2 / 2;
                        }
                        if (b != 0) b = c1 - c2;
                        break;
                    case "Sea":
                        if (r != 0) r = c1 - c2;
                        if (g != 127) {
                            if (g < 255) g = c2 / 2;
                            if (g > 128) g = c1 - c2 / 2;
                        }
                        if (b != 255) b = c2;
                        break;
                    case "Violet":
                        if (r != 127) {
                            if (r < 255) r = c2 / 2;
                            if (r > 128) r = c1 - c2 / 2;
                        }
                        if (g != 0) g = c1 - c2;
                        if (b != 255) b = c2;
                        break;
                    case "Lavender":
                        if (r != 255) r = c2;
                        if (g != 0) g = c1 - c2;
                        if (b != 127) {
                            if (b < 255) b = c2 / 2;
                            if (b > 128) b = c1 - c2 / 2;
                        }
                        break;
                    case "Forest":
                        if (r != 127) {
                            if (r < 255) r = c2 / 2;
                            if (r > 128) r = c1 - c2 / 2;
                        }
                        if (g != 255) g = c2;
                        if (b != 0) b = c1 - c2;
                        break;
                    case "Turquoise":
                        if (r != 0) r = c1 - c2;
                        if (g != 255) g = c2;
                        if (b != 127) {
                            if (b < 255) b = c2 / 2;
                            if (b > 128) b = c1 / 2;
                        }
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
        }*/

        Color AdvColorPix(){
            int c1;
            int c2;
            int r;
            int g;
            int b;
            double dr;
            double dg;
            double db;
            String fg; //foreground colour
            String bg; //background colour
            ClrVal.clear();
            for (int i = 0; i <= (count / 255) - 1; i++) ClrVal.add(255);
            if (ClrVal.size() < ColorIndex.size()) ClrVal.add(count % 255);

            if (ClrVal.size() >= 2) {
                c1 = Integer.valueOf(String.valueOf(ClrVal.get(ClrVal.size() - 2)));
                fg = String.valueOf(ColorIndex.get(ClrVal.size() - 2));
                c2 = Integer.valueOf(String.valueOf(ClrVal.get(ClrVal.size() - 1)));
                bg = String.valueOf(ColorIndex.get(ClrVal.size() - 1));

                dr = ((Integer.valueOf(bg.substring(0,3)))/256.0);
                dg = ((Integer.valueOf(bg.substring(3,6)))/256.0);
                db = ((Integer.valueOf(bg.substring(6,9)))/256.0);
                r =(int)((c2)* dr);
                g =(int)((c2)* dg);
                b =(int)((c2)* db);

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
            if (r > 255 || g > 255 || b>255){
                System.out.println(r + "," + g + "," + b);
                return Color.black;
            }else {
                return new Color(r, g, b);
            }
        }

        void loadConfig() { //sætter alle variabler udfra config fil, hvis filen ikke er der, lav en
            ColorIndex.add("180090000");
            ColorIndex.add("255255000");
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
            renderThread.params(Integer.valueOf(size.getText()), REEL_MAX, REEL_MIN, IMAG_MAX, IMAG_MIN, Coloring.toArray(new String[Coloring.size()]), FileName.getText());
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
                            count = 0;
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
                                g.setColor(AdvColorPix());
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

}
/*
UI:
Optimise UI<
Improve UI load time (multi threading helped a bit)
saves, loaded from the UI in a combobox, stored in multiple text files. the amount of saves stored in a main file + names
UI descriptions

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
