/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mailbox.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.james.mailbox.events.Event;
import org.apache.james.mailbox.events.Group;
import org.apache.james.mailbox.events.MailboxListener;

public class EventCollector implements MailboxListener.GroupMailboxListener {
    public static class EventCollectorGroup extends Group {}

    private static final Group GROUP = new EventCollectorGroup();

    private final List<Event> events = new ArrayList<>();

    @Override
    public Group getDefaultGroup() {
        return GROUP;
    }

    public List<Event> getEvents() {
        return events;
    }

    @Override
    public void event(Event event) {
        events.add(event);
    }

}
