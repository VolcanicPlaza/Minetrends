package com.volcanicplaza.Minetrends;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerEvents implements Listener {
	
	@EventHandler
	public static void onPlayerJoin(PlayerJoinEvent e) {
		Minetrends.playerJoins.put(e.getPlayer().getName(), System.currentTimeMillis());
		
	}

}
