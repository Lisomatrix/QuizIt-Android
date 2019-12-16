package pt.lisomatrix.ptproject.UI.topscore;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.UI.main.MainActivity;
import pt.lisomatrix.ptproject.adapter.TopScoreAdapter;
import pt.lisomatrix.ptproject.singleton.StateSingleton;

public class TopScoreFragment extends Fragment {

    private TopScoreViewModel mViewModel;
    private StateSingleton mStateSingleton;

    private RecyclerView mTopScoreList;
    private TopScoreAdapter mTopScoreAdapter;

    private Toolbar mToolbar;

    private Disposable backPressDisposable;
    private Disposable topScoreDisposable;

    public static TopScoreFragment newInstance() {
        return new TopScoreFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.top_score_fragment, container, false);

        mTopScoreList = view.findViewById(R.id.top_score_list);
        mToolbar = view.findViewById(R.id.toolbar);

        mToolbar.setNavigationIcon(getContext().getApplicationContext().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        mToolbar.setNavigationOnClickListener(v -> handleBackPress(true));

        mTopScoreAdapter = new TopScoreAdapter(new ArrayList<>());
        mTopScoreList.setHasFixedSize(true);
        mTopScoreList.setLayoutManager(new LinearLayoutManager(getContext()));
        mTopScoreList.setAdapter(mTopScoreAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(TopScoreViewModel.class);
        mStateSingleton = StateSingleton.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();

        backPressDisposable = ((MainActivity) getActivity())
                .getBackButtonPressed()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleBackPress);

        topScoreDisposable = mStateSingleton.getTopScore()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mTopScoreAdapter::setTopScore);

        mStateSingleton.getNetworkService().sendGetTopScore();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (backPressDisposable != null && !backPressDisposable.isDisposed()) {
            backPressDisposable.dispose();
        }

        if (topScoreDisposable != null && !topScoreDisposable.isDisposed()) {
            topScoreDisposable.dispose();
        }
    }

    private void handleBackPress(boolean backPressed) {
        ((MainActivity) getActivity()).showRoomsFragment();
    }
}
