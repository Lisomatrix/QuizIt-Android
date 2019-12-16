package pt.lisomatrix.ptproject.UI.main;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private boolean isSentToBackground = false;
    private MainActivity.ApplicationFragment currentApplicationFragment = MainActivity.ApplicationFragment.EMPTY;
    private boolean isApplicationLoaded = false;

    public boolean isApplicationLoaded() {
        return isApplicationLoaded;
    }

    public void setApplicationLoaded(boolean applicationLoaded) {
        isApplicationLoaded = applicationLoaded;
    }

    public MainActivity.ApplicationFragment getCurrentApplicationFragment() {
        return currentApplicationFragment;
    }

    public void setCurrentFragment(MainActivity.ApplicationFragment applicationFragment) {
        currentApplicationFragment = applicationFragment;
    }

    public boolean isSentToBackground() {
        return isSentToBackground;
    }

    public void setSentToBackground(boolean sentToBackground) {
        isSentToBackground = sentToBackground;
    }
}
