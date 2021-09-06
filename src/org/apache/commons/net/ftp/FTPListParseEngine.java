/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.ftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.net.util.Charsets;


/**
 * This class handles the entire process of parsing a listing of
 * file entries from the server.
 * <p>
 * This object defines a two-part parsing mechanism.
 * <p>
 * The first part is comprised of reading the raw input into an internal
 * list of strings.  Every item in this list corresponds to an actual
 * file.  All extraneous matter emitted by the server will have been
 * removed by the end of this phase.  This is accomplished in conjunction
 * with the FTPFileEntryParser associated with this engine, by calling
 * its methods <code>readNextEntry()</code> - which handles the issue of
 * what delimits one entry from another, usually but not always a line
 * feed and <code>preParse()</code> - which handles removal of
 * extraneous matter such as the preliminary lines of a listing, removal
 * of duplicates on versioning systems, etc.
 * <p>
 * The second part is composed of the actual parsing, again in conjunction
 * with the particular parser used by this engine.  This is controlled
 * by an iterator over the internal list of strings.  This may be done
 * either in block mode, by calling the <code>getNext()</code> and
 * <code>getPrevious()</code> methods to provide "paged" output of less
 * than the whole list at one time, or by calling the
 * <code>getFiles()</code> method to return the entire list.
 * <p>
 * Examples:
 * <p>
 * Paged access:
 * <pre>
 *    FTPClient f=FTPClient();
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPListParseEngine engine = f.initiateListParsing(directory);
 *
 *    while (engine.hasNext()) {
 *       FTPFile[] files = engine.getNext(25);  // "page size" you want
 *       //do whatever you want with these files, display them, etc.
 *       //expensive FTPFile objects not created until needed.
 *    }
 * </pre>
 * <p>
 * For unpaged access, simply use FTPClient.listFiles().  That method
 * uses this class transparently.
 */
public class FTPListParseEngine {
    private List<String> entries = new LinkedList<>();
    private ListIterator<String> internalIterator = entries.listIterator();

    private final FTPFileEntryParser parser;
    // Should invalid files (parse failures) be allowed?
    private final boolean saveUnparseableEntries;

    /**
     * An empty immutable {@code FTPFile} array.
     */
    private static final FTPFile[] EMPTY_FTP_FILE_ARRAY = new FTPFile[0];

    public FTPListParseEngine(final FTPFileEntryParser parser) {
        this(parser, null);
    }

    /**
     * Intended for use by FTPClient only
     * @since 3.4
     */
    FTPListParseEngine(final FTPFileEntryParser parser, final FTPClientConfig configuration) {
        this.parser = parser;
        if (configuration != null) {
            this.saveUnparseableEntries = configuration.getUnparseableEntries();
        } else {
            this.saveUnparseableEntries = false;
        }
    }

    /**
     * Reads (and closes) the initial reading and preparsing of the list returned by the server. After this method has
     * completed, this object will contain a list of unparsed entries (Strings) each referring to a unique file on the
     * server.
     *
     * @param inputStream input stream provided by the server socket.
     * @param charsetName the encoding to be used for reading the stream
     *
     * @throws IOException thrown on any failure to read from the sever.
     */
    public void readServerList(final InputStream inputStream, final String charsetName) throws IOException {
        this.entries = new LinkedList<>();
        read(inputStream, charsetName);
        this.parser.preParse(this.entries);
        resetIterator();
    }

