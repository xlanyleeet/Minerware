package org.gr_code.minerware.games.microgames;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class IceFloor extends MicroGame {

    private int countWater;
    private int count;

    public IceFloor(Arena arena) {
        super(380, arena, "the-frozen-floor");
    }

    @Override
    public void startGame() {
        countWater = 0;
        super.startGame();
    }

    @Override
	public String getAchievementForMsg() {
        String achievementMsg = getString("messages.achievement");
        return requireNonNull(achievementMsg).replace("<count>", Integer.toString(countWater));
    }

    private void newStage(Block block) {
    	ItemStack ItemStack = getItem(block);
        XMaterial firstStage = ManageHandler.getModernAPI().isLegacy() ? XMaterial.PACKED_ICE : XMaterial.BLUE_ICE;
        XMaterial secondStage = ManageHandler.getModernAPI().isLegacy() ? XMaterial.ICE : XMaterial.PACKED_ICE;
        XMaterial thirdStage = ManageHandler.getModernAPI().isLegacy() ? XMaterial.LIGHT_BLUE_STAINED_GLASS : XMaterial.ICE;
        if (ItemStack.isSimilar(firstStage.parseItem())) ManageHandler.getModernAPI().setBlock(requireNonNull(secondStage.parseItem()), block);
        else if (ItemStack.isSimilar(secondStage.parseItem())) ManageHandler.getModernAPI().setBlock(requireNonNull(thirdStage.parseItem()), block);
        else if (ItemStack.isSimilar(thirdStage.parseItem())) {
            ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.WATER.parseItem()), block);
            countWater++;
        }
    }

    private void generateFloor() {
        getArena().getProperties().destroySquares();
        ItemStack ice = ManageHandler.getModernAPI().isLegacy() ? XMaterial.PACKED_ICE.parseItem() : XMaterial.BLUE_ICE.parseItem();
        getArena().getProperties().getCuboid().getLocations().stream()
                .filter(location -> location.getBlockY() == getArena().getProperties().getFirstLocation().getBlockY())
                .forEach(loc -> ManageHandler.getModernAPI().setBlock(requireNonNull(ice), loc.getBlock()));
    }

    @Override
    public void secondStartGame() {
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        Cuboid cuboid = getArena().getProperties().getCuboid();
        count = getArena().isHardMode() ? 5 : 0;
        generateFloor();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        });
    }
    
    private void updateFloor() {
        getArena().getProperties().getCuboid().getLocations().stream()
                .filter(location -> location.getBlockY() == getArena().getProperties().getFirstLocation().getBlockY())
                .filter(l -> Math.random() <= 0.2).forEach(loc -> newStage(loc.getBlock()));
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        if (getTime() % (15 - count) == 0) updateFloor();
        List<GamePlayer> playerList = getArena().getPlayers();
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1;
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (y <= param_y || isFluid(player.getLocation().getBlock())) onLose(player, true);
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
        return Game.ICE_FLOOR;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.ICE.parseItem())).setDisplayName("&3&lTHE FROZEN FLOOR").build();
	}

}



