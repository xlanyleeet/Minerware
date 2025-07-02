package org.gr_code.minerware.listeners.bungee;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.ServerManager;

import java.util.UUID;

public class ServerListPing_Bungee implements Listener {

    private static final UUID uuid = UUID.randomUUID();

    @EventHandler
    public void onListPing(ServerListPingEvent serverListPingEvent){
        MinerPlugin minerPlugin = MinerPlugin.getInstance();
        if(minerPlugin == null)
            return;
        if (ManageHandler.notBungeeMode())
            return;
        Arena arena = ServerManager.getRandomArena(uuid);
        if(arena == null)
            return;
        serverListPingEvent.setMotd(arena.getStage().getSignString());
        serverListPingEvent.setMaxPlayers(arena.getProperties().getMaxPlayers());
    }
}


