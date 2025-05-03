package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.ServerManager;

import java.util.Objects;

public class CreatureSpawn_Games implements Listener{
	@EventHandler
	public void onSpawn(CreatureSpawnEvent creatureSpawnEvent) {
		String world = Objects.requireNonNull(creatureSpawnEvent.getLocation().getWorld()).getName();
		if (ServerManager.getArena(world) == null)
			return;
		Arena arena = ServerManager.getArena(world);
		EntityType type = creatureSpawnEvent.getEntity().getType();
		if (type == EntityType.ARROW || type == EntityType.ARMOR_STAND || type == EntityType.FISHING_HOOK)
			return;
		creatureSpawnEvent.setCancelled(true);
		if (creatureSpawnEvent.getSpawnReason() == SpawnReason.BREEDING || creatureSpawnEvent.getSpawnReason() == SpawnReason.NATURAL)
			return;
		assert arena != null;
		if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null))
			return;
		arena.getMicroGame().event(creatureSpawnEvent);
	}
}
