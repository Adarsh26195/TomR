package edu.tomr.demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import edu.tomr.node.map.operations.IMapOperation;
import edu.tomr.utils.Constants;

/**
 * Created by muchhals on 3/28/17.
 */
public class KeyWriter implements Runnable {

    private final IMapOperation operation;
    private final String ipAddress;
    private boolean forceFlush;

    public KeyWriter(IMapOperation operation, String ipAddress) {
        this.operation = operation;
        this.ipAddress = ipAddress;
        this.forceFlush = false;
    }

    public void setForceFlush(){ this.forceFlush = true; }

    public void resetForceFlush(){
        this.forceFlush = false;
    }

    @Override
    public void run() {

        while(true){
            try {
                if(this.operation.getKeySet().size() > 0 || forceFlush) {
                    resetForceFlush();
                    sendKeysToEndpoint(this.operation.getKeySet(), this.ipAddress);
                }
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Constants.globalLog.error("Thread sleep for pushing keys interrupted");
                e.printStackTrace();
            }
        }
    }

    private void sendKeysToEndpoint(Set<String> keySet, String ipAddress) {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode keys = mapper.createObjectNode();
        ArrayNode keyArray = keys.putArray("keys");
        keySet.forEach(x -> {
            ObjectNode kvNode = mapper.createObjectNode();
            kvNode.put("key", x);
            kvNode.put("value", this.operation.get(x));
            keyArray.add(kvNode);
        });
        keys.put("ipAddress", ipAddress);

        try {

            StringBuilder sb = new StringBuilder("http://bg-node:8080/data-node/keys");

            URL url = new URL(sb.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(keys.toString());
            wr.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            Constants.globalLog.error("Error while pushing keys", e);
            e.printStackTrace();
        } catch (IOException e) {
            Constants.globalLog.error("Error while pushing keys", e);
            e.printStackTrace();

        }

    }

    public static void main(String[] args) {



        ObjectMapper mapper = new ObjectMapper();
        ObjectNode keys = mapper.createObjectNode();
        ArrayNode keyArray = keys.putArray("keys");

        ObjectNode kvNode = mapper.createObjectNode();
        kvNode.put("key", "asd");
        kvNode.put("value", "asd1");
        keyArray.add(kvNode);

        kvNode = mapper.createObjectNode();
        kvNode.put("key", "asd");
        kvNode.put("value", "asd2");
        keyArray.add(kvNode);

        kvNode = mapper.createObjectNode();
        kvNode.put("key", "asd");
        kvNode.put("value", "asd2");
        keyArray.add(kvNode);

        System.out.println(keys.toString());
    }
}
