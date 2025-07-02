package org.gr_code.minerware.games.bossgames;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.BossGame;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.getItem;

public class BlockPartyBoss extends BossGame {
	
    private final static String[] new_list = {"YELLOW_TERRACOTTA", "WHITE_TERRACOTTA", "ORANGE_TERRACOTTA", "RED_TERRACOTTA", "BLUE_TERRACOTTA",
    		"PINK_TERRACOTTA", "LIME_TERRACOTTA", "GREEN_TERRACOTTA", "GRAY_TERRACOTTA", "CYAN_TERRACOTTA", "PURPLE_TERRACOTTA",
    		"MAGENTA_TERRACOTTA", "BLACK_TERRACOTTA", "BROWN_TERRACOTTA", "LIGHT_BLUE_TERRACOTTA", "LIGHT_GRAY_TERRACOTTA"};

	private final ItemStack[] listItem;

	public BlockPartyBoss(Arena arena) {
		super(2480, arena, "colours");
		listItem = new ItemStack[15];
	}

    @Override
    public void startGame() {
    	for (int i = 0; i < 15; i ++) listItem[i] = XMaterial.valueOf(new_list[new Random().nextInt(16)]).parseItem();
        super.startGame();
    }

    @Override
    public void secondStartGame() {
		getArena().getProperties().destroySquares();
		Cuboid cuboid = getArena().getProperties().getCuboid();
		getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
            gamePlayer.setState(State.PLAYING_GAME);
            player.setGameMode(GameMode.SURVIVAL);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    	setRandomFloor(listItem[0]);
    }
    
    private void setVoid(ItemStack item) {
    	Location first = getArena().getProperties().getFirstLocation();
    	getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
    	.filter(l -> !getItem(l.getBlock()).isSimilar(item)).forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.AIR.parseItem()), l.getBlock()));
    	getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
				.forEach(gamePlayer -> clearInventory(gamePlayer.getPlayer()));
    }
    
    private void setRandomFloor(ItemStack item) {
    	Location first = getArena().getProperties().getFirstLocation();
    	Cuboid cuboid = getArena().getProperties().getCuboid();
    	List<Location> locations = cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY()).collect(Collectors.toList());
    	locations.forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.AIR.parseItem()), l.getBlock()));
    	ManageHandler.getModernAPI().setBlock(item, locations.get(new Random().nextInt(locations.size())).getBlock());
    	while (locations.stream().anyMatch(l -> getItem(l.getBlock()).isSimilar(XMaterial.AIR.parseItem()))) {
    		List<Location> locationsAir = locations.stream()
					.filter(l -> getItem(l.getBlock()).isSimilar(XMaterial.AIR.parseItem())).collect(Collectors.toList());
    		Location location = locationsAir.get(new Random().nextInt(locationsAir.size()));
    		ItemStack stack = listItem[new Random().nextInt(15)];
    		locationsAir.stream().filter(l -> location.distance(l) <= new Random().nextInt(3) + 1)
					.forEach(l -> ManageHandler.getModernAPI().setBlock(stack, l.getBlock()));
    	}
    	getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player p = gamePlayer.getPlayer();
    		for (int i = 0; i < 9; i++) p.getInventory().setItem(i, item);
    	});
    }

    private void checkFloor() {
		switch (getTime()) {
			case 0: end(); break;
			case 2400: secondStartGame(); break;
			case 2300: setVoid(listItem[0]); break;
			case 2200: setRandomFloor(listItem[1]); break;
			case 2105: setVoid(listItem[1]); break;
			case 2005: setRandomFloor(listItem[2]); break;
			case 1915: setVoid(listItem[2]); break;
			case 1815: setRandomFloor(listItem[3]); break;
			case 1730: setVoid(listItem[3]); break;
			case 1630: setRandomFloor(listItem[4]); break;
			case 1550: setVoid(listItem[4]); break;
			case 1450: setRandomFloor(listItem[5]); break;
			case 1375: setVoid(listItem[5]); break;
			case 1275: setRandomFloor(listItem[6]); break;
			case 1205: setVoid(listItem[6]); break;
			case 1105: setRandomFloor(listItem[7]); break;
			case 1040: setVoid(listItem[7]); break;
			case 940: setRandomFloor(listItem[8]); break;
			case 880: setVoid(listItem[8]); break;
			case 780: setRandomFloor(listItem[9]); break;
			case 725: setVoid(listItem[9]); break;
			case 625: setRandomFloor(listItem[10]); break;
			case 575: setVoid(listItem[10]); break;
			case 475: setRandomFloor(listItem[11]); break;
			case 430: setVoid(listItem[11]); break;
			case 330: setRandomFloor(listItem[12]); break;
			case 290: setVoid(listItem[12]); break;
			case 190: setRandomFloor(listItem[13]); break;
			case 155: setVoid(listItem[13]); break;
			case 55: setRandomFloor(listItem[14]); break;
			case 25: setVoid(listItem[14]); break;
		}
	}

    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		checkFloor();
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		List<GamePlayer> playerList = getArena().getPlayers();
    	if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
    	if (playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).count() == 1) setTime(1);
		if (getTime() % 20 == 0) playerList.forEach(x -> x.getPlayer().setLevel(getTime() / 20));
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
        return Game.BOSS_BLOCK_PARTY;
    }

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.YELLOW_STAINED_GLASS.parseItem())).setDisplayName("&5&lCOLOURS").build();
	}

}


