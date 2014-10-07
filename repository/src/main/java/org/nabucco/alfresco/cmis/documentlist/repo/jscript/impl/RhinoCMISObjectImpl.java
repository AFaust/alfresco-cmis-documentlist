/*
 * Copyright 2013 PRODYNA AG
 *
 * Licensed under the Eclipse Public License (EPL), Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/eclipse-1.0.php or
 * http://www.nabucco.org/License.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.nabucco.alfresco.cmis.documentlist.repo.jscript.impl;

import org.alfresco.repo.jscript.Scopeable;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISObject;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISOperationContext;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISRepository;

/**
 * @author Axel Faust, <a href="http://www.prodyna.com">PRODYNA AG</a>
 */
public class RhinoCMISObjectImpl extends BaseCMISObject implements Scopeable
{

    protected Scriptable scope;

    public RhinoCMISObjectImpl(final ScriptCMISRepository repository, final CmisObject object)
    {
        super(repository, object);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void setScope(final Scriptable scope)
    {
        this.scope = scope;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getPaths()
    {
        final String[] paths = this.getPathsImpl();
        return Context.getCurrentContext().newArray(this.scope, paths);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getParents()
    {
        final ScriptCMISObject[] parents = this.getParentsImpl();
        return Context.getCurrentContext().newArray(this.scope, parents);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren(final boolean files, final boolean folders, final int offset, final int max, final String... ignoreTypes)
    {
        final ScriptCMISObject[] children = this.getChildrenImpl(files, folders, offset, max, ignoreTypes);
        return Context.getCurrentContext().newArray(this.scope, children);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren(final boolean files, final boolean folders, final int offset, final int max,
            final ScriptCMISOperationContext context, final String... ignoreTypes)
    {
        final ScriptCMISObject[] children = this.getChildrenImpl(files, folders, offset, max, context, ignoreTypes);
        return Context.getCurrentContext().newArray(this.scope, children);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAspects()
    {
        final String[] aspects = this.getAspectsImpl();
        return Context.getCurrentContext().newArray(this.scope, aspects);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getPermissions()
    {
        final String[] permissions = this.getPermissionsImpl(false);
        return Context.getCurrentContext().newArray(this.scope, permissions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getDirectPermissions()
    {
        final String[] permissions = this.getPermissionsImpl(true);
        return Context.getCurrentContext().newArray(this.scope, permissions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptCMISObject toScriptCMISObject(final CmisObject object)
    {
        return new RhinoCMISObjectImpl(this.repository, object);
    }

}
