
// Java implementation for a client 
// Save file as Client.java 
import java.util.Date;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Calendar;

// Client class 
public class Client {
	private static ObjectOutputStream objectOutput;
	private static ObjectInputStream objectInput;
	private static String[] authData = new String[2];
	private static String _tempID = "1";
	private static Date _start =  Calendar.getInstance().getTime();
	private static Date _end = Calendar.getInstance().getTime();

	public static void main(String[] args) throws IOException {
		try {
			String serverIP = args[0];
			int serverPort = Integer.parseInt(args[1]);
			int clientPort = Integer.parseInt(args[2]);
			Scanner scn = new Scanner(System.in);
			String command = "";
			boolean auth = false;

			

			InetAddress ip = InetAddress.getByName(serverIP);
			Socket s = new Socket(ip, serverPort);
			
			objectOutput = new ObjectOutputStream(s.getOutputStream());
			objectInput = new ObjectInputStream(s.getInputStream());

			// Change the state to handle the your_zid file
			synchronized(ManageState._state) 
			{ 
				ManageState.setSate("Checking");
			} 
			

			BeaconsHandler beconhandler = new BeaconsHandler(clientPort,objectOutput);
			beconhandler.start();

			auth = authenicate(scn);

			checkExpire checkExpires = new checkExpire(authData[0],objectOutput);
            checkExpires.start();


			while (true) {
				if (auth) {
					// get command
					command = scn.nextLine();
				} else {
					command = "logout";
				}

				if (command.equals("logout")) {
					logout(clientPort);
					s.close();
					break;

				} else {
					switch (command) {
						case "Download_tempID":
							downloadTempID(authData);
							break;

						case "Upload_contact_log":
							synchronized(ManageState._state) 
							{ 
								ManageState.setSate("Upload");
							} 
							break;
						
						default:
							String[] infoClient = command.split(" ");
							if (infoClient[0].equals("Beacon")) {
								String data = command + " " + _tempID + " " + Service.dateToString(_start) + " "
										+ Service.dateToString(_end);
								Service.printBeacon(new String[] { _tempID, Service.dateToString(_start),
										Service.dateToString(_end) });
								sendBeacon(infoClient, data);
							} else {
								System.out.println("Error. Invalid command");
							}
							break;
					}
				}

			}
			// closing resources
			scn.close();
			objectInput.close();
			objectOutput.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Send beacon to other clients
	private static void sendBeacon(String[] infoClient, String buffer)
			throws UnknownHostException, SocketException, IOException {
		InetAddress address = InetAddress.getByName(infoClient[1]);
		DatagramSocket socket = new DatagramSocket();
		DatagramPacket beacon = new DatagramPacket(buffer.getBytes(), buffer.length(), address,
				Integer.parseInt(infoClient[2]));
		socket.send(beacon);
		socket.close();
	}

	// Check authenication
	private static boolean authenicate(Scanner scn) throws IOException, ClassNotFoundException {
		boolean auth = false;

		System.out.print("Username: ");
		authData[0] = scn.nextLine();
		System.out.print("Password: ");
		authData[1] = scn.nextLine();

		// send username, password to the server
		MessagesFormats<String[]> outputMessage = new MessagesFormats<String[]>("auth", authData);
		objectOutput.writeObject(outputMessage);

		while (true) {
			// get the protocol from the server
			@SuppressWarnings("unchecked")
			MessagesFormats<String> inputData = (MessagesFormats<String>) objectInput.readObject();

			// Check its header and handle it
			if (inputData.getData().equals("Ok")) {
				System.out.println("Welcome to the BlueTrace Simulator!");
				auth = true;
				break;

			} else if (inputData.getData().equals("Out")) {
				System.out.println("Your account is blocked due to multiple login failures. Please try again later");
				break;

			} else if (inputData.getData().equals("Wrong Password")) {
				System.out.println("Invalid Password. Please try again");
				System.out.print("Password: ");
				authData[1] = scn.nextLine();
				String[] data = new String[] { authData[0], authData[1] };
				outputMessage = new MessagesFormats<String[]>("auth", data);
				objectOutput.writeObject(outputMessage);

			} else if (inputData.getData().equals("Phone number doesn't exist")) {
				System.out.println("Phone number doesn't exist. Please try again");
				System.out.print("Username: ");
				authData[0] = scn.nextLine();
				System.out.print("Password: ");
				authData[1] = scn.nextLine();
				String[] data = new String[] { authData[0], authData[1] };
				outputMessage = new MessagesFormats<String[]>("auth", data);
				objectOutput.writeObject(outputMessage);

			} else if (inputData.getData().equals("Wrong Password And Exit")) {
				System.out.println("Invalid Password. Your account has been blocked. Please try again later");
				break;
			}
		}
		return auth;
	}

	// get new tempID
	private static void downloadTempID(String[] d) throws IOException, ClassNotFoundException {
		Calendar cal = Calendar.getInstance();
		_start = cal.getTime();
		cal.add(Calendar.MINUTE, 15);
		_end = cal.getTime();

		
		String[] data = new String[] { d[0], Service.dateToString(_start), Service.dateToString(_end) };
		MessagesFormats<String[]> downloadMessage = new MessagesFormats<String[]>("Download", data);
		objectOutput.writeObject(downloadMessage);

		@SuppressWarnings("unchecked")
		MessagesFormats<String> downloadResponse = (MessagesFormats<String>) objectInput.readObject();

		System.out.println("TempID:");
		_tempID = downloadResponse.getData();
		System.out.println(_tempID);
	}

	// logout
	private static void logout(int clientPort) throws IOException {
		String[] data = new String[] { authData[0], authData[1] };

		// send protocol to server
		MessagesFormats<String[]> exitMessage = new MessagesFormats<String[]>("Exit", data);
		objectOutput.writeObject(exitMessage);

		// Send logout command to other threads
		sendBeacon(new String[] { "Beacon", "127.0.0.1", Integer.toString(clientPort) }, "Exit");
		synchronized(ManageState._state) 
        { 
			ManageState.setSate("Exit");
        } 
	}



}
