package com.ab.hicarerun.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ab.hicarerun.BaseActivity;
import com.ab.hicarerun.BaseApplication;
import com.ab.hicarerun.R;
import com.ab.hicarerun.databinding.ActivityHomeBinding;
import com.ab.hicarerun.fragments.FaceRecognizationFragment;
import com.ab.hicarerun.fragments.HomeFragment;
import com.ab.hicarerun.fragments.NotificationFragment;
import com.ab.hicarerun.network.NetworkCallController;
import com.ab.hicarerun.network.NetworkResponseListner;
import com.ab.hicarerun.network.models.HandShakeModel.HandShake;
import com.ab.hicarerun.network.models.LoginResponse;
import com.ab.hicarerun.network.models.LogoutResponse;
import com.ab.hicarerun.network.models.UpdateAppModel.UpdateData;
import com.ab.hicarerun.service.LocationManager;
import com.ab.hicarerun.service.ServiceLocationSend;
import com.ab.hicarerun.service.listner.LocationManagerListner;
import com.ab.hicarerun.utils.AppUtils;
import com.ab.hicarerun.utils.DownloadApk;
import com.ab.hicarerun.utils.HandShakeReceiver;
import com.ab.hicarerun.utils.SharedPreferencesUtility;

import java.util.Calendar;
import java.util.List;

import io.realm.RealmResults;

public class HomeActivity extends BaseActivity implements FragmentManager.OnBackStackChangedListener, LocationManagerListner {
    ActivityHomeBinding mActivityHomeBinding;

