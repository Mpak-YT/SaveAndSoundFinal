package com.sas.saveandsound.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserDto {

    private Long id;

    @NotBlank(message = "User name cannot be null, empty, or contain only spaces")
    @Size(min = 2, max = 50, message = "User name must be between 2 and 50 characters")
    @Pattern(regexp = "^[\\p{L}\\d ]+$", message = "User name can only contain letters, digits and spaces")
    private String name;
    private boolean role;

    @Email(message = "Email should be valid")
    private String email;
    private String nickname;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getRole() {
        return role;
    }

    public void setRole(boolean role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

}
