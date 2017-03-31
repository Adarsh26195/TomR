package edu.tomr.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import network.NetworkConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {

	public static final Logger globalLog = LoggerFactory.getLogger(NetworkConstants.class);

	/*
	 * Node statuses
	 */
	public static final String INITIALIZED = "Initialized";

	public static final String READY = "Ready";

}
