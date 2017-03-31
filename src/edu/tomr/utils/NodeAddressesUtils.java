package edu.tomr.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by muchhals on 3/29/17.
 */
public class NodeAddressesUtils {

    // List of READY nodes only
    public static List<String> getIpAddresses() {

        return LBUtils.dataNodes(Constants.READY);
    }

    public static void sendNodeDetailsToBgnode(String ipAddress, String status) {

        try {

            StringBuilder sb = new StringBuilder("http://bg-node:8080/data-node/");
            sb.append(ipAddress).append("/");
            sb.append(status);

            URL url = new URL(sb.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            Constants.globalLog.error("Error while pushing data node info", e);
            e.printStackTrace();
        } catch (IOException e) {
            Constants.globalLog.error("Error while pushing data node info", e);
            e.printStackTrace();

        }

    }

    /*
     * Add IP address happens at node startup,
     * We need to update the status of the node only
     *
     * This method can be used to update the data-node list in the background node at startup of node
     * and also when the node is added to the ring
     */
    public static void addIpAddress(String ipAddress, String status, boolean updateCache) {

        sendNodeDetailsToBgnode(ipAddress, status);
        if(updateCache) LBUtils.updateNodeListCache();
    }

    /*
     * Do not need a status parameter because we will set the state to initialized again
     */
    public static void removeIpAddress(String ipAddress) {

        sendNodeDetailsToBgnode(ipAddress, Constants.INITIALIZED);
        LBUtils.updateNodeListCache();
    }

    public static String getRandomIpAddress(){

        List<String> ips = getIpAddresses();

        Random rand = new Random();
        int  n = rand.nextInt(ips.size()-1);

        return ips.get(n);
    }

    // Randomly select a predec node
    public static String getPredecessorNode(String ipAddress) {
        return null;
    }

    public static String getSuccesorNode(String ipAddress) {

        String retIpAdd = null;
        List<String> ips = getIpAddresses();

        for(int i =0; i<ips.size(); i++) {
            if(ips.get(i).equalsIgnoreCase(ipAddress)){
                if(i == ips.size()-1)
                    retIpAdd = ips.get(0);
                else
                    retIpAdd = ips.get(i+1);
                break;
            }
        }
        return retIpAdd;
    }
}
