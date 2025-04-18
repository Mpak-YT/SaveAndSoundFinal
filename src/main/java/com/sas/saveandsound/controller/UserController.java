package com.sas.saveandsound.controller;

import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.exception.UserNotFoundException;
import com.sas.saveandsound.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "API for managing users")
public class UserController {

    private static final String SPECIAL_CHAR_PATTERN = "[\\n\\r\\t]";

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users", description = "Fetch all available users.")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        logger.info("Fetching all users...");
        List<UserDto> users = userService.getAllUsers();
        if (users.isEmpty()) {
            logger.warn("No users found.");
            throw new UserNotFoundException("No users found.");
        }
        logger.info("Found {} users.", users.size());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by ID", description = "Fetch a user by its ID.")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "ID of the user to fetch") @PathVariable long id) {
        logger.info("Fetching user with ID: {}", id);
        UserDto user = userService.searchUser(id);
        if (user == null) {
            logger.error("User with ID {} not found.", id);
            throw new UserNotFoundException("User with ID " + id + " not found.");
        }
        logger.info("User with ID {} retrieved successfully.", id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Search user by name", description = "Search for a user by name.")
    @GetMapping("/search")
    public ResponseEntity<UserDto> searchByName(
            @Parameter(description = "Name of the user to search for")
            @RequestParam(value = "name") String name) {
        name= name.replaceAll(SPECIAL_CHAR_PATTERN, "_");
        logger.info("Searching for user with name: {}", name);
        UserDto user = userService.search(name);
        if (user == null) {
            logger.warn("No user found with the name '{}'.", name);
            throw new UserNotFoundException("No user found with the name '" + name + "'.");
        }
        logger.info("User with name '{}' retrieved successfully.", name);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Create new users", description = "Add multiple users with the provided details.")
    @PostMapping("/add")
    public ResponseEntity<List<UserDto>> createUsers(
            @Valid @RequestBody List<UserDto> usersDto) {
        logger.info("Creating new users...");
        List<UserDto> createdUsers = usersDto.stream()
                .map(userService::createUser) // Создание каждого пользователя
                .toList();
        logger.info("Successfully created {} users.", createdUsers.size());
        return ResponseEntity.ok(createdUsers);
    }

    @Operation(summary = "Update multiple users",
            description = "Update existing users using their IDs and details.")
    @PutMapping("/bulk")
    public ResponseEntity<List<UserDto>> updateUsers(
            @Valid @RequestBody List<UserDto> usersDto) {
        logger.info("Bulk updating users...");
        List<UserDto> updatedUsers = usersDto.stream()
                .map(userDto -> userService.updateUser(userDto.getId(), userDto))
                .toList();
        logger.info("Successfully updated {} users.", updatedUsers.size());
        return ResponseEntity.ok(updatedUsers);
    }

    @Operation(summary = "Delete a user by ID",
            description = "Delete a specific user by its ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable long id) {
        logger.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        logger.info("User with ID {} deleted successfully.", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all users",
            description = "Delete all users from the database.")
    @DeleteMapping("")
    public ResponseEntity<Void> deleteAllUsers() {
        logger.info("Deleting all users...");
        userService.deleteUsers();
        logger.info("All users deleted successfully.");
        return ResponseEntity.noContent().build();
    }
}

