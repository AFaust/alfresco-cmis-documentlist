/*
 * Copyright 2013 PRODYNA AG
 *
 * Licensed under the Eclipse Public License (EPL), Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/eclipse-1.0.php or
 * http://www.nabucco.org/License.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.nabucco.alfresco.cmis.documentlist.repo.jscript;

/**
 * @author Axel Faust, <a href="http://www.prodyna.com">PRODYNA AG</a>
 */
public interface ScriptCMISObject
{

    String getObjectId();

    boolean isContainer();

    boolean isItem();

    boolean isDocument();

    String getPath();

    Object getPaths();

    ScriptCMISObject getParent();

    Object getParents();

    Object getChildren();

    Object getChildren(ScriptCMISOperationContext context);

    Object getChildren(int offset, int max);

    Object getChildren(int offset, int max, ScriptCMISOperationContext context);

    Object getChildren(int offset, int max, String... ignoreTypes);

    Object getChildren(int offset, int max, ScriptCMISOperationContext context, String... ignoreTypes);

    Object getChildren(boolean files, boolean folders);

    Object getChildren(boolean files, boolean folders, ScriptCMISOperationContext context);

    Object getChildren(boolean files, boolean folders, String... ignoreTypes);

    Object getChildren(boolean files, boolean folders, ScriptCMISOperationContext context, String... ignoreTypes);

    Object getChildren(boolean files, boolean folders, int offset, int max);

    Object getChildren(boolean files, boolean folders, int offset, int max, String... ignoreTypes);

    Object getChildren(boolean files, boolean folders, int offset, int max, ScriptCMISOperationContext context);

    Object getChildren(boolean files, boolean folders, int offset, int max, ScriptCMISOperationContext context, String... ignoreTypes);

    Object getProperties();

    Object getDecoratedProperties();

    String getType();

    Object getAspects();

    boolean isSubType(String type);

    boolean hasAspect(String type);

    boolean hasPermission(String permission);

    Object getPermissions();

    Object getDirectPermissions();

    ScriptCMISContent getContent();

    String getDownloadUrl();

    // TODO more operations - more parameterized variants - world domination
}
