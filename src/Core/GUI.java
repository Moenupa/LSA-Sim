package Core;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

public class GUI {

    private static File file;
    private static JFrame mainFrame;
    private static LSA lsa;

    private static final Color ACTIVE = new Color(0, 122, 255);
    private static final Color PASS = new Color(52, 199, 89);
    private static final Color MOD = new Color(162, 132, 94);
    private static final Color WARN = new Color(255, 149, 0);
    private static final Color ERROR = new Color(255, 59, 48);

    private static final int FIELD_LEN = 150;
    private static final int KEY_LEN = 100;

    private static void createContentPane() {
        // create and set card layouts
        // can shuffle between card1 and card2
        JPanel open = new JPanel(new BorderLayout());
        JPanel exe = new JPanel(new BorderLayout());
        JPanel cards = new JPanel(new CardLayout());
        cards.add("open", open);
        cards.add("exe", exe);
        CardLayout cl = (CardLayout) (cards.getLayout());

        // 3 elements for open card:
        // openFile btn, cardSwitch btn, preview textarea
        JButton openFile = new JButton("Open File");
        JButton proceed = new JButton("Proceed");
        JTextArea preview = new JTextArea("Preview of the file will be shown here.\n"
                + "You can edit here directly even after opening a file.\n"
        );

        JComboBox<String> cb_source = new JComboBox<>();
        JComboBox<String> cb_rmEdge_src = new JComboBox<>();
        JComboBox<String> cb_rmEdge_dest = new JComboBox<>();
        JComboBox<String> cb_rmNode = new JComboBox<>();
        JLabel lb_select = new JLabel("Select Source: ");
        JTextField tf_addNode = new JTextField("X: A:0");
        JLabel lb_rmEdge = new JLabel("TO");
        JButton btn_step = new JButton("Single Step");
        JButton btn_computeAll = new JButton("Compute All");
        JButton btn_back = new JButton("Return To File Preview");
        JTextArea stepPreview = new JTextArea();
        JButton btn_addNode = new JButton("⊕ Node");
        JButton btn_rmNode = new JButton("⊖ Node");
        JButton btn_rmEdge = new JButton("⊖ Edge");

        // appearance setting
        // don't merge the two setBorder unless ejecting lambda functions
        preview.setBorder(BorderFactory.createCompoundBorder(
                preview.getBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        stepPreview.setBorder(BorderFactory.createCompoundBorder(
                stepPreview.getBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        proceed.setForeground(WARN);
        openFile.setForeground(ACTIVE);
        cb_source.setMaximumSize(new Dimension(FIELD_LEN, 20));
        cb_rmNode.setMaximumSize(new Dimension(FIELD_LEN, 20));
        cb_rmEdge_src.setMaximumSize(new Dimension(FIELD_LEN / 3, 20));
        cb_rmEdge_dest.setMaximumSize(new Dimension(FIELD_LEN / 3, 20));
        btn_addNode.setMinimumSize(new Dimension(KEY_LEN, 0));
        btn_rmNode.setMinimumSize(new Dimension(KEY_LEN, 0));
        btn_rmEdge.setMinimumSize(new Dimension(KEY_LEN, 0));
        btn_step.setMinimumSize(new Dimension(KEY_LEN + FIELD_LEN, 0));
        btn_computeAll.setMinimumSize(new Dimension(KEY_LEN + FIELD_LEN, 0));
        btn_back.setForeground(WARN);
        btn_addNode.setForeground(MOD);
        btn_rmNode.setForeground(MOD);
        btn_rmEdge.setForeground(MOD);
        btn_step.setForeground(ACTIVE);
        btn_computeAll.setForeground(ACTIVE);
        tf_addNode.setMaximumSize(new Dimension(FIELD_LEN, 20));

        // bind with actions
        openFile.addActionListener(e -> {
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
            int option;
            boolean loadFailed = true;
            option = fileChooser.showOpenDialog(mainFrame);
            if (option == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                openFile.setText(file.getName());

                try {
                    // read file and show preview
                    lsa.loadFromFile(file);
                    preview.setText("");
                    for (String line : lsa.text)
                        preview.append(line + "\n");

                    // parse file
                    lsa.parse();
                    blink(openFile, PASS, ACTIVE);
                } catch (IOException ex) {
                    blink(openFile, ERROR, ACTIVE);
                    createPopUpWindow("Read/Write Error.");
                } catch (IllegalArgumentException ex) {
                    blink(openFile, ERROR, ACTIVE);
                    createPopUpWindow(ex.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        proceed.addActionListener(e -> {
            boolean parse_success = false;
            String errMsg = "Unknown Error.";
            try {
                if (file != null) {
                    FileWriter fw = new FileWriter(file);
                    preview.write(fw);
                    fw.close();
                }

                lsa.loadFromStr(preview.getText());
                lsa.parse();

                // load source selector and set first source as default
                reloadSteps(stepPreview, cb_source, cb_rmNode, cb_rmEdge_src, cb_rmEdge_dest);
                parse_success = true;
            } catch (IOException ex) {
                openFile.setForeground(ERROR);
                errMsg = "Read/Write Error: internal error.";
            } catch (IllegalArgumentException ex) {
                openFile.setForeground(ERROR);
                errMsg = ex.getMessage();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (parse_success)
                cl.next(cards);
            else
                createPopUpWindow(errMsg);
        });
        btn_back.addActionListener(e -> {
            cl.next(cards);
        });
        btn_addNode.addActionListener(e -> {
            try {
                lsa.parseLine(tf_addNode.getText());
                blink(btn_addNode, PASS, MOD);
                reloadSteps(stepPreview, cb_source, cb_rmNode, cb_rmEdge_src, cb_rmEdge_dest);
            } catch (Exception ex) {
                createPopUpWindow(ex.getMessage());
            }
        });
        btn_rmNode.addActionListener(e -> {
            lsa.RemoveNode(getSelectedStr(cb_rmNode));
            blink(btn_rmNode, PASS, MOD);
            reloadSteps(stepPreview, cb_source, cb_rmNode, cb_rmEdge_src, cb_rmEdge_dest);
        });
        btn_rmEdge.addActionListener(e -> {
            try {
                lsa.RemoveEdge(
                        getSelectedStr(cb_rmEdge_src),
                        getSelectedStr(cb_rmEdge_dest)
                );
                blink(btn_rmEdge, PASS, MOD);
                reloadSteps(stepPreview, cb_source, cb_rmNode, cb_rmEdge_src, cb_rmEdge_dest);
            } catch (Exception ex) {
                createPopUpWindow(ex.getMessage());
            }
        });
        cb_source.addActionListener(e -> {
            lsa.setSource(getSelectedStr(cb_source));
            stepPreview.setText("");
        });
        btn_step.addActionListener(e -> {
            String ret = lsa.SingleStep();
            stepPreview.append("SingleStep Ret: " + ret + "\n");
            if (!ret.isEmpty())
                stepPreview.append(lsa.toString() + "\n\n");
        });
        btn_computeAll.addActionListener(e -> {
            lsa.Run();
            stepPreview.setText(lsa.toString() + "\n\n");
        });

        // exe south group
        JPanel exe_s_g = new JPanel();
        exe_s_g.add(btn_back);

        JPanel file_s_g = new JPanel();
        file_s_g.add(openFile); file_s_g.add(proceed);

        JScrollPane stepScroll = new JScrollPane(
                stepPreview,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        stepScroll.setBorder(BorderFactory.createEmptyBorder());

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
                                                .addComponent(cb_source)))
                                .addComponent(btn_step)
                                .addComponent(btn_computeAll))
                        .addGap(5, 10, 10)
                        .addComponent(stepScroll)
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
                                        .addComponent(cb_source))
                                .addComponent(btn_step)
                                .addComponent(btn_computeAll)
                                .addGap(5,10,10)
                        )
                        .addComponent(stepScroll)
        );

        open.add(file_s_g, BorderLayout.SOUTH);
        open.add(preview, BorderLayout.CENTER);
        exe.add(exe_s_g, BorderLayout.SOUTH);
        exe.add(exe_func_g, BorderLayout.CENTER);

        cl.show(cards, "open");

        mainFrame.getContentPane().add(cards);
        mainFrame.setVisible(true);
    }

    private static void blink(JComponent c, Color blink, Color constant) {
        c.setForeground(blink);
        setTimeout(() -> {c.setForeground(constant);}, 1000);
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

    private static String getSelectedStr(JComboBox cb) {
        return String.valueOf(cb.getSelectedItem());
    }

    /**
     * Erase all information about calculated steps and reload
     *
     * @param pre preview of the steps
     */
    private static void reloadSteps(JTextArea pre, JComboBox source, JComboBox b1, JComboBox b2, JComboBox b3) {
        lsa.Reset();
        pre.setText("");
        reloadNodeSelector(source);
        reloadNodeSelector(b1);
        reloadNodeSelector(b2);
        reloadNodeSelector(b3);
        lsa.setSource(getSelectedStr(source));
    }

    private static void reloadNodeSelector(JComboBox box) {
        DefaultComboBoxModel model = new DefaultComboBoxModel(new Vector(lsa.Nodes.keySet()));
        box.setModel(model);
    }

    private static void createPopUpWindow(String msg) {
        JOptionPane.showMessageDialog(new JFrame(), msg, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private static void enableButton(JButton button, boolean bool) {
        button.setEnabled(bool);
        if (bool) {
            button.setForeground(ACTIVE);
        } else {
            button.setForeground(null);
        }
    }

    private static JFrame createWindow(String title, int width, int height) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(width, height);
        if (width <= 500 && height <= 300)
            frame.setMinimumSize(new Dimension(width, height));
        else
            frame.setMinimumSize(new Dimension(500, 300));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        return frame;
    }

    public static void main(String[] args) {
        lsa = new LSA();
        javax.swing.SwingUtilities.invokeLater(() -> {
            mainFrame = createWindow("LSA Demo", 800, 600);
            createContentPane();
        });
    }
}