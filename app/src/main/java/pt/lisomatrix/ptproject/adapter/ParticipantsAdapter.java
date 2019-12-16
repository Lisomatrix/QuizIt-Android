package pt.lisomatrix.ptproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.model.User;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ParticipantItemListViewHolder> {

    private List<User> mParticipants;

    public ParticipantsAdapter(List<User> participants) {
        this.mParticipants = participants;
        notifyDataSetChanged();
    }

    public void addParticipant(User user) {
        /*mParticipants.add(user);
        notifyDataSetChanged();*/
    }

    public void addAllParticipants(List<User> users) {
        mParticipants = users;
        notifyDataSetChanged();
    }

    public void removeParticipant(User user) {
        for (int i = 0; i < mParticipants.size(); i++) {
            if (mParticipants.get(i).getId().equals(user.getId())) {
                mParticipants.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ParticipantsAdapter.ParticipantItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.participant_item_list, parent, false);

        ParticipantsAdapter.ParticipantItemListViewHolder viewHolder = new ParticipantItemListViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantItemListViewHolder holder, int position) {
        holder.bind(mParticipants.get(position));
    }

    @Override
    public int getItemCount() {
        return mParticipants.size();
    }

    public class ParticipantItemListViewHolder extends RecyclerView.ViewHolder {

        private TextView participantName;

        public ParticipantItemListViewHolder(@NonNull View itemView) {
            super(itemView);

            participantName = itemView.findViewById(R.id.participant_name_text);
        }

        public void bind(User user) {
            participantName.setText(user.getName());
        }
    }
}
