package network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import edu.tomr.utils.NodeAddressesUtils;
import network.exception.NetworkException;
import network.requests.NWRequest;
import network.requests.incoming.NeighborConnectionHandler;
import network.requests.incoming.NodeCentralServerMessageHandler;
import network.requests.incoming.NodeClientRequestHandler;
import network.requests.incoming.StartupMessageHandler;
import network.requests.outgoing.NodeNeighborModule;
import network.responses.ClientResponseWrapper;
import network.responses.NWResponse;
import network.responses.incoming.NetworkResponseHandler;
import network.responses.outgoing.NodeResponseModule;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import edu.tomr.node.base.Node;
import edu.tomr.protocol.AckMessage;
import edu.tomr.protocol.DBMessage;
import edu.tomr.protocol.RedistributionMessage;
import edu.tomr.protocol.StartupMessage;
import edu.tomr.utils.Constants;
//Main network module. An object of this is created on every Node in the cluster
public class NodeNetworkModule {
	
	private static int startupMsgPort=5000;
	private static int neighborServerPort=5001;
	private static int selfServerPort=5001;
	private static int responsePort=5002;
	private static int clientPort=5003;
	
	public NetworkUtilities utils=null;
	private NodeNeighborModule neighborModule=null;
	private NodeResponseModule responseModule=null;
	private final Node mainNodeObject;
	
	private ConcurrentHashMap<String,Socket> clientConnectionList=new ConcurrentHashMap<String,Socket>();
	
	/**
	 * @throws NetworkException
	 */
	public NodeNetworkModule(Node mainNodeObject) throws NetworkException{
		this.utils=new NetworkUtilities();
		this.mainNodeObject=mainNodeObject;
		constructorCommon();
	}
	
	
	/**
	 * @param selfIP-initialize NodeNetworkModule with a particular IP address
	 */
	public NodeNetworkModule(Node mainNodeObject,String selfIP){
		this.utils=new NetworkUtilities(selfIP);
		this.mainNodeObject=mainNodeObject;
		constructorCommon();
	}
	
	
	/**
	 * Gets the startup message.
	 * Sets up thread to listen to incoming connections.
	 * Initializes the neighborModule. This handles all outgoing connections and requests
	 * Starts the thread that starts servicing the outgoing request queue
	 */
	public void initializeNetworkFunctionality(){
		NWRequest startupRequest=getStartUpRequest(startupMsgPort);
		NodeAddressesUtils.addIpAddress(utils.getSelfIP(), Constants.READY, true);
		this.mainNodeObject.handleStartupRequest(startupRequest.getStartupMessage().getNodeList());
		//if this is a dynamic addition:
		if(startupRequest.getStartupMessage().isDynamicAdd())
			this.neighborModule=setupNeighborConnections(startupRequest.getStartupMessage(),mainNodeObject,true);
		//else
		else
			this.neighborModule=setupNeighborConnections(startupRequest.getStartupMessage(),mainNodeObject,false);
		neighborModule.startServicingRequests();
		System.out.println("Node "+utils.getSelfIP()+" started neighbor connection queue");
		//everyone needs to start listening on port 5002 first
		NetworkResponseHandler incomingResponseHandler=null;
		try {
			//if this is a dynamic addition:
			if(startupRequest.getStartupMessage().isDynamicAdd())
				incomingResponseHandler = new NetworkResponseHandler(responsePort,this,mainNodeObject,true);
			//else
			else
				incomingResponseHandler = new NetworkResponseHandler(responsePort,this,mainNodeObject);
		} catch (NetworkException e) {
					
			e.printStackTrace();
		}
		Thread incomingResponseThread=new Thread(incomingResponseHandler);
		incomingResponseThread.start();
		System.out.println("Node "+utils.getSelfIP()+" started listening on 5002");

		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Constants.globalLog.error("Error while sleeping, after listening on port 5002", e);
		}
		this.responseModule=new NodeResponseModule(startupRequest.getStartupMessage().getNeighborList(), responsePort);
		responseModule.startServicingResponses();
		
		//Now for the incoming client Connections
		NodeClientRequestHandler clientHandler=new NodeClientRequestHandler(clientPort,mainNodeObject,clientConnectionList);
		Thread incomingClientThread=new Thread(clientHandler);
		incomingClientThread.start();
		
