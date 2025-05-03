package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;

import java.util.UUID;

public class PlayerInteract_Games implements Listener {
    @EventHandler
    public void onClick(PlayerInteractEvent playerInteractEvent) {
        Player player = playerInteractEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
        Arena arena = ServerManager.getArena(uuid);
        assert arena != null;
        if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
        arena.getMicroGame().event(playerInteractEvent);
    }
}