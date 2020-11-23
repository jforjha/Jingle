package com.example.jingle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;


import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import static com.example.jingle.ApplicationClass.ACTION_NEXT;
import static com.example.jingle.ApplicationClass.ACTION_PLAY;
import static com.example.jingle.ApplicationClass.ACTION_PREVIOUS;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final int REQUEST_CODE = 1;
    static ArrayList<MusicFile> musicFiles;
    static boolean shuffleBoolean = false, repeatBoolean = false;
    static ArrayList<MusicFile> albums =new ArrayList<>();
    private String MY_SORT_PREF = "SortOrder";
    ImageButton play;
    TextView title;
    NotificationManager notificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission();


    }

    private void createChannel(){
    {if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
        NotificationChannel channel =new NotificationChannel(ApplicationClass.CHANNEL_ID_2,"jingle",NotificationManager.IMPORTANCE_LOW);
        notificationManager =getSystemService(NotificationManager.class);
        if(notificationManager!=null)
        {
            notificationManager.createNotificationChannel(channel);
        }}
    }}

    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CODE);
        } else {

            musicFiles = getAllAudio(this);
            initViewPager();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            musicFiles = getAllAudio(this);
            initViewPager();

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CODE);
        }
    }

    private void initViewPager() {
        ViewPager viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongFragment(), "Songs");
        viewPagerAdapter.addFragments(new AlbumFragment(), "Albums");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }





    public static class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        void addFragments(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    public ArrayList<MusicFile> getAllAudio(Context context) {
        SharedPreferences preferences =getSharedPreferences(MY_SORT_PREF,MODE_PRIVATE);
         String sortOrder = preferences.getString("sorting","sortByName");


         String order = null ;


        ArrayList<String>duplicate =new ArrayList<>();
        ArrayList<MusicFile> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        switch (sortOrder)
                {
        case "sortByName":
            order =MediaStore.MediaColumns.DISPLAY_NAME + "";
            break;
        case "sortByDate":
        order =MediaStore.MediaColumns.DATE_ADDED + "";
        break;
        case "sortBySize":
        order =MediaStore.MediaColumns.SIZE + "";
        break; }
        String[] projection = {MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID

        };
        Cursor cursor = context.getContentResolver().query(uri,
                projection,
                null,
                null, order);

        if (cursor != null) {
            while (cursor.moveToNext()) {

                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);
                MusicFile musicFiles = new MusicFile(path, title, artist, album, duration, id);
                Log.e("path" + path, "Album" + album);
                tempAudioList.add(musicFiles);
                if(!duplicate.contains(album)){
                    albums.add(musicFiles);
                    duplicate.add(album);
                }
            }

            cursor.close();

        }
        return tempAudioList;
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.search,menu);
        MenuItem menuItem = menu.findItem(R.id.search_option);
        SearchView searchView =(SearchView)menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return  super.onCreateOptionsMenu(menu);

    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor =getSharedPreferences(MY_SORT_PREF,MODE_PRIVATE).edit();
        switch (item.getItemId())
        {
            case R.id.by_name:
                editor.putString("sorting","sortByName");
                editor.apply();
                this.recreate();
                break;

            case R.id.by_date:
                editor.putString("sorting","sortByDate");
                editor.apply();
                this.recreate();
                break;

            case R.id.by_size:
                editor.putString("sorting","sortBySize");
                editor.apply();
                this.recreate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
