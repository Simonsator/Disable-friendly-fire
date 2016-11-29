package de.simonsator.disablefriendlyfire;

import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author simonbrungs
 * @version 1.0.0 29.11.16
 */
public class DFFMain extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void onPVP(EntityDamageByEntityEvent pEvent) {
		if (pEvent.getDamager() instanceof Player && pEvent.getEntity() instanceof Player) {
			PAFPlayer friend1 = PAFPlayerManager.getInstance().getPlayer((pEvent.getDamager()).getName());
			PAFPlayer friend2 = PAFPlayerManager.getInstance().getPlayer(pEvent.getEntity().getName());
			if (friend1.isAFriendOf(friend2)) pEvent.setCancelled(true);
		}
	}
}
