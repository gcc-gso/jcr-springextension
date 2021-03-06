/**
 * Copyright 2009-2012 the original author or authors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.springframework.extensions.jcr;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.observation.ObservationManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.jcr.support.ListSessionHolderProviderManager;

public class JcrSessionFactoryTest {

    private JcrSessionFactory factory;

    private Repository repository;

    @Before
    public void setUp() throws Exception {

        repository = createMock(Repository.class);
        factory = new JcrSessionFactory();
        factory.setRepository(repository);
    }

    @After
    public void tearDown() throws Exception {

        try {
            verify(repository);
        } catch (IllegalStateException ex) {
            // ignore: test method didn't call replay
        }

        repository = null;
        factory = null;
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrSessionFactory.getSession()'
     */
    @Test
    public void testGetSession() {
        try {

            expect(repository.login(null, null)).andReturn(null);
            factory.getSession();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrSessionFactory.afterPropertiesSet'
     */
    @Test
    public void testAfterPropertiesSet() throws Exception {
        try {
            factory.setRepository(null);
            factory.afterPropertiesSet();
            fail("expected exception (session factory badly initialized");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testConstructor() {
        factory = new JcrSessionFactory(repository, "ws", null);
        assertEquals(repository, factory.getRepository());
        assertEquals("ws", factory.getWorkspaceName());
        assertNull(factory.getCredentials());

        factory.setWorkspaceName("ws");
        assertEquals(factory.getWorkspaceName(), "ws");
    }

    @Test
    public void testEquals() {
        assertEquals(factory.hashCode(), repository.hashCode() + 17 * 37);
        assertFalse(factory.equals(null));
        assertEquals(factory, factory);

        Repository repo2 = createNiceMock(Repository.class);

        replay(repo2, repository);

        JcrSessionFactory fact2 = new JcrSessionFactory();
        fact2.setRepository(repo2);
        assertFalse(factory.equals(fact2));
    }

    @Test
    public void testAddListeners() throws RepositoryException {
        EventListenerDefinition def1 = new EventListenerDefinition();
        EventListenerDefinition def2 = new EventListenerDefinition();

        EventListenerDefinition listeners[] = new EventListenerDefinition[] { def1, def2 };
        factory.setEventListeners(listeners);

        Session session = createMock(Session.class);

        Workspace workspace = createMock(Workspace.class);

        ObservationManager observationManager = createMock(ObservationManager.class);

        expect(repository.login(null, null)).andReturn(session);
        expect(session.getWorkspace()).andReturn(workspace);

        expect(workspace.getObservationManager()).andReturn(observationManager);

        observationManager.addEventListener(def1.getListener(), def1.getEventTypes(), def1.getAbsPath(), def1.isDeep(),
                def1.getUuid(), def1.getNodeTypeName(), def1.isNoLocal());
        observationManager.addEventListener(def2.getListener(), def2.getEventTypes(), def2.getAbsPath(), def2.isDeep(),
                def2.getUuid(), def2.getNodeTypeName(), def2.isNoLocal());

        replay(repository, session, workspace, observationManager);

        // coverage madness
        assertSame(listeners, factory.getEventListeners());
        Session sess = factory.getSession();
        assertSame(session, sess);

        verify(repository, session, workspace, observationManager);
    }

    @Test
    public void testRegisterNamespaces() throws Exception {
        Properties namespaces = new Properties();
        namespaces.put("foo", "bar");
        namespaces.put("hocus", "pocus");

        factory.setNamespaces(namespaces);

        Session session = createMock(Session.class);

        Workspace workspace = createMock(Workspace.class);

        NamespaceRegistry namespaceRegistry = createMock(NamespaceRegistry.class);

        // afterPropertiesSet
        expect(repository.login(null, null)).andReturn(session);
        expect(session.getWorkspace()).andReturn(workspace);
        expect(workspace.getNamespaceRegistry()).andReturn(namespaceRegistry);

        expect(namespaceRegistry.getPrefixes()).andReturn(new String[0]);

        // destroy
        namespaceRegistry.registerNamespace("foo", "bar");
        namespaceRegistry.registerNamespace("hocus", "pocus");

        session.logout();

        replay(namespaceRegistry, workspace, session, repository);

        factory.afterPropertiesSet();

        factory.destroy();

        verify(namespaceRegistry, workspace, session);

    }

    @Test
    public void testForceRegistryNamespace() throws Exception {
        String foo = "foo";
        Properties namespaces = new Properties();
        namespaces.put(foo, "bar");
        namespaces.put("hocus", "pocus");

        factory.setNamespaces(namespaces);
        factory.setForceNamespacesRegistration(true);
        factory.setSkipExistingNamespaces(false);
        factory.setKeepNewNamespaces(false);

        Session session = createMock(Session.class);

        Workspace workspace = createMock(Workspace.class);

        NamespaceRegistry namespaceRegistry = createMock(NamespaceRegistry.class);

        // afterPropertiesSet
        expect(repository.login(null, null)).andReturn(session);
        expect(session.getWorkspace()).andReturn(workspace);
        expect(workspace.getNamespaceRegistry()).andReturn(namespaceRegistry);

        // destroy
        expect(repository.login(null, null)).andReturn(session);
        expect(session.getWorkspace()).andReturn(workspace);
        expect(workspace.getNamespaceRegistry()).andReturn(namespaceRegistry);

        // registry record
        String[] prefixes = new String[] { foo };
        String oldURI = "old bar";
        expect(namespaceRegistry.getPrefixes()).andReturn(prefixes);
        expect(namespaceRegistry.getURI(foo)).andReturn(oldURI);

        session.logout();

        namespaceRegistry.unregisterNamespace(foo);

        namespaceRegistry.registerNamespace(foo, "bar");
        namespaceRegistry.registerNamespace("hocus", "pocus");

        namespaceRegistry.unregisterNamespace("foo");
        namespaceRegistry.unregisterNamespace("hocus");
        namespaceRegistry.registerNamespace(foo, oldURI);

        replay(namespaceRegistry, workspace, session, repository);

        factory.afterPropertiesSet();
        factory.destroy();

        verify(namespaceRegistry, workspace, session);
    }

    @Test
    public void testKeepRegistryNamespace() throws Exception {
        Properties namespaces = new Properties();
        namespaces.put("foo", "bar");
        namespaces.put("hocus", "pocus");

        factory.setNamespaces(namespaces);
        factory.setKeepNewNamespaces(true);

        Session session = createMock(Session.class);

        Workspace workspace = createMock(Workspace.class);

        NamespaceRegistry namespaceRegistry = createMock(NamespaceRegistry.class);

        // afterPropertiesSet
        expect(repository.login(null, null)).andReturn(session);
        expect(session.getWorkspace()).andReturn(workspace);
        expect(workspace.getNamespaceRegistry()).andReturn(namespaceRegistry);

        expect(namespaceRegistry.getPrefixes()).andReturn(new String[0]);

        namespaceRegistry.registerNamespace("foo", "bar");
        namespaceRegistry.registerNamespace("hocus", "pocus");

        session.logout();

        replay(namespaceRegistry, workspace, session, repository);

        factory.afterPropertiesSet();

        factory.destroy();

        verify(namespaceRegistry, workspace, session);
    }

    @Test
    public void testSkipRegisteredNamespaces() throws Exception {
        Properties namespaces = new Properties();
        namespaces.put("foo", "bar");
        namespaces.put("hocus", "pocus");

        factory.setNamespaces(namespaces);
        factory.setSkipExistingNamespaces(false);

        Session session = createMock(Session.class);

        Workspace workspace = createMock(Workspace.class);

        NamespaceRegistry namespaceRegistry = createMock(NamespaceRegistry.class);

        // afterPropertiesSet
        expect(repository.login(null, null)).andReturn(session);
        expect(session.getWorkspace()).andReturn(workspace);
        expect(workspace.getNamespaceRegistry()).andReturn(namespaceRegistry);

        namespaceRegistry.registerNamespace("foo", "bar");
        namespaceRegistry.registerNamespace("hocus", "pocus");

        session.logout();

        expect(namespaceRegistry.getPrefixes()).andReturn(new String[0]);

        replay(namespaceRegistry, workspace, session, repository);

        factory.afterPropertiesSet();

        factory.destroy();

        verify(namespaceRegistry, workspace, session);
    }

    @Test
    public void testDefaultSesionHolder() throws Exception {
        factory.afterPropertiesSet();
        Session session = factory.getSession();
        SessionHolder holder = factory.getSessionHolder(session);
        assertSame(SessionHolder.class, holder.getClass());
        // default session holder provider
        assertSame(SessionHolder.class, factory.getSessionHolder(null).getClass());
    }

    @Test
    public void testSessionHolder() throws Exception {
        final String REPO_NAME = "hocus_pocus";

        expect(repository.getDescriptor(Repository.REP_NAME_DESC)).andReturn(REPO_NAME);

        Session session = createMock(Session.class);

        expect(repository.login(null, null)).andReturn(session);

        replay(repository, session);

        List<SessionHolderProvider> providers = new ArrayList<SessionHolderProvider>();

        providers.add(new SessionHolderProvider() {

            /**
             * @see org.springframework.extensions.jcr.SessionHolderProvider#acceptsRepository(java.lang.String)
             */
            @Override
            public boolean acceptsRepository(String repositoryName) {
                return REPO_NAME.equals(repositoryName);
            }

            /**
             * @see org.springframework.extensions.jcr.SessionHolderProvider#createSessionHolder(javax.jcr.Session)
             */
            @Override
            public SessionHolder createSessionHolder(Session session) {
                return new CustomSessionHolder(session);
            }

        });

        ListSessionHolderProviderManager providerManager = new ListSessionHolderProviderManager();
        providerManager.setProviders(providers);

        factory.setSessionHolderProviderManager(providerManager);
        factory.afterPropertiesSet();

        Session sess = factory.getSession();
        assertSame(session, sess);
        assertSame(CustomSessionHolder.class, factory.getSessionHolder(sess).getClass());

        verify(repository, session);
    }

    /**
     * Used for testing.
     * @author Costin Leau
     * @author Sergio Bossa
     * @author Salvatore Incandela
     */
    private class CustomSessionHolder extends SessionHolder {

        /**
         * @param session
         */
        public CustomSessionHolder(Session session) {
            super(session);

        }

    }

}
