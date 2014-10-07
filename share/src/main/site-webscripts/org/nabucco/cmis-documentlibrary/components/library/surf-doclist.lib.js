<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/documentlibrary/data/surf-doclist.lib.js">

// replace the resolver
DocList_Custom = (function()
{
    var doclistActionGroupResolver = null, doclistDataUrlResolver = null, result;

    result = {
        /**
         * Overridable function to calculate correct action group based on node details. Its also possible to instead override the
         * "resolver.doclib.actionGroupResolver" bean OR to change the resolver bean by configuring "DocLibActions actionGroupResolver".
         * 
         * @method calculateActionGroupId
         * @param item
         *            {Object} Record object representing a node, as returned from data webscript
         * @param view
         *            {String} Current page view, currently "details" or "browse"
         * @param itemJSON
         *            {String} item object as a json string
         * @return {String} Action Group Id
         */
        calculateActionGroupId : function(item, view, itemJSON)
        {
            // Default group calculation
            if (!doclistActionGroupResolver)
            {
                if (config.scoped["CMISDocLibActions"]["actionGroupResolver"])
                {
                    doclistActionGroupResolver = resolverHelper
                            .getDoclistActionGroupResolver(config.scoped["CMISDocLibActions"]["actionGroupResolver"].value);
                }
                else
                {
                    // return default docLib configuration
                    doclistActionGroupResolver = resolverHelper
                            .getDoclistActionGroupResolver(config.scoped["DocLibActions"]["actionGroupResolver"].value);
                }
            }
            return ("" + doclistActionGroupResolver.resolve(itemJSON, view));
        },

        /**
         * Overridable function to calculate the remote data URL. The returned URL will be used as the parameter for remote.call() Its also
         * possible to instead override the "resolver.doclib.doclistDataUrl" bean OR to change the resolver bean by configuring
         * "DocumentLibrary doclist data-url-resolver".
         * 
         * @method calculateRemoteDataURL
         * @return {String} Remote Data URL
         */
        calculateRemoteDataURL : function()
        {
            var filledArgs = "?";

            if (args.sortAsc !== undefined && args.sortAsc !== null)
            {
                filledArgs += "sortAsc=" + encodeURIComponent(args.sortAsc) + "&";
            }

            if (args.sortField !== undefined && args.sortField !== null)
            {
                filledArgs += "sortField=" + encodeURIComponent(args.sortField) + "&";
            }

            if (args.path !== undefined && args.path !== null)
            {
                filledArgs += "path=" + encodeURIComponent(args.path) + "&";
            }

            if (args.pageSize !== undefined && args.pageSize !== null)
            {
                filledArgs += "maxItems=" + encodeURIComponent(args.pageSize) + "&";
            }

            if (args.pos !== undefined && args.pos !== null && args.pageSize !== undefined && args.pageSize !== null)
            {
                filledArgs += "skipCount=" + encodeURIComponent((parseInt(args.pos, 10) - 1) * parseInt(args.pageSize, 10)) + "&";
            }

            return "/api/nabucco/cmis/doclist?" + filledArgs.substr(0, filledArgs.length - 1);
        }
    };
    
    return result;
}());

DocList.defaultGetGroupActions = DocList.getGroupActions;
DocList.getGroupActions = function(groupId, allActions)
{
    var _config = config, actions, actionGroupExists = false, actionId;
    // replace config with a dummy forcing our custom config section as default (if configured)
    config =
    {
        scoped :
        {
            DocLibActions :
            {
                actionGroups : config.scoped["CMISDocLibActions"]["actionGroups"]
            }
        }
    };

    actions = DocList.defaultGetGroupActions(groupId, allActions);

    // reset global config object
    config = _config;

    for (actionId in actions)
    {
        actionGroupExists = true;
        break;
    }

    // in case we don't have a search specific config perform a lookup using the global config
    if (!actionGroupExists)
    {
        actions = DocList.defaultGetGroupActions(groupId, allActions);
    }

    return actions;
};

