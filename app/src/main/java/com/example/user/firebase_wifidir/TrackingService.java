package com.example.user.firebase_wifidir;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rvalerio.fgchecker.AppChecker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class TrackingService extends Service {

    private static final String TAG= "TrackingService";
    private FusedLocationProviderClient mFusedLocationClient;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;

    Object lon_user_test1;
    Object lat_user_test1;
    Object lon_user_test2;
    Object lat_user_test2;

    double long_user_test1;
    double lati_user_test1;
    double long_user_test2;
    double lati_user_test2;

    private Timer timer = new Timer();
    private android.os.Handler handler = new android.os.Handler();



    WifiManager wifiManager;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    long screentime=0;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    public final static long APP_START_TIME = System.currentTimeMillis();

    static final int MESSAGE_READ = 1;


    static boolean disconnect = false;

    static String appName = "12345";
    static int count = 0;


    boolean isconnectedpeer = false;
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1001;
    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";
    public static final String CHANNEL_3_ID = "channel3";

//    private static final int PEER_CONNECTION_USER_ACCEPT =   BASE + 6;

    static PendingIntent pendingIntent;


    private List<String> listPackageName = new ArrayList<>();
    private List<String> listAppName = new ArrayList<>();

    private AppChecker appChecker;
    String current = "NULL";
    String previous = "NULL";
    String timeleft = "NULL";
    String current_app="NULL";

    Boolean isConnected=false;
    long startTime = 0;
    long previousStartTime = 0;
    long endTime = 0;
    long totlaTime = 0;

    long scrrenoff=0;
    private long screenOnTime;
    private final long TIME_ERROR = 1000;





    public void onCreate() {
        super.onCreate();
        //  buildNotification();
        loginToFirebase();
        requestLocationUpdates();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("use", "new ");
            startMyOwnForeground();

        } else {
            Log.d("use", "old");
            startForeground(1, new Notification());

        }


        mAuth= FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        FirebaseUser user=mAuth.getCurrentUser();
        userID= user.getUid();


        mAuthListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser();
                if(user!=null){
                    toastMessage("Successfully signed in with: " + user.getEmail());
                    Intent s= new Intent(getApplicationContext(),Location_start.class);
                    startActivity(s);
                    Log.d(TAG,"onAuthStateChanged:signed_in:" +user.getUid());
                }
                else{
                    toastMessage("Successfully signed out.");
                    Log.d(TAG,"onAuthStateChanged:signed_out:");
                }
            }
        };

        // Write a message to the database
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Object value=dataSnapshot.getValue();

                if(userID.equals("eKmHurwRmBfyexLY3uBgXSaJ0k62")){
                   lon_user_test2=dataSnapshot.child(userID).child("Location").child("Longitude").getValue();
                   lat_user_test2=dataSnapshot.child(userID).child("Location").child("Latitude").getValue();
                    Object el_user_test2= dataSnapshot.child(userID).child("Location").child("Elevation").getValue();
                    if(lon_user_test2!=null){
                        long_user_test2= (double) lon_user_test2;
                        Log.d(TAG,"ANOTHER: "+lon_user_test2);
                    }
                    if(lat_user_test2!=null){
                        lati_user_test2= (double) lat_user_test2;
                    }
                    Log.d(TAG,"showdata: Longitude : "+lon_user_test2);
                    Log.d(TAG,"showdata: Latitude : "+lat_user_test2);
                    Log.d(TAG,"showdata: Elevation: "+el_user_test2);
                }

                if(userID.equals("sJ3TpdbBmIaGbYR3yqxd2blHDN62")){
                    lon_user_test1= dataSnapshot.child(userID).child("Location").child("Longitude").getValue();
                    lat_user_test1= dataSnapshot.child(userID).child("Location").child("Latitude").getValue();
                    Object el_user_test1=dataSnapshot.child(userID).child("Location").child("Elevation").getValue();
                    if(lon_user_test1!=null){
                        long_user_test1= (double) lon_user_test1;
                    }
                    if(lat_user_test1!=null){
                        lati_user_test1= (double) lat_user_test1;
                    }

                    Log.d(TAG,"showdata2: Longitude : "+lon_user_test1);
                    Log.d(TAG,"showdata2: Latitude : "+lat_user_test1);
                    Log.d(TAG,"showdata2: Elevation: "+el_user_test1);
                }

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                   chck();
                                if(distance(lati_user_test1,long_user_test1,lati_user_test2,long_user_test2)<0.1){
                                    Log.d("shimmy","done");
                                    toastMessage("Devices Co-located");
                                }


                            }
                        });
                    }
                }, 0, 1000);


