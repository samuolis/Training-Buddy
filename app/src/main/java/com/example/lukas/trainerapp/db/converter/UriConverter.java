package com.example.lukas.trainerapp.db.converter;

import android.net.Uri;

import androidx.room.TypeConverter;

public class UriConverter {
    @TypeConverter
    public static Uri toUri(String uri){
        return uri == null ? null : Uri.parse(uri);
    }

    @TypeConverter
    public static String toString(Uri uri){
        return uri == null ? null : uri.toString();
    }
}
