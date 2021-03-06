// Copyright 2009, StackFrame, LLC
// This code is licensed under GPL v2.0 http://www.gnu.org/licenses/gpl-2.0.html

package com.stackframe.pdfliberator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.reflect.Field;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;

import com.lowagie.text.DocumentException;

import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;

/**
 * A utility for removing restrictions on PDF files by removing the owner password.
 *
 * @author Gene McCulley <a href="mailto:mcculley@stackframe.com">mcculley@stackframe.com</a>
 */
public class PDFLiberator {

    private PDFLiberator() {
        // Inhibit construction as all methods are static.
    }

    /**
     * Set the value of a potentially private boolean field on an object.
     *
     * @param o the Object on which to set the field
     * @param fieldName the name of the field to set
     * @param value the new value to set the field to
     * @throws NoSuchFieldException if <code>fieldName</code> is not the name of a field in the class of <code>o</code>
     */
    private static void setBooleanField(Object o, String fieldName, boolean value) throws NoSuchFieldException {
        Field field = o.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        try {
            field.setBoolean(o, value);
        } catch (IllegalAccessException iae) {
            // The call to setAccessible() should have stopped this from happening.  If it didn't then we are probably running in
            // some more strict container or virtual machine.
            throw new RuntimeException(iae);
        }
    }

    /**
     * Removes the owner password on a PDF document passed through as a stream.
     *
     * @param input the InputStream containing the document to liberate
     * @param output the OutputStream to write the liberated document to
     * @throws IOException if there is a problem reading from or writing to the supplied streams
     * @throws DocumentException if there is a problem parsing or writing the PDF document
     */
    public static void liberate(InputStream input, OutputStream output) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(input);
        try {
            setBooleanField(reader, "ownerPasswordUsed", false);
            setBooleanField(reader, "encrypted", false);
        } catch (NoSuchFieldException nsfe) {
            // We expect these fields to be part of iText.  If they are not found, then we are probably using a different version.
            AssertionError ae = new AssertionError("could not find a field");
            ae.initCause(nsfe);
            throw ae;
        }

        reader.removeUsageRights();
        PdfCopyFields copy = new PdfCopyFields(output);
        copy.addDocument(reader);
        copy.close();
    }

    /**
     * Removes the owner password on a PDF file.
     *
     * @param file the File to liberate.
     * @throws Exception if anything goes wrong and leaves <code>file</code> untouched
     */
    public static void liberate(File file) throws Exception {
        File tmp = File.createTempFile(file.getName(), ".tmp", file.getParentFile());
        tmp.deleteOnExit();
        liberate(new FileInputStream(file), new FileOutputStream(tmp));
        tmp.renameTo(file);
    }

    /**
     * The command line entry point.
     *
     * @param args the files to liberate
     */
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("-")) {
            try {
                liberate(System.in, System.out);
            } catch (Exception e) {
                System.err.println("Error processing stdin: " + e);
            }
        } else if (args.length == 0) {
            GUI.launch();
        } else {
            for (String arg : args) {
                System.out.println("Liberating " + arg + '.');
                try {
                    liberate(new File(arg));
                } catch (Exception e) {
                    System.err.println("Could not fix " + arg + ": " + e);
                }
            }
        }
    }

}
