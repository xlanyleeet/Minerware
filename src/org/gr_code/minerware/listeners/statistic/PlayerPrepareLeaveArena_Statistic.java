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
    public void onPrepareLeave(PlayerPrepareLeaveArenaEvent playerPrepareLeaveArenaEvent) {
        UUID uuid = playerPrepareLeaveArenaEvent.getPlayer().getUniqueId();
        int points = playerPrepareLeaveArenaEvent.getArena().getPlayer(uuid).getPoints();
        Cached cached = CachedMySQL.get(uuid);
        if (cached == null)
            return;
        if (playerPrepareLeaveArenaEvent.isFinishedMatch()) {
            Objects.requireNonNull(cached).setGamesPlayed(cached.getGamesPlayed() + 1);
            FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
            IArena iArena = playerPrepareLeaveArenaEvent.getArena();
            Player player = playerPrepareLeaveArenaEvent.getPlayer();

            // Визначаємо місце гравця
            int place = iArena.getPlayer(uuid).getPlace();
            String rewardKey;

            // Вибираємо відповідний ключ винагороди на основі місця
            switch (place) {
                case 1:
                    rewardKey = "rewards.1st-place.";
                    cached.setWins(cached.getWins() + 1); // Збільшуємо кількість перемог тільки для 1-го місця
                    break;
                case 2:
                    rewardKey = "rewards.2nd-place.";
                    break;
                case 3:
                    rewardKey = "rewards.3rd-place.";
                    break;
                default:
                    rewardKey = "rewards.participation.";
                    break;
            }

            // Відправляємо повідомлення та додаємо досвід
            String message = fileConfiguration.getString(rewardKey + "message");
            if (message != null) {
                player.sendMessage(Utils.translate(message));
            }

            int expReward = fileConfiguration.getInt(rewardKey + "exp", 0);
            if (expReward > 0) {
                cached.addExp(expReward);
            }
        }
        if (points > Objects.requireNonNull(cached).getMaxPoints())
            cached.setMaxPoints(points);
        StatisticManager.update(uuid);
    }
}
