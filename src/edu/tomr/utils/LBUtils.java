package edu.tomr.utils;

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

/**
 * Created by muchhals on 3/28/17.
 */
public class LBUtils {

    private static List<String> nodes;

    public static List<String> dataNodes(String status) {
        //if(nodes == null) {
            nodes = getDataNodes(status);
        //}
        return nodes;
    }

    public static void updateNodeListCache() {
        //nodes = getDataNodes();
    }

    private static List<String> getDataNodes(String status) {
        List<String> dataNodes = new ArrayList<>();
        int tries = 10;

        while(tries > 0) {
            try {
                URL url = new URL("http://bg-node:8080/data-nodes");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
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
                System.out.println("Data-nodes: " + sb.toString());
                ObjectMapper mapper = new ObjectMapper();
                DataNodeList nodes = mapper.readValue(sb.toString(), DataNodeList.class);
                if (nodes.getIpAddressList(status).size() == 0) {
                    tries--;
                } else {
                    tries = 0;
                    dataNodes = nodes.getIpAddressList(status);
                }

            } catch (IOException e) {
                tries--;
                Constants.globalLog.error("Error while fetching data node list", e);
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Constants.globalLog.error("Interrupted while sleeping for data-node list query");
                e.printStackTrace();
            }
        }
        return dataNodes;
    }

}
