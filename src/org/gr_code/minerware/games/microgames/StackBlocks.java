package org.gr_code.minerware.games.microgames;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.api.events.PlayerStackedAllBlocksEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class StackBlocks extends MicroGame {

	public StackBlocks(Arena arena) {
		super(380, arena, "stack-blocks");
	}

    @Override
    public void onWin(Player player, boolean teleport) {
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
		super.onWin(player, teleport);
		String win = translate(getString("messages.stacked-ten-blocks"));
		sendMessage(player, win);
    }

	@Override
	public String getAchievementForMsg() {
		String achievementMsg = getString("messages.achievement");
		List<GamePlayer> achievement = getArena().getPlayers().stream()
				.filter(x -> Integer.parseInt(x.getAchievement()) > 0).collect(Collectors.toList());
		if (achievement.isEmpty()) return "";
		int maximum = 0;
		GamePlayer gamePlayer = null;
		for (GamePlayer key : achievement) {
			int doubleKey = Integer.parseInt(key.getAchievement());
			if (doubleKey <= maximum) continue;
			maximum = doubleKey;
			gamePlayer = key;
		}
		String name = gamePlayer.getPlayer().getName();
		return requireNonNull(achievementMsg).replace("<name>", name).replace("<count>", Integer.toString(maximum));
	}

    @Override
    public void secondStartGame() {
		List<String> blocks = getStringList("blocks");
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		Cuboid cuboid = getArena().getProperties().getCuboid();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(State.PLAYING_GAME);
            gamePlayer.setTask("0:0:0:0"); gamePlayer.setAchievement("0");
            player.getInventory().setHeldItemSlot(0);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
			player.setGameMode(GameMode.SURVIVAL);
			ItemStack wool = requireNonNull(XMaterial.valueOf(blocks.get(new Random().nextInt(blocks.size()))).parseItem());
			wool.setAmount(20); player.getInventory().setItem(0, wool);
		});
    }

    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		List<GamePlayer> playerList = getArena().getPlayers();
    	if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		int y = player.getLocation().getBlockY();
    		if (y <= param_y) onLose(player, true);
    	});
    }

    @Override
	public void end() {
		getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
				.forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
		super.end();
	}

    @Override
    public Game getGame() {
        return Game.STACK_BLOCKS;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}


    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.STONE.parseItem())).setDisplayName("&d&lSTACK BLOCKS").build();
	}

	private void blockPlace(BlockPlaceEvent e) {
		Player player = e.getPlayer();
		GamePlayer x = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		if (x.getState() == State.LOSER_IN_GAME) return;
		if ((x).getTask() == null) return;
		e.setCancelled(false);
		String msg = getString("messages.place-block");
		assert msg != null;
		int x_pos = e.getBlock().getLocation().getBlockX();
		int y_pos = e.getBlock().getLocation().getBlockY();
		int z_pos = e.getBlock().getLocation().getBlockZ();
		String[] task = x.getTask().split(":");
		boolean sameX = Integer.parseInt(task[0]) == x_pos;
		boolean sameY = Integer.parseInt(task[1]) + 1 == y_pos;
		boolean sameZ = Integer.parseInt(task[2]) == z_pos;
		if (!(sameX && sameY && sameZ)) {
			x.setTask(x_pos + ":" + y_pos + ":" + z_pos + ":1");
			sendMessage(player, translate(msg.replace("<count_blocks>", 1+"")));
			if (Integer.parseInt(task[3]) != 0) return;
		}
		switch (Integer.parseInt(task[3])) {
			case 0:
				x.setAchievement("1");
				break;
			case 9:
				onWin(player, false);
				int count_blocks = Integer.parseInt(task[3]) + 1;
				x.setTask(x_pos + ":" + y_pos + ":" + z_pos + ":" + count_blocks);
				if (count_blocks > Integer.parseInt(x.getAchievement())) x.setAchievement(count_blocks + "");
				break;
			case 19:
				List<Block> listBlocks = new ArrayList<>();
				for (int i = 0; i < 20; i++)
					listBlocks.add(e.getBlock().getLocation().clone().add(0, -i, 0).getBlock());
				PlayerStackedAllBlocksEvent eventStack = new PlayerStackedAllBlocksEvent(player, getArena(), e.getBlock(), listBlocks);
				Bukkit.getPluginManager().callEvent(eventStack);
				count_blocks = Integer.parseInt(task[3]) + 1;
				sendMessage(player, translate(msg.replace("<count_blocks>", "" + count_blocks)));
				if (count_blocks > Integer.parseInt(x.getAchievement())) x.setAchievement(count_blocks + "");
				break;
			default:
				count_blocks = Integer.parseInt(task[3]) + 1;
				x.setTask(x_pos + ":" + y_pos + ":" + z_pos + ":" + count_blocks);
				sendMessage(player, translate(msg.replace("<count_blocks>", "" + count_blocks)));
				if (count_blocks > Integer.parseInt(x.getAchievement())) x.setAchievement(count_blocks + "");
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof EntityDamageEvent) entityDamage(event);
		else if (event instanceof BlockPlaceEvent) blockPlace((BlockPlaceEvent) event);

	}

	private void entityDamage(Event event) {
		if (!getArena().isHardMode()) return;
		EntityDamageEvent e = (EntityDamageEvent) event;
		Player player = (Player) e.getEntity();
		UUID uuid = player.getUniqueId();
		if (requireNonNull(getArena().getPlayer(uuid)).getState() != State.PLAYING_GAME) return;
		if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
		e.setCancelled(false);
		e.setDamage(0);
		player.setHealth(20);
	}

}


