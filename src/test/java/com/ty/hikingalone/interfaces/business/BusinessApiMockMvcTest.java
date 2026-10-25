package com.ty.hikingalone.interfaces.business;

import com.ty.hikingalone.domain.user.entity.UserAccount;
import com.ty.hikingalone.domain.user.repository.UserAccountRepository;
import com.ty.hikingalone.domain.user.service.PasswordHasher;
import com.ty.hikingalone.domain.user.service.RememberTokenStore;
import com.ty.hikingalone.testsupport.InMemoryRememberTokenStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 代表性业务接口 MockMvc 集成测试（真实 MySQL + 拦截器登录态）：
 * /train 计划归属校验（他人计划一律「不存在」，不泄露存在性）、
 * /journal 参数校验的 400 语义（HTTP 状态码与 body code 一致——验证全局异常处理约定）。
 */
@SpringBootTest
@AutoConfigureMockMvc
class BusinessApiMockMvcTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private PasswordHasher passwordHasher;

    @TestConfiguration
    static class TokenStoreOverride {
        @Bean
        @Primary
        RememberTokenStore inMemoryRememberTokenStore() {
            return new InMemoryRememberTokenStore();
        }
    }

    private static final String PASSWORD = "pw-123456";

    /** 建用户并返回已登录会话 */
    private MockHttpSession loggedInSession() throws Exception {
        String email = "biz-" + System.nanoTime() + "@integration.test";
        userAccountRepository.save(
                UserAccount.register("biz" + System.nanoTime(), PASSWORD, email, passwordHasher));
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/user/login").session(session).contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk());
        return session;
    }

    private static final String PLAN_BODY = """
            {"title":"集成测试计划","description":"MockMvc 建的","startDate":"2026-10-01",
             "endDate":"2026-12-31","cycleType":0,"cycleAnchor":null,
             "items":[{"name":"深蹲","mode":"sets","totalTimes":30,"totalSets":3,"unit":"组"}]}
            """;

    @Test
    void planInvisibleToOtherUser() throws Exception {
        MockHttpSession owner = loggedInSession();
        MockHttpSession stranger = loggedInSession();

        MvcResult created = mockMvc.perform(post("/train/plans").session(owner)
                        .contentType(APPLICATION_JSON).content(PLAN_BODY))
                .andExpect(status().isOk())
                .andReturn();
        // POST /train/plans 的 data 即新计划 id（number）
        long planId = ((Number) com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.data")).longValue();

        // 本人可见
        mockMvc.perform(get("/train/plans/" + planId).session(owner)).andExpect(status().isOk());
        // 他人访问：一律「训练计划不存在」（不泄露存在性），HTTP 400 + body code 400
        mockMvc.perform(get("/train/plans/" + planId).session(stranger))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("训练计划不存在"));
    }

    @Test
    void journalValidationReturns400Semantics() throws Exception {
        MockHttpSession session = loggedInSession();

        // 缺必填字段：@Valid 失败 → HTTP 400 + body code 400 + 具体字段消息
        mockMvc.perform(post("/journal").session(session).contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                // 具体消息取决于字段校验顺序（当前为「心情不能为空」），断言非空以免脆弱
                .andExpect(jsonPath("$.message").isNotEmpty());

        // 路径参数类型错误：/journal/{id} 传非数字 → 400 参数类型错误
        mockMvc.perform(get("/journal/abc").session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        // 合法创建 → 200，列表可见
        mockMvc.perform(post("/journal").session(session).contentType(APPLICATION_JSON).content("""
                        {"title":"集成测试日记","hikeDate":"2026-10-25","difficulty":3,"mood":4,
                         "distanceKm":8.5,"content":"MockMvc 建的日记"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("集成测试日记"));
        mockMvc.perform(get("/journal/list").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.title == '集成测试日记')]").exists());
    }
}
