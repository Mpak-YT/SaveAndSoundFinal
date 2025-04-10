package com.sas.saveandsound.controller;

import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.exception.UserNotFoundException;
import com.sas.saveandsound.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "API for managing users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users", description = "Fetch all available users.")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> results = userService.getAllUsers();
        if (results.isEmpty()) {
            throw new UserNotFoundException("No users found.");
        }
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get user by ID", description = "Fetch a user by its ID.")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "ID of the user to fetch") @PathVariable long id) {
        UserDto result = userService.searchUser(id);
        if (result == null) {
            throw new UserNotFoundException("User with ID " + id + " not found.");
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Search user by name", description = "Search for a user by its name.")
    @GetMapping("/search")
    public ResponseEntity<UserDto> searchByName(
            @Parameter(description = "Name of the user to search for")
            @RequestParam(value = "name") String name) {
        UserDto result = userService.search(name);
        if (result == null) {
            throw new UserNotFoundException("No user found with the name '" + name + "'.");
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Create a new user",
            description = "Add a new user with the provided details.")
    @PostMapping("/add")
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody Map<String, Object> userData) {
        UserDto createdUser = userService.createUser(userData);
        return ResponseEntity.ok(createdUser);
    }

    @Operation(summary = "Update a user",
            description = "Update an existing user by its ID.")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "ID of the user to update") @PathVariable long id,
            @RequestBody Map<String, Object> userData) {
        UserDto updatedUser = userService.updateUser(id, userData);
        if (updatedUser == null) {
            throw new UserNotFoundException("Unable to update user with ID " + id + ". User not found.");
        }
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Delete a user by ID", description = "Delete a specific user by its ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all users", description = "Delete all users from the database.")
    @DeleteMapping("")
    public ResponseEntity<Void> deleteAll() {
        userService.deleteUsers();
        return ResponseEntity.noContent().build();
    }
}
