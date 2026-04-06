package com.example.makeup433;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AllergenDao {

    @Query("SELECT * FROM allergens ORDER BY name COLLATE NOCASE")
    List<Allergen> getAll();

    @Insert
    long insert(Allergen allergen);

    @Update
    void update(Allergen allergen);

    @Delete
    void delete(Allergen allergen);
}