package org.gr_code.minerware.games.microgames;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.arena.Properties.Square;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.listeners.game.ProjectileHit_Games;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.nms.version.v1_8_R3;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.manager.type.Utils.*;

public class ExplosiveBow extends MicroGame {
	
	public ExplosiveBow(Arena arena) {
		super(580, arena, "explosive-bow-fight");
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
	
	private void createColumns() {
		for (Square sq : getArena().getProperties().getSquares()) sq.getLocations().forEach(location -> {
			for (int i = 0; i < new Random().nextInt(4) + 2; i ++)
				ManageHandler.getNMS().setBlock(
						requireNonNull(XMaterial.OAK_PLANKS.parseItem()),
						location.clone().add(0, 2 + i, 0).getBlock());

		});
		
	}
	
	private void createWalls() {
		Location first = getArena().getProperties().getFirstLocation();
		Location second = getArena().getProperties().getSecondLocation();
		List<Location> locations = getArena().getProperties().getCuboid().getLocations();
		boolean isHard = getArena().isHardMode();
		Predicate<Location> predicate = isHard ? l -> l.getBlockY() == first.getBlockY() + 3 || l.getBlockY() == first.getBlockY() + 2
				: l -> l.getBlockY() < first.getBlockY() + 3;
		if (isHard) {
			getArena().getProperties().destroySquares();
			locations.stream().filter(l -> l.getBlockY() == first.getBlockY()).forEach(l -> l.getBlock().setType(Material.AIR));
		}
		//FLOOR
		locations.stream().filter(predicate).forEach(l -> ManageHandler.getNMS()
				.setBlock(requireNonNull(XMaterial.GRASS_BLOCK.parseItem()), l.getBlock()));
		//WALLS
		locations.stream().filter(l -> l.getBlockY() >= first.getBlockY() + 3 && l.getBlockY() < first.getBlockY() + 9)
				.filter(l -> l.getBlockX() == first.getBlockX() || l.getBlockZ() == first.getBlockZ()
						|| l.getBlockX() == second.getBlockX() || l.getBlockZ() == second.getBlockZ())
				.forEach(l -> ManageHandler.getNMS().setBlock(
						requireNonNull(XMaterial.OAK_PLANKS.parseItem()), l.getBlock()));
		//CEILING
		locations.stream().filter(l -> l.getBlockY() == first.getBlockY() + 8)
				.filter(l -> l.getBlock().getType() == Material.AIR)
				.forEach(l -> ManageHandler.getNMS().setBlock(
						requireNonNull(XMaterial.GLASS.parseItem()), l.getBlock()));
	}
	
	@Override
	public void secondStartGame() {
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
    	createColumns(); createWalls();
		XMaterial xHelmet = getArena().isHardMode() ? XMaterial.CHAINMAIL_HELMET : XMaterial.IRON_HELMET;
		XMaterial xChestplate = getArena().isHardMode() ? XMaterial.LEATHER_CHESTPLATE : XMaterial.IRON_CHESTPLATE;
		XMaterial xLeggings = getArena().isHardMode() ? XMaterial.LEATHER_LEGGINGS : XMaterial.IRON_LEGGINGS;
		XMaterial xBoots = getArena().isHardMode() ? XMaterial.CHAINMAIL_BOOTS : XMaterial.IRON_BOOTS;
    	ItemStack bow = ItemBuilder.start(requireNonNull(XMaterial.BOW.parseItem())).setUnbreakable(true).addEnchantment(Enchantment.ARROW_INFINITE, 1, true).build();
		ItemStack helmet = ItemBuilder.start(requireNonNull(xHelmet.parseItem())).setUnbreakable(true).build();
		ItemStack chestplate = ItemBuilder.start(requireNonNull(xChestplate.parseItem())).setUnbreakable(true).build();
		ItemStack leggings = ItemBuilder.start(requireNonNull(xLeggings.parseItem())).setUnbreakable(true).build();
		ItemStack boots = ItemBuilder.start(requireNonNull(xBoots.parseItem())).setUnbreakable(true).build();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		player.teleport(Cuboid.getRandomLocation(getArena()).add(0, 3, 0));
			sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.getInventory().setHeldItemSlot(0);
            player.getInventory().setItem(0, bow);
            player.getInventory().setItem(1, XMaterial.ARROW.parseItem());
            player.getInventory().setHelmet(helmet);
            player.getInventory().setChestplate(chestplate);
            player.getInventory().setLeggings(leggings);
            player.getInventory().setBoots(boots);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
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
				.forEach(gamePlayer -> onWin(gamePlayer.getPlayer(), false));
		super.end();
	}

    @Override
    public Game getGame() {
        return Game.EXPLOSIVE_BOW;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.FIRE_CHARGE.parseItem())).setDisplayName(ChatColor.RED + "&lEXPLOSIVE BOW FIGHT").build();
	}

	@Override
	public void event(Event event) {
		if (event instanceof EntityDamageEvent) entityDamage(event);
		else if (event instanceof ProjectileHitEvent) projectileHit(event);
		else if ((ManageHandler.getNMS() instanceof v1_8_R3 || !ManageHandler.getNMS().oldVersion()) && event instanceof BlockExplodeEvent) blockExplode(event);
		else if (ManageHandler.getNMS().oldVersion() && event instanceof EntityExplodeEvent) entityExplode(event);
		else if (event instanceof EntityShootBowEvent) entityShootBow(event);
    }

    private void entityDamage(Event event) {
		EntityDamageEvent e = (EntityDamageEvent) event;
		Player player = (Player) e.getEntity();
		UUID uuid = player.getUniqueId();
		boolean isEntityExplosion = e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;
		boolean isBlockExplosion = e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION;
		if (!(isBlockExplosion || isEntityExplosion)) return;
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(uuid));
		if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
		e.setCancelled(false);
		e.setDamage(e.getDamage() * 0.8);
		if (e.getDamage() < player.getHealth()) return;
		e.setCancelled(true);
		player.setHealth(20);
		onLose(player, true);
	}

	private void projectileHit(Event event) {
		ProjectileHitEvent e = (ProjectileHitEvent) event;
		Block hitBlock = ProjectileHit_Games.getHitBlockNMS(e);
		assert hitBlock != null;
		if (requireNonNull(getArena().getMicroGame()).getTime() < 5) return;
		hitBlock.getWorld().createExplosion(hitBlock.getLocation().add(0,1,0), 2.5F);
	}

	private void entityExplode(Event event) {
		EntityExplodeEvent e = (EntityExplodeEvent) event;
		if (e.getEntity() != null) return;
		Location first = getArena().getProperties().getFirstLocation();
		Location second = getArena().getProperties().getSecondLocation();
		e.setCancelled(false);
		e.blockList().removeAll(getDeleteBlocks(first, second, e.blockList()));
	}

	private List<Block> getDeleteBlocks(Location first, Location second, List<Block> blockList) {
		List<Block> blocks = new ArrayList<>();
		for (Block block : blockList) {
			ItemStack itemBlock = getItem(block);
			if (itemBlock.isSimilar(XMaterial.GRASS_BLOCK.parseItem())) continue;
			if (!itemBlock.isSimilar(XMaterial.OAK_PLANKS.parseItem())) {
				blocks.add(block);
				continue;
			}
			boolean isRightFirstX = block.getX() == first.getBlockX();
			boolean isRightFirstZ = block.getZ() == first.getBlockZ();
			boolean isRightSecondX = block.getX() == second.getBlockX();
			boolean isRightSecondZ = block.getZ() == second.getBlockZ();
			if (isRightFirstX || isRightSecondX || isRightFirstZ || isRightSecondZ) blocks.add(block);
		}
		return blocks;
	}

	private void blockExplode(Event event) {
		BlockExplodeEvent e = (BlockExplodeEvent) event;
		Location first = getArena().getProperties().getFirstLocation();
		Location second = getArena().getProperties().getSecondLocation();
		e.setCancelled(false);
		e.blockList().removeAll(getDeleteBlocks(first, second, e.blockList()));
	}

	private void entityShootBow(Event event) {
		EntityShootBowEvent e = (EntityShootBowEvent) event;
		if (!(e.getEntity() instanceof Player)) return;
		Player player = (Player) e.getEntity();
		UUID uuid = player.getUniqueId();
		if (requireNonNull(getArena().getPlayer(uuid)).getState() != State.PLAYING_GAME) return;
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(uuid));
		if (requireNonNull(gamePlayer).getAchievement() == null) gamePlayer.setAchievement("1");
		else gamePlayer.setAchievement(Integer.toString(Integer.parseInt(gamePlayer.getAchievement()) + 1));
	}

}