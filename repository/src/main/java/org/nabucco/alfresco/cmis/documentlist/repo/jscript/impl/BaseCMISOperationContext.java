package org.nabucco.alfresco.cmis.documentlist.repo.jscript.impl;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.util.Pair;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISOperationContext;

public abstract class BaseCMISOperationContext implements ScriptCMISOperationContext
{
    protected List<Pair<String, Boolean>> sorts = null;
    
    protected List<Pair<String, Boolean>> getSortsImpl()
    {    
        return sorts;
    }

    @Override
    public void addSort(String field, boolean ascending)
    {
        if (null == sorts) {
            // Use a lined list in order to keep order of terms in the list
            sorts = new LinkedList<Pair<String,Boolean>>();
        }
        sorts.add(new Pair<String, Boolean>(field, Boolean.valueOf(ascending)));
    }
    
}
