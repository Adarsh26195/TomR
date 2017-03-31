package network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import network.requests.NWRequest;
import network.responses.NWResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import edu.tomr.utils.Constants;

//handles connections to a particular server on a port
public class Connection {
	
	protected Socket socket;

	public Connection(String IP_Address, int port_num) {
		int tries = 5;
		Constants.globalLog.debug("IP: "+IP_Address+" port: "+port_num);
		while (tries > 0) {
			try {
				socket = new Socket(IP_Address, port_num);
				Constants.globalLog.debug("Connection with IP: " + IP_Address + " was successful");
				tries = 0;
			} catch (UnknownHostException e) {
				tries = handleRetry(e, tries, "Unknown Host:" + IP_Address);
			} catch (IOException e) {
				tries = handleRetry(e, tries, "Some kind of IO Exception at Host:" + IP_Address);
			}
		}
	}

	private int handleRetry(Exception e, int count, String message) {
		Constants.globalLog.error(message, e);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		Constants.globalLog.debug("Retrying connection");
		return count--;
	}
	
	public Connection(Socket clientSocket){
		try {
			socket = clientSocket;
		} catch (Exception e) {
			Constants.globalLog.debug("Some kind of IO Exception: ");
			e.printStackTrace();
		}
	}
	
	public void closeSocket(){
		
		try {
			this.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send_request(NWRequest request){
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		DataOutputStream output_stream=null;
		try {
			output_stream= new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			Constants.globalLog.debug("Unable to open output stream to host:"+socket.getInetAddress());
			e.printStackTrace();
			return;
		}
		
		try {
			//mapper.writeValue(System.out, request);
			mapper.writeValue(output_stream, request);
			//end of message marker.
			output_stream.writeChar('\n');
			output_stream.flush();
		} catch (JsonGenerationException e) {
			Constants.globalLog.debug("Problem Generating JSON");
			e.printStackTrace();
		} catch (JsonMappingException e) {
			Constants.globalLog.debug("Problem with JSON mapping");
			e.printStackTrace();
		} catch (IOException e) {
			Constants.globalLog.debug("Problem with IO with host:"+socket.getInetAddress());
			e.printStackTrace();
		}
		
	}
	
	public void send_response(NWResponse response) {
		// TODO Auto-generated method stub
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		DataOutputStream output_stream=null;
		try {
			output_stream= new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			Constants.globalLog.debug("Unable to open output stream to host:"+socket.getInetAddress());
			e.printStackTrace();
			return;
		}
		
		try {
			//mapper.writeValue(System.out, request);
			mapper.writeValue(output_stream, response);
			//end of message marker.
			output_stream.writeChar('\n');
			output_stream.flush();
		} catch (JsonGenerationException e) {
			Constants.globalLog.debug("Problem Generating JSON");
			e.printStackTrace();
		} catch (JsonMappingException e) {
			Constants.globalLog.debug("Problem with JSON mapping");
			e.printStackTrace();
		} catch (IOException e) {
			Constants.globalLog.debug("Problem with IO with host:"+socket.getInetAddress());
			e.printStackTrace();
		}
	}
	
	public NWResponse getnextResponse(){
		ObjectMapper mapper = new ObjectMapper();
		NWResponse response=null;
		Scanner inputScanner=null;
		try {
			inputScanner = new Scanner(socket.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//currently using scanner. Scanner waits for a newLine character which marks the end of an object
		while(!inputScanner.hasNextLine());
		try {
			response=mapper.readValue(inputScanner.nextLine(), NWResponse.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return response;
	}

}
