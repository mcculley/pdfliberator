// Copyright 2009, StackFrame, LLC
// This code is licensed under GPL v2.0 http://www.gnu.org/licenses/gpl-2.0.html

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.reflect.Field;

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
     * @param input the InputStream containing the document to enable
     * @param output the OutputStream to write the enabled document to
     * @throws IOException if there is a problem reading from or writing to the supplied streams
     * @throws DocumentException if there is a problem parsing or writing the PDF document
     */
    public static void enable(InputStream input, OutputStream output) throws IOException, DocumentException {
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
     * @param file the File to enable.
     * @throws Exception if anything goes wrong and leaves <code>file</code> untouched
     */
    public static void enable(File file) throws Exception {
        System.out.println("Enabling " + file);
        File tmp = File.createTempFile(file.getName(), ".tmp", file.getParentFile());
        tmp.deleteOnExit();
        enable(new FileInputStream(file), new FileOutputStream(tmp));
        tmp.renameTo(file);
    }

    /**
     * The command line entry point.
     *
     * @param args the files to enable
     */
    public static void main(String[] args) {
        for (String arg : args) {
            try {
                enable(new File(arg));
            } catch (Exception e) {
                System.err.println("Could not fix " + arg + ": " + e);
            }
        }
    }

}
