// Copyright 2009, StackFrame, LLC
// This code is licensed under GPL v2.0 http://www.gnu.org/licenses/gpl-2.0.html

import java.awt.Color;

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
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import javax.swing.filechooser.FileFilter;

import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

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
     * A simple implementation of FileFilter for dealing with files of a particular extension.
     */
    private static class ExtensionFileFilter extends FileFilter {

        private final String extension;

        private ExtensionFileFilter(String extension) {
            this.extension = extension;
        }

        @Override
        public boolean accept(File f) {
            return f.getName().toLowerCase().endsWith("." + extension.toLowerCase());
        }

        @Override
        public String getDescription() {
            return extension + " files";
        }

    }

    /**
     * Instantiate the GUI.
     */
    public static void launch() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                final JFrame frame = new JFrame();
                final JTextPane text = new JTextPane();
                text.setEditable(false);
                frame.getContentPane().add(new JScrollPane(text));
                JMenuBar menuBar = new JMenuBar();
                frame.setJMenuBar(menuBar);
                JMenu fileMenu = new JMenu("File");
                // TODO: Use the OS X wrapper class to do the right thing for the menu bar.
                menuBar.add(fileMenu);
                JMenuItem liberateMenuItem = new JMenuItem("Liberate...");
                fileMenu.add(liberateMenuItem);
                liberateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                                                       liberateMenuItem.getToolkit().getMenuShortcutKeyMask()));
                liberateMenuItem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent evt) {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setDialogTitle("Liberate");
                        chooser.setFileFilter(new ExtensionFileFilter("PDF"));
                        chooser.setMultiSelectionEnabled(true);
                        // TODO: Use prefs to store current directory.
                        int result = chooser.showOpenDialog(frame);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            File[] selected = chooser.getSelectedFiles();
                            for (File file : selected) {
                                String message;
                                Color messageColor;
                                try {
                                    PDFLiberator.liberate(file);
                                    messageColor = Color.BLACK;
                                    message = "Liberated " + file;
                                } catch (Exception e) {
                                    messageColor = Color.RED;
                                    message = "Error processing " + file + ": " + e;
                                }

                                SimpleAttributeSet set = new SimpleAttributeSet();
                                set.addAttribute(StyleConstants.Foreground, messageColor);
                                try {
                                    text.getDocument().insertString(0, message + "\n", set);
                                } catch (BadLocationException ble) {
                                    throw new AssertionError(ble);
                                }
                            }
                        }
                    }

                });

                JMenuItem quit = new JMenuItem("Quit");
                fileMenu.add(quit);
                quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, quit.getToolkit().getMenuShortcutKeyMask()));
                quit.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent evt) {
                        System.exit(0);
                    }

                });

                frame.setSize(320, 240);
                frame.setVisible(true);
            }

        });
    }

}
