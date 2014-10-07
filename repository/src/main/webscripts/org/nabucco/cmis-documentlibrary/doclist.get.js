importScript("classpath", "/alfresco/templates/webscripts/org/nabucco/cmis-documentlibrary/evaluator.lib.js", true);

function main()
{
    var skipCount, max, cmisSession, parent, children, node, items, idx, length, result;

    skipCount = parseInt(args.skipCount || "0", 10);
    max = parseInt(args.max || "50", 10);

    cmisSession = cmisConnector.connect("cmis-alfresco-test");

    if (args.path !== null)
    {
        parent = cmisSession.getObjectByPath(String(args.path).trim());
    }
    else
    {
        parent = cmisSession.getRootFolder();
    }

    children = parent.getChildren(skipCount, max);

    items = [];

    for (idx = 0, length = children.length; idx < length; idx++)
    {
        node = Evaluator.run(children[idx], false);

        if (node !== null)
        {
            if (children[idx].parent !== null)
            {
                node.parent = Evaluator.run(children[idx].parent, true);
                node.location =
                {
                    file : children[idx].properties["cm:name"],
                    path : children[idx].parent.path,
                    parent : children[idx].parent.id
                };
            }
            items.push(node);
        }
    }

    result =
    {
        paging :
        {
            totalRecords : items.length,
            startIndex : skipCount
        },
        itemCount : {

        },
        items : items
    };

    return result;
}

model.doclist = main();
