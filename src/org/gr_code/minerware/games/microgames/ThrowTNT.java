package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.sendTitle;
import static org.gr_code.minerware.manager.type.Utils.translate;

public class ThrowTNT extends MicroGame {
	
    public List<TNTPrimed> listTNT;
	private static final List<String> cd = Arrays.asList(
			"", "&4&l|", "&4&l||", "&4&l|||", "&4&l||||", "&4&l|||||", "&4&l||||||", "&4&l|||||||", "&4&l||||||||", "&4&l|||||||||", "&4&l||||||||||",
			"&4&l|||||||||||", "&4&l||||||||||||", "&4&l|||||||||||||", "&4&l||||||||||||||", "&4&l|||||||||||||||", "&4&l||||||||||||||||",
			"&4&l|||||||||||||||||", "&4&l||||||||||||||||||", "&4&l|||||||||||||||||||", "&4&l||||||||||||||||||||");
	static {
		cd.sort(Comparator.reverseOrder());
	}

	public ThrowTNT(Arena arena) {
		super(480, arena, "throw-the-tnt");
		listTNT = new ArrayList<>();
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
		String name = gamePlayer.getPlayer().getName();
		return requireNonNull(achievementMsg).replace("<name>", name).replace("<count>", Integer.toString(maximum));
	}

	private ItemStack setMegaArmor(XMaterial xMaterial) {
    	return ItemBuilder.start(requireNonNull(xMaterial.parseItem())).setUnbreakable(true)
				.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 1, true).build();
	}
	
	private void generateBlocks() {
		getArena().getProperties().destroySquares();
		Location first = getArena().getProperties().getFirstLocation();
		getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY() + 1).filter(l -> Math.random() > 0.9)
				.forEach(l -> {
					for (int i = 0; i < new Random().nextInt(4) + 2; i ++) 
						ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.IRON_BLOCK.parseItem()), l.clone().add(0, i, 0).getBlock());
				});
	}

    @Override
    public void secondStartGame() {
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		generateBlocks();
		Cuboid cuboid = getArena().getProperties().getCuboid();
		XMaterial xHelmet = getArena().isHardMode() ? XMaterial.LEATHER_HELMET : XMaterial.CHAINMAIL_HELMET;
		XMaterial xChestplate = getArena().isHardMode() ? XMaterial.LEATHER_CHESTPLATE : XMaterial.CHAINMAIL_CHESTPLATE;
		XMaterial xLeggings = getArena().isHardMode() ? XMaterial.LEATHER_LEGGINGS : XMaterial.CHAINMAIL_LEGGINGS;
		XMaterial xBoots = getArena().isHardMode() ? XMaterial.LEATHER_BOOTS : XMaterial.CHAINMAIL_BOOTS;
		ItemStack helmet = setMegaArmor(xHelmet);
		ItemStack chestplate = setMegaArmor(xChestplate);
		ItemStack leggings = setMegaArmor(xLeggings);
		ItemStack boots = setMegaArmor(xBoots);
    	getArena().getPlayers().forEach(gamePlayer -> {
    		gamePlayer.setTask("0");
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.getInventory().setHelmet(helmet);
            player.getInventory().setChestplate(chestplate);
            player.getInventory().setLeggings(leggings);
            player.getInventory().setBoots(boots);
            player.getInventory().setItem(0, XMaterial.TNT.parseItem());
            player.getInventory().setHeldItemSlot(0);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    }

    @Override
    public void check() {
		if (getTime() % 2 != 0) return;
		String bar = translate(getString("action-bar.cooldown"));
		List<GamePlayer> playerList = getArena().getPlayers();
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
    	playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		if (Integer.parseInt(gamePlayer.getTask()) > 0) gamePlayer.setTask(Integer.toString(Integer.parseInt(gamePlayer.getTask()) - 1));
    		String cooldown = cd.get(Integer.parseInt(gamePlayer.getTask()));
    		ManageHandler.getModernAPI().sendActionBar(player, translate(bar + cooldown));
    		int y = player.getLocation().getBlockY();
    		if (y <= param_y) onLose(player, true);
    	});
    }

    @Override
	public void end() {
		listTNT.forEach(Entity::remove);
		listTNT.clear();
		getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
				.forEach(gamePlayer -> onWin(gamePlayer.getPlayer(), false));
		super.end();
	}

    @Override
    public Game getGame() {
        return Game.THROW_TNT;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

	@Override
    public void aFinish(boolean forceStop) {
    	listTNT.forEach(Entity::remove);
    	listTNT.clear();
    	super.aFinish(forceStop);
    }

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.DROPPER.parseItem())).setDisplayName("&a&lTHROW THE TNT").build();
	}

	@Override
	public void event(Event event) {
    	if (event instanceof PlayerInteractEvent) playerInteractEvent(event);
    	else if (event instanceof EntityDamageEvent) entityDamageEvent(event);
		else if (event instanceof EntityExplodeEvent) entityExplode(event);
	}

	private void playerInteractEvent(Event event) {
		PlayerInteractEvent e = (PlayerInteractEvent) event;
		Player player = e.getPlayer();
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		if (gamePlayer.getState() != State.PLAYING_GAME) return;
		if (e.getItem() == null) return;
		if (!e.getItem().isSimilar(XMaterial.TNT.parseItem())) return;
		if (Integer.parseInt(gamePlayer.getTask()) != 0) return;
		Location loc = player.getLocation();
		TNTPrimed tnt = (TNTPrimed) requireNonNull(loc.getWorld()).spawnEntity(loc.add(0, 1, 0), EntityType.PRIMED_TNT);
		tnt.setFuseTicks(40);
		tnt.setYield(3.5F);
		tnt.setVelocity(player.getLocation().getDirection().normalize());
		gamePlayer.setTask("20");
		listTNT.add(tnt);
		if (gamePlayer.getAchievement() == null) gamePlayer.setAchievement("1");
		else gamePlayer.setAchievement((Integer.parseInt(gamePlayer.getAchievement()) + 1) + "");
	}

	private void entityDamageEvent(Event event) {
		EntityDamageEvent e = (EntityDamageEvent) event;
		Player player = (Player) e.getEntity();
		UUID uuid = player.getUniqueId();
		if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return;
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(uuid));
		if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
		e.setCancelled(false);
		e.setDamage(e.getDamage() * 0.7);
		if (e.getDamage() < player.getHealth()) return;
		e.setCancelled(true);
		player.setHealth(20);
		onLose(player, true);
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	private void entityExplode(Event event) {
		EntityExplodeEvent e = (EntityExplodeEvent) event;
		if (e.getEntity() == null) return;
		if (e.getEntity().getType() != EntityType.PRIMED_TNT) return;
		Location first = getArena().getProperties().getFirstLocation();
		List<Block> blocks = new ArrayList<>();
		for (int i = 0; i < e.blockList().size(); i ++) if (e.blockList().get(i).getY() <= first.getBlockY()) blocks.add(e.blockList().get(i));
		e.blockList().removeAll(blocks);
		e.setCancelled(false);
		listTNT.remove(e.getEntity());
	}

}


