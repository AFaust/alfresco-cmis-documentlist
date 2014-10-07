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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.alfresco.repo.jscript.NativeMap;
import org.alfresco.repo.jscript.Scopeable;
import org.alfresco.util.ParameterCheck;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
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

    public Scriptable getProperties()
    {
        final Map<Object, Object> propertiesImpl = this.getPropertiesImpl();
        return new NativeMap(this.scope, propertiesImpl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Scriptable getPaths()
    {
        final String[] paths = this.getPathsImpl();
        final Object[] pathObj = new Object[paths.length];
        System.arraycopy(paths, 0, pathObj, 0, paths.length);
        return Context.getCurrentContext().newArray(this.scope, pathObj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Scriptable getParents()
    {
        final ScriptCMISObject[] parents = this.getParentsImpl();
        final Object[] parentsObj = new Object[parents.length];
        System.arraycopy(parents, 0, parentsObj, 0, parents.length);
        return Context.getCurrentContext().newArray(this.scope, parentsObj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Scriptable getChildren(final boolean files, final boolean folders, final int offset, final int max, final String... ignoreTypes)
    {
        final ScriptCMISObject[] children = this.getChildrenImpl(files, folders, offset, max, ignoreTypes);
        final Object[] childrenObj = new Object[children.length];
        System.arraycopy(children, 0, childrenObj, 0, children.length);
        return Context.getCurrentContext().newArray(this.scope, childrenObj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Scriptable getChildren(final boolean files, final boolean folders, final int offset, final int max,
            final ScriptCMISOperationContext context, final String... ignoreTypes)
    {
        final ScriptCMISObject[] children = this.getChildrenImpl(files, folders, offset, max, context, ignoreTypes);
        final Object[] childrenObj = new Object[children.length];
        System.arraycopy(children, 0, childrenObj, 0, children.length);
        return Context.getCurrentContext().newArray(this.scope, childrenObj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Scriptable getAspects()
    {
        final String[] aspects = this.getAspectsImpl();
        final Object[] aspectsObj = new Object[aspects.length];
        System.arraycopy(aspects, 0, aspectsObj, 0, aspects.length);
        return Context.getCurrentContext().newArray(this.scope, aspectsObj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Scriptable getPermissions()
    {
        final String[] permissions = this.getPermissionsImpl(false);
        final Object[] permissionsObj = new Object[permissions.length];
        System.arraycopy(permissions, 0, permissionsObj, 0, permissions.length);
        return Context.getCurrentContext().newArray(this.scope, permissionsObj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Scriptable getDirectPermissions()
    {
        final String[] permissions = this.getPermissionsImpl(true);
        final Object[] permissionsObj = new Object[permissions.length];
        System.arraycopy(permissions, 0, permissionsObj, 0, permissions.length);
        return Context.getCurrentContext().newArray(this.scope, permissionsObj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptCMISObject toScriptCMISObject(final CmisObject object)
    {
        return new RhinoCMISObjectImpl(this.repository, object);
    }

    @SuppressWarnings("fallthrough")
    protected Object toScriptValue(final Object value, final PropertyDefinition<?> definition)
    {
        ParameterCheck.mandatory("value", value);
        ParameterCheck.mandatory("definition", definition);

        final Object result;

        switch (definition.getPropertyType())
        {
        case DATETIME:
            if (!(value instanceof GregorianCalendar))
                throw new IllegalArgumentException("CMIS date time should by definition be GregorionCalendar");
            final Date actualDate = ((GregorianCalendar) value).getTime();

            result = ScriptRuntime.newObject(Context.getCurrentContext(), this.scope, "Date",
                    new Object[] { Long.valueOf(actualDate.getTime()) });

            break;
        case INTEGER:
            if (!(value instanceof BigInteger))
                throw new IllegalArgumentException("CMIS integer should by definition be BigInteger");
            result = Long.valueOf(((BigInteger) value).longValue());
            break;
        case DECIMAL:
            if (!(value instanceof BigDecimal))
                throw new IllegalArgumentException("CMIS decimal should by definition be BigDecimal");
            result = Double.valueOf(((BigInteger) value).doubleValue());
            break;
        default:
            result = value;
        }

        return result;
    }
}
