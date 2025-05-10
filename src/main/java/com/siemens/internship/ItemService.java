package com.siemens.internship;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ItemService {
    private final ItemRepository itemRepository;
    private final static ExecutorService executor = Executors.newFixedThreadPool(10);


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();

        List<CompletableFuture<Item>> futures = itemIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    try{
                        Optional<Item> itemOptional = itemRepository.findById(id);
                        if(itemOptional.isEmpty()) return null;

                        Item item = itemOptional.get();
                        item.setStatus("PROCESSED");
                        return  itemRepository.save(item);
                    }
                    catch (Exception e) {
                        return null;
                    }
                }, executor))
                .toList();

        CompletableFuture<Void> allProcessed = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        return allProcessed.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .toList());
    }

}

