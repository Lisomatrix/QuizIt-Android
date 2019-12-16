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
import pt.lisomatrix.ptproject.model.TopScore;

public class TopScoreAdapter extends RecyclerView.Adapter<TopScoreAdapter.TopScoreItemListViewHolder> {

    private List<TopScore> mTopScores;

    public void setTopScore(List<TopScore> topScore) {
        mTopScores = topScore;
        notifyDataSetChanged();
    }

    public TopScoreAdapter(List<TopScore> topScores) {
        mTopScores = topScores;
    }

    @NonNull
    @Override
    public TopScoreItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.top_score_list_item, parent, false);

        TopScoreAdapter.TopScoreItemListViewHolder viewHolder = new TopScoreItemListViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TopScoreItemListViewHolder holder, int position) {
        holder.bind(mTopScores.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mTopScores.size();
    }

    public class TopScoreItemListViewHolder extends RecyclerView.ViewHolder {

        private TextView nameText;
        private TextView scoreText;

        public TopScoreItemListViewHolder(@NonNull View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.name_text);
            scoreText = itemView.findViewById(R.id.score_text);
        }

        public void bind(TopScore topScore, int position) {
            nameText.setText(topScore.getName());
            scoreText.setText(topScore.getScore() + "");
        }
    }
}
