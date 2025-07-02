package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;

import java.util.UUID;

public class BlockBreak_Games implements Listener {
    @EventHandler
    public void onClick(BlockBreakEvent blockBreakEvent) {
        Player player = blockBreakEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
        Arena arena = ServerManager.getArena(uuid);
        assert arena != null;
        if (arena.getStage().equals(Arena.Stage.WAITING)) {
            blockBreakEvent.setCancelled(true);
            return;
        }
        if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
        blockBreakEvent.setCancelled(true);
        if (arena.getProperties().getCuboid().notInside(blockBreakEvent.getBlock().getLocation())) return;
        if (!ManageHandler.getModernAPI().oldVersion())
            blockBreakEvent.setDropItems(false);
        blockBreakEvent.setExpToDrop(0);
        arena.getMicroGame().event(blockBreakEvent);
    }
}


