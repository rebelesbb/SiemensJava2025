package com.siemens.internship.repository;

import com.siemens.internship.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * @return all item ids from the db (without loading full entities)
     */
    @Query("SELECT id FROM Item")
    List<Long> findAllIds();
}
