package de.simonsator.disablefriendlyfire;

import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author simonbrungs
 * @version 1.0.0 29.11.16
 */
public class DFFMain extends JavaPlugin implements Listener {
	private Map<String, Boolean> currentCache = null;
	private long cacheTimer;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		if (getConfig().getBoolean("Cache.Use")) {
			currentCache = new HashMap<>();
			cacheTimer = getConfig().getLong("Cache.TimeInSeconds") * 20;
		}
		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void onPVP(EntityDamageByEntityEvent pEvent) {
		if (pEvent.getDamager() instanceof Player && pEvent.getEntity() instanceof Player) {
			Player damager = (Player) pEvent.getDamager();
			Player damaged = (Player) pEvent.getEntity();
			String firstUUID = String.valueOf(damager.getUniqueId());
			String secondUUID = String.valueOf(damaged.getUniqueId());
			if (secondUUID.compareTo(firstUUID) > 0) {
				String temp = firstUUID;
				firstUUID = secondUUID;
				secondUUID = temp;
			}
			final String cacheIdentifier = firstUUID + secondUUID;
			if (currentCache != null) {
				Boolean isFriend = currentCache.get(cacheIdentifier);
				if (isFriend != null) {
					if (isFriend)
						pEvent.setCancelled(true);
					return;
				}
			}
			PAFPlayer friend1 = PAFPlayerManager.getInstance().getPlayer(damager.getName());
			PAFPlayer friend2 = PAFPlayerManager.getInstance().getPlayer(damaged.getName());
			if (friend1.isAFriendOf(friend2)) {
				pEvent.setCancelled(true);
				if (currentCache != null)
					currentCache.put(cacheIdentifier, true);
				else
					return;
			} else {
				if (currentCache != null)
					currentCache.put(cacheIdentifier, false);
				else
					return;
			}
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				@Override
				public void run() {
					currentCache.remove(cacheIdentifier);
				}
			}, cacheTimer);
		}
	}
}
