package org.gr_code.minerware.manager.type;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.manager.ManageHandler;

import java.util.*;

import static org.gr_code.minerware.manager.type.Utils.*;

public class LobbyHelper {

    private static final FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();

    private static final int LIMIT = ManageHandler.getNMS().isLegacy() ? 16 : 64;

    private static final Set<LobbyAssist> tracks = new HashSet<>();

    private static boolean enabled;

    private static List<String> lines;

    private static String title;

    private static Thread thread;

    public static void load() {
        ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection("lobby-scoreboard");
        assert configurationSection != null;
        enabled = configurationSection.getBoolean("enabled");
        lines = configurationSection.getStringList("list");
        title = Utils.translate(configurationSection.getString("title"));
        thread = new Thread() {
            @Override
            public void run() {
                while (MinerPlugin.getInstance().isEnabled()) {
                    tracks.forEach(LobbyAssist::doBoardTask);
                    try {
                        //noinspection BusyWait
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                interrupt();
            }
        };
        thread.start();
    }

    public static void addPlayer(Player player) {
        if(!enabled)
            return;
        LobbyAssist lobbyAssist = new LobbyAssist(player);
        lobbyAssist.startDisplaying();
        tracks.add(lobbyAssist);
    }

    public static void removePlayer(Player player) {
        if(!enabled)
            return;
        UUID uuid = player.getUniqueId();
        LobbyAssist lobbyHelper = tracks.stream()
                .filter(p -> p.getPlayer().getUniqueId().equals(uuid))
                .findFirst().orElse(null);
        if(lobbyHelper == null)
            return;
        lobbyHelper.resetScoreBoard();
        tracks.remove(lobbyHelper);
    }

    public static void clear() {
        if(!enabled)
            return;
        tracks.forEach(LobbyAssist::resetScoreBoard);
        tracks.clear();
        thread.interrupt();
    }

    private static class LobbyAssist {

        private final Player player;

        private Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

        private final Objective objective = ManageHandler.getNMS().isLegacy() ?
                scoreboard.registerNewObjective("MinerwareL", "dummy") :
                scoreboard.registerNewObjective("MinerwareL", "dummy", translate(title));

        public LobbyAssist(Player player) {
            this.player = player;
            objective.setDisplayName(translate(title));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        private void doBoardTask() {
            if (player.getScoreboard().getObjective("MinerwareL") == null)
                startDisplaying();
            List<String> strings = lines;
            int size = strings.size();
            if (scoreboard.getTeams().size() != strings.size())
                resetScores();
            for (String string : strings) {
                String s = size + ":" + player.getUniqueId().toString().substring(0, 10);
                Team team = getTeam(s, size);
                String task = translate(Utils.request(string, player));
                updateTeam(team, task);
                updateScore(ChatColor.values()[size].toString() + ChatColor.RESET.toString(), size);
                size--;
            }
        }

        private Team getTeam(String string, int size){
            Team team = scoreboard.getTeam(string);
            if (team == null) {
                team = scoreboard.registerNewTeam(string);
                team.addEntry(ChatColor.values()[size] + ChatColor.RESET.toString());
            }
            return team;
        }

        public void resetScoreBoard() {
            for (Team team : scoreboard.getTeams())
                team.unregister();
            if(scoreboard.getObjective("MinerwareL") != null)
                objective.unregister();
            scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
            player.setScoreboard(scoreboard);
        }

        private void resetScores(){
            scoreboard.getEntries().forEach(scoreboard :: resetScores);
            for (Team team : scoreboard.getTeams())
                team.unregister();
        }

        public void update() {
            doBoardTask();
        }

        private void updateTeam(Team team, String scoreboardText) {
            String prefix = team.getPrefix();
            String suffix;
            if (scoreboardText.length() > LIMIT) {
                prefix = scoreboardText.substring(0, LIMIT);
                suffix = scoreboardText.substring(LIMIT);
                String colors = ChatColor.getLastColors(prefix);
                if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
                    prefix = prefix.substring(0, prefix.length() - 1);
                    ChatColor chatColor = ChatColor.getByChar(suffix.charAt(0));
                    colors = chatColor == null ? "" : chatColor.toString();
                    suffix = suffix.substring(colors.length());
                }
                suffix = colors + suffix;
                suffix = suffix.length() > LIMIT ? suffix.substring(0, LIMIT) : suffix;
                team.setSuffix(suffix);
                team.setPrefix(prefix);
                return;
            }
            if(!prefix.equals(scoreboardText))
                team.setPrefix(scoreboardText);
            team.setSuffix("");
        }

        private void updateScore(String entry, int size){
            if(objective.getScore(entry).isScoreSet())
                return;
            objective.getScore(entry).setScore(size);
        }

        public void startDisplaying() {
            player.setScoreboard(scoreboard);
        }

        public Player getPlayer() {
            return player;
        }

    }

}
