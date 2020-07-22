import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Date;
class BeaconsHandler extends Thread {
    private int _port;
    static String state = "Writing";
    BeaconsHandler(int port) {
        _port = port;
    }

    @Override
    public void run() {
        try (DatagramSocket beaconHanldersSocket = new DatagramSocket(_port)) {
            while (true) {
                byte[] buffer = new byte[65507];
                DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
                beaconHanldersSocket.receive(packet);
                String message = new String(packet.getData(), packet.getOffset(), packet.getLength(), "UTF-8");
                state = "Writing";
                if (message.equals("Exit")) {
                    break;
                } else {
                    System.out.println("received beacon:");
                    Service.printBeacon(
                            new String[] { message.split(" ")[3], message.split(" ")[4] + " " + message.split(" ")[5],
                                    message.split(" ")[6] + " " + message.split(" ")[7] });
                    System.out.println("Current time is:");
                    Date currentTime = Calendar.getInstance().getTime();
                    System.out.println(Service.dateToString(currentTime) + ".");

                }
            }
            beaconHanldersSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class checkExpire extends Thread{
    @Override
    public void run(){

    }
}