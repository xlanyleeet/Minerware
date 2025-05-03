package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.arena.Properties.Square;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.sendTitle;
import static org.gr_code.minerware.manager.type.Utils.translate;

public class IgniteTNT extends MicroGame {

	public IgniteTNT(Arena arena) {
		super(280, arena, "ignite-the-tnt");
	}

	@Override
	public String getAchievementForMsg() {
		String achievementMsg = getString("messages.achievement");
		List<GamePlayer> achievement = getArena().getPlayers().stream().filter(x -> x.getAchievement() != null).collect(Collectors.toList());
		if (achievement.isEmpty()) return "";
		int maximum = 0;
		GamePlayer gamePlayer = null;
		for (GamePlayer key : achievement) {
			int doubleKey = Integer.parseInt(key.getAchievement());
			if (doubleKey <= maximum) continue;
			maximum = doubleKey;
			gamePlayer = key;
		}
		assert achievementMsg != null;
		String name = gamePlayer.getPlayer().getName();
		return achievementMsg.replace("<name>", name).replace("<count>", Integer.toString(maximum));
	}
	
	private void generateTNTs() {
		int count = getArena().getProperties().getType().equals("MEGA") ? 2 : 1;
		List<Square> squares = new ArrayList<>(Arrays.asList(getArena().getProperties().getSquares()));
		if (getArena().isHardMode()) {
			int hard = getArena().getProperties().getSquares().length / 2;
			for (int i = 0; i < hard; i ++) squares.remove(new Random().nextInt(squares.size()));
		}
		for (Square sq : squares) for (int i = 0; i < count; i ++) {
			Location location = sq.getLocations().get(new Random().nextInt(sq.getLocations().size()));
			ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.TNT.parseItem()), location.getBlock());
		}
	}

    @Override
    public void secondStartGame() {
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		generateTNTs();
		Cuboid cuboid = getArena().getProperties().getCuboid();
		ItemStack flint = ItemBuilder.start(requireNonNull(XMaterial.FLINT_AND_STEEL.parseItem())).setUnbreakable(true).build();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.getInventory().setItem(0, flint);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    }
    
    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
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
        return Game.IGNITE_TNT;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.FLINT_AND_STEEL.parseItem())).setDisplayName("&c&lIGNITE THE TNT").build();
	}

	@Override
	public void event(Event event) {
    	if (event instanceof PlayerInteractEvent) playerInteractEvent(event);
    	else if (event instanceof EntityExplodeEvent) entityExplode(event);
	}

	private void playerInteractEvent(Event event) {
		PlayerInteractEvent e = (PlayerInteractEvent) event;
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null) return;
		if (!e.getItem().isSimilar(ItemBuilder.start(requireNonNull(XMaterial.FLINT_AND_STEEL.parseItem())).setUnbreakable(true).build())) return;
		if (!Utils.getItem(e.getClickedBlock()).isSimilar(XMaterial.TNT.parseItem())) {
			e.setCancelled(true);
			return;
		}
		e.setCancelled(false);
		GamePlayer gamPl = requireNonNull(getArena().getPlayer(uuid));
		if (requireNonNull(gamPl).getAchievement() == null) gamPl.setAchievement("1");
		else gamPl.setAchievement((Integer.parseInt(gamPl.getAchievement()) + 1) + "");
		onWin(player, false);
	}

	private void entityExplode(Event event) {
		EntityExplodeEvent e = (EntityExplodeEvent) event;
		if (e.getEntity() == null) return;
		if (e.getEntity().getType() != EntityType.PRIMED_TNT) return;
		e.setCancelled(false);
		e.blockList().clear();
		double x = e.getEntity().getLocation().getX();
		double y = e.getEntity().getLocation().getY();
		double z = e.getEntity().getLocation().getZ();
		getArena().getPlayers().stream().filter(gamePlayer -> distance(gamePlayer, e.getEntity())).forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			double second_x = player.getLocation().getX();
			double second_y = player.getLocation().getY();
			double second_z = player.getLocation().getZ();
			player.setVelocity(new Vector((second_x - x), (second_y - y) + 1, (second_z - z)).normalize());
		});
	}

	private boolean distance(GamePlayer gamePlayer, Entity entity) {
		Player player = gamePlayer.getPlayer();
		return player.getLocation().distance(entity.getLocation()) <= 2.5;
	}

}