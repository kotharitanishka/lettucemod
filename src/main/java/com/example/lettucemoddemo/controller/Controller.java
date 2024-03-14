package com.example.lettucemoddemo.controller;
import org.springframework.web.bind.annotation.RestController;

import com.example.lettucemoddemo.model.Person;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import com.redis.lettucemod.json.SetMode;
import com.redis.lettucemod.search.CreateOptions;
import com.redis.lettucemod.search.Field;
import com.redis.lettucemod.search.SearchResults;

import io.lettuce.core.RedisURI;

import java.sql.Timestamp;
import java.util.Map;
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
    
    //create modules client
    RedisModulesClient client = RedisModulesClient.create(RedisURI.create("localhost",6379)); 

    //connect to redis server
    StatefulRedisModulesConnection<String, String> connection = client.connect();

    //Obtain the command API for synchronous execution.
    RedisModulesCommands<String, String> commands = connection.sync();

    @PostMapping("/new")
    public String addPerson(@RequestBody Person p, @RequestParam String user) {
        String p_json;
        if (p.getId() == null) {
            return "cannot create without id";
        }
        try {
            CreateOptions<String, String> options = CreateOptions.<String, String>builder()
                                                     .on(CreateOptions.DataType.JSON)
                                                     .prefixes("People:")
                                                     .build();
            commands.ftCreate("pix", options, Field.text("$.id").as("id").build() ,Field.text("$.name").as("name").build(), Field.numeric("$.age").as("age").build(), Field.tag("$.active").as("active").build());
            //commands.ftCreate("p", options , Field.text(null).)
        } catch (Exception e) {
            System.out.println("already exists index");
        }
        try {
            
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String time = timestamp.toString();
            p.setCreatedOn(time);
            p.setCreatedBy(user);
            p.setActive(true);
            p_json = new ObjectMapper().writeValueAsString(p);
            String pid = p.getId();    
            String key_p = "People:" + pid + ":1";
            String s = commands.jsonSet(key_p, "$", p_json , SetMode.NX);
            if (s == null) {
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
    public String getAllPerson(@RequestParam(required = false) Boolean inact) {
        SearchResults<String,String> ans;
        if (inact == null || inact == false )  {
            ans = commands.ftSearch("pidx", "@active:{true}");
        } else {
            ans = commands.ftSearch("pidx", "*");
        }
        if (ans.isEmpty() == true) {
            return "empty";
        } else {
            return ans.toString();
        } 
    }


    @GetMapping("/getById")
    public String getPersonById(@RequestParam String id , @RequestParam(required = false) Boolean inact) {
        String hkey = "version:People:" + id;
        String ver = commands.hget(hkey, "v");
        String key_p = "People:" + id + ":" + ver;
        String person = commands.jsonGet(key_p, "$");
        if (person == null ) {
            return "id not found";
        } 
        else {
            try {
                JSONArray array = new JSONArray(person);
                JSONObject object = array.getJSONObject(0); 
                Person p = new ObjectMapper().readValue(object.toString(), Person.class);
                if (p.isActive()) {
                    if (inact == null || inact == false) {
                        return person;
                    }
                    else {
                        String q = "@id:" + id ;
                        return commands.ftSearch("pix", q).toString();
                    }
                } 
                else {
                    return "user is deleted";
                }

                } catch (Exception e) {
                    System.out.println(e);
                return "error";
            }
            
        }
       
    }
 

    @GetMapping("/getByName")
    public String getPersonByName(@RequestParam String n, @RequestParam(required = false) Boolean inact) {
        
        String q;
        if (inact == null || inact == false) {
            q = "(@name:" + n + " & @active:{true})" ;
        }
        else {
            q = "@name:" + n ;
        }
        SearchResults<String , String> s = commands.ftSearch("pidx", q);
        System.out.println(n);
        System.out.println(s);
        if (s.isEmpty() == true) {
            return "name not found";
        }
        else{
            return s.toString();
        } 
    }


    @PutMapping("/updateEntireById")
    public String updatePersonFullById(@RequestParam String id ,@RequestParam String user ,@RequestBody(required = false) Map<String,Object> m) {
        String hkey = "version:People:" + id;
        String ver = commands.hget(hkey, "v");
        String key_p = "People:" + id + ":" + ver;
        Object[] keys = m.keySet().toArray();
        String p = commands.jsonGet(key_p, "$");
        try {
            JSONArray array = new JSONArray(p);
            JSONObject object = array.getJSONObject(0); 
            Person person = new ObjectMapper().readValue(object.toString(), Person.class);
            commands.jsonSet(key_p , "$.active" , "false");

            for (int i = 0 ; i < keys.length ; i ++) {
                if (keys[i] == "name") {
                    person.setName(m.get(keys[i]).toString());
                }
                else if (keys[i] == "age") {
                    int a = (Integer) m.get(keys[i]);
                    person.setAge(a);
                }
                else if (keys[i] == "id") {
                    System.out.println("cannot change key");
                }
                else {
                    person.setDetail(keys[i].toString(), m.get(keys[i]));
                }
            }

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String time = timestamp.toString();
            person.setUpdatedOn(time);
            person.setUpdatedBy(user);
            person.setActive(true);
            String p_json = new ObjectMapper().writeValueAsString(person);

            Integer version = (Integer.parseInt(ver) + 1) ;
            ver = String.valueOf(version);
            commands.hset(hkey, "v", ver);
            key_p = "People:" + id + ":" + ver;

            String updatedPerson = commands.jsonSet(key_p, "$", p_json , SetMode.NX);
            System.out.println(updatedPerson);

            return "updated";
        } catch (Exception e) {
            System.out.println(e);
            return "error , did not update";
        }   
    }

    
    @DeleteMapping("/deleteById")
    public String deletePersonById(@RequestParam String id) {
        
        String hkey = "version:People:" + id;
        String ver = commands.hget(hkey, "v");
        if (ver == null) {
            return "enter valid id";
        }
        String key_p = "People:" + id + ":" + ver;
        commands.jsonSet(key_p , "$.active" , "false");
        return "Deleted People : " + id;
    }
    
    
}
