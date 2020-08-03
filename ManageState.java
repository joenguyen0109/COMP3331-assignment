class ManageState{
    volatile static String _state = "";
    static void setSate(String state){
        _state = state;
    }
}