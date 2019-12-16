package pt.lisomatrix.ptproject.adapter;

import android.widget.TextView;

import pt.lisomatrix.ptproject.model.Room;

public class RoomClickEventModel {

    private final int position;
    private final Room room;
    private final TextView textView;

    public RoomClickEventModel(int position, Room room, TextView textView) {
        this.position = position;
        this.room = room;
        this.textView = textView;
    }

    public int getPosition() {
        return position;
    }

    public Room getRoom() {
        return room;
    }

    public TextView getTextView() {
        return textView;
    }
}
