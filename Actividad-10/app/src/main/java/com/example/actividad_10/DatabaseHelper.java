package com.example.actividad_10;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/** Owns the SQLite database and every CRUD operation on the contacts table. */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_CONTACTS = "contacts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_EMAIL = "email";

    private static final String SQL_CREATE_CONTACTS =
            "CREATE TABLE " + TABLE_CONTACTS + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_NAME + " TEXT NOT NULL, "
                    + COLUMN_PHONE + " TEXT, "
                    + COLUMN_EMAIL + " TEXT)";

    /** Selection used by update/delete; the ID is always passed as an argument, never concatenated. */
    private static final String SELECTION_BY_ID = COLUMN_ID + " = ?";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Only version 1 exists so far. Future schema changes should be applied step by step here,
        // e.g. "if (oldVersion < 2) db.execSQL("ALTER TABLE ...")", so existing contacts are kept.
    }

    // ---------- Create ----------

    /** Inserts a contact and returns its new row ID, or -1 if the insert failed. */
    public long addContact(String name, String phone, String email) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            return db.insert(TABLE_CONTACTS, null, toContentValues(name, phone, email));
        } catch (SQLException e) {
            Log.e(TAG, "Failed to insert contact", e);
            return -1;
        }
    }

    // ---------- Read ----------

    /** Returns every contact ordered alphabetically by name (empty list on error). */
    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        String[] columns = {COLUMN_ID, COLUMN_NAME, COLUMN_PHONE, COLUMN_EMAIL};
        String orderBy = COLUMN_NAME + " COLLATE NOCASE ASC";

        try (Cursor cursor = getReadableDatabase().query(
                TABLE_CONTACTS, columns, null, null, null, null, orderBy)) {
            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_NAME);
            int phoneIndex = cursor.getColumnIndexOrThrow(COLUMN_PHONE);
            int emailIndex = cursor.getColumnIndexOrThrow(COLUMN_EMAIL);
            while (cursor.moveToNext()) {
                contacts.add(new Contact(
                        cursor.getLong(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getString(phoneIndex),
                        cursor.getString(emailIndex)));
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to read contacts", e);
        }
        return contacts;
    }

    // ---------- Update ----------

    /** Updates the contact with the given ID and returns the number of rows changed (0 or 1). */
    public int updateContact(long id, String name, String phone, String email) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            return db.update(TABLE_CONTACTS, toContentValues(name, phone, email),
                    SELECTION_BY_ID, new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update contact " + id, e);
            return 0;
        }
    }

    // ---------- Delete ----------

    /** Deletes the contact with the given ID and returns the number of rows removed (0 or 1). */
    public int deleteContact(long id) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            return db.delete(TABLE_CONTACTS, SELECTION_BY_ID, new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            Log.e(TAG, "Failed to delete contact " + id, e);
            return 0;
        }
    }

    private static ContentValues toContentValues(String name, String phone, String email) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_EMAIL, email);
        return values;
    }
}
