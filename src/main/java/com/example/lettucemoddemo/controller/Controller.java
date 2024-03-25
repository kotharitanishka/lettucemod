package com.example.lettucemoddemo.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.lettucemoddemo.model.Person;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.redis.lettucemod.search.SearchOptions;
import com.redis.lettucemod.search.SearchResults;
import com.redis.lettucemod.search.SearchOptions.SortBy;

import io.lettuce.core.RedisURI;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class Controller {

    // create modules client
    RedisModulesClient client = RedisModulesClient.create(RedisURI.create("localhost", 6379));

    // connect to redis server
    StatefulRedisModulesConnection<String, String> connection = client.connect();

    // Obtain the command API for synchronous execution.
    RedisModulesCommands<String, String> commands = connection.sync();

    public Map<String, Object> jsonResponse(SearchResults<String, String> ans) {

        Map<String, Object> m1 = new LinkedHashMap<String, Object>();
        System.out.println(ans.get(0).getId().split(":")[1]);
        Integer c = ans.size();
        for (Integer i = 0; i < c; i++) {
            String k1 = (ans.get(i).getId().split(":")[1]);
            String k = k1;
            Object v = (ans.toArray()[i]);
            m1.put(k, v);
        }
        return m1;

    }

    @PostMapping("/new")
    public String addPerson(@RequestBody Person p, @RequestParam(required = false) String user) {
        String p_json;
        if (p.getId() == null) {
            return "cannot create without id";
        }
        if (user == null) {
            return "ERROR : enter audit field user ";
        }
        try {
            CreateOptions<String, String> options = CreateOptions.<String, String>builder()
                    .on(CreateOptions.DataType.JSON)
                    .prefixes("People:")
                    .build();

            commands.ftCreate("pidx", options, Field.text("$.id").as("id").sortable(true).build(),
                    Field.text("$.name").as("name").build(), Field.numeric("$.age").as("age").build(),
                    Field.tag("$.active").as("active").build());
        } catch (Exception e) {
            System.out.println("already exists index");
        }
        try {

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String time = timestamp.toString();
            p.setCreatedOn(time);
            p.setCreatedBy(user);
            p.setActive(false);
            p_json = new ObjectMapper().writeValueAsString(p);
            String pid = p.getId();
            String key_p0 = "People:" + pid + ":0";
            String key_p1 = "People:" + pid + ":1";
            String s1 = commands.jsonSet(key_p1, "$", p_json, SetMode.NX);
            p.setActive(true);
            p_json = new ObjectMapper().writeValueAsString(p);
            commands.jsonSet(key_p0, "$", p_json, SetMode.NX);
            if (s1 == null) {
                return "id already exists";
            }
            String hkey = "version:People:" + p.getId();
            commands.hset(hkey, "v", "1");

            return p_json;
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/getAll")
    public Map<String, Object> getAllPerson(@RequestParam(required = false) Boolean inact,
            @RequestParam(required = false) Integer offset, @RequestParam(required = false) Integer limit,
            HttpServletResponse resp) {
        SearchResults<String, String> ans;
        limit = (limit == null) ? 10 : limit;
        if (offset == null) {
            offset = 0;
        }
        // for index of offset starting from 1 instead of 0 :
        // offset = offset - 1
        // if null then set to 1

        SearchOptions<String, String> options = SearchOptions.<String, String>builder()
                .limit(offset, limit)                
                .returnFields("name")
                .sortBy(SortBy.asc("id"))
                .build();
                
        if (inact == null || inact == false) {
            ans = commands.ftSearch("pidx", "@active:{true}", options);
        } else {
            ans = commands.ftSearch("pidx", "*", options);
        }
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        if (ans.isEmpty() == true) {
            map.put("message", "Person not found");
            map.put("data", List.of());
            return map;
        } else {

            Long tc = ans.getCount();
            String tcount = tc.toString();
            resp.setHeader("TotalCount", tcount);

            map.put("message", "People found");
            map.put("data", jsonResponse(ans));
            Integer t = offset + limit;
            map.put("next ", t);
            return map;
        }
    }

    @GetMapping("/getById")
    public Map<String, Object> getPersonById(@RequestParam String id, @RequestParam(required = false) Boolean inact , @RequestParam(required = false) Integer min , @RequestParam(required = false) Integer max) {
                
        
        String key_p = "People:" + id + ":0";
        String person = commands.jsonGet(key_p, "$");
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        Map<String, Object> versions = new LinkedHashMap<String, Object>();

        if (person == null) {
            map.put("message", "Person not found");
            map.put("base", List.of());
            map.put("history", List.of());
            return map;

        } 
        
        else{
            try {
                JSONArray array = new JSONArray(person);
                JSONObject object = array.getJSONObject(0);
                Person p = new ObjectMapper().readValue(object.toString(), Person.class);
                if (p.isActive()) {

                    //case 1 : min and max both are not given
                    if (min == null && max == null) {
                        map.put("base", p);
                        map.put("history", List.of());
                    }

                    //case 2 : min = 1
                    else if (min == 1) {
                        if (max == null) {
                            String hkey = "version:People:" + id;
                            String ver = commands.hget(hkey, "v");
                            max = Integer.parseInt(ver);
                            System.out.println("max = " + max);
                        }
                        //for min
                        String key_min = "People:" + id + ":1" ;
                        String person_min = commands.jsonGet(key_min, "$");
                        JSONArray pmin_arr = new JSONArray(person_min);
                        JSONObject pmin_obj = pmin_arr.getJSONObject(0);
                        Person pmin = new ObjectMapper().readValue(pmin_obj.toString(), Person.class);
                        map.put("base",pmin);

                        //for max
                        String key_v ;
                        String delta_v ;                         
                        ArrayNode delta_node ;
                        for (Integer i = min+1 ; i <= max ; i ++){
                            key_v = "People:" + id + ":" + i.toString();
                            delta_v = commands.jsonGet(key_v, "$");
                            delta_node = (ArrayNode) new ObjectMapper().readTree(delta_v);                                                 
                            versions.put(i.toString(), delta_node.get(0));
                        }
                        map.put("history", versions);
                    }

                    //case 3 : min not 1
                    else {
                        if (max == null) {
                            String hkey = "version:People:" + id;
                            String ver = commands.hget(hkey, "v");
                            max = Integer.parseInt(ver);
                            System.out.println("max = " + max);
                        }
                        //for min
                        String key_min = "People:" + id + ":1" ;
                        String person_min = commands.jsonGet(key_min, "$");
                        JSONArray pmin_arr = new JSONArray(person_min);
                        JSONObject pmin_obj = pmin_arr.getJSONObject(0);
                        Person pmin = new ObjectMapper().readValue(pmin_obj.toString(), Person.class);
                        String key_min_v ;
                        String delta_min_v ;
                        ArrayNode delta_node_min_v ; 
                        
                        
                        for (Integer k = 2 ; k <= min ; k ++){
                            key_min_v = "People:" + id + ":" + k.toString();
                            delta_min_v = commands.jsonGet(key_min_v, "$");
                            delta_node_min_v = (ArrayNode) new ObjectMapper().readTree(delta_min_v);
                            pmin = updateDelta(pmin , delta_node_min_v.get(0));
                        }
                        map.put("base",pmin);

                        //for max
                        String key_v ;
                        String delta_v ;                         
                        ArrayNode delta_node ;
                        for (Integer i = min+1 ; i <= max ; i ++){
                            key_v = "People:" + id + ":" + i.toString();
                            delta_v = commands.jsonGet(key_v, "$");
                            delta_node = (ArrayNode) new ObjectMapper().readTree(delta_v);                                                 
                            versions.put(i.toString(), delta_node.get(0));
                        }
                        map.put("history", versions);
                    }
                    
                    return map;
                    
                } else {
                    map.put("message", "Person not found");
                    map.put("base", List.of());
                    map.put("history", List.of());
                    return map;
                    // return "id not found";
                }

            } catch (Exception e) {
                System.out.println(e);
                map.put("message", "Error" + e.getMessage());
                map.put("data", List.of());
                return map;
                // return e.getMessage();
            }

        }

    }

    @GetMapping("/getByName")
    public Map<String, Object> getPersonByName(@RequestParam String n, @RequestParam(required = false) Boolean inact) {

        String q;
        if (inact == null || inact == false) {
            q = "(@name:" + n + " & @active:{true})";
        } else {
            q = "@name:" + n;
        }
        SearchOptions<String, String> options = SearchOptions.<String, String>builder()
                .sortBy(SortBy.asc("id"))
                .build();
        SearchResults<String, String> s = commands.ftSearch("pidx", q, options);
        // System.out.println(n);
        // System.out.println(s);
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (s.isEmpty() == true) {
            map.put("message", "Person not found");
            map.put("data", List.of());
            return map;
            // return "name not found";
        } else {
            map.put("message", "Person found");
            map.put("data", jsonResponse(s));
            return map;
            // return goodAns(s);
        }
    }


    @PutMapping("/updateById")
    public String updatePersonById(@RequestParam String id, @RequestParam String user,
            @RequestBody(required = false) Map<String, Object> m) {

        String key_p0 = "People:" + id + ":0";

        Set<String> p_keys = new LinkedHashSet<String>();
        p_keys.add("name");
        p_keys.add("age");
        p_keys.add("detail");
        Object[] keys = m.keySet().toArray();
        try {
            for (int i = 0; i < keys.length; i++) {
                String val = new ObjectMapper().writeValueAsString(m.get(keys[i]));
                System.out.println(val);
                if (p_keys.contains(keys[i])) {
                    String path = "$." + keys[i];
                    commands.jsonSet(key_p0, path, val, SetMode.XX);
                } else if (keys[i] == "id") {
                    System.out.println("cannot change key");
                }
                else {
                    m.remove(keys[i]);
                }
            }

            if (m.isEmpty() == true) {
                return "enter something valid to update";
            }
            Map<String, Object> delta = new LinkedHashMap<String, Object>();
            delta.put("data", m);
            String u = new ObjectMapper().writeValueAsString(user);
            commands.jsonSet(key_p0, "$.updatedBy", u, SetMode.XX);
            delta.put("updatedBy", user);

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String time = timestamp.toString();
            try {
                String t = new ObjectMapper().writeValueAsString(time);
                commands.jsonSet(key_p0, "$.updatedOn", t, SetMode.XX);
                delta.put("updatedOn", time);
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

            return "updated";
        } catch (Exception e) {
            System.out.println(e);
            return "error , did not update";
        }
    }


    
    public Person updateDelta(Person p , JsonNode d ) {

        JsonNode data = d.get("data");

        if (data.has("name") ) {
            p.setName(data.get("name").asText());
        }
        if (data.has("age")) {
            Integer a = (Integer) data.get("age").asInt();
            p.setAge(a);
        }
        if (d.has("updatedBy")) {
            p.setUpdatedBy(d.get("updatedBy").asText());
        }
        if (d.has("updatedOn")) {
            p.setUpdatedOn(d.get("updatedOn").asText());
        }
        if (data.has("detail")){

            Iterator<Entry<String, JsonNode>> i = data.get("detail").fields();
            while (i.hasNext()){
                Entry<String, JsonNode> iter = i.next();
                p.setDetail(iter.getKey(), iter.getValue());
            }
        }
            
        return p;
        
    }

    @DeleteMapping("/deleteById")
    public String deletePersonById(@RequestParam String id) {

        String hkey = "version:People:" + id;
        String ver = commands.hget(hkey, "v");
        if (ver == null) {
            return "enter valid id";
        }
        String key_p = "People:" + id + ":0";
        commands.jsonSet(key_p, "$.active", "false");
        return "Deleted People : " + id;
    }

}
