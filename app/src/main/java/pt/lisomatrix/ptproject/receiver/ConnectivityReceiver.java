package pt.lisomatrix.ptproject.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class ConnectivityReceiver extends BroadcastReceiver {

    private BehaviorSubject isConnectedToNetworkSubject = BehaviorSubject.create();

    public Observable<Boolean> getHasNetwork() {
        return isConnectedToNetworkSubject.serialize();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)){
                // Has Connection
                isConnectedToNetworkSubject.onNext(true);
            } else {
                // Connection Lost
                isConnectedToNetworkSubject.onNext(false);
            }
        }
    }
}
