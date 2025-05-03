package org.gr_code.minerware.games;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public abstract class BossGame extends MicroGame {

    private final List<GamePlayer> winners = new ArrayList<>();
    private final List<GamePlayer> losers = new ArrayList<>();
    private final List<Location> fourLocations = new ArrayList<>();
    private static final FireworkEffect fireEffect = FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL).build();

    public BossGame(int time, Arena arena, String config) {
        super(time, arena, config, true);
    }
    
    @Override
    public void onLose(Player player, boolean teleport) {
        if (player == null) return;
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (gamePlayer.getState() != State.PLAYING_GAME) return;
        losers.add(gamePlayer);
        super.onLose(player, teleport);
    }
    
    @Override
    public void onWin(Player player, boolean teleport) {
        if (player == null) return;
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (gamePlayer.getState() != State.PLAYING_GAME) return;
        winners.add(gamePlayer);
        player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_PLAYER_LEVELUP.parseSound()), 5, 12);
        gamePlayer.setState(State.WINNER_IN_GAME);
        setupToGame(player);
        clearInventory(player);
        player.closeInventory();
        if (teleport) player.teleport(getArena().getProperties().getLobbyLocationLoser().clone().add(0,1,0));
    }
    
    private List<String> getListWinners() {
        GamePlayer oneName, twoName, threeName;
        List<String> msg_winners = getStringList("messages.winners");
        List<GamePlayer> newWinners = winners.stream().filter(gP -> getArena().getPlayers().contains(gP)).collect(Collectors.toList());
        List<GamePlayer> newLosers = losers.stream().filter(gP -> getArena().getPlayers().contains(gP)).collect(Collectors.toList());
        switch (newWinners.size()) {
            case 2:
                oneName = newWinners.get(0);
                twoName = newWinners.get(1);
                threeName = newLosers.get(newLosers.size() - 1);
                break;
            case 1:
                oneName = newWinners.get(0);
                twoName = newLosers.get(newLosers.size() - 1);
                threeName = newLosers.get(newLosers.size() - 2);
                break;
            case 0:
                oneName = newLosers.get(newLosers.size() - 1);
                twoName = newLosers.get(newLosers.size() - 2);
                threeName = newLosers.get(newLosers.size() - 3);
                break;
            default:
                oneName = newWinners.get(0);
                twoName = newWinners.get(1);
                threeName = newWinners.get(2);
        }
        oneName.addPoints(3);
        twoName.addPoints(2);
        threeName.addPoints(1);
        List<String> newMsgWinners = new ArrayList<>();
        msg_winners.forEach(msg -> newMsgWinners.add(msg.replace("<topOneName>", oneName.getPlayer().getName())
                .replace("<topTwoName>", twoName.getPlayer().getName())
                .replace("<topThreeName>", threeName.getPlayer().getName())));
        return  newMsgWinners;
    }
    
    private List<String> getListTwoWinners() {
        GamePlayer oneName, twoName;
        List<String> msg_duo_winners = getStringList("messages.duo-winners");
        List<GamePlayer> newWinners = winners.stream().filter(gP -> getArena().getPlayers().contains(gP)).collect(Collectors.toList());
        List<GamePlayer> newLosers = losers.stream().filter(gP -> getArena().getPlayers().contains(gP)).collect(Collectors.toList());
        switch (newWinners.size()) {
            case 1:
                oneName = newWinners.get(0);
                twoName = newLosers.get(newLosers.size() - 1);
                break;
            case 2:
                oneName = newWinners.get(0);
                twoName = newWinners.get(1);
                break;
            default:
                oneName = newLosers.get(newLosers.size() - 1);
                twoName = newLosers.get(newLosers.size() - 2);
        }
        oneName.addPoints(3);
        twoName.addPoints(2);
        List<String> newMsgDuo = new ArrayList<>();
        msg_duo_winners.forEach(msg -> newMsgDuo.add(msg.replace("<topOneName>", oneName.getPlayer().getName())
                .replace("<topTwoName>", twoName.getPlayer().getName())));
        return  newMsgDuo;
    }
    
    @Override
    public void printEndMessage() {
        List<String> endMsg = getArena().getCurrentPlayers() == 2 ? getListTwoWinners() : getListWinners();
        String winMessage = getString("messages.on-win");
        String loseMessage = getString("messages.on-lose");
        String winTitle = getString("titles.on-win");
        String loseTitle = getString("titles.on-lose");
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            String winOrLose = gamePlayer.getState() == State.WINNER_IN_GAME ? winMessage : loseMessage;
            String title = gamePlayer.getState() == State.WINNER_IN_GAME ? winTitle : loseTitle;
            endMsg.forEach(msg -> sendMessage(player, translate(msg.replace("<winOrLose>", requireNonNull(winOrLose)))));
            sendTitle(player, translate(title));
        });
    }
    
    @Override
    public void startGame() {
        int size = Cuboid.getSize(getArena()) / 4;
        Location param = getArena().getProperties().getCuboid().getCenter().getBlock().getLocation();
        fourLocations.addAll(Arrays.asList(param.clone().add(size, 0, size), param.clone().add(-size, 0, size),
                param.clone().add(-size, 0, -size), param.clone().add(size, 0, -size)));
        Cuboid cuboid = getArena().getProperties().getCuboid();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            setupToGame(player);
            player.setGameMode(GameMode.ADVENTURE);
            if (cuboid.notInside(player.getLocation()))
                player.teleport(getRandomLocation(getArena()));
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ENDER_DRAGON_GROWL.parseSound()), 5, 1);
        });
    }
    
    @Override
    public void countdown() {
        //OBFUSCATED PRIMERO
        String Iii = translate(getString("titles.subtitle-start-game"));
        String iIIIi = translate(getString(".titles.start-game"));
        String[] iII = {"", "&4&k|", "&4&k||", "&4&k|||"};
        int iiI = (getTime() - 80) / 20;
        int iIi = getTime() - 80 - (iiI * 20);
        if (!(iIi == 19 || iIi == 13 || iIi == 7 || iIi == 1)) return;
        fourLocations.forEach(loc -> {
            Firework fire = (Firework) requireNonNull(loc.getWorld()).spawnEntity(loc, EntityType.FIREWORK);
            FireworkMeta meta = fire.getFireworkMeta();
            meta.addEffect(fireEffect);
            fire.setFireworkMeta(meta);
        });
        Sound III = requireNonNull(XSound.BLOCK_NOTE_BLOCK_HAT.parseSound());
        getArena().getPlayers().forEach(gamePlayer -> {
            Player iIiI = gamePlayer.getPlayer();
            int ii = iIi / 6;
            if (iIi == 19) iIiI.playSound(iIiI.getLocation(), III, 5, 1);
            sendTitle(iIiI, translate(iII[ii]+iIIIi+iII[ii]), translate(Iii), 0, 70, 20);
        });
    }
    
    @Override
    public String getTask(GamePlayer gamePlayer) {
        return "";
    }
    
}
