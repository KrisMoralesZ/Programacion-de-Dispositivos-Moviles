# Inventory Management Application for Abarrotes El Buen Precio

Integrative academic project: an Android application written in **Kotlin** that manages a small
grocery store's product inventory with a **local SQLite database**.

> **Note on the case study.** "Abarrotes El Buen Precio" is a *hypothetical* business used only as
> the context for this project. The problem description and expected benefits below describe a
> typical small neighborhood grocery store; they are not claims about any real company.

---

## 1. Project Title

**Inventory Management Application for Abarrotes El Buen Precio** (app name: *Inventory Manager*).

## 2. Company Description

Abarrotes El Buen Precio is a hypothetical small neighborhood grocery store (*tienda de abarrotes*).
It is run by its owner with the help of one or two employees and sells everyday products to nearby
households: rice, beans, cooking oil, canned food, beverages, snacks, cleaning and personal-care
products. A typical store of this kind:

- Handles a catalog of a few hundred products with frequently changing prices.
- Restocks from suppliers several times a week.
- Has no dedicated IT staff and no point-of-sale or inventory software.
- Has an owner or employee who already carries an Android smartphone during the workday.

## 3. Problem Statement

The store manages its inventory **manually** (notebooks, loose paper lists or memory). As a result:

- **Records become outdated.** When a price changes or a product is discontinued, the paper list is
  not always corrected, so different people work with different information.
- **Available stock is hard to know.** Checking whether a product is available requires physically
  checking the shelves or the storeroom.
- **Information is hard to modify or remove.** Crossing out and rewriting entries makes lists
  difficult to read, and obsolete products remain on them.
- **Records can be lost or damaged**, and nobody can search them quickly.

**Impact on daily operations:** time is lost answering customer questions about prices and
availability, restocking decisions are made on guesswork (leading to stock-outs or over-buying),
and prices may be charged inconsistently.

## 4. Justification

A mobile application with a local database is an appropriate solution for this context because:

- **Low cost and familiar hardware.** It runs on an Android phone the owner already has; no
  computer, server or paid service is needed.
- **Works offline.** SQLite stores data on the device itself, so the app works without an internet
  connection, which a small store cannot always guarantee.
- **Structured, persistent records.** A database table enforces a consistent format (name,
  description, price, quantity) and keeps data after the app or phone is restarted.
- **Easy to update.** Creating, editing and deleting a record takes seconds, so the inventory can be
  kept current.
- **Appropriate scale.** A single-store catalog of a few hundred products is well within what
  SQLite on a phone handles comfortably.
- **Academic value.** It demonstrates Kotlin development in Android Studio and direct use of the
  Android SQLite APIs (`SQLiteOpenHelper`, `SQLiteDatabase`, `ContentValues`, `Cursor`).

## 5. General Objective

To develop an Android mobile application, using Kotlin and Android Studio, that enables a small
grocery store to efficiently manage its inventory by creating, saving, viewing, modifying and
deleting product records stored in a local SQLite database.

## 6. Specific Objectives

**Inventory management**

1. Register new products with a name, description, price and available stock quantity.
2. Display every registered product in a list showing its name, price and stock.
3. Show the complete information of a selected product.
4. Modify the information of an existing product, identified by its unique database ID.
5. Delete a product after an explicit confirmation from the user.
6. Validate input so that only complete, valid records (positive price, non-negative whole quantity)
   are stored.

**Kotlin development**

7. Implement all application logic in Kotlin, separating responsibilities into a data model
   (`Product`), validation (`ProductValidator`), data access (`InventoryDatabaseHelper`) and UI
   (`MainActivity`, `ProductFormActivity`, `ProductAdapter`).
8. Build the interface with XML layouts and traditional Android Views.

**SQLite integration**

9. Create and manage the `inventory.db` database through a class extending `SQLiteOpenHelper`.
10. Perform insert, query, update and delete operations with `ContentValues` and parameterized
    selection arguments, closing cursors and the database correctly.
