import java.util.Date;

class UploadObject {
    private final String _id;
    private final Date _start;
    private final Date _end;

    UploadObject(String id, Date start, Date end) {
        this._id = id;
        this._start = start;
        this._end = end;
    }

    public String get_id() {
        return _id;
    }

    public Date get_start() {
        return _start;
    }

    public Date get_end() {
        return _end;
    }

}