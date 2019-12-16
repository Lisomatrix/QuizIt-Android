package pt.lisomatrix.ptproject.UI.rooms;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import pt.lisomatrix.ptproject.UI.main.MainActivity;
import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.UI.room.RoomFragment;
import pt.lisomatrix.ptproject.adapter.RoomClickEventModel;
import pt.lisomatrix.ptproject.adapter.RoomsAdapter;
import pt.lisomatrix.ptproject.model.Room;
import pt.lisomatrix.ptproject.singleton.StateSingleton;

public class RoomsFragment extends Fragment {

    private RoomsViewModel mViewModel;
    private StateSingleton mStateSingleton;

    private RecyclerView mRoomsList;
    private RoomsAdapter mRoomsListAdapter;

    private TextView mNoRoomsText;
    private TextView mTopScoreText;

    private ExtendedFloatingActionButton mNewRoomFab;

    private Disposable clickedDisposable;
    private Disposable getRoomsDisposable;

    public static RoomsFragment newInstance() {
        return new RoomsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rooms_fragment, container, false);

        mRoomsList = view.findViewById(R.id.rooms_list);
        mNewRoomFab = view.findViewById(R.id.new_fab);
        mNoRoomsText = view.findViewById(R.id.no_room_text);
        mTopScoreText = view.findViewById(R.id.wrong_questions_button);
        mTopScoreText.setOnClickListener(textView -> ((MainActivity) getActivity()).showTopScoreFragment());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(RoomsViewModel.class);
        mStateSingleton = StateSingleton.getInstance();

        init();
    }

    @Override
    public void onResume() {
        super.onResume();

        mStateSingleton.getNetworkService().sendGetRoomsRequest();

        clickedDisposable = mRoomsListAdapter
                .getClickedItem()
                .subscribe(this::handleClickListItem);

        getRoomsDisposable = mStateSingleton
                .getRooms()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleGetRooms);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (clickedDisposable != null && !clickedDisposable.isDisposed()) {
            clickedDisposable.dispose();
        }

        if (getRoomsDisposable != null && !getRoomsDisposable.isDisposed()) {
            getRoomsDisposable.dispose();
        }
    }

    private void init() {
        mRoomsListAdapter = new RoomsAdapter(new ArrayList<>());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        mRoomsList.setHasFixedSize(true);
        mRoomsList.setLayoutManager(layoutManager);
        mRoomsList.setAdapter(mRoomsListAdapter);

        mNewRoomFab.setOnClickListener(this::handleFabClick);
    }

    private void handleFabClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Nome do lobby");

        EditText input = new EditText(getContext());
        input.getBackground().mutate().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);

        builder.setView(input);

        builder.setPositiveButton("Criar", (dialog, which) ->
                mStateSingleton.getNetworkService().sendCreateRoomRequest(input.getText().toString()));

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void handleClickListItem(RoomClickEventModel roomClickEventModel) {
        mStateSingleton.getNetworkService().sendJoinRoomRequest(roomClickEventModel.getRoom().getId());

        Fragment roomFragment = RoomFragment.newInstance(roomClickEventModel.getRoom().getId() + "");

        ((MainActivity) getActivity()).changeFragmentWithTransition(roomFragment, roomClickEventModel.getTextView(), MainActivity.ApplicationFragment.ROOM);
    }

    private void handleGetRooms(List<Room> rooms) {
        if (rooms.size() > 0) {
            mNoRoomsText.setVisibility(View.INVISIBLE);
        } else {
            mNoRoomsText.setVisibility(View.VISIBLE);
        }

        mRoomsListAdapter.updateRooms(rooms);
    }
}
