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

package org.apache.james.jmap.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.apache.james.jmap.model.mailbox.Mailbox;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.inmemory.InMemoryId;
import org.apache.james.mailbox.inmemory.InMemoryMailboxSessionMapperFactory;
import org.apache.james.mailbox.inmemory.manager.InMemoryIntegrationResources;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.store.mail.MailboxMapperFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailboxUtilsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailboxUtilsTest.class);

    private MailboxManager mailboxManager;
    private MailboxMapperFactory<InMemoryId> mailboxMapperFactory;
    private MailboxSession mailboxSession;
    private MailboxUtils<InMemoryId> sut;

    @Before
    public void setup() throws Exception {
        InMemoryIntegrationResources inMemoryIntegrationResources = new InMemoryIntegrationResources();
        mailboxManager = inMemoryIntegrationResources.createMailboxManager(inMemoryIntegrationResources.createGroupMembershipResolver());
        mailboxMapperFactory = new InMemoryMailboxSessionMapperFactory();
        mailboxSession = mailboxManager.login("user@domain.org", "pass", LOGGER);
        sut = new MailboxUtils<>(mailboxManager, mailboxMapperFactory);
    }

    @Test
    public void mailboxFromMailboxPathShouldReturnNotEmptyWhenMailboxExists() throws Exception {
        MailboxPath mailboxPath = new MailboxPath("#private", "user", "mailbox");
        mailboxManager.createMailbox(mailboxPath, mailboxSession);

        Optional<Mailbox> optionalMailbox = sut.mailboxFromMailboxPath(mailboxPath, mailboxSession);
        assertThat(optionalMailbox).isPresent();
        Mailbox mailbox = optionalMailbox.get();
        assertThat(mailbox.getId()).isNotEmpty();
    }

    @Test
    public void mailboxFromMailboxPathShouldReturnEmptyWhenMailboxDoesntExist() throws Exception {
        MailboxPath mailboxPath = new MailboxPath("#private", "user", "mailbox");

        Optional<Mailbox> optionalMailbox = sut.mailboxFromMailboxPath(mailboxPath, mailboxSession);
        assertThat(optionalMailbox).isEmpty();
    }

    @Test
    public void getNameShouldReturnMailboxNameWhenRootMailbox() throws Exception {
        String expected = "mailbox";
        MailboxPath mailboxPath = new MailboxPath("#private", "user", expected);

        String name = sut.getName(mailboxPath, mailboxSession);
        assertThat(name).isEqualTo(expected);
    }

    @Test
    public void getNameShouldReturnMailboxNameWhenChildMailbox() throws Exception {
        String expected = "mailbox";
        MailboxPath mailboxPath = new MailboxPath("#private", "user", "inbox." + expected);

        String name = sut.getName(mailboxPath, mailboxSession);
        assertThat(name).isEqualTo(expected);
    }

    @Test
    public void getNameShouldReturnMailboxNameWhenChildOfChildMailbox() throws Exception {
        String expected = "mailbox";
        MailboxPath mailboxPath = new MailboxPath("#private", "user", "inbox.children." + expected);

        String name = sut.getName(mailboxPath, mailboxSession);
        assertThat(name).isEqualTo(expected);
    }

    @Test
    public void getMailboxNameFromIdShouldReturnNotEmptyWhenMailboxExists() throws Exception {
        String expected = "mailbox";
        MailboxPath mailboxPath = new MailboxPath("#private", "user", expected);
        mailboxManager.createMailbox(mailboxPath, mailboxSession);
        InMemoryId mailboxId = mailboxMapperFactory.getMailboxMapper(mailboxSession)
            .findMailboxByPath(mailboxPath)
            .getMailboxId();

        Optional<String> optionalName = sut.getMailboxNameFromId(mailboxId.serialize(), mailboxSession);
        assertThat(optionalName).isPresent();
        String name = optionalName.get();
        assertThat(name).isEqualTo(expected);
    }

    @Test
    public void getMailboxNameFromIdShouldReturnEmptyWhenMailboxDoesntExist() throws Exception {
        Optional<String> optionalName = sut.getMailboxNameFromId("unknown", mailboxSession);
        assertThat(optionalName).isEmpty();
    }

    @Test
    public void getParentIdFromMailboxPathShouldReturNullWhenRootMailbox() throws Exception {
        MailboxPath mailboxPath = new MailboxPath("#private", "user", "mailbox");
        mailboxManager.createMailbox(mailboxPath, mailboxSession);

        String id = sut.getParentIdFromMailboxPath(mailboxPath, mailboxSession);
        assertThat(id).isNull();
    }

    @Test
    public void getParentIdFromMailboxPathShouldReturnParentIdWhenChildMailbox() throws Exception {
        MailboxPath parentMailboxPath = new MailboxPath("#private", "user", "inbox");
        mailboxManager.createMailbox(parentMailboxPath, mailboxSession);
        InMemoryId parentId = mailboxMapperFactory.getMailboxMapper(mailboxSession)
                .findMailboxByPath(parentMailboxPath)
                .getMailboxId();

        MailboxPath mailboxPath = new MailboxPath("#private", "user", "inbox.mailbox");
        mailboxManager.createMailbox(mailboxPath, mailboxSession);

        String id = sut.getParentIdFromMailboxPath(mailboxPath, mailboxSession);
        assertThat(id).isEqualTo(parentId.serialize());
    }

    @Test
    public void getParentIdFromMailboxPathShouldReturnParentIdWhenChildOfChildMailbox() throws Exception {
        MailboxPath mailboxPath = new MailboxPath("#private", "user", "inbox.children.mailbox");
        mailboxManager.createMailbox(new MailboxPath("#private", "user", "inbox"), mailboxSession);

        MailboxPath parentMailboxPath = new MailboxPath("#private", "user", "inbox.children");
        mailboxManager.createMailbox(parentMailboxPath, mailboxSession);
        InMemoryId parentId = mailboxMapperFactory.getMailboxMapper(mailboxSession)
                .findMailboxByPath(parentMailboxPath)
                .getMailboxId();

        mailboxManager.createMailbox(mailboxPath, mailboxSession);

        String id = sut.getParentIdFromMailboxPath(mailboxPath, mailboxSession);
        assertThat(id).isEqualTo(parentId.serialize());
    }
}
