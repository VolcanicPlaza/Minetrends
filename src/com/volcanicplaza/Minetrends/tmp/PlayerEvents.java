package com.volcanicplaza.Minetrends.tmp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvents implements Listener {

	@EventHandler
	public static void onPlayerJoin(PlayerJoinEvent e) {
		Minetrends.playerJoins.put(e.getPlayer().getName(), System.currentTimeMillis());
	}

	@EventHandler
	public static void onPlayerQuit(PlayerQuitEvent e) {
		Minetrends.playerJoins.remove(e.getPlayer().getName());
	}

}
