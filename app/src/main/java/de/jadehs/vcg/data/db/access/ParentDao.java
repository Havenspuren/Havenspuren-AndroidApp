package de.jadehs.vcg.data.db.access;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

public interface ParentDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(T... data);

    @Update
    void update(T... data);

    @Delete
    void delete(T... data);
}
