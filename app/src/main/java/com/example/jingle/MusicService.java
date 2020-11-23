package com.example.jingle;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.jingle.MusicFile;

import java.util.ArrayList;

import static com.example.jingle.ApplicationClass.ACTION_NEXT;
import static com.example.jingle.ApplicationClass.ACTION_PLAY;
import static com.example.jingle.ApplicationClass.ACTION_PREVIOUS;
import static com.example.jingle.ApplicationClass.CHANNEL_ID_2;
import static com.example.jingle.AudioPlayer.listSongs;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    MyBinder mBinder =new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFile>musicFiles = new ArrayList<>();
int position=-1;
    Uri uri;
ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat =new MediaSessionCompat(getBaseContext(),"Our Jingle");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind","Method");
        return mBinder;

    }


    public class MyBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition =intent.getIntExtra("servicePosition",-1);
        String actionName = intent.getStringExtra("ActionName");

        if(myPosition!=-1){
        playMedia(myPosition);}
        if(actionName!=null){
            switch (actionName){
            case "playPause":
            Toast.makeText(this,"PlayPause",Toast.LENGTH_SHORT).show();
            if(actionPlaying!=null)
            {
                Log.e("Inside","Action");
                actionPlaying.playpauseBtnCicked();
            }
            break;
                case "next":
                    Toast.makeText(this,"Next",Toast.LENGTH_SHORT).show();
                    if(actionPlaying!=null){
                        Log.e("Inside","Action");
                        actionPlaying.nextBtnCicked();
                    }
                    break;
                case "previous":
                    Toast.makeText(this,"Previous",Toast.LENGTH_SHORT).show();
                    if(actionPlaying!=null){
                        Log.e("Inside","Action");
                        actionPlaying.prevBtnCicked();
                    }
                    break;

            }

        }
        return START_STICKY;
    }

    private void playMedia(int Startposition) {
        musicFiles =listSongs;
        position=Startposition;
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(musicFiles!=null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else
        {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    void start(){
        mediaPlayer.start();
    }
    boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }
    void stop()
    {
        mediaPlayer.start();
    }
    void release(){
        mediaPlayer.release();
    }
    int getDuration(){
        return mediaPlayer.getDuration();
    }
    void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    void createMediaPlayer(int positionInner){
        position = positionInner;
        uri = Uri.parse(String.valueOf(musicFiles.get(position).getPath()));

        mediaPlayer =MediaPlayer.create(getBaseContext(),uri);
    }
    void pause(){
        mediaPlayer.pause();
    }
    void OnCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(actionPlaying!=null){
            actionPlaying.nextBtnCicked();
            if(mediaPlayer!=null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
                OnCompleted();

            } } }
    void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;

    }
    public void showNotification(int playPauseBtn)
    {
        Intent intent = new Intent(this,AudioPlayer.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent,0);
        Intent prevIntent =new Intent(this,NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this,0,intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent =new Intent(this,NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this,
                0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        Intent nextIntent =new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent =PendingIntent.getBroadcast(this,
                0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture=null;
        picture =getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb=null;
        if(picture!=null){
            thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);

        }
        else{
            thumb=BitmapFactory.decodeResource(getResources(),R.drawable.main_logo);

        }
        Notification notification=new NotificationCompat.Builder(this,CHANNEL_ID_2).setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_skip_previous,"Previous",prevPendingIntent)
                .addAction(playPauseBtn,"Pause",pausePendingIntent)
                .addAction(R.drawable.ic_skipnext,"Next",nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        startForeground(0,notification);
    }

    private byte[] getAlbumArt(Uri uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] art=retriever.getEmbeddedPicture();
        retriever.release();
        return  art;
    }

}
