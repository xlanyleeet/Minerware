package org.gr_code.minerware.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.api.arena.IPlayer;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.gr_code.minerware.manager.type.Utils.*;

public class GamePlayer implements IPlayer {

    private final Arena arena;

    private Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

    private final FileConfiguration fileConfiguration = MinerPlugin.getInstance().getMessages();

    private final String objectString = fileConfiguration.getString("waiting-scoreboard.title");

    @SuppressWarnings("deprecation")
    Objective objective = ManageHandler.getNMS().isLegacy() ?
            scoreboard.registerNewObjective("MinerWare", "dummy") :
            scoreboard.registerNewObjective("MinerWare", "dummy", translate(objectString));

    private final java.util.UUID UUID;

    private int points = 0, exp = 0;

    private float level;

    private int place = -1;

    private String voted = null;

    private ItemStack[] itemStacks;

    private String task, achievement;

    private State state = State.WAITING_GAME;

    private final Player player;

    public GamePlayer(UUID UUID, Arena arena) {
        this.UUID = UUID;
        this.player = Bukkit.getPlayer(UUID);
        this.arena = arena;
        objective.setDisplayName(translate(objectString));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public UUID getUUID() {
        return UUID;
    }

    public int getPoints() {
        return points;
    }

    @Override
    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public ItemStack[] getItemStacks() {
        return itemStacks;
    }

    public void setItemStacks(ItemStack[] itemStacks) {
        this.itemStacks = itemStacks;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void reset() {
        setTask(null);
        setAchievement(null);
        setState(State.WAITING_GAME);
    }

    public boolean isLoser() {
        return place != 1;
    }

    private void doBoardTask(boolean game) {
        if (Objects.requireNonNull(Bukkit.getPlayer(UUID)).getScoreboard().getObjective("MinerWare") == null)
            startDisplaying();
        List<String> strings = new ArrayList<>();
        String path = (game ? "game" : "waiting") + "-scoreboard.list";
        if (game) {
            strings.add("");
            for (String string : getLeaders(arena))
                strings.add(format(string));
        }
        strings.addAll(fileConfiguration.getStringList(path));
        int size = strings.size();
        if (scoreboard.getTeams().size() != strings.size())
            resetScores();
        for (String string : strings) {
            String s = size + ":" + UUID.toString().substring(0, 10);
            Team team = getTeam(s, size);
            String task = replacePlaceholders(arena,
                    Utils.request(string.replace("<points>", getPoints() + ""),
                            Bukkit.getPlayer(UUID)));
            if (game) {
                assert arena.getMicroGame() != null;
                task = task.replace("<game_name>", arena.getMicroGame().getName());
            }
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
        if(scoreboard.getObjective("MinerWare") != null)
            objective.unregister();
        scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
    }

    private void resetScores(){
        scoreboard.getEntries().forEach(scoreboard :: resetScores);
        for (Team team : scoreboard.getTeams())
            team.unregister();
    }

    public void update() {
        switch (arena.getStage()) {
            case NEW_GAME_STARTING:
            case PLAYING:
            case FINISHED:
                doBoardTask(true);
                break;
            default:
                doBoardTask(false);
        }
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
        org.bukkit.entity.Player player = Bukkit.getPlayer(getUUID());
        assert player != null;
        player.setScoreboard(scoreboard);
    }

    public float getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void setLevel(float level) {
        this.level = level;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public String getAchievement() {
        return achievement;
    }

    public void setAchievement(String achievement) {
        this.achievement = achievement;
    }

    private String format(String string){
        return translate(Utils.request(Objects.requireNonNull(fileConfiguration.
                getString("game-scoreboard.leader-format"))
                .replace("<name>", string.length() > LIMIT / 2 ? string.substring(0, LIMIT / 2): string)
                .replace("<points>", Objects.requireNonNull(arena.getPlayer
                        (Objects.requireNonNull(Bukkit.getPlayer(string)).getUniqueId())).getPoints() + ""), Bukkit.getPlayer(string)));
    }

    public enum State {
            PLAYING_GAME, LOSER_IN_GAME, WINNER_IN_GAME, WAITING_GAME
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    private static final int LIMIT = ManageHandler.getNMS().isLegacy() ? 16 : 64;

    public Arena getArena() {
        return arena;
    }

    public void setVoted(String voted) {
        this.voted = voted;
    }

    public Player getPlayer(){
        return player;
    }

    public String getVote() {
        return voted;
    }
}
