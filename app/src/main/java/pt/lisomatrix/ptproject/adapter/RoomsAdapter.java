package pt.lisomatrix.ptproject.adapter;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.model.Room;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomItemListViewHolder> {

    private List<Room> mRooms;
    private Subject<RoomClickEventModel> clickedRoomSubject = PublishSubject.create();

    public RoomsAdapter(List<Room> rooms) {
        mRooms = rooms;
    }

    public void updateRooms(List<Room> rooms) {
        mRooms = rooms;
        notifyDataSetChanged();
    }
    
    public Observable<RoomClickEventModel> getClickedItem() {
        return clickedRoomSubject.serialize();
    }

    @NonNull
    @Override
    public RoomItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.room_item_list, parent, false);

        RoomItemListViewHolder viewHolder = new RoomItemListViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RoomItemListViewHolder holder, int position) {
        holder.bind(mRooms.get(position));
    }

    @Override
    public int getItemCount() {
        return mRooms.size();
    }


    public  class RoomItemListViewHolder extends RecyclerView.ViewHolder {

        private TextView roomName;
        private CardView container;
        private Button firstLetterButton;

        public RoomItemListViewHolder(@NonNull View itemView) {
            super(itemView);

            firstLetterButton = itemView.findViewById(R.id.first_letter_button);
            container = itemView.findViewById(R.id.card_container);
            roomName = itemView.findViewById(R.id.room_name_text);
        }


        public void bind(Room room) {
            roomName.setText(room.getName());
            ViewCompat.setTransitionName(roomName, room.getId() + "");
            container.setOnClickListener(view -> {
                clickedRoomSubject.onNext(new RoomClickEventModel(this.getAdapterPosition(), room, roomName));
            });
            firstLetterButton.setText(room.getName().charAt(0) + "");
        }
    }
}
