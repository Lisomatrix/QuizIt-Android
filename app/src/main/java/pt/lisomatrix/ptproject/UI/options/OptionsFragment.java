package pt.lisomatrix.ptproject.UI.options;

import androidx.cardview.widget.CardView;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProviders;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import pt.lisomatrix.ptproject.UI.main.MainActivity;
import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.messages.AnswerMessage;
import pt.lisomatrix.ptproject.model.Question;
import pt.lisomatrix.ptproject.model.Room;
import pt.lisomatrix.ptproject.model.User;
import pt.lisomatrix.ptproject.singleton.StateSingleton;
import pt.lisomatrix.ptproject.singleton.TimerSingleton;

public class OptionsFragment extends Fragment {

    public static final double SECONDS_PER_QUESTION = 20.0;

    private TextSwitcher mQuestionTextSwitcher;
    private TextView mQuestionText;
    private Button mOptionOneButton;
    private Button mOptionTwoButton;
    private Button mOptionThreeButton;
    private Button mOptionFourButton;

    private CardView mOptionOneCard;
    private CardView mOptionTwoCard;
    private CardView mOptionThreeCard;
    private CardView mOptionFourCard;

    private TextView mOptionTextOne;
    private TextView mOptionTextTwo;
    private TextView mOptionTextThree;
    private TextView mOptionTextFour;

    private TextView mChapterText;
    private TextView mTimeText;

    private Toolbar mToolbar;

    private ProgressBar mTimeProgressBar;

    private OptionsViewModel mViewModel;
    private StateSingleton mStateSingleton;

    private Question lastQuestion;
    private User me;
    private Room room;

    private Disposable getMeDisposable;
    private Disposable getJoinedRoomDisposable;
    private Disposable getQuestionDisposable;
    private Disposable tickDisposable;
    private  Disposable questionDisposable;

    private ColorStateList enabledColor = ColorStateList.valueOf(Color.parseColor("#ffffff"));
    private ColorStateList disabledColor = ColorStateList.valueOf(Color.parseColor("#f2f2f2"));

    private ColorStateList primaryColor = ColorStateList.valueOf(Color.parseColor("#FF5722"));
    private ColorStateList blackColor = ColorStateList.valueOf(Color.parseColor("#000000"));

    private ColorStateList correctColor = ColorStateList.valueOf(Color.parseColor("#FF4CAF50"));

    private boolean correctOptionIsShown = false;

    private int originalHeight = 0;

    public static OptionsFragment newInstance() {
        return new OptionsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.options_fragment, container, false);

        mTimeProgressBar = view.findViewById(R.id.question_time_progress);
        mQuestionTextSwitcher = view.findViewById(R.id.question_text_switcher);

        mOptionOneCard = view.findViewById(R.id.option_one);
        mOptionTwoCard = view.findViewById(R.id.option_two);
        mOptionThreeCard = view.findViewById(R.id.option_three);
        mOptionFourCard = view.findViewById(R.id.option_four);

        mOptionOneButton = view.findViewById(R.id.A);
        mOptionTwoButton = view.findViewById(R.id.B);
        mOptionThreeButton = view.findViewById(R.id.C);
        mOptionFourButton = view.findViewById(R.id.D);

        mOptionTextOne = view.findViewById(R.id.A_text);
        mOptionTextTwo = view.findViewById(R.id.B_text);
        mOptionTextThree = view.findViewById(R.id.C_text);
        mOptionTextFour = view.findViewById(R.id.D_text);

        mChapterText = view.findViewById(R.id.chapter_text);
        mTimeText = view.findViewById(R.id.seconds_text);

        mToolbar = view.findViewById(R.id.toolbar2);

        mQuestionTextSwitcher.setFactory(() -> {
            mQuestionText = new TextView(getContext());
            mQuestionText.setTextSize(35);
            mQuestionText.setTextColor(Color.rgb(255, 255, 255));
            mQuestionText.setGravity(Gravity.CENTER);

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mQuestionText, 10, 50, 2, TypedValue.COMPLEX_UNIT_DIP);
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mQuestionText, 25, 30, 1, TypedValue.COMPLEX_UNIT_DIP);

