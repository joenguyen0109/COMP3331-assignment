import java.util.Date;
/* 
    this class is the value of the hashmap which has phone number as the key
    to keep track of number fail log in. 
*/
class AuthObject{
    private final String _password;
    private Date _date;
    private int _signTime = 0;

    AuthObject(String password){
        this._password = password;
    }

    public Date get_date() {
        return _date;
    }
    public void set_date() {
        this._date = new Date();
    }

    public int get_signTime() {
        return _signTime;
    }

    public void set_signTime(int _signTime) {
        this._signTime = _signTime;
    }

    public String get_password() {
        return _password;
    }



}