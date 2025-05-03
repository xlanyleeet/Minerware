package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.ServerManager;

import java.util.Objects;

public class ItemSpawn_Games implements Listener {
    @EventHandler
    public void onSpawn(ItemSpawnEvent itemSpawnEvent) {
        String world = Objects.requireNonNull(itemSpawnEvent.getLocation().getWorld()).getName();
        if (ServerManager.getArena(world) == null) return;
        Arena arena = ServerManager.getArena(world);
        assert arena != null;
        if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
        if (arena.getProperties().getCuboid().notInside(itemSpawnEvent.getLocation())) return;
        if (itemSpawnEvent.getEntity().getType() == EntityType.ARROW || itemSpawnEvent.getEntity().getType() == EntityType.FISHING_HOOK) return;
        itemSpawnEvent.setCancelled(true);
    }
}
