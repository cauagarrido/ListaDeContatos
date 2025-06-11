package com.example.projetolistacontatos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.projetolistacontatos.model.Contact;
import java.util.ArrayList;
import java.util.List;

public class ContactDAO {

    private SQLiteDatabase db;
    private final DatabaseHelper dbHelper;

    public ContactDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private void open() {
        db = dbHelper.getWritableDatabase();
    }

    private void close() {
        dbHelper.close();
    }

    public long validateLogin(String login, String password) {
        open();
        long userId = -1;
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USER_ID},
                DatabaseHelper.COLUMN_USER_LOGIN + " = ? AND " + DatabaseHelper.COLUMN_USER_PASSWORD + " = ?",
                new String[]{login, password}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
            cursor.close();
        }
        close();
        return userId;
    }

    public boolean addContact(Contact contact) {
        open();
        if (cpfExists(contact.getCpf(), 0)) {
            close();
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CONTACT_NAME, contact.getName());
        values.put(DatabaseHelper.COLUMN_CONTACT_EMAIL, contact.getEmail());
        values.put(DatabaseHelper.COLUMN_CONTACT_PHONE, contact.getPhone());
        values.put(DatabaseHelper.COLUMN_CONTACT_CPF, contact.getCpf());
        values.put(DatabaseHelper.COLUMN_CONTACT_USER_ID, contact.getUserId());
        long result = db.insert(DatabaseHelper.TABLE_CONTACTS, null, values);
        close();
        return result != -1;
    }

    public boolean updateContact(Contact contact) {
        open();
        if (cpfExists(contact.getCpf(), contact.getId())) {
            close();
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CONTACT_NAME, contact.getName());
        values.put(DatabaseHelper.COLUMN_CONTACT_EMAIL, contact.getEmail());
        values.put(DatabaseHelper.COLUMN_CONTACT_PHONE, contact.getPhone());
        values.put(DatabaseHelper.COLUMN_CONTACT_CPF, contact.getCpf());

        int result = db.update(DatabaseHelper.TABLE_CONTACTS, values,
                DatabaseHelper.COLUMN_CONTACT_ID + " = ?",
                new String[]{String.valueOf(contact.getId())});
        close();
        return result > 0;
    }

    public void deleteContact(long contactId) {
        open();
        db.delete(DatabaseHelper.TABLE_CONTACTS,
                DatabaseHelper.COLUMN_CONTACT_ID + " = ?",
                new String[]{String.valueOf(contactId)});
        close();
    }

    public List<Contact> getAllContactsFromUser(long userId) {
        open();
        List<Contact> contacts = new ArrayList<>();
        String query = "SELECT c.* FROM " + DatabaseHelper.TABLE_CONTACTS + " c " +
                "INNER JOIN " + DatabaseHelper.TABLE_USERS + " u ON c." + DatabaseHelper.COLUMN_CONTACT_USER_ID + " = u." + DatabaseHelper.COLUMN_USER_ID +
                " WHERE u." + DatabaseHelper.COLUMN_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT_NAME)));
                contact.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT_EMAIL)));
                contact.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT_PHONE)));
                contact.setCpf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT_CPF)));
                contact.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT_USER_ID)));
                contacts.add(contact);
            } while (cursor.moveToNext());
            cursor.close();
        }
        close();
        return contacts;
    }

    private boolean cpfExists(String cpf, long currentContactId) {
        String selection = DatabaseHelper.COLUMN_CONTACT_CPF + " = ? AND " + DatabaseHelper.COLUMN_CONTACT_ID + " != ?";
        Cursor cursor = db.query(DatabaseHelper.TABLE_CONTACTS, new String[]{DatabaseHelper.COLUMN_CONTACT_ID},
                selection, new String[]{cpf, String.valueOf(currentContactId)}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}