package org.gr_code.minerware.listeners.bungee;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.commands.subcommand.PluginCommand;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.ServerManager;

import java.util.UUID;

public class PlayerLogin_Bungee implements Listener {
    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent playerLoginEvent){
        if (ManageHandler.notBungeeMode())
            return;
        UUID uuid = playerLoginEvent.getUniqueId();
        Arena arena = ServerManager.getRandomArena(uuid);
        if (arena == null || !arena.canJoin(uuid))
            playerLoginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, PluginCommand.Language.BUNGEE_JOIN.getString());
    }
}


