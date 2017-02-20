package edu.tomr.loadbalancer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

import edu.tomr.model.DataNodeList;
import edu.tomr.utils.Constants;
import network.Connection;
import network.NetworkConstants;
import network.NetworkUtilities;
import network.exception.NetworkException;
import network.requests.NWRequest;
import network.requests.incoming.LBClientServer;
import edu.tomr.handler.AddMessageHandler;
import edu.tomr.handler.PortRequestHandler;
import edu.tomr.network.heartbeat.server.ServerBeatController;
import edu.tomr.protocol.StartupMessage;
import edu.tomr.utils.ConfigParams;

public class LoadBalancer {

	static {
		 ConfigParams.loadProperties();
	}

	static int startupMsgPort=5000;
	
	static int sizeOfThreadPool=20;
	private ServerBeatController beatController;

	public LoadBalancer() {
		beatController = new ServerBeatController();
	}

	public void startHeartBeatServer() {
		beatController.start();
	}

	public void startServer() {

		NetworkUtilities utils = null;

		try {
			utils = new NetworkUtilities();
		} catch (NetworkException e) {
			e.printStackTrace();
		}

		List<String> nodeAddresses = getDataNodes();//ConfigParams.getIpAddresses();

		//make the last one connectFirst
		int i;
		for(i=0;i<nodeAddresses.size()-1;i++){

			List<String> neighbors=generateNeighborList(i,nodeAddresses);
			StartupMessage msg=new StartupMessage("Testing123", neighbors, nodeAddresses);
			NWRequest startupRequest=utils.getNewStartupRequest(msg);

			Connection temp_connection=new Connection(nodeAddresses.get(i),startupMsgPort);

			temp_connection.send_request(startupRequest);

		}

		List<String> neighbors = generateNeighborList(i,nodeAddresses);
		StartupMessage msg = new StartupMessage("Testing123", neighbors, nodeAddresses, true);
		NWRequest startupRequest = utils.getNewStartupRequest(msg);

		Connection temp_connection=new Connection(nodeAddresses.get(i),startupMsgPort);

		temp_connection.send_request(startupRequest);
	}

	private static List<String> generateNeighborList(int i, List<String> iPAddresses) {

		List<String> neighbors=new ArrayList<String>();

		if(i+1>iPAddresses.size()-1){
			neighbors.add(iPAddresses.get(0));
		}
		else{
			neighbors.add(iPAddresses.get(i+1));
		}

		return neighbors;
	}

	private void listenForClients() {
		LBClientServer IPServer = new LBClientServer();
				
	}

	private void startServicingPorts() {

		//Start servicing add message port LB_ADD_LISTEN_PORT
		PortRequestHandler<AddMessageHandler> addMsgHandler = new PortRequestHandler<AddMessageHandler>(NetworkConstants.LB_ADD_LISTEN_PORT,
				AddMessageHandler.class.getName());
		Thread addMsgThread = new Thread(addMsgHandler);
		addMsgThread.start();
		
	}

	private List<String> getDataNodes() {

		try {

			URL url = new URL("http://bg-node:8080/data-nodes");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			conn.disconnect();

			System.out.println("Data-nodes: "+sb.toString());
			ObjectMapper mapper = new ObjectMapper();
			DataNodeList nodes = mapper.readValue(sb.toString(), DataNodeList.class);
			return nodes.getIpAddressList();

		} catch (MalformedURLException e) {
			Constants.globalLog.error("Error while fetching data node list", e);
			e.printStackTrace();
		} catch (IOException e) {
			Constants.globalLog.error("Error while fetching data node list", e);
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
	
		LoadBalancer loadBalancer = new LoadBalancer();
		//loadBalancer.startHeartBeatServer();
		loadBalancer.startServer();
		loadBalancer.listenForClients();
		loadBalancer.startServicingPorts();
	}


}
