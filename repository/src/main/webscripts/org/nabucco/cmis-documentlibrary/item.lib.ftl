<#macro itemJSON item>
    <#escape x as jsonUtils.encodeJSONString(x)>
    "version": "${item.node.properties["cmis:versionLabel"]!"1.0"}",
    <#if item.activeWorkflows?? && (item.activeWorkflows?size > 0)>"activeWorkflows": ${item.activeWorkflows?size?c},</#if>
    <#if (item.workingCopyJSON?length > 2)>"workingCopy": <#noescape>${item.workingCopyJSON}</#noescape>,</#if>
    "location":
    {
        "path": "${item.location.path!""}",
        "file": "${item.location.file!""}",
        "parent":
        {
            <#if item.location.parent??>"nodeRef" : "${item.location.parent}"</#if>
        }
    }
    </#escape>
</#macro>