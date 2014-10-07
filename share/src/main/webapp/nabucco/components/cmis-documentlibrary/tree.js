if (NABUCCO === undefined || !NABUCCO)
{
    var NABUCCO = {};
}

(function()
{

    NABUCCO.component = NABUCCO.component || {};

    NABUCCO.component.CMISDocListTree = function(htmlId)
    {
        NABUCCO.component.CMISDocListTree.superclass.constructor.call(this, htmlId);

        // note: we might change the widget name here, but Alfresco.DocListTree is pretty ingrained in some default features we want to use

        return this;
    };

    YAHOO.extend(NABUCCO.component.CMISDocListTree, Alfresco.DocListTree,
    {

        _buildTreeNodeUrl : function(path)
        {
            var uriTemplate = "api/nabucco/cmis/treenode?path=" + Alfresco.util.encodeURIPath(path);
            uriTemplate += "&children=" + this.options.evaluateChildFolders + "&max=" + this.options.maximumFolderCount;
            return Alfresco.constants.PROXY_URI + uriTemplate;
        }
    });
}());