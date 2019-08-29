package com.test.bxdnotes;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private static final int READ_BLOCK_SIZE = 100;
    public String TAG="NOTES_TAG";
    public EditText etMain;
    public TextView tvPath;
    private final int requestcodeOPEN=1, requestcodeSAVE=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etMain = (EditText)findViewById(R.id.etMain);
        tvPath=(TextView)findViewById(R.id.tvPath);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:

                break;
            case R.id.action_save:
                browseFile("save");
                break;
            case R.id.action_open:
                browseFile("open");
                break;
            case R.id.action_copy_all:
                setClipboard(this,etMain.getText().toString());
                Toast.makeText(this, "copied to Clipboard", Toast.LENGTH_SHORT).show();
                break;

        }


        return super.onOptionsItemSelected(item);
    }

    public String browseFile(String mode){
        String fileName, filePath;
        if (mode=="open"){
            Intent openFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            openFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
            openFileIntent.setType("*/*");
            Intent intent = Intent.createChooser(openFileIntent, "Choose a file");
            startActivityForResult(intent, requestcodeOPEN);
        }

        if (mode=="save"){
            Intent saveFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            saveFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
            saveFileIntent.setType("*/*");

            try {
                startActivityForResult(saveFileIntent, requestcodeSAVE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No file found!", Toast.LENGTH_SHORT).show();
            }
        }
        return  tvPath.getText().toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==requestcodeOPEN && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            tvPath.setText(uri.getPath());
            readtxtfile(uri);
        }

        if (requestCode==requestcodeSAVE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            tvPath.setText(uri.getPath());
            writetxtfile(uri);

        }
    }

    public void writetxtfile(Uri uri) {
        BufferedWriter writer=null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(getContentResolver().openOutputStream(uri)));
            writer.write(etMain.getText().toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readtxtfile(final Uri uri){

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                BufferedReader reader=null;
                try {
                    reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
                    String str = "" ;
                    String allStr="";


                    while((str=reader.readLine())!= null){
                        allStr+="\n\r"+str;
                        Log.d(TAG, "Reading file: "+"\n\r"+str);
                        //todo: removal of last \n\r required within readtxtfile
                    }
                    etMain.setText(allStr);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    public void setClipboard(Context context, String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }
}
