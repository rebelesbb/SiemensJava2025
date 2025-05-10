package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Service class for managing Item entities and processing them asynchronously.
 * Provides basic CRUD operations and a method to process all items in parallel.
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ItemService {
    private final ItemRepository itemRepository;
    private final static ExecutorService executor = Executors.newFixedThreadPool(10);


    /**
     * @return a list of all items
     */
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    /**
     * Searches for an item by its id
     * @param id the item's id
     * @return Optional of item, if found, empty Optional otherwise
     */
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    /**
     * Saves a new item or updates an existing one
     * @param item item to be saved/updated
     * @return the saved/updated item
     */
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    /**
     * Deletes an item by its id
     * @param id the id of the item to be deleted
     */
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    /**
     * Asynchronously processes all items in the database by updating their status to "PROCESSED"
     * @return only the items that were successfully updated and saved
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        // retrieves all ids
        List<Long> itemIds = itemRepository.findAllIds();

        // for each id, create an async task to process the item
        List<CompletableFuture<Item>> futures = itemIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    try{
                        // get item by it
                        Optional<Item> itemOptional = itemRepository.findById(id);
                        if(itemOptional.isEmpty()) return null; // if null, return null

                        // else update status and save
                        Item item = itemOptional.get();
                        item.setStatus("PROCESSED");
                        return  itemRepository.save(item);
                    }
                    catch (Exception e) {
                        return null;
                    }
                }, executor))
                .toList();

        // wait for all tasks to be completed
        CompletableFuture<Void> allProcessed = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // collect non-null results from completed futures
        return allProcessed.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .toList());
    }

}

