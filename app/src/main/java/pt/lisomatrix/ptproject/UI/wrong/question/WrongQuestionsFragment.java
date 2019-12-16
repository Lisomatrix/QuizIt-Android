package pt.lisomatrix.ptproject.UI.wrong.question;

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
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.UI.main.MainActivity;
import pt.lisomatrix.ptproject.adapter.WrongQuestionsAdapter;
import pt.lisomatrix.ptproject.model.Question;

public class WrongQuestionsFragment extends Fragment {

    private WrongQuestionsViewModel mViewModel;

    private AbstractMap.SimpleEntry<String, List<Question>> chapter;

    private Toolbar mToolbar;

    private RecyclerView mQuestionsList;
    private WrongQuestionsAdapter mWrongQuestionsAdapter;

    private Disposable backButtonDisposable;

    public static WrongQuestionsFragment newInstance(AbstractMap.SimpleEntry<String, List<Question>> entry) {
        return new WrongQuestionsFragment(entry);
    }

    public WrongQuestionsFragment(AbstractMap.SimpleEntry<String, List<Question>> chapter) {
        this.chapter = chapter;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrong_questions_fragment, container, false);

        mQuestionsList = view.findViewById(R.id.wrong_questions_list);

        mToolbar = view.findViewById(R.id.toolbar);

        mToolbar.setNavigationIcon(getContext().getApplicationContext().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        mToolbar.setNavigationOnClickListener(v -> ((MainActivity) getActivity()).showWrongChapters());
        mToolbar.setTitle("Capítulo " + chapter.getKey());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(WrongQuestionsViewModel.class);


        mQuestionsList.setHasFixedSize(false);
        mQuestionsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mWrongQuestionsAdapter = new WrongQuestionsAdapter(chapter.getValue());
        mQuestionsList.setAdapter(mWrongQuestionsAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        backButtonDisposable = ((MainActivity) getActivity()).getBackButtonPressed()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(x -> ((MainActivity) getActivity()).showWrongChapters());
    }

    @Override
    public void onStop() {
        super.onStop();

        if (backButtonDisposable != null && !backButtonDisposable.isDisposed()) {
            backButtonDisposable.dispose();
        }
    }
}
