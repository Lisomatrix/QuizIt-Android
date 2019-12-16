package pt.lisomatrix.ptproject.UI.score;

import androidx.lifecycle.ViewModelProviders;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.reactivex.disposables.Disposable;
import pt.lisomatrix.ptproject.UI.main.MainActivity;
import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.messages.ScoreMessage;
import pt.lisomatrix.ptproject.singleton.StateSingleton;

public class ScoreFragment extends Fragment {

    private TextView scoreTextView;
    private TextView wrongTextView;
    private TextView correctTextView;
    private TextView winnerTextView;
    private TextView winnerNameTextView;

    private Button backToRoomsButton;
    private Button wrongQuestionsButton;

    private ScoreViewModel mViewModel;
    private StateSingleton mStateSingleton;

    private Disposable getScoreDisposable;

    public static ScoreFragment newInstance() {
        return new ScoreFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.score_fragment, container, false);

        scoreTextView = view.findViewById(R.id.score_text);
        wrongTextView = view.findViewById(R.id.wrong_text_view);
        correctTextView = view.findViewById(R.id.correct_text_view);
        backToRoomsButton = view.findViewById(R.id.back_button);
        wrongQuestionsButton = view.findViewById(R.id.wrong_questions_button);
        winnerTextView = view.findViewById(R.id.winner_text);
        winnerNameTextView = view.findViewById(R.id.winner_name_text);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        backToRoomsButton.setOnClickListener(view -> ((MainActivity) getActivity()).showRoomsFragment());
        wrongQuestionsButton.setOnClickListener(view -> ((MainActivity) getActivity()).showWrongChapters());
        getScoreDisposable = mStateSingleton.getScore().subscribe(this::handleScore);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getScoreDisposable != null && !getScoreDisposable.isDisposed()) {
            getScoreDisposable.dispose();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(ScoreViewModel.class);
        mStateSingleton = StateSingleton.getInstance();
    }

    private void handleScore(ScoreMessage scoreMessage) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                scoreTextView.setText("O teu score foi: " + (int) scoreMessage.getScore());
                correctTextView.setText("Corretas: " + scoreMessage.getCorrect());
                wrongTextView.setText("Erradas: " + scoreMessage.getWrong());

                if (!scoreMessage.getWinner().equals("")) {
                    winnerTextView.setText("O vencedor foi:");
                    winnerNameTextView.setText(scoreMessage.getWinner());
                    winnerTextView.setVisibility(View.VISIBLE);
                    winnerNameTextView.setVisibility(View.VISIBLE);
                    animate();
                } else {
                    winnerTextView.setVisibility(View.INVISIBLE);
                    winnerNameTextView.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private void animate() {

        ObjectAnimator rotate = ObjectAnimator.ofFloat(winnerNameTextView, "rotation", -35f, 35f);
        rotate.setDuration(2000);
        rotate.setRepeatCount(999);
        rotate.setRepeatMode(ValueAnimator.REVERSE);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(winnerNameTextView, "scaleX", 1, 2);
        scaleX.setDuration(1000);
        scaleX.setRepeatCount(999);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(winnerNameTextView, "scaleY", 1, 2);
        scaleY.setDuration(1000);
        scaleY.setRepeatCount(999);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);

        rotate.start();
        scaleX.start();
        scaleY.start();

    }
}
