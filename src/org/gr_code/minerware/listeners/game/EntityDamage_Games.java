package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class EntityDamage_Games implements Listener {
    @EventHandler
    public void onDamage(EntityDamageEvent entityDamageEvent) {
    	if (!(entityDamageEvent.getEntity() instanceof Player)) {
	    	String world = requireNonNull(entityDamageEvent.getEntity().getLocation().getWorld()).getName();
			if (ServerManager.getArena(world) == null) return;
	    	Arena arena = requireNonNull(ServerManager.getArena(world));
			if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
	    	entityDamageEvent.setCancelled(true);
			if (entityDamageEvent instanceof EntityDamageByEntityEvent)
				entityByEntity((EntityDamageByEntityEvent) entityDamageEvent);
	    	return;
    	}
        Player player = (Player) entityDamageEvent.getEntity();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
		Arena arena = ServerManager.getArena(uuid);
		assert arena != null;
		if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
		if (entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.CUSTOM) return;
		entityDamageEvent.setCancelled(true);
		if (entityDamageEvent instanceof EntityDamageByEntityEvent)
			playerByEntity((EntityDamageByEntityEvent) entityDamageEvent);
		else arena.getMicroGame().event(entityDamageEvent);
    }

    private void entityByEntity(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player)) return;
		Player player = (Player) event.getDamager();
		UUID uuid = player.getUniqueId();
		if (!Utils.isInGame(uuid)) return;
		Arena arena = ServerManager.getArena(uuid);
		assert arena != null;
		if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
		arena.getMicroGame().event(event);
    }

	private void playerByEntity(EntityDamageByEntityEvent event) {
		Player player = (Player) event.getEntity();
		UUID uuid = player.getUniqueId();
		Arena arena = ServerManager.getArena(uuid);
		if (event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			UUID uuidD = damager.getUniqueId();
			if (!Utils.isInGame(uuidD)) return;
			assert arena != null;
			assert arena.getMicroGame() != null;
			arena.getMicroGame().event(event);
			return;
		}
		assert arena != null;
		assert arena.getMicroGame() != null;
		arena.getMicroGame().event(event);
	}
}
