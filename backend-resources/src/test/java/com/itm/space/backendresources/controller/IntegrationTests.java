package com.itm.space.backendresources.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.mapper.UserMapper;
import com.itm.space.backendresources.mapper.UserMapperImpl;
import com.itm.space.backendresources.service.UserService;
import com.itm.space.backendresources.service.UserServiceImpl;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class IntegrationTests extends BaseIntegrationTest {

    @Mock
    private Keycloak keycloak;
    private UserMapper userMapper;
    private UserService userService;
    private UserController userController;
    private UserRequest userRequest;
    private ObjectMapper objectMapper;
    private static final String EXPECTED_USER_RESPONSE = "Expected_UserResponse.json";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        MockitoAnnotations.openMocks(this);
        when(keycloak.realm(any())).thenReturn(mock(RealmResource.class));
        when(keycloak.realm(any()).users()).thenReturn(mock(UsersResource.class));
        userMapper = new UserMapperImpl();
        userService = new UserServiceImpl(keycloak, userMapper);
        userController = new UserController(userService);
        userRequest = new UserRequest("Selmeg01",
                "selmeg@mail.ru",
                "selmeg",
                "Selmeg",
                "Ts");
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "Selmeg", roles = "MODERATOR")
    void testHello() {
        MvcResult result = mvc.perform(get("/api/users/hello")
        ).andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();

        Assertions.assertEquals("Selmeg", responseContent);
    }


}
