package org.gr_code.minerware.listeners.statistic;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.api.arena.IArena;
import org.gr_code.minerware.api.events.PlayerPrepareLeaveArenaEvent;
import org.gr_code.minerware.manager.type.StatisticManager;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.database.cached.Cached;
import org.gr_code.minerware.manager.type.database.cached.CachedMySQL;

import java.util.Objects;
import java.util.UUID;

public class PlayerPrepareLeaveArena_Statistic implements Listener {
    @EventHandler
    public void onPrepareLeave(PlayerPrepareLeaveArenaEvent playerPrepareLeaveArenaEvent){
        UUID uuid = playerPrepareLeaveArenaEvent.getPlayer().getUniqueId();
        int points = playerPrepareLeaveArenaEvent.getArena().getPlayer(uuid).getPoints();
        Cached cached = CachedMySQL.get(uuid);
        if(cached == null)
            return;
        if(playerPrepareLeaveArenaEvent.isFinishedMatch()) {
            Objects.requireNonNull(cached).setGamesPlayed(cached.getGamesPlayed() + 1);
            FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
            IArena iArena = playerPrepareLeaveArenaEvent.getArena();
            Player player = playerPrepareLeaveArenaEvent.getPlayer();
            String lose = iArena.getPlayer(uuid).getPlace() == 1 ? "win." : "lose.";
            String message = fileConfiguration.getString(lose+"message");
            player.sendMessage(Utils.translate(message));
            cached.addExp(fileConfiguration.getInt(lose+"exp"));
        }
        if(points > Objects.requireNonNull(cached).getMaxPoints())
            cached.setMaxPoints(points);
        StatisticManager.update(uuid);
    }
}


