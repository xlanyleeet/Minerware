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

public class FireWinEffect extends WinEffect {

    private final Arena arena;

    private int time;

    public FireWinEffect(Arena arena, int time) {
        super(arena, time);
        this.arena = arena;
        this.time = super.getTime();
    }

    @Override
    public void update() {
        if(time == super.getTime())
            start();
        doTask();
    }

    @Override
    public void start() {
        arena.getProperties().destroySquares();
    }

    @Override
    public void stop() {
        arena.getProperties().restoreSquares();
        for (GamePlayer player : arena.getPlayers()) {
            ManageHandler.getNMS().sendRestorePackets(Bukkit.getPlayer(player.getUUID()), arena);
        }
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.start(Objects.requireNonNull(XMaterial.LAVA_BUCKET.parseItem())).setDisplayName("&6Fire").build();
    }

    public void doTask(){
        if (time == 0) {
            stop();
            return;
        }
        NMS nms = ManageHandler.getNMS();
        for (GamePlayer gamePlayer : arena.getPlayers()) {
            org.bukkit.entity.Player player = Bukkit.getPlayer(gamePlayer.getUUID());
            nms.updateFireWinEffect(player);
        }
        time--;
    }

    @Override
    public Type getType() {
        return Type.FIRE;
    }

}
