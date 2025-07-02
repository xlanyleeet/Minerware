package org.gr_code.minerware.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.gr_code.minerware.api.arena.IArena;
import org.gr_code.minerware.arena.Arena;

import java.util.List;

public class PlayerStackedAllBlocksEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList(){
        return handlerList;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public PlayerStackedAllBlocksEvent(Player player, Arena arena, Block block, List<Block> listBlocks) {
        this.arena = arena;
        this.player = player;
        this.block = block;
        this.listBlocks = listBlocks;
    }

    private final Player player;

    private final IArena arena;

    private final Block block;

    private final List<Block> listBlocks;

    public Player getPlayer() {
        return player;
    }

    public IArena getArena() {
        return arena;
    }

    public Block getBlock() {
        return block;
    }

    public List<Block> getStackedBlocks() {
        return listBlocks;
    }

}


