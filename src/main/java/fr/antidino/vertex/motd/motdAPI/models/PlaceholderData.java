package fr.antidino.vertex.motd.motdAPI.models;

public class PlaceholderData {

    private final int protocol;
    private final String hostname;
    private final String ip;

    public PlaceholderData(
            int protocol,
            String hostname,
            String ip
    ) {
        this.protocol = protocol;
        this.hostname = hostname;
        this.ip = ip;
    }


    public int getProtocol() {
        return protocol;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }
}