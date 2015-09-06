package com.idragonit.talkpad;

import java.util.List;

import com.idragonit.talkpad.adapter.MenuAdapter;
import com.idragonit.talkpad.editor.Settings;
import com.idragonit.talkpad.fragment.AboutFragment;
import com.idragonit.talkpad.fragment.BlankFragment;
import com.idragonit.talkpad.fragment.MainFragment;
import com.idragonit.talkpad.fragment.OpenFragment;
import com.idragonit.talkpad.fragment.RecentFragment;
import com.idragonit.talkpad.fragment.SaveFragment;
import com.idragonit.talkpad.utils.CommonMethods;
import com.idragonit.talkpad.utils.Constants;
import com.idragonit.talkpad.utils.Stack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
        DialogInterface.OnClickListener, Constants {

    private boolean FROM_MENU = false;

    private String TAG = "MainActivity";
    private MainActivity instance;

    DrawerLayout slideLayout;
    ListView slideList;
    MenuAdapter slideAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        AppConstants.ASSET_MANAGER = getAssets();

        setContentView(R.layout.activity_main);

        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(AppConstants.WINDOW);

        AppConstants.IS_CREATED = false;
        instance = this;

        checkVoiceRecognition();

        AppConstants.APP_HANDLER = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case ACTION_MENU:
                        instance.openMenu();
                        break;

                    case ACTION_NEW_PAD:
                        instance.changeFragment(NEW_FRAGMENT);
                        break;

                    case ACTION_OPEN_PAD:
                        if (msg.arg2 == 1)
                            FROM_MENU = true;

                        instance.changeFragment(msg.arg1);
                        break;

                    case ACTION_GALLERY:
                    case ACTION_CAMERA:
                        Message new_msg = new Message();
                        new_msg.obj = msg.obj;
                        new_msg.what = msg.what;

                        if (!AppConstants.IS_CREATED)
                            AppConstants.APP_HANDLER.sendMessageDelayed(new_msg,
                                    ACTION_DELAY_TIME);
                        else
                            AppConstants.COMMAND_HANDLER.sendMessageDelayed(
                                    new_msg, ACTION_DELAY_TIME);
                        break;

                    case ACTION_SPEECH:
                        Log.i(TAG, "Voice button clicked");
                    case ACTION_COMMAND:
                        Message n_msg = new Message();
                        n_msg.what = msg.what;
                        n_msg.setData(msg.getData());

                        if (!AppConstants.IS_CREATED)
                            AppConstants.APP_HANDLER.sendMessageDelayed(n_msg,
                                    ACTION_DELAY_TIME);
                        else
                            AppConstants.COMMAND_HANDLER.sendMessageDelayed(n_msg,
                                    ACTION_DELAY_TIME);
                        break;

                }
            }
        };

        init(savedInstanceState);
    }

    public void checkVoiceRecognition() {
        // Check if voice recognition is present
//		PackageManager pm = getPackageManager();
//		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
//		if (activities.size() == 0) {
//			Toast.makeText(this, "Voice recognizer not present", Toast.LENGTH_SHORT).show();
//		}
    }

    private void init(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        slideLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        slideLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        slideList = (ListView) findViewById(R.id.list_slide);
        slideAdapter = new MenuAdapter(this);

        String[] menu = getResources().getStringArray(R.array.sidebar_menu);
        for (int i = 0; i < menu.length; i++)
            slideAdapter.add(menu[i]);

        slideList.setAdapter(slideAdapter);
        slideList.setSelector(R.drawable.list_selector);

        changeFragment(1);
        slideList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                                    long id) {
                slideLayout.closeDrawers();
                Message msg = new Message();
                msg.what = ACTION_OPEN_PAD;
                msg.arg1 = position + 1;
                msg.arg2 = 0;
                FROM_MENU = true;
                AppConstants.APP_HANDLER.sendMessageDelayed(msg,
                        ACTION_DELAY_TIME * 3);
            }
        });

        try {
            SharedPreferences pref = getApplicationContext()
                    .getSharedPreferences(APP_NAME, MODE_PRIVATE);
            AppConstants.LINE_NUMBER = pref.getBoolean(Constants.PREFERENCE_LINENUMBER, DEFAULT_LINENUMBER);
            AppConstants.SPEECH_LANGUAGE = pref.getString(Constants.PREFERENCE_SPEECHLANGUAGE, DEFAULT_SPEECHLANGUAGE);
        } catch (Exception f) {
        }
    }

    private void changeFragment(int window) {
        Fragment fragment = null;

        AppConstants.APP_STATUS = window;
        AppConstants.IS_CREATED = false;

        switch (window) {
            case NEW_FRAGMENT:
                slideLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                if (FROM_MENU) {
                    if (Settings.TEMP_NOTES.equals(Settings.BEFORE_NOTES)) {
                        instance.init();
                        fragment = new MainFragment();
                    } else {
                        showConfirmDialog(true);
                    }

                } else {
                    fragment = new MainFragment();
                }

                break;

            case OPEN_FRAGMENT:
                slideLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                fragment = new OpenFragment();
                break;

            case SAVE_FRAGMENT:
                if (AppConstants.SAVE_MODE == NOT_SAVE_MODE) {
                    slideLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    fragment = new SaveFragment();
                } else {

                    if (CommonMethods.writeFile(AppConstants.FILE_PATH,
                            Settings.TEMP_NOTES)) {
                        try {
                            SharedPreferences pref = getApplicationContext()
                                    .getSharedPreferences(APP_NAME, MODE_PRIVATE);
                            Editor editor = pref.edit();

                            int recent = pref.getInt(PREFERENCE_RECENT_COUNT, 0);
                            int k = 0;

                            do {
                                k = 0;
                                for (int i = 1; i <= recent; i++) {
                                    String path = pref.getString(
                                            PREFERENCE_RECENT_FILE + i, "");
                                    if (path.length() == 0
                                            || path.equals(AppConstants.FILE_PATH)) {
                                        k = i;
                                        break;
                                    }
                                }

                                if (k != 0) {
                                    for (int i = k; i < recent; i++) {
                                        int v = i + 1;
                                        String path = pref.getString(
                                                PREFERENCE_RECENT_FILE + v, "");
                                        editor.putString(
                                                PREFERENCE_RECENT_FILE + i, path);
                                    }

                                    editor.commit();
                                    recent--;
                                }
                            } while (k != 0);

                            if (recent == RECENT_FILE_COUNT) {
                                for (int i = 1; i < recent; i++) {
                                    int v = i + 1;
                                    String path = pref.getString(
                                            PREFERENCE_RECENT_FILE + v, "");
                                    editor.putString(PREFERENCE_RECENT_FILE + i,
                                            path);
                                }
                            } else {
                                recent++;
                            }

                            editor.putInt(PREFERENCE_RECENT_COUNT, recent);
                            editor.putString(PREFERENCE_RECENT_FILE + recent,
                                    AppConstants.FILE_PATH);
                            editor.commit();
                        } catch (Exception e) {
                        }

                        Settings.BEFORE_NOTES = Settings.TEMP_NOTES;
                    }

                    if (AppConstants.APP_EXIT) {
                        instance.init();
                        AppConstants.FILE_MANAGER__LOCATION = "";
                        finish();
                    } else if (AppConstants.IS_SAVED) {
                        instance.init();
                        slideLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        fragment = new MainFragment();
                    } else {
                        slideLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        fragment = new MainFragment();
                    }
                }
                break;

            case SAVE_AS_FRAGMENT:
                slideLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                fragment = new SaveFragment();
                break;

            case RECENT_FRAGMENT:
                slideLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                fragment = new RecentFragment();
                break;


            case ABOUT_FRAGMENT:
                slideLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                fragment = new AboutFragment();
                break;

            case EXIT_FRAGMENT:
                if (Settings.TEMP_NOTES.equals(Settings.BEFORE_NOTES)) {
                    instance.init();
                    AppConstants.FILE_MANAGER__LOCATION = "";
                    finish();
                } else {
                    showConfirmDialog();
                }
                break;

            case BLANK_FRAGMENT:
            default:
                slideLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                fragment = new BlankFragment();
                break;
        }

        FROM_MENU = false;

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragTrs = fragmentManager.beginTransaction();
            fragTrs.setCustomAnimations(R.anim.slide1, R.anim.slide2);
            fragTrs.addToBackStack(null);
            fragTrs.replace(R.id.content_frame, fragment).commitAllowingStateLoss();
            // fragTrs.commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        // Log.e("screen changed", ""+AppConstants.APP_STATUS);
        changeFragment(AppConstants.APP_STATUS);
    }

    public void openMenu() {
        // TODO Auto-generated method stub
        slideLayout.openDrawer(Gravity.START);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.e("back", "");
            slideLayout.closeDrawers();

            if (AppConstants.APP_STATUS == NEW_FRAGMENT) {
                showConfirmDialog();
            } else {
                AppConstants.APP_HANDLER.sendEmptyMessageDelayed(ACTION_NEW_PAD, ACTION_DELAY_TIME * 3);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void aboutFragmentShow(Fragment f) {
        f = new AboutFragment();
//		f.show(getActivity().getSupportFragmentManager(), "CommandsDialogFragment");
    }

    public void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to save notes?")
                .setNegativeButton("No", this).setPositiveButton("Yes", this)
                .show();
    }

    public void showConfirmDialog(boolean t) {
        AppConstants.IS_SAVED = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to save changes?")
                .setNegativeButton("No", this).setPositiveButton("Yes", this)
                .show();
    }

    public static void init() {
        AppConstants.init();
        Settings.init();
        Stack.clear();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                // Yes button clicked
                if (AppConstants.IS_SAVED) {
                    changeFragment(SAVE_FRAGMENT);
                } else {
                    AppConstants.APP_EXIT = true;
                    changeFragment(SAVE_FRAGMENT);
                }
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                // No button clicked
                if (AppConstants.IS_SAVED) {
                    instance.init();
                    changeFragment(NEW_FRAGMENT);
                } else {
                    instance.init();
                    AppConstants.FILE_MANAGER__LOCATION = "";
                    finish();
                }
                break;
        }
    }

}
