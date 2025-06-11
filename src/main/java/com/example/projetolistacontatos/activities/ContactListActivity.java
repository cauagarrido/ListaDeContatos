package com.example.projetolistacontatos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetolistacontatos.R;
import com.example.projetolistacontatos.adapter.ContactAdapter;
import com.example.projetolistacontatos.database.ContactDAO;
import com.example.projetolistacontatos.model.Contact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ContactListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private List<Contact> contactList = new ArrayList<>();
    private ContactDAO contactDAO;
    private long userId;
    private int selectedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        userId = getIntent().getLongExtra("USER_ID", -1);
        if (userId == -1) {
            finish();
            return;
        }

        contactDAO = new ContactDAO(this);
        recyclerView = findViewById(R.id.recyclerViewContacts);
        FloatingActionButton fab = findViewById(R.id.fabAddContact);

        setupRecyclerView();

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(ContactListActivity.this, AddEditContactActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        registerForContextMenu(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }

    private void setupRecyclerView() {
        adapter = new ContactAdapter(contactList, position -> {
            selectedPosition = position;
            recyclerView.showContextMenuForChild(recyclerView.getChildAt(position));
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadContacts() {
        List<Contact> updatedContacts = contactDAO.getAllContactsFromUser(userId);
        adapter.updateContacts(updatedContacts);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.contact_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        Contact selectedContact = adapter.getContactAt(selectedPosition);

        int itemId = item.getItemId();
        if (itemId == R.id.menu_edit) {
            editContact(selectedContact);
            return true;
        } else if (itemId == R.id.menu_delete) {
            confirmDeleteContact(selectedContact);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    private void editContact(Contact contact) {
        Intent intent = new Intent(this, AddEditContactActivity.class);
        intent.putExtra("CONTACT", contact);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    private void confirmDeleteContact(Contact contact) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_option)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    contactDAO.deleteContact(contact.getId());
                    loadContacts();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}