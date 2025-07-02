package org.gr_code.minerware.listeners.waiting;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;

import java.util.Objects;

public class PlayerInteract_Waiting implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent playerInteractEvent){
        if(playerInteractEvent.getItem() == null)
            return;
        Player player = playerInteractEvent.getPlayer();
        if(!Utils.isInGame(player.getUniqueId()))
            return;
        if(!playerInteractEvent.getAction().toString().startsWith("RIGHT"))
            return;
        if(ManageHandler.getModernAPI().equalsItemStack(Utils.VOTE, playerInteractEvent.getItem())){
            playerInteractEvent.setCancelled(true);
            player.openInventory(Objects.requireNonNull(
                    ServerManager.getArena(player.getUniqueId())).getVotingSession().getVotingInventory());
            return;
        }
        if(!ManageHandler.getModernAPI().equalsItemStack(Utils.LEAVE_THE_ARENA, playerInteractEvent.getItem()))
            return;
        playerInteractEvent.setCancelled(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                player.chat("/minerware leave");
            }
        }.runTaskLater(MinerPlugin.getInstance(), 5);

    }

}



