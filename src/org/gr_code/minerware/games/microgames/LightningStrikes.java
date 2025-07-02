package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.sendTitle;
import static org.gr_code.minerware.manager.type.Utils.translate;

public class LightningStrikes extends MicroGame {
    

    private final List<Block> ironBlocks;

    public LightningStrikes(Arena arena) {
        super(280, arena, "lightning-strikes");
        ironBlocks = new CopyOnWriteArrayList<>();
    }

    @Override
    public void secondStartGame() {
        Location first = getArena().getProperties().getFirstLocation();
        getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
                .filter(l -> Math.random() < 0.015).forEach(l -> {
            ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.IRON_BLOCK.parseItem()), l.getBlock());
            ironBlocks.add(l.getBlock());
        });
        getArena().getProperties().destroySquares();
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            player.teleport(getRandomLocation(getArena()));
            sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
            player.setPlayerTime(18000, false);
        });
    }
    
    private void updateStrikes() {
        Location first = getArena().getProperties().getFirstLocation();
        for (Block block : ironBlocks) {
            ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.BEDROCK.parseItem()), block);
            block.getWorld().strikeLightning(block.getLocation());
            ironBlocks.remove(block);
        }
        float count = getArena().isHardMode() ? 0.005f : 0;
        getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
                .filter(l -> Math.random() < 0.015 + count).forEach(l -> {
            ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.IRON_BLOCK.parseItem()), l.getBlock());
            ironBlocks.add(l.getBlock());
        });
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        if ((getTime() + 10) % 30 == 0) updateStrikes();
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        List<GamePlayer> playerList = getArena().getPlayers();
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (y <= param_y) onLose(player, true);
        });
    }

    @Override
    public void end() {
        getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
                .forEach(gamePlayer -> onWin(gamePlayer.getPlayer(), false));
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.LIGHTNING_STRIKES;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.IRON_BLOCK.parseItem())).setDisplayName("&6&lLIGHTNING STRIKES").build();
    }

    @Override
    public void event(Event event) {
        if (!(event instanceof EntityDamageEvent)) return;
        EntityDamageEvent e = (EntityDamageEvent) event;
        Player player = (Player) e.getEntity();
        UUID uuid = player.getUniqueId();
        if (e.getCause() != EntityDamageEvent.DamageCause.LIGHTNING) return;
        if (requireNonNull(getArena().getPlayer(uuid)).getState() != State.PLAYING_GAME) return;
        onLose(player, true);
    }

}


