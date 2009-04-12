// Copyright 2009, StackFrame, LLC
// This code is licensed under GPL v2.0 http://www.gnu.org/licenses/gpl-2.0.html

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.io.File;

import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * A GUI for PDFLiberator.
 *
 * @author Gene McCulley <a href="mailto:mcculley@stackframe.com">mcculley@stackframe.com</a>
 */
public class GUI {

    private GUI() {
        // Inhibit construction as all methods are static.
    }

    /**
     * Instantiate the GUI.
     */
    public static void launch() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                final JFrame frame = new JFrame();
                final JTextArea textArea = new JTextArea();
                textArea.setEditable(false);
                frame.getContentPane().add(new JScrollPane(textArea));
                JMenuBar menuBar = new JMenuBar();
                frame.setJMenuBar(menuBar);
                JMenu fileMenu = new JMenu("File");
                menuBar.add(fileMenu);
                JMenuItem liberateMenuItem = new JMenuItem("Liberate...");
                liberateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                                                       liberateMenuItem.getToolkit().getMenuShortcutKeyMask()));
                liberateMenuItem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent evt) {
                        System.err.println("Liberate...");
                        JFileChooser chooser = new JFileChooser();
                        chooser.setMultiSelectionEnabled(true);
                        int result = chooser.showOpenDialog(frame);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            File[] selected = chooser.getSelectedFiles();
                            for (File file : selected) {
                                try {
                                    PDFLiberator.liberate(file);
                                    textArea.insert("Liberated " + file + ".\n", 0);
                                } catch (Exception e) {
                                    textArea.insert("Error processing " + file + ": " + e + '\n', 0);
                                }
                            }
                        }
                    }

                });
                fileMenu.add(liberateMenuItem);
                frame.setSize(320, 240);
                frame.setVisible(true);
            }

        });
    }

}
