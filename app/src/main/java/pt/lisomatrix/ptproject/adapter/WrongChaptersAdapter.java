package pt.lisomatrix.ptproject.adapter;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.AbstractMap;
import java.util.List;


import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.model.Question;

public class WrongChaptersAdapter extends RecyclerView.Adapter<WrongChaptersAdapter.WrongChapterItemList> {

    private PublishSubject<AbstractMap.SimpleEntry<String, List<Question>>> clickSubject = PublishSubject.create();

    private List<AbstractMap.SimpleEntry<String, List<Question>>> mChapters;

    public WrongChaptersAdapter(List<AbstractMap.SimpleEntry<String, List<Question>>> chapters) {
        this.mChapters = chapters;
        notifyDataSetChanged();
    }

    public void setChapters( List<AbstractMap.SimpleEntry<String, List<Question>>> chapters) {
        this.mChapters = chapters;
        notifyDataSetChanged();
    }

    public Observable<AbstractMap.SimpleEntry<String, List<Question>>> getClickedItem() {
        return clickSubject.serialize();
    }

    @NonNull
    @Override
    public WrongChapterItemList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wrong_chapter_list_item, parent, false);

        WrongChapterItemList viewHolder = new WrongChapterItemList(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WrongChapterItemList holder, int position) {
        holder.bind(mChapters.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mChapters.size();
    }

    public class  WrongChapterItemList extends RecyclerView.ViewHolder {

        private TextView chapterNumber;
        private TextView questionsNumber;
        private ConstraintLayout container;

        public WrongChapterItemList(@NonNull View itemView) {
            super(itemView);

            chapterNumber = itemView.findViewById(R.id.chapter_text);
            questionsNumber= itemView.findViewById(R.id.question_size_text);
            container = itemView.findViewById(R.id.container);
        }

        public void bind(AbstractMap.SimpleEntry<String, List<Question>> entry, int position) {
            container.setOnClickListener(view -> clickSubject.onNext(entry));
            chapterNumber.setText(entry.getKey());
            questionsNumber.setText(entry.getValue().size() + " Respostas incorretas");
        }
    }
}
