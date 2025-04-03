package com.sas.saveandsound.controller;

import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.exception.UserNotFoundException;
import com.sas.saveandsound.service.UserService;
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
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> results = userService.getAllUsers();
        if (results.isEmpty()) {
            throw new UserNotFoundException("No users found.");
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable long id) {
        UserDto result = userService.searchUser(id);
        if (result == null) {
            throw new UserNotFoundException("User with ID " + id + " not found.");
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<UserDto> searchByName(@RequestParam(value = "name") String name) {
        UserDto result = userService.search(name);
        if (result == null) {
            throw new UserNotFoundException("No user found with the name '" + name + "'.");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/add")
    public ResponseEntity<UserDto> createUser(@RequestBody Map<String, Object> userData) {
        UserDto createdUser = userService.createUser(userData);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable long id,
                                              @RequestBody Map<String, Object> userData) {
        UserDto updatedUser = userService.updateUser(id, userData);
        if (updatedUser == null) {
            throw new UserNotFoundException("Unable to update user with ID " + id + ". User not found.");
        }
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("")
    public ResponseEntity<Void> deleteAll() {
        userService.deleteUsers();
        return ResponseEntity.noContent().build();
    }
}