//                if(lon_user_test1!=null){
//                    long_user_test1= (double) lon_user_test1;
//                }
//              //  double long_user_test1= (double) lon_user_test1;
//                if(lat_user_test1!=null){
//                    lati_user_test1= (double) lat_user_test1;
//                }
//                if(lon_user_test2!=null){
//                    long_user_test2= (double) lon_user_test2;
//                }
//                if(lat_user_test2!=null){
//                    lati_user_test2= (double) lat_user_test2;
//                }

//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//
////                                if(lon_user_test1!=null){
////                                    long_user_test1= (double) lon_user_test1;
////                                }
////                                //  double long_user_test1= (double) lon_user_test1;
////                                if(lat_user_test1!=null){
////                                    lati_user_test1= (double) lat_user_test1;
////                                }
////                                if(lon_user_test2!=null){
////                                    long_user_test2= (double) lon_user_test2;
////                                }
////                                if(lat_user_test2!=null){
////                                    lati_user_test2= (double) lat_user_test2;
////                                }
//////
////                                if(distance(lati_user_test1,long_user_test1,lati_user_test2,long_user_test2)<0.1){
////                                    toastMessage("Neaarby devices");
////                                }
//
//                                chck();
//
//
//                            }
//                        });
//                    }
//                }, 0, 1000);



//                if(distance(lati_user_test1,long_user_test1,lati_user_test2,long_user_test2)<0.1){
//                   toastMessage("Neaarby devices");
//                }
//                else if(distance(lati_user_test1,long_user_test1,lati_user_test2,long_user_test2)>50){
//                    toastMessage("Not Neaarby devices");
//                }

                Log.d(TAG, "Value is: " + value);
                //showData(dataSnapshot);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        initialWork();
        //  chec();
        tim();
        // disconnect();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        exqListener();




                    }
                });
            }
        }, 0, 5000);

        registerReceiver(mReceiver, mIntentFilter);
        appName = "12345";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // exqListener();
        installedapp();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                retrieveStats();
            }
        }, 0, 1000);


    }

    public void chck(){

        if(distance(lati_user_test1,long_user_test1,lati_user_test2,long_user_test2)<0.1){
            Log.d("shimmy","done");
            toastMessage("Devices Co-located");
        }

    }




//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        mAuth.addAuthStateListener(mAuthListener);
//    }


    private void loginToFirebase() {

//        FirebaseUser user= mAuth.getCurrentUser();
//        String userID= user.getUid();
//        myRef.child(userID).child("Location").child("Latitude").setValue(la);
    }

    private void startMyOwnForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.noti)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);

        }
    }

    private void requestLocationUpdates(){
        LocationRequest request = new LocationRequest();


//Specify how often your app should request the device’s location//

        request.setInterval(1000);
        //  request.setInterval(1000);

//Get the most accurate location data available//

        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        //If the app currently has access to the location permission...//

        if (permission == PackageManager.PERMISSION_GRANTED){
//...then request location updates//

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

//Get a reference to the database, so your app can perform read and write operations//

                    android.location.Location location = locationResult.getLastLocation();
                    if(location!=null){
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        double elevation= location.getAltitude();

                        FirebaseUser user= mAuth.getCurrentUser();
                        final String userID= user.getUid();
                        myRef.child(userID).child("Location").child("Latitude").setValue(latitude);
                        myRef.child(userID).child("Location").child("Longitude").setValue(longitude);
                        myRef.child(userID).child("Location").child("Elevation").setValue(elevation);

                        }





                }
            }, null);

        }

    }



    /** calculates the distance between two locations in MILES
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2*/

    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometers

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist;
    }




    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    private void exqListener() {
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (myKM.inKeyguardRestrictedInputMode()){
            if (mManager != null) {


                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d("conn", "disc");
                        //   createNotification(CHANNEL_1_ID, "Discovery", "Discovering Peers", "Tap to reopen App");
                        Toast.makeText(getApplicationContext(), "Discovering for peers", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d("conn", "failed");
                        // createNotification(CHANNEL_1_ID, "Discovery", "Failed to Discover Peers", "Tap to reopen App");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            getApplicationContext().startForegroundService(new Intent(getApplicationContext(), TrackingService.class));
                        } else {
                            getApplicationContext().startService(new Intent(getApplicationContext(), MainActivity.class));

                        }

                    }
                });
            }
        }
        else{
            if (mManager != null) {


                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d("conn", "disc");
                        //   createNotification(CHANNEL_1_ID, "Discovery", "Discovering Peers", "Tap to reopen App");
                        Toast.makeText(getApplicationContext(), "Discovering for peers", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d("conn", "failed");
                        // createNotification(CHANNEL_1_ID, "Discovery", "Failed to Discover Peers", "Tap to reopen App");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            getApplicationContext().startForegroundService(new Intent(getApplicationContext(), TrackingService.class));
                        } else {
                            getApplicationContext().startService(new Intent(getApplicationContext(), MainActivity.class));

                        }

                    }
                });
            }
        }
