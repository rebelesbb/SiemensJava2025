package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
public class ItemServiceCrudTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @BeforeEach
    public void clearRepo(){
        itemRepository.deleteAll();
    }

    @Test
    public void testSaveAndFindBy(){
        assertEquals(0, itemService.findAll().size());

        itemService.save(new Item(null, "A", "DescriptionA", "PENDING", "a@example.com"));
        Item saved = itemService.save(new Item(null, "B", "DescriptionB", "PENDING", "b@example.com"));

        assertNotNull(saved.getId());
        assertEquals(2, itemService.findAll().size());

        Optional<Item> itemOptional = itemService.findById(saved.getId());
        assertTrue(itemOptional.isPresent());
        assertEquals("B", itemOptional.get().getName());
    }

    @Test
    public void testFindAll(){
        assertEquals(0, itemService.findAll().size());
        itemService.save(new Item(null, "A", "DescriptionA", "PENDING", "a@example.com"));
        itemService.save(new Item(null, "B", "DescriptionB", "PENDING", "b@example.com"));

        List<Item> items = itemService.findAll();
        assertEquals(2, items.size());

        List<String> itemNames = items.stream()
                .map(Item::getName)
                .toList();

        assertTrue(itemNames.contains("A"));
        assertTrue(itemNames.contains("B"));
    }

    @Test
    public void testDelete(){
        Item itemToKeep = itemService.save(new Item(null, "A", "DescriptionA", "PENDING", "a@example.com"));
        Item itemToDelete = itemService.save(new Item(null, "B", "DescriptionB", "PENDING", "b@example.com"));

        assertEquals(2, itemService.findAll().size());
        itemService.deleteById(itemToDelete.getId());
        assertEquals(1, itemService.findAll().size());

        assertTrue(itemService.findById(itemToDelete.getId()).isEmpty());

        Optional<Item> itemOptional = itemService.findById(itemToKeep.getId());
        assertTrue(itemOptional.isPresent());

        assertEquals("A", itemOptional.get().getName());
    }
}
