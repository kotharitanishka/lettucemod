package com.example.lettucemoddemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.lettucemoddemo.model.Person;
import com.example.lettucemoddemo.model.UserAuth;
import com.fasterxml.jackson.databind.ObjectMapper;

// @ImportAutoConfiguration(SecurityConfig.class)
// @WebMvcTest(Controller.class)
@SpringBootTest
@AutoConfigureMockMvc
public class integrationTwoTest {

       @Autowired
       private MockMvc mockMvc;

       Person p;

       String newId = "30";

       @Test
       @WithMockUser(username = "acvndiovn")
       public void testApiCheckIntegration0() throws Exception {

              // name does not exist
              MvcResult result = mockMvc.perform(get("/checkIntegration")
                            .contentType(MediaType.TEXT_PLAIN_VALUE))
                            .andExpect(status().isForbidden())
                            .andReturn();

              System.out.println("\nthis is the result -->" + result.getResponse().getContentAsString());

       }

       @Test
       @WithMockUser(username = "acvndiovn", authorities = "ROLE_USER")
       public void testApiCheckIntegration1() throws Exception {

              // name does not exist
              MvcResult result = mockMvc.perform(get("/checkIntegration")
                            .contentType(MediaType.TEXT_PLAIN_VALUE))
                            .andExpect(status().isForbidden())
                            .andReturn();

              System.out.println("\nthis is the result -->" + result.getResponse().getContentAsString());

       }

       @Test
       @WithMockUser(username = "acvndiovn", authorities = "ROLE_ADMIN")
       public void testApiCheckIntegration2() throws Exception {

              // name does not exist
              MvcResult result = mockMvc.perform(get("/checkIntegration")
                            .contentType(MediaType.TEXT_PLAIN_VALUE))
                            .andExpect(status().isOk())
                            .andReturn();

              System.out.println("\nthis is the result -->" + result.getResponse().getContentAsString());

       }

       
       @Test
       @WithUserDetails("avi")
       public void testApiNewUser() throws Exception {

       String encryptedRSAPass = "Xgh8L7Aizhc5FiW/8hYYzQ3Lui2Y4dbpf2NyhtqZltGxePlJn9fyuJDovLR5hW0ANzXksxNAaJ51WIcEUz7YFtklb6T5V8FbBAHBOvsjudu/Dws0OPLv1wdU4lxTQNwBrf4HIvYysTQLMYFkUnyuPdKWjoq2hz8Uquik+BBsymNwXouUGhaRnM/0Faq54Dd6Pvz8ckC2BBgoXKxBg/Zf6INdq4P+eIYch5Rs9vmpSI1TMptxqmzPRRRB60ne94Bu++pCAUIab6zdiXu05ZSNCv2iQ5Bz36+YHJc+On/YSYVwKS1VxSsL9b73x6m43DAvpezVw966FERZ/oSESpcr5w==";
       String username = "bhavik";

       //case 0 : catch while decrypt --> pass not rsa encry 
       UserAuth user2 = new UserAuth();
       user2.setId("99");
       user2.setUsername(username);
       user2.setPassword("abc");
       user2.setAuthority("USER");

       String userString2 = new ObjectMapper().writeValueAsString(user2);

       MvcResult result2 = mockMvc.perform(post("/newUser")
       .contentType("application/json")
       .content(userString2)).andExpect(status().isBadRequest()).andReturn();

       System.out.println("\nthis is the result -->" + result2.getResponse().getContentAsString());


       // case 1 : working 
       UserAuth user1 = new UserAuth();
       user1.setId("8");
       user1.setUsername(username);
       user1.setPassword(encryptedRSAPass);
       user1.setAuthority("USER");

       String userString = new ObjectMapper().writeValueAsString(user1);

       MvcResult result = mockMvc.perform(post("/newUser")
       .contentType("application/json")
       .content(userString)).andExpect(status().isOk()).andReturn();

       System.out.println("\nthis is the result -->" + result.getResponse().getContentAsString());

       }


