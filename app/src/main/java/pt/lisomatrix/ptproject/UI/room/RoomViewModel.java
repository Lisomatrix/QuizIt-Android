package pt.lisomatrix.ptproject.UI.room;

import androidx.lifecycle.ViewModel;

import pt.lisomatrix.ptproject.model.Room;

public class RoomViewModel extends ViewModel {

    private Room joinedRoom;

    public void setJoinedRoom(Room room) {
        joinedRoom = room;
    }

    public Room getJoinedRoom() {
        return joinedRoom;
    }
}
