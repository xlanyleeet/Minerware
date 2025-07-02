package org.gr_code.minerware.listeners.game;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.ServerManager;

import java.util.Objects;

public class BlockFade_Games implements Listener {
    @EventHandler
    public void onFade(BlockFadeEvent blockFadeEvent) {
        String world = Objects.requireNonNull(blockFadeEvent.getBlock().getLocation().getWorld()).getName();
        if (ServerManager.getArena(world) == null) return;
        Arena arena = ServerManager.getArena(world);
        assert arena != null;
        if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
        if (arena.getProperties().getCuboid().notInside(blockFadeEvent.getBlock().getLocation())) return;
        blockFadeEvent.setCancelled(true);
    }
}


