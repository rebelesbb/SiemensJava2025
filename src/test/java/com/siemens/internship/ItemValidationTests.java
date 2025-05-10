package com.siemens.internship;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ItemValidationTests {

    private Validator itemsValidator;

    @BeforeEach
    public void createValidator(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        itemsValidator = factory.getValidator();
    }

    @Test
    public void testValidItem(){
        Item item = new Item();
        item.setName("a".repeat(100));
        item.setDescription("a".repeat(255));
        item.setStatus("status");
        item.setEmail("email@example.com");

        Set<ConstraintViolation<Item>> constraintViolations = itemsValidator.validate(item);
        assertTrue(constraintViolations.isEmpty());
    }

    @Test
    public void testInvalidItem(){
        Item item1 = new Item(null, "", "", "", "");
        Set<ConstraintViolation<Item>> constraintViolations = itemsValidator.validate(item1);
        assertEquals(constraintViolations.size(), 5, "Failed on case: All fields blank");
        // 5 violations because both the NotBlank and the Pattern constraints will be violated

        String invalidName = "a".repeat(101);
        String invalidDescription = "a".repeat(256);
        String invalidEmail = "a";
        Item item2 = new Item(null, invalidName, invalidDescription, "", invalidEmail);
        constraintViolations = itemsValidator.validate(item2);
        assertEquals(constraintViolations.size(), 4, "Failed on case: All fields invalid");
    }

    @Test
    public void testInvalidEmail(){
        String validName = "name";
        String validDescription = "description";
        String validStatus = "status";

        Item item1 = new Item(null, validName, validDescription, validStatus, null);
        Set<ConstraintViolation<Item>> constraintViolations = itemsValidator.validate(item1);
        assertEquals(constraintViolations.size(), 1, "Failed on case: Null email");

        // the cases "" and "a" were tested above

        List<String> invalidEmails = Arrays.asList("a@", "a@example", "a@example.",
                "a@example.c", "aexample.com", "@example.com", "@.");

        for(String invalidEmail : invalidEmails){
            Item invalidItem = new Item(null, validName, validDescription, validStatus, invalidEmail);
            constraintViolations = itemsValidator.validate(invalidItem);
            assertEquals(constraintViolations.size(), 1, "Failed on case: Invalid format - " + invalidEmail);
        }

    }
}
