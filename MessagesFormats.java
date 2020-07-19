import java.io.Serializable;
@SuppressWarnings("serial")
class MessagesFormats<T> implements Serializable{
    private String _command;
    private T _data;

    MessagesFormats(String command, T data ){
        this._command = command;
        this._data = data;
    }

	String getTitle() {
		return _command;
    }
    T getData(){
        return _data;
    }
}