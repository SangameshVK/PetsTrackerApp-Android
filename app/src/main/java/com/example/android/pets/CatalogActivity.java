package com.example.android.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetDbHelper;
import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private PetDbHelper mDbHelper;
    private PetCursorAdapter mPetCursorAdapter;
    private final int PETS_LOADER_ID = 0;

    @Override
    protected void onStart() {
        super.onStart();
        //setAdapterToListView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        mDbHelper = new PetDbHelper(this);
        setAdapterAndInitLoader();
        //displayDatabaseInfoIntoTextView();//evolve 1 => Cursor loaded on background thread using a cursor loader
        //setAdapterWithCursorToListView(); //evolve 0 => Cursor loaded on main thread.

    }

    private void setAdapterAndInitLoader() {
        ListView petsListView = (ListView) findViewById(R.id.pets_list_view);
        View emptyView = findViewById(R.id.empty_view);
        petsListView.setEmptyView(emptyView);
        mPetCursorAdapter = new PetCursorAdapter(this, null);
        petsListView.setAdapter(mPetCursorAdapter);
        petsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.setData(ContentUris.withAppendedId(PetEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(PETS_LOADER_ID, null,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    private void insertDummyData() {
        try {
            ContentValues values = new ContentValues();
            String nullVal = null;
            values.put(PetEntry.COLUMN_PET_NAME, "Kadatya");
            values.put(PetEntry.COLUMN_PET_BREED, "SSBJ Naayi");
            values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
            values.put(PetEntry.COLUMN_PET_WEIGHT, 26);

            // PetDbHelper mDbHelper = new PetDbHelper(this); not needed in evolve 2

            // Create and/or open a database to read from it
            //SQLiteDatabase db = mDbHelper.getWritableDatabase(); //Not needed in evolve 2
            //long rowId = db.insert(PetEntry.TABLE_NAME, null, values); //evolve 1
            Uri rowUri = getContentResolver().insert(PetEntry.CONTENT_URI, values); //evolve 2
            Log.i("CatalogActivity", "New row ID: " + ContentUris.parseId(rowUri));
        }
        catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyData();
                //setAdapterToListView();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };
        return new CursorLoader(this, PetEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mPetCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPetCursorAdapter.swapCursor(null);
    }

    private void deleteAllPets() {
        int numOfPetsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        Toast.makeText(this, numOfPetsDeleted + " pets deleted", Toast.LENGTH_SHORT).show();
    }


    // Legacy code ahead========

    private void setAdapterWithCursorToListView() {
        ListView petsListView = (ListView) findViewById(R.id.pets_list_view);
        View emptyView = findViewById(R.id.empty_view);
        petsListView.setEmptyView(emptyView);
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER
        };
        Cursor cursor = getContentResolver().query(PetEntry.CONTENT_URI, projection, null, null, null);
        PetCursorAdapter petCursorAdapter = new PetCursorAdapter(this, cursor);
        petsListView.setAdapter(petCursorAdapter);
    }


    /**
     * Temporary Helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfoIntoTextView() {
        // Create and/or open a database to read from it
        //SQLiteDatabase db = mDbHelper.getReadableDatabase(); //Useless in evolve 2

        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };
        // Perform this raw SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table.
        //Cursor cursor = db.rawQuery("SELECT * FROM " + PetEntry.TABLE_NAME, null); //Evolve 0
        //Cursor cursor = db.query(PetEntry.TABLE_NAME, projection, null, null, null, null, null); //Evolve 1
        Cursor cursor = getContentResolver().query(PetEntry.CONTENT_URI, projection, null, null, null); //Evolve 2
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_pet);
            displayView.setText("Number of rows in pets database table: " + cursor.getCount());
            int idIndex = cursor.getColumnIndex(PetEntry._ID);
            int nameIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
            displayView.append("\n_ID - NAME - BREED - GENDER - WEIGHT\n");
            while (cursor.moveToNext()) {
                int id = cursor.getInt(idIndex);
                String name = cursor.getString(nameIndex);
                String breed = cursor.getString(breedIndex);
                int gender = cursor.getInt(genderIndex);
                int weight = cursor.getInt(weightIndex);
                displayView.append(String.format("%d - %s - %s - %d - %d\n", id, name, breed, gender, weight));
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }



}
