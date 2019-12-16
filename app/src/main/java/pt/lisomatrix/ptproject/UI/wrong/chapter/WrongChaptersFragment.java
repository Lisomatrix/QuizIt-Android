package pt.lisomatrix.ptproject.UI.wrong.chapter;

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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.UI.main.MainActivity;
import pt.lisomatrix.ptproject.adapter.WrongChaptersAdapter;
import pt.lisomatrix.ptproject.model.Question;
import pt.lisomatrix.ptproject.singleton.StateSingleton;

public class WrongChaptersFragment extends Fragment {

    private WrongChaptersViewModel mViewModel;
    private StateSingleton mStateSingleton;

    private Toolbar mToolbar;

    private RecyclerView mChaptersList;
    private WrongChaptersAdapter mWrongChaptersAdapter;

    private Disposable wrongQuestionsDisposable;
    private Disposable backButtonDisposable;
    private Disposable clickedDisposable;

    public static WrongChaptersFragment newInstance() {
        return new WrongChaptersFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrong_chapters_fragment, container, false);

        mChaptersList = view.findViewById(R.id.wrong_chapters_list);
        mToolbar = view.findViewById(R.id.toolbar);

        mToolbar.setNavigationIcon(getContext().getApplicationContext().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        mToolbar.setNavigationOnClickListener(v -> ((MainActivity) getActivity()).showScoreFragment());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(WrongChaptersViewModel.class);
        mStateSingleton = StateSingleton.getInstance();

        mChaptersList.setHasFixedSize(true);
        mChaptersList.setLayoutManager(new LinearLayoutManager(getContext()));
        mWrongChaptersAdapter = new WrongChaptersAdapter(new ArrayList<>());
        mChaptersList.setAdapter(mWrongChaptersAdapter);

        clickedDisposable = mWrongChaptersAdapter.getClickedItem()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(entry -> ((MainActivity) getActivity()).showWrongQuestions(entry));
    }

    @Override
    public void onStart() {
        super.onStart();

        wrongQuestionsDisposable = mStateSingleton.getWrongQuestions()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleWrongQuestions);

        backButtonDisposable = ((MainActivity) getActivity()).getBackButtonPressed()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(x -> ((MainActivity) getActivity()).showScoreFragment());
    }

    @Override
    public void onStop() {
        super.onStop();

        if (wrongQuestionsDisposable != null && !wrongQuestionsDisposable.isDisposed()) {
            wrongQuestionsDisposable.dispose();
        }

        if (backButtonDisposable != null && !backButtonDisposable.isDisposed()) {
            backButtonDisposable.dispose();
        }

        if (clickedDisposable != null && !clickedDisposable.isDisposed()) {
            clickedDisposable.dispose();
        }
    }

    private void handleWrongQuestions(Map<String, List<Question>> wrongQuestionsMap) {
        List<AbstractMap.SimpleEntry<String, List<Question>>> chaptersList = new ArrayList<>();

        for (Map.Entry<String, List<Question>> entry : wrongQuestionsMap.entrySet()) {
            String id = entry.getKey();
            List<Question> answers = entry.getValue();

            chaptersList.add(new AbstractMap.SimpleEntry<>(id, answers));
        }

        mWrongChaptersAdapter.setChapters(chaptersList);
    }
}
