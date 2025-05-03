package org.gr_code.minerware.listeners.game;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.ServerManager;

import java.util.Objects;

public class EntityExplode_Games implements Listener {
    @EventHandler
    public void onClick(EntityExplodeEvent entityExplodeEvent) {
        Arena arena = ServerManager.getArena(Objects.requireNonNull(entityExplodeEvent.getLocation().getWorld()).getName());
        if (arena == null) return;
        if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
        entityExplodeEvent.setCancelled(true);
        if (arena.getProperties().getCuboid().notInside(entityExplodeEvent.getLocation())) return;
        arena.getMicroGame().event(entityExplodeEvent);
    }
}