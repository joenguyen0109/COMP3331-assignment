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
			InetAddress ip = InetAddress.getByName("localhost");
			// establish the connection with server port 5056
			Socket s = new Socket(ip, 5056);

			ObjectOutputStream objectOutput = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream objectInput = new ObjectInputStream(s.getInputStream());
			System.out.print("Username: ");
			String[] authData = new String[4];
			authData[0] = "Username";
			authData[1] = scn.nextLine();
			System.out.print("Password: ");
			authData[2] = "Password";
			authData[3] = scn.nextLine();
			MessagesFormats<String[]> ouputMessage = new MessagesFormats<String[]>("auth", authData);
			objectOutput.writeObject(ouputMessage);
			String command = "";
			while (true) {
				@SuppressWarnings("unchecked")
				MessagesFormats<String> inputData = (MessagesFormats<String>) objectInput.readObject();
				if (inputData.getData().equals("Ok")) {
					break;
				} else {

				}
			}

			while (true) {

				command = scn.nextLine(); // next command

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
