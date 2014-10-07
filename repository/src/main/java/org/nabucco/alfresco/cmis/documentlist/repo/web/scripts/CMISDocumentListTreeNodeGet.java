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
package org.nabucco.alfresco.cmis.documentlist.repo.web.scripts;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.alfresco.util.ISO8601DateFormat;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Item;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.bindings.spi.StandardAuthenticationProvider;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Axel Faust, <a href="http://www.prodyna.com">PRODYNA AG</a>
 */
public class CMISDocumentListTreeNodeGet extends DeclarativeWebScript
{

    /**
     *
     * @author Axel Faust, <a href="http://www.prodyna.com">PRODYNA AG</a>
     */
    public static class DateFormatter
    {
        public String format(final Date date)
        {
            return ISO8601DateFormat.format(date);
        }
    }

    /**
     *
     * @author Axel Faust, <a href="http://www.prodyna.com">PRODYNA AG</a>
     */
    public static class CertificateNotValidatingAuthenticationProvider extends StandardAuthenticationProvider
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public SSLSocketFactory getSSLSocketFactory()
        {
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
            {
                public java.security.cert.X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }

                public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType)
                {
                }

                public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType)
                {
                }
            } };

            // Install the all-trusting trust manager
            try
            {
                final SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                return sc.getSocketFactory();
            }
            catch (final KeyManagementException ex)
            {
                return null;
            }
            catch (final NoSuchAlgorithmException ex)
            {
                return null;
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache)
    {
        Map<String, Object> model = super.executeImpl(req, status, cache);
        if (model == null)
        {
            model = new HashMap<String, Object>();
        }

        final String pathParam = req.getParameter("path");
        final String maxItemsParam = req.getParameter("max");

        final int maxItems = getIntParam(maxItemsParam, 10);

        final String path = pathParam != null && pathParam.trim().length() != 0 ? pathParam.trim() : "/";

        final SessionFactoryImpl sessionFactory = SessionFactoryImpl.newInstance();

        final Map<String, String> parameters = new HashMap<String, String>();

        parameters.put(SessionParameter.USER, "admin");
        parameters.put(SessionParameter.PASSWORD, "admin");
        parameters.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, CertificateNotValidatingAuthenticationProvider.class.getName());

        parameters.put(SessionParameter.BROWSER_URL, "http://cmis.alfresco.com/cmisbrowser");
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());

        final List<Repository> repositories = sessionFactory.getRepositories(parameters);
        final Repository mainRepository = repositories.get(0);
        parameters.put(SessionParameter.REPOSITORY_ID, mainRepository.getId());

        final Session session = sessionFactory.createSession(parameters);
        this.setupOperationContext(maxItems, session);

        boolean resultsTrimmed = false;
        final List<Object> nodes = new ArrayList<Object>();
        final CmisObject pathObject = session.getObjectByPath(path);
        if (pathObject instanceof Folder)
        {
            final Folder parent = (Folder) pathObject;

            if (Boolean.TRUE.equals(mainRepository.getCapabilities().isGetFolderTreeSupported()))
            {
                final List<Tree<FileableCmisObject>> folderTree = parent.getFolderTree(-1);
                for (final Tree<FileableCmisObject> subTree : folderTree)
                {
                    final FileableCmisObject item = subTree.getItem();

                    final Map<String, Object> nodeModel = new HashMap<String, Object>();
                    nodeModel.put("cmisObject", item);

                    nodeModel.put("isContainer", Boolean.valueOf(item instanceof Folder));
                    nodeModel.put("isItem", Boolean.valueOf(item instanceof Item));

                    nodes.add(nodeModel);

                    if (nodes.size() >= maxItems)
                    {
                        resultsTrimmed = true;
                        break;
                    }
                }
            }
        }

        final Map<String, Object> nodeModel = new HashMap<String, Object>();
        nodeModel.put("cmisObject", pathObject);

        nodeModel.put("isContainer", Boolean.valueOf(pathObject instanceof Folder));
        nodeModel.put("isItem", Boolean.valueOf(pathObject instanceof Item));

        model.put("parent", nodeModel);

        model.put("nodes", nodes);
        model.put("resultsTrimmed", Boolean.valueOf(resultsTrimmed));
        model.put("dateFormatter", new DateFormatter());

        return model;
    }

    protected void setupOperationContext(final int maxItems, final Session session)
    {
        final OperationContext operationContext = session.createOperationContext();
        operationContext.setCacheEnabled(true);

        operationContext.setOrderBy(MessageFormat.format("{0} ASC", PropertyIds.NAME));
        operationContext.setMaxItemsPerPage(maxItems);

        operationContext.setFilterString(null);
        operationContext.setIncludePathSegments(false);

        operationContext.setIncludeAcls(false);
        operationContext.setIncludeAllowableActions(false);
        operationContext.setIncludePolicies(false);

        operationContext.setIncludeRelationships(IncludeRelationships.NONE);
        operationContext.setLoadSecondaryTypeProperties(true);
        session.setDefaultContext(operationContext);
    }

    protected static int getIntParam(final String param, final int defaultValue)
    {
        final int result;
        if (param == null || param.trim().length() == 0)
        {
            result = defaultValue;
        }
        else if (param.trim().matches("\\d+"))
        {
            result = Integer.parseInt(param.trim());
        }
        else
        {
            result = defaultValue;
        }

        return result;
    }
}
