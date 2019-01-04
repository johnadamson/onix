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
import org.postgresql.util.HStoreConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class PgSqlRepository implements DbRepository {

    @Autowired
    private Lib util;

    @Autowired
    private Database db;

    public PgSqlRepository() {
    }

    /*
       ITEMS
     */

    @Override
    public Result createOrUpdateItem(String key, JSONObject json) throws IOException, SQLException, ParseException {
        Result result = new Result();
        ResultSet set = null;
        try {
            Object name = json.get("name");
            Object description = json.get("description");
            String meta = util.toJSONString(json.get("meta"));
            String tag = util.toArrayString(json.get("tag"));
            Object attribute = json.get("attribute");
            Object status = json.get("status");
            Object type = json.get("type");
            Object version = json.get("version");

            db.prepare(getSetItemSQL());
            db.setString(1, key); // key_param
            db.setString(2, (name != null) ? (String) name : null); // name_param
            db.setString(3, (description != null) ? (String) description : null); // description_param
            db.setString(4, meta); // meta_param
            db.setString(5, tag); // tag_param
            db.setString(6, (attribute != null) ? HStoreConverter.toString((LinkedHashMap<String, String>) attribute) : null); // attribute_param
            db.setInt(7, (status != null) ? (int) status : null); // status_param
            db.setString(8, (type != null) ? (String) type : null); // item_type_key_param
            db.setObject(9, version); // version_param
            db.setString(10, getUser()); // changedby_param
            result.setOperation(db.executeQueryAndRetrieveStatus("set_item"));
        }
        catch(Exception ex) {
            result.setError(true);
            result.setMessage(ex.getMessage());
        }
        finally {
            db.close();
        }
        return result;
    }

    @Override
    public ItemData getItem(String key) throws SQLException, ParseException {
        try {
            db.prepare(getGetItemSQL());
            db.setString(1, key);
            ItemData item = util.toItemData(db.executeQuerySingleRow());

            ResultSet set;

            db.prepare(getFindLinksSQL());
            db.setString(1, item.getKey()); // start_item
            db.setObjectRange(2, 9, null);
            set = db.executeQuery();
            while (set.next()) {
                item.getFromLinks().add(util.toLinkData(set));
            }

            db.prepare(getFindLinksSQL());
            db.setString(1, null); // start_item
            db.setString(2, item.getKey()); // end_item
            db.setObjectRange(3, 9, null);
            set = db.executeQuery();
            while (set.next()) {
                item.getFromLinks().add(util.toLinkData(set));
            }
            return item;
        }
        finally {
            db.close();
        }
    }

    @Override
    public Result deleteItem(String key) throws SQLException {
        return delete(getDeleteItemSQL(), key);
    }

    @Override
    public ItemList findItems(String itemTypeKey, List<String> tagList, ZonedDateTime createdFrom, ZonedDateTime createdTo, ZonedDateTime updatedFrom, ZonedDateTime updatedTo, Short status, Integer top) throws SQLException, ParseException {
        ItemList items = new ItemList();
        db.prepare(getFindItemsSQL());
        db.setString(1, util.toArrayString(tagList));
        db.setString(2, null); // attribute
        db.setObject(3, status);
        db.setString(4, itemTypeKey);
        db.setObject(5, (createdFrom != null) ? java.sql.Date.valueOf(createdFrom.toLocalDate()) : null);
        db.setObject(6, (createdTo != null) ? java.sql.Date.valueOf(createdTo.toLocalDate()) : null);
        db.setObject(7, (updatedFrom != null) ? java.sql.Date.valueOf(updatedFrom.toLocalDate()) : null);
        db.setObject(8, (updatedTo != null) ? java.sql.Date.valueOf(updatedTo.toLocalDate()) : null);
        db.setObject(9, (top == null) ? 20 : top);
        ResultSet set = db.executeQuery();
        while (set.next()) {
            items.getItems().add(util.toItemData(set));
        }
        return items;
    }

    /*
       LINKS
     */
    @Override
    public LinkData getLink(String key) throws SQLException, ParseException {
        LinkData link = null;
        try {
            db.prepare(getGetLinkSQL());
            db.setString(1, key);
            ResultSet set = db.executeQuerySingleRow();
            link = util.toLinkData(set);
        }
        finally {
            db.close();
        }
        return link;
    }

    @Override
    public Result createOrUpdateLink(String key, JSONObject json) throws SQLException, ParseException {
        String description = (String)json.get("description");
        String linkTypeKey = (String)json.get("linkType");
        String startItemKey = (String)json.get("startItem");
        String endItemKey = (String)json.get("endItem");
        String meta = util.toJSONString(json.get("meta"));
        String tag = util.toArrayString(json.get("tag"));
        Object attribute = json.get("attribute");
        Object version = json.get("version");
        Result result = new Result();
        try {
            db.prepare(getSetLinkSQL());
            db.setString(1, key);
            db.setString(2, linkTypeKey);
            db.setString(3, startItemKey);
            db.setString(4, endItemKey);
            db.setString(5, description);
            db.setString(6, meta);
            db.setString(7, tag);
            db.setString(8, (attribute != null) ? HStoreConverter.toString((LinkedHashMap<String, String>) attribute) : null);
            db.setObject(9, version);
            db.setString(10, getUser());
            result.setOperation(db.executeQueryAndRetrieveStatus("set_link"));
        }
        finally {
            db.close();
        }
        return result;
    }

    @Override
    public Result deleteLink(String key) throws SQLException {
        return delete(getDeleteLinkSQL(), key);
    }

    @Override
    public LinkList findLinks() {
        // TODO: implement findLinks()
        throw new UnsupportedOperationException("findLinks");
    }

    @Override
    public Result clear() throws SQLException {
        return delete(getClearAllSQL(), null);
    }

    private Result delete(String sql, String key) throws SQLException {
        Result result = new Result();
        try {
            db.prepare(sql);
            if (key != null) {
                db.setString(1, key);
            }
            boolean deleted = db.execute();
            result.setOperation((deleted) ? "D" : "N");
        }
        finally {
            db.close();
        }
        return result;
    }
    /*
        ITEM TYPES
     */
    @Override
    public ItemTypeData getItemType(String key) {
        // TODO: implement getItemType()
        throw new UnsupportedOperationException("getItemType");
    }

    @Override
    public Result deleteItemTypes() throws SQLException {
        return delete(getDeleteItemTypes(), null);
    }

    @Override
    public List<ItemTypeData> getItemTypes() throws SQLException {
        List<ItemTypeData> list = new ArrayList<>();
        try {
            db.prepare(getFindItemTypesSQL());
        }
        finally {
            db.close();
        }
        return list;
    }

    @Override
    public Result createOrUpdateItemType(String key, JSONObject json) throws SQLException {
        Result result = new Result();
        Object name = json.get("name");
        Object description = json.get("description");
        Object attribute = json.get("attribute_validation");
        Object version = json.get("version");
        try {
            db.prepare(getSetItemTypeSQL());
            db.setString(1, key); // key_param
            db.setString(2, (name != null) ? (String) name : null); // name_param
            db.setString(3, (description != null) ? (String) description : null); // description_param
            db.setString(4, (attribute != null) ? HStoreConverter.toString((LinkedHashMap<String, String>) attribute) : null); // attribute_param
            db.setObject(5, version); // version_param
            db.setString(6, getUser()); // changedby_param
            result.setOperation(db.executeQueryAndRetrieveStatus("set_item_type"));
        }
        finally {
            db.close();
        }
        return result;
    }

    @Override
    public Result deleteItemType(String key) throws SQLException {
        return delete(getDeleteItemTypeSQL(), key);
    }

    /*
        LINK TYPES
     */
    @Override
    public List<LinkTypeData> getLinkTypes() {
        // TODO: implement getLinkTypes()
        throw new UnsupportedOperationException("getLinkTypes");
    }

    @Override
    public Result createOrUpdateLinkType(String key) {
        // TODO: implement createOrUpdateLinkType()
        throw new UnsupportedOperationException("createOrUpdateLinkType");
    }

    @Override
    public Result deleteLinkType(String key) throws SQLException {
        return delete(getDeleteLinkTypeSQL(), key);
    }

    @Override
    public Result deleteLinkTypes() throws SQLException {
        return delete(getDeleteLinkTypes(), null);
    }

    /*
        LINK RULES
     */
    @Override
    public List<ItemTypeData> getLinkRules() {
        // TODO: implement getLinkRules()
        throw new UnsupportedOperationException("getLinkRules");
    }

    @Override
    public Result createOrUpdateLinkRule(String key) {
        // TODO: implement createOrUpdateLinkRule()
        throw new UnsupportedOperationException("createOrUpdateLinkRule");
    }

    @Override
    public Result deleteLinkRule(String key) throws SQLException {
        return delete(getDeleteLinkRuleSQL(), key);
    }

    @Override
    public Result deleteLinkRules() throws SQLException {
        return delete(getDeleteLinkRulesSQL(), null);
    }

    /*
        AUDIT
     */
    @Override
    public List<AuditItemData> findAuditItems() {
        // TODO: implement findAuditItems()
        throw new UnsupportedOperationException("findAuditItems");
    }

    @Override
    public String getGetItemSQL() {
        return "SELECT * FROM item(?::character varying)";
    }

    @Override
    public String getSetItemSQL() {
        return "SELECT set_item(" +
                "?::character varying," +
                "?::character varying," +
                "?::text," +
                "?::jsonb," +
                "?::text[]," +
                "?::hstore," +
                "?::smallint," +
                "?::character varying," +
                "?::bigint," +
                "?::character varying)";
    }

    @Override
    public String getFindItemsSQL() {
        return "SELECT * FROM find_items(" +
                "?::text[]," + // tag
                "?::hstore," + // attribute
                "?::smallint," + // status
                "?::character varying," + // item_type_key
                "?::timestamp with time zone," + // created_from
                "?::timestamp with time zone," + // created_to
                "?::timestamp with time zone," + // updated_from
                "?::timestamp with time zone," + // updated_to
                "?::integer" + // max_items
                ")";
    }

    @Override
    public String getDeleteItemSQL() {
        return "SELECT delete_item(?::character varying)";
    }

    @Override
    public String getDeleteLinkSQL() {
        return "SELECT delete_link(?::character varying)";
    }

    @Override
    public String getGetLinkSQL() {
        return "SELECT * FROM link(?::character varying)";
    }

    @Override
    public String getSetLinkSQL() {
        return "SELECT set_link(" +
                "?::character varying," + // key
                "?::character varying," + // link_type_key
                "?::character varying," + // start_item_key
                "?::character varying," + // end_item_key
                "?::text," + // description
                "?::jsonb," + // meta
                "?::text[]," + // tag
                "?::hstore," + // attribute
                "?::bigint," + // version
                "?::character varying" + // changed_by
                ")";
    }

    @Override
    public String getFindLinksSQL() {
        return "SELECT * FROM find_links(" +
                "?::character varying," +
                "?::character varying," +
                "?::text[]," +
                "?::hstore," +
                "?::character varying," +
                "?::timestamp with time zone," +
                "?::timestamp with time zone," +
                "?::timestamp with time zone," +
                "?::timestamp with time zone" +
                ")";
    }

    @Override
    public String getClearAllSQL() {
        return "SELECT clear_all()";
    }

    @Override
    public String getDeleteItemTypeSQL() {
        return "SELECT delete_item_type(?::character varying)";
    }

    @Override
    public String getDeleteItemTypes() {
        return "SELECT delete_item_types()";
    }

    @Override
    public String getFindItemTypesSQL() {
        return "SELECT * FROM find_links(" +
                "?::character varying," +
                ")";
    }

    @Override
    public String getSetItemTypeSQL() {
        return "SELECT set_item_type(" +
                "?::character varying," + // key
                "?::character varying," + // name
                "?::text," + // description
                "?::hstore," + // attr_valid
                "?::bigint," + // version
                "?::character varying" + // changed_by
                ")";
    }

    @Override
    public String getDeleteLinkTypeSQL() {
        return "SELECT delete_link_type(?::character varying)";
    }

    @Override
    public String getDeleteLinkTypes() {
        return "SELECT delete_link_types()";
    }

    @Override
    public String getSetLinkTypeSQL() {
        return null;
    }

    @Override
    public String getDeleteLinkRuleSQL() {
        return "SELECT delete_link_rule(?::character varying)";
    }

    @Override
    public String getDeleteLinkRulesSQL() {
        return "SELECT delete_link_rules()";
    }

    private String getUser() {
        String username = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails details = (UserDetails)principal;
            username = details.getUsername();
            for (GrantedAuthority a : details.getAuthorities()){
                username += "|" + a.getAuthority();
            };
        }
        else {
            username = principal.toString();
        }
        return username;
    }
}