		//Start listening for Server Messages
		NodeCentralServerMessageHandler serverHandler=new NodeCentralServerMessageHandler(NetworkConstants.C_SERVER_LISTEN_PORT,this.neighborModule,this.responseModule,this.utils,this.mainNodeObject);
		Thread serverHandlerThread=new Thread(serverHandler);
		serverHandlerThread.start();
		
	}
	
	/**
	 * @param msg-The outgoing DBMessage
	 * @param destIP-can't remember what this means
	 */
	public void sendOutgoingRequest(DBMessage msg,String destIP){
		NWRequest request=utils.getNewDBRequest(msg, destIP);
		this.neighborModule.insertOutgoingRequest(request);
	}
	
	public void sendOutgoingRequest(RedistributionMessage msg,String destIP){
		NWRequest request=utils.getNewRedisRequest(msg, destIP);
		this.neighborModule.insertOutgoingRequest(request);
	}
	
	/**
	 * @param request-Directly construct and send a NWRequest object.
	 * 				Currently isn't needed except by the incoming request handler thread 04/12/15
	 */
	public void sendOutgoingRequest(NWRequest request){
		this.neighborModule.insertOutgoingRequest(request);
	}
	
	
	public void sendOutgoingNWResponse(AckMessage message, String destIP){
		
		NWResponse response=new NWResponse(this.utils.getSelfIP(),destIP,message);
		this.responseModule.insertOutgoingNWResponse(response);
		
	}
	
	public void sendOutgoingNWResponse(NWResponse response){
		this.responseModule.insertOutgoingNWResponse(response);
	}
	
	//DUMMY-Waiting for ClientResponse Class
	public void sendOutgoingClientResponse(AckMessage message, String clientIPAddress){
		NWResponse response=new NWResponse(message);
		//Socket clientSocket=clientConnectionList.get(clientIPAddress);
		Socket clientSocket=clientConnectionList.get(message.getRequestIdServiced());
		ClientResponseWrapper clientResponse=new ClientResponseWrapper(response,clientSocket);
		this.responseModule.insertOutgoingClientResponse(clientResponse);
		//remove this entry from the list of clients. The socket has been closed
		clientConnectionList.remove(clientIPAddress);
		/*sendResponse(clientSocket,response);
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Constants.globalLog.error("Couldn't close the Client Socket");
		}*/
		
		
	}
	
	
	
	/********************************Private Methods********************************************************/
	
	private void constructorCommon(){
		//currently nothing
		
	}
	
	
	
	private NWRequest getStartUpRequest(int startUpMsgPort) {
		
		//1. Wait for the startup message
		StartupMessageHandler myStartupHandler=new StartupMessageHandler(startupMsgPort);
		NWRequest startupRequest=null;
		try {
			startupRequest=myStartupHandler.getRequest();
		} catch (NetworkException e) {
			e.printStackTrace();
		}
		try {
			myStartupHandler.shutdown();
		} catch (NetworkException e) {
			e.printStackTrace();
		}
		return startupRequest;
	}

	
	private NodeNeighborModule setupNeighborConnections(StartupMessage startupMessage, Node mainNodeObject,boolean sendInitACK) {
		
		//Use following:
		//NeighborConnection in order to establish a connection with neighbor
		//NeighborConnectionHandler in order to accept and continue receiving requests from a neighbor
		
		NodeNeighborModule neighborModule=null;
		
		boolean connectFirst=startupMessage.isConnectFirst();
		
		NeighborConnectionHandler incomingNeighborConnectionHandler = null;
		
		try {
			incomingNeighborConnectionHandler = new NeighborConnectionHandler(selfServerPort,this,mainNodeObject,sendInitACK);
		} catch (NetworkException e) {
			e.printStackTrace();
		}
		
		
		if(connectFirst){
			Constants.globalLog.debug("I am node "+utils.getSelfIP()+" and I will connect first");
			Constants.globalLog.debug("I will connect to node "+startupMessage.getNeighborList().get(0));
//			try {
//				Thread.sleep(3000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//				Constants.globalLog.error("Error while sleeping, before sending the neighbor-connect msg", e);
//			}
			//first connect
			neighborModule=new NodeNeighborModule(startupMessage.getNeighborList(),neighborServerPort);
			//then listen
			Thread incomingConnectionsThread=new Thread(incomingNeighborConnectionHandler);
			incomingConnectionsThread.start();
		}
		else{
			Constants.globalLog.debug("I am node "+utils.getSelfIP()+" and I will listen first");
			//listen
			Thread incomingConnectionsThread=new Thread(incomingNeighborConnectionHandler);
			incomingConnectionsThread.start();
			//then connect
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Constants.globalLog.error("Error while sleeping, before sending the neighbor-connect msg", e);
			}
			Constants.globalLog.debug("now I will initate connections");
			Constants.globalLog.debug("I will connect to node "+startupMessage.getNeighborList().get(0));
			neighborModule=new NodeNeighborModule(startupMessage.getNeighborList(),neighborServerPort);
			
		}
		
		return neighborModule;
		
	}


}
