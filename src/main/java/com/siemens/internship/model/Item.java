package com.siemens.internship.model;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Name can't be blank")
    @Size(max = 100, message = "Name can't have more than 100 characters")
    private String name;

    @NotBlank(message = "Description can't be blank")
    @Size(max = 255, message = "Description can't have more than 255 characters")
    private String description;

    @NotBlank(message = "Status can't be blank")
    private String status;

    @NotBlank(message = "Email can't be blank")
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+\\.[A-Za-z]{2,}$",
            message = "Invalid email format"
    )
    private String email;
}