package com.example.makeup433;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "allergens")
public class Allergen {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "note")
    public String note;

    public Allergen(@NonNull String name, String note) {
        this.name = name;
        this.note = note;
    }
}