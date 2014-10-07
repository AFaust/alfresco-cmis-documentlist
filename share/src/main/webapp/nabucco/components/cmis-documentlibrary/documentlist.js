if (NABUCCO === undefined || !NABUCCO)
{
    var NABUCCO = {};
}

(function()
{

    NABUCCO.component = NABUCCO.component || {};

    NABUCCO.component.CMISDocumentList = function(htmlId)
    {
        // replace Bubbling.on with NO-OP, so the superclass can't register its event listeners (never-ever)
        var on = YAHOO.Bubbling.on;

        YAHOO.Bubbling.on = function()
        {
            // NO-OP
            return;
        };

        try
        {
            NABUCCO.component.CMISDocumentList.superclass.constructor.call(this, htmlId);
            // restore
            YAHOO.Bubbling.on = on;
        }
        catch (e)
        {
            // restore
            YAHOO.Bubbling.on = on;
            throw e;
        }

        this.name = "NABUCCO.component.CMISDocumentList";
        Alfresco.util.ComponentManager.reregister(this);
        
        this.dataSourceUrl = Alfresco.constants.URL_SERVICECONTEXT + 'nabucco/components/cmis-documentlist/data?';

        if (htmlId !== "null")
        {
            // we actually want to react to metadataRefresh
            YAHOO.Bubbling.on("metadataRefresh", this.onDocListRefresh, this);
            YAHOO.Bubbling.on("filterChanged", this.onFilterChanged, this);
            YAHOO.Bubbling.on("changeFilter", this.onChangeFilter, this);
        }

        this.dragAndDropAllowed = false;

        this.setOptions(
        {
            preferencePrefix : "org.nabucco.cmis-documentlibrary"
        });
    };

    YAHOO.extend(NABUCCO.component.CMISDocumentList, Alfresco.DocumentList,
    {
        onSortAscending : function()
        {
            NABUCCO.component.CMISDocumentList.withPreferencePrefixOverride.call(this,
                    NABUCCO.component.CMISDocumentList.superclass.onSortAscending, arguments);
        },

        onSortField : function()
        {
            NABUCCO.component.CMISDocumentList.withPreferencePrefixOverride.call(this,
                    NABUCCO.component.CMISDocumentList.superclass.onSortField, arguments);
        },

        onShowFolders : function()
        {
            NABUCCO.component.CMISDocumentList.withPreferencePrefixOverride.call(this,
                    NABUCCO.component.CMISDocumentList.superclass.onShowFolders, arguments);
        },

        onViewRendererSelect : function()
        {
            NABUCCO.component.CMISDocumentList.withPreferencePrefixOverride.call(this,
                    NABUCCO.component.CMISDocumentList.superclass.onViewRendererSelect, arguments);
        },

        onSimpleDetailed : function()
        {
            NABUCCO.component.CMISDocumentList.withPreferencePrefixOverride.call(this,
                    NABUCCO.component.CMISDocumentList.superclass.onSimpleDetailed, arguments);
        },

        _buildDocListParams : function(p_obj)
        {
            var params = "", obj =
            {
                path : this.currentPath
            };

            // Pagination in use?
            if (this.options.usePagination)
            {
                obj.page = this.widgets.paginator.getCurrentPage() || this.currentPage;
                obj.pageSize = this.widgets.paginator.getRowsPerPage();
            }

            // Passed-in overrides
            if (typeof p_obj === "object")
            {
                obj = YAHOO.lang.merge(obj, p_obj);
            }

            params = "path=" + obj.path;

            // Paging parameters
            if (this.options.usePagination)
            {
                params += "&pageSize=" + obj.pageSize + "&pos=" + obj.page;
            }

            // Sort parameters
            params += "&sortAsc=" + this.options.sortAscending + "&sortField=" + encodeURIComponent(this.options.sortField);

            // View mode and No-cache
            params += "&view=" + this.actionsView + "&noCache=" + new Date().getTime();

            return params;
        }
    });

    NABUCCO.component.CMISDocumentList.withPreferencePrefixOverride = function(callback, args)
    {
        var prefSet, result, scope = this;
        if (YAHOO.lang.isString(this.options.preferencePrefix) && this.options.preferencePrefix !== "org.alfresco.share.documentList")
        {
            prefSet = this.services.preferences.set;
            this.services.preferences.set = function(prefKey, value, responseConfig)
            {
                prefKey = prefKey.replace("org.alfresco.share.documentList.", scope.options.preferencePrefix + '.');
                return prefSet.call(this, prefKey, value, responseConfig);
            };
            try
            {
                result = callback.apply(this, args);
                this.services.preferences.set = prefSet;
            }
            catch (e)
            {
                this.services.preferences.set = prefSet;
                throw e;
            }

            return result;
        }
        return callback.apply(this, args);
    };

    // necessary to fix default thumbnail icons for non-standard node types, especially non-file-folder types
    NABUCCO.component.CMISDocumentList.withFileIconOverride = function(callback, args)
    {
        var getFileIcon = Alfresco.util.getFileIcon, node = args[1].getData().jsNode, result;
        Alfresco.util.getFileIcon = function(p_fileName, p_fileType, p_iconSize, p_fileParentType)
        {
            if (p_fileType === undefined)
            {
                if (node.isLink && YAHOO.lang.isObject(node.linkedNode) && YAHOO.lang.isString(node.linkedNode.type))
                {
                    p_fileType = node.linkedNode.type;
                }
                else
                {
                    p_fileType = node.type;
                }
            }

            return getFileIcon.call(Alfresco.util, p_fileName, p_fileType, p_iconSize, p_fileParentType);
        };
        Alfresco.util.getFileIcon.types = getFileIcon.types;
        try
        {
            result = callback.apply(this, args);
            Alfresco.util.getFileIcon = getFileIcon;
        }
        catch (e)
        {
            Alfresco.util.getFileIcon = getFileIcon;
            throw e;
        }
        return result;
    };

    // necessary to fix thumbnail URL generation to avoid HTTP 400 responses for attempts on items without content
    NABUCCO.component.CMISDocumentList.withThumbnailOverride = function(callback, args)
    {
        var generateThumbnailUrl = Alfresco.DocumentList.generateThumbnailUrl, result;
        Alfresco.DocumentList.generateThumbnailUrl = function(record)
        {
            var node = record.jsNode;
            if ((node.isContent || (node.isLink && node.linkedNode.isContent))
                    && (YAHOO.lang.isString(node.contentURL) || (node.isLink && YAHOO.lang.isString(node.linkedNode.contentURL))))
            {
                return generateThumbnailUrl(record);
            }
            return Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util.getFileIcon(record.displayName);
        };
        try
        {
            result = callback.apply(this, args);
            Alfresco.DocumentList.generateThumbnailUrl = generateThumbnailUrl;
        }
        catch (e)
        {
            Alfresco.DocumentList.generateThumbnailUrl = generateThumbnailUrl;
            throw e;
        }

        return result;
    };

    // adapt the document list fnRenderCellThumbnail to remove preview when no preview can be generated (node without content) and use
    // information available for file icon determination
    Alfresco.DocumentList.prototype._nbc_fnRenderCellThumbnail = Alfresco.DocumentList.prototype.fnRenderCellThumbnail;
    Alfresco.DocumentList.prototype.fnRenderCellThumbnail = function(renderChain)
    {
        var scope = this, realRenderer = this._nbc_fnRenderCellThumbnail(), renderCallback = renderChain;

        return function(elCell, oRecord, oColumn, oData)
        {
            var id, node = oRecord.getData().jsNode;

            NABUCCO.component.CMISDocumentList.withFileIconOverride.call(this, function()
            {
                NABUCCO.component.CMISDocumentList.withThumbnailOverride.call(this, function()
                {
                    if (YAHOO.lang.isFunction(renderCallback))
                    {
                        renderCallback.call(this, realRenderer, arguments);
                    }
                    else
                    {
                        realRenderer.apply(this, arguments);
                    }
                }, arguments);
            }, [ elCell, oRecord, oColumn, oData ]);

            // OOTB view renderer always prepare preview even if node has no content
            if (!(node.isContainer || (node.isLink && node.linkedNode.isContainer))
                    && !(YAHOO.lang.isString(node.contentURL) || (node.isLink && YAHOO.lang.isString(node.linkedNode.contentURL))))
            {
                // check for any thumbnails that are not supported due to node without content
                id = scope.id + '-preview-' + oRecord.getId();
                if (Alfresco.util.arrayContains(scope.previewTooltips, id))
                {
                    scope.previewTooltips = Alfresco.util.arrayRemove(scope.previewTooltips, id);
                }
            }
        };
    };

    // adapt size renderer for items without content as well as links
    Alfresco.DocumentList.prototype._nbc_setupMetadataRenderers = Alfresco.DocumentList.prototype._setupMetadataRenderers;
    Alfresco.DocumentList.prototype._setupMetadataRenderers = function()
    {
        this._nbc_setupMetadataRenderers();

        /**
         * File size
         */
        this.registerRenderer("size", function(record, label)
        {
            var jsNode = record.jsNode, html = "";

            if ((YAHOO.lang.isString(jsNode.contentURL) || YAHOO.lang.isNumber(jsNode.size)) || (jsNode.isLink && (YAHOO.lang.isString(jsNode.linkedNode.contentURL) || YAHOO.lang.isNumber(jsNode.linkedNode.size))))
            {
                html += '<span class="item">' + label
                        + Alfresco.util.formatFileSize(YAHOO.lang.isString(jsNode.contentURL) || YAHOO.lang.isNumber(jsNode.size) ? jsNode.size : jsNode.linkedNode.size)
                        + '</span>';
            }

            return html;
        });
    };

    (function()
    {
        // additional properties for jsNode
        var additionalJsNodeProps = [ "isContent" ];

        // adapt Node to support our additional properties
        Alfresco.util._nbc_Node = Alfresco.util.Node;
        Alfresco.util.Node = function(p_node)
        {
            var jsNode = Alfresco.util._nbc_Node(p_node), idx, propName;

            if (YAHOO.lang.isObject(jsNode))
            {
                for (idx = 0; idx < additionalJsNodeProps.length; idx++)
                {
                    propName = additionalJsNodeProps[idx];
                    // override only if no such property has been defined yet
                    if (p_node.hasOwnProperty(propName) && !jsNode.hasOwnProperty(propName))
                    {
                        if (propName.indexOf("Node") !== -1 && propName.substr(propName.indexOf("Node")) === "Node"
                                && YAHOO.lang.isString(p_node[propName]))
                        {
                            jsNode[propName] = new Alfresco.util.NodeRef(p_node[propName]);
                        }
                        else
                        {
                            jsNode[propName] = p_node[propName];
                        }
                    }
                }
            }

            return jsNode;
        };
    }());
    
    Alfresco.util.getFileIcon.types["D:cmiscustom:document"] = "file";
    Alfresco.util.getFileIcon.types["cmis:document"] = "file";
    Alfresco.util.getFileIcon.types["cmis:folder"] = "folder";
}());