importScript("classpath", "/alfresco/templates/webscripts/org/nabucco/cmis-documentlibrary/evaluator.lib.js", true);

function main()
{
    var idx, max, nodeIn, nodesOut = [], nodeOut, result, parentNode;

    parentNode = Evaluator.run(parent, true);

    for (idx = 0, max = nodes.size !== undefined ? nodes.size() : nodes.length; idx < max; idx++)
    {
        nodeIn = nodes.size !== undefined ? nodes.get(idx) : nodes[idx];
        nodeOut = Evaluator.run(nodeIn, false);

        if (nodeOut !== null)
        {
            nodesOut.push(nodeOut);
        }
    }

    result =
    {
        items : nodesOut,
        parent : parentNode,
        resultsTrimmed : resultsTrimmed
    };

    return result;
}

model.treenode = main();
