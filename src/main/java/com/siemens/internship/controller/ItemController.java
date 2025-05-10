package com.siemens.internship.controller;

import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * REST controller for managing Item resources.
 * Provides endpoints for CRUD operations and asynchronous processing.
 */
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    /**
     * Retrieves all items from the database
     * @return 200 OK with the list of items
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    /**
     * Creates a new item
     * @param item the item to be created
     * @param result validation result
     * @return 201 CREATED if successful,
     *      400 BAD_REQUEST if validation fails
     */
    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest() // returning bad request status
                    .body(result.getAllErrors()); // and list of validation errors
        }
        Item savedItem = itemService.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    /**
     * Retrieves an item by its id
     * @param id the id of the item
     * @return 200 OK with the item if found
     *      404 NOT_FOUND otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        // not found instead of no content since that would mean that the item exists
        // but the response doesn't have a body
    }

    /**
     * Updates an existing item
     * @param id the id of the item to be updated
     * @param item the updated item
     * @param result validation result
     * @return 200 OK if updated successfully,
     *      400 BAD_REQUEST if validation fails,
     *      404 NOT_FOUND if the item to update doesn't exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest() // returning bad request status
                    .body(result.getAllErrors()); // and validation errors
        }

        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.OK);
            // instead of created status, since the item already exists and is just updated
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes an item by id
     * @param id id of the item to be deleted
     * @return 204 NO_CONTENT if deleted,
     *      404 NOT_FOUND if item to delete doesn't exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if(itemService.findById(id).isPresent()) {
            itemService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // recommended status for delete
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Processes asynchronously all the items,
     * each item is updated and saved with status "PROCESSED"
     * @return 200 OK with list of processed items,
     *      500 INTERNAL_SERVER_ERROR if something fails
     */
    @GetMapping("/process")
    public ResponseEntity<?> processItems() {
        try {
            List<Item> future = itemService.processItemsAsync().get();
            return ResponseEntity.ok(future);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Processing was interrupted.");
        }
        catch (ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the items.");
        }
    }
}