       @Test
       public void testApiLoginUser() throws Exception {

       String encryptedRSAPass = "Xgh8L7Aizhc5FiW/8hYYzQ3Lui2Y4dbpf2NyhtqZltGxePlJn9fyuJDovLR5hW0ANzXksxNAaJ51WIcEUz7YFtklb6T5V8FbBAHBOvsjudu/Dws0OPLv1wdU4lxTQNwBrf4HIvYysTQLMYFkUnyuPdKWjoq2hz8Uquik+BBsymNwXouUGhaRnM/0Faq54Dd6Pvz8ckC2BBgoXKxBg/Zf6INdq4P+eIYch5Rs9vmpSI1TMptxqmzPRRRB60ne94Bu++pCAUIab6zdiXu05ZSNCv2iQ5Bz36+YHJc+On/YSYVwKS1VxSsL9b73x6m43DAvpezVw966FERZ/oSESpcr5w==";
       String username = "umi";

       //case 0 : catch --> wrong password
       Map<String , String > loginCredentials0 = new HashMap<>();
       loginCredentials0.put("username", username);
       loginCredentials0.put("password", "abc");

       String userString0 = new ObjectMapper().writeValueAsString(loginCredentials0);

       MvcResult result0 = mockMvc.perform(post("/loginUser")
       .contentType("application/json")
       .content(userString0)).andExpect(status().isBadRequest()).andReturn();

       System.out.println("\nthis is the result -->" + result0.getResponse().getContentAsString() + "\n\n");


       // case 1 : working 
       Map<String , String > loginCredentials1 = new HashMap<>();
       loginCredentials1.put("username", username);
       loginCredentials1.put("password", encryptedRSAPass);

       String userString1 = new ObjectMapper().writeValueAsString(loginCredentials1);

       MvcResult result1 = mockMvc.perform(post("/loginUser")
       .contentType("application/json")
       .content(userString1)).andExpect(status().isOk()).andReturn();

       System.out.println("\n\nthis is the result -->" + result1.getResponse().getContentAsString() + "\n\n");

       }


       @Test
       public void testApiRefreshToken() throws Exception {

       //case 0 : failed --> authheader does not start with bearer
       String authHeader0 = "asnviongio";
       MvcResult result0 = mockMvc.perform(post("/refreshToken")
       .contentType("application/json")
       .header("authorization", authHeader0)).andExpect(status().isBadRequest()).andReturn();

       System.out.println("\nthis is the result -->" + result0.getResponse().getContentAsString());

       //case 1 : failed --> refresh token validity false
       String authHeader1 = "Bearer dvjsdnvjs";
       MvcResult result1 = mockMvc.perform(post("/refreshToken")
       .contentType("application/json")
       .header("authorization", authHeader1)).andExpect(status().isBadRequest()).andReturn();

       System.out.println("\nthis is the result -->" + result1.getResponse().getContentAsString());

       //case 2 : working 
       String encryptedRSAPass = "Xgh8L7Aizhc5FiW/8hYYzQ3Lui2Y4dbpf2NyhtqZltGxePlJn9fyuJDovLR5hW0ANzXksxNAaJ51WIcEUz7YFtklb6T5V8FbBAHBOvsjudu/Dws0OPLv1wdU4lxTQNwBrf4HIvYysTQLMYFkUnyuPdKWjoq2hz8Uquik+BBsymNwXouUGhaRnM/0Faq54Dd6Pvz8ckC2BBgoXKxBg/Zf6INdq4P+eIYch5Rs9vmpSI1TMptxqmzPRRRB60ne94Bu++pCAUIab6zdiXu05ZSNCv2iQ5Bz36+YHJc+On/YSYVwKS1VxSsL9b73x6m43DAvpezVw966FERZ/oSESpcr5w==";
       String username = "umi";

       Map<String , String > loginCredentials1 = new HashMap<>();
       loginCredentials1.put("username", username);
       loginCredentials1.put("password", encryptedRSAPass);

       String userString1 = new ObjectMapper().writeValueAsString(loginCredentials1);

       MvcResult result2 = mockMvc.perform(post("/loginUser")
       .contentType("application/json")
       .content(userString1)).andExpect(status().isOk()).andReturn();

       String response = result2.getResponse().getContentAsString();
       Map<String, Object> map = new ObjectMapper().readValue(response, HashMap.class);
       String refreshToken = map.get("refreshToken").toString();

       String authHeader2 = "Bearer " + refreshToken;
       MvcResult result3 = mockMvc.perform(post("/refreshToken")
       .contentType("application/json")
       .header("authorization", authHeader2)).andExpect(status().isOk()).andReturn();

       System.out.println("\nthis is the result -->" + result3.getResponse().getContentAsString());


       }


       @Test
       @WithUserDetails("avi")
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
       @WithUserDetails("avi")
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
       @WithUserDetails("umi")
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
       @WithUserDetails("avi")
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
       @WithUserDetails("avi")
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
       p.setId(newId);
       p.setName("palak");
       p.setAge(23);
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
       @WithUserDetails("avi")
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
       .param("id", newId)
       .param("user", "user2")).andExpect(status().isOk());
       }

}
