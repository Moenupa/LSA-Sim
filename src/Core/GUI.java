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
    private static final Color WARN = new Color(255, 149, 0);
    private static final Color ERROR = new Color(255, 59, 48);

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
        JTextArea preview = new JTextArea("\n\n\n\nPreview of the file will be shown here.\n\n\n\n");

        // appearance setting
        // preview.setEditable(false);
        preview.setBorder(BorderFactory.createCompoundBorder(
                preview.getBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        enableButton(proceed, false);
        enableButton(openFile, true);

        JComboBox<String> srcSelector = new JComboBox<>();
        JLabel selectLabel = new JLabel("Select Source: ");
        JButton step = new JButton("Step");
        JButton computeAll = new JButton("Compute All");
        JButton back = new JButton("Return");
        JTextArea selectPreview = new JTextArea("");
        JTextArea stepPreview = new JTextArea();
        back.setForeground(WARN);

        // bind with actions
        openFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fileChooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().matches("\\.lsr\\s*$") || f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return "LSR Files, .lsr";
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

                    // enable button to switch page
                    enableButton(proceed, true);

                    // parse file
                    lsa.parse();
                    openFile.setForeground(PASS);
                } catch (IOException ex) {
                    openFile.setForeground(ERROR);
                    createPopUpWindow("Read/Write Error.");
                } catch (IllegalArgumentException ex) {
                    openFile.setForeground(ERROR);
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
                FileWriter fw = new FileWriter(file);
                preview.write(fw);
                fw.close();

                lsa.loadFromFile(file);
                lsa.parse();

                // load source selector and set first source as default
                DefaultComboBoxModel model = new DefaultComboBoxModel(new Vector(lsa.Nodes.keySet()));
                srcSelector.setModel(model);
                lsa.setSource(String.valueOf(srcSelector.getSelectedItem()));
                selectPreview.setText("Source: " + lsa.source);
                stepPreview.setText("");
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

        back.addActionListener(e -> {
            cl.next(cards);
        });
        srcSelector.addActionListener(e -> {
            lsa.setSource(
                    String.valueOf(srcSelector.getSelectedItem())
            );
            selectPreview.setText("Source: " + lsa.source);
            stepPreview.setText("");
        });
        step.addActionListener(e -> {
            String ret = lsa.SingleStep();
            stepPreview.append("SingleStep Ret: " + ret + "\n");
            if (!ret.isEmpty())
                stepPreview.append(lsa.toString() + "\n\n");
        });
        computeAll.addActionListener(e -> {
            lsa.Run();
            stepPreview.setText(lsa.toString() + "\n\n");
        });

        // btn groups and preview groups
        JPanel e_btn_g = new JPanel();
        e_btn_g.add(selectLabel); e_btn_g.add(srcSelector);
        e_btn_g.add(step); e_btn_g.add(computeAll); e_btn_g.add(back);

        JPanel f_btn_g = new JPanel();
        f_btn_g.add(openFile); f_btn_g.add(proceed);

        JPanel e_pre_g = new JPanel(new BorderLayout());
        JScrollPane stepScroll = new JScrollPane(
                stepPreview,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        e_pre_g.add(selectPreview, BorderLayout.SOUTH);
        e_pre_g.add(stepScroll, BorderLayout.CENTER);

        open.add(f_btn_g, BorderLayout.SOUTH);
        open.add(preview, BorderLayout.CENTER);
        exe.add(e_btn_g, BorderLayout.SOUTH);
        exe.add(e_pre_g, BorderLayout.CENTER);

        cl.show(cards, "open");

        mainFrame.getContentPane().add(cards);
        mainFrame.setVisible(true);
    }

    private static void createMenuBar() {

    }

    private static void createPopUpWindow(String msg) {
        JFrame popup = createWindow("Error",400,200);
        popup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        popup.setLayout(new BorderLayout());
        JPanel btn_g = new JPanel();

        JLabel label = new JLabel(msg, SwingConstants.CENTER);
        JButton btn_ok = new JButton("OK");
        btn_ok.addActionListener(e -> {
            popup.dispose();
        });

        btn_g.add(btn_ok);

        popup.add(label, BorderLayout.CENTER);
        popup.add(btn_g, BorderLayout.SOUTH);
        popup.setVisible(true);
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
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        return frame;
    }

    public static void main(String[] args) {
        lsa = new LSA();
        javax.swing.SwingUtilities.invokeLater(() -> {
            mainFrame = createWindow("LSA Demo", 800, 600);
            createContentPane();
            createMenuBar();
        });
    }
}