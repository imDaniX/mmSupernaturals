package me.matterz.supernaturals.manager;

import me.matterz.supernaturals.SuperNPlayer;
import me.matterz.supernaturals.SupernaturalsPlugin;
import me.matterz.supernaturals.io.SNConfigHandler;
import me.matterz.supernaturals.util.EntityUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class AngelManager extends ClassManager {

	public AngelManager() {
		super();
	}

	@Override
	public double damagerEvent(EntityDamageByEntityEvent event, double damage) {
		Player player = (Player) event.getDamager();
		SuperNPlayer snplayer = SuperNManager.get(player);
		if (event.getEntity() instanceof Animals) {
			if (player.getInventory().getItemInMainHand().getType() == Material.DIAMOND_SWORD) {
				SuperNManager.sendMessage(snplayer, "Angels cannot use diamond swords on animals!");
				event.setCancelled(true);
				return 0;
			}
		}
		return damage;
	}

	@Override
	public void spellEvent(EntityDamageByEntityEvent event, Player target) {
		Player player = (Player) event.getDamager();
		SuperNPlayer snplayer = SuperNManager.get(player);
		SuperNPlayer sntarget = SuperNManager.get(target);
		Material itemType = player.getInventory().getItemInMainHand().getType();
		if (itemType == Material.FEATHER) {
			if (snplayer.getPower() > SNConfigHandler.angelHealPowerCost) {
				EntityUtil.setHealth(target, target.getHealth() + SNConfigHandler.angelHealHealthGain);
				SuperNManager.alterPower(snplayer, -SNConfigHandler.angelHealPowerCost, "Healing "
						+ target.getName());
				target.sendMessage(ChatColor.RED + "Healed by "
						+ player.getName());
			} else {
				SuperNManager.sendMessage(snplayer, "Not enough power to heal!");
			}
		} else if (itemType == Material.PAPER) {
			if (snplayer.getPower() > SNConfigHandler.angelCurePowerCost) {
				if (sntarget.isSuper()) {
					SuperNManager.cure(sntarget);
					target.sendMessage(ChatColor.RED + "Cured by "
							+ player.getName());
					SuperNManager.alterPower(snplayer, -SNConfigHandler.angelCurePowerCost, "Cured "
							+ target.getName());
				} else {
					SuperNManager.sendMessage(snplayer, "Player is not a supernatural!");
				}
			} else {
				SuperNManager.sendMessage(snplayer, "Not enough power!");
			}
		}
	}

	@Override
	public boolean playerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();
		Material itemInHandMaterial = player.getInventory().getItemInMainHand().getType();
		SuperNPlayer snplayer = SuperNManager.get(player);
		if (action.equals(Action.LEFT_CLICK_AIR)) {
			if (itemInHandMaterial == Material.DANDELION) {
				if (snplayer.getPower() > SNConfigHandler.angelJumpPowerCost) {
					jump(player, SNConfigHandler.angelJumpDeltaSpeed);
				} else {
					SuperNManager.sendMessage(snplayer, "Not enough power to jump!");
				}
			}
			return false;
		}
		if (action.equals(Action.LEFT_CLICK_BLOCK)) {
			Block targetBlock = player.getTargetBlock(null, 20);
			Location targetBlockLocation = targetBlock.getLocation();
			switch (itemInHandMaterial) {
				case BONE -> {
					Wolf spawnedWolf = (Wolf) player.getWorld().spawnEntity(targetBlockLocation, EntityType.WOLF);
					spawnedWolf.setTamed(true);
					spawnedWolf.setOwner(player);
					spawnedWolf.setHealth(20);
					event.setCancelled(true);
					SuperNManager.alterPower(snplayer, -SNConfigHandler.angelSummonPowerCost, "Summoned wolf.");
					return true;
				}
				case SALMON, COD -> {
					Cat spawnedCat = (Cat) player.getWorld().spawnEntity(targetBlockLocation, EntityType.CAT);
					spawnedCat.setTamed(true);
					spawnedCat.setOwner(player);
					spawnedCat.setHealth(20);
					spawnedCat.setCatType(Cat.Type.values()[ThreadLocalRandom.current().nextInt(Cat.Type.values().length)]);
					event.setCancelled(true);
					SuperNManager.alterPower(snplayer, -SNConfigHandler.angelSummonPowerCost, "Summoned cat.");
					return true;
				}
				case BEEF -> {
					player.getWorld().spawnEntity(targetBlockLocation, EntityType.COW);
					event.setCancelled(true);
					SuperNManager.alterPower(snplayer, -SNConfigHandler.angelSummonPowerCost, "Summoned cow.");
					return true;
				}
				case PORKCHOP -> {
					player.getWorld().spawnEntity(targetBlockLocation, EntityType.PIG);
					event.setCancelled(true);
					SuperNManager.alterPower(snplayer, -SNConfigHandler.angelSummonPowerCost, "Summoned pig.");
					return true;
				}
				case MUTTON -> {
					player.getWorld().spawnEntity(targetBlockLocation, EntityType.SHEEP);
					event.setCancelled(true);
					SuperNManager.alterPower(snplayer, -SNConfigHandler.angelSummonPowerCost, "Summoned sheep.");
					return true;
				}
				case CHICKEN -> {
					player.getWorld().spawnEntity(targetBlockLocation, EntityType.CHICKEN);
					event.setCancelled(true);
					SuperNManager.alterPower(snplayer, -SNConfigHandler.angelSummonPowerCost, "Summoned chicken.");
					return true;
				}
				case RABBIT -> {
					player.getWorld().spawnEntity(targetBlockLocation, EntityType.RABBIT);
					event.setCancelled(true);
					SuperNManager.alterPower(snplayer, -SNConfigHandler.angelSummonPowerCost, "Summoned rabbit.");
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public double victimEvent(EntityDamageEvent event, double damage) {
		if (event.getCause().equals(DamageCause.FALL)) {
			event.setCancelled(true);
			return 0;
		}
		return damage;
	}

	public static void jump(Player player, double deltaSpeed) {
		SuperNPlayer snplayer = SuperNManager.get(player);

		if (snplayer.getPower() < SNConfigHandler.angelJumpPowerCost) {
			SuperNManager.sendMessage(snplayer, "Not enough Power to jump.");
			return;
		} else {
			SuperNManager.alterPower(snplayer, -SNConfigHandler.angelJumpPowerCost, "SuperJump!");
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(snplayer.getName() + " used jump!");
			}
		}

		Vector vjadd;
		vjadd = new Vector(0, 1, 0);
		vjadd.multiply(deltaSpeed);

		player.setVelocity(player.getVelocity().add(vjadd));
	}

	@Override
	public void armorCheck(Player player) {
		PlayerInventory inv = player.getInventory();
		ItemStack helmet = inv.getHelmet();
		ItemStack chest = inv.getChestplate();
		ItemStack leggings = inv.getLeggings();
		ItemStack boots = inv.getBoots();

		if (helmet != null) {
			if (helmet.getType() == Material.DIAMOND_HELMET
					|| helmet.getType() == Material.GOLDEN_HELMET) {
				inv.setHelmet(null);
				dropItem(player, helmet);
			}
		}
		if (chest != null) {
			if (chest.getType() == Material.DIAMOND_CHESTPLATE
					|| chest.getType() == Material.GOLDEN_CHESTPLATE) {
				inv.setChestplate(null);
				dropItem(player, chest);
			}
		}
		if (leggings != null) {
			if (leggings.getType() == Material.DIAMOND_LEGGINGS
					|| leggings.getType() == Material.GOLDEN_LEGGINGS) {
				inv.setLeggings(null);
				dropItem(player, leggings);
			}
		}
		if (boots != null) {
			if (boots.getType() == Material.DIAMOND_BOOTS
					|| boots.getType() == Material.GOLDEN_BOOTS) {
				inv.setBoots(null);
				dropItem(player, boots);
			}
		}
	}

	public void waterAdvanceTime(Player player) {
		if (player.isDead()) {
			return;
		}

		if (player.isInsideVehicle()) {
			if (player.getVehicle() instanceof Boat) {
				return;
			}
		}

		SuperNPlayer snplayer = SuperNManager.get(player);

		Material material = player.getLocation().getBlock().getType();

		if (material == Material.WATER) {
			SuperNManager.alterPower(snplayer, SNConfigHandler.angelSwimPowerGain, "Swimming in water");
		}
	}

}