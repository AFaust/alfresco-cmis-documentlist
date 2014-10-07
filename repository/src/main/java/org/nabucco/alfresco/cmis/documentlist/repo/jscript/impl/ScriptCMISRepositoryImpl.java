package org.nabucco.alfresco.cmis.documentlist.repo.jscript.impl;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISObject;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISRepository;

public class ScriptCMISRepositoryImpl implements ScriptCMISRepository
{
    protected Session session;
    
    @Override
    public ScriptCMISObject getRootFolder()
    {
        // TODO Auto-generated method stub
        Folder folder = session.getRootFolder();
        ScriptCMISObjectImpl scriptObject = new ScriptCMISObjectImpl(this, folder);
        scriptObject.toScriptCmisObject(folder);
        return scriptObject;
    }

    @Override
    public ScriptCMISObject getObjectByPath(String path)
    {
        CmisObject object = session.getObjectByPath(path);
        ScriptCMISObjectImpl scriptObject = new ScriptCMISObjectImpl(this, folder);
        scriptObject.toScriptCmisObject(folder);
        return scriptObject;
    }

    @Override
    public ScriptCMISObject getObjectById(String id)
    {
        CmisObject object = session.getObject(id);
        ScriptCMISObjectImpl scriptObject = new ScriptCMISObjectImpl(this, folder);
        scriptObject.toScriptCmisObject(folder);
        return scriptObject;
    }

    public void setCMISSession(Session session) {
        this.session = session;
    }
}
