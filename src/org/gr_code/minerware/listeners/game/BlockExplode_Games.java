package org.gr_code.minerware.listeners.game;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.ServerManager;

import java.util.Objects;

public class BlockExplode_Games implements Listener {
    @EventHandler
    public void onClick(BlockExplodeEvent blockExplodeEvent) {
        Arena arena = ServerManager.getArena(Objects.requireNonNull(blockExplodeEvent.getBlock().getLocation().getWorld()).getName());
        if (arena == null) return;
        if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
        blockExplodeEvent.setCancelled(true);
        if (arena.getProperties().getCuboid().notInside(blockExplodeEvent.getBlock().getLocation())) return;
        arena.getMicroGame().event(blockExplodeEvent);
    }
}

