package com.example.outsidethebox;

import android.content.Context;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class GlobalVariables implements Serializable {

    private static final int NUM_OF_LEVELS = 1;
    public static boolean[] levels = new boolean[NUM_OF_LEVELS];


    public static void SaveData(Context app){

        try {
            FileOutputStream file=app.openFileOutput("my_data",Context.MODE_PRIVATE);
            ObjectOutputStream os=new ObjectOutputStream(file);
            os.writeObject(new GlobalVariables());
            os.close();
            file.close();

            Log.d("DataStorageService","Data Saved!");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void LoadData(Context app)  {


        try {
            FileInputStream file = app.openFileInput("my_data");
            ObjectInputStream os=new ObjectInputStream(file);
            os.readObject();
            os.close();
            file.close();

            Log.d("DataStorageService","Data Loaded!");


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

}