package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.manager.type.Utils.*;

public class Move extends MicroGame {
	
	private double countMeter;

	public Move(Arena arena) {
		super(480, arena, "move-game");
	}

    @Override
    public void startGame() {
    	countMeter = 0;
        super.startGame();
    }

	@Override
	public String getAchievementForMsg() {
    	if (countMeter == 0) return "";
		String achievementMsg = getString("messages.achievement");
		return requireNonNull(achievementMsg).replace("<count>", String.format("%.1f", countMeter).replace(",", "."));
	}

    @Override
    public void secondStartGame() {
		if (getArena().isHardMode()) {
			ItemStack lava = requireNonNull(XMaterial.LAVA.parseItem());
			for (Properties.Square square : getArena().getProperties().getSquares())
				square.getLocations().forEach(l -> ManageHandler.getModernAPI().setBlock(lava, l.clone().add(0,-1,0).getBlock()));
			getArena().getProperties().destroySquares();
		}
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		Cuboid cuboid = getArena().getProperties().getCuboid();
		int count = getArena().getProperties().getSize();
		getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(Cuboid.getRandomLocation(getArena()));
    		gamePlayer.setState(State.PLAYING_GAME);
            player.addPotionEffect(PotionEffectType.SPEED.createEffect(999999, count));
    		double now_x = player.getLocation().getX();
    		double now_z = player.getLocation().getZ();
    		String loc = now_x + "," + now_z;
            gamePlayer.setTask(loc);
			sendTitle(player, title, subtitle,0,70,20);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    }
    
    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
    	Location firstArena = getArena().getProperties().getFirstLocation();
    	List<GamePlayer> playerList = getArena().getPlayers();
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
    	playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		double now_x = player.getLocation().getX();
    		double now_z = player.getLocation().getZ();
    		String[] oldString = gamePlayer.getTask().split(",");
    		double old_x = Double.parseDouble(oldString[0]);
    		double old_z = Double.parseDouble(oldString[1]);
    		String loc = now_x + "," + now_z;
    		gamePlayer.setTask(loc);
    		if ((old_x == now_x && old_z == now_z) || player.isSneaking()) onLose(player, true);
    		Location first = new Location(firstArena.getWorld(), old_x, firstArena.getBlockY(), old_z);
    		Location second = new Location(firstArena.getWorld(), now_x, firstArena.getBlockY(), now_z);
    		countMeter += Double.parseDouble(getSeconds(first.distance(second)));
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
        return Game.MOVE;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.DIAMOND_BOOTS.parseItem())).setDisplayName("&b&lMOVE").build();
	}

}



