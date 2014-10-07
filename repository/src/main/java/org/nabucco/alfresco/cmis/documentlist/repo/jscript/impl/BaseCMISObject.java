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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.util.ParameterCheck;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Item;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISContent;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISObject;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISOperationContext;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISRepository;

/**
 * @author Axel Faust, <a href="http://www.prodyna.com">PRODYNA AG</a>
 */
public abstract class BaseCMISObject implements ScriptCMISObject
{

    protected CmisObject object;

    protected final ScriptCMISRepository repository;

    public BaseCMISObject(final ScriptCMISRepository repository, final CmisObject object)
    {
        ParameterCheck.mandatory("repository", repository);
        ParameterCheck.mandatory("object", object);

        this.repository = repository;
        this.object = object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getObjectId()
    {
        return this.object.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isContainer()
    {
        return this.object instanceof Folder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isItem()
    {
        return this.object instanceof Item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDocument()
    {
        return this.object instanceof Document;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath()
    {
        final String result;

        final String[] paths = this.getPathsImpl();

        result = paths.length > 0 ? paths[0] : null;

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptCMISObject getParent()
    {
        final ScriptCMISObject result;

        // Note: We could use FileableCmisObject.getParents() but want to ensure getPath() always lines up with getParent()
        // Also this way might load less data for multi-filed objects
        final String path = this.getPath();

        if (path != null && path.matches("(/[^/]+)+"))
        {
            final String realPath = path.substring(0, path.lastIndexOf('/'));
            if (realPath.length() == 0)
            {
                result = this.repository.getRootFolder();
            }
            else
            {
                result = this.repository.getObjectByPath(path);
            }
        }
        else
        {
            result = null;
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren()
    {
        return this.getChildren(true, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren(final ScriptCMISOperationContext context)
    {
        return this.getChildren(true, true, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren(final boolean files, final boolean folders)
    {
        return this.getChildren(files, folders, new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren(final boolean files, final boolean folders, final ScriptCMISOperationContext context)
    {
        return this.getChildren(files, folders, context, new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren(final boolean files, final boolean folders, final String... ignoreTypes)
    {
        return this.getChildren(true, true, 0, Integer.MAX_VALUE, new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren(final boolean files, final boolean folders, final ScriptCMISOperationContext context,
            final String... ignoreTypes)
    {
        return this.getChildren(true, true, 0, Integer.MAX_VALUE, context, new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren(final int offset, final int max)
    {
        return this.getChildren(true, true, offset, max, new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren(final int offset, final int max, final ScriptCMISOperationContext context)
    {
        return this.getChildren(true, true, offset, max, context, new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren(final int offset, final int max, final String... ignoreTypes)
    {
        return this.getChildren(true, true, offset, max, ignoreTypes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren(final int offset, final int max, final ScriptCMISOperationContext context, final String... ignoreTypes)
    {
        return this.getChildren(true, true, offset, max, context, ignoreTypes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren(final boolean files, final boolean folders, final int offset, final int max)
    {
        return this.getChildren(files, folders, offset, max, new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChildren(final boolean files, final boolean folders, final int offset, final int max,
            final ScriptCMISOperationContext context)
    {
        return this.getChildren(files, folders, offset, max, context, new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperties()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getDecoratedProperties()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType()
    {
        return this.object.getType().getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSubType(final String type)
    {
        ParameterCheck.mandatoryString("type", type);

        boolean isSubType = false;

        BaseTypeId baseType = null;
        for (final BaseTypeId baseTypeCandidate : BaseTypeId.values())
        {
            if (baseTypeCandidate.value().equals(type))
            {
                baseType = baseTypeCandidate;
                break;
            }
        }

        if (baseType != null && this.object.getBaseTypeId() == baseType)
        {
            isSubType = true;
        }
        else
        {
            ObjectType currentType = this.object.getType();
            while (currentType != null && !currentType.isBaseType() && !isSubType)
            {
                if (type.equals(currentType.getId()))
                {
                    isSubType = true;
                    break;
                }

                currentType = currentType.getParentType();
            }
        }

        return isSubType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAspect(final String type)
    {
        ParameterCheck.mandatoryString("type", type);

        boolean hasAspect = false;

        final List<SecondaryType> secondaryTypes = this.object.getSecondaryTypes();
        final Iterator<SecondaryType> secondaryTypeIterator = secondaryTypes.iterator();
        while (secondaryTypeIterator.hasNext() && !hasAspect)
        {
            ObjectType currentType = secondaryTypeIterator.next();
            while (currentType != null && !currentType.isBaseType() && !hasAspect)
            {
                if (type.equals(currentType.getId()))
                {
                    hasAspect = true;
                    break;
                }

                currentType = currentType.getParentType();
            }
        }

        return hasAspect;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPermission(final String permission)
    {
        // TODO Check both CMIS permission/allowable action and fallback to mapping from Alfresco base permissions to allowable action
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptCMISContent getContent()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDownloadUrl()
    {
        // TODO Auto-generated method stub
        return null;
    }

    protected String[] getPathsImpl()
    {
        final String[] result;

        if (this.object instanceof FileableCmisObject)
        {
            result = ((FileableCmisObject) this.object).getPaths().toArray(new String[0]);
        }
        else
        {
            result = new String[0];
        }

        return result;
    }

    protected ScriptCMISObject[] getParentsImpl()
    {
        final ScriptCMISObject[] result;

        if (this.object instanceof FileableCmisObject)
        {
            final List<Folder> parents = ((FileableCmisObject) this.object).getParents();

            result = new ScriptCMISObject[parents.size()];
            int idx = 0;
            for (final Folder parent : parents)
            {
                result[idx++] = this.toScriptCMISObject(parent);
            }
        }
        else
        {
            result = new ScriptCMISObject[0];
        }

        return result;
    }

    protected ScriptCMISObject[] getChildrenImpl(final boolean files, final boolean folders, final int offset, final int max,
            final String... ignoreTypes)
    {
        final ScriptCMISObject[] result;
        if (this.isContainer())
        {
            if (files && folders && (ignoreTypes == null || ignoreTypes.length == 0))
            {
                final ItemIterable<CmisObject> children = ((Folder) this.object).getChildren();
                final Iterator<CmisObject> iterator = children.skipTo(offset).getPage(max).iterator();

                final List<ScriptCMISObject> cmisChildren = new ArrayList<ScriptCMISObject>();
                while (iterator.hasNext())
                {
                    final CmisObject object = iterator.next();
                    cmisChildren.add(this.toScriptCMISObject(object));
                }

                result = cmisChildren.toArray(new ScriptCMISObject[0]);
            }
            else
            {
                // TODO implement using CMIS Query
                result = new ScriptCMISObject[0];
            }
        }
        else
        {
            throw new UnsupportedOperationException(
                    "CMIS object is not a container and can't have children - always check isContainer first");
        }

        return result;
    }

    protected ScriptCMISObject[] getChildrenImpl(final boolean files, final boolean folders, final int offset, final int max,
            final ScriptCMISOperationContext context, final String... ignoreTypes)
    {
        final ScriptCMISObject[] result;
        if (this.isContainer())
        {
            final OperationContext openCMISContext = DefaultTypeConverter.INSTANCE.convert(OperationContext.class, context);
            if (files && folders && (ignoreTypes == null || ignoreTypes.length == 0))
            {
                final ItemIterable<CmisObject> children = ((Folder) this.object).getChildren(openCMISContext);
                final Iterator<CmisObject> iterator = children.skipTo(offset).getPage(max).iterator();

                final List<ScriptCMISObject> cmisChildren = new ArrayList<ScriptCMISObject>();
                while (iterator.hasNext())
                {
                    final CmisObject object = iterator.next();
                    cmisChildren.add(this.toScriptCMISObject(object));
                }

                result = cmisChildren.toArray(new ScriptCMISObject[0]);
            }
            else
            {
                // TODO implement using CMIS Query
                result = new ScriptCMISObject[0];
            }
        }
        else
        {
            throw new UnsupportedOperationException(
                    "CMIS object is not a container and can't have children - always check isContainer first");
        }

        return result;
    }

    protected String[] getAspectsImpl()
    {
        final String[] aspects;

        final List<SecondaryType> secondaryTypes = this.object.getSecondaryTypes();
        aspects = new String[secondaryTypes.size()];

        int idx = 0;
        for (final SecondaryType secondaryType : secondaryTypes)
        {
            aspects[idx++] = secondaryType.getId();
        }

        return aspects;
    }

    protected String[] getPermissionsImpl(final boolean directOnly)
    {
        final String[] result;

        final Acl acl = this.object.getAcl();
        final List<Ace> aces = acl.getAces();

        final List<String> permissions = new ArrayList<String>();

        for (final Ace ace : aces)
        {
            if (ace.isDirect() || !directOnly)
            {
                for (final String permission : ace.getPermissions())
                {
                    final String permissionStr = MessageFormat.format("ALLOWED;{0};{1}", ace.getPrincipalId(), permission);
                    permissions.add(permissionStr);
                }
            }
        }

        result = permissions.toArray(new String[0]);

        return result;
    }

    protected abstract ScriptCMISObject toScriptCMISObject(CmisObject object);
}
