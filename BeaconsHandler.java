import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.Charset;

class BeaconsHandler extends Thread {
    private int _port;

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
                if (message.equals("Exit")) {
                    break;
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