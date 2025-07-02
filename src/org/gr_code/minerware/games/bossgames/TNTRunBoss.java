package org.gr_code.minerware.games.bossgames;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.cuboid.Cuboid.getSize;
import static org.gr_code.minerware.manager.type.Utils.getSeconds;


public class TNTRunBoss extends BossGame {
	
	private int pointY = 0;
	private final static float[][] coord = {{-0.3f, -0.3f}, {0.3f, 0.3f}, {-0.3f, 0.3f}, {0.3f, -0.3f}};
	private List<Location> locations;

	public TNTRunBoss(Arena arena) {
		super(2480, arena, "tnt-run");
	}


	@SuppressWarnings("IntegerDivisionInFloatingPointContext")
	@Override
	public void startGame() {
		Location first = getArena().getProperties().getFirstLocation();
		int centerX = getArena().getProperties().getCuboid().getCenter().getBlockX();
		int centerZ = getArena().getProperties().getCuboid().getCenter().getBlockZ();
		Location center = new Location(first.getWorld(), centerX, first.getBlockY(), centerZ);
		Cuboid cuboid = getArena().getProperties().getCuboid();
		locations = cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
				.filter(l -> center.distance(l) <= getSize(getArena()) / 2 + 1).collect(Collectors.toList());
		int count = (getSize(getArena()) + 1) / 6;
		for (int i = 0; i < count; i ++) {
			pointY += 5;
			locations.addAll(cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY() + pointY)
					.filter(l -> center.clone().add(0, pointY, 0).distance(l) <= getSize(getArena()) / 2 + 1).collect(Collectors.toList()));
		}
		super.startGame();
	}
	
	@Override
    public void secondStartGame() {
		getArena().getProperties().destroySquares();
		Location first = getArena().getProperties().getFirstLocation();
		getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
				.forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.AIR.parseItem()), l.getBlock()));
		locations.forEach(location -> location.getBlock().setType(Material.TNT));
		getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			player.teleport(getRandomLocation(getArena()).add(0, pointY, 0));
            gamePlayer.setState(State.PLAYING_GAME);
            player.setGameMode(GameMode.ADVENTURE);
            float now_x = Float.parseFloat(getSeconds(((float)player.getLocation().getX())));
			float now_z = Float.parseFloat(getSeconds(((float)player.getLocation().getZ())));
    		String loc = now_x + "," + now_z;
            gamePlayer.setTask(loc);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    }
    
    private void removeTNT(Player player, GamePlayer gamePlayer) {
		float now_x = Float.parseFloat(getSeconds(((float)player.getLocation().getX())));
		float now_z = Float.parseFloat(getSeconds(((float)player.getLocation().getZ())));
		String task = requireNonNull(getArena().getPlayer(player.getUniqueId())).getTask();
		String[] oldString = task.split(",");
		float old_x = Float.parseFloat(oldString[0]);
		float old_z = Float.parseFloat(oldString[1]);
		String loc = now_x + "," + now_z;
        gamePlayer.setTask(loc);
		Location location = new Location(player.getWorld(), old_x, player.getLocation().getBlockY() - 1, old_z);
		for (float[] coordSecond : coord) {
			Block block = location.clone().add(coordSecond[0], 0, coordSecond[1]).getBlock();
			if (block.getType() != Material.TNT) continue;
			ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.AIR.parseItem()), block);
		}
    }
    
    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		List<GamePlayer> playerList = getArena().getPlayers();
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
    	if (playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).count() == 1) setTime(1);
		if (getTime() % 20 == 0) playerList.forEach(gamePlayer -> gamePlayer.getPlayer().setLevel(getTime() / 20));
    	if (getTime() < 2300)
			playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
					.forEach(gamePlayer -> checkFor2300(gamePlayer, param_y));
		else
			playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
					.forEach(gamePlayer -> checkFor2400(gamePlayer, param_y));
    }

    private void checkFor2300(GamePlayer gamePlayer, int param_y) {
		Player player = gamePlayer.getPlayer();
		removeTNT(player, gamePlayer);
		int y = player.getLocation().getBlockY();
		if (y <= param_y) onLose(player, true);
	}

	private void checkFor2400(GamePlayer gamePlayer, int param_y) {
		Player player = gamePlayer.getPlayer();
		int y = player.getLocation().getBlockY();
		if (y <= param_y) onLose(player, true);
	}

    @Override
	public void end() {
		getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
				.forEach(gamePlayer -> onWin(gamePlayer.getPlayer(), false));
		super.end();
	}

	@Override
    public Game getGame() {
        return Game.BOSS_TNT_RUN;
    }

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.TNT.parseItem())).setDisplayName("&4&lTNT-RUN").build();
	}

}


