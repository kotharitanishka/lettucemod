package com.example.lettucemoddemo.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.lettucemoddemo.model.Person;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import com.redis.lettucemod.json.SetMode;
import com.redis.lettucemod.search.CreateOptions;
import com.redis.lettucemod.search.Field;
//import com.redis.lettucemod.search.Field;
import com.redis.lettucemod.search.SearchOptions;
import com.redis.lettucemod.search.SearchResults;
import com.redis.lettucemod.search.SearchOptions.SortBy;

import io.lettuce.core.RedisURI;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

//import java.lang.reflect.Field;
//import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class Controller {

    // // create modules client
    // RedisModulesClient client =
    // RedisModulesClient.create(RedisURI.create("localhost", 6379));

    // // connect to redis server
    // StatefulRedisModulesConnection<String, String> connection = client.connect();

    // // Obtain the command API for synchronous execution.
    // RedisModulesCommands<String, String> commands = connection.sync();

    @Autowired
    StatefulRedisModulesConnection<String, String> connection;

    RedisModulesCommands<String, String> commands;



    public Map<String, Object> jsonResponse(SearchResults<String, String> ans) {

        Map<String, Object> m1 = new LinkedHashMap<String, Object>();
        // System.out.println(ans.get(0).getId().split(":")[1]);
        // System.out.println(ans);
        Integer c = ans.size();
        for (Integer i = 0; i < c; i++) {
            // System.out.println(ans.get(i).getId());
            String k1 = (ans.get(i).getId().split(":")[1]);
            String k = k1;
            Object v = (ans.toArray()[i]);
            m1.put(k, v);
            // System.out.println(m1);
        }
        return m1;

    }

    @PostMapping("/new")
    public ResponseEntity<Map<String, Object>> addPerson(@RequestBody @Valid Person p,
            @RequestParam(required = false) String user) {

        commands = connection.sync();
        String p_json;
        // System.out.println("\n\ntest\n");
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        ResponseEntity<Map<String, Object>> result;
        result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);

        if (user == null || user.isEmpty()) {
            System.out.println("\n\n\nuser is null\n\n\n");
            map.put("message", "ERROR : enter audit field user");
            map.put("error", HttpStatus.BAD_REQUEST.name());
            map.put("code", HttpStatus.BAD_REQUEST.value());
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
            return result;
            // return new ResponseEntity<String>("ERROR : enter audit field user ",
            // HttpStatus.BAD_REQUEST);
        }
        if (p.getId() == null) {
            // return new ResponseEntity<String>("cannot create without id",
            // HttpStatus.BAD_REQUEST);
            map.put("message", "cannot create without id");
            map.put("error", HttpStatus.BAD_REQUEST.name());
            map.put("code", HttpStatus.BAD_REQUEST.value());
            // ResponseEntity.status(HttpStatus.NO_CONTENT).body(map);
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
            return result;
        }
        try {
            CreateOptions<String, String> options = CreateOptions.<String, String>builder()
                    .on(CreateOptions.DataType.JSON)
                    .prefixes("People:")
                    .build();

            commands.ftCreate("pidx", options, Field.text("$.id").as("id").sortable(true).build(),
                    Field.text("$.name").as("name").build(), Field.numeric("$.age").as("age").build(),
                    Field.tag("$.active0").as("active0").build());
        } catch (Exception e) {
            System.out.println("already exists index");
        }
        try {

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String time = timestamp.toString();
            p.setcreatedOn0(time);
            p.setcreatedBy0(user);
            p.setactive0(false);
            p_json = new ObjectMapper().writeValueAsString(p);
            String pid = p.getId();
            String key_p0 = "People:" + pid + ":0";
            String key_p1 = "People:" + pid + ":1";
            String s1 = commands.jsonSet(key_p1, "$", p_json, SetMode.NX);
            p.setactive0(true);
            p_json = new ObjectMapper().writeValueAsString(p);
            commands.jsonSet(key_p0, "$", p_json, SetMode.NX);
            if (s1 == null) {
                map.put("message", "id already exists");
                map.put("error", HttpStatus.BAD_REQUEST.name());
                map.put("code", HttpStatus.BAD_REQUEST.value());
                result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
                return result;
                // return new ResponseEntity<String>("id already exists",
                // HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String hkey = "version:People:" + p.getId();
            commands.hset(hkey, "v", "1");

            map.put("message", "created new person " + p.getId());
            map.put("success", HttpStatus.OK.name());
            map.put("code", HttpStatus.OK.value());
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            return result;

            // return new ResponseEntity<String>(p_json, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            map.put("error", "ERROR  ");
            map.put("code", HttpStatus.BAD_REQUEST);
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
            return result;
            // return new ResponseEntity<String>("error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAllPerson(@RequestParam(required = false) Boolean inact,
            @RequestParam(required = false) Integer offset, @RequestParam(required = false) Integer limit,
            HttpServletResponse resp) {
        commands = connection.sync();
        SearchResults<String, String> ans;
        limit = (limit == null) ? 5 : limit;
        if (offset == null) {
            offset = 0;
        }
        // for index of offset starting from 1 instead of 0 :
        // offset = offset - 1
        // if null then set to 1

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        ResponseEntity<Map<String, Object>> result;
        result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);

        SearchOptions<String, String> options1 = SearchOptions.<String, String>builder()
                .limit(offset, limit)
                .returnFields("name")
                .sortBy(SortBy.asc("id"))
                .build();
        // Integer.parseInt(id)

        if (inact == null || inact == false) {
            ans = commands.ftSearch("pidx", "@active0:{true}", options1);
            // System.out.println(ans);
        } else {
            ans = commands.ftSearch("pidx", "*", options1);
        }
        // System.out.println(ans);
        // Map<String, Object> map = new LinkedHashMap<String, Object>();

        if (ans == null || ans.isEmpty() == true) {
            map.put("message", "list empty");
            map.put("error", HttpStatus.BAD_REQUEST.name());
            map.put("code", HttpStatus.BAD_REQUEST.value());
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
            return result;
            // map.put("data", List.of());
            // return map;
        } else {

            Long tc = ans.getCount();
            String tcount = tc.toString();
            resp.setHeader("TotalCount", tcount);

            map.put("data", jsonResponse(ans));
            // System.out.println(jsonResponse(ans));
            ;
            Integer t;
            if (limit >= tc) {
                t = -1;
            } else {
                t = offset + limit;
            }
            Integer lp = (int) Math.ceil(Math.abs((double) tc / limit));

            map.put("nextOffset", t);
            map.put("lastPage", lp);
            map.put("success", HttpStatus.OK.name());
            map.put("code", HttpStatus.OK.value());
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            return result;
            // return map;
        }
    }

    public Person keyToPerson(String key) {
        commands = connection.sync();
        String person = commands.jsonGet(key, "$");
        if (person == null) {
            return null;
        }
        try {
            JSONArray array = new JSONArray(person);
            JSONObject object = array.getJSONObject(0);
            Person p = new ObjectMapper().readValue(object.toString(), Person.class);
            return p;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @GetMapping("/getById")
    public ResponseEntity<Map<String, Object>> getPersonById(@RequestParam String id,
            @RequestParam(required = false) Integer min, @RequestParam(required = false) Integer max) {

        commands = connection.sync();

        String key_p = "People:" + id + ":0";
        Person p0 = keyToPerson(key_p);
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        Map<String, Object> versions = new LinkedHashMap<String, Object>();
        ResponseEntity<Map<String, Object>> result;
        result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);

        // Person p0 = keyToPerson(key_p);
        if (p0 == null) {
            map.put("message", "enter valid id");
            map.put("error", HttpStatus.BAD_REQUEST.name());
            map.put("code", HttpStatus.BAD_REQUEST.value());
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
            return result;
            // return map;
        }

        else {
            try {
                if (p0.isactive0()) {

                    // case 1 : min and max both are not given
                    if (min == null) {
                        if (max == null) {
                            map.put("base", p0);
                            map.put("history", versions);
                            map.put("success", HttpStatus.OK.name());
                            map.put("code", HttpStatus.OK.value());
                            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
                            return result;
                            // return map;
                        } else {
                            min = 1;
                        }
                    } else {
                        if (max == null) {
                            String hkey = "version:People:" + id;
                            String ver = commands.hget(hkey, "v");
                            max = Integer.parseInt(ver);
                            System.out.println("max = " + max);
                        }
                    }

                    // for min (base)
                    String key_min = "People:" + id + ":1";
                    Person pmin = keyToPerson(key_min);

                    if (min == 1) {
                        map.put("base", pmin);
                    } else {
                        String key_min_v;
                        String delta_min_v;
                        ArrayNode delta_node_min_v;
                        for (Integer k = 2; k <= min; k++) {
                            key_min_v = "People:" + id + ":" + k.toString();
                            delta_min_v = commands.jsonGet(key_min_v, "$");
                            delta_node_min_v = (ArrayNode) new ObjectMapper().readTree(delta_min_v);
                            pmin = updateDelta(pmin, delta_node_min_v.get(0));
                        }
                        map.put("base", pmin);
                    }

                    // for max (history)
                    String key_v;
                    String delta_v;
                    ArrayNode delta_node;
                    for (Integer i = min + 1; i <= max; i++) {
                        key_v = "People:" + id + ":" + i.toString();
                        delta_v = commands.jsonGet(key_v, "$");
                        delta_node = (ArrayNode) new ObjectMapper().readTree(delta_v);
                        versions.put(i.toString(), delta_node.get(0));
                    }
                    map.put("history", versions);
                    map.put("success", "true");
                    map.put("code", HttpStatus.OK.value());
                    result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
                    return result;

                    // return map;

                } else {
                    map.put("message", "enter valid id");
                    map.put("error", HttpStatus.BAD_REQUEST.name());
                    map.put("code", HttpStatus.BAD_REQUEST.value());
                    result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
                    return result;
                    // return map;
                }

            } catch (Exception e) {
                System.out.println(e);
                map.put("error", "ERROR");
                map.put("code", HttpStatus.BAD_REQUEST);
                result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
                return result;
                // return map;
            }
        }

    }

    public SearchOptions<String, String> options2 = SearchOptions.<String, String>builder()
            .sortBy(SortBy.asc("id"))
            .build();

    @GetMapping("/getByName")
    public ResponseEntity<Map<String, Object>> getPersonByName(@RequestParam String n) {

        commands = connection.sync();

        String q;
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        // Map<String, Object> versions = new LinkedHashMap<String, Object>();
        ResponseEntity<Map<String, Object>> result;
        result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
        q = "(@name:" + n + " & @active0:{true})";

        SearchResults<String, String> s = commands.ftSearch("pidx", q, options2);
        // Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (s == null || s.isEmpty() == true) {
            map.put("data", List.of());
            // list.of() --> collections.emptylist
            map.put("error", HttpStatus.BAD_REQUEST.name());
            map.put("code", HttpStatus.BAD_REQUEST.value());
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
            return result;
            // return map;
        } else {

            System.out.println(s);

            Object[] ob = new Object[s.size()];
            String s0;
            Person pi;
            Person[] plist = new Person[s.size()];

            for (int i = 0; i < ob.length; i++) {
                ob[i] = s.get(i).values().toArray()[1];
                s0 = ob[i].toString();
                try {
                    pi = new ObjectMapper().readValue(s0, Person.class);
                    plist[i] = pi;
                } catch (Exception e) {
                    map.put("data", List.of());
                    e.printStackTrace();
                }

            }
            map.put("data", plist);
            map.put("success", HttpStatus.OK.name());
            map.put("code", HttpStatus.OK.value());
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            return result;

            // return map;
        }
    }

    @PutMapping("/updateById")
    public ResponseEntity<Map<String, Object>> updatePersonById(@RequestParam String id, @RequestParam String user,
            @RequestBody(required = false) Map<String, Object> m) {

        commands = connection.sync();

        String key_p0 = "People:" + id + ":0";
        Person p0 = keyToPerson(key_p0);
        // System.out.println(p0.getClass().getDeclaredFields());
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        ResponseEntity<Map<String, Object>> result;
        result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);

        if (p0 == null || p0.isactive0() == false) {
            map.put("message", "enter relevant id");
            map.put("error", HttpStatus.BAD_REQUEST.name());
            map.put("code", HttpStatus.BAD_REQUEST.value());
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
            return (result);

        }
        java.lang.reflect.Field f[] = p0.getClass().getDeclaredFields();
        Set<String> p_keys = new LinkedHashSet<String>();
        for (int fi = 0; fi < f.length; fi++) {
            // System.out.println(f[fi].getName());
            String fkey = f[fi].getName();
            p_keys.add(fkey);
        }

        if (m == null || m.isEmpty() == true) {
            map.put("message", "enter relevant data to update");
            map.put("error", HttpStatus.BAD_REQUEST.name());
            map.put("code", HttpStatus.BAD_REQUEST.value());
            // ResponseEntity.status(HttpStatus.NO_CONTENT).body(map);
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
            return result;
        }
        Object[] keys = m.keySet().toArray();
        try {
            for (int i = 0; i < keys.length; i++) {
                String val = new ObjectMapper().writeValueAsString(m.get(keys[i]));

                String path;
                if (p_keys.contains(keys[i])) {
                    if (keys[i] == "id") {
                        System.out.println("cannot change key");
                    } else if (keys[i] == "detail") {
                        // System.out.println();
                        Object[] ob = m.values().toArray();
                        // System.out.println(ob[i]);
                        String d[] = ob[i].toString().replace("{", "").replace("}", "").replace(" ", "").split(",");
                        // System.out.println(keys[i]);
                        for (int k = 0; k < d.length; k++) {
                            // System.out.println(d[k].split("=")[0]);
                            String d_key = d[k].split("=")[0];
                            String d_val = d[k].split("=")[1];
                            // System.out.println(d_val);
                            path = "$.detail" + "." + d_key;
                            // System.out.println(path);
                            String u = new ObjectMapper().writeValueAsString(d_val);
                            String ans1 = commands.jsonSet(key_p0, path, u, SetMode.XX);
                            if (ans1 == null) {
                                commands.jsonSet(key_p0, path, u, SetMode.NX);
                            }
                        }
                    } else {
                        path = "$." + keys[i];
                        commands.jsonSet(key_p0, path, val, SetMode.XX);

                    }
                } else {
                    m.remove(keys[i]);
                }
            }

            Map<String, Object> delta = new LinkedHashMap<String, Object>();
            delta.put("data", m);
            String u = new ObjectMapper().writeValueAsString(user);
            commands.jsonSet(key_p0, "$.updatedBy0", u, SetMode.XX);
            delta.put("updatedBy0", user);

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String time = timestamp.toString();
            try {
                String t = new ObjectMapper().writeValueAsString(time);
                commands.jsonSet(key_p0, "$.updatedOn0", t, SetMode.XX);
                delta.put("updatedOn0", time);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                System.out.println(e);
            }

            String hkey = "version:People:" + id;
            String ver = commands.hget(hkey, "v");
            Integer version = (Integer.parseInt(ver) + 1);
            ver = String.valueOf(version);
            commands.hset(hkey, "v", ver);
            String key_p = "People:" + id + ":" + ver;

            String value = new ObjectMapper().writeValueAsString(delta);
            commands.jsonSet(key_p, "$", value, SetMode.NX);

            map.put("message", "updated");
            map.put("success", HttpStatus.OK.name());
            map.put("code", HttpStatus.OK.value());
            // ResponseEntity.status(HttpStatus.OK).body(map);
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            return result;
            // return result;
            // return new ResponseEntity<String>("updated", HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e);
            map.put("error", "error");
            map.put("code", HttpStatus.BAD_REQUEST);
            // ResponseEntity.status(HttpStatus.NO_CONTENT).body(map);
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
            return result;
            // return result;
            // return new ResponseEntity<String>("error , did not update",
            // HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Person updateDelta(Person p, JsonNode d) {

        JsonNode data = d.get("data");

        if (data.has("name")) {
            p.setName(data.get("name").asText());
        }
        if (data.has("age")) {
            Integer a = (Integer) data.get("age").asInt();
            p.setAge(a);
        }
        if (data.has("mobNo")) {
            p.setMobNo((data.get("mobNo").asText()));
        }
        if (data.has("dob")) {
            p.setDob((data.get("dob").asText()));
        }
        if (d.has("updatedBy0")) {
            p.setupdatedBy0(d.get("updatedBy0").asText());
        }
        if (d.has("updatedOn0")) {
            p.setupdatedOn0(d.get("updatedOn0").asText());
        }
        if (data.has("detail")) {

            Iterator<Entry<String, JsonNode>> i = data.get("detail").fields();
            while (i.hasNext()) {
                Entry<String, JsonNode> iter = i.next();
                p.setDetail(iter.getKey(), iter.getValue());
            }
        }

        return p;

    }

    @DeleteMapping("/deleteById")
    public ResponseEntity<Map<String, Object>> deletePersonById(@RequestParam String id, @RequestParam String user) {

        commands = connection.sync();
        String hkey = "version:People:" + id;
        String ver = commands.hget(hkey, "v");
        System.out.println(ver);
        Map<String, Object> delta = new LinkedHashMap<String, Object>();
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        ResponseEntity<Map<String, Object>> result;
        result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
        // ResponseEntity<Object> result = new ResponseEntity<Object>();
        if (ver == null) {
            map.put("message", "enter valid id");
            map.put("error", HttpStatus.BAD_REQUEST.name());
            map.put("code", HttpStatus.BAD_REQUEST.value());
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
            return result;
            // return new ResponseEntity<String>("enter relevant id",
            // HttpStatus.NO_CONTENT);
        }
        try {

            String key_p0 = "People:" + id + ":0";
            Person p0 = keyToPerson(key_p0);
            if (p0 == null || p0.isactive0() == false) {

                // map.put("base", versions);
                // map.put("history", versions);
                // return map;
                map.put("message", "enter valid id");
                map.put("error", HttpStatus.BAD_REQUEST.name());
                map.put("code", HttpStatus.BAD_REQUEST.value());
                result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
                return result;
                // return new ResponseEntity<String>("enter relevant id",
                // HttpStatus.NO_CONTENT);
            }
            String u;
            u = new ObjectMapper().writeValueAsString(user);

            commands.jsonSet(key_p0, "$.updatedBy0", u, SetMode.XX);
            delta.put("updatedBy0", user);

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String time = timestamp.toString();
            String t = new ObjectMapper().writeValueAsString(time);
            commands.jsonSet(key_p0, "$.updatedOn0", t, SetMode.XX);
            delta.put("updatedOn0", time);

            commands.jsonSet(key_p0, "$.active0", "false");
            delta.put("active0", false);

            Integer version = (Integer.parseInt(ver) + 1);
            ver = String.valueOf(version);
            commands.hset(hkey, "v", ver);
            String key_p = "People:" + id + ":" + ver;

            String value;

            value = new ObjectMapper().writeValueAsString(delta);

            commands.jsonSet(key_p, "$", value, SetMode.NX);

            // return new ResponseEntity<String>("Deleted People : " + id, HttpStatus.OK);
            map.put("success", "Deleted People :" + id);
            map.put("code", HttpStatus.OK);
            // ResponseEntity.status(HttpStatus.OK).body(map);
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            return result;
        } catch (Exception e) {
            map.put("error", "enter valid id");
            map.put("code", HttpStatus.BAD_REQUEST);
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
            return result;
            // return new ResponseEntity<String>("error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
