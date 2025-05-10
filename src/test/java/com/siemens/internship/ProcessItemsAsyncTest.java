package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class ProcessItemsAsyncTest {

    @Autowired
    @SpyBean
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @BeforeEach
    public void addItems(){
        itemRepository.deleteAll();

        itemRepository.save(new Item(null, "item1", "description1", "PENDING", "email1@example.com"));
        itemRepository.save(new Item(null, "item2", "description2", "PENDING", "email2@example.com"));
        itemRepository.save(new Item(null, "item3", "description3", "PENDING", "email3@example.com"));
    }

    @Test
    public void testProcessItemsAsyncSuccessfully(){
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        try {
            List<Item> items = future.get();
            assertEquals(3, items.size());
        }
        catch (Exception e) {
            fail("ProcessItemsAsyncSuccessfully test failed: " + e.getMessage());
        }
    }

    @Test
    public void testProcessItemsAsyncFailUpdate(){
        Item itemToFail = itemRepository.save(new Item(null, "item4", "description4", "PENDING", "email4@example.com"));

        Mockito.doThrow(new RuntimeException())
                .when(itemRepository)
                .save(Mockito.argThat(item -> item.getId().equals(itemToFail.getId())));

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        try{
            List<Item> items = future.get();
            assertEquals(3, items.size());
            assertFalse(items.contains(itemToFail));
        }
        catch (Exception e) {
            fail("ProcessItemsAsyncFailUpdate test failed: " + e.getMessage());
        }
    }

    @Test
    public void testProcessItemsAsyncFailFindItem(){
        Item itemNotFound = itemRepository.save(new Item(null, "item4", "description4", "PENDING", "email4@example.com"));

        Mockito.doReturn(Optional.empty())
                .when(itemRepository)
                .findById(itemNotFound.getId());

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        try{
            List<Item> items = future.get();
            assertEquals(3, items.size());
            assertFalse(items.contains(itemNotFound));
        }
        catch (Exception e) {
            fail("ProcessItemsAsyncFailFindItem test failed: " + e.getMessage());
        }
    }
}
