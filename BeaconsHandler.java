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
    private static String _phone;
    private static ObjectOutputStream _objectOutput;

    checkExpire(String phone, ObjectOutputStream objectOutput) {
        _phone = phone;
        _objectOutput = objectOutput;
    }

    @Override
    public void run() {
        while (true) {
            if (BeaconsHandler.state.equals("Exit")) {
                break;
            } else {
                switch (BeaconsHandler.state) {
                    case "Checking":
                        try {
                            FileWriter f2 = new FileWriter("your_zID_contactlog.txt", false);
                            f2.write("");
                            f2.close();
                            BeaconsHandler.state = "No action";
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Upload":
                        uploadFile();
                        BeaconsHandler.state = "Waiting";
                        break;
                    case "Waiting":
                        if (!timeExpired.isEmpty()) {
                            if (timeExpired.getFirst().before(Calendar.getInstance().getTime())) {
                                try {
                                    String[] data = getDataFromFile();
                                    FileWriter f2 = new FileWriter("your_zID_contactlog.txt", false);
                                    f2.write("");
                                    f2.close();
                                    Service.appendToFile("your_zID_contactlog.txt", data, 1, "Remove");
                                    timeExpired.removeFirst();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        break;
                    case "Writing":
                        System.out.println(BeaconsHandler.dataTofile);
                        Service.appendToFile("your_zID_contactlog.txt", new String[] { BeaconsHandler.dataTofile }, 1,
                                "Writing");
                        BeaconsHandler.state = "Waiting";
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.SECOND, 3);
                        timeExpired.add(cal.getTime());
                        break;

                    default:
                        break;
                }
            }
        }

    }

    private static String[] getDataFromFile() {
        int lineCount = Service.countFileLine("your_zID_contactlog.txt");
        String[] data = new String[] {};
        if (lineCount > 1) {
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
        }
        return data;
    }

    private static void uploadFile() {

        String[] data = new String[Service.countFileLine("your_zID_contactlog.txt") + 1];
        data[0] = _phone;
        try {
            String filename = "your_zID_contactlog.txt";
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            int i = 1;
            while ((line = reader.readLine()) != null) {
                Service.printOutLog(line);
                data[i] = line;
                i++;
            }
            MessagesFormats<String[]> uploadMessage = new MessagesFormats<String[]>("Upload", data);
            _objectOutput.writeObject(uploadMessage);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
