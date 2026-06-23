package fr.antidino.vertex.motd.motdAPI;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.net.InetAddress;

public class ServerListEvent implements Listener {
	@EventHandler
	public void onServerListEvent(ServerListPingEvent event){
        InetAddress Ip = event.getAddress();
        Ip.get
    }
}
