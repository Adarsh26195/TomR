package edu.tomr.network.heartbeat.base;

public final class ConnectionAddress {

	private String ipAddress;
	private int portNumber;
	
	public String getIpAddress() {
		return ipAddress;
	}
	public int getPortNumber() {
		return portNumber;
	}

	public ConnectionAddress() {}
	
	public ConnectionAddress(String ipAddress, int portNumber) {
		this.ipAddress = ipAddress;
		this.portNumber = portNumber;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ConnectionAddress that = (ConnectionAddress) o;

		if (portNumber != that.portNumber) return false;
		return ipAddress != null ? ipAddress.equalsIgnoreCase(that.ipAddress) : that.ipAddress == null;
	}

}
