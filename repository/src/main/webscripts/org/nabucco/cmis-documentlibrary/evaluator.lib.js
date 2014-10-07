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

    convertPropertyValue : function(property, propertyValue)
    {
        // TODO: call out to (potential) decorators using importScript API
        var propertyTypeId = property.type.value(), decoratedValue = {};

        if (propertyValue !== null)
        {
            switch (String(propertyTypeId))
            {
                case "boolean":
                    decoratedValue = propertyValue === true || (propertyValue.equals !== undefined && propertyValue.equals(true));
                    break;
                case "id": // fallthrough
                case "html": // fallthrough
                case "uri": // fallthrough
                case "string":
                    decoratedValue = String(propertyValue);
                    break;
                case "integer":
                    if (typeof propertyValue === "number")
                    {
                        decoratedValue = propertyValue;
                    }
                    else if (propertyValue.intValue !== undefined)
                    {
                        decoratedValue = propertyValue.intValue;
                    }
                    else
                    {
                        decoratedValue = parseInt(String(propertyValue), 10);
                    }
                    break;
                case "decimal":
                    if (typeof propertyValue === "number")
                    {
                        decoratedValue = propertyValue;
                    }
                    else if ((propertyValue.isNaN !== undefined && propertyValue.isNaN())
                            || (propertyValue.isInfinite !== undefined && propertyValue.isInfinite()))
                    {
                        decoratedValue = null;
                    }
                    else if (propertyValue.doubleValue !== undefined)
                    {
                        decoratedValue = propertyValue.doubleValue;
                    }
                    else
                    {
                        // TODO: else?
                        decoratedValue = null;
                    }
                    break;
                case "datetime":
                    decoratedValue =
                    {
                        value : String(propertyValue.time),
                        iso8601 : dateFormatter.format(propertyValue.time)
                    };
                    break;
                default:
                    logger.info("Unsupported property type: {}", propertyTypeId);
            }
        }

        return decoratedValue;
    },

    run : function(node, isParent)
    {
        var idx, propIdx, max, propMax, nodeType = Evaluator.getNodeType(node), nodeData = {}, workingCopy = {}, properties = {}, property, propertyData, propertyValue, propertyValueElement, secondaryTypes, path;

        // root values
        nodeData.nodeRef = node.cmisObject.id;

        nodeData.type = node.cmisObject.type.id;
        nodeData.aspects = [];
        secondaryTypes = node.cmisObject.secondaryTypes;
        if (secondaryTypes !== null)
        {
            for (idx = 0, max = secondaryTypes.size(); idx < max; idx++)
            {
                nodeData.aspects.push(secondaryTypes.get(idx).id);
            }
        }

        nodeData.isContainer = node.isContainer;
        nodeData.isContent = !node.isContainer && !node.isItem;
        nodeData.isLocked = false;
        nodeData.isLink = false;

        // TODO: map
        nodeData.permissions =
        {
            inherited : false,
            roles : [],
            user : {}
        };

        nodeData.properties = properties;

        // mirror some core properties as Alfresco properties
        properties["cm:name"] = node.cmisObject.name;
        properties["cm:description"] = node.cmisObject.description;
        properties["cm:creator"] =
        {
            userName : String(node.cmisObject.createdBy)
        };

        Evaluator.fillUserData(properties["cm:creator"]);

        properties["cm:created"] =
        {
            value : String(node.cmisObject.creationDate.time),
            iso8601 : dateFormatter.format(node.cmisObject.creationDate.time)
        };

        properties["cm:modifier"] =
        {
            userName : String(node.cmisObject.lastModifiedBy)
        };

        Evaluator.fillUserData(properties["cm:modifier"]);

        properties["cm:modified"] =
        {
            value : String(node.cmisObject.lastModificationDate.time),
            iso8601 : dateFormatter.format(node.cmisObject.lastModificationDate.time)
        };

        for (idx = 0, max = node.cmisObject.properties.size(); idx < max; idx++)
        {
            property = node.cmisObject.properties.get(idx);
            propertyValue = property.getValue();

            if (property.isMultiValued())
            {
                propertyData = [];
                for (propIdx = 0, propMax = propertyValue.size(); propIdx < propMax; propIdx++)
                {
                    propertyValueElement = propertyValue.get(propIdx);
                    propertyData.push(Evaluator.convertPropertyValue(property, propertyValueElement));
                }
            }
            else
            {
                propertyData = Evaluator.convertPropertyValue(property, propertyValue);
            }
            nodeData.properties[property.id] = propertyData;
        }

        // enhance the node
        node.properties = nodeData.properties;
        node.nodeRef = nodeData.nodeRef;
        node.type = nodeData.type;
        node.aspects = nodeData.aspects;
        node.permissions = nodeData.permissions;

        if (!isParent)
        {
            // Get relevant actions set
            switch (nodeType)
            {
                case "document":
                    nodeData.size = node.cmisObject.contentStreamLength;
                    nodeData.mimetype = node.cmisObject.contentStreamMimeType;

                    if (nodeData.mimetype !== null && nodeData.mimetype.indexOf(";charset=") !== -1)
                    {
                        nodeData.encoding = nodeData.mimetype.substring(nodeData.mimetype.indexOf(";charset=") + 9);
                        nodeData.mimetype = nodeData.mimetype.substring(0, nodeData.mimetype.indexOf(";charset="));
                    }

                    // Working Copy?
                    // TODO: use CMIS data
                    // if (node.hasAspect("{http://www.alfresco.org/model/content/1.0}workingcopy"))
                    // {
                    // var wcLink = node.sourceAssocs["cm:workingcopylink"];
                    // var isWorkingCopy = wcLink != null;
                    // if (isWorkingCopy)
                    // {
                    // var wcNode = wcLink[0];
                    // workingCopy["isWorkingCopy"] = true;
                    // workingCopy["sourceNodeRef"] = wcNode.nodeRef;
                    // if (wcNode.hasAspect("{http://www.alfresco.org/model/content/1.0}versionable"))
                    // {
                    // workingCopy["workingCopyVersion"] = wcNode.properties["cm:versionLabel"];
                    // }
                    //
                    // // Google Doc?
                    // if (node.hasAspect("{http://www.alfresco.org/model/googledocs/1.0}googleResource"))
                    // {
                    // // Property is duplicated here for convenience
                    // workingCopy["googleDocUrl"] = node.properties["gd:url"];
                    // }
                    // }
                    // else
                    // {
                    // logger.error("Node: " + node.nodeRef +" hasn't \"cm:workingcopylink\" association");
                    // }
                    // }
                    // Locked?
                    // else if (node.isLocked && !node.hasAspect("trx:transferred") &&
                    // node.hasAspect("{http://www.alfresco.org/model/content/1.0}checkedOut"))
                    // {
                    // var srcNode = node.assocs["cm:workingcopylink"][0];
                    // workingCopy["hasWorkingCopy"] = true;
                    // workingCopy["workingCopyNodeRef"] = srcNode.nodeRef;
                    //
                    // }
                    break;
            }
        }

        if (node !== null)
        {
            path = parent.cmisObject.getPropertyValue("cmis:path");

            return (
            {
                node : node,
                location :
                {
                    file : node.cmisObject.name,
                    path : path,
                    parent : node.cmisObject.getPropertyValue("cmis:parentId")
                },
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