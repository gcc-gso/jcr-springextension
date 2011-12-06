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
package org.springframework.extensions.jcr.jackrabbit.ocm;

import java.util.Collection;

import org.apache.jackrabbit.ocm.query.Query;
import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of JCR mapping operations. Not often used, but a useful option to
 * enhance testability, as it can easily be mocked or stubbed.
 * <p/>
 * <p/>
 * Provides JcrMappingTemplate's data access methods that mirror various PersistenceManager methods. See the
 * required javadocs for details on those methods.
 * @author Costin Leau
 */
public interface JcrMappingOperations {

    /**
     * Execute a JcrMappingCallback.
     * @param callback callback to execute
     * @return the callback result
     * @throws DataAccessException
     */
    public <T> T execute(JcrMappingCallback<T> callback) throws DataAccessException;

    /**
     * @param object
     * @see org.apache.jackrabbit.ocm.manager.ObjectContentManager#insert(java.lang.Object)
     */
    public void insert(final java.lang.Object object);

    /**
     * @param object
     * @see org.apache.jackrabbit.ocm.manager.ObjectContentManager#update(java.lang.Object)
     */
    public void update(final java.lang.Object object);

    /**
     * @param path
     * @see org.apache.jackrabbit.ocm.manager.ObjectContentManager#remove(java.lang.String)
     */
    public void remove(final java.lang.String path);

    /**
     * @param path
     * @see org.apache.jackrabbit.ocm.manager.ObjectContentManager#getObject(java.lang.String)
     */
    public Object getObject(final java.lang.String path);

    /**
     * @param query
     * @return
     * @see org.apache.jackrabbit.ocm.manager.ObjectContentManager#getObjects(org.apache.jackrabbit.ocm.query.Query)
     */
    public Collection getObjects(final Query query);

}