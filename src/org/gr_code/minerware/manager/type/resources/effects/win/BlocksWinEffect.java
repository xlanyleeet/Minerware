package org.gr_code.minerware.manager.type.resources.effects.win;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.nms.NMS;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.effects.WinEffect;

import java.util.Objects;

public class BlocksWinEffect extends WinEffect {

    private final Arena arena;

    private int time;

    public BlocksWinEffect(Arena arena, int time) {
        super(arena, time);
        this.arena = arena;
        this.time = time;
    }

    @Override
    public void update() {
        if(time == super.getTime())
            start();
        doTask();
    }

    @Override
    public void start() { }

    @Override
    public void stop() {
        arena.getProperties().restoreSquares();
        for (GamePlayer player : arena.getPlayers())
            ManageHandler.getNMS().sendRestorePackets(Bukkit.getPlayer(player.getUUID()), arena);
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.start(Objects.requireNonNull(XMaterial.SNOWBALL.parseItem())).setDisplayName("Blocks").build();
    }

    public void doTask() {
        NMS nms = ManageHandler.getNMS();
        if(time == 0){
            stop();
            return;
        }
        for (GamePlayer player : arena.getPlayers())
            nms.updateBlocksWinEffect(Bukkit.getPlayer(player.getUUID()));
        time--;
    }

    @Override
    public Type getType() {
        return Type.BLOCKS;
    }
}
