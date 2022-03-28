package Core;

import java.awt.*;
import java.io.File;
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
        preview.setEditable(false);
        preview.setBorder(BorderFactory.createCompoundBorder(
                preview.getBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        enableButton(proceed, false);
        enableButton(openFile, true);

        // TODO: step and computeAll buttons
        JComboBox<String> srcSelector = new JComboBox<>();
        JLabel selectLabel = new JLabel("Select Source: ");
        JButton step = new JButton("Step");
        JButton computeAll = new JButton("Compute All");
        JButton back = new JButton("Return");
        JTextArea selectPreview = new JTextArea("Source: ");
        JTextArea stepPreview = new JTextArea();
        back.setForeground(WARN);

        // bind with actions
        openFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fileChooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().matches("\\.lsr$") || f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return "LSR Files";
                }
            });
            int option = fileChooser.showOpenDialog(mainFrame);
            if (option == JFileChooser.APPROVE_OPTION){
                file = fileChooser.getSelectedFile();
                openFile.setText(file.getName());

                try {
                    // read&process file and show preview
                    lsa.Initialize(file.getAbsolutePath());
                    preview.setText("");
                    for (String line : lsa.text)
                        preview.append(line + "\n");

                    // enable button to switch page
                    enableButton(proceed, true);
                    openFile.setForeground(PASS);
                    DefaultComboBoxModel model = new DefaultComboBoxModel(new Vector(lsa.Nodes.keySet()));
                    srcSelector.setModel( model );
                    lsa.setSource(String.valueOf(srcSelector.getSelectedItem()));
                    selectPreview.append(lsa.source);
                } catch (Exception ex) {
                    preview.setText("\n\nError when parsing the file.\nPlease recheck file format.\n\n");
                    openFile.setForeground(ERROR);
                    enableButton(proceed, false);
                }
            }
        });
        proceed.addActionListener(e -> {
            cl.next(cards);
        });

        back.addActionListener(e -> {
            cl.next(cards);
        });
        srcSelector.addActionListener(e -> {
            lsa.setSource(
                    String.valueOf(srcSelector.getSelectedItem())
            );
            selectPreview.append(lsa.source);
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