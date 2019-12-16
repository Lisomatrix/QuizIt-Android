package pt.lisomatrix.ptproject.model;

import pt.lisomatrix.ptproject.constants.MessageType;

public class Message {

    private MessageType type;
    private Object data;

    public Message(MessageType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public Message() {
        super();
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
