package org.gr_code.minerware.listeners.bungee;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.StatisticManager;

import java.util.UUID;

public class PlayerJoin_Bungee implements Listener {
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        UUID uuid = playerJoinEvent.getPlayer().getUniqueId();
        StatisticManager.add(uuid);
        if (ManageHandler.notBungeeMode())
            return;
        Arena arena = ServerManager.getRandomArena(uuid);
        playerJoinEvent.setJoinMessage(null);
        assert arena != null;
        arena.addPlayer(uuid);
    }
}
