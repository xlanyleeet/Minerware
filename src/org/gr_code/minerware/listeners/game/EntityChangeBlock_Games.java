package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.ServerManager;

import java.util.Objects;

public class EntityChangeBlock_Games implements Listener {
    @EventHandler
    public void onClick(EntityChangeBlockEvent entityChangeBlockEvent) {
        if (entityChangeBlockEvent.getEntityType() != EntityType.FALLING_BLOCK) return;
        Arena arena = ServerManager.getArena(Objects.requireNonNull(entityChangeBlockEvent.getEntity().getLocation().getWorld()).getName());
        if (arena == null) return;
        if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
        entityChangeBlockEvent.setCancelled(true);
        if (arena.getProperties().getCuboid().notInside(entityChangeBlockEvent.getEntity().getLocation())) return;
        arena.getMicroGame().event(entityChangeBlockEvent);
    }
}