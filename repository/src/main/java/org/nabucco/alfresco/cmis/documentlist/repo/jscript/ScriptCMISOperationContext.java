package org.nabucco.alfresco.cmis.documentlist.repo.jscript;

/**
 * @author Axel Faust, <a href="http://www.prodyna.com">PRODYNA AG</a>
 */
public interface ScriptCMISOperationContext
{

    Object getSorts();

    void addSort(String field, boolean ascending);

    // TODO add other settings here
}
