package com.example.projetolistacontatos.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetolistacontatos.R;
import com.example.projetolistacontatos.database.ContactDAO;
import com.example.projetolistacontatos.model.Contact;

public class AddEditContactActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPhone, editTextCpf;
    private ContactDAO contactDAO;
    private Contact currentContact;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_contact);

        contactDAO = new ContactDAO(this);
        userId = getIntent().getLongExtra("USER_ID", -1);
        currentContact = (Contact) getIntent().getSerializableExtra("CONTACT");

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextCpf = findViewById(R.id.editTextCpf);
        Button buttonSave = findViewById(R.id.buttonSave);

        if (currentContact != null) {
            setTitle(getString(R.string.edit_contact_title));
            populateFields();
        } else {
            setTitle(getString(R.string.add_contact_title));
        }

        buttonSave.setOnClickListener(v -> saveContact());
    }

    private void populateFields() {
        editTextName.setText(currentContact.getName());
        editTextEmail.setText(currentContact.getEmail());
        editTextPhone.setText(currentContact.getPhone());
        editTextCpf.setText(currentContact.getCpf());
    }

    private boolean validateInput() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String cpf = editTextCpf.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || cpf.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveContact() {
        if (!validateInput()) {
            return;
        }

        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String cpf = editTextCpf.getText().toString().trim();

        boolean success;
        if (currentContact != null) {
            currentContact.setName(name);
            currentContact.setEmail(email);
            currentContact.setPhone(phone);
            currentContact.setCpf(cpf);
            success = contactDAO.updateContact(currentContact);
        } else {
            Contact newContact = new Contact();
            newContact.setName(name);
            newContact.setEmail(email);
            newContact.setPhone(phone);
            newContact.setCpf(cpf);
            newContact.setUserId(userId);
            success = contactDAO.addContact(newContact);
        }

        if (success) {
            finish();
        } else {
            Toast.makeText(this, getString(R.string.error_cpf_exists), Toast.LENGTH_SHORT).show();
        }
    }
}