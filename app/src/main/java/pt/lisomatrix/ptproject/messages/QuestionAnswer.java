package pt.lisomatrix.ptproject.messages;

public class QuestionAnswer {

    private int question;
    private boolean isCorrect;

    public QuestionAnswer(int question, boolean isCorrect) {
        this.question = question;
        this.isCorrect = isCorrect;
    }

    public int getQuestion() {
        return question;
    }

    public void setQuestion(int question) {
        this.question = question;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}
