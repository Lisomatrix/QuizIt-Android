package pt.lisomatrix.ptproject.UI.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.SavedStateVMFactory;
import androidx.lifecycle.ViewModelProviders;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import androidx.transition.Fade;
import androidx.transition.Slide;

import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

import java.util.AbstractMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.UI.disconnected.NoInternetFragment;
import pt.lisomatrix.ptproject.UI.options.OptionsFragment;
import pt.lisomatrix.ptproject.UI.room.RoomFragment;
import pt.lisomatrix.ptproject.UI.rooms.RoomsFragment;
import pt.lisomatrix.ptproject.UI.score.ScoreFragment;
import pt.lisomatrix.ptproject.UI.topscore.TopScoreFragment;
import pt.lisomatrix.ptproject.UI.user.UserFragment;
import pt.lisomatrix.ptproject.UI.wrong.chapter.WrongChaptersFragment;
import pt.lisomatrix.ptproject.UI.wrong.question.WrongQuestionsFragment;
import pt.lisomatrix.ptproject.model.Question;
import pt.lisomatrix.ptproject.model.Room;
import pt.lisomatrix.ptproject.receiver.ConnectivityReceiver;
import pt.lisomatrix.ptproject.service.NetworkService;
import pt.lisomatrix.ptproject.singleton.StateSingleton;
import pt.lisomatrix.ptproject.singleton.TimerSingleton;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MainActivity extends AppCompatActivity {

    private PublishSubject<Boolean> isBackPressed = PublishSubject.create();

    private StateSingleton mStateSingleton;

    // View Model
    private MainViewModel mMainViewModel;

    private Disposable getStartDisposable;
    private Disposable getScoreDisposable;
    private Disposable hasNetworkDisposable;
    private Disposable joinedRoomDisposable;

    String notification = "";

    private ConnectivityReceiver mConnectivityReceiver = new ConnectivityReceiver();


    public void changeFragmentWithTransition(Fragment fragment, View view, ApplicationFragment applicationFragment) {
        mMainViewModel.setCurrentFragment(applicationFragment);

        getSupportFragmentManager()
                .beginTransaction()
                .addSharedElement(view, ViewCompat.getTransitionName(view))
                .addToBackStack(TAG)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        isBackPressed.onNext(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey("NOTIFICATION")) {
            notification = intent.getExtras().getString("NOTIFICATION");
        }

        mMainViewModel = ViewModelProviders.of(this, new SavedStateVMFactory(this)).get(MainViewModel.class);
        StateSingleton.init(getApplicationContext());
        TimerSingleton.init(getApplicationContext());
        mStateSingleton = StateSingleton.getInstance();
        setContentView(R.layout.splash_screen);

        try {
            mMainViewModel.setCurrentFragment(ApplicationFragment.valueOf(savedInstanceState.getString("FRAGMENT")));
        } catch (Exception ex) {
        }

        this.getSupportActionBar().hide();
    }

    private void loadCurrentFragment() {
        init();

        if (mMainViewModel.isSentToBackground()) {
            return;
        }

        if (notification.equals("START")) {
            mMainViewModel.setCurrentFragment(ApplicationFragment.OPTIONS);
        }

        Log.d("DEBUG", "CURRENT FRAGMENT: " + mMainViewModel.getCurrentApplicationFragment().name());

        switch (mMainViewModel.getCurrentApplicationFragment()) {
            case WRONG_CHAPTER:
            case WRONG_QUESTION:
                showWrongChapters();
                break;
            case TOP_SCORE:
                showTopScoreFragment();
                break;
            case OPTIONS:
                showOptionsFragment();
                break;
            case ROOM:
                showRoomFragment("");
                break;
            case USER:
                showUserFragment();
                break;
            case ROOMS:
                showRoomsFragment();
                break;
            case SCORE:
                showScoreFragment();
                break;
            case EMPTY: {
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

                if (sharedPref.contains(getString(R.string.USERNAME_KEY))) {
                    mStateSingleton.getNetworkService()
                            .sendCreateUserRequest(sharedPref.getString(getString(R.string.USERNAME_KEY), ""));
                    showRoomsFragment();
                } else {
                    showUserFragment();
                }
            }

        }

        mMainViewModel.setSentToBackground(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(mConnectivityReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isConnected = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT < 23) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                if (networkInfo != null) {
                    isConnected = (networkInfo.isConnected() && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE));
                }
            } else {
                Network network = connectivityManager.getActiveNetwork();

                if (network != null) {
                    isConnected = true;
                }
            }
        }

        if (!isConnected) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Sem conexão");
            alertDialog.setMessage("Internet não disponível!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
        }

        mStateSingleton.getIsConnected()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connected -> {
                    if (!mMainViewModel.isApplicationLoaded()) {
                        setContentView(R.layout.activity_main);
                        mMainViewModel.setApplicationLoaded(true);
                        mMainViewModel.setCurrentFragment(ApplicationFragment.EMPTY);
                    }

                    if (connected) {
                        if (mMainViewModel.getCurrentApplicationFragment().equals(ApplicationFragment.NO_INTERNET)) {
                            mMainViewModel.setCurrentFragment(ApplicationFragment.EMPTY);
                        }

                        loadCurrentFragment();
                    } else {
                        showDisconnectedFragment();
                    }
                });

        mStateSingleton.setIsInBackground(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mConnectivityReceiver);
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (getStartDisposable != null && !getStartDisposable.isDisposed()) {
            getStartDisposable.dispose();
        }

        if (getScoreDisposable != null && !getScoreDisposable.isDisposed()) {
            getScoreDisposable.dispose();
        }

        if (hasNetworkDisposable != null && !hasNetworkDisposable.isDisposed()) {
            hasNetworkDisposable.dispose();
        }

        if (getStartDisposable != null && !getStartDisposable.isDisposed()) {
            getStartDisposable.dispose();
        }

        if (joinedRoomDisposable != null && !joinedRoomDisposable.isDisposed()) {
            joinedRoomDisposable.dispose();
        }

        mStateSingleton.setIsInBackground(true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("FRAGMENT", mMainViewModel.getCurrentApplicationFragment().name());
        mMainViewModel.setSentToBackground(true);
    }


    public Observable<Boolean> getBackButtonPressed() {
        return isBackPressed.serialize();
    }

    public void showWrongChapters() {
        Fragment wrongChaptersFragment = WrongChaptersFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, 0, 0);
        transaction.replace(R.id.fragment_container, wrongChaptersFragment);

        transaction.commit();
        mMainViewModel.setCurrentFragment(ApplicationFragment.WRONG_CHAPTER);
    }

    public void showWrongQuestions(AbstractMap.SimpleEntry<String, List<Question>> entry) {
        Fragment wrongChaptersFragment = WrongQuestionsFragment.newInstance(entry);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, 0, 0);
        transaction.replace(R.id.fragment_container, wrongChaptersFragment);

        transaction.commit();
        mMainViewModel.setCurrentFragment(ApplicationFragment.WRONG_QUESTION);
    }

    public void showTopScoreFragment() {
        Fragment topScoreFragment = TopScoreFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, 0, 0);
        transaction.replace(R.id.fragment_container, topScoreFragment);

        transaction.commit();
        mMainViewModel.setCurrentFragment(ApplicationFragment.TOP_SCORE);
    }

    private void showDisconnectedFragment() {
        Fragment disconnectedFragment = NoInternetFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, 0, 0);
        transaction.replace(R.id.fragment_container, disconnectedFragment);

        transaction.commit();
        mMainViewModel.setCurrentFragment(ApplicationFragment.NO_INTERNET);
    }

    private void showUserFragment() {
        if (mMainViewModel.getCurrentApplicationFragment().equals(ApplicationFragment.USER)) {
            return;
        }

        Fragment userFragment = UserFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, 0, 0);
        transaction.replace(R.id.fragment_container, userFragment);

        transaction.commit();
        mMainViewModel.setCurrentFragment(ApplicationFragment.USER);
    }

    public void showOptionsFragment() {

        Fragment optionsFragment = OptionsFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, 0, 0);
        transaction.replace(R.id.fragment_container, optionsFragment, ApplicationFragment.OPTIONS.name());
        //transaction.setMaxLifecycle(optionsFragment, Lifecycle.State.RESUMED);

        transaction.commit();
        mMainViewModel.setCurrentFragment(ApplicationFragment.OPTIONS);
        mMainViewModel.getCurrentApplicationFragment();
    }

    private void showRoomFragment(String id) {
        Fragment roomFragment = RoomFragment.newInstance(id);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        roomFragment.setEnterTransition(new Slide());
        transaction.replace(R.id.fragment_container, roomFragment);

        transaction.commit();
        mMainViewModel.setCurrentFragment(ApplicationFragment.ROOM);
    }

    public void showRoomsFragment() {

        if (mMainViewModel.getCurrentApplicationFragment().equals(ApplicationFragment.ROOMS) && mMainViewModel.isSentToBackground()) {
            return;
        }

        Fragment roomsFragment = RoomsFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        roomsFragment.setEnterTransition(new Fade());
        roomsFragment.setEnterTransition(new Fade());

        transaction.replace(R.id.fragment_container, roomsFragment);

        transaction.commit();
        mMainViewModel.setCurrentFragment(ApplicationFragment.ROOMS);
    }

    public void showScoreFragment() {
        Fragment scoreFragment = ScoreFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, 0, 0);
        transaction.replace(R.id.fragment_container, scoreFragment);

        transaction.commit();
        mMainViewModel.setCurrentFragment(ApplicationFragment.SCORE);
    }

    /***
     * Initialize view and listeners
     */
    private void init() {

        getStartDisposable = mStateSingleton.getStart().subscribe(x -> {
            if (mMainViewModel.getCurrentApplicationFragment()!= ApplicationFragment.OPTIONS) {
                showOptionsFragment();
            }
        });

        observeJoinedRoom();

        getScoreDisposable = mStateSingleton.getScore().subscribe(scoreMessage -> showScoreFragment());

        hasNetworkDisposable = mConnectivityReceiver.getHasNetwork().observeOn(AndroidSchedulers.mainThread()).subscribe(hasNetwork -> {
            if (!hasNetwork) {
                if (mMainViewModel.getCurrentApplicationFragment() != ApplicationFragment.NO_INTERNET) {
                    showDisconnectedFragment();
                }
            } else {
                showRoomsFragment();
            }
        });
    }

    private void observeJoinedRoom() {
        joinedRoomDisposable = mStateSingleton.getJoinedRoom().subscribe(this::handleJoinedRoom);
    }

    private void handleJoinedRoom(Room room) {
        ApplicationFragment currentFragment = mMainViewModel.getCurrentApplicationFragment();

        if (room != null && currentFragment != ApplicationFragment.ROOM && currentFragment.equals(ApplicationFragment.ROOMS)) {
            showRoomFragment(room.getId() + "");
            joinedRoomDisposable.dispose();
            observeJoinedRoom();
        }
    }

    public enum ApplicationFragment {
        ROOMS,
        ROOM,
        USER,
        OPTIONS,
        SCORE,
        EMPTY,
        NO_INTERNET,
        TOP_SCORE,
        WRONG_CHAPTER,
        WRONG_QUESTION
    }
}
