package org.nabucco.alfresco.cmis.documentlist.repo.jscript.impl;

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.jscript.Scopeable;
import org.alfresco.repo.jscript.ScriptableHashMap;
import org.alfresco.util.Pair;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class RhinoCMISOperationContext extends BaseCMISOperationContext implements Scopeable
{
    protected Scriptable scope;
    public static String SORT_PROPERTY = "property";
    public static String SORT_ORDER = "order";

    @Override
    public Scriptable getSorts()
    {
        List<Pair<String, Boolean>> sorts = this.getSortsImpl();
        Scriptable[] sortTerms = new Scriptable[sorts.size()];
        int position = 0;
        ScriptableHashMap<String, Serializable> map = null;
        for (Pair<String, Boolean> sort : sorts)
        {
            map = new ScriptableHashMap<String, Serializable>();
            map.put(SORT_PROPERTY, sort.getFirst());
            map.put(SORT_ORDER, sort.getSecond());
            sortTerms[position] = Context.getCurrentContext().newObject(map);
            position++;
        }

        return Context.getCurrentContext().newArray(this.scope, sortTerms);
    }

    @Override
    public void setScope(Scriptable scope)
    {
        this.scope = scope;
    }

}
