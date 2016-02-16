package org.stepic.droid.store.dao;

import android.database.Cursor;

import java.util.List;

public interface IDao<T> {
    void insertOrUpdate(T persistentObject);

    boolean isInDb(T persistentObject);

    List<T> getAll();

    List<T> getAll(String whereColumnName, String whereValue);

    T get(String whereColumnName, String whereValue);

    T parsePersistentObject(Cursor cursor);
}
