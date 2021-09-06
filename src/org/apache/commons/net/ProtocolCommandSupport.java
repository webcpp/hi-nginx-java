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

package org.apache.commons.net;

import java.io.Serializable;
import java.util.EventListener;

import org.apache.commons.net.util.ListenerList;

/**
 * ProtocolCommandSupport is a convenience class for managing a list of
 * ProtocolCommandListeners and firing ProtocolCommandEvents.  You can
 * simply delegate ProtocolCommandEvent firing and listener
 * registering/unregistering tasks to this class.
 *
 *
 * @see ProtocolCommandEvent
 * @see ProtocolCommandListener
 */

public class ProtocolCommandSupport implements Serializable
{
    private static final long serialVersionUID = -8017692739988399978L;

    private final Object source;
    private final ListenerList listeners;

    /**
     * Creates a ProtocolCommandSupport instance using the indicated source
     * as the source of ProtocolCommandEvents.
     *
     * @param source  The source to use for all generated ProtocolCommandEvents.
     */
    public ProtocolCommandSupport(final Object source)
    {
        this.listeners = new ListenerList();
        this.source = source;
    }


    /**
     * Fires a ProtocolCommandEvent signalling the sending of a command to all
     * registered listeners, invoking their
     * {@link org.apache.commons.net.ProtocolCommandListener#protocolCommandSent protocolCommandSent() }
     *  methods.
     *
     * @param command The string representation of the command type sent, not
     *      including the arguments (e.g., "STAT" or "GET").
     * @param message The entire command string verbatim as sent to the server,
     *        including all arguments.
     */
    public void fireCommandSent(final String command, final String message)
    {
        final ProtocolCommandEvent event;

        event = new ProtocolCommandEvent(source, command, message);

        for (final EventListener listener : listeners)
        {
           ((ProtocolCommandListener)listener).protocolCommandSent(event);
        }
    }

    /**
     * Fires a ProtocolCommandEvent signalling the reception of a command reply
     * to all registered listeners, invoking their
     * {@link org.apache.commons.net.ProtocolCommandListener#protocolReplyReceived protocolReplyReceived() }
     *  methods.
     *
     * @param replyCode The integer code indicating the natureof the reply.
     *   This will be the protocol integer value for protocols
     *   that use integer reply codes, or the reply class constant
     *   corresponding to the reply for protocols like POP3 that use
     *   strings like OK rather than integer codes (i.e., POP3Repy.OK).
     * @param message The entire reply as received from the server.
     */
    public void fireReplyReceived(final int replyCode, final String message)
    {
        final ProtocolCommandEvent event;
        event = new ProtocolCommandEvent(source, replyCode, message);

        for (final EventListener listener : listeners)
        {
            ((ProtocolCommandListener)listener).protocolReplyReceived(event);
        }
    }

    /**
     * Adds a ProtocolCommandListener.
     *
     * @param listener  The ProtocolCommandListener to add.
     */
    public void addProtocolCommandListener(final ProtocolCommandListener listener)
    {
        listeners.addListener(listener);
    }

    /**
     * Removes a ProtocolCommandListener.
     *
     * @param listener  The ProtocolCommandListener to remove.
     */
    public void removeProtocolCommandListener(final ProtocolCommandListener listener)
    {
        listeners.removeListener(listener);
    }


    /**
     * Returns the number of ProtocolCommandListeners currently registered.
     *
     * @return The number of ProtocolCommandListeners currently registered.
     */
    public int getListenerCount()
    {
        return listeners.getListenerCount();
    }

}

