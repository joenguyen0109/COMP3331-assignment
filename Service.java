import java.io.*;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

class Service {
    static void appendToFile(String filePath, String[] text, int noOfLines) {
        File file = new File(filePath);
        FileWriter fr = null;
        BufferedWriter br = null;
        try {
            // to append to file, you need to initialize FileWriter using below constructor
            fr = new FileWriter(file, true);
            br = new BufferedWriter(fr);
            br.newLine();
            // you can use write or append method

            for (String line : text) {
                br.write(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    static int countFileLine(String name) {
        int linecount = 0;
        try {
            String filename = name;
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            while (reader.readLine() != null) {
                linecount++;
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return linecount;
    }

    static Date stringToDate(String stringdate) throws ParseException {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(stringdate);
    }

    static String dateToString(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
    }

    static void printBeacon(String[] beacon) {
        System.out.println(beacon[0] + ",");
        System.out.println(beacon[1] + ",");
        System.out.println(beacon[2] + ".");
    }

    static void printOutLog(String line) {
        String[] dataString = line.split(" ");
        System.out.println(dataString[0] + ",");
        System.out.println(dataString[1] + " " + dataString[2] + ",");
        System.out.println(dataString[3] + " " + dataString[4] + ";");
    }
}