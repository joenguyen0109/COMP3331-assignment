// Java implementation for a client 
// Save file as Client.java 

import java.io.*;
import java.net.*;
import java.util.Scanner;

// Client class 
public class Client {
	public static void main(String[] args) throws IOException {
		try {
			Scanner scn = new Scanner(System.in);
			String command = "";
			boolean auth = false;
			InetAddress ip = InetAddress.getByName("localhost");
			// establish the connection with server port 5056
			Socket s = new Socket(ip, 5056);

			ObjectOutputStream objectOutput = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream objectInput = new ObjectInputStream(s.getInputStream());
			System.out.print("Username: ");
			String[] authData = new String[2];
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
					System.out
							.println("Your account is blocked due to multiple login failures. Please try again later");
					break;
				} else if (inputData.getData().equals("Wrong Password")) {
					System.out.println("Invalid Password. Please try again");
					System.out.print("Password: ");
					authData[1] = scn.nextLine();
					String[] data = new String[]{authData[0],authData[1]};
					outputMessage = new MessagesFormats<String[]>("auth", data);
					objectOutput.writeObject(outputMessage);
				} else if (inputData.getData().equals("Wrong Phone")) {
					System.out.println("Phone number doesn't exist. Please try again");
					System.out.print("Username: ");
					authData[0] = scn.nextLine();
					System.out.print("Password: ");
					authData[1] = scn.nextLine();
					String[] data = new String[]{authData[0],authData[1]};
					outputMessage = new MessagesFormats<String[]>("auth", data);
					objectOutput.writeObject(outputMessage);
				}else if (inputData.getData().equals("Wrong Password And Exit")) {
					System.out.println("Invalid Password. Your account has been blocked. Please try again later");
					break;
				}
			}

			while (true) {
				if (auth) {
					command = scn.nextLine(); // next command
				}else{
					command = "logout";
				}

				if (command.equals("logout")) {
					MessagesFormats<String> exitMessage = new MessagesFormats<String>("Exit", "Exit");
					objectOutput.writeObject(exitMessage);
					System.out.println("Closing this connection : " + s);
					s.close();
					System.out.println("Connection closed");
					break;
				} else {
					switch (command) {
						case "Download_tempID":
							// do something here
							break;
						case "Upload_contact_log":
							// do something here
							break;
						default:
							System.out.println("Error. Invalid command");
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
}
