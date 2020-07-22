import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


class BeaconsHandler extends Thread {
    private int _port;

    BeaconsHandler(int port) {
        _port = port;
    }

    @Override
    public void run() {
        try (DatagramSocket beaconHanldersSocket = new DatagramSocket(_port)) {
            byte[] buffer = new byte[65507]; 
            while(!Client.exit){
                DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
                beaconHanldersSocket.receive(packet);
                String message = new String(packet.getData());
                System.out.println(message);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
			e.printStackTrace();
		}
    }
}