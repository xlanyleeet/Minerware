package org.gr_code.minerware.listeners.setup;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.commands.subcommand.PluginCommand;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.Utils;

import java.util.UUID;

public class PlayerInteract_Setup implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent playerInteractEvent){
        UUID uuid = playerInteractEvent.getPlayer().getUniqueId();
        if(!Utils.isInSession(uuid))
            return;
        if(playerInteractEvent.getItem() == null)
            return;
        if(ManageHandler.getNMS().equalsItemStack(playerInteractEvent.getItem(), Utils.CLOSED_MENU)){
            Properties properties = PluginCommand.getArenaHashMap().get(uuid);
            if(!properties.closed || !playerInteractEvent.getAction().toString().startsWith("RIGHT"))
                return;
            properties.setTask("OPENED DEFAULT");
            playerInteractEvent.getPlayer().openInventory(properties.getEditGUI());
            properties.closed = false;
        }
    }
}
