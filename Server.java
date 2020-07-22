
import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.security.SecureRandom;
import java.net.*;
import java.util.Date;
import java.text.ParseException;

// Server class 
public class Server {

	static HashMap<String, AuthObject> authData = new HashMap<String, AuthObject>();
	static int second_block = 0;

	public static void main(String[] args) throws IOException {
		// server is listening on port 5056
		int serverPort = Integer.parseInt(args[0]);
		second_block = Integer.parseInt(args[1]);
		ServerSocket ss = new ServerSocket(serverPort);
		readAuthFile();
		// running infinite loop for getting
		// client request
		while (true) {
			Socket s = null;

			try {
				// socket object to receive incoming client requests
				s = ss.accept();

				System.out.println("A new client is connected : " + s);

				// obtaining input and out streams
				ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
				ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());

				System.out.println("Assigning new thread for this client");

				// create a new thread object
				Thread t = new ClientHandler(s, dis, dos);

				// Invoking the start() method
				t.start();
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
	final ObjectInputStream dis;
	final ObjectOutputStream dos;
	final Socket s;

	// Constructor
	public ClientHandler(Socket s, ObjectInputStream dis, ObjectOutputStream dos) {
		this.s = s;
		this.dis = dis;
		this.dos = dos;
	}

	@Override
	public void run() {
		String received;
		// String toreturn;
		while (true) {
			try {
				@SuppressWarnings("unchecked")
				MessagesFormats<String[]> inputData = (MessagesFormats<String[]>) dis.readObject();
				// receive the answer from client
				received = inputData.getTitle();
				System.out.println(received);
				if (received.equals("Exit")) {
					System.out.println(inputData.getData()[0] + " logout");
					this.s.close();
					break;
				} else {
					switch (received) {
						case "auth":
							System.out.println(inputData.getData()[0] + " " + inputData.getData()[1]);
							System.out.println(Server.authData.containsKey(inputData.getData()[0]));
							MessagesFormats<String> authResponse = new MessagesFormats<String>("auth",
									checkAuth(inputData.getData()[0], inputData.getData()[1]));
							dos.writeObject(authResponse);
							System.out.println("Sent response");
							// Check file
							break;
						case "Download":
							MessagesFormats<String> downloadResponse = new MessagesFormats<String>("Download",
									generateNewTempID(inputData.getData()));

							dos.writeObject(downloadResponse);
							System.out.println("user: " + inputData.getData()[0]);
							System.out.println("TempID:");
							System.out.println(downloadResponse.getData());
							break;

						case "Upload":
							System.out.println("Received contact log from " + inputData.getData()[0]);
							for(int i = 1; i< inputData.getData().length; i++ ){
								Service.printOutLog(inputData.getData()[i]);
								//System.out.println(inputData.getData()[i]);
							}
							System.out.println("Contact log checking");
							contact(inputData.getData());
							break;

						case "Beacon":
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
			this.dis.close();
			this.dos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
			Service.appendToFile("tempIDs.txt", new String[]{data}, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retrunString;
	}

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
