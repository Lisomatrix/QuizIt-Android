package pt.lisomatrix.ptproject.adapter;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.model.Question;

public class WrongQuestionsAdapter extends RecyclerView.Adapter<WrongQuestionsAdapter.WrongQuestionItemList> {

    private List<Question> questionList;

    public WrongQuestionsAdapter(List<Question> questions) {
        this.questionList = questions;
    }

    @NonNull
    @Override
    public WrongQuestionItemList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wrong_question_list_item, parent, false);

        WrongQuestionItemList viewHolder = new WrongQuestionItemList(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WrongQuestionItemList holder, int position) {
        holder.bind(questionList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public class WrongQuestionItemList extends RecyclerView.ViewHolder {

        private TextView questionNumberText;
        private TextView questionText;
        private TextView questionAnswerText;

        public WrongQuestionItemList(@NonNull View itemView) {
            super(itemView);

            questionNumberText = itemView.findViewById(R.id.question_number_text);
            questionText = itemView.findViewById(R.id.question_text);
            questionAnswerText = itemView.findViewById(R.id.answer_text);
        }

        public void bind(Question question, int position) {
            questionAnswerText.setText("Resposta: " + question.getOptions()[question.getAnswer()]);
            questionText.setText("Pergunta: " + question.getQuestion());
            questionNumberText.setText(position + 1 + "");
        }
    }
}
