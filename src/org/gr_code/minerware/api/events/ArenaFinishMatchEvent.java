package org.gr_code.minerware.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.gr_code.minerware.api.arena.IArena;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.resources.effects.WinEffect;

public class ArenaFinishMatchEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    private WinEffect.Type winEffect;

    private IArena arena;

    public IArena getArena() {
        return arena;
    }

    protected void setArena(Arena arena) {
        this.arena = arena;
        this.winEffect = arena.getCurrentWinEffect();
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList(){
        return handlerList;
    }

    public WinEffect.Type getWinEffect() {
        return winEffect;
    }

}