//        if (mManager != null) {
//
//
//        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
//
//            @Override
//            public void onSuccess() {
//                Log.d("conn", "disc");
//             //   createNotification(CHANNEL_1_ID, "Discovery", "Discovering Peers", "Tap to reopen App");
//                Toast.makeText(getApplicationContext(), "Discovering for peers", Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onFailure(int reason) {
//                Log.d("conn", "failed");
//                // createNotification(CHANNEL_1_ID, "Discovery", "Failed to Discover Peers", "Tap to reopen App");
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    getApplicationContext().startForegroundService(new Intent(getApplicationContext(), WiFiTwoWay.class));
//                } else {
//                    getApplicationContext().startService(new Intent(getApplicationContext(), MainActivity.class));
//
//                }
//
//            }
//        });
//    }
    }

    private void retrieveStats() {


    }

    private void initialWork() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mReceiver = new WiFiDirectBroadcastTwoWay(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {

        @Override
        public void onPeersAvailable(final WifiP2pDeviceList peerList) {
            Log.d("Peers", "Getting Peers");
            if (count != 1) {
                if (!peerList.getDeviceList().equals(peers)) {
                    peers.clear();
                    peers.addAll(peerList.getDeviceList());


                    deviceNameArray = new String[peerList.getDeviceList().size()];

                }
                KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                if (myKM.inKeyguardRestrictedInputMode()){
                    if (peerList.getDeviceList().size() != 0) {

                        deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                        int index = 0;


                        for (WifiP2pDevice devices : peerList.getDeviceList()) {
                            deviceNameArray[0] = devices.deviceName;
                            deviceArray[0] = devices;
                            Log.d("WTH",devices.deviceName);
                            //   Toast.makeText(getApplicationContext(), "Device nearby is: " +devices.deviceName, Toast.LENGTH_SHORT).show();
                            if(devices.deviceName.equals("Pixel 3a")){
                                isConnected=true;
                                Toast.makeText(getApplicationContext(), "Device nearby is: " +devices.deviceName, Toast.LENGTH_SHORT).show();
                            }
                            else{
                                isConnected=false;
                            }
//                        if(devices.deviceName.equals("LG nexus")){
//                        isConnected=true;
//                            Toast.makeText(getApplicationContext(), "Device nearby is: " +devices.deviceName, Toast.LENGTH_SHORT).show();
//                        }
//                        else{
//                            isConnected=false;
//                        }

                            if(isConnected){
                                KeyguardManager myKM2 = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                                if (myKM2.inKeyguardRestrictedInputMode()) {
                                    Log.d("Locked","Good");
                                } else {
                                    AppChecker appChecker = new AppChecker();
                                    current_app = appChecker.getForegroundApp(getBaseContext());
                                    if(current_app!=null){
                                        screentime=System.currentTimeMillis();
                                        if(screentime!=0 && startTime!=screenOnTime) {
                                            endTime = screentime - startTime;
                                            Log.d("Glass", "TISS " + current_app + " App time" +  endTime + "\t" + startTime + "\t" + screentime);
                                        }
                                    }
                                    if(endTime>60000){
                                        Toast.makeText(getApplicationContext(), "STOP!!", Toast.LENGTH_SHORT).show();
                                        Log.d("jet", String.valueOf(endTime));
//                        Intent notifi = new Intent(getBaseContext(),NotificationReceive.class);
//                    //    intent.putExtra("Technique",techniqueAsk);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(notifi);
                                        //startservice(notif):
                                    }
                                }
                            }
                        }


                        if (peers.size() == 0) {
                            Toast.makeText(getApplicationContext(), "No pairs available", Toast.LENGTH_SHORT).show();
                            return;
                            //    createNotification(CHANNEL_3_ID, "Connection", "NO device found", "Tap to reopen App");
                        }



                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                        //   createNotification(CHANNEL_3_ID, "Device", "Connected to the device", deviceArray[0].toString());
                        Log.d("part", deviceArray[0].toString());
                        final WifiP2pDevice device = deviceArray[0];
                        final WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = device.deviceAddress;

                    }
                }
                else{
                    if (peerList.getDeviceList().size() != 0) {

                        deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                        int index = 0;


                        for (WifiP2pDevice devices : peerList.getDeviceList()) {
                            deviceNameArray[0] = devices.deviceName;
                            deviceArray[0] = devices;
                            Log.d("WTH",devices.deviceName);
                            //   Toast.makeText(getApplicationContext(), "Device nearby is: " +devices.deviceName, Toast.LENGTH_SHORT).show();
//                            if(devices.deviceName.equals("Pixel 3a")){
//                                isConnected=true;
//                                Toast.makeText(getApplicationContext(), "Device nearby is: " +devices.deviceName, Toast.LENGTH_SHORT).show();
//                            }
//                            else{
//                                isConnected=false;
//                            }
                            if(devices.deviceName.equals("LG nexus")){
                                isConnected=true;
                                Toast.makeText(getApplicationContext(), "Device nearby is: " +devices.deviceName, Toast.LENGTH_SHORT).show();
                            }
                            else{
                                isConnected=false;
                            }

                            if(isConnected){
                                KeyguardManager myKM2 = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                                if (myKM2.inKeyguardRestrictedInputMode()) {
                                    Log.d("Locked","Good");
                                } else {
                                    AppChecker appChecker = new AppChecker();
                                    current_app = appChecker.getForegroundApp(getBaseContext());
                                    if(current_app!=null){
                                        screentime=System.currentTimeMillis();
                                        if(screentime!=0 && startTime!=screenOnTime) {
                                            endTime = screentime - startTime;
                                            Log.d("Glass", "TISS " + current_app + " App time" +  endTime + "\t" + startTime + "\t" + screentime);
                                        }
                                    }
                                    if(endTime>60000){
                                        Toast.makeText(getApplicationContext(), "STOP!!", Toast.LENGTH_SHORT).show();
                                        Log.d("jet", String.valueOf(endTime));
//                        Intent notifi = new Intent(getBaseContext(),NotificationReceive.class);
//                    //    intent.putExtra("Technique",techniqueAsk);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(notifi);
                                        //startservice(notif):
                                    }
                                }
                            }
                        }


                        if (peers.size() == 0) {
                            Toast.makeText(getApplicationContext(), "No pairs available", Toast.LENGTH_SHORT).show();
                            return;
                            //    createNotification(CHANNEL_3_ID, "Connection", "NO device found", "Tap to reopen App");
                        }



                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                        //   createNotification(CHANNEL_3_ID, "Device", "Connected to the device", deviceArray[0].toString());
                        Log.d("part", deviceArray[0].toString());
                        final WifiP2pDevice device = deviceArray[0];
                        final WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = device.deviceAddress;

                    }

                }

//                if (peerList.getDeviceList().size() != 0) {
//
//                    deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
//                    int index = 0;
//
//
//                    for (WifiP2pDevice devices : peerList.getDeviceList()) {
//                        deviceNameArray[0] = devices.deviceName;
//                        deviceArray[0] = devices;
//                        Log.d("WTH",devices.deviceName);
//                     //   Toast.makeText(getApplicationContext(), "Device nearby is: " +devices.deviceName, Toast.LENGTH_SHORT).show();
//                        if(devices.deviceName.equals("Pixel 3a")){
//                            isConnected=true;
//                            Toast.makeText(getApplicationContext(), "Device nearby is: " +devices.deviceName, Toast.LENGTH_SHORT).show();
//                        }
//                        else{
//                            isConnected=false;
//                        }
////                        if(devices.deviceName.equals("LG nexus")){
////                        isConnected=true;
////                            Toast.makeText(getApplicationContext(), "Device nearby is: " +devices.deviceName, Toast.LENGTH_SHORT).show();
////                        }
////                        else{
////                            isConnected=false;
////                        }
//
//                        if(isConnected){
//                            KeyguardManager myKM2 = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//                            if (myKM2.inKeyguardRestrictedInputMode()) {
//                                Log.d("Locked","Good");
//                            } else {
//                                AppChecker appChecker = new AppChecker();
//                                current_app = appChecker.getForegroundApp(getBaseContext());
//                                if(current_app!=null){
//                                    screentime=System.currentTimeMillis();
//                                    if(screentime!=0 && startTime!=screenOnTime) {
//                                        endTime = screentime - startTime;
//                                        Log.d("Glass", "TISS " + current_app + " App time" +  endTime + "\t" + startTime + "\t" + screentime);
//                                    }
//                                }
//                                if(endTime>60000){
//                                    Toast.makeText(getApplicationContext(), "STOP!!", Toast.LENGTH_SHORT).show();
//                                    Log.d("jet", String.valueOf(endTime));
////                        Intent notifi = new Intent(getBaseContext(),NotificationReceive.class);
////                    //    intent.putExtra("Technique",techniqueAsk);
////                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                        startActivity(notifi);
//                                    //startservice(notif):
//                                }
//                            }
//                        }
//                    }
//
//
//                    if (peers.size() == 0) {
//                        Toast.makeText(getApplicationContext(), "No pairs available", Toast.LENGTH_SHORT).show();
//                        return;
//                        //    createNotification(CHANNEL_3_ID, "Connection", "NO device found", "Tap to reopen App");
//                    }
//
//
//
//                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
//                 //   createNotification(CHANNEL_3_ID, "Device", "Connected to the device", deviceArray[0].toString());
//                    Log.d("part", deviceArray[0].toString());
//                    final WifiP2pDevice device = deviceArray[0];
//                    final WifiP2pConfig config = new WifiP2pConfig();
//                    config.deviceAddress = device.deviceAddress;
//
//                }


            }


            if (peerList.getDeviceList().size() ==0) {
                Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_SHORT).show();
                //  createNotification(CHANNEL_3_ID, "Connection", "NO device found", "Tap to reopen App");
               // disconnect();
            }
        }


    };

    public void installedapp() {
        List<PackageInfo> packageList = getPackageManager().getInstalledPackages(0);
        //  List<ApplicationInfo> applications = getPackageManager().getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        // Log.d("pkg inofo->", appInfo.packageName);
        for (int i = 0; i < packageList.size(); i++) {
            PackageInfo packageInfo = packageList.get(i);

            String appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            String pacName = packageInfo.packageName;

            listAppName.add(appName);
            listPackageName.add(pacName);


            Log.e("APPNAME", "app is " + appName + "----" + pacName + "\n");

            String app = appName + "\t" + pacName + "\t" + "\n";


            try {
                File data3 = new File("appname.txt");
                FileOutputStream fos = openFileOutput("appname.txt", Context.MODE_APPEND);
                fos.write((app).getBytes());
                fos.close();
//                FileWriter fw =new FileWriter("appname.txt", false);
//                fw.write(app);
//                fw.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    public void aggregationapp() {
        String lastknown = "NULL";
        String appName = "NULL";
        String previous1 = "NULL";
//        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
//        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        Date systemDate = Calendar.getInstance().getTime();
        String myDate = sdf.format(systemDate);
//        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
//        if (appList != null && appList.size() > 0) {
//            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
//            for (UsageStats usageStats : appList) {
//                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
//            }
//            if (mySortedMap != null && !mySortedMap.isEmpty()) {
//                String dateFormat = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy HH:mm:ss", new java.util.Date()));
//                current = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
//
//                //  lastknown = String.valueOf(new Date(mySortedMap.get(mySortedMap.lastKey()).getLastTimeUsed()));
//                //  int index = listPackageName.indexOf(previous);
//                // appName = listAppName.get(index);
        AppChecker appChecker = new AppChecker();
        current = appChecker.getForegroundApp(getBaseContext());
        //  screentime=System.currentTimeMillis();

        java.text.DateFormat df = new java.text.SimpleDateFormat("hh:mm:ss");
        {
            if (current != null) {
//                screentime=System.currentTimeMillis();
//                if(screentime!=0 && startTime!=screenOnTime){
//                    endTime=screentime-startTime;
//                    Log.d("ZHK", "TISS " + current + " App time" + totlaTime + "\t" + endTime + "\t" + startTime + "\t" + screentime );
                //    }
                if (!current.equals(previous)) {
                    Log.d("panda", "zebra" + previous);
                    Log.d("side", "dish" + current);
                    Log.d("tims", "Horton" + myDate);
//
//
                    //  previous = appChecker.getForegroundApp(getBaseContext());
                    startTime = System.currentTimeMillis();


//
                    int index = listPackageName.indexOf(previous);
                    if (index < 0) {
                        appName = "Null";
                    } else {
                        appName = listAppName.get(index);
                    }


                    if (startTime != previousStartTime && previousStartTime != 0) {
                        totlaTime = 0;

                        totlaTime = startTime - previousStartTime;

                        //  totlaTime=previousStartTime-startTime;
//
                    }
//                    endTime=screentime-previousStartTime;
//
//                    Log.d("fuss", String.valueOf(endTime));


//
                    // long startTime=0;
//                        previous = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
//
////
//                       if (startTime != previousStartTime) {
//                           totlaTime = startTime - previousStartTime;
//                         //  totlaTime=previousStartTime-startTime;
////
//                       }
////
                    Log.d("FinalZ2", "app name " + previous + " App time" + totlaTime + "\t" + previousStartTime + "\t" + startTime);
//
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                            // TODO: Consider calling
//                            //    ActivityCompat#requestPermissions
//                            // here to request the missing permissions, and then overriding
//                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                            //                                          int[] grantResults)
//                            // to handle the case where the user grants the permission. See the documentation
//                            // for ActivityCompat#requestPermissions for more details.
//                            return;
//                        }
//
//                        // Added to chcke if the phone is locked vs unlocked//
//
                    String status = "NULL";
                    KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                    if (myKM.inKeyguardRestrictedInputMode()) {
                        status = "locked";
                    } else {
                        status = "unlocked";
                    }
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
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (!current.equals("NULL")) {
                        if (location != null) {
                            double longitude = location.getLongitude();
                            double latitude = location.getLatitude();
                            String date = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy HH:mm:ss", new java.util.Date()));
                            String appt = date + "\t" + latitude + "\t" + longitude + "\t" + previous + "\t" + appName + "\t" + totlaTime + "\t" + status + "\n";
                            try {
                                File data7 = new File("individual.txt");
                                FileOutputStream fos = openFileOutput("individual.txt", Context.MODE_APPEND);
                                fos.write((appt).getBytes());
                                fos.close();
                            } catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
//
                            previousStartTime = startTime;
                        }
                    }
                } else if (current.equals(previous)) {
//
//
//                        //endTime = startTime;
//
//                        lastknown = String.valueOf(new Date(mySortedMap.get(mySortedMap.lastKey()).getLastTimeUsed()));
                    Log.d("Birds", "crow" + lastknown);
                }
                previous = current;

                Log.d("zoo", "animals" + previous);
//
//
            }

        }

//
//
//            } else {
//                ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
//                List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
//            }
//        }
    }

    public void tim(){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        aggregationapp();




                    }
                });
            }
        }, 0, 1000);
    }



}
