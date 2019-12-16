package pt.lisomatrix.ptproject.singleton;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

import static pt.lisomatrix.ptproject.service.NetworkService.START_ID;

public class TimerSingleton {

    private static TimerSingleton instance;

    public static void init(Context context) {
        if (instance == null) {
            instance = new TimerSingleton(context);
        }
    }

    public static TimerSingleton getInstance() {
        return instance;
    }

    private StateSingleton stateSingleton;

    private Context context;

    private int passedTime = 0;

    private BehaviorSubject<Integer> passedTimeSubject = BehaviorSubject.createDefault(passedTime);

    private Disposable timerDisposable;

    private boolean roomChanged = true;

    private Vibrator vibrator;

    private TimerSingleton(Context context) {
        this.context = context;

        StateSingleton.init(context);
        stateSingleton = StateSingleton.getInstance();
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        stateSingleton.getQuestions().subscribe(question -> {
            startTimer();
            vibrate();
        });
        stateSingleton.getStart().subscribe(x -> setRoomChanged(true));
        stateSingleton.getScore().subscribe(x -> stopTimer());
    }


    private void vibrate() {
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibrator.vibrate(500);
        }
    }

    public boolean isRoomChanged() {
        return roomChanged;
    }

    public void setRoomChanged(boolean roomChanged) {
        this.roomChanged = roomChanged;

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(START_ID);
    }

    public Observable<Integer> getPassedTime() {
        return passedTimeSubject.serialize();
    }

    public void startTimer() {
        stopTimer();

        passedTime = 0;
        timerDisposable = Observable.interval(25, TimeUnit.MILLISECONDS).subscribe(timer -> {
            passedTime += 25;
            passedTimeSubject.onNext(passedTime);
        });
    }

    public void stopTimer() {
        if (timerDisposable != null && !timerDisposable.isDisposed()) {
            timerDisposable.dispose();
        }
    }
}
