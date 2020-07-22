// Java implementation for a client 
// Save file as Client.java 

import java.io.*;
import java.net.*;
import java.util.Scanner;

// Client class 
public class Client {
	private static ObjectOutputStream objectOutput;
	private static ObjectInputStream objectInput;
	private static String[] authData = new String[2];
	static boolean exit = false;
	public static void main(String[] args) throws IOException {
		try {
			String serverIP = args[0];
			int serverPort = Integer.parseInt(args[1]);
			int clientPort = Integer.parseInt(args[2]);
			Scanner scn = new Scanner(System.in);
			String command = "";
			boolean auth = false;

			BeaconsHandler beconhandler = new BeaconsHandler(clientPort);
			beconhandler.start();

			InetAddress ip = InetAddress.getByName(serverIP);
			Socket s = new Socket(ip, serverPort);
			objectOutput = new ObjectOutputStream(s.getOutputStream());
			objectInput = new ObjectInputStream(s.getInputStream());
			auth = authenicate(scn);

			while (true) {
				if (auth) {
					command = scn.nextLine(); // next command
				} else {
					command = "logout";
				}

				if (command.equals("logout")) {
					exit = true;
					logout();
					s.close();
					break;
				} else {
					switch (command) {
						case "Download_tempID":
							downloadTempID(new String[] { authData[0] });
							break;
						case "Upload_contact_log":
							uploadFile();
							break;
						default:
							String[] infoClient = command.split(" ");
							if (infoClient[0].equals("Beacon")) {
								InetAddress address = InetAddress.getByName(infoClient[1]);
								DatagramSocket socket = new DatagramSocket();
								String buffer = "Message";
								DatagramPacket beacon = new DatagramPacket(buffer.getBytes(), buffer.length(), address,
										Integer.parseInt(infoClient[2]));
								socket.send(beacon);
								socket.close();
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
			// dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean authenicate(Scanner scn) throws IOException, ClassNotFoundException {
		boolean auth = false;
		System.out.print("Username: ");
		authData[0] = scn.nextLine();
		System.out.print("Password: ");
		authData[1] = scn.nextLine();
		MessagesFormats<String[]> outputMessage = new MessagesFormats<String[]>("auth", authData);
		objectOutput.writeObject(outputMessage);
		while (true) {
			@SuppressWarnings("unchecked")
			MessagesFormats<String> inputData = (MessagesFormats<String>) objectInput.readObject();
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
			} else if (inputData.getData().equals("Wrong Phone")) {
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

	private static void downloadTempID(String[] data) throws IOException, ClassNotFoundException {
		MessagesFormats<String[]> downloadMessage = new MessagesFormats<String[]>("Download", data);
		objectOutput.writeObject(downloadMessage);
		@SuppressWarnings("unchecked")
		MessagesFormats<String> downloadResponse = (MessagesFormats<String>) objectInput.readObject();
		System.out.println("TempID:");
		System.out.println(downloadResponse.getData());
	}

	private static void logout() throws IOException {
		MessagesFormats<String[]> exitMessage = new MessagesFormats<String[]>("Exit", authData);
		objectOutput.writeObject(exitMessage);
	}

	private static void uploadFile() {
		String[] data = new String[Service.countFileLine("your_zID_contactlog.txt")];
		try {
			String filename = "your_zID_contactlog.txt";
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				Service.printOutLog(line);
				data[i] = line;
				i++;
			}
			MessagesFormats<String[]> uploadMessage = new MessagesFormats<String[]>("Upload", data);
			objectOutput.writeObject(uploadMessage);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
