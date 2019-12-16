package pt.lisomatrix.ptproject.UI.options;

import android.os.Build;
import android.util.Log;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import pt.lisomatrix.ptproject.model.Room;

public class OptionsViewModel extends ViewModel {

    private BehaviorSubject<Integer> totalQuestionsSubject = BehaviorSubject.create();

    private List<String> shuffledOptions;

    private int selectedOption;

    private int currentQuestionId = -1;

    public void resetQuestions() {
        totalQuestionsSubject.onNext(0);
    }

    public int getCurrentQuestionId() {
        return currentQuestionId;
    }

    public void setCurrentQuestionId(int currentQuestionId) {
        this.currentQuestionId = currentQuestionId;
    }

    public void setSelectedOption(int option) {
        this.selectedOption = option;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public OptionsViewModel() {
        totalQuestionsSubject.onNext(0);
    }

    public void setShuffledOptions(List<String> options) {
        shuffledOptions = options;
    }

    public List<String> getShuffledOptions() {
        return shuffledOptions;
    }


    public void addQuestionNumber() {
        totalQuestionsSubject.onNext(totalQuestionsSubject.getValue() + 1);
    }

    public Observable<Integer> getCurrentQuestionNumber() {
        return totalQuestionsSubject.serialize();
    }
}
