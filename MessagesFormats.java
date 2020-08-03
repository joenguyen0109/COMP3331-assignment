import java.io.Serializable;

/* 
    This is the application protocol
*/
@SuppressWarnings("serial")
class MessagesFormats<T> implements Serializable{
    private String _command;
    private T _data;

    MessagesFormats(String command, T data ){
        this._command = command;
        this._data = data;
    }

    void setData(T data){
        this._data = data;
    }
    void setTitle(String command){
        this._command = command;
    }
	String getTitle() {
		return _command;
    }
    T getData(){
        return _data;
    }
}