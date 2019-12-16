package pt.lisomatrix.ptproject.UI.user;

import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import pt.lisomatrix.ptproject.UI.main.MainActivity;
import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.singleton.StateSingleton;

public class UserFragment extends Fragment {

    private EditText usernameEditText;
    private Button startButton;

    private UserViewModel mViewModel;
    private StateSingleton mStateSingleton;

    private Disposable getMeDisposable;

    public static UserFragment newInstance() {
        return new UserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.user_fragment, container, false);

        init(view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mStateSingleton = StateSingleton.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

        getMeDisposable = mStateSingleton.getMe().observeOn(AndroidSchedulers.mainThread())
                .subscribe(me -> {
                    if (me != null) {
                        ((MainActivity) getActivity()).showRoomsFragment();
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();

        if (getMeDisposable != null && !getMeDisposable.isDisposed()) {
            getMeDisposable.dispose();
        }
    }

    private void init(View view) {
        usernameEditText = view.findViewById(R.id.username_input);
        startButton = view.findViewById(R.id.username_enter_button);

        startButton.setOnClickListener(this::handleStartClick);
    }

    private void handleStartClick(View view) {
        usernameEditText.setError(null);

        String username = usernameEditText.getText().toString();

        if (validate(username)) {
            mViewModel.setUsername(username);

            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.USERNAME_KEY), username);
            editor.commit();

            mStateSingleton.getNetworkService().sendCreateUserRequest(username);
        } else {
            usernameEditText.setError("O nome de utilizador não pode estar vazio!");
        }

    }

    private boolean validate(String username) {
        return !username.trim().equals("");
    }
}
