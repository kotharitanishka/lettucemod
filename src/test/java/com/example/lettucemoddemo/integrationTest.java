package com.example.lettucemoddemo;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.lettucemoddemo.controller.Controller;
import com.example.lettucemoddemo.model.Person;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import com.redis.lettucemod.search.SearchResults;

@WebMvcTest(controllers = Controller.class)
public class integrationTest {

       @Autowired
       private MockMvc mockMvc;

       Person p;

       @Test
       public void testApiGetByName() throws Exception {

              // name does not exist
              mockMvc.perform(get("/getByName")
                            .contentType("application/json")
                            .param("n", "sanika")).andExpect(status().isBadRequest())
                            .andReturn();

              // name exist
              mockMvc.perform(get("/getByName")
                            .contentType("application/json")
                            .param("n", "akshit")).andExpect(status().isOk()).andReturn();

       }

       @Test
       public void testApiGetAll() throws Exception {

              Integer l = 10;
              Integer o = 3;

              // no params given
              mockMvc.perform(get("/getAll")
                            .contentType("application/json")).andExpect(status().isOk());

              // only offset given
              mockMvc.perform(get("/getAll")
                            .contentType("application/json")
                            .param("offset", o.toString())).andExpect(status().isOk());

              // only limit given
              mockMvc.perform(get("/getAll")
                            .contentType("application/json")
                            .param("limit", l.toString())).andExpect(status().isOk());

              // inactive false given
              mockMvc.perform(get("/getAll")
                            .contentType("application/json")
                            .param("inact", "false")).andExpect(status().isOk());

              // inactive true given
              mockMvc.perform(get("/getAll")
                            .contentType("application/json")
                            .param("inact", "true")).andExpect(status().isOk());

              // limit , offset ,inact true
              mockMvc.perform(get("/getAll")
                            .contentType("application/json")
                            .param(("limit"), l.toString())
                            .param("offset", o.toString())
                            .param("inact", "true")).andExpect(status().isOk());

              // only limit , offset
              mockMvc.perform(get("/getAll")
                            .contentType("application/json")
                            .param(("limit"), l.toString())
                            .param("offset", o.toString())).andExpect(status().isOk());

       }

       @Test
       public void testApiGetById() throws Exception {

              Integer min2 = 26;
              Integer min = 3;
              Integer max = 20;
              Integer min1 = 1;

              // id does not exist
              mockMvc.perform(get("/getById")
                            .contentType("application/json")
                            .param("id", "500")).andExpect(status().isBadRequest());

              // id deleted (inactive)
              mockMvc.perform(get("/getById")
                            .contentType("application/json")
                            .param("id", "10")).andExpect(status().isBadRequest());

              // id (active) & min , max both not given
              mockMvc.perform(get("/getById")
                            .contentType("application/json")
                            .param("id", "1")).andExpect(status().isOk());

              // id (active) , min not given , max is given
              mockMvc.perform(get("/getById")
                            .contentType("application/json")
                            .param("id", "1")
                            .param("max", max.toString()))
                            .andExpect(status().isOk());

              // id (active) , min given , max is not given
              mockMvc.perform(get("/getById")
                            .contentType("application/json")
                            .param("id", "1")
                            .param("min", min2.toString()))
                            .andExpect(status().isOk());

              // id (active) , min = 1
              mockMvc.perform(get("/getById")
                            .contentType("application/json")
                            .param("id", "1")
                            .param("min", min1.toString()))
                            .andExpect(status().isOk());

              // id (active) , min not 1 , max given
              mockMvc.perform(get("/getById")
                            .contentType("application/json")
                            .param("id", "1")
                            .param("min", min.toString())
                            .param("max", max.toString()))
                            .andExpect(status().isOk());
       }

       @Test
       public void testApiUpdateById() throws Exception {

              Map<String, Object> map = new LinkedHashMap<String, Object>();
              Map<String, Object> detail = new LinkedHashMap<String, Object>();

              // id does not exist
              mockMvc.perform(put("/updateById")
                            .contentType("application/json")
                            .param("id", "99")
                            .param("user", "user2")).andExpect(status().isBadRequest());

              // id deleted (inactive)
              mockMvc.perform(put("/updateById")
                            .contentType("application/json")
                            .param("id", "7")
                            .param("user", "user2")).andExpect(status().isBadRequest());

              // delta not given
              mockMvc.perform(put("/updateById")
                            .contentType("application/json")
                            .param("id", "4")
                            .param("user", "user2")).andExpect(status().isBadRequest());

              // empty delta
              String c = new ObjectMapper().writeValueAsString(map);
              mockMvc.perform(put("/updateById")
                            .contentType("application/json")
                            .param("id", "4")
                            .param("user", "user2")
                            .content(c)).andExpect(status().isBadRequest());

              detail.put("sport", "volleyball");
              detail.put("pet", "dog");
              detail.put("petname", "bruno");
              detail.put("newdetail", "testingNX");

              map.put("name", "alistair");
              map.put("age", 20);
              map.put("id", "14");
              map.put("detail", detail);
              map.put("ps5", "fifa24");

              String u = new ObjectMapper().writeValueAsString(map);

              // working
              ResultActions r1 = mockMvc.perform(put("/updateById")
                            .contentType("application/json")
                            .param("id", "1")
                            .param("user", "user2")
                            .content(u)).andExpect(status().isOk());
              System.out.println(r1.toString());
       }

       @Test
       public void testApiAddNew() throws Exception {

              Person p = new Person();
              String u;

              // field validation
              p.setAge(2);
              p.setMobNo("12345");
              u = new ObjectMapper().writeValueAsString(p);
              mockMvc.perform(post("/new")
                            .contentType("application/json")
                            .param("user", "user2")
                            .content(u)).andExpect(status().isBadRequest());

              // user not given in param
              p.setAge(22);
              p.setMobNo("1234567890");
              u = new ObjectMapper().writeValueAsString(p);
              mockMvc.perform(post("/new")
                            .contentType("application/json")
                            .content(u)).andExpect(status().isBadRequest());

              // id not given
              u = new ObjectMapper().writeValueAsString(p);
              mockMvc.perform(post("/new")
                            .contentType("application/json")
                            .param("user", "user2")
                            .content(u)).andExpect(status().isBadRequest());

              // id already exists
              p.setId("1");
              u = new ObjectMapper().writeValueAsString(p);
              mockMvc.perform(post("/new")
                            .contentType("application/json")
                            .param("user", "user2")
                            .content(u)).andExpect(status().isBadRequest());

              // working
              p.setId("20");
              p.setName("palak");
              p.setAge(22);
              p.setMobNo("1234567890");
              p.setDob("22-01-2002");
              p.setDetail("color", "pink");
              u = new ObjectMapper().writeValueAsString(p);
              mockMvc.perform(post("/new")
                            .contentType("application/json")
                            .param("user", "user2")
                            .content(u)).andExpect(status().isOk());
       }

       @Test
       public void testApiDeleteById() throws Exception {

              // id does not exist
              mockMvc.perform(delete("/deleteById")
                            .contentType("application/json")
                            .param("id", "99")
                            .param("user", "user2")).andExpect(status().isBadRequest());

              // id already deleted (inactive)
              mockMvc.perform(delete("/deleteById")
                            .contentType("application/json")
                            .param("id", "7")
                            .param("user", "user2")).andExpect(status().isBadRequest());

              // working
              mockMvc.perform(delete("/deleteById")
                            .contentType("application/json")
                            .param("id", "137")
                            .param("user", "user2")).andExpect(status().isOk());
       }

}