package com.uranites.shhhquiet;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocationService extends Service {

    public static final String TAG = "TAG";
    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseAuth fAuth;

    public static double latitude;
    public static double longitude;

    String sLat;
    String sLon;
    String fLat;
    String fLon;

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                latitude = locationResult.getLastLocation().getLatitude();
                longitude = locationResult.getLastLocation().getLongitude();

                //Toast.makeText(LocationService.this, "LON = "+longitude+ " LAT = "+latitude, Toast.LENGTH_SHORT).show();

                sLat = String.valueOf(latitude);
                sLon = String.valueOf(longitude);
                fLat = sLat.substring(0,6);
                fLon = sLon.substring(0,6);

                db = FirebaseFirestore.getInstance();
                fAuth = FirebaseAuth.getInstance();

                String emailId = fAuth.getCurrentUser().getEmail();

                DocumentReference docRef = db.collection(emailId).document("workloc");
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot Latitude: " + document.getString("lat"));
                                Log.d(TAG, "DocumentSnapshot Longitude: " + document.getString("lon"));

                                String wlat = document.getString("lat");
                                String wlon = document.getString("lon");

                                //Toast.makeText(LocationService.this, wlat.getClass().getSimpleName(), Toast.LENGTH_SHORT).show();

                                if (fLat.equals(wlat) && fLon.equals(wlon))
                                {
                                    adjustAudio(true);
                                }
                                else
                                {
                                    adjustAudio(false);
                                }

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void adjustAudio(boolean setMute) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int adJustMute;
            if (setMute) {
                adJustMute = AudioManager.ADJUST_MUTE;
            } else {
                adJustMute = AudioManager.ADJUST_UNMUTE;
            }
            audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, adJustMute, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, adJustMute, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, adJustMute, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, adJustMute, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, adJustMute, 0);
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, setMute);
            audioManager.setStreamMute(AudioManager.STREAM_ALARM, setMute);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, setMute);
            audioManager.setStreamMute(AudioManager.STREAM_RING, setMute);
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, setMute);
        }
    }

    private void startLocationService() {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelId
        );
        builder.setSmallIcon(R.mipmap.shhhquietlogo);
        builder.setContentTitle("Shhh Quiet Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Shhh Quiet Location Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
    }

    private void stopLocationService()
    {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null)
        {
            String action = intent.getAction();
            if(action != null)
            {
                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE))
                {
                    startLocationService();
                }
                else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE))
                {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
