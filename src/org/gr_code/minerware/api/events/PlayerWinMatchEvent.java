package org.gr_code.minerware.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.gr_code.minerware.api.arena.IArena;

public class PlayerWinMatchEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    private Player player;

    private IArena arena;

    public Player getPlayer() {
        return player;
    }

    public IArena getArena() {
        return arena;
    }

    protected void setPlayer(Player player) {
        this.player = player;
    }

    protected void setArena(org.gr_code.minerware.arena.Arena arena) {
        this.arena = arena;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList(){
        return handlerList;
    }

}
