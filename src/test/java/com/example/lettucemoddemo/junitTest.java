package com.example.lettucemoddemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
//import org.mockito.Mock.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.lettucemoddemo.controller.Controller;
import com.example.lettucemoddemo.model.Person;
import com.example.lettucemoddemo.model.UserAuth;
import com.example.lettucemoddemo.utils.JwtUtil;
import com.example.lettucemoddemo.utils.RSA;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import com.redis.lettucemod.json.SetMode;
import com.redis.lettucemod.search.Document;
import com.redis.lettucemod.search.Field;
import com.redis.lettucemod.search.SearchResults;

import io.lettuce.core.RedisCommandExecutionException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class junitTest {

  @Mock
  StatefulRedisModulesConnection<String, String> connection;

  @Mock
  RedisModulesCommands<String, String> commands;

  @Spy
  RSA rsa;

  @Mock
  PasswordEncoder passwordEncoder;

  @Mock
  AuthenticationManager authenticationManager;

  @Mock
  JwtUtil jwtUtil;

  @InjectMocks
  Controller controller;

  @Test
  public void testApiNewUser() throws Exception {

    String encryptedRSAPass = "Xgh8L7Aizhc5FiW/8hYYzQ3Lui2Y4dbpf2NyhtqZltGxePlJn9fyuJDovLR5hW0ANzXksxNAaJ51WIcEUz7YFtklb6T5V8FbBAHBOvsjudu/Dws0OPLv1wdU4lxTQNwBrf4HIvYysTQLMYFkUnyuPdKWjoq2hz8Uquik+BBsymNwXouUGhaRnM/0Faq54Dd6Pvz8ckC2BBgoXKxBg/Zf6INdq4P+eIYch5Rs9vmpSI1TMptxqmzPRRRB60ne94Bu++pCAUIab6zdiXu05ZSNCv2iQ5Bz36+YHJc+On/YSYVwKS1VxSsL9b73x6m43DAvpezVw966FERZ/oSESpcr5w==";
    String decryptedRSAPass = "user@123";
    String bcryptEncodedPass = "$2a$10$en1nRzt42zZtodFgu9upvuUMo8C97ocabWRypcPFIW8DzcfzEOAg2";

    UserAuth user1 = new UserAuth();
    user1.setId("8");
    user1.setUsername("bhavik");
    user1.setPassword(encryptedRSAPass);
    user1.setAuthority("USER");

    // case 1 : working
    // set up mock behavior
    when(rsa.decrypt(user1.getPassword())).thenReturn(decryptedRSAPass);
    when(passwordEncoder.encode(decryptedRSAPass)).thenReturn(bcryptEncodedPass);
    // user1.setPassword(bcryptEncodedPass);
    String jsonBody = new ObjectMapper().writeValueAsString(user1);
    when(commands.jsonSet(eq("user:" + user1.getId()), eq("$"), any())).thenReturn("OK");
    // call method using mock object
    ResponseEntity<Map<String, Object>> result1 = controller.newUser(user1);
    System.out.println("result is -->" + result1);
    System.out.println("My json body is -->" + jsonBody);

    // check result
    assertEquals("done", result1.getBody().get("message"));

    // case 0 :catch ft create
    user1.setPassword(encryptedRSAPass);
    // set up mock behavior
    when(commands.ftCreate("uidx", controller.options3, Field.text("$.id").as("id").sortable(true).build(),
        Field.text("$.username").as("username").build()))
        .thenThrow(new RedisCommandExecutionException("catch"));

    when(rsa.decrypt(encryptedRSAPass)).thenThrow(new InvalidKeyException("catch"));

    // call method using mock object
    ResponseEntity<Map<String, Object>> result0 = controller.newUser(user1);
    System.out.println(result0);
    assertEquals("could not create in redis", result0.getBody().get("message"));

  }

  @Test
  public void testApiLoginUser() throws Exception {

    String encryptedRSAPass = "Xgh8L7Aizhc5FiW/8hYYzQ3Lui2Y4dbpf2NyhtqZltGxePlJn9fyuJDovLR5hW0ANzXksxNAaJ51WIcEUz7YFtklb6T5V8FbBAHBOvsjudu/Dws0OPLv1wdU4lxTQNwBrf4HIvYysTQLMYFkUnyuPdKWjoq2hz8Uquik+BBsymNwXouUGhaRnM/0Faq54Dd6Pvz8ckC2BBgoXKxBg/Zf6INdq4P+eIYch5Rs9vmpSI1TMptxqmzPRRRB60ne94Bu++pCAUIab6zdiXu05ZSNCv2iQ5Bz36+YHJc+On/YSYVwKS1VxSsL9b73x6m43DAvpezVw966FERZ/oSESpcr5w==";
    String decryptedRSAPass = "user@123";
    String username = "umesh";

    UserAuth user1 = new UserAuth();
    user1.setId("8");
    user1.setUsername(username);
    user1.setPassword(encryptedRSAPass);
    user1.setAuthority("USER");

    Map<String, String> loginCred = new HashMap<>();
    loginCred.put("username", username);
    loginCred.put("password", encryptedRSAPass);

    String accessToken = "eyJhbGciOiJSUzI1NiJ9.eyJSb2xlcyI6IlJPTEVfVVNFUiIsInN1YiI6InVtZXNoIiwiZXhwIjoxNzE0OTMzNDU0fQ.rX1XPNyEagP8Cfhxp-aOa_9Z3LN78ttmf6z8m4d8LL2fMHsfSHOKx2uX0N_9vfYzSnCnNdh6wfr56U8J4_8ZszVKSSGjE2_Gu6OsNeV1IVSlIzbdQhCJziBH3kFCcu2WI0w6B1j9VthVSanXzWyAxf_bjmwYxoNQr7lkS_Yj-uqTQgHgX1TFc5KdJ96D6c6fWMTyV-q8n3galOhItjJJMd4UFjY6-BXUKxKiAGgkLhHrFhp6izNWYlB4eVTqxJz6uHZNXo1EmnUL04tVa704Py-XEXbbsoVX9LdwycL8XeK7lgLeXZQYlPZcPWET2t4xN_EHXhnzXYPiIxcnmwvbPQ";
    String refreshToken = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1bWVzaCIsImV4cCI6MTcxNDkzMzY5NH0.iR5Zshsd-5P_gEDm59w4ZtsJaW7aXJM-DOaIIe7yRJfQxD-4mZ2Sp-7NHfKK8O_5MSlGqVhdeMBWFMM9Nl1WMXimprCcrqM2e0xZHOvR8WnRZ43i73ihwsvEJ8ilDq1cAXWklnCYHk7mN4XXjDHRPmK3q0ZQwruHlAshIM2DvfTP8S3aNSRj5fFubpKOwQmawAgAgOUFM2fc7-gwj7-LOyTrgeuurAI1zY0eUml1K-ZI2wyjTBCtxFKONS0e1rNtvqe33_pIghOlIRSUfqjBLi3f9Bx1h7pawOr6jpVgdL6YAbfcskrHW9ztEBltPfMhYE9Vcd_fNkD8EgkIYaVcsQ";
    Authentication authenticate = mock(Authentication.class);
    User user = mock(User.class);

    // case 1 : working
    // set up mock behavior
    when(rsa.decrypt(user1.getPassword())).thenReturn(decryptedRSAPass);
    // verify(rsa).decrypt(user1.getPassword());

    when(authenticationManager.authenticate(any())).thenReturn(authenticate);
    when(authenticate.getPrincipal()).thenReturn(user);
    when(user.getUsername()).thenReturn(username);
    when(jwtUtil.generateAccessToken(user.getUsername())).thenReturn(accessToken);
    when(jwtUtil.generateRefreshToken(user.getUsername())).thenReturn(refreshToken);
    // verify(jwtUtil).generateAccessToken("umesh");
    ResponseEntity<Map<String, Object>> result1 = controller.login(loginCred);

    System.out.println(result1);

    assertEquals(accessToken, result1.getBody().get("accessToken"));

    // case 0 : catch
    // set up mock behavior
    when(rsa.decrypt(user1.getPassword())).thenThrow(new InvalidKeyException("catch"));
    verify(rsa, times(2)).decrypt(user1.getPassword());

    ResponseEntity<Map<String, Object>> result0 = controller.login(loginCred);

    System.out.println(result0);

    assertEquals("cannot login", result0.getBody().get("failed"));

  }

  @Test
  public void testApiRefreshToken() throws Exception {

    String authHeader = "Bearer abccidsvo";
    String refreshToken = "abccidsvo";
    String accessToken = "acsoiginfg";
    String username = "umesh";

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // case 2 : failed --> authheader is null
    String authHeader1 = null;
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader1);

    ResponseEntity<Map<String, Object>> result2 = controller.refreshToken(response, request);
    System.out.println("the result is --->" + result2);

    assertEquals("refresh token not valid", result2.getBody().get("failed"));

    // case 3 : failed --> authheader is not start with bearer
    String authHeader2 = "ancdsfni";
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader2);

    ResponseEntity<Map<String, Object>> result3 = controller.refreshToken(response, request);
    System.out.println("the result is --->" + result3);

    assertEquals("refresh token not valid", result3.getBody().get("failed"));

    // case 4 : failed --> refresh token validity false
    String authHeader3 = "Bearer dvjsdnvjs";
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader3);
    when(jwtUtil.validateToken(refreshToken)).thenReturn(false);

    ResponseEntity<Map<String, Object>> result4 = controller.refreshToken(response, request);
    System.out.println("the result is --->" + result4);

    assertEquals("refresh token not valid", result4.getBody().get("failed"));

    // case 0 : working
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
    when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
    when(jwtUtil.extractUsername(refreshToken)).thenReturn(username);
    when(jwtUtil.generateAccessToken(username)).thenReturn(accessToken);

    ResponseEntity<Map<String, Object>> result1 = controller.refreshToken(response, request);
    System.out.println("the result is --->" + result1);

    assertEquals(accessToken, result1.getBody().get("accessToken"));

  }

  @Test
  public void testApiGetByName() throws Exception {

    Person person1 = new Person();
    person1.setId("1000");
    person1.setName("tk");
    person1.setAge(22);
    person1.setactive0(true);

    Person person2 = new Person();
    person2.setId("1001");
    person2.setName("tk");
    person2.setAge(25);
    person2.setactive0(true);

    String jsonPerson1 = new ObjectMapper().writeValueAsString(person1);
    String jsonPerson2 = new ObjectMapper().writeValueAsString(person2);

    when(connection.sync()).thenReturn(commands);

    // case 0 : name does not exist --> searchresults null
    String query0;
    String name0 = "xyz";
    query0 = "(@name:" + name0 + " & @active0:{true})";
    // set up mock behavior
    when(commands.ftSearch("pidx", query0, controller.options2)).thenReturn(null);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result0 = controller.getPersonByName("xyz");
    System.out.println(result0);
    // verify that mock behavior was called
    verify(commands).ftSearch("pidx", query0, controller.options2);
    // check result
    assertEquals(400, result0.getBody().get("code"));

    // case 1 : name does not exist --> searchresults empty
    String query1;
    String name1 = "abc";
    query1 = "(@name:" + name1 + " & @active0:{true})";
    SearchResults<String, String> searchResults1 = new SearchResults<>();
    // set up mock behavior
    when(commands.ftSearch("pidx", query1, controller.options2)).thenReturn(searchResults1);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result1 = controller.getPersonByName("abc");
    System.out.println(result1);
    // verify that mock behavior was called
    verify(commands).ftSearch("pidx", query1, controller.options2);
    // check result
    assertEquals(400, result1.getBody().get("code"));

    // case 3 : catch
    String query3;
    String name3 = "pqr";
    query3 = "(@name:" + name3 + " & @active0:{true})";
    SearchResults<String, String> searchResults3 = new SearchResults<>();
    Document<String, String> document3 = new Document<String, String>();
    document3.put("id", "1000");
    document3.put("$", "testing");

    searchResults3.add(document3);
    // set up mock behavior
    when(commands.ftSearch("pidx", query3, controller.options2)).thenReturn(searchResults3);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result3 = controller.getPersonByName("pqr");
    System.out.println(result3);
    // verify that mock behavior was called
    verify(commands).ftSearch("pidx", query3, controller.options2);
    // check result
    assertEquals(400, result1.getBody().get("code"));

    // case 4: name exist
    String query2;
    String name2 = "tk";
    query2 = "(@name:" + name2 + " & @active0:{true})";
    SearchResults<String, String> searchResults2 = new SearchResults<String, String>();

    // searchResults2 = personlist;
    Document<String, String> document1 = new Document<String, String>();
    document1.put("id", "1000");
    document1.put("$", jsonPerson1);

    Document<String, String> document2 = new Document<String, String>();
    document2.put("id", "1001");
    document2.put("$", jsonPerson2);

    searchResults2.add(document1);
    searchResults2.add(document2);

    // set up mock behavior
    when(commands.ftSearch("pidx", query2, controller.options2)).thenReturn(searchResults2);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result2 = controller.getPersonByName("tk");
    System.out.println(result2);

    List<Object> personList = new ArrayList<Object>();
    System.out.println("result 2 -->" + result2.getBody().get("data"));
    // personList = Arrays.asList(result2.getBody().get("data"));
    // System.out.println("clas ==> "+personList.get(0).getClass());

    // verify that mock behavior was called
    verify(commands).ftSearch("pidx", query2, controller.options2);
    // check result
    assertEquals("OK", result2.getBody().get("success"));

  }

  @Test
  public void testApiDeleteById() throws Exception {

    Person person1 = new Person();
    person1.setId("1000");
    person1.setName("tk");
    person1.setAge(22);
    person1.setactive0(true);

    Person person2 = new Person();
    person2.setId("8888");
    person2.setName("abc");
    person2.setAge(27);
    person2.setactive0(false);

    List<Person> personList1 = new ArrayList<Person>();
    personList1.add(person1);

    List<Person> personList2 = new ArrayList<Person>();
    personList2.add(person2);

    when(connection.sync()).thenReturn(commands);

    // case 1 : id does not exist
    String hashKey1 = "version:People:" + "9999";
    // set up mock behavior
    when(commands.hget(hashKey1, "v")).thenReturn(null);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result1 = controller.deletePersonById("9999", "user2");
    System.out.println(result1);
    // verify that mock behavior was called
    verify(commands).hget(hashKey1, "v");
    // check result
    assertEquals("enter valid id", result1.getBody().get("message"));

    // case 2 : catch keytoperson
    String hashKey4 = "version:People:" + "1000";
    String key4 = "People:1000:0";
    String jsonPerson4 = new ObjectMapper().writeValueAsString(person1);
    // set up mock behavior
    when(commands.hget(hashKey4, "v")).thenReturn("5");
    when(commands.jsonGet(key4, "$")).thenReturn(jsonPerson4);

    // call method using mock object
    ResponseEntity<Map<String, Object>> result4 = controller.deletePersonById("1000", "user2");
    System.out.println(result4);
    // verify that mock behavior was called
    verify(commands).hget(hashKey4, "v");
    // check result
    assertEquals(400, result4.getBody().get("code"));

    // case 4 : catch delete
    String hashKey5 = "version:People:" + "1000";
    String key5 = "People:1000:0";
    String jsonPerson5 = new ObjectMapper().writeValueAsString(personList1);
    // set up mock behavior
    when(commands.hget(hashKey5, "v")).thenReturn("abc");
    when(commands.jsonGet(key5, "$")).thenReturn(jsonPerson5);

    // call method using mock object
    ResponseEntity<Map<String, Object>> result5 = controller.deletePersonById("1000", "user2");
    System.out.println(result5);
    // verify that mock behavior was called
    verify(commands, times(2)).hget(hashKey5, "v");
    // check result
    assertEquals(400, result5.getBody().get("code"));

    // case 5 : id already deleted
    String hashKey2 = "version:People:" + "8888";
    String key2 = "People:8888:0";
    String jsonPerson2 = new ObjectMapper().writeValueAsString(personList2);

    when(commands.hget(hashKey2, "v")).thenReturn("20");
    when(commands.jsonGet(key2, "$")).thenReturn(jsonPerson2);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result2 = controller.deletePersonById("8888", "user2");
    System.out.println(result2);
    // verify that mock behavior was called
    verify(commands).hget(hashKey2, "v");
    // check result
    assertEquals("enter valid id", result2.getBody().get("message"));

    // case 6 : working
    String hashKey3 = "version:People:" + "1000";
    String key3 = "People:1000:0";
    String jsonPerson3 = new ObjectMapper().writeValueAsString(personList1);
    // set up mock behavior
    when(commands.hget(hashKey3, "v")).thenReturn("5");
    when(commands.jsonGet(key3, "$")).thenReturn(jsonPerson3);

    // call method using mock object
    ResponseEntity<Map<String, Object>> result3 = controller.deletePersonById("1000", "user2");
    System.out.println(result3);
    // verify that mock behavior was called
    verify(commands, times(3)).hget(hashKey3, "v");
    // check result
    assertEquals("Deleted People :1000", result3.getBody().get("message"));
  }

  @Test
  public void testApiAddNew() throws Exception {

    Person person1 = new Person();
    person1.setId("1000");
    person1.setName("tk");
    person1.setAge(22);

    Person person3 = new Person();
    person3.setId("3000");
    person3.setName("xyz");
    person3.setAge(27);

    Person person4 = new Person();
    person4.setId("7000");
    person4.setName("pqr");
    person4.setAge(29);

    Person person2 = new Person();
    person2.setName("abc");
    person2.setAge(25);

    when(connection.sync()).thenReturn(commands);

    // case 0 : audit field user empty
    // call method using mock object
    ResponseEntity<Map<String, Object>> result0 = controller.addPerson(person2, "");
    System.out.println(result0);
    // check result
    assertEquals("ERROR : enter audit field user", result0.getBody().get("message"));

    // case 1 : audit field not given
    // call method using mock object
    ResponseEntity<Map<String, Object>> result1 = controller.addPerson(person2, null);
    System.out.println(result1);
    // check result
    assertEquals("ERROR : enter audit field user", result1.getBody().get("message"));

    // case 2 : id not given
    // call method using mock object
    ResponseEntity<Map<String, Object>> result2 = controller.addPerson(person2, "user2");
    System.out.println(result2);
    // check result
    assertEquals("cannot create without id", result2.getBody().get("message"));

    // case 5 :catch ft create
    String key5 = "People:7000:0";
    // set up mock behavior
    when(commands.ftCreate(("pidx"), (controller.options0), Field.text("$.id").as("id").sortable(true).build(),
        Field.text("$.name").as("name").build(), Field.numeric("$.age").as("age").build(),
        Field.tag("$.active0").as("active0").build()))
        .thenThrow(new RedisCommandExecutionException("catch"));
    when(commands.jsonSet(eq(key5), eq("$"), anyString(), eq(SetMode.NX)))
        .thenThrow(new RedisCommandExecutionException("catch"));
    // call method using mock object
    ResponseEntity<Map<String, Object>> result5 = controller.addPerson(person4, "user2");
    System.out.println(result5);
    // verify that mock behavior was called
    // verify(commands).ftCreate(eq("pidx"), eq(controller.options0), any(), any(),
    // any());
    // check result
    assertEquals("ERROR", result5.getBody().get("message"));

    // case 3 : id already exists
    String key1 = "People:1000:1";
    // set up mock behavior
    when(commands.jsonSet(eq(key1), eq("$"), anyString(), eq(SetMode.NX))).thenReturn(null);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result3 = controller.addPerson(person1, "user2");
    System.out.println(result3);
    // verify that mock behavior was called
    verify(commands).jsonSet(eq(key1), eq("$"), anyString(), eq(SetMode.NX));
    // check result
    assertEquals("id already exists", result3.getBody().get("message"));

    // case 4 : working
    String key2 = "People:3000:1";
    person3.setcreatedBy0("user2");
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String time = timestamp.toString();
    person3.setcreatedOn0(time);
    // String jsonPerson1 = new ObjectMapper().writeValueAsString(person3);
    // set up mock behavior
    when(commands.jsonSet(eq(key2), eq("$"), anyString(), eq(SetMode.NX))).thenReturn("OK");
    // call method using mock object
    ResponseEntity<Map<String, Object>> result4 = controller.addPerson(person3, "user2");
    System.out.println(result4);
    // verify that mock behavior was called
    verify(commands).jsonSet(eq(key2), eq("$"), anyString(), eq(SetMode.NX));
    // check result
    assertEquals("created new person " + person3.getId(), result4.getBody().get("message"));

  }

  @Test
  public void testApiUpdateById() throws Exception {

    Map<String, Object> map = new LinkedHashMap<String, Object>();
    Map<String, Object> detail = new LinkedHashMap<String, Object>();

    Person person1 = new Person();
    person1.setId("1000");
    person1.setName("tk");
    person1.setAge(22);
    person1.setDetail("sport", "cricket");
    person1.setactive0(true);

    List<Person> personList1 = new ArrayList<Person>();
    personList1.add(person1);

    Person person2 = new Person();
    person2.setId("9000");
    person2.setName("abc");
    person2.setAge(22);
    person2.setactive0(true);

    Person person3 = new Person();
    person3.setId("5000");
    person3.setName("xyz");
    person3.setAge(22);
    person3.setactive0(false);

    List<Person> personList2 = new ArrayList<Person>();
    personList2.add(person3);

    when(connection.sync()).thenReturn(commands);

    // case 1.0 : catch
    String personKey00 = "People:5000:0";
    String jsonPerson00 = new ObjectMapper().writeValueAsString(personList2);
    // set up mock behavior
    when(commands.jsonGet(personKey00, "$")).thenReturn(jsonPerson00);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result00 = controller.updatePersonById("5000", "user2", null);
    System.out.println(result00);
    // verify that mock behavior was called
    verify(commands).jsonGet(personKey00, "$");
    // check result
    assertEquals("enter relevant id",
        result00.getBody().get("message"));

    // case 1 : already deleted
    String personKey1 = "People:8000:0";
    // set up mock behavior
    when(commands.jsonGet(personKey1, "$")).thenReturn(null);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result1 = controller.updatePersonById("8000", "user2", null);
    System.out.println(result1);
    // verify that mock behavior was called
    verify(commands).jsonGet(personKey1, "$");
    // check result
    assertEquals("enter relevant id", result1.getBody().get("message"));

    // case 1.0 : catch
    String personKey0 = "People:5000:0";
    String jsonPerson0 = new ObjectMapper().writeValueAsString(personList1);
    map.put("data", "test");
    // set up mock behavior
    when(commands.jsonGet(personKey0, "$")).thenReturn(jsonPerson0);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result0 = controller.updatePersonById("5000", "user2", map);
    System.out.println(result0);
    // verify that mock behavior was called
    verify(commands, times(2)).jsonGet(personKey0, "$");
    // check result
    assertEquals("error",
        result0.getBody().get("message"));

    // case 2.0 : delta empty
    String personKey2 = "People:5000:0";
    String jsonPerson2 = new ObjectMapper().writeValueAsString(personList1);
    // set up mock behavior
    when(commands.jsonGet(personKey2, "$")).thenReturn(jsonPerson2);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result2 = controller.updatePersonById("5000", "user2", map);
    System.out.println(result2);
    // verify that mock behavior was called
    verify(commands, times(3)).jsonGet(personKey2, "$");
    // check result
    assertEquals("enter relevant data to update",
        result2.getBody().get("message"));

    // case 2.1 : delta null
    String personKey4 = "People:5000:0";
    String jsonPerson4 = new ObjectMapper().writeValueAsString(personList1);
    // set up mock behavior
    when(commands.jsonGet(personKey4, "$")).thenReturn(jsonPerson4);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result4 = controller.updatePersonById("5000", "user2", null);
    System.out.println(result4);
    // verify that mock behavior was called
    verify(commands, times(4)).jsonGet(personKey4, "$");
    // check result
    assertEquals("enter relevant data to update",
        result2.getBody().get("message"));

    // case 3 : working
    String personKey3 = "People:1000:0";
    String jsonPerson3 = new ObjectMapper().writeValueAsString(personList1);

    when(commands.jsonGet(personKey3, "$")).thenReturn(jsonPerson3);

    detail.put("sport", "volleyball");
    detail.put("pet", "dog");

    map.put("id", "14");
    map.put("detail", detail);
    map.put("name", "alistair");
    map.put("age", 20);
    map.put("ps5", "fifa24");

    // String jsonMap = new ObjectMapper().writeValueAsString(map);

    Map<String, Object> delta = new LinkedHashMap<String, Object>();
    delta.put("data", map);

    String user = new ObjectMapper().writeValueAsString("user2");
    when(commands.jsonSet(personKey3, "$.updatedBy0", user, SetMode.XX)).thenReturn("OK");
    delta.put("updatedBy0", user);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String time = timestamp.toString();
    String jsonTime = new ObjectMapper().writeValueAsString(time);
    when(commands.jsonSet(personKey3, "$.updatedOn0", jsonTime, SetMode.XX)).thenReturn("OK");
    delta.put("updatedOn0", time);
    // verify(commands).jsonSet(personKey3, "$.updatedOn0", jsonTime, SetMode.XX);

    String hashKey1 = "version:People:1000";
    when(commands.hget(hashKey1, "v")).thenReturn("5");
    when(commands.hset(hashKey1, "v", "6")).thenReturn(true);
    String deltaKey = "People:1000:6";

    String value = new ObjectMapper().writeValueAsString(delta);
    when(commands.jsonSet(deltaKey, "$", value, SetMode.NX)).thenReturn("OK");

    // set up mock behavior

    // call method using mock object
    ResponseEntity<Map<String, Object>> result3 = controller.updatePersonById("1000", "user2", map);
    System.out.println(result3);
    // verify that mock behavior was called
    verify(commands).jsonGet(personKey3, "$");
    // check result
    assertEquals("updated", result3.getBody().get("message"));

  }

  @Test
  public void testApiGetAll() throws Exception {

    when(connection.sync()).thenReturn(commands);

    // case 0 : get all empty list
    // SearchOptions<String, String> options0 = SearchOptions.<String,
    // String>builder()
    // .limit(0, 5)
    // .returnFields("name")
    // .sortBy(SortBy.asc("id"))
    // .build();

    SearchResults<String, String> searchResults0 = new SearchResults<>();
    // set up mock behavior
    when(commands.ftSearch(eq("pidx"), eq("@active0:{true}"), any())).thenReturn(searchResults0);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result0 = controller.getAllPerson(null, null, null, null);
    System.out.println(result0);
    // verify that mock behavior was called
    verify(commands).ftSearch(eq("pidx"), eq("@active0:{true}"), any());
    // check result
    assertEquals(400, result0.getBody().get("code"));

    // case 1 : get all empty list
    // SearchOptions<String, String> options1 = SearchOptions.<String,
    // String>builder()
    // .limit(0, 5)
    // .returnFields("name")
    // .sortBy(SortBy.asc("id"))
    // .build();

    // set up mock behavior
    when(commands.ftSearch(eq("pidx"), eq("@active0:{true}"), any())).thenReturn(null);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result1 = controller.getAllPerson(null, null, null, null);
    System.out.println(result1);
    // verify that mock behavior was called
    verify(commands, times(2)).ftSearch(eq("pidx"), eq("@active0:{true}"), any());
    // check result
    assertEquals(400, result1.getBody().get("code"));

    Person person1 = new Person();
    person1.setId("1000");
    person1.setName("tk");
    person1.setAge(22);
    person1.setactive0(true);

    Person person2 = new Person();
    person2.setId("1001");
    person2.setName("abc");
    person2.setAge(25);
    person2.setactive0(true);

    Person person3 = new Person();
    person3.setId("5000");
    person3.setName("xyz");
    person3.setAge(22);
    person3.setactive0(false);

    // case 2 : active only , no limit , no offset
    // SearchOptions<String, String> options2 = SearchOptions.<String,
    // String>builder()
    // .limit(0, 5)
    // .returnFields("name")
    // .sortBy(SortBy.asc("id"))
    // .build();

    SearchResults<String, String> searchResults1 = new SearchResults<>();

    // searchResults1 = personlist;
    Document<String, String> document1 = new Document<String, String>();
    document1.put("name", person1.getName());
    document1.setId("People:1000:0");

    Document<String, String> document2 = new Document<String, String>();
    document2.put("name", person2.getName());
    document2.setId("People:1001:0");

    searchResults1.add(document1);
    searchResults1.add(document2);

    // set up mock behavior
    when(commands.ftSearch(eq("pidx"), eq("@active0:{true}"), any())).thenReturn(searchResults1);
    System.out.println(searchResults1);
    MockHttpServletResponse response = new MockHttpServletResponse();
    // call method using mock object
    ResponseEntity<Map<String, Object>> result2 = controller.getAllPerson(false, null, null, response);
    System.out.println(result2);
    // verify that mock behavior was called
    verify(commands, times(3)).ftSearch(eq("pidx"), eq("@active0:{true}"), any());
    // check result
    assertEquals(200, result2.getBody().get("code"));

    // inact true
    SearchResults<String, String> searchResults2 = new SearchResults<>();

    Document<String, String> document3 = new Document<String, String>();
    document3.put("name", person3.getName());
    document3.setId("People:5000:0");

    searchResults2.add(document1);
    searchResults2.add(document2);
    searchResults2.add(document3);

    // SearchOptions<String, String> options3 = SearchOptions.<String,
    // String>builder()
    // .limit(0, 5)
    // .returnFields("name")
    // .sortBy(SortBy.asc("id"))
    // .build();

    // set up mock behavior
    when(commands.ftSearch(eq("pidx"), eq("*"), any())).thenReturn(searchResults2);
    System.out.println(searchResults2);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result3 = controller.getAllPerson(true, null, null, response);
    System.out.println(result3);
    // verify that mock behavior was called
    verify(commands).ftSearch(eq("pidx"), eq("*"), any());
    // check result
    assertEquals(200, result3.getBody().get("code"));

    // limit = 1, offset = 1
    SearchResults<String, String> searchResults3 = new SearchResults<>();

    Document<String, String> document4 = new Document<String, String>();
    document4.put("name", person3.getName());
    document4.setId("People:5000:0");

    searchResults3.add(document2);
    searchResults3.setCount(3);

    // SearchOptions<String, String> options4 = SearchOptions.<String,
    // String>builder()
    // .limit(1, 1)
    // .returnFields("name")
    // .sortBy(SortBy.asc("id"))
    // .build();

    // set up mock behavior
    when(commands.ftSearch(eq("pidx"), eq("*"), any())).thenReturn(searchResults3);
    System.out.println(searchResults3);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result4 = controller.getAllPerson(true, 1, 1, response);
    System.out.println(result4);
    // verify that mock behavior was called
    verify(commands, times(2)).ftSearch(eq("pidx"), eq("*"), any());
    // check result
    assertEquals(200, result4.getBody().get("code"));

  }

  @Test
  public void testApiGetById() throws Exception {

    Person person1 = new Person();
    person1.setId("1000");
    person1.setName("tk");
    person1.setAge(22);
    person1.setDetail("sport", "cricket");
    person1.setactive0(true);

    Map<String, Object> map1 = new LinkedHashMap<String, Object>();
    Map<String, Object> delta1 = new LinkedHashMap<String, Object>();
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String time = timestamp.toString();

    map1.put("name", "tanishka");
    delta1.put("data", map1);
    delta1.put("updatedBy0", "user2");
    delta1.put("updatedOn0", time);

    Map<String, Object> map2 = new LinkedHashMap<String, Object>();
    Map<String, Object> detail2 = new LinkedHashMap<String, Object>();
    Map<String, Object> delta2 = new LinkedHashMap<String, Object>();
    Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
    String time2 = timestamp2.toString();

    detail2.put("sport", "football");
    detail2.put("pet", "dog");
    map2.put("age", 24);
    map2.put("mobNo", "1234567890");
    map2.put("dob", "18-03-2000");
    map2.put("detail", detail2);
    delta2.put("data", map2);
    delta2.put("updatedBy0", "user3");
    delta2.put("updatedOn0", time2);

    Person person0 = person1;
    person0.setName("tanishka");
    person0.setAge(24);
    person0.setMobNo("1234567890");
    person0.setDob("18-03-2000");
    person0.setDetail("sport", "football");
    person0.setDetail("pet", "dog");
    person0.setupdatedBy0("user3");
    person0.setupdatedOn0(time2);

    List<Map<String, Object>> deltaList1 = new ArrayList<Map<String, Object>>();
    deltaList1.add(delta1);
    String jsonDelta1 = new ObjectMapper().writeValueAsString(deltaList1);

    List<Map<String, Object>> deltaList2 = new ArrayList<Map<String, Object>>();
    deltaList2.add(delta2);
    String jsonDelta2 = new ObjectMapper().writeValueAsString(deltaList2);

    List<Person> personList1 = new ArrayList<Person>();
    personList1.add(person1);
    String jsonPerson1 = new ObjectMapper().writeValueAsString(personList1);

    List<Person> personList0 = new ArrayList<Person>();
    personList0.add(person0);
    String jsonPerson0 = new ObjectMapper().writeValueAsString(personList0);

    Person person2 = new Person();
    person2.setId("5000");
    person2.setName("xyz");
    person2.setAge(22);
    person2.setactive0(false);

    List<Person> personList3 = new ArrayList<Person>();
    personList3.add(person2);
    String jsonPerson3 = new ObjectMapper().writeValueAsString(personList3);

    when(connection.sync()).thenReturn(commands);

    // case 1: id does not exist
    String personKey1 = "People:1:0";
    // set up mock behavior
    when(commands.jsonGet(personKey1, "$")).thenReturn(null);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result1 = controller.getPersonById("1", null, null);
    System.out.println(result1);
    // verify that mock behavior was called
    verify(commands).jsonGet(personKey1, "$");
    // check result
    assertEquals("enter valid id", result1.getBody().get("message"));

    // case 2: min null max null
    String personKey2 = "People:1000:0";
    // set up mock behavior
    when(commands.jsonGet(personKey2, "$")).thenReturn(jsonPerson0);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result2 = controller.getPersonById("1000", null, null);
    System.out.println(result2);
    // verify that mock behavior was called
    verify(commands).jsonGet(personKey2, "$");
    // check result
    assertEquals(200, result2.getBody().get("code"));

    // case 7: catch
    String personKey10 = "People:1000:0";
    String personKey11 = "People:1000:1";
    String deltaKey4 = "People:1000:2";

    // set up mock behavior
    when(commands.jsonGet(personKey10, "$")).thenReturn(jsonPerson0);
    when(commands.jsonGet(personKey11, "$")).thenReturn(jsonPerson1);
    when(commands.jsonGet(deltaKey4, "$")).thenReturn(null);

    // delta_v = commands.jsonGet(key_v, "$");
    // call method using mock object
    ResponseEntity<Map<String, Object>> result7 = controller.getPersonById("1000", null, 2);
    System.out.println(result7);
    // verify that mock behavior was called
    verify(commands, times(2)).jsonGet(personKey10, "$");
    // check result
    assertEquals(400, result7.getBody().get("code"));

    // case 3: min null max 2
    String personKey3 = "People:1000:0";
    String personKey4 = "People:1000:1";
    String deltaKey1 = "People:1000:2";

    // set up mock behavior
    when(commands.jsonGet(personKey3, "$")).thenReturn(jsonPerson0);
    when(commands.jsonGet(personKey4, "$")).thenReturn(jsonPerson1);
    when(commands.jsonGet(deltaKey1, "$")).thenReturn(jsonDelta1);

    // delta_v = commands.jsonGet(key_v, "$");
    // call method using mock object
    ResponseEntity<Map<String, Object>> result3 = controller.getPersonById("1000", null, 2);
    System.out.println(result3);
    // verify that mock behavior was called
    verify(commands, times(3)).jsonGet(personKey3, "$");
    // check result
    assertEquals(200, result3.getBody().get("code"));

    // case 4: min 1 AND max null
    String personKey5 = "People:1000:0";
    String personKey6 = "People:1000:1";
    String deltaKey2 = "People:1000:2";
    String deltaKey3 = "People:1000:3";
    String hashKey2 = "version:People:1000";
    // set up mock behavior
    when(commands.hget(hashKey2, "v")).thenReturn("3");
    when(commands.jsonGet(personKey5, "$")).thenReturn(jsonPerson0);
    when(commands.jsonGet(personKey6, "$")).thenReturn(jsonPerson1);
    when(commands.jsonGet(deltaKey2, "$")).thenReturn(jsonDelta1);
    when(commands.jsonGet(deltaKey3, "$")).thenReturn(jsonDelta2);

    // call method using mock object
    ResponseEntity<Map<String, Object>> result4 = controller.getPersonById("1000", 1, null);
    System.out.println(result4);
    // verify that mock behavior was called
    verify(commands, times(4)).jsonGet(personKey5, "$");
    // check result
    assertEquals(200, result4.getBody().get("code"));

    // case 5: min 2 AND max 3
    String personKey7 = "People:1000:0";
    String personKey8 = "People:1000:1";
    String deltaMaxKey3 = "People:1000:3";
    String deltaMinKey = "People:1000:2";
    // set up mock behavior
    when(commands.jsonGet(personKey7, "$")).thenReturn(jsonPerson0);
    when(commands.jsonGet(personKey8, "$")).thenReturn(jsonPerson1);
    when(commands.jsonGet(deltaMinKey, "$")).thenReturn(jsonDelta1);
    when(commands.jsonGet(deltaMaxKey3, "$")).thenReturn(jsonDelta2);

    // call method using mock object
    ResponseEntity<Map<String, Object>> result5 = controller.getPersonById("1000", 2, 3);
    System.out.println(result5.getBody().get("base"));
    Object testOutput = result5.getBody().get("base");

    ObjectMapper mapper = new ObjectMapper();
    Person checkPerson = mapper.convertValue(testOutput, Person.class);
    System.out.println(checkPerson.getName());

    // verify that mock behavior was called
    verify(commands, times(5)).jsonGet(personKey5, "$");
    // check result
    assertEquals("tanishka", checkPerson.getName());

    // case 7: min 3 AND max null
    String personKey12 = "People:1000:0";
    String personKey13 = "People:1000:1";
    String deltaMinKey1 = "People:1000:2";
    String deltaMinKey2 = "People:1000:3";
    // set up mock behavior
    when(commands.jsonGet(personKey12, "$")).thenReturn(jsonPerson0);
    when(commands.jsonGet(personKey13, "$")).thenReturn(jsonPerson1);
    when(commands.jsonGet(deltaMinKey1, "$")).thenReturn(jsonDelta1);
    when(commands.jsonGet(deltaMinKey2, "$")).thenReturn(jsonDelta2);

    // call method using mock object
    ResponseEntity<Map<String, Object>> result8 = controller.getPersonById("1000", 3, null);
    System.out.println(result8.getBody().get("base"));
    Object testOutput2 = result8.getBody().get("base");

    Person checkPerson2 = mapper.convertValue(testOutput2, Person.class);
    System.out.println(checkPerson2.getAge());

    // verify that mock behavior was called
    verify(commands, times(6)).jsonGet(personKey5, "$");
    // check result
    assertEquals(24, checkPerson.getAge());

    // case 6: id deleted (inactive)
    String personKey9 = "People:5000:0";
    // set up mock behavior
    when(commands.jsonGet(personKey9, "$")).thenReturn(jsonPerson3);
    // call method using mock object
    ResponseEntity<Map<String, Object>> result6 = controller.getPersonById("5000", null, null);
    System.out.println(result6);
    // verify that mock behavior was called
    verify(commands).jsonGet(personKey9, "$");
    // check result
    assertEquals("enter valid id", result1.getBody().get("message"));

  }

}