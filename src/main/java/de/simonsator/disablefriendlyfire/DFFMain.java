package de.simonsator.disablefriendlyfire;

import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author simonbrungs
 * @version 1.0.0 29.11.16
 */
public class DFFMain extends JavaPlugin implements Listener {
	private Map<String, Boolean> currentCache = null;
	private long cacheTimer;
	private List<String> enabledWorlds = null;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		if (getConfig().getBoolean("Cache.Use")) {
			currentCache = new HashMap<>();
			cacheTimer = getConfig().getLong("Cache.TimeInSeconds") * 20;
			if (getConfig().getBoolean("PerWorld.Enabled"))
				enabledWorlds = getConfig().getStringList("PerWorld.EnabledWorlds");
		}
		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void onPVP(EntityDamageByEntityEvent pEvent) {
		Player damaged = null;
		Player damager = null;
		if (pEvent.getDamager() instanceof Player)
			damager = (Player) pEvent.getDamager();
		else if (pEvent.getDamager() instanceof Projectile) {
			ProjectileSource shooter = ((Projectile) pEvent.getDamager()).getShooter();
			if (shooter instanceof Player) {
				damager = (Player) (((Projectile) pEvent.getDamager()).getShooter());
			}
		}
		if (pEvent.getEntity() instanceof Player)
			damaged = (Player) pEvent.getEntity();
		if (damaged != null && damager != null) {
			if (enabledWorlds != null) {
				if (!enabledWorlds.contains(Objects.requireNonNull(damager.getLocation().getWorld()).getName()))
					return;
			}
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
			Bukkit.getScheduler().runTaskLater(this, () -> currentCache.remove(cacheIdentifier), cacheTimer);
		}
	}
}
