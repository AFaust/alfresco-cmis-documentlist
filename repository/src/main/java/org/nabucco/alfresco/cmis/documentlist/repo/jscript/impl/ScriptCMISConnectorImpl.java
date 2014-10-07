package org.nabucco.alfresco.cmis.documentlist.repo.jscript.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.util.Pair;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.client.util.OperationContextUtils;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISConnector;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISOperationContext;
import org.nabucco.alfresco.cmis.documentlist.repo.jscript.ScriptCMISRepository;
import org.nabucco.alfresco.cmis.documentlist.repo.web.scripts.CMISDocumentListGet.CertificateNotValidatingAuthenticationProvider;

public class ScriptCMISConnectorImpl implements ScriptCMISConnector
{

    //TODO refactor to get cleaner code
    static
    {
        DefaultTypeConverter.INSTANCE.addConverter(BaseCMISOperationContext.class, OperationContext.class,
                new TypeConverter.Converter<BaseCMISOperationContext, OperationContext>()
                {
                    public OperationContext convert(BaseCMISOperationContext sourceContext)
                    {
                        OperationContext operationContext = OperationContextUtils.createOperationContext();
                        operationContext.setCacheEnabled(true);
                        operationContext.setOrderBy(getCMISOrderByClause(sourceContext));

                        // TODO Add other parametes
                        // operationContext.setMaxItemsPerPage(ctx.getMaxItems);
                        operationContext.setFilterString(null);
                        operationContext.setRenditionFilterString("*");
                        operationContext.setIncludePathSegments(true);

                        // TODO: need to be included once
                        operationContext.setIncludeAcls(false);
                        operationContext.setIncludeAllowableActions(false);
                        operationContext.setIncludePolicies(false);
                        operationContext.setIncludeRelationships(IncludeRelationships.NONE);
                        operationContext.setLoadSecondaryTypeProperties(true);
                            
                        return operationContext;
                    }

                    private String getCMISOrderByClause(BaseCMISOperationContext context)
                    {
                        StringBuilder sb = new StringBuilder();
                        int position = 0; // keep track of the position in order to put the commas
                        String way = ""; // ASC or DESC
                        for (Pair<String, Boolean> sort : context.getSortsImpl())
                        {
                            way = sort.getSecond() ? "ASC" : "DESC";
                            // append "," if it's not the first item 
                            if (position > 0)
                            {
                                sb.append(",");
                            }
                            // Produce a string as: "cm:name ASC"
                            sb.append(sort.getFirst());
                            sb.append(" ");
                            sb.append(way);
                            position++;
                        }

                        return sb.toString();
                    }
                });
    }

    @Override
    public ScriptCMISOperationContext newContext()
    {
        ScriptCMISOperationContext context = new RhinoCMISOperationContext();
        return context;
    }

    @Override
    public ScriptCMISRepository connect(String serverId)
    {
        return connect(serverId, newContext());
    }

    @Override
    public ScriptCMISRepository connect(String serverId, ScriptCMISOperationContext ctx)
    {
        // TODO GET the information form configuration file
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SessionParameter.USER, "admin");
        parameters.put(SessionParameter.PASSWORD, "admin");
        parameters.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, CertificateNotValidatingAuthenticationProvider.class.getName());
        parameters.put(SessionParameter.BROWSER_URL, "http://cmis.alfresco.com/cmisbrowser");
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());

        final SessionFactoryImpl sessionFactory = SessionFactoryImpl.newInstance();
        final List<Repository> repositories = sessionFactory.getRepositories(parameters);
        final Repository mainRepository = repositories.get(0);
        parameters.put(SessionParameter.REPOSITORY_ID, mainRepository.getId());

        final Session session = sessionFactory.createSession(parameters);        
        session.setDefaultContext(DefaultTypeConverter.INSTANCE.convert(OperationContext.class, ctx));
        
        ScriptCMISRepositoryImpl scriptCMISRepository = new ScriptCMISRepositoryImpl();
        scriptCMISRepository.setCMISSession(session);        
        return scriptCMISRepository;
    }
}
