package edu.tomr.loadbalancer;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

import edu.tomr.utils.LBUtils;
import edu.tomr.utils.NodeAddressesUtils;
import network.Connection;
import network.responses.NWResponse;
import edu.tomr.protocol.ClientServiceMessage;
import edu.tomr.utils.ConfigParams;
import edu.tomr.utils.Constants;

public class ClientConnectionHandler implements Runnable {
	private Socket clientSocket;
	private UUID clientUID;
	private static volatile Integer turnOf = 0;
	
	static{
		//ConfigParams.loadProperties();
	}

	public ClientConnectionHandler(Socket clientConnection) {
		this.clientSocket = clientConnection;
		this.clientUID = generateUID();
	}

	

	private static UUID generateUID() {
		return UUID.randomUUID();
		
	}



	@Override
	public void run() {
		String IPAddress = null;
		try{
			IPAddress = getIPAddress();
		}
		catch(NullPointerException e){
			Constants.globalLog.debug("NULL value returned for the IPAddress which is servicing the Client");
			e.printStackTrace();
		}
		
		ClientServiceMessage serviceMessage = new ClientServiceMessage(IPAddress,UUID.randomUUID().toString());
		NWResponse clientResponse=new NWResponse(serviceMessage);
		//Send this request to the client. 
		Connection clientServiceConnection = new Connection(clientSocket);
		clientServiceConnection.send_response(clientResponse);
		//close client socket
		try {
			clientSocket.close();
		} catch (IOException e) {
			Constants.globalLog.debug("Erro while trying to close the socket");
			e.printStackTrace();
		}
		//Exit Thread
		
	}



	private  String getIPAddress() {
		String IPAddress = null;
		synchronized(turnOf){
			try{
				List<String> dataNodes = NodeAddressesUtils.getIpAddresses();
			if(turnOf > dataNodes.size() - 1){
				turnOf = 0;
			}		
			IPAddress = dataNodes.get(turnOf);
			turnOf++;
			}
			catch(Exception e){
				Constants.globalLog.debug("Error while trying to access the IP Addresses for scheduling");
				e.printStackTrace();
				
			}
		}
		return IPAddress;
		
		}
	

}
