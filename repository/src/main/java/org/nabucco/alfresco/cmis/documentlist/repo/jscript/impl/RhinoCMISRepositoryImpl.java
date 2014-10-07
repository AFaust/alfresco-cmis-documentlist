package org.nabucco.alfresco.cmis.documentlist.repo.jscript.impl;

import org.alfresco.util.ParameterCheck;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISObject;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISRepository;

public class RhinoCMISRepositoryImpl implements ScriptCMISRepository
{
    protected final Session session;

    public RhinoCMISRepositoryImpl(final Session session)
    {
        ParameterCheck.mandatory("session", session);
        this.session = session;
    }

    @Override
    public ScriptCMISObject getRootFolder()
    {
        // TODO Auto-generated method stub
        final Folder folder = this.session.getRootFolder();
        final ScriptCMISObject scriptObject = new RhinoCMISObjectImpl(this, folder);
        return scriptObject;
    }

    @Override
    public ScriptCMISObject getObjectByPath(final String path)
    {
        final CmisObject object = this.session.getObjectByPath(path);
        final ScriptCMISObject scriptObject = new RhinoCMISObjectImpl(this, object);
        return scriptObject;
    }

    @Override
    public ScriptCMISObject getObjectById(final String id)
    {
        final CmisObject object = this.session.getObject(id);
        final ScriptCMISObject scriptObject = new RhinoCMISObjectImpl(this, object);
        return scriptObject;
    }
}
