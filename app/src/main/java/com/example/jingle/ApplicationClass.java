package com.example.jingle;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ApplicationClass extends Application {
    public static final  String CHANNEL_ID_1 ="channel1";
    public static final  String CHANNEL_ID_2 ="channel1";
    public static final  String ACTION_PREVIOUS ="PREVIOUS";
    public static final  String ACTION_NEXT ="NEXT";
    public static final  String ACTION_PLAY ="PLAY";


   public static Notification notification;



}
