<#assign p = treenode.parent.node>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "totalResults": ${treenode.items?size?c},
    "resultsTrimmed": ${treenode.resultsTrimmed?string},
    "parent":
    {
        "nodeRef": "${p.nodeRef}",
        "userAccess":
        {
            "create": ${(p.permissions["CreateChildren"]!false)?string},
            "edit": ${(p.permissions["Write"]!false)?string},
            "delete": ${(p.permissions["Delete"]!false)?string}
        }
    },
    "items":
    [
    <#list treenode.items as item>
        <#assign t = item.node>
        {
            "nodeRef": "${t.nodeRef}",
            "name": "${t.properties["cm:name"]}",
            "description": "${(t.properties["cm:description"]!"")}",
            <#-- assume for now - TODO: calculate at some point -->
            "hasChildren": true,
            "userAccess":
            {
                "create": ${(t.permissions["CreateChildren"]!false)?string},
                "edit": ${(t.permissions["Write"]!false)?string},
                "delete": ${(t.permissions["Delete"]!false)?string}
            },
            "aspects": 
            [
                <#list t.aspects as aspect>
                "${aspect}"<#if aspect_has_next>,</#if>
                </#list>
            ]
        }<#if item_has_next>,</#if>
    </#list>
    ]
}
</#escape>
