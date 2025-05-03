package org.gr_code.minerware.listeners.waiting;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.Utils;

public class PlayerDropItem_Waiting implements Listener {

    @EventHandler
    public void onDrop(PlayerDropItemEvent playerDropItemEvent){
    	if(!Utils.isInGame(playerDropItemEvent.getPlayer().getUniqueId()))
    	    return;
        if(!(ManageHandler.getNMS().equalsItemStack(playerDropItemEvent.getItemDrop().getItemStack(), Utils.LEAVE_THE_ARENA)
        || ManageHandler.getNMS().equalsItemStack(playerDropItemEvent.getItemDrop().getItemStack(), Utils.VOTE)))
            return;
        playerDropItemEvent.setCancelled(true);
    }
}
