
import java.io.*;
import java.util.HashMap;
import java.net.*;
import java.text.ParseException;

// Server class 
public class Server {
	// data to keep track of log in process
	static HashMap<String, AuthObject> authData = Service.readAuthFile();
	static int second_block = 0;

	public static void main(String[] args) throws IOException {
		// Input user to port and time
		int serverPort = Integer.parseInt(args[0]);
		second_block = Integer.parseInt(args[1]);

		ServerSocket ss = new ServerSocket(serverPort);
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
				e.printStackTrace();
				s.close();
				ss.close();
			}
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
				String command = inputData.getTitle();

				if (command.equals("Exit")) {
					// Handle log out command
					System.out.println(inputData.getData()[0] + " logout");
					this.s.close();
					break;
					
				} else {
					switch (command) {
						// Handle sign in command
						case "auth":
							// Sent message to the client
							MessagesFormats<String> authResponse = new MessagesFormats<String>("auth",
									Service.checkAuth(inputData.getData()[0], inputData.getData()[1]));
							objectOutput.writeObject(authResponse);
							break;

						case "Download":
							// Generate ID and send it back to the client
							MessagesFormats<String> downloadResponse = new MessagesFormats<String>("Download",
									Service.generateNewTempID(inputData.getData()));
							objectOutput.writeObject(downloadResponse);
							break;

						case "Upload":
							// Contact check
							Service.contact(inputData.getData());
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

	




}
