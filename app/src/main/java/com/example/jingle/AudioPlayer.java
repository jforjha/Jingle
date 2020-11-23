package com.example.jingle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
//import android.media.MediaPlayer;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Random;

import android.media.ThumbnailUtils;

import static androidx.appcompat.widget.AppCompatDrawableManager.get;
import static com.example.jingle.AlbumDetailsAdapter.albumFiles;
import static com.example.jingle.ApplicationClass.ACTION_NEXT;
import static com.example.jingle.ApplicationClass.ACTION_PLAY;
import static com.example.jingle.ApplicationClass.ACTION_PREVIOUS;
import static com.example.jingle.ApplicationClass.CHANNEL_ID_2;
import static com.example.jingle.MainActivity.albums;
import static com.example.jingle.MainActivity.musicFiles;
import static com.example.jingle.MainActivity.repeatBoolean;
import static com.example.jingle.MainActivity.shuffleBoolean;

public  class AudioPlayer extends AppCompatActivity  implements ActionPlaying,ServiceConnection {
TextView songname,artistname,durationplayed,durationtotal;
ImageView cover_art,nextBtn,prevBtn,backBtn,shuffleBtn,repeatbtn;
FloatingActionButton playpauseBtn;
SeekBar seekbar;
int position=-1;
public static ArrayList<MusicFile>listSongs=new ArrayList<>();
static Uri uri;
//static MediaPlayer mediaPlayer;
private Handler handler =new Handler();
private Thread playThread,prevThread,nextThread;
MusicService musicService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setFulScreen();
        setContentView(R.layout.activity_audio_player);
        getSupportActionBar().hide();
        songname = findViewById(R.id.song_name);
        artistname = findViewById(R.id.artist_name);
        durationplayed = findViewById(R.id.duration_played);
        durationtotal = findViewById(R.id.duration_total);
        cover_art = findViewById(R.id.main_art);
        nextBtn = findViewById(R.id.next);
        prevBtn = findViewById(R.id.previous);
        backBtn=findViewById(R.id.back_button);
        shuffleBtn=findViewById(R.id.shuffle);
        repeatbtn=findViewById(R.id.repeat);
        playpauseBtn=findViewById(R.id.play_pause);
        seekbar=findViewById(R.id.Seek_Bar);

        getIntentMethod();

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(musicService!=null && fromUser)
                {
                    musicService.seekTo(progress*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        AudioPlayer.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService!=null)
                {
                    int mCurrentPosition=musicService.getCurrentPosition()/1000;
                    seekbar.setProgress(mCurrentPosition);
                    durationplayed.setText(FormattedTime(mCurrentPosition));
                }
                handler.postDelayed(this,1000);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shuffleBoolean)
                {
                    shuffleBoolean=false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
                }
                else
                {
                    shuffleBoolean=true;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });
        repeatbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatBoolean)
                {
                    repeatBoolean=false;
                    repeatbtn.setImageResource(R.drawable.ic_repeat_off);

                }
                else
                {
                    repeatBoolean=true;
                    repeatbtn.setImageResource(R.drawable.ic_repeat_on);
                }
            }
        });

     }

    private void setFulScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
