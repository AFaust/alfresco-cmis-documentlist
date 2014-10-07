package org.nabucco.alfresco.cmis.documentlist.repo.jscript.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.util.Pair;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISOperationContext;

public abstract class BaseCMISOperationContext implements ScriptCMISOperationContext
{
    protected List<Pair<String, Boolean>> sorts = null;

    protected List<Pair<String, Boolean>> getSortsImpl()
    {
        return this.sorts != null ? new ArrayList<Pair<String, Boolean>>(this.sorts) : Collections.<Pair<String, Boolean>>emptyList();
    }

    @Override
    public void addSort(final String field, final boolean ascending)
    {
        if (null == this.sorts) {
            // Use a lined list in order to keep order of terms in the list
            this.sorts = new LinkedList<Pair<String,Boolean>>();
        }
        this.sorts.add(new Pair<String, Boolean>(field, Boolean.valueOf(ascending)));
    }

}
