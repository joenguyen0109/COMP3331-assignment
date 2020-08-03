import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

class BeaconsHandler extends Thread {
    private int _port;
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
                // recieve data
                byte[] buffer = new byte[65507];
                DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
                beaconHanldersSocket.receive(packet);
                String message = new String(packet.getData(), packet.getOffset(), packet.getLength(), "UTF-8");

                if (message.equals("Exit")) {
                    break;
                } else {
                    // Print out beacon
                    System.out.println("received beacon:");
                    dataTofile = message.split(" ")[3] + " " + message.split(" ")[4] + " " + message.split(" ")[5] + " "
                            + message.split(" ")[6] + " " + message.split(" ")[7];
                    Service.printBeacon(dataTofile.split(" "));

                    // Print out current time
                    System.out.println("Current time is:");
                    Date currentTime = Calendar.getInstance().getTime();
                    System.out.println(Service.dateToString(currentTime) + ".");

                    // Check beacon valid
                    Date start = Service.stringToDate(message.split(" ")[4] + " " + message.split(" ")[5]);
                    Date end = Service.stringToDate(message.split(" ")[6] + " " + message.split(" ")[7]);
                    if (start.before(currentTime) && end.after(currentTime)) {
                        System.out.println("The beacon is valid");
                        MessagesFormats<String[]> m = new MessagesFormats<String[]>("Beacon", message.split(" "));
                        _objectOutput.writeObject(m);
                        ManageState.setSate("Writing");
                    } else {
                        System.out.println("The beacon is invalid");
                    }
                }
            }
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
            if (ManageState.state.equals("Exit")) {
                break;
            } else {
                switch (ManageState.state) {
                    case "Checking":
                        try {
                            FileWriter f2 = new FileWriter("your_z5253838_contactlog.txt", false);
                            f2.write("");
                            f2.close();
                            ManageState.setSate("No action"); 
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Upload":
                        uploadFile();
                        ManageState.setSate("Waiting");
                        break;
                    case "Waiting":
                        if (!timeExpired.isEmpty()) {
                            if (timeExpired.getFirst().before(Calendar.getInstance().getTime())) {
                                String[] data = getDataFromFile();
                                Service.appendToFile("your_z5253838_contactlog.txt", data, 1, "Remove");
                                timeExpired.removeFirst();
                            }
                        }
                        break;
                    case "Writing":
                        Service.appendToFile("your_z5253838_contactlog.txt", new String[] { BeaconsHandler.dataTofile }, 1,
                                "Writing");
                        ManageState.setSate("Waiting");
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MINUTE, 3);
                        timeExpired.add(cal.getTime());
                        break;

                    default:
                        break;
                }
            }
        }

    }

    private static String[] getDataFromFile() {
        int lineCount = Service.countFileLine("your_z5253838_contactlog.txt");
        String[] data = new String[] {};
        if (lineCount > 1) {
            data = new String[lineCount - 1];
            try {
                String filename = "your_z5253838_contactlog.txt";
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
        String[] data = new String[Service.countFileLine("your_z5253838_contactlog.txt") + 1];
        data[0] = _phone;
        try {
            String filename = "your_z5253838_contactlog.txt";
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
