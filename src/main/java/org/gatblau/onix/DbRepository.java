/*
Onix CMDB - Copyright (c) 2018-2019 by www.gatblau.org

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contributors to this project, hereby assign copyright in their code to the
project, to be licensed under the same terms as the rest of the code.
*/

package org.gatblau.onix;

import org.gatblau.onix.data.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

/*
    Provides an abstraction to the underlying onix cmdb database.
 */
public interface DbRepository {
    /*
        ITEMS
    */
    Result createOrUpdateItem(String key, JSONObject json) throws IOException, SQLException, ParseException;
    ItemData getItem(String key) throws SQLException, ParseException;
    Result deleteItem(String key) throws SQLException;
    ItemList findItems(String itemTypeKey, List<String> tagList, ZonedDateTime createdFrom, ZonedDateTime createdTo, ZonedDateTime updatedFrom, ZonedDateTime updatedTo, Short status, Integer top) throws SQLException, ParseException;

    /*
        LINKS
    */
    LinkData getLink(String key) throws SQLException, ParseException;
    Result createOrUpdateLink(String key, JSONObject json) throws SQLException, ParseException;
    Result deleteLink(String key) throws SQLException;
    LinkList findLinks();
    Result clear() throws SQLException;

    /*
        ITEM TYPES
    */
    ItemTypeData getItemType(String key);
    Result deleteItemTypes() throws SQLException;
    List<ItemTypeData> getItemTypes() throws SQLException;
    Result createOrUpdateItemType(String key, JSONObject json) throws SQLException;
    Result deleteItemType(String key) throws SQLException;

    /*
        LINK TYPES
    */
    List<LinkTypeData> getLinkTypes();
    Result createOrUpdateLinkType(String key);
    Result deleteLinkType(String key) throws SQLException;
    Result deleteLinkTypes() throws SQLException;

    /*
        LINK RULES
    */
    List<ItemTypeData> getLinkRules();
    Result createOrUpdateLinkRule(String key);
    Result deleteLinkRule(String key) throws SQLException;
    Result deleteLinkRules() throws SQLException;

    /*
        AUDIT
    */
    List<AuditItemData> findAuditItems();

    /*
        Function Calls
     */
    String getGetItemSQL();
    String getSetItemSQL();
    String getFindItemsSQL();
    String getDeleteItemSQL();

    String getDeleteLinkSQL();
    String getGetLinkSQL();
    String getSetLinkSQL();
    String getFindLinksSQL();

    String getClearAllSQL();

    String getDeleteItemTypeSQL();
    String getDeleteItemTypes();
    String getFindItemTypesSQL();
    String getSetItemTypeSQL();

    String getDeleteLinkTypeSQL();
    String getDeleteLinkTypes();
    String getSetLinkTypeSQL();

    String getDeleteLinkRuleSQL();
    String getDeleteLinkRulesSQL();
}