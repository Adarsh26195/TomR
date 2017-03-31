package edu.tomr.start;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import edu.tomr.utils.ConfigParams;
import edu.tomr.utils.NodeAddressesUtils;
import network.NetworkUtilities;
import network.exception.NetworkException;
import edu.tomr.network.heartbeat.client.ClientBeatController;
import edu.tomr.node.base.Node;
import edu.tomr.utils.Constants;

public class NodeStarter {

	//private static int selfBeatPost = 5010;

	static {
		 ConfigParams.loadProperties();
	}

	private Node dbNode;
	private ClientBeatController beatController;

	public NodeStarter() {
		NetworkUtilities utils = null;
		try {
			utils = new NetworkUtilities();
			initDbNode(utils.getSelfIP());
			System.out.println("IP>>>>>>>>>> "+utils.getSelfIP());
			NodeAddressesUtils.addIpAddress(utils.getSelfIP(), Constants.INITIALIZED, false);
			dbNode.initNetworkModule();
		} catch (NetworkException e) {

			Constants.globalLog.debug("Exception in obtaining IP address");
			e.printStackTrace();
		}
		//beatController = new ClientBeatController(ConfigParams.getProperty("SERVER_IP"),
				//	ConfigParams.getIntProperty("SERVER_PORT_NO"), utils.getSelfIP(), NetworkConstants.C_BEAT_PORT);

	}

	private void initDbNode(String ipAddress) {
		this.dbNode = new Node(ipAddress);
	}

	private void startBeatClient() {
		try {
			beatController.startHeartBeats();
		} catch (InterruptedException e) {

			Constants.globalLog.debug("Exception in client heart beat module");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {


		System.setProperty("java.net.preferIPv4Stack" , "true");
		
		NodeStarter nodeStarter = new NodeStarter();
		//nodeStarter.startBeatClient();

	}

}
