package me.cooperka.searchsms;

import java.util.Date;

// inbox = "1", sent = "2", others unknown
enum msgType {
    OTHER, INBOX, SENT
}

public class Message {

    private String text = "", name = "", number = "";
    private msgType type = msgType.OTHER;
    private long date = 0;
    private boolean marked = false;

    public boolean matches(String search) {
        if (text.toUpperCase().contains(search)
                || name.toUpperCase().contains(search)
                || number.toUpperCase().contains(search)
                || (new Date(date).toString()).toUpperCase().contains(search))
            return true;
        return false;
    }

    public msgType getType() {
        return type;
    }

    public String getTypeText() {
        if (this.type == msgType.INBOX)
            return "From";
        if (this.type == msgType.SENT)
            return "To";
        else
            return "[other]";
    }

    public void setType(String Type) {
        if (Type.equals("1"))
            this.type = msgType.INBOX;
        else if (Type.equals("2"))
            this.type = msgType.SENT;
        else
            this.type = msgType.OTHER;
        // TODO figure out other types
    }

    // Returns whether this 'type' is being searched, based on check boxes
    public boolean isBeingSearched(boolean I, boolean S, boolean O) {
        if (this.type == msgType.INBOX)
            return I;
        else if (this.type == msgType.SENT)
            return S;
        else // if (this.type == msgType.OTHER)
            return O;
    }

    public String getText() {
        return text;
    }

    public void setText(String Text) {
        this.text = Text;
    }

    public String getName() {
        return name;
    }

    public void setName(String Name) {
        this.name = Name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String Number) {
        this.number = Number;
    }

    public long getDate() {
        return date;
    }

    public String getDateString() {
        return new Date(date).toString();
    }

    public void setDate(long Date) {
        this.date = Date;
    }

    public boolean getMarked() {
        return marked;
    }

    public void setMarked(boolean Marked) {
        this.marked = Marked;
    }

}