11. Verify that records persist after the application is closed and reopened.

## 7. Scope

**Included**

- Full CRUD (create, read, update, delete) of products in a local SQLite database.
- Product list sorted alphabetically, with an empty-inventory message and a product counter.
- Detail dialog, edit form, and delete confirmation dialog.
- Input validation with per-field error messages and success/error feedback messages (Toasts).
- Support for portrait and landscape orientation on standard phone screen sizes.
- Automated unit tests (validation) and instrumented tests (database operations).

**Excluded**

- User accounts, login or permissions per employee.
- Synchronization between devices, cloud storage or backups to a server.
- Sales/point-of-sale registration, automatic stock decrement, purchase orders or suppliers.
- Barcode scanning, product photos, categories, reports or low-stock alerts.
- Search and filtering of the product list.

## 8. Target Users

- **Store owner:** maintains the product catalog, prices and stock levels.
- **Store employees:** consult prices and availability, and update stock after restocking.

Users are assumed to be comfortable with basic smartphone use but have no technical training, so
the interface uses plain language, few screens and clear feedback.

## 9. Technology Stack

| Component            | Technology                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| IDE                  | Android Studio                                                              |
| Language             | Kotlin (AGP 9 built-in Kotlin support)                                      |
| Build system         | Gradle (Kotlin DSL) with the Android Gradle Plugin 9.4.1, Gradle 9.6.0     |
| Database             | SQLite, local to the device, via `SQLiteOpenHelper` / `SQLiteDatabase`      |
| User interface       | XML layouts, Android Views (`ListView`, `TextInputLayout`, dialogs), Material Components |
| Android SDK          | `minSdk` 36, `targetSdk`/`compileSdk` 37                                     |
| Testing              | JUnit 4 (local unit tests), AndroidX Test (instrumented tests)             |

No Room, Firebase, cloud database or external server is used.

## 10. Database Design

