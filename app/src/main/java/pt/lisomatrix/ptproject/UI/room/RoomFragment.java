package pt.lisomatrix.ptproject.UI.room;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.AbstractMap;
import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import pt.lisomatrix.ptproject.UI.main.MainActivity;
import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.adapter.ParticipantsAdapter;
import pt.lisomatrix.ptproject.model.Room;
import pt.lisomatrix.ptproject.model.User;
import pt.lisomatrix.ptproject.singleton.StateSingleton;

public class RoomFragment extends Fragment {

    // View Models
    private RoomViewModel mViewModel;
    private StateSingleton mStateSingleton;

    private RecyclerView mParticipantsList;
    private ParticipantsAdapter mParticipantsListAdapter;

    private ExtendedFloatingActionButton mStartButton;
    private TextView mRoomName;

    private Room joinedRoom;
    private Toolbar mToolbar;

    private Disposable backClickDisposable;
    private Disposable getJoinedRoomDisposable;
    private Disposable getUsersDisposable;
    private Disposable hasEndedDisposable;
    private Disposable isDeletedDisposable;

    private final String transitionName;

    public static RoomFragment newInstance(String transitionName) {
        return new RoomFragment(transitionName);
    }

    public RoomFragment(String transitionName) {
        this.transitionName = transitionName;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        postponeEnterTransition();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(TransitionInflater
                    .from(getContext()).inflateTransition(android.R.transition.move));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.room_fragment, container, false);

        mRoomName = view.findViewById(R.id.room_name);
        mParticipantsList = view.findViewById(R.id.participants_list);
        mStartButton = view.findViewById(R.id.start_fab);
        mToolbar = view.findViewById(R.id.toolbar);

        mToolbar.setNavigationIcon(getContext().getApplicationContext().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        mToolbar.setNavigationOnClickListener(v -> handleBackPress(true));


        if (joinedRoom != null) {
            mRoomName.setText(joinedRoom.getName());
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mRoomName.setTransitionName(transitionName);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(RoomViewModel.class);
        mStateSingleton = StateSingleton.getInstance();

        init();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mStateSingleton = StateSingleton.getInstance();

        if (context instanceof MainActivity) {
            getUsersDisposable = mStateSingleton.getUsers().observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleNextUser);
            hasEndedDisposable = mStateSingleton.hasEnded().subscribe(this::handleHasEnded);
            isDeletedDisposable = mStateSingleton.isJoinedRoomDeleted().observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleRoomDeleted);
            getJoinedRoomDisposable = mStateSingleton.getJoinedRoom()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleJoinedRoom);

            backClickDisposable = ((MainActivity) context).getBackButtonPressed()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleBackPress);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (backClickDisposable != null && !backClickDisposable.isDisposed()) {
            backClickDisposable.dispose();
        }

        if (getUsersDisposable != null && !getUsersDisposable.isDisposed()) {
            getUsersDisposable.dispose();
        }

        if (getJoinedRoomDisposable != null && !getJoinedRoomDisposable.isDisposed()) {
            getJoinedRoomDisposable.dispose();
        }

        if (hasEndedDisposable != null && !hasEndedDisposable.isDisposed()) {
            hasEndedDisposable.dispose();
        }

        if (isDeletedDisposable != null && !isDeletedDisposable.isDisposed()) {
            isDeletedDisposable.dispose();
        }
    }

    /***
     * Initialize Fragment views and listeners
     */
    private void init() {
        mParticipantsListAdapter = new ParticipantsAdapter(new ArrayList<>());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        mParticipantsList.setHasFixedSize(true);
        mParticipantsList.setLayoutManager(layoutManager);
        mParticipantsList.setAdapter(mParticipantsListAdapter);


        mStartButton.setOnClickListener(view -> {
            mStateSingleton.getNetworkService().sendStartRequest();
            ((MainActivity) getActivity()).showOptionsFragment();
        });
    }

    private void handleRoomDeleted(int deletedId) {
        if (mViewModel.getJoinedRoom() == null) {
            return;
        }

        if (mViewModel.getJoinedRoom().getId() != deletedId) {
            return;
        }

        mStateSingleton.clearJoinedRoom();
        new AlertDialog.Builder(getActivity())
                .setTitle("Lobby apagado")
                .setMessage("O lobby ao qual te juntaste foi apagado.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    ((MainActivity) getActivity()).showRoomsFragment();
                })
                .setIcon(R.drawable.ic_info_orange_24dp)
                .show();
    }

    private void handleBackPress(boolean clicked) {
        if (joinedRoom.isCreator()) {
            mStateSingleton.getNetworkService().sendDeleteRoom();
        } else {
            mStateSingleton.getNetworkService().sendLeaveRoom();
        }

        ((MainActivity) getActivity()).showRoomsFragment();
    }

    private void handleJoinedRoom(Room room) {
        this.joinedRoom = room;
        mViewModel.setJoinedRoom(room);
        mParticipantsListAdapter.addAllParticipants(room.getParticipants());

        if (!room.isCreator()) {
            mStartButton.setVisibility(View.INVISIBLE);
        }


        mRoomName.setText(room.getName());
    }

    private void handleHasEnded(boolean hasEnded) {
        if (!hasEnded) {
            // Start questions fragment
            ((MainActivity) getActivity()).showOptionsFragment();
        }
    }

    private void handleNextUser(AbstractMap.SimpleEntry<Boolean, User> entry) {
        if (entry.getKey()) {
            mParticipantsListAdapter.addParticipant(entry.getValue());
        } else {
            mParticipantsListAdapter.removeParticipant(entry.getValue());
        }
    }
}