protected void onResume(){
        Intent intent =new Intent(this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);
        playThreadbtn();
        nextThreadbtn();
        prevThreadbtn();
        super.onResume();
}

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(this);
    }

    private void prevThreadbtn() {
        prevThread =new Thread()
        {
            @Override
            public void run(){
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnCicked();
                    }
                });

            }
        };
        prevThread.start();
    }

    public void prevBtnCicked() {
        if(musicService.isPlaying())
        {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean&&!repeatBoolean)
            {
                position =getRandom(listSongs.size()-1);
            }
            else if(!shuffleBoolean && !repeatBoolean){


                position = ((position-1)<0?(listSongs.size()-1):(position-1));}

            uri =Uri.parse(String.valueOf(listSongs.get(position).getPath()));
          musicService.createMediaPlayer(position);
            metaData(uri);
            songname.setText(listSongs.get(position).getTitle());
            artistname.setText(listSongs.get(position).getArtist());
            seekbar.setMax(musicService.getDuration()/1000);
            AudioPlayer.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null)
                    {
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekbar.setProgress(mCurrentPosition);

                    }
                    handler.postDelayed(this,1000);
                }
            });
           musicService.OnCompleted();
            playpauseBtn.setImageResource(R.drawable.ic_pause);
            musicService.start();
        }
        else
        {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean&&!repeatBoolean)
            {
                position =getRandom(listSongs.size()-1);
            }
            else if(!shuffleBoolean && !repeatBoolean){


                position = ((position-1)<0?(listSongs.size()-1):(position-1));}
            uri =Uri.parse(String.valueOf(listSongs.get(position).getPath()));
           musicService.createMediaPlayer(position);
            metaData(uri);
            songname.setText(listSongs.get(position).getTitle());
            artistname.setText(listSongs.get(position).getArtist());
            seekbar.setMax(musicService.getDuration()/1000);
            AudioPlayer.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null)
                    {
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekbar.setProgress(mCurrentPosition);

                    }
                    handler.postDelayed(this,1000);
                }
            });
            playpauseBtn.setImageResource(R.drawable.ic_play);
           musicService.OnCompleted();

        }

    }

    private void nextThreadbtn() {

        nextThread =new Thread()
        {
            @Override
            public void run(){
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnCicked();
                    }
                });

            }
        };
        nextThread.start();


        
    }

    public void nextBtnCicked() {
if(musicService.isPlaying())
{
    musicService.stop();
    musicService.release();
    if(shuffleBoolean&&!repeatBoolean)
    {
        position =getRandom(listSongs.size()-1);
    }
    else if(!shuffleBoolean && !repeatBoolean){


    position = ((position+1)%listSongs.size());}
    uri =Uri.parse(String.valueOf(listSongs.get(position).getPath()));
  musicService.createMediaPlayer(position);
    metaData(uri);
    songname.setText(listSongs.get(position).getTitle());
    artistname.setText(listSongs.get(position).getArtist());
    seekbar.setMax(musicService.getDuration()/1000);
    AudioPlayer.this.runOnUiThread(new Runnable() {
        @Override
        public void run() {
            if(musicService!=null)
            {
                int mCurrentPosition=musicService.getCurrentPosition()/1000;
                seekbar.setProgress(mCurrentPosition);

            }
            handler.postDelayed(this,1000);
        }
    });
    playpauseBtn.setBackgroundResource(R.drawable.ic_pause);
musicService.start();
}
else
{
    musicService.stop();
    musicService.release();
    if(shuffleBoolean&&!repeatBoolean)
    {
        position =getRandom(listSongs.size()-1);
    }
    else if(!shuffleBoolean && !repeatBoolean){


        position = ((position+1)%listSongs.size());}
    uri =Uri.parse(String.valueOf(listSongs.get(position).getPath()));
musicService.createMediaPlayer(position);
    metaData(uri);
    songname.setText(listSongs.get(position).getTitle());
    artistname.setText(listSongs.get(position).getArtist());
    seekbar.setMax(musicService.getDuration()/1000);
    AudioPlayer.this.runOnUiThread(new Runnable() {
        @Override
        public void run() {
            if(musicService!=null)
            {
                int mCurrentPosition=musicService.getCurrentPosition()/1000;
                seekbar.setProgress(mCurrentPosition);

            }
            handler.postDelayed(this,1000);
        }
    });
    musicService.OnCompleted();
    musicService.showNotification(R.drawable.ic_baseline_pause_circle_outline_24);
    playpauseBtn.setBackgroundResource(R.drawable.ic_play);
    musicService.start();

}

    }


    private int getRandom(int i) {

        Random random =new Random();
        return random.nextInt(i+1);
    }

    private void playThreadbtn() {
        playThread =new Thread()
        {
            @Override
                    public void run(){
                super.run();
                playpauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playpauseBtnCicked();
                    }
                });

        }
        };
        playThread.start();
    }

    public void playpauseBtnCicked() {
        if(musicService.isPlaying())
        {
            playpauseBtn.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
            musicService.pause();
            seekbar.setMax(musicService.getDuration()/1000);
            AudioPlayer.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null)
                    {
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekbar.setProgress(mCurrentPosition);

                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_play);
            playpauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
        else
        {
            playpauseBtn.setImageResource(R.drawable.ic_pause);
            musicService.start();
            seekbar.setMax(musicService.getDuration()/1000);
            AudioPlayer.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null)
                    {
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekbar.setProgress(mCurrentPosition);

                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_baseline_pause_circle_outline_24);
            playpauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private String FormattedTime(int mCurrentPosition) {

String Totalout="";
String Totalnew="";
String Seconds = String.valueOf(mCurrentPosition%60);
String minutes = String.valueOf(mCurrentPosition/60);
Totalout=minutes+":"+Seconds;
Totalnew = minutes + ":" +"0"+Seconds;
if(Seconds.length()==1)
{
    return Totalnew;
}
else
{
   return Totalout;
}



    }

    private void getIntentMethod() {
        position=getIntent().getIntExtra("position",-1);
        String sender =getIntent().getStringExtra("sender");
        if(sender!=null&&sender.equals("Album details"))
        {
            listSongs=albumFiles;
        }
        else{
        listSongs=musicFiles;}
        if(listSongs!=null)
        {
            playpauseBtn.setImageResource(R.drawable.ic_pause);
            uri =Uri.parse(String.valueOf(listSongs.get(position).getPath()));
        }

    Intent intent =new Intent(this,MusicService.class);
        intent.putExtra("servicePosition",position);
        startService(intent);

metaData(uri);
    }
    private void metaData(Uri uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal=Integer.parseInt(listSongs.get(position).getDuration())/1000;
        durationtotal.setText(FormattedTime(durationTotal));
        byte[] art =retriever.getEmbeddedPicture();
        Bitmap bitmap;

        if(art!=null)
        {

            bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
            ImageAnimation(this,cover_art,bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch =palette.getDominantSwatch();
                    if(swatch!=null)
                    {
                        ImageView gredient =findViewById(R.id.main_art);
                        RelativeLayout mcontainer =findViewById(R.id.container);
                        gredient.setBackgroundResource(R.drawable.gradient_bg);
                        mcontainer.setBackgroundResource(R.drawable.main_logo);
                        GradientDrawable gradientDrawableBg =new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(),swatch.getRgb()});
                        mcontainer.setBackground(gradientDrawableBg);
                        songname.setTextColor(swatch.getTitleTextColor());
                        artistname.setTextColor(swatch.getBodyTextColor());

                    }
                    else
                    {
                        ImageView gredient =findViewById(R.id.main_art);
                        RelativeLayout mcontainer =findViewById(R.id.container);
                        gredient.setBackgroundResource(R.drawable.gradient_bg);
                        mcontainer.setBackgroundResource(R.drawable.main_logo);
                        GradientDrawable gradientDrawable =new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000,0x00000000});
                        gredient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg =new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000,0x00000000});
                        mcontainer.setBackground(gradientDrawableBg);
                        songname.setTextColor(Color.WHITE);
                        artistname.setTextColor(Color.DKGRAY);

                    }
                }
            });


        }
        else
        {
            Glide.with(this).asBitmap().load(R.drawable.main_logo).into(cover_art);
            ImageView gredient =findViewById(R.id.main_art);
            RelativeLayout mcontainer =findViewById(R.id.container);
            gredient.setBackgroundResource(R.drawable.gradient_bg);
            mcontainer.setBackgroundResource(R.drawable.main_logo);
            songname.setTextColor(Color.WHITE);
            artistname.setTextColor(Color.DKGRAY);
        }

    }
    public void ImageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap)
    {
        Animation animOut = AnimationUtils.loadAnimation(context,android.R.anim.fade_out);
        final Animation animIn = AnimationUtils.loadAnimation(context,android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);

    }





    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
MusicService.MyBinder myBinder = (MusicService.MyBinder) service;

musicService =myBinder.getService();
musicService.setCallBack(this);
        Toast.makeText(this,"Connected" + musicService,Toast.LENGTH_SHORT).show();
        seekbar.setMax(musicService.getDuration()/1000);
        metaData(uri);
        songname.setText(listSongs.get(position).getTitle());
        musicService.OnCompleted();
        artistname.setText(listSongs.get(position).getTitle());
        musicService.showNotification(R.drawable.ic_baseline_pause_circle_outline_24);


    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService =null;

    }


}