            return mQuestionText;
        });

        mTimeProgressBar.setProgressTintList(primaryColor);

        enableOrDisableButtons(false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mStateSingleton = StateSingleton.getInstance();
        mViewModel = ViewModelProviders.of(getActivity()).get(OptionsViewModel.class);

        if (TimerSingleton.getInstance().isRoomChanged()) {
            TimerSingleton.getInstance().setRoomChanged(false);
            mViewModel.resetQuestions();
        }

        init();
    }

    @Override
    public void onResume() {
        super.onResume();

        questionDisposable = mViewModel.getCurrentQuestionNumber()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(number -> mToolbar.setTitle("Pergunta: " + number));

        tickDisposable = TimerSingleton.getInstance().getPassedTime()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::tick);

        getMeDisposable = mStateSingleton.getMe().subscribe(user -> me = user);
        getJoinedRoomDisposable = mStateSingleton.getJoinedRoom().subscribe(room -> this.room = room);
        getQuestionDisposable = mStateSingleton.getQuestions()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleQuestions);

        reveal(mOptionOneCard, 50);
        reveal(mOptionTwoCard, 300);
        reveal(mOptionThreeCard, 550);
        reveal(mOptionFourCard, 800);
    }

    private void reveal(View view, int delay) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1, 1.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1, 1.1f);
        scaleX.setDuration(250);
        scaleY.setDuration(250);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        scaleX.setRepeatCount(1);
        scaleY.setRepeatCount(1);
        scaleX.setStartDelay(delay);
        scaleY.setStartDelay(delay);

        scaleX.start();
        scaleY.start();
    }

    private void showCorrectAnimation(int pos) {
        if (pos == 1) {
            scale(mOptionOneCard, (int) (originalHeight * 1.5));
            scale(mOptionTwoCard, 0);
            scale(mOptionThreeCard, 0);
            scale(mOptionFourCard, 0);
        } else if (pos == 2) {
            scale(mOptionOneCard, 0);
            scale(mOptionTwoCard, (int) (originalHeight * 1.5));
            scale(mOptionThreeCard, 0);
            scale(mOptionFourCard, 0);
        } else if (pos == 3) {
            scale(mOptionOneCard, 0);
            scale(mOptionTwoCard, 0);
            scale(mOptionThreeCard, (int) (originalHeight * 1.5));
            scale(mOptionFourCard, 0);
        } else {
            scale(mOptionOneCard, 0);
            scale(mOptionTwoCard, 0);
            scale(mOptionThreeCard, 0);
            scale(mOptionFourCard, (int) (originalHeight * 1.5));
        }
    }

    private void scale(View view, int value) {
        ValueAnimator anim = ValueAnimator.ofInt(view.getMeasuredHeight(), value);
        anim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = val;
            view.setLayoutParams(layoutParams);
        });
        anim.setDuration(500);
        anim.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getMeDisposable != null && !getMeDisposable.isDisposed()) {
            getMeDisposable.dispose();
        }

        if (getQuestionDisposable != null && !getQuestionDisposable.isDisposed()) {
            getQuestionDisposable.dispose();
        }

        if (getJoinedRoomDisposable != null && !getJoinedRoomDisposable.isDisposed()) {
            getJoinedRoomDisposable.dispose();
        }

        if (tickDisposable != null && !tickDisposable.isDisposed()) {
            tickDisposable.dispose();
        }

        if (questionDisposable != null && !questionDisposable.isDisposed()) {
            questionDisposable.dispose();
        }
    }

    private void tick(int passedTime) {
        double percentage = (passedTime + 0.0) / (SECONDS_PER_QUESTION * 1000.0) * 1000.0;
        int seconds = (int) (SECONDS_PER_QUESTION - (passedTime / 1000));

        if (percentage > 1000) {
            percentage = 1000;
        }

        if (seconds < 0) {
            seconds = 0;
        }

        if (seconds == 1) {
            mTimeText.setText(seconds + " Segundo");
        } else {
            mTimeText.setText(seconds + " Segundos");
        }


        if (passedTime > (SECONDS_PER_QUESTION * 1000.0)  && lastQuestion != null) {

            if (correctOptionIsShown) {
                return;
            }

            correctOptionIsShown = true;

            String correctOptionText = lastQuestion.getOptions()[lastQuestion.getAnswer()];
            OPTION correctOption = getOptionByText(correctOptionText);
            enableOrDisableButtons(false);

            for (int i = 0; i < mViewModel.getShuffledOptions().size(); i++) {
                if (mViewModel.getShuffledOptions().get(i).equals(correctOptionText)) {
                    switch (i) {
                        case 0:
                            correctOption = OPTION.A;
                            break;
                        case 1:
                            correctOption = OPTION.B;
                            break;
                        case 2:
                            correctOption = OPTION.C;
                            break;
                        case 3:
                            correctOption = OPTION.D;
                            break;
                    }
                    break;
                }
            }

            if (correctOption.equals(OPTION.A)) {
                showCorrectOption(mOptionOneCard, mOptionTextOne, mOptionOneButton);
                showCorrectAnimation(1);

            } else if (correctOption.equals(OPTION.B)) {
                showCorrectOption(mOptionTwoCard, mOptionTextTwo, mOptionTwoButton);
                showCorrectAnimation(2);

            } else if (correctOption.equals(OPTION.C)) {
                showCorrectOption(mOptionThreeCard, mOptionTextThree, mOptionThreeButton);
                showCorrectAnimation(3);

            } else if (correctOption.equals(OPTION.D)) {
                showCorrectOption(mOptionFourCard, mOptionTextFour, mOptionFourButton);
                showCorrectAnimation(4);
            }
        }

        mTimeProgressBar.setProgress((int) percentage);
    }

    private void sendAnswer(OPTION option) {
        enableOrDisableButtons(false);
        mStateSingleton.getNetworkService()
                .sendAnswerRequest(new AnswerMessage(option.ordinal(), me.getId(), room.getId()));
    }

    private void showCorrectOption(View card, TextView text, Button button) {
        card.setBackgroundTintList(correctColor);
        text.setTextColor(enabledColor);
        button.setBackgroundTintList(correctColor);
        button.setTextColor(enabledColor);
    }

    private void showSelectedOption(int buttonNumber) {
        switch (buttonNumber) {
            case 1:
                updateSelectedOptionUI(mOptionOneCard, mOptionTextOne, mOptionOneButton);
                break;
            case 2:
                updateSelectedOptionUI(mOptionTwoCard, mOptionTextTwo, mOptionTwoButton);
                break;
            case 3:
                updateSelectedOptionUI(mOptionThreeCard, mOptionTextThree, mOptionThreeButton);
                break;

            case 4:
                updateSelectedOptionUI(mOptionFourCard, mOptionTextFour, mOptionFourButton);
                break;
        }
    }

    private void updateSelectedOptionUI(View card, TextView text, Button button) {
        card.setBackgroundTintList(primaryColor);
        text.setTextColor(enabledColor);
        button.setBackgroundTintList(enabledColor);
        button.setTextColor(blackColor);
    }

    private void init() {

        mOptionOneCard.setOnClickListener(view -> {
            sendAnswer(getOptionByText(mViewModel.getShuffledOptions().get(0)));
            showSelectedOption(1);
            mViewModel.setSelectedOption(1);
        });
        mOptionTwoCard.setOnClickListener(view -> {
            sendAnswer(getOptionByText(mViewModel.getShuffledOptions().get(1)));
            showSelectedOption(2);
            mViewModel.setSelectedOption(2);
        });
        mOptionThreeCard.setOnClickListener(view -> {
            sendAnswer(getOptionByText(mViewModel.getShuffledOptions().get(2)));
            showSelectedOption(3);
            mViewModel.setSelectedOption(3);
        });
        mOptionFourCard.setOnClickListener(view -> {
            sendAnswer(getOptionByText(mViewModel.getShuffledOptions().get(3)));
            showSelectedOption(4);
            mViewModel.setSelectedOption(4);
        });
    }

    private OPTION getOptionByText(String answer) {
        for (int i = 0; i < lastQuestion.getOptions().length - 1; i++) {
            if (lastQuestion.getOptions()[i].equals(answer)) {
                switch (i) {
                    case 0: return OPTION.A;
                    case 1: return OPTION.B;
                    case 2: return OPTION.C;
                    case 3: return OPTION.D;
                }
                break;
            }
        }

        return OPTION.A;
    }

    private void handleQuestions(Question question) {
        correctOptionIsShown = false;

        if (mViewModel.getCurrentQuestionId() != question.getId()) {
            mViewModel.addQuestionNumber();
            mViewModel.setCurrentQuestionId(question.getId());
            List<String> shuffledOptions = Arrays.asList(question.getOptions().clone());
            Collections.shuffle(shuffledOptions);
            mViewModel.setShuffledOptions(shuffledOptions);
            mViewModel.setSelectedOption(-1);
        }

        lastQuestion = question;

        MainActivity mActivity = (MainActivity) getActivity();
        if (mActivity == null) {
            MainActivity activity = ((MainActivity) getActivity());

            if (activity != null) {

                activity.runOnUiThread(() -> updateUI(question, mViewModel.getShuffledOptions()));
            }
        } else {
            mActivity.runOnUiThread(() -> updateUI(question, mViewModel.getShuffledOptions()));
            int selectedOption = mViewModel.getSelectedOption();

            if (selectedOption != -1) {
                showSelectedOption(selectedOption);
            }
        }
    }

    private void updateUI(Question question, List<String> shuffledOptions) {
        enableOrDisableButtons(true);
        mQuestionTextSwitcher.setText(question.getQuestion());

        mOptionTextOne.setText(shuffledOptions.get(0));
        mOptionTextTwo.setText(shuffledOptions.get(1));
        mOptionTextThree.setText(shuffledOptions.get(2));

        mChapterText.setText("Capítulo: " + question.getChapter());

        if (question.getOptions().length == 4) {
            mOptionTextFour.setText(shuffledOptions.get(3));
            mOptionFourCard.setVisibility(View.VISIBLE);
        } else {
            mOptionFourCard.setVisibility(View.INVISIBLE);
        }

        if (originalHeight == 0) {
            originalHeight = mOptionOneCard.getMeasuredHeight();
        } else {
            if (originalHeight == 0) {
                originalHeight = mOptionOneCard.getMeasuredHeight();
            }

            scale(mOptionOneCard, originalHeight);
            scale(mOptionTwoCard, originalHeight);
            scale(mOptionThreeCard, originalHeight);
            scale(mOptionFourCard, originalHeight);
        }

        reveal(mOptionOneCard, 50);
        reveal(mOptionTwoCard, 300);
        reveal(mOptionThreeCard, 550);
        reveal(mOptionFourCard, 800);
    }

    private void enableOrDisableButtons(boolean enable) {
        setEnable(mOptionOneCard, mOptionOneButton, mOptionTextOne, enable);
        setEnable(mOptionTwoCard, mOptionTwoButton, mOptionTextTwo, enable);
        setEnable(mOptionThreeCard, mOptionThreeButton, mOptionTextThree, enable);
        setEnable(mOptionFourCard, mOptionFourButton, mOptionTextFour, enable);
    }

    private void setEnable(View card, Button button, TextView text, boolean enable) {
        card.setEnabled(enable);
        button.setBackgroundTintList(primaryColor);
        button.setTextColor(enabledColor);
        text.setTextColor(blackColor);
        card.setBackgroundTintList(enable ? enabledColor : disabledColor);
    }

    public enum OPTION {
        A,
        B,
        C,
        D
    }

}
