package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;

import java.util.UUID;

public class BlockPlace_Games implements Listener {
    @EventHandler
    public void onClick(BlockPlaceEvent blockPlaceEvent) {
        Player player = blockPlaceEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
        Arena arena = ServerManager.getArena(uuid);
        assert arena != null;
        if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
        blockPlaceEvent.setCancelled(true);
        if (arena.getProperties().getCuboid().notInside(blockPlaceEvent.getBlock().getLocation())) return;
        arena.getMicroGame().event(blockPlaceEvent);
    }
}