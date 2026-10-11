package com.example.actividad_10;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final long NO_CONTACT = -1;
    private static final String STATE_EDITING_ID = "editing_id";
    private static final String STATE_EDITING_NAME = "editing_name";

    private DatabaseHelper databaseHelper;

    private TextInputLayout layoutName;
    private TextInputLayout layoutPhone;
    private TextInputLayout layoutEmail;
    private TextInputEditText editName;
    private TextInputEditText editPhone;
    private TextInputEditText editEmail;
    private TextView textFormMode;
    private Button buttonSave;
    private Button buttonUpdate;
    private Button buttonCancel;
    private ListView listContacts;

    private final List<Contact> contacts = new ArrayList<>();
    private ContactAdapter adapter;

    /** ID of the contact loaded in the form, or NO_CONTACT while creating a new one. */
    private long editingContactId = NO_CONTACT;
    private String editingContactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applySystemBarInsets(findViewById(R.id.main));

        databaseHelper = new DatabaseHelper(this);

        layoutName = findViewById(R.id.layoutName);
        layoutPhone = findViewById(R.id.layoutPhone);
        layoutEmail = findViewById(R.id.layoutEmail);
        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editEmail = findViewById(R.id.editEmail);
        textFormMode = findViewById(R.id.textFormMode);
        buttonSave = findViewById(R.id.buttonSave);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonCancel = findViewById(R.id.buttonCancel);
        listContacts = findViewById(R.id.listContacts);

        adapter = new ContactAdapter(this, contacts, this::confirmDelete);
        listContacts.setAdapter(adapter);
        listContacts.setEmptyView(findViewById(R.id.textEmpty));
        listContacts.setOnItemClickListener((parent, view, position, id) ->
                startEditing(contacts.get(position)));

        buttonSave.setOnClickListener(v -> saveContact());
        buttonUpdate.setOnClickListener(v -> updateContact());
        buttonCancel.setOnClickListener(v -> exitEditMode());

        // The EditTexts restore their own text; only the edit mode needs to be restored here.
        if (savedInstanceState != null) {
            editingContactId = savedInstanceState.getLong(STATE_EDITING_ID, NO_CONTACT);
            editingContactName = savedInstanceState.getString(STATE_EDITING_NAME);
        }
        refreshContacts();
        updateFormMode();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_EDITING_ID, editingContactId);
        outState.putString(STATE_EDITING_NAME, editingContactName);
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

    // ---------- Create ----------

    private void saveContact() {
        ContactInput input = readValidatedInput();
        if (input == null) {
            return;
        }
        long newId = databaseHelper.addContact(input.name, input.phone, input.email);
        if (newId == -1) {
            showMessage(getString(R.string.contact_save_failed));
            return;
        }
        showMessage(getString(R.string.contact_saved, input.name));
        clearForm();
        refreshContacts();
    }

    // ---------- Read ----------

    /** Reloads every contact from SQLite into the list. */
    private void refreshContacts() {
        contacts.clear();
        contacts.addAll(databaseHelper.getAllContacts());
        adapter.notifyDataSetChanged();
        highlightEditingContact();
    }

    // ---------- Update ----------

    /** Loads the selected contact into the form and switches to edit mode. */
    private void startEditing(Contact contact) {
        editingContactId = contact.getId();
        editingContactName = contact.getName();
        clearErrors();
        editName.setText(contact.getName());
        editPhone.setText(contact.getPhone());
        editEmail.setText(contact.getEmail());
        editName.requestFocus();
        updateFormMode();
        highlightEditingContact();
    }

    private void updateContact() {
        if (editingContactId == NO_CONTACT) {
            return;
        }
        ContactInput input = readValidatedInput();
        if (input == null) {
            return;
        }
        int rows = databaseHelper.updateContact(editingContactId, input.name, input.phone, input.email);
        if (rows == 0) {
            showMessage(getString(R.string.contact_update_failed));
        } else {
            showMessage(getString(R.string.contact_updated, input.name));
        }
        exitEditMode();
        refreshContacts();
    }

    private void exitEditMode() {
        editingContactId = NO_CONTACT;
        editingContactName = null;
        clearForm();
        updateFormMode();
        highlightEditingContact();
    }

    // ---------- Delete ----------

    private void confirmDelete(Contact contact) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_dialog_title)
                .setMessage(getString(R.string.delete_dialog_message, contact.getName()))
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.delete_dialog_confirm, (dialog, which) -> deleteContact(contact))
                .show();
    }

    private void deleteContact(Contact contact) {
        int rows = databaseHelper.deleteContact(contact.getId());
        if (rows == 0) {
            showMessage(getString(R.string.contact_delete_failed));
        } else {
            showMessage(getString(R.string.contact_deleted, contact.getName()));
        }
        // Don't leave the form pointing at a record that no longer exists.
        if (contact.getId() == editingContactId) {
            exitEditMode();
        }
        refreshContacts();
    }

    // ---------- Form helpers ----------

    /** Shows Save in create mode, or Update + Cancel in edit mode. */
    private void updateFormMode() {
        boolean editing = editingContactId != NO_CONTACT;
        buttonSave.setVisibility(editing ? View.GONE : View.VISIBLE);
        buttonUpdate.setVisibility(editing ? View.VISIBLE : View.GONE);
        buttonCancel.setVisibility(editing ? View.VISIBLE : View.GONE);
        textFormMode.setText(editing
                ? getString(R.string.mode_edit, editingContactName)
                : getString(R.string.mode_create));
    }

    /** Marks the row being edited in the list, or clears the selection. */
    private void highlightEditingContact() {
        listContacts.clearChoices();
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getId() == editingContactId) {
                listContacts.setItemChecked(i, true);
                break;
            }
        }
    }

    /** Returns the trimmed form values, or null after showing field errors if they are invalid. */
    private ContactInput readValidatedInput() {
        clearErrors();
        String name = textOf(editName);
        String phone = textOf(editPhone);
        String email = textOf(editEmail);

        View firstInvalid = null;
        if (name.isEmpty()) {
            layoutName.setError(getString(R.string.error_name_required));
            firstInvalid = editName;
        }
        if (!phone.isEmpty() && !Patterns.PHONE.matcher(phone).matches()) {
            layoutPhone.setError(getString(R.string.error_phone_invalid));
            if (firstInvalid == null) firstInvalid = editPhone;
        }
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError(getString(R.string.error_email_invalid));
            if (firstInvalid == null) firstInvalid = editEmail;
        }
        if (firstInvalid != null) {
            firstInvalid.requestFocus();
            return null;
        }
        // Optional fields are stored as NULL instead of empty strings.
        return new ContactInput(name, phone.isEmpty() ? null : phone, email.isEmpty() ? null : email);
    }

    private void clearForm() {
        editName.setText(null);
        editPhone.setText(null);
        editEmail.setText(null);
        clearErrors();
        editName.clearFocus();
        editPhone.clearFocus();
        editEmail.clearFocus();
    }

    /** Removes field errors and the space reserved for them below each field. */
    private void clearErrors() {
        for (TextInputLayout layout : new TextInputLayout[]{layoutName, layoutPhone, layoutEmail}) {
            layout.setError(null);
            layout.setErrorEnabled(false);
        }
    }

    private static String textOf(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /** Validated values typed into the form. */
    private static final class ContactInput {
        final String name;
        final String phone;
        final String email;

        ContactInput(String name, String phone, String email) {
            this.name = name;
            this.phone = phone;
            this.email = email;
        }
    }

    /** Keeps content clear of the status and navigation bars (edge-to-edge is enforced on API 35+). */
    static void applySystemBarInsets(View root) {
        int left = root.getPaddingLeft();
        int top = root.getPaddingTop();
        int right = root.getPaddingRight();
        int bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
