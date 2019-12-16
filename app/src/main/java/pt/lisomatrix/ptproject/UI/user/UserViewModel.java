package pt.lisomatrix.ptproject.UI.user;

import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {

    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