    /**
     * Internal method for reading (and closing) the input into the <code>entries</code> list. After this method has
     * completed, <code>entries</code> will contain a collection of entries (as defined by
     * <code>FTPFileEntryParser.readNextEntry()</code>), but this may contain various non-entry preliminary lines from
     * the server output, duplicates, and other data that will not be part of the final listing.
     *
     * @param inputStream The socket stream on which the input will be read.
     * @param charsetName The encoding to use.
     *
     * @throws IOException thrown on any failure to read the stream
     */
    private void read(final InputStream inputStream, final String charsetName) throws IOException {
        try (final BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputStream, Charsets.toCharset(charsetName)))) {

            String line = this.parser.readNextEntry(reader);

            while (line != null) {
                this.entries.add(line);
                line = this.parser.readNextEntry(reader);
            }
        }
    }

    /**
     * Returns an array of at most <code>quantityRequested</code> FTPFile
     * objects starting at this object's internal iterator's current position.
     * If fewer than <code>quantityRequested</code> such
     * elements are available, the returned array will have a length equal
     * to the number of entries at and after after the current position.
     * If no such entries are found, this array will have a length of 0.
     *
     * After this method is called this object's internal iterator is advanced
     * by a number of positions equal to the size of the array returned.
     *
     * @param quantityRequested
     * the maximum number of entries we want to get.
     *
     * @return an array of at most <code>quantityRequested</code> FTPFile
     * objects starting at the current position of this iterator within its
     * list and at least the number of elements which  exist in the list at
     * and after its current position.
     * <p><b>
     * NOTE:</b> This array may contain null members if any of the
     * individual file listings failed to parse.  The caller should
     * check each entry for null before referencing it.
     */
    public FTPFile[] getNext(final int quantityRequested) {
        final List<FTPFile> tmpResults = new LinkedList<>();
        int count = quantityRequested;
        while (count > 0 && this.internalIterator.hasNext()) {
            final String entry = this.internalIterator.next();
            FTPFile temp = this.parser.parseFTPEntry(entry);
            if (temp == null && saveUnparseableEntries) {
                temp = new FTPFile(entry);
            }
            tmpResults.add(temp);
            count--;
        }
        return tmpResults.toArray(EMPTY_FTP_FILE_ARRAY);

    }

    /**
     * Returns an array of at most <code>quantityRequested</code> FTPFile
     * objects starting at this object's internal iterator's current position,
     * and working back toward the beginning.
     *
     * If fewer than <code>quantityRequested</code> such
     * elements are available, the returned array will have a length equal
     * to the number of entries at and after after the current position.
     * If no such entries are found, this array will have a length of 0.
     *
     * After this method is called this object's internal iterator is moved
     * back by a number of positions equal to the size of the array returned.
     *
     * @param quantityRequested
     * the maximum number of entries we want to get.
     *
     * @return an array of at most <code>quantityRequested</code> FTPFile
     * objects starting at the current position of this iterator within its
     * list and at least the number of elements which  exist in the list at
     * and after its current position.  This array will be in the same order
     * as the underlying list (not reversed).
     * <p><b>
     * NOTE:</b> This array may contain null members if any of the
     * individual file listings failed to parse.  The caller should
     * check each entry for null before referencing it.
     */
    public FTPFile[] getPrevious(final int quantityRequested) {
        final List<FTPFile> tmpResults = new LinkedList<>();
        int count = quantityRequested;
        while (count > 0 && this.internalIterator.hasPrevious()) {
            final String entry = this.internalIterator.previous();
            FTPFile temp = this.parser.parseFTPEntry(entry);
            if (temp == null && saveUnparseableEntries) {
                temp = new FTPFile(entry);
            }
            tmpResults.add(0,temp);
            count--;
        }
        return tmpResults.toArray(EMPTY_FTP_FILE_ARRAY);
    }

    /**
     * Returns an array of FTPFile objects containing the whole list of
     * files returned by the server as read by this object's parser.
     *
     * @return an array of FTPFile objects containing the whole list of
     *         files returned by the server as read by this object's parser.
     * None of the entries will be null
     * @throws IOException - not ever thrown, may be removed in a later release
     */
    public FTPFile[] getFiles()
    throws IOException // TODO remove; not actually thrown
    {
        return getFiles(FTPFileFilters.NON_NULL);
    }

    /**
     * Returns an array of FTPFile objects containing the whole list of
     * files returned by the server as read by this object's parser.
     * The files are filtered before being added to the array.
     *
     * @param filter FTPFileFilter, must not be <code>null</code>.
     *
     * @return an array of FTPFile objects containing the whole list of
     *         files returned by the server as read by this object's parser.
     * <p><b>
     * NOTE:</b> This array may contain null members if any of the
     * individual file listings failed to parse.  The caller should
     * check each entry for null before referencing it, or use the
     * a filter such as {@link FTPFileFilters#NON_NULL} which does not
     * allow null entries.
     * @since 2.2
     * @throws IOException - not ever thrown, may be removed in a later release
     */
    public FTPFile[] getFiles(final FTPFileFilter filter)
    throws IOException // TODO remove; not actually thrown
    {
        final List<FTPFile> tmpResults = new ArrayList<>();
        final Iterator<String> iter = this.entries.iterator();
        while (iter.hasNext()) {
            final String entry = iter.next();
            FTPFile temp = this.parser.parseFTPEntry(entry);
            if (temp == null && saveUnparseableEntries) {
                temp = new FTPFile(entry);
            }
            if (filter.accept(temp)) {
                tmpResults.add(temp);
            }
        }
        return tmpResults.toArray(EMPTY_FTP_FILE_ARRAY);

    }

    /**
     * convenience method to allow clients to know whether this object's
     * internal iterator's current position is at the end of the list.
     *
     * @return true if internal iterator is not at end of list, false
     * otherwise.
     */
    public boolean hasNext() {
        return internalIterator.hasNext();
    }

    /**
     * convenience method to allow clients to know whether this object's
     * internal iterator's current position is at the beginning of the list.
     *
     * @return true if internal iterator is not at beginning of list, false
     * otherwise.
     */
    public boolean hasPrevious() {
        return internalIterator.hasPrevious();
    }

    /**
     * resets this object's internal iterator to the beginning of the list.
     */
    public void resetIterator() {
        this.internalIterator = this.entries.listIterator();
    }

    // DEPRECATED METHODS - for API compatibility only - DO NOT USE

    /**
     * Do not use.
     * @param inputStream the stream from which to read
     * @throws IOException on error
     * @deprecated use {@link #readServerList(InputStream, String)} instead
    */
    @Deprecated
    public void readServerList(final InputStream inputStream) throws IOException {
        readServerList(inputStream, null);
    }

}
