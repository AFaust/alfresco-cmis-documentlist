function main()
{
    var evaluateChildFolders = "true", maximumFolderCount = "-1", docLibConfig = config.scoped["CMISDocLib"], defaultDocLibConfig = config.scoped["DocumentLibrary"], tree, tmp, docListTree;
    if (docLibConfig !== null || defaultDocLibConfig !== null)
    {
        if (docLibConfig !== null)
        {
            tree = docLibConfig["tree"] || (defaultDocLibConfig !== null ? defaultDocLibConfig["tree"] : null);
        }
        else
        {
            tree = defaultDocLibConfig["tree"];
        }

        if (tree != null)
        {
            tmp = tree.getChildValue("evaluate-child-folders");
            evaluateChildFolders = tmp !== null ? tmp : "true";
            tmp = tree.getChildValue("maximum-folder-count");
            maximumFolderCount = tmp !== null ? tmp : "-1";
        }
    }

    docListTree =
    {
        id : "DocListTree",
        name : "NABUCCO.component.CMISDocListTree",
        options :
        {
            siteId : "",
            containerId : "documentLibrary",
            evaluateChildFolders : (evaluateChildFolders == "true"),
            maximumFolderCount : parseInt(maximumFolderCount),
            setDropTargets : true
        }
    };
    model.widgets = [ docListTree ];
}

main();
