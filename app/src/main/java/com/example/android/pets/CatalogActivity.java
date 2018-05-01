/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ListView;
import android.widget.TextView;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

//Displays list of pets that were entered and stored in the app.

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int PET_LOADER = 0;

    PetCursorAdapter mCursorAdaptor;

    private PetDbHelper mDbHelper;

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

        // Find the ListView which will be populated with the pet data
        //ListView petListView = (ListView) findViewById(R.id.list);

       // mDbHelper = new PetDbHelper(this);
         //displayDatabaseInfo();
    }


    //After the user has clicked the save in editor activity, the list will refresh with new pets in database.
    //This will llow the row count on the screen to increase
    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }



     //Temporary helper method to display information in the onscreen TextView about the state of
     //the pets database.

    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        //Create an instance of PetDbHelper class
        //PetDbHelper mDbHelper = new PetDbHelper(this);
        // Create and/or open a database to read from it
        //SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Perform this raw SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table
        //Cursor cursor = db.rawQuery("SELECT * FROM " + PetEntry.TABLE_NAME, null);

        //Define the projection
        String[] projection =
                {
                        PetEntry._ID,
                        PetEntry.COLUMN_PET_NAME,
                        PetEntry.COLUMN_PET_BREED,
                        PetEntry.COLUMN_PET_GENDER,
                        PetEntry.COLUMN_PET_WEIGHT

                };

        //perform a query on the provider using content resolver
        Cursor cursor = getContentResolver().query(PetEntry.CONTENT_URI, projection, null, null, null);

        //find the listview which will be populated with pets data.
        ListView petListView = (ListView) findViewById(R.id.list);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        PetCursorAdapter adapter = new PetCursorAdapter(this, cursor);

        // Attach the adapter to the ListView.
         petListView.setAdapter(adapter);
    }

    private void insertPet(){
        //get the data repository in writable format
       // SQLiteDatabase db =  mDbHelper.getWritableDatabase();
        //create a content values object.
        ContentValues values = new ContentValues();

        values.put(PetEntry.COLUMN_PET_NAME,"Toto");
        values.put(PetEntry.COLUMN_PET_BREED,"Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER,PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT,7);

        //long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);

        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
               insertPet();
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      String[] projection = {
              PetEntry._ID,
              PetEntry.COLUMN_PET_NAME,
              PetEntry.COLUMN_PET_BREED
      };
        return new CursorLoader(this,
                PetEntry.CONTENT_URI,projection,null,null,null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdaptor.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdaptor.swapCursor(null);

    }
}
