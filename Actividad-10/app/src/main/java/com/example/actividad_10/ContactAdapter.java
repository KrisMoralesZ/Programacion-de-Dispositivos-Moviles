package com.example.actividad_10;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

/** Shows each contact in a row with its own delete button. */
public class ContactAdapter extends ArrayAdapter<Contact> {

    /** Called when the delete button of a row is tapped. */
    public interface OnDeleteClickListener {
        void onDeleteClick(Contact contact);
    }

    private final OnDeleteClickListener deleteListener;

    public ContactAdapter(Context context, List<Contact> contacts, OnDeleteClickListener deleteListener) {
        super(context, 0, contacts);
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = LayoutInflater.from(getContext()).inflate(R.layout.item_contact, parent, false);
            // ImageButton makes itself focusable in its constructor, overriding the XML attribute.
            // A focusable child stops the ListView from receiving row clicks, so undo it here.
            row.findViewById(R.id.buttonDeleteContact).setFocusable(false);
        }

        Contact contact = getItem(position);
        TextView textName = row.findViewById(R.id.textContactName);
        TextView textPhone = row.findViewById(R.id.textContactPhone);
        TextView textEmail = row.findViewById(R.id.textContactEmail);
        ImageButton buttonDelete = row.findViewById(R.id.buttonDeleteContact);

        textName.setText(contact.getName());
        textPhone.setText(TextUtils.isEmpty(contact.getPhone())
                ? getContext().getString(R.string.no_phone) : contact.getPhone());
        textEmail.setText(TextUtils.isEmpty(contact.getEmail())
                ? getContext().getString(R.string.no_email) : contact.getEmail());
        buttonDelete.setContentDescription(
                getContext().getString(R.string.delete_contact_description, contact.getName()));
        buttonDelete.setOnClickListener(v -> deleteListener.onDeleteClick(contact));
        return row;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
