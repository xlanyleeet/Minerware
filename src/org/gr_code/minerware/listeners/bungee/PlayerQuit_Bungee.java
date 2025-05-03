package org.gr_code.minerware.listeners.bungee;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.LobbyHelper;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.StatisticManager;

import java.util.UUID;

public class PlayerQuit_Bungee implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent playerQuitEvent) {
        Player player = playerQuitEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!ManageHandler.notBungeeMode()) {
            Arena arena = ServerManager.getArena(uuid);
            if (arena != null)
                arena.removePlayer(uuid, true);
        }
        StatisticManager.remove(uuid);
        LobbyHelper.removePlayer(player);
        playerQuitEvent.setQuitMessage(null);
    }
}
