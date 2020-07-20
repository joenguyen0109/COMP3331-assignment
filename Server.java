
import java.io.*;
import java.text.*;
import java.util.HashMap;

import java.net.*;
import java.util.Date;

// Server class 
public class Server {

	static HashMap<String, AuthObject> authData = new HashMap<String, AuthObject>();

	public static void main(String[] args) throws IOException {
		// server is listening on port 5056
		ServerSocket ss = new ServerSocket(5056);
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
	DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
	DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
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
					System.out.println("Client " + this.s + " sends exit...");
					System.out.println("Closing this connection.");
					this.s.close();
					System.out.println("Connection closed");
					break;
				} else {
					switch (received) {
						case "auth" :
							System.out.println(inputData.getData()[0] + " " + inputData.getData()[1]);
							System.out.println(Server.authData.containsKey(inputData.getData()[0]));
							MessagesFormats<String> authResponse = new MessagesFormats<String>("auth",
									checkAuth(inputData.getData()[0], inputData.getData()[1]));
							dos.writeObject(authResponse);
							System.out.println("Sent response");
							// Check file
							break;
						default:
							break;
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
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

	String checkAuth(String phone, String password) {
		if (Server.authData.containsKey(phone)) {
			AuthObject object = Server.authData.get(phone);
			int time = object.get_signTime();
			String returnString = "";
			switch (time) {
				case 3:
					Date date = new Date();
					if (date.getTime() - object.get_date().getTime() < 60000) {
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
