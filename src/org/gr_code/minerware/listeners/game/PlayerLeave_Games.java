package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;

import java.util.Objects;
import java.util.UUID;

public class PlayerLeave_Games implements Listener {
    @EventHandler
    public void onLeave(PlayerQuitEvent playerQuitEvent) {
        Player player = playerQuitEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
        playerQuitEvent.setQuitMessage(null);
        Objects.requireNonNull(ServerManager.getArena(uuid)).removePlayer(uuid, true);
    }
}


