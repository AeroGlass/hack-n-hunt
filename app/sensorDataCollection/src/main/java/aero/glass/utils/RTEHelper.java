package aero.glass.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zolta on 2018. 04. 03..
 */

public class RTEHelper {
    private final File dbFile;

    public RTEHelper(File file) {
        dbFile = file;
    }

    public List<Bitmap> getImage(String base_table_name, long base_id) {
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        List<Bitmap> images = new ArrayList<Bitmap>();
        Cursor relation_cursor = sqLiteDatabase.rawQuery(
                "SELECT related_table_name, related_primary_column, mapping_table_name FROM gpkgext_relations " +
                        "WHERE base_table_name = '" + base_table_name + "' AND relation_name = 'media';", null);

        if (relation_cursor != null && relation_cursor.moveToFirst()) {
            do {
                String mapping_table = relation_cursor.getString(relation_cursor.getColumnIndex("mapping_table_name"));
                String related_table = relation_cursor.getString(relation_cursor.getColumnIndex("related_table_name"));
                String related_column = relation_cursor.getString(relation_cursor.getColumnIndex("related_primary_column"));
                Cursor image_cursor = sqLiteDatabase.rawQuery(
                        "SELECT data, content_type, id FROM " + related_table + " WHERE " + related_column + " IN ("
                                + "SELECT related_id FROM " + mapping_table + " WHERE base_id = " + base_id + ");", null);
                if (image_cursor != null && image_cursor.moveToFirst()) {
                    do {
                        byte[] data = image_cursor.getBlob(image_cursor.getColumnIndex("data"));
                        images.add(BitmapFactory.decodeByteArray(data, 0, data.length));
                    } while (image_cursor.moveToNext());
                }
            } while (relation_cursor.moveToNext());
        }
        sqLiteDatabase.close();
        return images;
    }

    public void createRTEMappingTables() {

        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        Cursor maxID_cursor = sqLiteDatabase.rawQuery("SELECT MAX(id) AS id FROM gpkgext_relations;", null);
        maxID_cursor.moveToFirst();
        int maxID = maxID_cursor.getInt(maxID_cursor.getColumnIndex("id")) + 1;

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS cnp_custom_photos;");
        sqLiteDatabase.execSQL("CREATE TABLE cnp_custom_photos(base_id INTEGER NOT NULL, related_id INTEGER NOT NULL);");
        sqLiteDatabase.delete("gpkgext_relations", "mapping_table_name = 'cnp_custom_photos'", null);
        sqLiteDatabase.execSQL("INSERT INTO gpkgext_relations VALUES (" + ++maxID + ", 'cnp_custom', 'fid', 'photos_custom', 'id', 'media', 'cnp_custom_photos');");

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS aoi_custom_photos;");
        sqLiteDatabase.execSQL("CREATE TABLE aoi_custom_photos(base_id INTEGER NOT NULL, related_id INTEGER NOT NULL);");
        sqLiteDatabase.delete("gpkgext_relations", "mapping_table_name = 'aoi_custom_photos'", null);
        sqLiteDatabase.execSQL("INSERT INTO gpkgext_relations VALUES (" + ++maxID + ", 'aoi_custom', 'fid', 'photos_custom', 'id', 'media', 'aoi_custom_photos');");

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS photos_custom;");
        sqLiteDatabase.execSQL("CREATE TABLE photos_custom(id INTEGER PRIMARY KEY, data BLOB NOT NULL, content_type TEXT NOT NULL);");

        sqLiteDatabase.close();
    }

    public void addImage(String base_table_name, long base_id, Bitmap image) {
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        Cursor relation_cursor = sqLiteDatabase.rawQuery(
                "SELECT related_table_name, related_primary_column, mapping_table_name FROM gpkgext_relations " +
                        "WHERE base_table_name = '" + base_table_name + "' AND relation_name = 'media';", null);
        relation_cursor.moveToFirst();
        String mapping_table = relation_cursor.getString(relation_cursor.getColumnIndex("mapping_table_name"));
        String related_table = relation_cursor.getString(relation_cursor.getColumnIndex("related_table_name"));
        String related_primary_column = relation_cursor.getString(relation_cursor.getColumnIndex("related_primary_column"));

        Cursor maxID_cursor = sqLiteDatabase.rawQuery("SELECT MAX(" + related_primary_column + ") AS id FROM " + related_table, null);
        maxID_cursor.moveToFirst();
        final int id = maxID_cursor.getInt(maxID_cursor.getColumnIndex("id")) + 1;
        ContentValues values = new ContentValues();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        values.put("data", stream.toByteArray());
        SQLiteStatement insertBlob = sqLiteDatabase.compileStatement("INSERT INTO " + related_table + " VALUES (?, ?, 'image/jpeg');");
        insertBlob.clearBindings();
        insertBlob.bindLong(1, id);
        insertBlob.bindBlob(2, stream.toByteArray());
        insertBlob.executeInsert();

        sqLiteDatabase.execSQL("INSERT INTO " + mapping_table + " VALUES (" + base_id + ", " + id + ");");
        sqLiteDatabase.close();

    }
}
