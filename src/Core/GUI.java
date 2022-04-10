package Core;

import guru.nidi.graphviz.engine.Engine;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class GUI {

    private static File file;
    private static JFrame mainFrame;
    private static LSA lsa;

    private static final Color ACTIVE = new Color(0, 122, 255);
    private static final Color PASS = new Color(52, 199, 89);
    private static final Color MOD = new Color(162, 132, 94);
    private static final Color WARN = new Color(255, 149, 0);
    private static final Color ERROR = new Color(255, 59, 48);

    private static final int FIELD_WIDTH = 150;
    private static final int KEY_WIDTH = 100;

    private static final int GRAPH_WIDTH = 400;
    private static final int GRAPH_HEIGHT = 100;
    private static BufferedImage blank;

    // JPanel that displays a BufferedImage
    // ref: https://gist.github.com/javagl/4dc0382be7bcb7eaefe7bc65de70e70e
    private static class ImagePanel extends JPanel {
        private BufferedImage image;

        void setImage(BufferedImage image) {
            this.image = image;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, null);
            }
        }

    }

    private static final JPanel pg_open = new JPanel(new BorderLayout());
    private static final JPanel pg_exe = new JPanel(new BorderLayout());
    private static final JPanel cards = new JPanel(new CardLayout());
    private static final CardLayout cl = (CardLayout) (cards.getLayout());

    private static final JButton btn_open = new JButton("Open File");
    private static final JTextArea ta_file = new JTextArea(
            """
            Preview of the file will be shown here.
            You can edit here directly even after opening a file.
            """
    );
    private static final JButton btn_next = new JButton("Proceed to Compute");
    private static final JButton btn_back = new JButton("Return to Preview");

    // dynamic network topology
    private static final JButton btn_addNode = new JButton("⊕ Node");
    private static final JTextField tf_addNode = new JTextField("X: A:1");

    private static final JButton btn_rmEdge = new JButton("⊖ Edge");
    private static final JComboBox<String> cb_rmEdge_src = new JComboBox<>();
    private static final JLabel lb_rmEdge = new JLabel("TO");
    private static final JComboBox<String> cb_rmEdge_dest = new JComboBox<>();

    private static final JButton btn_rmNode = new JButton("⊖ Node");
    private static final JComboBox<String> cb_rmNode = new JComboBox<>();

    // LSR computation
    private static final JLabel lb_select = new JLabel("Select Source: ");
    private static final JComboBox<String> cb_sel_src = new JComboBox<>();
    private static final JButton btn_reload = new JButton("Reload");
    private static final JButton btn_step = new JButton("Single Step");
    private static final JButton btn_computeAll = new JButton("Compute All");

    private static final JTextArea ta_step = new JTextArea();
    private static final JScrollPane scr_step = new JScrollPane(
            ta_step,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    );
    private static final ImagePanel img_pn = new ImagePanel();

    private static void stylingComponents() {
        // appearance setting
        // don't merge the two setBorder unless ejecting lambda functions
        ta_file.setBorder(BorderFactory.createCompoundBorder(
                ta_file.getBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        ta_step.setBorder(BorderFactory.createCompoundBorder(
                ta_step.getBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        btn_next.setForeground(WARN);
        btn_open.setForeground(ACTIVE);
        btn_addNode.setMinimumSize(new Dimension(KEY_WIDTH, 0));
        btn_rmNode.setMinimumSize(new Dimension(KEY_WIDTH, 0));
        btn_rmEdge.setMinimumSize(new Dimension(KEY_WIDTH, 0));
        btn_reload.setMinimumSize(new Dimension(KEY_WIDTH + FIELD_WIDTH, 0));
        btn_step.setMinimumSize(new Dimension(KEY_WIDTH + FIELD_WIDTH, 0));
        btn_computeAll.setMinimumSize(new Dimension(KEY_WIDTH + FIELD_WIDTH, 0));
        btn_back.setForeground(WARN);
        btn_addNode.setForeground(MOD);
        btn_rmNode.setForeground(MOD);
        btn_rmEdge.setForeground(MOD);
        btn_reload.setForeground(ACTIVE);
        btn_step.setForeground(ACTIVE);
        btn_computeAll.setForeground(ACTIVE);

        cb_sel_src.setMaximumSize(new Dimension(FIELD_WIDTH, 20));
        cb_rmNode.setMaximumSize(new Dimension(FIELD_WIDTH, 20));
        cb_rmEdge_src.setMaximumSize(new Dimension(FIELD_WIDTH / 3, 20));
        cb_rmEdge_dest.setMaximumSize(new Dimension(FIELD_WIDTH / 3, 20));

        tf_addNode.setMaximumSize(new Dimension(FIELD_WIDTH, 20));
        img_pn.setBackground(Color.white);

        scr_step.setBorder(BorderFactory.createEmptyBorder());
        scr_step.setPreferredSize(new Dimension(GRAPH_WIDTH, GRAPH_HEIGHT));
    }

    private static void bindButtonActions() {
        btn_open.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().matches(".*\\.lsa") || f.isDirectory();
                }
                @Override
                public String getDescription() {
                    return "LSA Files, .lsa";
                }
            });
            int option = fileChooser.showOpenDialog(mainFrame);
            if (option == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                btn_open.setText(file.getName());

                try {
                    // read file and show preview
                    lsa.loadFromFile(file);
                    ta_file.setText("");
                    for (String line : lsa.text)
                        ta_file.append(line + "\n");

                    // parse file
                    lsa.parse();
                    blink(btn_open, PASS, ACTIVE);
                } catch (IOException ex) {
                    blink(btn_open, ERROR, ACTIVE);
                    createPopUpWindow("Read/Write Error.");
                } catch (IllegalArgumentException ex) {
                    blink(btn_open, ERROR, ACTIVE);
                    createPopUpWindow(ex.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        btn_next.addActionListener(e -> {
            boolean parse_success = false;
            String errMsg = "Unknown Error.";
            try {
                if (file != null) {
                    FileWriter fw = new FileWriter(file);
                    ta_file.write(fw);
                    fw.close();
                }

                lsa.loadFromStr(ta_file.getText());
                lsa.parse();

                // load source selector and set first source as default
                reloadSteps();
                parse_success = true;
                cl.next(cards);
            } catch (IOException ex) {
                errMsg = "Read/Write Error: internal error.";
            } catch (IllegalArgumentException ex) {
                errMsg = ex.getMessage();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (!parse_success)
                createPopUpWindow(errMsg);

            mainFrame.pack();
        });
        btn_back.addActionListener(e -> {
            cl.next(cards);
            setBlankImage();
            mainFrame.pack();
        });
        btn_addNode.addActionListener(e -> {
            try {
                lsa.parseLine(tf_addNode.getText());
                blink(btn_addNode, PASS, MOD);
                reloadSteps();
                setBlankImage();
            } catch (Exception ex) {
                blink(btn_rmEdge, ERROR, MOD);
                createPopUpWindow(ex.getMessage());
            }
        });
        btn_rmNode.addActionListener(e -> {
            lsa.RemoveNode(getSelectedStr(cb_rmNode));
            blink(btn_rmNode, PASS, MOD);
            reloadSteps();
            setBlankImage();
        });
        btn_rmEdge.addActionListener(e -> {
            try {
                lsa.RemoveEdge(
                        getSelectedStr(cb_rmEdge_src),
                        getSelectedStr(cb_rmEdge_dest)
                );
                blink(btn_rmEdge, PASS, MOD);
                reloadSteps();
                setBlankImage();
            } catch (Exception ex) {
                blink(btn_rmEdge, ERROR, MOD);
                createPopUpWindow(ex.getMessage());
            }
        });
        cb_sel_src.addActionListener(e -> {
            lsa.setSource(getSelectedStr(cb_sel_src));
            ta_step.setText("");
        });
        btn_reload.addActionListener(e -> {
            reloadSteps();
            setBlankImage();
        });
        btn_step.addActionListener(e -> {
            String ret = lsa.SingleStep();
            if (!ret.isEmpty()) {
                ta_step.append("SingleStep Finds Router: " + ret + "\n");
                ta_step.append(lsa.toString() + "\n\n");
                resetImage(ret);
            } else {
                ta_step.append("All routers exhausted.\n");
            }
            mainFrame.pack();
        });
        btn_computeAll.addActionListener(e -> {
            lsa.Run();
            lsa.draw(null);
            ta_step.setText(lsa.toString() + "\n\n");
            resetImage(null);
            mainFrame.pack();
        });
    }

    private static void initMainFrame() {
        // create and set card layouts
        // can shuffle between card1 and card2
        cards.add("open", pg_open);
        cards.add("exe", pg_exe);

        stylingComponents();
        setBlankImage();
        bindButtonActions();

        JPanel exe_func_g = new JPanel();
        GroupLayout layout = new GroupLayout(exe_func_g);
        exe_func_g.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGap(5, 10, 10)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                .addComponent(btn_addNode)
                                                .addComponent(btn_rmNode)
                                                .addComponent(btn_rmEdge)
                                                .addComponent(lb_select))
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(tf_addNode)
                                                .addComponent(cb_rmNode)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(cb_rmEdge_src)
                                                        .addComponent(lb_rmEdge)
                                                        .addComponent(cb_rmEdge_dest)
                                                )
                                                .addComponent(cb_sel_src)))
                                .addComponent(btn_reload)
                                .addComponent(btn_step)
                                .addComponent(btn_computeAll))
                        .addGap(5, 10, 10)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(img_pn)
                            .addComponent(scr_step)
                        )
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(5, 10, 10)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(tf_addNode)
                                        .addComponent(btn_addNode))
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(btn_rmNode)
                                        .addComponent(cb_rmNode))
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(btn_rmEdge)
                                        .addComponent(cb_rmEdge_src)
                                        .addComponent(cb_rmEdge_dest)
                                        .addComponent(lb_rmEdge))
                                .addGap(10, 20, 20)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lb_select)
                                        .addComponent(cb_sel_src))
                                .addComponent(btn_reload)
                                .addComponent(btn_step)
                                .addComponent(btn_computeAll)
                                .addGap(5,10,10)
                        )
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(img_pn)
                                .addComponent(scr_step)
                        )
        );

        JPanel exe_s_g = new JPanel(); exe_s_g.add(btn_back);
        JPanel file_s_g = new JPanel(); file_s_g.add(btn_open); file_s_g.add(btn_next);

        pg_open.add(file_s_g, BorderLayout.SOUTH);
        pg_open.add(ta_file, BorderLayout.CENTER);
        pg_exe.add(exe_s_g, BorderLayout.SOUTH);
        pg_exe.add(exe_func_g, BorderLayout.CENTER);

        cl.show(cards, "open");

        mainFrame.getContentPane().add(cards);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private static void blink(JComponent c, Color blink, Color constant) {
        c.setForeground(blink);
        setTimeout(() -> {c.setForeground(constant);}, 2000);
    }

    public static void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    private static void resetImage(String dest) {
        BufferedImage image = Graphviz.fromGraph(lsa.draw(dest))
                .width(GRAPH_WIDTH)
                .engine(Engine.NEATO)
                .render(Format.SVG)
                .toImage();
        img_pn.setImage(image);
        img_pn.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
    }

    private static void setBlankImage() {
        if (blank == null) {
            blank = new BufferedImage(GRAPH_WIDTH, GRAPH_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = blank.createGraphics();
            g2d.setColor(Color.white);
            g2d.fillRect(0, 0, blank.getWidth(), blank.getHeight());
        }
        img_pn.setImage(blank);
    }

    private static String getSelectedStr(JComboBox cb) {
        return String.valueOf(cb.getSelectedItem());
    }

    /**
     * Erase all information about calculated steps and reload
     */
    private static void reloadSteps() {
        lsa.Reset();
        ta_step.setText("");
        reloadNodeSelector(cb_sel_src);
        reloadNodeSelector(cb_rmNode);
        reloadNodeSelector(cb_rmEdge_src);
        reloadNodeSelector(cb_rmEdge_dest);
        lsa.setSource(getSelectedStr(cb_sel_src));
    }

    private static void reloadNodeSelector(JComboBox box) {
        DefaultComboBoxModel model = new DefaultComboBoxModel(new Vector(lsa.Nodes.keySet()));
        box.setModel(model);
    }

    private static void createPopUpWindow(String msg) {
        JOptionPane.showMessageDialog(new JFrame(), msg, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private static JFrame createWindow(String title, int width, int height) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(width, height);
        if (width <= 500 && height <= 400)
            frame.setMinimumSize(new Dimension(width, height));
        else
            frame.setMinimumSize(new Dimension(500, 400));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        return frame;
    }

    public static void main(String[] args) {
        lsa = new LSA();
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {}
            mainFrame = createWindow("LSA Demo", 800, 600);
            initMainFrame();
        });
    }
}