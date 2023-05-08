package com.itm.space.backendresources.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.mapper.UserMapper;
import com.itm.space.backendresources.mapper.UserMapperImpl;
import com.itm.space.backendresources.service.UserService;
import com.itm.space.backendresources.service.UserServiceImpl;
import com.itm.space.backendresources.utils.JsonUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserControllerTest {

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

    @AfterEach
    void tearDown() {
        clearInvocations(keycloak);
    }

    @Test
    @SneakyThrows
    void createUserShouldBeSuccessfulTest() {
        Response response = Response.status(Response.Status.CREATED).location(new URI("user_id")).build();
        when(keycloak.realm(any()).users().create(any())).thenReturn(response);

        try {
            userController.create(userRequest);
        } catch (WebApplicationException exception) {
            fail();
        }
        verify(keycloak.realm(any()).users(), times(1)).create(any());
    }

    @Test
    void createUserFailingTest() {
        Response response = Response.status(Response.Status.NOT_FOUND).build();
        when(keycloak.realm(any()).users().create(any())).thenReturn(response);

        assertThrows(BackendResourcesException.class, () -> userController.create(userRequest));
    }

    @Test
    @SneakyThrows
    void getUserByIdShouldBeSuccessfulTest() {
        UUID id = UUID.randomUUID();
        UserRepresentation userRepresentation = makeUserRepresentation();
        userRepresentation.setId(id.toString());

        List<String> userRoles = new ArrayList<>();
        userRoles.add("MODERATOR");
        List<String> userGroups = new ArrayList<>();
        userGroups.add("Moderators");

        userRepresentation.setRealmRoles(userRoles);
        userRepresentation.setGroups(userGroups);

        when(keycloak.realm(any()).users().get(any())).thenReturn(mock(UserResource.class));
        when(keycloak.realm(any()).users().get(any()).toRepresentation()).thenReturn(userRepresentation);
        when(keycloak.realm(any()).users().get(any()).roles()).thenReturn(mock(RoleMappingResource.class));
        when(keycloak.realm(any()).users().get(any()).roles().getAll()).thenReturn(mock(MappingsRepresentation.class));
        when(keycloak.realm(any()).users().get(any()).roles().getAll().getRealmMappings()).thenReturn(getRoleRepresentation(userRoles));
        when(keycloak.realm(any()).users().get(any()).groups()).thenReturn(getGroupRepresentation(userGroups));

        UserResponse actualUserResponse = userController.getUserById(id);

        var actual = JsonUtils.serializeToJson(actualUserResponse);
        var expected = JsonUtils.loadJsonFromFile(EXPECTED_USER_RESPONSE);

        assertEquals(objectMapper.readTree(expected), objectMapper.readTree(actual));
        verify(keycloak.realm(any()).users(), times(3)).get(id.toString());
    }

    @Test
    void getUserByIdFailingTest() {
        UUID notValidId = UUID.randomUUID();
        when(keycloak.realm(any()).users().get(String.valueOf(notValidId)))
                .thenThrow(new RuntimeException("User not found"));

        assertThrows(BackendResourcesException.class, () -> userController.getUserById(notValidId));
    }

    private UserRepresentation makeUserRepresentation() {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName("Selmeg");
        userRepresentation.setLastName("Ts");
        userRepresentation.setEmail("selmeg@mail.ru");
        return userRepresentation;
    }

    private List<RoleRepresentation> getRoleRepresentation(List<String> userRoles) {
        List<RoleRepresentation> roles = new ArrayList<>();

        for (String role: userRoles) {
            RoleRepresentation roleRepresentation = new RoleRepresentation();
            roleRepresentation.setName(role);
            roles.add(roleRepresentation);
        }
        return roles;
    }

    private List<GroupRepresentation> getGroupRepresentation(List<String> userGroups) {
        List<GroupRepresentation> groups = new ArrayList<>();
        for (String group: userGroups) {
            GroupRepresentation groupRepresentation = new GroupRepresentation();
            groupRepresentation.setName(group);
            groups.add(groupRepresentation);
        }
        return groups;
    }
}