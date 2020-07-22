import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

class BeaconsHandler extends Thread {
    private int _port;
    static String state = "Checking";
    private ObjectOutputStream _objectOutput;
    static String dataTofile = "";

    BeaconsHandler(int port, ObjectOutputStream objectOutput) {
        _port = port;
        _objectOutput = objectOutput;
    }

    @Override
    public void run() {
        try (DatagramSocket beaconHanldersSocket = new DatagramSocket(_port)) {
            checkExpire checkExpires = new checkExpire();
            checkExpires.start();
            while (true) {
                byte[] buffer = new byte[65507];
                DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
                beaconHanldersSocket.receive(packet);
                String message = new String(packet.getData(), packet.getOffset(), packet.getLength(), "UTF-8");
                if (message.equals("Exit")) {
                    break;
                } else {
                    System.out.println("received beacon:");
                    dataTofile = message.split(" ")[3] + " " + message.split(" ")[4] + " " + message.split(" ")[5] + " "
                            + message.split(" ")[6] + " " + message.split(" ")[7];
                    
                    Service.printBeacon(dataTofile.split(" "));
                    System.out.println("Current time is:");
                    Date currentTime = Calendar.getInstance().getTime();
                    System.out.println(Service.dateToString(currentTime) + ".");
                    Date start = Service.stringToDate(message.split(" ")[4] + " " + message.split(" ")[5]);
                    Date end = Service.stringToDate(message.split(" ")[6] + " " + message.split(" ")[7]);
                    if (start.before(currentTime) && end.after(currentTime)) {
                        System.out.println("The beacon is valid");
                        MessagesFormats<String[]> m = new MessagesFormats<String[]>("Beacon", message.split(" "));
                        _objectOutput.writeObject(m);
                        state = "Writing";
                    } else {
                        System.out.println("The beacon is invalid");
                    }
                }
            }
            state = "Exit";
            beaconHanldersSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}

class checkExpire extends Thread {
    LinkedList<Date> timeExpired = new LinkedList<Date>();

    @Override
    public void run() {

        while (true) {
            if (BeaconsHandler.state.equals("Exit")) {
                break;
            } else {
                switch (BeaconsHandler.state) {
                    case "Waiting":
                        //System.out.println("Waiting State");
                        if (!timeExpired.isEmpty()) {
                            if (timeExpired.getFirst().before(Calendar.getInstance().getTime())) {
                                try {
                                    FileWriter f2 = new FileWriter("your_zID_contactlog.txt", false);
                                    f2.write("");
                                    f2.close();
                                    Service.appendToFile("your_zID_contactlog.txt", getDataFromFile(), 1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        break;
                    case "Writing":
                        System.out.println("Writing State");
                        System.out.println(BeaconsHandler.dataTofile);
                        Service.appendToFile("your_zID_contactlog.txt", new String[] { BeaconsHandler.dataTofile }, 1);
                        BeaconsHandler.state = "Waiting";
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MINUTE, 3);
                        timeExpired.add(cal.getTime());
                        break;
                    default:
                        //System.out.println("Default State");
                        break;
                }
            }
        }

    }

    private static String[] getDataFromFile() {
        int lineCount = Service.countFileLine("your_zID_contactlog.txt");
        String[] data;
        if(lineCount == 0){
            data = new String[lineCount - 1];
            try {
                String filename = "your_zID_contactlog.txt";
                String line;
                BufferedReader reader = new BufferedReader(new FileReader(filename));
                reader.readLine();
                int i = 0;
                while ((line = reader.readLine()) != null) {
                    data[i] = line;
                    i++;
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            data = new String[]{}; 
        }
        return data;
    }
}
