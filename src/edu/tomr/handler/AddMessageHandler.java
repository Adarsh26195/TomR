package edu.tomr.handler;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.tomr.utils.LBUtils;
import edu.tomr.utils.NodeAddressesUtils;
import network.Connection;
import network.NetworkConstants;
import network.NetworkUtilities;
import network.Topology;
import network.exception.NetworkException;
import network.requests.NWRequest;

import org.codehaus.jackson.map.ObjectMapper;

import edu.tomr.hash.ConsistentHashing;
import edu.tomr.protocol.BreakFormationMessage;
import edu.tomr.protocol.InitRedistributionMessage;
import edu.tomr.protocol.StartupMessage;
import edu.tomr.protocol.UpdateConnMessage;
import edu.tomr.protocol.UpdateRingMessage;
import edu.tomr.utils.Constants;

public class AddMessageHandler implements Runnable {

	private Socket socket;

	public AddMessageHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {

		NetworkUtilities utils = null;
		ObjectMapper mapper = new ObjectMapper();
		NWRequest request = null;
		UpdateConnMessage message = null;
		Scanner scanner;
		try {
			utils = new NetworkUtilities();
			scanner = new Scanner(socket.getInputStream());
			if(scanner.hasNextLine()){
				request = mapper.readValue(scanner.nextLine(), NWRequest.class);
			}
			scanner.close();
			message = request.getupdateConnMessage();
			
			if(message.isAdd()) {
				
				updateConsistentHash(message.getNewNodeIpAddress());
				String predec = NodeAddressesUtils.getRandomIpAddress();
				String newNodeSuccessor = Topology.getNeighbor(predec);
				//Need to send update ring request to all existing nodes
				//originalNodes.remove(predec);
	
				List<String> temp = new ArrayList<String>();
				//temp.add(NodeAddressesUtils.getSuccesorNode(message.getNewNodeIpAddress()));
				temp.add(newNodeSuccessor);
	
				NWRequest newStartUpRequest = utils.getNewStartupRequest(new StartupMessage(true, "New_node", temp, NodeAddressesUtils.getIpAddresses()/*LBUtils.dataNodes(Constants.READY)*/));
				Connection temp_connection=new Connection(message.getNewNodeIpAddress() ,NetworkConstants.C_SERVER_LISTEN_PORT);
				temp_connection.send_request(newStartUpRequest);
				Constants.globalLog.debug("AddMessageHandler: Sending startup request to node: "+message.getNewNodeIpAddress());
	
				//String newNodeSucessor = NodeAddressesUtils.getSuccesorNode(message.getNewNodeIpAddress());
				NWRequest breakFormRequest = utils.getNewBreakFormRequest(new 
						BreakFormationMessage("Break_Form", message.getNewNodeIpAddress(), newNodeSuccessor));
				temp_connection=new Connection(predec , NetworkConstants.C_SERVER_LISTEN_PORT);
				temp_connection.send_request(breakFormRequest);
				Constants.globalLog.debug("AddMessageHandler: Break from request to node: "+predec);
				Topology.removeNodeTopology(predec);
				Topology.addNodeTopology(predec, message.getNewNodeIpAddress());
				Topology.addNodeTopology(message.getNewNodeIpAddress(), newNodeSuccessor);
				
				temp_connection.getnextResponse();
	
			} else {
				
				String nodeToRemove = message.getNewNodeIpAddress();
				
				Connection temp_connection=new Connection(nodeToRemove ,NetworkConstants.C_SERVER_LISTEN_PORT);
				NWRequest newInitRedisRequest = utils.getNewInitRedisRequest(new InitRedistributionMessage(nodeToRemove));
				temp_connection.send_request(newInitRedisRequest);
				
				//Wait for acknowledgement
				temp_connection.getnextResponse();
				
				List<String> originalNodes = NodeAddressesUtils.getIpAddresses();

				String predec = Topology.getOriginalNode(nodeToRemove);
				//String predec = NodeAddressesUtils.getPredecessorNode(nodeToRemove);
				originalNodes.remove(nodeToRemove);


				//String newNodeSucessor = NodeAddressesUtils.getSuccesorNode(message.getNewNodeIpAddress());
				String newNodeSucessor = Topology.getNeighbor(nodeToRemove);
				NWRequest breakFormRequest = utils.getNewBreakFormRequest(new
						BreakFormationMessage("Break_Form", newNodeSucessor, newNodeSucessor));
				temp_connection=new Connection(predec , NetworkConstants.C_SERVER_LISTEN_PORT);
				temp_connection.send_request(breakFormRequest);
				Constants.globalLog.debug("AddMessageHandler: Break from request to node: "+predec);
				
				ConsistentHashing.updateCircle(originalNodes);
				NodeAddressesUtils.removeIpAddress(nodeToRemove);
				Topology.removeNodeTopology(predec);
				Topology.removeNodeTopology(nodeToRemove);
				Topology.addNodeTopology(predec, newNodeSucessor);
			}

			//TODO: Remove the whole try catch for add and remove msgs
			/*try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}*/
			sendUpdateRingMessage(NodeAddressesUtils.getIpAddresses(), message.getNewNodeIpAddress(), message.isAdd());
			
		} catch (IOException e) {

			Constants.globalLog.debug("IOException while adding new node");
			e.printStackTrace();
		} catch (NetworkException e) {

			Constants.globalLog.debug("NwException while adding new node");
			e.printStackTrace();
		}
	}

	private void updateConsistentHash(String newAddress) {

		List<String> ips = NodeAddressesUtils.getIpAddresses();
		ips.add(newAddress);

		ConsistentHashing.updateCircle(ips);
	}

	private void sendUpdateRingMessage(List<String> originalNodes, String newNode, boolean add){

		NetworkUtilities utils=null;
		try {
			utils=new NetworkUtilities();

			for(String ipAddress: originalNodes){

				UpdateRingMessage msg = new UpdateRingMessage(newNode, add);
				NWRequest updateRingRequest = utils.getNewUpdateRingRequest(msg);

				Connection temp_connection=new Connection(ipAddress, NetworkConstants.C_SERVER_LISTEN_PORT);
				temp_connection.send_request(updateRingRequest);
				Constants.globalLog.debug("AddMessageHandler: Sending update ring request to node: "+ipAddress);
			}
		} catch (NetworkException e) {
			e.printStackTrace();
		}
		Constants.globalLog.debug("AddMessageHandler: Sent update ring requests to nodes");

		// Set status of the removed node to INIT again
		if(!add) {
			NodeAddressesUtils.removeIpAddress(newNode);
		}
	}

}
