package fr.antidino.vertex.motd.motdAPI.listenners;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import fr.antidino.vertex.motd.motdAPI.MotdAPI;


import fr.antidino.vertex.motd.motdAPI.manager.PlaceholderManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.util.CachedServerIcon;


public class ServerListPingListenner implements Listener {
	protected MotdAPI motdAPI;
	public ServerListPingListenner(MotdAPI motdAPI) {
		this.motdAPI = motdAPI;
	}
	@EventHandler
	public void onServerListEvent(PaperServerListPingEvent event){
		String ip = event.getAddress().getHostAddress();
		boolean isNew = !PlaceholderManager.isIpRegister(ip);
		PlaceholderManager.register(
				ip,
				event.getProtocolVersion(),
				event.getClient().getVirtualHost() != null
						? event.getClient().getVirtualHost().getHostName()
						: "unknown"
		);
		if (isNew){ Bukkit.getLogger().info("[MotdAPI][" + ip + " is now registered]");}



		String country = motdAPI.geoIPManager.resolveCountry(ip);
		String path = motdAPI.iconResolver.resolveIcon(country);
		CachedServerIcon icon = motdAPI.iconResolver.loadIconFromPath(path);

		String motd = motdAPI.motdResolver.resolveMotd(country);
		motd = PlaceholderManager.build(motd, ip);
		event.setMotd(motd);
		event.setServerIcon(icon);
    }
}
