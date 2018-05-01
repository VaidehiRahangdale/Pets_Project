package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;



import com.example.android.pets.data.PetContract.PetEntry;

import java.net.URI;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    //log tag messages
    //public static final String LOG_TAG =PetProvider.class.getSimpleName();

    //URI matcher code for the content uri fro the pets table
    public static final int PETS = 100;
    //URI matcher code for the content uri for the single pet in the pets table.
    public static final int PETS_ID = 101;

    //Uri matcher object to match a content URI to the corresponding code.
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);

        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PETS_ID);
    }

    //Database helper object
    private PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        // Create and initialize a PetDbHelper object to gain access to the pets database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }



    //Updates the data at the given selection and selection arguments, with the new ContentValues.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(PetEntry.TABLE_NAME, projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case PETS_ID:

                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        //Set notification URI on the cursor.
        //so we know what content uri the cursor was created for.
        //if data at this uri changes then we know we need to update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    //Insert new data into the provider with the given ContentValues.
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    //Insert a pet into the database with the given content values. Return the new content URI
    // for that specific row in the database.
     private Uri insertPet(Uri uri, ContentValues values) {

         // Check that the name is not null
         String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
         if (name == null) {
             throw new IllegalArgumentException("Pet requires a name");
         }
        //Sanity Check for all the attributes
         Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
         if (gender == null || !PetEntry.isValidGender(gender)) {
             throw new IllegalArgumentException("Pet requires valid gender");
         }

         // If the weight is provided, check that it's greater than or equal to 0 kg
         Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
         if (weight != null && weight < 0) {
             throw new IllegalArgumentException("Pet requires valid weight");
         }


         // Get writeable database
         SQLiteDatabase database = mDbHelper.getWritableDatabase();

         // Insert the new pet with the given values
         long id = database.insert(PetEntry.TABLE_NAME, null, values);

         //Notify all the listeners that the data has changed for the pet content uri
         getContext().getContentResolver().notifyChange(uri, null);

         // Once we know the ID of the new row in the table,
         // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);

         // If the ID is -1, then the insertion failed. Log an error and return null.
         //if (id == -1) {
           //  Log.e(LOG_TAG, "Failed to insert row for " + uri);
             //return null;
         //}

    }

    //Delete the data at the given selection and selection arguments.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
       // Track the number of rows that were deleted
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                             break;
            case PETS_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
               //return database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                 break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

                                  if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
           }
              // Return the number of rows deleted
                             return rowsDeleted;
    }



    //Update
    @Override
    public int update(Uri uri,ContentValues contentValues ,String selection,String[] selectionArgs) {
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Returns the number of database rows affected by the update statement
        //return database.update(PetEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(PetEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
                    // given URI has changed
   if (rowsUpdated != 0) {
          getContext().getContentResolver().notifyChange(uri, null);
                   }
        // Return the number of rows updated
                                return rowsUpdated;
    }

    //Returns the MIME type of data for the content URI.
    @Override
    public String getType(Uri uri) {
        return null;
    }
}