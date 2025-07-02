package org.gr_code.minerware.listeners.waiting;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.Voting;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;

import java.util.Objects;

public class PlayerInventoryClick_Waiting implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent inventoryClickEvent) {
        if (!Utils.isInGame(inventoryClickEvent.getWhoClicked().getUniqueId()))
            return;
        Arena arena = ServerManager.getArena(inventoryClickEvent.getWhoClicked().getUniqueId());
        assert arena != null;
        if(arena.isStarted())
            return;
        inventoryClickEvent.setCancelled(true);
        if (inventoryClickEvent.getCurrentItem() == null)
            return;
        Inventory inventory = inventoryClickEvent.getClickedInventory();
        assert inventory != null;
        GamePlayer gamePlayer = arena.getPlayer(inventoryClickEvent.getWhoClicked().getUniqueId());
        if (inventory.getHolder() instanceof Voting.VoteHolder) {
            Voting voting = Objects.requireNonNull(ServerManager.
                    getArena(inventoryClickEvent.getWhoClicked().getUniqueId()))
                    .getVotingSession();
            switch (inventoryClickEvent.getAction()) {
                case MOVE_TO_OTHER_INVENTORY:
                case HOTBAR_MOVE_AND_READD:
                case HOTBAR_SWAP:
                    return;
            }
            voting.onClick(inventoryClickEvent.getSlot(), gamePlayer);
        }
    }
}


