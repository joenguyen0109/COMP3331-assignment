import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/* This class is for handle the jobs from client and server: 
like print log, append to file, covert String to Date, convert Date to String, generate new ID,....
*/
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

    //  read credential file
    static HashMap<String, AuthObject> readAuthFile() { 
        HashMap<String, AuthObject> authData = new HashMap<String, AuthObject>();
		try {
			String filename = "credentials.txt";
			BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
			while ((line = reader.readLine()) != null) {
				authData.put(line.split(" ")[0], new AuthObject(line.split(" ")[1]));
			}
            reader.close();
		} catch (Exception e) {
			e.printStackTrace();
        }
        return authData;
	}

    // Check contact after upload file
    static void contactCheck(String[] data) throws IOException, ParseException {

        System.out.println("Received contact log from " + data[0]);
        // Print out all the beacon in upload file
        for(int i = 1; i< data.length; i++ ){
            Service.printOutLog(data[i]);
        }

        System.out.println("Contact log checking");
		String filename = "tempIDs.txt";
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		HashMap<String, ContactObject> map = new HashMap<String, ContactObject>();
		while ((line = reader.readLine()) != null) {
			String[] stringData = line.split(" ");
			map.put(stringData[1],
					new ContactObject(stringData[0], Service.stringToDate(stringData[2] + " " + stringData[3]),
							Service.stringToDate(stringData[4] + " " + stringData[5])));
		}
		for (String d : data) {
			String id = d.split(" ")[0];
			if (map.containsKey(id)) {
				System.out.println(map.get(id).get_phone() + ",");
				System.out.println(Service.dateToString(map.get(id).get_start()) + ",");
				System.out.println(id + ";");
			}
		}
		reader.close();
	}


    //Generate new id and write to the file
	static String generateNewTempID(String[] inputData) {
		String retrunString = "";
		try {
			byte[] byte20 = new byte[20];
			new SecureRandom().nextBytes(byte20);
			String data = "";
			StringBuilder sb = new StringBuilder();
			for (byte b : byte20) {
				sb.append(String.format("%02X", b));
			}

			BigInteger id = new BigInteger(sb.toString(), 16);
			retrunString = id.toString();

			data = inputData[0] + " " + id.toString() + " " + inputData[1] + " " + inputData[2];
            Service.appendToFile("tempIDs.txt", new String[]{data}, 1,"Download");
            
            // Print out the ID
			System.out.println("user: " + inputData[0]);
			System.out.println("TempID:");
            System.out.println(retrunString);
            
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retrunString;
	}

	// Check Sign in info
	static String checkAuth(String phone, String password) {
		if (Server.authData.containsKey(phone)) {
			AuthObject object = Server.authData.get(phone);
			int time = object.get_signTime();
			String returnString = "";
			switch (time) {
				case 3:
					Date date = new Date();
					if (date.getTime() - object.get_date().getTime() < (Server.second_block * 1000)) {
						returnString = "Out";
						break;
					} else {
						Server.authData.get(phone).set_signTime(0);
					}
				default:
					if (object.get_password().equals(password)) {
						Server.authData.get(phone).set_signTime(0);
                        returnString = "Ok";
                        System.out.println(phone + " login successfully");
					} else {
						// wrong password
						if (time == 2) {
							Server.authData.get(phone).set_date();
							returnString = "Wrong Password And Exit";
						} else {
							returnString = "Wrong Password";
						}
						time = object.get_signTime() + 1;
						Server.authData.get(phone).set_signTime(time);
					}
					break;
			}
			return returnString;
		} else {
			return "Phone number doesn't exist";
		}
	}

}