    private static final int LOGOUT_REQ = 1000;
    private static final int UPDATE_REQ = 2000;
    private Location mLocation;
    private LocationManagerListner mListner;
    public static final String ARG_HANDSHAKE = "ARG_HANDSHAKE";
    public static final String ARG_EVENT = "ARG_EVENT";
    public static final String ARG_USER = "ARG_USER";
    RealmResults<LoginResponse> LoginRealmModels = null;
    List<HandShake> items = null;
    boolean isClicked = false;
    String userName = "";
    String userId = "";
    private android.location.LocationManager locationManager;
    private AlarmManager mAlarmManager = null;
    private PendingIntent pendingUpdateIntent = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityHomeBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_home);
        setSupportActionBar(mActivityHomeBinding.toolbar);
        initNavigationDrawer();

        try {
            isClicked = getIntent().getExtras().getBoolean(ARG_EVENT, false);
            LoginRealmModels =
                    BaseApplication.getRealm().where(LoginResponse.class).findAll();
            if (LoginRealmModels != null && LoginRealmModels.size() > 0) {
                LocationManager.Builder builder = new LocationManager.Builder(this);
                builder.setLocationListner(this);
                builder.build();
                userId = LoginRealmModels.get(0).getUserID();
                SharedPreferencesUtility.savePrefString(HomeActivity.this, SharedPreferencesUtility.PREF_USERID, userId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        locationManager =
                (android.location.LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                && locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
            getServiceCalled();
            addFragment(HomeFragment.newInstance(), "HomeActivity - HomeFragment");
        } else {
            AppUtils.statusCheck(HomeActivity.this);
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (AppUtils.checkConnection(HomeActivity.this)) {
            getVersionFromApi();
        } else {
            AppUtils.showOkActionAlertBox(HomeActivity.this, "Something went wrong! please check your internet connection.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }

        AppUtils.statusCheck(HomeActivity.this);

    }

    private void getVersionFromApi() {
        NetworkCallController controller = new NetworkCallController();
        controller.setListner(new NetworkResponseListner() {
            @Override
            public void onResponse(int requestCode, Object response) {
                UpdateData data = (UpdateData) response;
                checkCurrentVersion(data.getApkurl(), data.getVersion());
            }

            @Override
            public void onFailure(int requestCode) {

            }
        });
        controller.getUpdateApp(UPDATE_REQ);
    }

    private void checkCurrentVersion(final String apkurl, String version) {
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String mobileVersion = pInfo.versionName;

        if (Float.parseFloat(mobileVersion) < Float.parseFloat(version)) {
            String title = "New update available";
            String messageAlert = "<html><body><p>Please update your app to new version.<br><br>Current app version: " + mobileVersion + "<br><br>New version: " + version + "</p></body></html>";
            AppUtils.showDownloadActionAlertBox(HomeActivity.this, title, String.valueOf(Html.fromHtml(messageAlert)), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (AppUtils.checkConnection(HomeActivity.this)) {
                        ProgressDialog progress = new ProgressDialog(HomeActivity.this);
                        DownloadApk downloadAndInstall = new DownloadApk();
                        progress.setCancelable(false);
                        progress.setMessage("Downloading...");
                        downloadAndInstall.setContext(HomeActivity.this, progress);
                        downloadAndInstall.execute(apkurl);
                    } else {
                        AppUtils.showOkActionAlertBox(HomeActivity.this, "No Internet Found.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                    }
                }
            });

        }
    }

    private void getServiceCalled() {
        try {
            if (isClicked) {
                userName = getIntent().getStringExtra(ARG_USER);
                SharedPreferencesUtility.savePrefString(HomeActivity.this, SharedPreferencesUtility.PREF_USERNAME, userName);
                items = (List<HandShake>) getIntent().getSerializableExtra(ARG_HANDSHAKE);
                long REPEATED_TIME = 1000 * 60 * Integer.parseInt(items.get(1).getValue());
//                long REPEATED_TIME = 60000;
                SharedPreferencesUtility.savePrefString(HomeActivity.this, SharedPreferencesUtility.PREF_INTERVAL, String.valueOf(REPEATED_TIME));
                Log.i("callHandshake", String.valueOf(REPEATED_TIME));
                SharedPreferencesUtility.savePrefString(HomeActivity.this, SharedPreferencesUtility.PREF_TIME, items.get(1).getValue());
                if (items.get(0).getText().equals("EnableTrace")) {
                    if (items.get(0).getValue().equals("true")) {
//                        Context ctx = getApplicationContext();
///** this gives us the time for the first trigger.  */
//                        Calendar cal = Calendar.getInstance();
//                        mAlarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
//                        long interval = 1000 * 60 * 5; // 5 minutes in milliseconds
//                        Intent serviceIntent = new Intent(ctx, ServiceLocationSend.class);
//// make sure you **don't** use *PendingIntent.getBroadcast*, it wouldn't work
//                        pendingUpdateIntent =
//                                PendingIntent.getService(ctx,
//                                        0, // integer constant used to identify the service
//                                        serviceIntent,
//                                        PendingIntent.FLAG_CANCEL_CURRENT);  // FLAG to avoid creating a second service if there's already one running
//// there are other options like setInexactRepeating, check the docs
//
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                            mAlarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, interval, pendingUpdateIntent);
//                        } else {
//                            mAlarmManager.setRepeating(
//                                    AlarmManager.RTC_WAKEUP,//type of alarm. This one will wake up the device when it goes off, but there are others, check the docs
//                                    cal.getTimeInMillis(),
//                                    interval,
//                                    pendingUpdateIntent
//                            );
//                        }


//                        Intent itAlarm = new Intent(this, ServiceLocationSend.class);
//                        PendingIntent pendingIntent = PendingIntent.getService(this, 0, itAlarm, 0);
//                        Calendar calendar = Calendar.getInstance();
//                        calendar.setTimeInMillis(System.currentTimeMillis());
//                        calendar.add(Calendar.SECOND, 0);
//                        AlarmManager alarme = (AlarmManager) getSystemService(ALARM_SERVICE);
//                        alarme.setRepeating(AlarmManager.RTC_WAKEUP,
//                                calendar.getTimeInMillis(),
//                                1000 * 60 * Integer.parseInt(items.get(1).getValue()),
//                                pendingIntent);
//                        alarme.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, 1000 * 60 * Integer.parseInt(items.get(1).getValue()), pendingIntent);


//                        Intent alaramIntent = new Intent(HomeActivity.this, HandShakeReceiver.class);
//                        alaramIntent.setAction("HandshakeAction");
//                        alaramIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//                        PendingIntent pendingIntent = PendingIntent.getBroadcast(HomeActivity.this, 0, alaramIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                        Calendar calendar = Calendar.getInstance();
//                        calendar.setTimeInMillis(System.currentTimeMillis());
//                        calendar.add(Calendar.SECOND, 0);
//                        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
//                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
//                                calendar.getTimeInMillis(),
//                                1000 * 60 * Integer.parseInt(items.get(1).getValue()),
//                                pendingInten
                        Intent intent = new Intent(getApplicationContext(), HandShakeReceiver.class);
                        intent.setAction("HandshakeAction");
                        pendingUpdateIntent = PendingIntent.getBroadcast(getApplicationContext(),
                                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        Calendar futureDate = Calendar.getInstance();
                        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                        if (android.os.Build.VERSION.SDK_INT >= 19) {
                            mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, futureDate.getTime().getTime(), REPEATED_TIME, pendingUpdateIntent);
                        } else {
                            mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, futureDate.getTime().getTime(), REPEATED_TIME, pendingUpdateIntent);
                        }
                    }
                }
            } else {
                if (pendingUpdateIntent != null) {
                    mAlarmManager.cancel(pendingUpdateIntent);
                    getApplicationContext().stopService(new Intent(getApplicationContext(), ServiceLocationSend.class));
                }
                String time = SharedPreferencesUtility.getPrefString(HomeActivity.this, SharedPreferencesUtility.PREF_TIME);
                long REPEATED_TIME = 1000 * 60 * Integer.parseInt(time);
//                long REPEATED_TIME = 60000;
                Intent intent = new Intent(getApplicationContext(), HandShakeReceiver.class);
                intent.setAction("HandshakeAction");
                pendingUpdateIntent = PendingIntent.getBroadcast(getApplicationContext(),
                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                Calendar futureDate = Calendar.getInstance();
                mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                if (android.os.Build.VERSION.SDK_INT >= 19) {
                    mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, futureDate.getTime().getTime(), REPEATED_TIME, pendingUpdateIntent);
                } else {
                    mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, futureDate.getTime().getTime(), REPEATED_TIME, pendingUpdateIntent);
                }
            }
//            else {
//                items = (List<HandShake>) getIntent().getSerializableExtra(ARG_HANDSHAKE);
//                String time = SharedPreferencesUtility.getPrefString(HomeActivity.this, SharedPreferencesUtility.PREF_TIME);
//                Intent itAlarm = new Intent(this, ServiceLocationSend.class);
//                PendingIntent pendingIntent = PendingIntent.getService(this, 0, itAlarm, 0);
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTimeInMillis(System.currentTimeMillis());
//                calendar.add(Calendar.SECOND, 0);
//                AlarmManager alarme = (AlarmManager) getSystemService(ALARM_SERVICE);
//                alarme.setRepeating(AlarmManager.RTC_WAKEUP,
//                        calendar.getTimeInMillis(),
//                        1000 * 60 * Integer.parseInt(time),
//                        pendingIntent);

//                alarme.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, 1000 * 60 * Integer.parseInt(time), pendingIntent);


//                Intent alaramIntent = new Intent(HomeActivity.this, HandShakeReceiver.class);
//                alaramIntent.setAction("HandshakeAction");
//                alaramIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(HomeActivity.this, 0, alaramIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTimeInMillis(System.currentTimeMillis());
//                calendar.add(Calendar.SECOND, 3);
//                AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
//                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
//                        calendar.getTimeInMillis(),
//                        1000 * 60 * Integer.parseInt(time),
//                        pendingIntent);
//            alarme.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, 1000 * 60 * Integer.parseInt(items.get(1).getValue()), pendingIntent);
//            }
        } catch (Exception e) {
            RealmResults<LoginResponse> mLoginRealmModels = BaseApplication.getRealm().where(LoginResponse.class).findAll();
            if (mLoginRealmModels != null && mLoginRealmModels.size() > 0) {
                String userName = "TECHNICIAN NAME : " + mLoginRealmModels.get(0).getUserName();
                String lineNo = String.valueOf(new Exception().getStackTrace()[0].getLineNumber());
                String DeviceName = "DEVICE_NAME : " + Build.DEVICE + ", DEVICE_VERSION : " + Build.VERSION.SDK_INT;
                AppUtils.sendErrorLogs(e.getMessage(), getClass().getSimpleName(), "getServiceCalled", lineNo, userName, DeviceName);
            }
        }

    }


    public void initNavigationDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        LoginRealmModels =
                BaseApplication.getRealm().where(LoginResponse.class).findAll();
        if (LoginRealmModels != null && LoginRealmModels.size() > 0) {
            String isTsEnable = LoginRealmModels.get(0).getIsTechnician();
            Menu menu = navigationView.getMenu();
            MenuItem groom = menu.findItem(R.id.nav_groom);
            if (isTsEnable.equals("0")) {
                groom.setVisible(true);
            } else {
                groom.setVisible(false);
            }
            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String mobileVersion = pInfo.versionName;
            String Uname = LoginRealmModels.get(0).getUserName();

            View header = navigationView.getHeaderView(0);
            TextView name = (TextView) header.findViewById(R.id.drawer_name);
            TextView version = (TextView) header.findViewById(R.id.txtVersion);
            name.setText("Hi, " + Uname);
            version.setText("V " + mobileVersion);


        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();

                switch (id) {

                    case R.id.nav_home:
                        getSupportFragmentManager().beginTransaction().replace(mActivityHomeBinding.container.getId(), HomeFragment.newInstance()).addToBackStack(null).commit();
                        mActivityHomeBinding.drawer.closeDrawers();
                        break;


                    case R.id.nav_groom:
                        mActivityHomeBinding.drawer.closeDrawers();
                        startActivity(new Intent(HomeActivity.this, TechnicianSeniorActivity.class).putExtra(HomeActivity.ARG_EVENT, false));
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;

                    case R.id.nav_notifications:
                        getSupportFragmentManager().beginTransaction().replace(mActivityHomeBinding.container.getId(), NotificationFragment.newInstance()).addToBackStack(null).commit();

                        mActivityHomeBinding.drawer.closeDrawers();
                        break;

                    case R.id.nav_training:
                        mActivityHomeBinding.drawer.closeDrawers();
                        startActivity(new Intent(HomeActivity.this, TrainingActivity.class).putExtra(HomeActivity.ARG_EVENT, false));
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;

                    case R.id.nav_help:
                        mActivityHomeBinding.drawer.closeDrawers();
                        startActivity(new Intent(HomeActivity.this, HelpActivity.class).putExtra(HomeActivity.ARG_EVENT, false));
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;

                    case R.id.nav_myid:
                        mActivityHomeBinding.drawer.closeDrawers();
                        startActivity(new Intent(HomeActivity.this, TechIdActivity.class).putExtra(HomeActivity.ARG_EVENT, false));
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;

                    case R.id.nav_logout:
                        mActivityHomeBinding.drawer.closeDrawers();

                        final AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
                        dialog.setTitle("Logout");
                        dialog.setMessage("Do you want to logout?");
                        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                NetworkCallController controller = new NetworkCallController();
                                String UserId = SharedPreferencesUtility.getPrefString(HomeActivity.this, SharedPreferencesUtility.PREF_USERID);

                                controller.setListner(new NetworkResponseListner() {
                                    @Override
                                    public void onResponse(int requestCode, Object response) {
                                        LogoutResponse logres = (LogoutResponse) response;

                                        if (logres.getSuccess()) {
                                            if (pendingUpdateIntent != null) {
                                                mAlarmManager.cancel(pendingUpdateIntent);
                                                getApplicationContext().stopService(new Intent(getApplicationContext(), ServiceLocationSend.class));
                                            }
                                            SharedPreferencesUtility.savePrefBoolean(getApplicationContext(), SharedPreferencesUtility.IS_USER_LOGIN,
                                                    false);
                                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(HomeActivity.this, "Logout failed! try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(int requestCode) {

                                    }
                                });

                                controller.getLogout(LOGOUT_REQ, UserId);

                            }
                        });
                        dialog.setNegativeButton("No", null);
                        dialog.show();

                        break;

                }
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mActivityHomeBinding.drawer, mActivityHomeBinding.toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        mActivityHomeBinding.drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    @Override
    public void onBackPressed() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        getSupportFragmentManager().beginTransaction().remove(FaceRecognizationFragment.newInstance(false, "avatar")).commit();
        getSupportFragmentManager().popBackStack();
        if (backStackEntryCount == 0) {
            showExitAlert();
        } else {
            super.onBackPressed();
        }
    }

    private void showExitAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Exit");
        dialog.setMessage("Do you want to exit?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishAffinity();
            }
        });
        dialog.setNegativeButton("No", null);
        dialog.show();
    }

    @Override
    public void onBackStackChanged() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void locationFetched(Location mLocation, Location oldLocation, String time, String locationProvider) {
        this.mLocation = mLocation;
        if (mListner != null) {
            mListner.locationFetched(mLocation, oldLocation, time, locationProvider);
        }
    }

    public Location getmLocation() {
        return mLocation;
    }

}
