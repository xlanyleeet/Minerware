package org.gr_code.minerware.listeners.setup;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.commands.subcommand.PluginCommand;
import org.gr_code.minerware.manager.type.Utils;

import java.util.UUID;

public class InventoryClose_Setup implements Listener {
    @EventHandler
    public void onClose(InventoryCloseEvent inventoryCloseEvent){
        UUID uuid = inventoryCloseEvent.getPlayer().getUniqueId();
        if(!Utils.isInSession(uuid))
            return;
        Properties properties = PluginCommand.getArenaHashMap().get(uuid);
        properties.closed = true;
    }
}
