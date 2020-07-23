
import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.security.SecureRandom;
import java.net.*;
import java.util.Date;
import java.text.ParseException;

// Server class 
public class Server {
	// data to keep track of log in process
	static HashMap<String, AuthObject> authData = new HashMap<String, AuthObject>();
	static int second_block = 0;

	public static void main(String[] args) throws IOException {
		// Input user to port and time
		int serverPort = Integer.parseInt(args[0]);
		second_block = Integer.parseInt(args[1]);

		ServerSocket ss = new ServerSocket(serverPort);
		readAuthFile();
		while (true) {
			Socket s = null;
			try {

				s = ss.accept();

				// obtaining input and out streams
				ObjectInputStream objectInput = new ObjectInputStream(s.getInputStream());
				ObjectOutputStream objectOutput = new ObjectOutputStream(s.getOutputStream());


				// create a new thread object and start thread to handle Client
				Thread client = new ClientHandler(s, objectInput, objectOutput);
				client.start();

			} catch (Exception e) {
				s.close();
				e.printStackTrace();
			}
		}
	}

	static void readAuthFile() {
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
	}

}

// ClientHandler class
class ClientHandler extends Thread {
	final ObjectInputStream objectInput;
	final ObjectOutputStream objectOutput;
	final Socket s;

	// Constructor
	public ClientHandler(Socket s, ObjectInputStream objectInput, ObjectOutputStream objectOutput) {
		this.s = s;
		this.objectInput = objectInput;
		this.objectOutput = objectOutput;
	}

	@Override
	public void run() {
		while (true) {
			try {
				// get data from client
				@SuppressWarnings("unchecked")
				MessagesFormats<String[]> inputData = (MessagesFormats<String[]>) objectInput.readObject();
				
				// get command
				String received = inputData.getTitle();

				if (received.equals("Exit")) {
					// Handle log out command
					System.out.println(inputData.getData()[0] + " logout");
					this.s.close();
					break;
				} else {
					switch (received) {
						// Handle sign in command
						case "auth":
							System.out.println(inputData.getData()[0] + " " + inputData.getData()[1]);
							System.out.println(Server.authData.containsKey(inputData.getData()[0]));

							// Sent message to the client
							MessagesFormats<String> authResponse = new MessagesFormats<String>("auth",
									checkAuth(inputData.getData()[0], inputData.getData()[1]));
							objectOutput.writeObject(authResponse);


							break;

						case "Download":
							// Generate ID and send it back to the client
							MessagesFormats<String> downloadResponse = new MessagesFormats<String>("Download",
									generateNewTempID(inputData.getData()));
							objectOutput.writeObject(downloadResponse);

							// Print out the ID
							System.out.println("user: " + inputData.getData()[0]);
							System.out.println("TempID:");
							System.out.println(downloadResponse.getData());
							break;

						case "Upload":
							System.out.println("Received contact log from " + inputData.getData()[0]);

							// Print out all the beacon in upload file
							for(int i = 1; i< inputData.getData().length; i++ ){
								Service.printOutLog(inputData.getData()[i]);
							}

							// Contact check
							System.out.println("Contact log checking");
							contact(inputData.getData());
							break;
						
						case "Beacon":
							// Print out the beacon sent from the client
							System.out.println(inputData.getData()[0] + " " + inputData.getData()[1] + " "
									+ inputData.getData()[2]);
							Service.printBeacon(new String[] { inputData.getData()[3],
									inputData.getData()[4] + " " + inputData.getData()[5],
									inputData.getData()[6] + " " + inputData.getData()[7] });
							break;
						default:
							break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		try {
			// closing resources
			this.objectInput.close();
			this.objectOutput.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Map tempID with upload file
	void contact(String[] data) throws IOException, ParseException {
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
	String generateNewTempID(String[] inputData) {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retrunString;
	}

	// Check Sign in info
	String checkAuth(String phone, String password) {
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
			return "Wrong Phone";
		}
	}

}
