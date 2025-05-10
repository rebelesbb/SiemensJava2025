package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the ItemController.
 * Uses MockMvc to validate REST endpoints including validation, persistence, and async processing.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerTests {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    @SpyBean
    private ItemService itemService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void clearRepo() {
        itemRepository.deleteAll();
    }

    @Test
    public void testCreateValidItem(){
        String itemJson = """
                {
                    "name": "Valid Item",
                    "description": "Valid Description",
                    "status": "PENDING",
                    "email": "email@example.com"
                }
                """;
        try {
            mockMvc.perform(post("/api/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(itemJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("Valid Item"));
        } catch (Exception e) {
            fail("TestCreateValidItem failed: " + e.getMessage());
        }
    }

    @Test
    public void testCreateInvalidItem(){
        String itemJson = """
                {
                    "name": "",
                    "description": "",
                    "status": "",
                    "email": "email"
                }
                """;
        try{
            mockMvc.perform(post("/api/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(itemJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(4));

        }
        catch(Exception e){
            fail("TestCreateInvalidItem failed: " + e.getMessage());
        }
    }

    @Test
    public void testGetAllItems(){
        itemRepository.save(new Item(null, "Item A", "Description A", "PENDING", "a@example.com"));
        itemRepository.save(new Item(null, "Item B", "Description B", "PENDING", "b@example.com"));
        try {
            mockMvc.perform(get("/api/items")
                            .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.length()").value(2));
        }
        catch(Exception e){
            fail("TestGetAllItems failed: " + e.getMessage());
        }
    }

    @Test
    public void testFindById(){
        Item saved = itemRepository.save(new Item(null, "Item", "Description", "PENDING", "email@example.com"));

        try {
            mockMvc.perform(get("/api/items/" + saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Item"));

            mockMvc.perform(get("/api/items/99999"))
                    .andExpect(status().isNotFound());
        }
        catch(Exception e){
            fail("TestFindById failed: " + e.getMessage());
        }
    }

    @Test
    public void updateValidItem(){
        Item original = itemRepository.save(new Item(null, "Old Name", "Old description", "PENDING", "old@example.com"));

        String updatedJson = """
                {
                    "name": "Updated Name",
                    "description": "Updated Description",
                    "status": "PROCESSED",
                    "email": "updated@example.com"
                }
                """;

        try {
            mockMvc.perform(put("/api/items/" + original.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updatedJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Name"))
                    .andExpect(jsonPath("$.description").value("Updated Description"))
                    .andExpect(jsonPath("$.status").value("PROCESSED"))
                    .andExpect(jsonPath("$.email").value("updated@example.com"));
        }
        catch(Exception e){
            fail("TestUpdateValidItem failed: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateInvalidItem(){
        // first update: invalid description + email (expect 400)
        // second update: valid data but nonexistent ID (expect 404)

        Item original = itemRepository.save(new Item(null, "Old Name", "Old description", "PENDING", "old@example.com"));

        String invalidUpdatedJson = """
                {
                    "name": "Updated Name",
                    "description": "",
                    "status": "PROCESSED",
                    "email": "updatedexample.com"
                }
                """; // invalid description and email

        String validUpdatedJson = """
                {
                    "name": "Updated Name",
                    "description": "Updated Description",
                    "status": "PROCESSED",
                    "email": "updated@example.com"
                }
                """;

        try {
            // invalid data
            mockMvc.perform(put("/api/items/" + original.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidUpdatedJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));

            // invalid id
            mockMvc.perform(put("/api/items/99999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validUpdatedJson))
                    .andExpect(status().isNotFound());
        }
        catch(Exception e){
            fail("TestUpdateValidItem failed: " + e.getMessage());
        }
    }

    @Test
    public void testDelete(){
        Item item = itemRepository.save(new Item(null, "ToDelete", "desc", "PENDING", "x@example.com"));

        try {
            mockMvc.perform(delete("/api/items/" + item.getId()))
                    .andExpect(status().isNoContent());

            assertFalse(itemRepository.findById(item.getId()).isPresent());

            mockMvc.perform(delete("/api/items/" + item.getId()))
                    .andExpect(status().isNotFound());
        }
        catch(Exception e){
            fail("TestDeleteItem failed: " + e.getMessage());
        }
    }

    @Test
    public void testProcessItemsSuccessfully(){
        itemRepository.save(new Item(null, "A", "Desc", "PENDING", "a@example.com"));
        itemRepository.save(new Item(null, "B", "Desc", "PENDING", "b@example.com"));

        try {
            mockMvc.perform(get("/api/items/process"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].status").value("PROCESSED"));
        }
        catch(Exception e){
            fail("TestProcessItems failed: " + e.getMessage());
        }
    }

    @Test
    public void testProcessItemsFailedError(){
        // simulates a failure during async processing (ExecutionException)
        CompletableFuture<List<Item>> failingFuture = new CompletableFuture<>();
        failingFuture.completeExceptionally(new RuntimeException());

        try {
            Mockito.doReturn(failingFuture).when(itemService).processItemsAsync();
            mockMvc.perform(get("/api/items/process"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("An error occurred while processing the items."));
        } catch (Exception e) {
            fail("TestProcessItemsFailedError failed: " + e.getMessage());
        }
    }
}
