package pt.lisomatrix.ptproject.messages;

public class AnswerMessage {

    private int roomId;
    private String participantId;
    private int answer;

    public AnswerMessage(int answer, String id, int roomId) {
        this.roomId = roomId;
        this.participantId = id;
        this.answer = answer;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }
}
