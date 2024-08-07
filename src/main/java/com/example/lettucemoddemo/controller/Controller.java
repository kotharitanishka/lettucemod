package com.example.lettucemoddemo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.lettucemoddemo.model.Person;
import com.example.lettucemoddemo.model.UserAuth;
import com.example.lettucemoddemo.utils.JwtUtil;
import com.example.lettucemoddemo.utils.RSA;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import com.redis.lettucemod.json.SetMode;
import com.redis.lettucemod.search.CreateOptions;
import com.redis.lettucemod.search.Document;
import com.redis.lettucemod.search.Field;
import com.redis.lettucemod.search.SearchOptions;
import com.redis.lettucemod.search.SearchResults;
import com.redis.lettucemod.search.SearchOptions.SortBy;

import io.lettuce.core.RedisURI;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import okhttp3.OkHttpClient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    // @Autowired
    // StatefulRedisModulesConnection<String, String> connection;

    // RedisModulesCommands<String, String> commands;

    OkHttpClient client1 = new OkHttpClient();

    WebClient client3 = WebClient.create();

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    RSA rsa;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping("/checkIntegration")
    public String checkIntegration() {
        return "Hi working integration testing";
    }

    public CreateOptions<String, String> options3 = CreateOptions.<String, String>builder()
            .on(CreateOptions.DataType.JSON)
            .prefixes("user:")
            .build();

    @PostMapping("/newUser")
    public ResponseEntity<Map<String, Object>> newUser(@RequestBody UserAuth user) {

        ResponseEntity<Map<String, Object>> result;
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        try {
            String create = commands.ftCreate("uidx", options3, Field.text("$.id").as("id").sortable(true).build(),
                    Field.text("$.username").as("username").build());
            System.out.println(create);
        } catch (Exception e) {
            // System.out.println(e.getClass().getName());
            System.out.println("already exists index");
        }

        try {
            String decrptedPass = rsa.decrypt(user.getPassword());
            System.out.println("decryptedPass --> " + decrptedPass);
            // PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String ecrypedPassword = passwordEncoder.encode(decrptedPass);
            System.out.println("encoded password --> " + ecrypedPassword);

            user.setPassword(ecrypedPassword);
            System.out.println("users pass is -->" + user.getPassword());

            String id = user.getId();
            System.out.println("id is --> " + id);
            String jsonBody = new ObjectMapper().writeValueAsString(user);
            System.out.println("Json body is -->" + jsonBody);
            String set = commands.jsonSet("user:" + id, "$", jsonBody);
            System.out.println("jsonset is -->" + set);
        } catch (Exception e) {
            // e.printStackTrace();
            map.put("message", "could not create in redis");
            map.put("code", HttpStatus.BAD_REQUEST.value());
            result = new ResponseEntity<Map<String, Object>>(map,
                    HttpStatus.BAD_REQUEST);
            return result;

        }

        map.put("message", "done");
        map.put("created", user);
        map.put("code", HttpStatus.OK.value());
        result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        return result;

    }

    @PostMapping("/loginUser")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {

        // JwtUtil jwtUtil = new JwtUtil();
        Map<String, Object> tokens = new LinkedHashMap<String, Object>();
        ResponseEntity<Map<String, Object>> result;
        String decrptedPass;
        try {
            decrptedPass = rsa.decrypt(loginRequest.get("password"));
            UsernamePasswordAuthenticationToken unauthenticatedObject = new UsernamePasswordAuthenticationToken(
                    loginRequest.get("username"), decrptedPass);

            System.out.println("check unauthenticatedObject before auth ----> " + unauthenticatedObject);
            Authentication authenticate = authenticationManager
                    .authenticate(unauthenticatedObject);
            System.out.println("authenticate -->  : " + authenticate);
            User user = (User) authenticate.getPrincipal();
            System.out.println("this is user -->" + user);
            System.out.println("this is username --> " + user.getUsername());
            String accessToken = jwtUtil.generateAccessToken(user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
            // System.out.println("\naccess token in controller is --> " + accessToken);
            tokens.put("code", HttpStatus.OK.value());
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);
            result = new ResponseEntity<Map<String, Object>>(tokens,
                    HttpStatus.OK);
            return result;
        } catch (Exception e) {
            // e.printStackTrace();
            tokens.put("code", HttpStatus.BAD_REQUEST.value());
            tokens.put("failed", "cannot login");
            result = new ResponseEntity<Map<String, Object>>(tokens,
                    HttpStatus.BAD_REQUEST);
            System.out.println("\n\nfailed\n\n");
            return result;
        }

    }

    @PostMapping("/refreshToken")
    public ResponseEntity<Map<String, Object>> refreshToken(HttpServletResponse response, HttpServletRequest request) {

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        ResponseEntity<Map<String, Object>> result;

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        System.out.println("authheader in controller is --> " + authHeader);
        String refreshToken = null;
        String username = null;
        String accessToken = null;
        // System.out.println("checking authhead in refresh token : " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            refreshToken = authHeader.substring(7);
            System.out.println("refresh token reached here --> " + refreshToken);
            Boolean valid = jwtUtil.validateToken(refreshToken);
            System.out.println("validity --> " + valid);
            if (valid == false) {
                System.out.println("\nrefresh token is expired . login again\n");
            } else {
                username = jwtUtil.extractUsername(refreshToken);
                System.out.println("username in refreshtoken : " + username);
            }
        }
        if (username != null) {
            accessToken = jwtUtil.generateAccessToken(username);
            System.out.println("\nsuccess refresh token\n");
            map.put("code", HttpStatus.OK.value());
            map.put("accessToken", accessToken);
            result = new ResponseEntity<Map<String, Object>>(map,
                    HttpStatus.OK);
            return result;
        }
        map.put("code", HttpStatus.BAD_REQUEST.value());
        map.put("failed", "refresh token not valid");
        result = new ResponseEntity<Map<String, Object>>(map,
                HttpStatus.BAD_REQUEST);
        System.out.println("\n\nfailed\n\n");
        return result;
    }

    @PostMapping("/redisPost")
    public ResponseEntity<Map<String, Object>> redisPost() throws IOException, InterruptedException {

        ResponseEntity<Map<String, Object>> result;
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");
        long begin = System.currentTimeMillis();
        String timestampBegin = formatter.format(begin);
        System.out.println("begin time in simple format : " + timestampBegin + " and in epoch time : " + begin);

        List<Thread> threadApi = new ArrayList<>();

        for (Integer i = 1; i <= 500; i++) {

            Integer limit = 10;
            String stringURL = "https://jsonplaceholder.typicode.com/photos?_page=" + i.toString() + "&_limit="
                    + limit.toString();
            AtomicInteger index = new AtomicInteger(((i - 1) * limit) + 1);

            Runnable task = () -> {

                WebClient.ResponseSpec responseSpec = client3.get()
                        .uri(stringURL)
                        .retrieve();

                List<Map<String, Object>> responseBody = responseSpec.bodyToMono(List.class).block();

                responseBody.stream().forEach(x -> {
                    try {
                        String jsonBody = new ObjectMapper().writeValueAsString(x);
                        String id = x.get("title").toString();
                        String set = commands.jsonSet("Testing:" + id, "$", jsonBody);

                        String hkey = "keysTesting:";
                        commands.hset(hkey, index.toString(), id);
                        index.getAndIncrement();

                    } catch (JsonProcessingException e) {
                        System.out.println("Error : --> " + index);
                    }
                });
            };

            Thread myThread = new Thread(task);
            // Thread myThread = Thread.ofVirtual().unstarted(task);
            threadApi.add(myThread);

        }

        threadApi.stream().forEach(thread -> {
            thread.start();
        });
        for (Thread thread : threadApi) {
            thread.join();
            ;
        }

        long end = System.currentTimeMillis();
        String timestampEnd = formatter.format(begin);
        System.out.println("\nend time in simple format: " + timestampEnd + " and in epoch time : " + end);

        long total = end - begin;
        System.out.println("\n\ntotal time taken in seconds --> " + total / 1000.0);
        // System.out.println("Java 21 Virtual threads");
        System.out.println("Java 17 multithreading");

        map.put("message", "done");
        map.put("java version", 17);
        map.put("time taken in secs", (total / 1000.0));
        map.put("code", HttpStatus.OK.value());
        result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        return result;

    }

    @GetMapping("/redisFetch")
    public ResponseEntity<Map<String, Object>> redisFetch() throws IOException, InterruptedException {

        ResponseEntity<Map<String, Object>> result;
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");
        long begin = System.currentTimeMillis();
        String timestampBegin = formatter.format(begin);
        System.out.println("begin time in simple format : " + timestampBegin + " and in epoch time : " + begin);

        List<Thread> threadApi = new ArrayList<>();
        List<JsonNode> response = new ArrayList<>();
        List<String> stringResponse = new ArrayList<>();

        String hkey = "keysTesting:";
        Integer n = 160000;
        // String duptimes = " 6";
        for (Integer i = 1; i <= n; i++) {

            AtomicInteger index = new AtomicInteger(i);
            // AtomicInteger dupindex = new AtomicInteger(n+i);
            Runnable task = () -> {
                String oldId = commands.hget(hkey, index.toString());
                String key = "Testing:" + oldId;

                String stringJSON = commands.jsonGet(key, "$");
                stringResponse.add(stringJSON);

                try {
                    JsonNode node = (ArrayNode) new ObjectMapper().readTree(stringJSON);
                    response.add(node.get(0));

                    // String newId = (oldId + duptimes);
                    // String set = commands.jsonSet("Testing:" + newId, "$",
                    // node.get(0).toString());
                    // String dupkey = "keysTesting:";
                    // commands.hset(dupkey, dupindex.toString(), newId);
                    // dupindex.getAndIncrement();

                    index.getAndIncrement();

                } catch (Exception e) {
                    System.out.println("ERROR : string to json convert --> " + index + stringJSON);
                }
            };

            Thread myThread = new Thread(task);
            //Thread myThread = Thread.ofVirtual().unstarted(task);
            threadApi.add(myThread);

        }

        threadApi.stream().forEach(thread -> {
            thread.start();
        });
        for (Thread thread : threadApi) {
            thread.join();
        }

        long end = System.currentTimeMillis();
        String timestampEnd = formatter.format(begin);
        System.out.println("\nend time in simple format: " + timestampEnd + " and in epoch time : " + end);

        long total = end - begin;
        System.out.println("\n\ntotal time taken in seconds --> " + total / 1000.0);
        System.out.println("Java 21 Virtual threads");
        //System.out.println("Java 17 multithreading");

        map.put("message", "done");
        map.put("java version", 21);
        map.put("time taken in secs", (total / 1000.0));
        map.put("code", HttpStatus.OK.value());
        map.put("String response", stringResponse);
        map.put("response", response);
        result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        return result;

    }

    public Map<String, Object> jsonResponse(SearchResults<String, String> ans) {

        Map<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
        Stream<Document<String, String>> answerStream = ans.stream();
        jsonAnswer = answerStream.collect(Collectors.toMap(key -> (key.getId().split(":")[1]), value -> value,
                (document1, document2) -> document1));
        return jsonAnswer;

    }

    public CreateOptions<String, String> options0 = CreateOptions.<String, String>builder()
            .on(CreateOptions.DataType.JSON)
            .prefixes("People:")
            .build();

    @Tag(name = "Person Post", description = "Post data of Person")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Person added!", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Couldn't add person.", content = @Content(mediaType = "application/json")) })
    @PostMapping("/new")
    public ResponseEntity<Map<String, Object>> addPerson(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data of Person to add new", required = true) @RequestBody @Valid Person p,
            @Parameter(description = "Audit field user", required = false) @RequestParam(required = false) String user) {

        commands = connection.sync();
        String p_json;
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
            commands.ftCreate("pidx", options0, Field.text("$.id").as("id").sortable(true).build(),
                    Field.text("$.name").as("name").build(), Field.numeric("$.age").as("age").build(),
                    Field.tag("$.active0").as("active0").build());
        } catch (Exception e) {
            System.out.println(e.getClass().getName());
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
            map.put("message", "ERROR");
            map.put("error", HttpStatus.BAD_REQUEST.name());
            map.put("code", HttpStatus.BAD_REQUEST.value());
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
            return result;
            // return new ResponseEntity<String>("error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Tag(name = "Person Get", description = "GET data of Person")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Got the person list!", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Couldn't get person list.", content = @Content(mediaType = "application/json")) })
    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAllPerson(
            @Parameter(description = "Inactive User", required = false) @RequestParam(required = false) Boolean inact,
            @Parameter(description = "Page Offset", required = false) @RequestParam(required = false) Integer offset,
            @Parameter(description = "Page Limit", required = false) @RequestParam(required = false) Integer limit,
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

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Got the person!", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Couldn't get the person.", content = @Content(mediaType = "application/json")) })
    @Tag(name = "Person Get", description = "GET data of Person")
    @GetMapping("/getById")
    public ResponseEntity<Map<String, Object>> getPersonById(
            @Parameter(description = "Person Id", required = true) @RequestParam String id,
            @Parameter(description = "Minimum version for base Person", required = false) @RequestParam(required = false) Integer min,
            @Parameter(description = "Maximum versions for history", required = false) @RequestParam(required = false) Integer max) {

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
                map.put("message", "ERROR");
                map.put("error", HttpStatus.BAD_REQUEST.name());
                map.put("code", HttpStatus.BAD_REQUEST.value());
                result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
                return result;
                // return map;
            }
        }

    }

    public SearchOptions<String, String> options2 = SearchOptions.<String, String>builder()
            .sortBy(SortBy.asc("id"))
            .build();

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Got the person!", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Couldn't get the person.", content = @Content(mediaType = "application/json")) })
    @Tag(name = "Person Get", description = "GET data of Person")
    @GetMapping("/getByName")
    public ResponseEntity<Map<String, Object>> getPersonByName(
            @Parameter(description = "Name of Person you want", required = true) @RequestParam String n) {

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
            List<Person> plist;

            plist = s.stream()
                    .map(x -> x.values().toArray()[1].toString())
                    .map(x -> {
                        try {
                            return new ObjectMapper().readValue(x, Person.class);
                        } catch (Exception e) {
                            // e.printStackTrace();
                            return null;
                        }
                    })
                    .collect(Collectors.toList());
            System.out.println("\n plist by streams ==> " + plist);

            // if (plist == null) {
            // map.put("data", List.of());
            // map.put("error", HttpStatus.BAD_REQUEST.name());
            // map.put("code", HttpStatus.BAD_REQUEST.value());
            // result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            // return result;
            // }

            map.put("data", plist);
            map.put("success", HttpStatus.OK.name());
            map.put("code", HttpStatus.OK.value());
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            return result;

        }
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Person updated!", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Couldn't update person.", content = @Content(mediaType = "application/json")) })
    @Tag(name = "Person Modify", description = "Modify data of Person to update or delete")
    @PutMapping("/updateById")
    public ResponseEntity<Map<String, Object>> updatePersonById(
            @Parameter(description = "Id of Person you want to update", required = true) @RequestParam String id,
            @Parameter(description = "Audit field", required = true) @RequestParam String user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data of Person to update", required = true) @RequestBody(required = false) Map<String, Object> m) {

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

        List<java.lang.reflect.Field> fieldsList = Arrays.asList(p0.getClass().getDeclaredFields());
        Set<String> p_keys;
        p_keys = fieldsList.stream().map(x -> x.getName()).collect(Collectors.toSet());

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
                            String d_key = d[k].split("=")[0];
                            String d_val = d[k].split("=")[1];
                            path = "$.detail" + "." + d_key;
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

            String t = new ObjectMapper().writeValueAsString(time);
            commands.jsonSet(key_p0, "$.updatedOn0", t, SetMode.XX);
            delta.put("updatedOn0", time);

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
            map.put("message", "error");
            map.put("error", HttpStatus.BAD_REQUEST.value());
            map.put("code", HttpStatus.BAD_REQUEST.value());
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

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Person deleted!", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Couldn't delete person.", content = @Content(mediaType = "application/json")) })
    @Tag(name = "Person Modify", description = "Modify data of Person to update or delete")
    @DeleteMapping("/deleteById")
    public ResponseEntity<Map<String, Object>> deletePersonById(
            @Parameter(description = "Id of Person you want to delete", required = true) @RequestParam String id,
            @Parameter(description = "Audit field user", required = true) @RequestParam String user) {

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
            map.put("message", "Deleted People :" + id);
            map.put("success", HttpStatus.OK.name());
            map.put("code", HttpStatus.OK.value());
            // ResponseEntity.status(HttpStatus.OK).body(map);
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            return result;
        } catch (Exception e) {
            map.put("message", "enter valid id");
            map.put("error", HttpStatus.BAD_REQUEST.name());
            map.put("code", HttpStatus.BAD_REQUEST.value());
            result = new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
            return result;
            // return new ResponseEntity<String>("error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
