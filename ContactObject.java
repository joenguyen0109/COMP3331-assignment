import java.util.Date;

// This class is the value for the hashmap which has tempID as the key, In order to map tempID and contact log
class ContactObject {
    private final String _phone;
    private final Date _start;
    private final Date _end;

    ContactObject(String id, Date start, Date end) {
        this._phone = id;
        this._start = start;
        this._end = end;
    }

    public String get_phone() {
        return _phone;
    }

    public Date get_start() {
        return _start;
    }

    public Date get_end() {
        return _end;
    }

}