- **Database file:** `inventory.db` (stored in the app's private `databases/` directory).
- **Schema version:** 1.
- **Helper class:** `InventoryDatabaseHelper` (extends `SQLiteOpenHelper`).

**Table `products`**

| Column        | Data type | Constraints                  | Description                          |
|---------------|-----------|------------------------------|--------------------------------------|
| `id`          | INTEGER   | PRIMARY KEY AUTOINCREMENT    | Unique identifier of the product     |
| `name`        | TEXT      | NOT NULL                     | Product name (max. 100 characters)   |
| `description` | TEXT      | (nullable)                   | Optional description (max. 500 chars); stored as NULL when empty |
| `price`       | REAL      | NOT NULL                     | Unit price (> 0, at most 2 decimals, ≤ 999,999.99) |
| `quantity`    | INTEGER   | NOT NULL                     | Units in stock (0 – 1,000,000)        |

```sql
CREATE TABLE products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    price REAL NOT NULL,
    quantity INTEGER NOT NULL
);
```

**Data-access methods in `InventoryDatabaseHelper`**

| Method                         | SQL operation | Notes                                                    |
|--------------------------------|---------------|----------------------------------------------------------|
| `insertProduct(product)`       | INSERT        | Uses `ContentValues`; returns the new id or -1 on error  |
| `getAllProducts()`             | SELECT        | Ordered by name (case-insensitive); cursor closed with `use` |
| `getProductById(id)`           | SELECT … WHERE id = ? | Parameterized selection argument                 |
| `updateProduct(product)`       | UPDATE … WHERE id = ? | Uses `ContentValues`; returns true if one row changed |
| `deleteProduct(id)`            | DELETE … WHERE id = ? | Returns true if one row was deleted               |

`onCreate()` creates the table. `onUpgrade()` is written for incremental, data-preserving
migrations (`if (oldVersion < N) …`); since version 1 is the initial schema, it contains no steps
yet. Validation rules live in `ProductValidator`, so the database layer only receives valid data.

## 11. Functional Requirements

| ID   | Operation | Requirement                                                                                     |
|------|-----------|-------------------------------------------------------------------------------------------------|
| RF-1 | Create    | The user can register a product with name, description, price and stock quantity.             |
| RF-2 | Create    | Name, price and quantity are required; price must be > 0 with ≤ 2 decimals; quantity must be a whole number ≥ 0. Errors are shown on the field. |
| RF-3 | Create    | A valid product is saved in SQLite and a success (or error) message is displayed.             |
| RF-4 | Read      | The main screen lists all products from SQLite showing name, price and stock.                  |
| RF-5 | Read      | Tapping a product shows its complete information (ID, name, description, price, stock).        |
| RF-6 | Read      | When there are no products, an "inventory is empty" message is displayed.                      |
| RF-7 | Update    | From the details dialog, *Edit* opens a form pre-filled with the product's data.               |
| RF-8 | Update    | Saving writes the changes to the record identified by its ID and the list is refreshed.       |
| RF-9 | Delete    | From the details dialog (*Delete*) or by long-pressing a product, a confirmation dialog is shown. |
| RF-10| Delete    | On confirmation the record is deleted by its ID, the list is refreshed and feedback is shown; cancelling leaves the record unchanged. |

**How to use the app**

- **Add:** tap **ADD PRODUCT**, fill in the form, tap **SAVE PRODUCT**.
- **View:** tap any product in the list.
- **Edit:** tap a product → **EDIT** → change fields → **SAVE CHANGES**.
- **Delete:** tap a product → **DELETE** (or long-press it) → confirm **DELETE**.

## 12. Expected Results

*These are expected benefits for a store that adopts the application; they have not been measured
in a real business.*

- A single, always-current digital list of products, prices and stock instead of paper records.
- Faster answers to price and availability questions.
- Fewer inconsistent prices, since each product has one record that is edited in place.
- Better restocking decisions, because current stock is visible at a glance.
- Obsolete products can be removed cleanly, keeping the catalog readable.

## 13. Testing Plan

Test environment used: Android emulator **Actividad8_API36** (Android 16 / API 36, 1080×2400),
debug build installed from this project.

### 13.1 Automated tests

| Suite | Location | What it covers | Result |
|-------|----------|----------------|--------|
| `ProductValidatorTest` (8 tests) | `app/src/test/…` | Trimming/parsing, optional description, zero stock allowed, comma decimal separator, missing required fields, invalid prices (text, `0`, negative, 3 decimals, too large), invalid quantities (negative, decimal, text, too large), over-long texts, inclusive limits | **Passed** (`./gradlew testDebugUnitTest`) |
| `InventoryDatabaseHelperTest` (7 tests) | `app/src/androidTest/…` | Insert + read by id, empty list and alphabetical order, empty description round-trip, update affects only the selected id, delete affects only the selected id, update/delete of a missing id return false, persistence after closing and reopening a database file | **Passed** on the API 36 emulator (`./gradlew connectedDebugAndroidTest`) |

### 13.2 Manual test cases (executed on the emulator)

| ID | Operation | Steps | Expected result | Result |
|----|-----------|-------|-----------------|--------|
| T-01 | Read (empty) | Launch app with no data | "The inventory is empty" message, "0 products registered" | Passed |
| T-02 | Validation | Open *Add product*, tap *Save* with all fields empty | Errors on name, price and quantity; nothing saved | Passed |
| T-03 | Validation | Price `0` | "The price must be greater than 0" | Passed |
| T-04 | Validation | Price `1.999` | "Use at most 2 decimal places" | Passed |
| T-05 | Validation | Type `-3` in quantity and `-5` in price | The numeric keyboard filters the `-` sign, so a negative value cannot be entered (fields show `3` and `5`). The negative-value rule itself is verified by the unit tests. | Passed |
| T-06 | Create | Save "Arroz 1 kg", description, `25.50`, `10` | "Product saved" toast; product appears in the list | Passed |
| T-07 | Create | Save a product with empty description and one with stock `0` | Both saved; list shows "Stock: 1 unit" / "Stock: 0 units" | Passed |
| T-08 | Read | View list with 3 products | Sorted by name with name, price (`$25.50`) and stock | Passed |
| T-09 | Read | Tap a product | Dialog shows ID, description, price and stock | Passed |
| T-10 | Update | *Edit* "Arroz 1 kg" | Form titled *Edit product*, pre-filled with ID 1's data | Passed |
| T-11 | Update | Change to "Arroz 2 kg", `47.00`, `6`, *Save changes* | List refreshed with new values; other products unchanged | Passed |
| T-12 | Update | Change stock in the edit form, rotate to landscape, rotate back, save | Typed value is kept and saved | Passed |
| T-13 | Delete | *Delete* → *Cancel* | Confirmation dialog shown; product count unchanged | Passed |
| T-14 | Delete | *Delete* → *Delete* | Product removed, list and counter refreshed | Passed |
| T-15 | Delete | Long-press a product | Same confirmation dialog appears | Passed |
| T-16 | Persistence | Force-stop the app and relaunch | The same products and values are shown | Passed |
| T-17 | Persistence | Copy `databases/inventory.db` from the device and inspect it with `sqlite3` | Schema matches section 10; rows match the UI (deleted row absent) | Passed |
| T-18 | Stability | Check `logcat` crash buffer after the session | No crashes for the app | Passed |

## 14. Limitations

- **Local only.** The database lives on a single device. Records are **not synchronized** between
  phones, so two employees with separate phones will have separate inventories.
- **Data loss risk.** Uninstalling the app or clearing its data deletes the inventory. Only
  Android's standard auto-backup (if enabled on the device) may preserve it; the app has no export
  or backup function of its own.
- **Manual stock updates.** Stock is not decremented automatically by sales; it must be edited by
  hand.
- **No search or categories.** With hundreds of products, finding one requires scrolling the
  alphabetical list.
- **No access control.** Anyone using the phone can modify or delete products.
- **Single currency format.** Prices are displayed as `$0.00`; no currency or locale configuration.
- **Device requirements.** The project targets Android 16 (`minSdk` 36), inherited from the
  existing project template; older Android versions are not supported.
- **Testing scope.** Testing was done on one emulator configuration (API 36 phone). It was not
  tested on physical devices, tablets or Genymotion.

---

## Appendix: Building and Running

1. Open the `Actividad-12` folder in **Android Studio** and let Gradle sync.
2. Start an Android emulator (API 36 or newer) or connect a device with Android 16+.
3. Select the **app** run configuration and press **Run**, or from a terminal:
   ```bash
   ./gradlew installDebug
   ```
4. Run the tests:
   ```bash
   ./gradlew testDebugUnitTest          # validation unit tests (JVM)
   ./gradlew connectedDebugAndroidTest  # SQLite tests on a running emulator/device
   ```
   Note: `connectedDebugAndroidTest` uninstalls the app when it finishes, which removes the data
   saved on that emulator.

**Source files** (`app/src/main/java/com/example/actividad_12/`)

| File | Responsibility |
|------|----------------|
| `Product.kt` | Data model of a product record |
| `ProductValidator.kt` | Validation of form input (no Android dependencies) |
| `InventoryDatabaseHelper.kt` | SQLite database creation and all CRUD operations |
| `ProductAdapter.kt` | Adapter that renders products in the `ListView` |
| `MainActivity.kt` | Inventory list, details dialog, delete confirmation |
| `ProductFormActivity.kt` | Add / edit form |
| `ViewInsets.kt` | Keeps content clear of system bars and keyboard (edge-to-edge) |