DocList.defaultGetAllActions = DocList.getAllActions;
DocList.getAllActions = function()
{
    var _config = config, specificActions, globalActions, actions = {}, actionId;

    // replace config with a dummy forcing our custom config section as default (if configured)
    config =
    {
        scoped :
        {
            DocLibActions :
            {
                actions : config.scoped["CMISDocLibActions"]["actions"]
            }
        }
    };

    // get specific actions config
    specificActions = DocList.defaultGetAllActions();

    // reset global config
    config = _config;

    // get standard actions config
    globalActions = DocList.defaultGetAllActions();

    for (actionId in globalActions)
    {
        actions[actionId] = globalActions[actionId];
    }

    for (actionId in specificActions)
    {
        // merge, giving precedence to specific configuration
        actions[actionId] = DocList.merge(actions[actionId] || {}, specificActions[actionId]);
    }

    return actions;
};

DocList.defaultGetAllIndicators = DocList.getAllIndicators;
DocList.getAllIndicators = function()
{
    var _config = config, specificIndicators, globalIndicators, indicators = {}, indicatorId;

    // replace config with a dummy forcing our custom config section as default (if configured)
    config =
    {
        scoped :
        {
            DocumentLibrary :
            {
                indicators : config.scoped["CMISDocLib"]["indicators"]
            }
        }
    };

    // get specific indicators config
    specificIndicators = DocList.defaultGetAllIndicators();

    // reset global config
    config = _config;

    // get standard indicators config
    globalIndicators = DocList.defaultGetAllIndicators();

    for (indicatorId in globalIndicators)
    {
        indicators[indicatorId] = globalIndicators[indicatorId];
    }

    for (indicatorId in specificIndicators)
    {
        // merge, giving precedence to specific configuration
        indicators[indicatorId] = DocList.merge(indicators[indicatorId] || {}, specificIndicators[indicatorId]);
    }

    return indicators;
};

DocList.defaultGetAllMetadataTemplates = DocList.getAllMetadataTemplates;
DocList.getAllMetadataTemplates = function()
{
    var _config = config, specificTemplates, globalTemplates, templates = {}, templateId, template;

    // replace config with a dummy forcing our custom config section as default (if configured)
    config =
    {
        scoped :
        {
            DocumentLibrary : {

            }
        }
    };

    config.scoped["DocumentLibrary"]["metadata-templates"] = _config.scoped["CMISDocLib"]["metadata-templates"];

    // get specific templates config
    specificTemplates = DocList.defaultGetAllMetadataTemplates();

    // reset global config
    config = _config;

    // get standard indicators config
    globalTemplates = DocList.defaultGetAllMetadataTemplates();

    for (templateId in globalTemplates)
    {
        templates[templateId] = globalTemplates[templateId];
    }

    for (templateId in specificTemplates)
    {
        // merge, giving precedence to specific configuration
        // but templates need to be merged more carefully to preserve sub-structures
        if (templates[templateId] !== undefined)
        {
            template = DocList.merge({}, templates[templateId] || specificTemplates[templateId]);
            if (specificTemplates[templateId].evaluators !== undefined)
            {
                template.evaluators = specificTemplates[templateId].evaluators;
            }
            template.title = specificTemplates[templateId].title !== undefined && specificTemplates[templateId].title !== null ? specificTemplates[templateId].title
                    : template.title;
            template.banners = DocList.merge(template.banners || {}, specificTemplates[templateId].banners || {});
            template.lines = DocList.merge(template.lines || {}, specificTemplates[templateId].lines || {});
            templates[templateId] = template;
        }
        else
        {
            templates[templateId] = specificTemplates[templateId];
        }
    }

    return templates;
};

var standardMain = surfDoclist_main;
surfDoclist_main = function()
{
    /*
     * Note: we call once to deal with errors correctly and then delegate to the standard function, relying on a caching remote connector to
     * not call the backend again
     */
    var json, dataUrl = DocList_Custom.calculateRemoteDataURL(), result = remote.call(dataUrl);

    if (result.status.code === status.STATUS_OK)
    {
        standardMain();
    }
    else
    {
        json = jsonUtils.toObject(result);
        if (json && json.message)
        {
            status.setCode(result.status, json.message);
        }
        else
        {
            status.setCode(result.status);
        }
    }
};