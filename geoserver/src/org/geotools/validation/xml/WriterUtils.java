/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;


/**
 * WriterUtils purpose.
 * 
 * <p>
 * Used to provide assitance writing xml to a Writer.
 * </p>
 * 
 * <p></p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @version $Id: WriterUtils.java,v 1.6 2004/01/21 21:51:45 dmzwiers Exp $
 */
class WriterUtils {

    /** The output writer. */
    protected Writer writer;

    /**
     * WriterUtils constructor.
     * 
     * <p>
     * Should never be called.
     * </p>
     */
    protected WriterUtils() {
    }

    /**
     * WriterUtils constructor.
     * 
     * <p>
     * Stores the specified writer to use for output.
     * </p>
     *
     * @param writer the writer which will be used for outputing the xml.
     */
    public WriterUtils(Writer writer) {
        this.writer = writer;
    }

    /**
     * write purpose.
     * 
     * <p>
     * Writes the String specified to the stored output writer.
     * </p>
     *
     * @param s The String to write.
     *
     * @throws ValidationException When an IO exception occurs.
     */
    public void write(String s) throws ValidationException {
        try {
            writer.write(s);
            writer.flush();
        } catch (IOException e) {
            throw new ValidationException("Write" + writer, e);
        }
    }

    /**
     * writeln purpose.
     * 
     * <p>
     * Writes the String specified to the stored output writer.
     * </p>
     *
     * @param s The String to write.
     *
     * @throws ValidationException When an IO exception occurs.
     */
    public void writeln(String s) throws ValidationException {
        try {
            writer.write(s + "\n");
            writer.flush();
        } catch (IOException e) {
            throw new ValidationException("Writeln" + writer, e);
        }
    }

    /**
     * openTag purpose.
     * 
     * <p>
     * Writes an open xml tag with the name specified to the stored output
     * writer.
     * </p>
     *
     * @param tagName The tag name to write.
     *
     * @throws ValidationException When an IO exception occurs.
     */
    public void openTag(String tagName) throws ValidationException {
        writeln("<" + tagName + ">");
    }

    /**
     * openTag purpose.
     * 
     * <p>
     * Writes an open xml tag with the name and attributes specified to the
     * stored output writer.
     * </p>
     *
     * @param tagName The tag name to write.
     * @param attributes The tag attributes to write.
     *
     * @throws ValidationException When an IO exception occurs.
     */
    public void openTag(String tagName, Map attributes)
        throws ValidationException {
        write("<" + tagName + " ");

        Iterator i = attributes.keySet().iterator();

        while (i.hasNext()) {
            String s = (String) i.next();
            write(s + " = " + "\"" + (attributes.get(s)).toString() + "\" ");
        }

        writeln(">");
    }

    /**
     * closeTag purpose.
     * 
     * <p>
     * Writes an close xml tag with the name specified to the stored output
     * writer.
     * </p>
     *
     * @param tagName The tag name to write.
     *
     * @throws ValidationException When an IO exception occurs.
     */
    public void closeTag(String tagName) throws ValidationException {
        writeln("</" + tagName + ">");
    }

    /**
     * textTag purpose.
     * 
     * <p>
     * Writes a text xml tag with the name and text specified to the stored
     * output writer.
     * </p>
     *
     * @param tagName The tag name to write.
     * @param data The text data to write.
     *
     * @throws ValidationException When an IO exception occurs.
     */
    public void textTag(String tagName, String data)
        throws ValidationException {
        writeln("<" + tagName + ">" + data + "</" + tagName + ">");
    }

    /**
     * valueTag purpose.
     * 
     * <p>
     * Writes an xml tag with the name and value specified to the stored output
     * writer.
     * </p>
     *
     * @param tagName The tag name to write.
     * @param value The text data to write.
     *
     * @throws ValidationException When an IO exception occurs.
     */
    public void valueTag(String tagName, String value)
        throws ValidationException {
        writeln("<" + tagName + " value = \"" + value + "\" />");
    }

    /**
     * attrTag purpose.
     * 
     * <p>
     * Writes an xml tag with the name and attributes specified to the stored
     * output writer.
     * </p>
     *
     * @param tagName The tag name to write.
     * @param attributes The tag attributes to write.
     *
     * @throws ValidationException When an IO exception occurs.
     */
    public void attrTag(String tagName, Map attributes)
        throws ValidationException {
        write("<" + tagName + " ");

        Iterator i = attributes.keySet().iterator();

        while (i.hasNext()) {
            String s = (String) i.next();
            write(s + " = " + "\"" + (attributes.get(s)).toString() + "\" ");
        }

        write(" />");
    }

    /**
     * textTag purpose.
     * 
     * <p>
     * Writes an xml tag with the name, text and attributes specified to the
     * stored output writer.
     * </p>
     *
     * @param tagName The tag name to write.
     * @param attributes The tag attributes to write.
     * @param data The tag text to write.
     *
     * @throws ValidationException When an IO exception occurs.
     */
    public void textTag(String tagName, Map attributes, String data)
        throws ValidationException {
        write("<" + tagName + " ");

        Iterator i = attributes.keySet().iterator();

        while (i.hasNext()) {
            String s = (String) i.next();
            write(s + " = " + "\"" + (attributes.get(s)).toString() + "\" ");
        }

        write(">" + data + "</" + tagName + ">");
    }

    /**
     * comment purpose.
     * 
     * <p>
     * Writes an xml comment with the text specified to the stored output
     * writer.
     * </p>
     *
     * @param comment The comment text to write.
     *
     * @throws ValidationException When an IO exception occurs.
     */
    public void comment(String comment) throws ValidationException {
        writeln("<!--");
        writeln(comment);
        writeln("-->");
    }
}
