package com.example.android.pets;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;
    private boolean mPetEdited = false;

    private Uri mUri;
    private long mPetNumber = -1;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = PetEntry.GENDER_UNKNOWN;

    // private PetDbHelper mDbHelper; older evolve

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetEdited = true;
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        mNameEditText.setOnTouchListener(onTouchListener);
        mBreedEditText.setOnTouchListener(onTouchListener);
        mWeightEditText.setOnTouchListener(onTouchListener);
        mGenderSpinner.setOnTouchListener(onTouchListener);
        //mDbHelper = new PetDbHelper(this); older evolve
        setupSpinner();

        mUri = getIntent().getData();
        if (mUri != null) {
            setTitle(R.string.editor_activity_title_edit_pet);
            mPetNumber = ContentUris.parseId(mUri);
            Toast.makeText(this, "Pet Number " + mPetNumber, Toast.LENGTH_SHORT).show();
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetEntry.GENDER_UNKNOWN; // Unknown
            }
        });
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mPetEdited) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        if (mUri == null) {
            //getMenuInflater().
            //actionDelete.setVisibility(View.GONE);
        }
        return true;
    }

    private void savePet() {
        try {
            //SQLiteDatabase db = mDbHelper.getWritableDatabase(); //Not needed in evolve 2
            //long rowId = db.insert(PetEntry.TABLE_NAME, null, getContentValues()); //evolve 1
            if (mUri == null) {
                Uri rowUri = getContentResolver().insert(PetEntry.CONTENT_URI, getContentValues());
                Toast.makeText(this, "Pet Number " + ContentUris.parseId(rowUri) + " Added", Toast.LENGTH_LONG).show();
            }
            else {
                int rowsUpdated = getContentResolver().update(mUri, getContentValues(), null, null);
                Toast.makeText(this, "Pet Number " + mPetNumber + " Updated", Toast.LENGTH_SHORT).show();
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, mNameEditText.getText().toString().trim());
        values.put(PetEntry.COLUMN_PET_BREED, mBreedEditText.getText().toString().trim());
        String petWeight = mWeightEditText.getText().toString().trim();
        if (!petWeight.isEmpty()) {
            values.put(PetEntry.COLUMN_PET_WEIGHT, Integer.parseInt(petWeight));
        }
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        return values;
    }

    private void deletePet() {
        if (mUri == null) {
            Toast.makeText(this, "Pet doesn't exist", Toast.LENGTH_SHORT).show();
            return;
        }
        int rowsDeleted = getContentResolver().delete(mUri, null, null);
        if (rowsDeleted == -1) {
            Toast.makeText(this, "Pet Number " + mPetNumber + " could not be deleted", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Pet Number " + mPetNumber + " Deleted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                deletePet();
                finish();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mPetEdited) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Navigate back to parent activity (CatalogActivity)
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, mUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        populateFields(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        clearFields();
    }

    private void populateFields(Cursor cursor) {
        int nameIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
        int breedIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
        int genderIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
        int weightIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
        if (cursor.moveToNext()){
            mNameEditText.setText(cursor.getString(nameIndex));
            mBreedEditText.setText(cursor.getString(breedIndex));
            mWeightEditText.setText(cursor.getInt(weightIndex) + "");
            mGenderSpinner.setSelection(cursor.getInt(genderIndex));
        }
    }

    private void clearFields() {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
    }
}