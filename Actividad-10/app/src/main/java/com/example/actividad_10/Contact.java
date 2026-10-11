package com.example.actividad_10;

/** A single row of the contacts table. */
public class Contact {

    private final long id;
    private final String name;
    private final String phone;
    private final String email;

    public Contact(long id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
