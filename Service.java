import java.io.*;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// This class is for handle the common function between client and server: like print log, append to file, covert String to Date, convert Date to String.....
class Service {

    // Append text to file
    static void appendToFile(String filePath, String[] text, int noOfLines, String command) {
        int linecount = countFileLine(filePath);
        File file = new File(filePath);
        FileWriter fr = null;
        BufferedWriter br = null;
        try {
            fr = new FileWriter(file, true);
            br = new BufferedWriter(fr);
            if (command.equals("Download") || command.equals("Writing")) {
                if (linecount != 0) {
                    br.newLine();
                }
                br.write(text[0]);
            } else {
                int i = 0;
                for (String line : text) {
                    if(i == 0){
                        br.write(line);
                    }else{
                        br.newLine();
                        br.write(text[i]);
                    }
                    i++;
                }
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

    // Count how many line in the file
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

    // Convert String to Date
    static Date stringToDate(String stringdate) throws ParseException {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(stringdate);
    }

    // Convert Date to String
    static String dateToString(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
    }

    // Print out the beacon
    static void printBeacon(String[] beacon) {
        System.out.println(beacon[0] + ",");
        System.out.println(beacon[1] + ",");
        System.out.println(beacon[2] + ".");
    }

    // Print out upload file
    static void printOutLog(String line) {
        String[] dataString = line.split(" ");
        System.out.println(dataString[0] + ",");
        System.out.println(dataString[1] + " " + dataString[2] + ",");
        System.out.println(dataString[3] + " " + dataString[4] + ";");
    }
}