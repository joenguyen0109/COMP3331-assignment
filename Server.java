// Java implementation of Server side 
// It contains two classes : Server and ClientHandler 
// Save file as Server.java 

import java.io.*; 
import java.text.*; 
import java.net.*; 

// Server class 
public class Server 
{ 
	public static void main(String[] args) throws IOException 
	{ 
		// server is listening on port 5056 
		ServerSocket ss = new ServerSocket(5056); 
		
		// running infinite loop for getting 
		// client request 
		while (true) 
		{ 
			Socket s = null; 
			
			try
			{ 
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
				
			} 
			catch (Exception e){ 
				s.close(); 
				e.printStackTrace(); 
			} 
		} 
	} 
} 

// ClientHandler class 
class ClientHandler extends Thread 
{ 
	DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd"); 
	DateFormat fortime = new SimpleDateFormat("hh:mm:ss"); 
	final ObjectInputStream dis; 
	final ObjectOutputStream dos; 
	final Socket s; 
	

	// Constructor 
	public ClientHandler(Socket s, ObjectInputStream dis, ObjectOutputStream dos) 
	{ 
		this.s = s; 
		this.dis = dis; 
		this.dos = dos; 
	} 

	@Override
	public void run() 
	{ 
		String received; 
		//String toreturn; 
		while (true) 
		{ 
			try { 
				@SuppressWarnings("unchecked")
				MessagesFormats<String[]> inputData = (MessagesFormats<String[]>) dis.readObject();
				// receive the answer from client 
				received = inputData.getTitle();
			
				if(received.equals("Exit")) 
				{ 
					System.out.println("Client " + this.s + " sends exit..."); 
					System.out.println("Closing this connection."); 
					this.s.close(); 
					System.out.println("Connection closed"); 
					break; 
				} 
				
				switch (received) { 
					case "auth":
						System.out.println(inputData.getData()[1] + inputData.getData()[3]);
						MessagesFormats<String> authResponse = new MessagesFormats<String>("auth","Ok");
						dos.writeObject(authResponse);
						System.out.println("Sent response");
						// Check file
						break;
					default: 
						break; 
				} 
			} catch (IOException e) { 
				e.printStackTrace(); 
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} 
		
		try
		{ 
			// closing resources 
			this.dis.close(); 
			this.dos.close(); 
			
		}catch(IOException e){ 
			e.printStackTrace(); 
		} 
	} 
} 



