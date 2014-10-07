// evaluator adapted from Alfresco standard
var Evaluator =
{
    getNodeType : function(node)
    {
        var nodeType = "document";
        if (node.isContainer)
        {
            nodeType = "folder";
        }
        else if (node.isItem)
        {
            nodeType = "item";
        }
        return nodeType;
    },

    fillUserData : function(userObj)
    {
        var person = people.getPerson(userObj.userName);
        if (person !== null)
        {
            userObj.firstName = person.properties.firstName;
            userObj.lastName = person.properties.lastName;
        }
        else if (userObj.userName === "System" || userObj.userName.indexOf("System@") === 0)
        {
            userObj.firstName = "System";
            userObj.lastName = "User";
        }
        else
        {
            userObj.firstName = "External";
            userObj.lastName = "User";
        }

        userObj.displayName = (userObj.firstName !== null ? (userObj.firstName + (userObj.lastName !== null ? " " : "")) : "")
                + (userObj.lastName !== null ? userObj.lasttName : "");
    },

    run : function(node, isParent)
    {
        var key, nodeType = Evaluator.getNodeType(node), nodeData = {}, workingCopy = {};

        // root values
        // TODO check if we REALLY need to fake this
        nodeData.nodeRef = node.id;

        nodeData.type = node.type;
        nodeData.aspects = node.aspects;

        nodeData.isContainer = node.isContainer;
        nodeData.isContent = node.isDocument;

        nodeData.isLocked = false;
        nodeData.isLink = false;

        // TODO: map
        nodeData.permissions =
        {
            inherited : false,
            roles : [],
            user : {}
        };

        nodeData.properties = {};

        for (key in node.properties)
        {
            nodeData.properties[key] = node.properties[key];
        }

        // property decoration
        // TODO Move into ScriptCMISObject
        nodeData.properties["cm:creator"] =
        {
            userName : String(nodeData.properties["cm:creator"])
        };

        Evaluator.fillUserData(nodeData.properties["cm:creator"]);

        nodeData.properties["cm:modifier"] =
        {
            userName : String(nodeData.properties["cm:modifier"])
        };

        Evaluator.fillUserData(nodeData.properties["cm:modifier"]);

        nodeData.properties["cm:created"] =
        {
            value : new Date().getTime(),
            iso8601 : "2014-10-07T12:00:00.000Z"
        };

        nodeData.properties["cm:modified"] =
        {
            value : new Date().getTime(),
            iso8601 : "2014-10-07T12:00:00.000Z"
        };

        if (!isParent)
        {
            // Get relevant actions set
            switch (nodeType)
            {
                case "document":
                    nodeData.size = 0;
                    nodeData.mimetype = "binary/octet-stream";

                    break;
            }
        }

        if (node !== null)
        {
            return (
            {
                node : node,
                nodeJSON : jsonUtils.toJSONString(nodeData),
                type : nodeType,
                activeWorkflows : [],
                workingCopy : workingCopy,
                workingCopyJSON : jsonUtils.toJSONString(workingCopy)
            });
        }
        return null;
    }
};