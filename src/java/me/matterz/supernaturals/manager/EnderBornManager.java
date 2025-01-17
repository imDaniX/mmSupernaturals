package me.matterz.supernaturals.manager;

import me.matterz.supernaturals.SuperNPlayer;
import me.matterz.supernaturals.SupernaturalsPlugin;
import me.matterz.supernaturals.io.SNConfigHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class EnderBornManager extends ClassManager {

	public final SupernaturalsPlugin plugin;
	public final HashMap<SuperNPlayer, Boolean> teleMap = new HashMap<>();
	public final HashMap<SuperNPlayer, Integer> deathTimesMap = new HashMap<>();

	public EnderBornManager(SupernaturalsPlugin instance) {
		super();
		plugin = instance;
	}

	public boolean changeTele(SuperNPlayer snplayer) {
		if (!teleMap.containsKey(snplayer)) {
			teleMap.put(snplayer, true);
			return true;
		}
		if (!teleMap.get(snplayer)) {
			teleMap.put(snplayer, true);
			return true;
		} else {
			teleMap.put(snplayer, false);
			return false;
		}
	}

	public boolean willTele(SuperNPlayer snplayer) {
		if (!teleMap.containsKey(snplayer)) {
			return false;
		}
		return teleMap.get(snplayer);
	}

	@Override
	public void spellEvent(EntityDamageByEntityEvent event, Player target) {
		Player player = (Player) event.getDamager();
		SuperNPlayer snplayer = SuperNManager.get(player);
		ItemStack item = player.getInventory().getItemInMainHand();
		Material itemMaterial = item.getType();
		ItemStack targetItem = target.getInventory().getItemInMainHand();
		Material targetItemMaterial = targetItem.getType();
		SuperNPlayer sntarget = SuperNManager.get(target);
		if (itemMaterial == Material.ENDER_PEARL
				&& targetItemMaterial == Material.ENDER_PEARL) {
			SuperNManager.sendMessage(snplayer, "You have converted "
					+ ChatColor.WHITE + target.getName() + ChatColor.RED + "!");
			SuperNManager.sendMessage(sntarget, "An energy takes over your body...");
			SuperNManager.convert(sntarget, "enderborn");
			event.setCancelled(true);
		}
	}

	@Override
	public double damagerEvent(EntityDamageByEntityEvent event, double damage) {
		Entity damager = event.getDamager();
		Player pDamager = (Player) damager;
		SuperNPlayer snDamager = SuperNManager.get(pDamager);

		ItemStack item = pDamager.getInventory().getItemInMainHand();
		Material itemMaterial = item.getType();

		if (SNConfigHandler.enderWeapons.contains(itemMaterial)) {
			SuperNManager.sendMessage(snDamager, ChatColor.RED
					+ "EnderBorns cannot use "
					+ itemMaterial.toString().replace('_', ' '));
			return 0;
		}
		if (Math.random() == 0.35) {
			damage += damage
					* snDamager.scale(SNConfigHandler.enderDamageFactor);
			return damage;
		}
		return damage;
	}

	@Override
	public double victimEvent(EntityDamageEvent event, double damage) {
		if (event instanceof EntityDamageByEntityEvent edbeEvent) {
			Entity victim = edbeEvent.getEntity();
			if (victim instanceof Player pVictim) {
				SuperNPlayer snVictim = SuperNManager.get(pVictim);

				ItemStack item = pVictim.getInventory().getItemInMainHand();
				if (item.getType() == Material.ENDER_PEARL
						&& snVictim.getPower() > SNConfigHandler.enderProtectPower) {
					damage -= damage
							* snVictim.scale(1 - SNConfigHandler.enderDamageReceivedFactor);
					SuperNManager.alterPower(snVictim, -SNConfigHandler.enderProtectPower, "Protected by Pearl!");
					return damage;
				}
			}
		}
		return damage;
	}

	@Override
	public void deathEvent(Player player) {
		SuperNPlayer snplayer = SuperNManager.get(player);
		SuperNManager.alterPower(snplayer, -SNConfigHandler.enderDeathPowerPenalty, "You died!");
		if (!deathTimesMap.containsKey(snplayer)) {
			deathTimesMap.put(snplayer, 1);
			SuperNManager.sendMessage(snplayer, "You have "
					+ (5 - getDeathTimes(snplayer))
					+ " deaths untill you are reborn as human.");
		}
		if (deathTimesMap.get(snplayer).equals(5)) {
			SuperNManager.sendMessage(snplayer, "You have been reborn as a human.");
			SuperNManager.cure(snplayer);
			deathTimesMap.remove(snplayer);
		} else {
			deathTimesMap.put(snplayer, (deathTimesMap.get(snplayer) + 1));
			SuperNManager.sendMessage(snplayer, "You have "
					+ (5 - getDeathTimes(snplayer))
					+ " deaths untill you are reborn as human.");
		}
	}

	public int getDeathTimes(SuperNPlayer snplayer) {
		if(!deathTimesMap.containsKey(snplayer)) {
			deathTimesMap.put(snplayer, 1);
			return 1;
		} else {
			return deathTimesMap.get(snplayer);
		}
	}

	public void killEvent(SuperNPlayer damager, SuperNPlayer victim) {
		if (victim == null) {
			SuperNManager.alterPower(damager, SNConfigHandler.enderKillPower, "Stole power");
		} else {
			SuperNManager.alterPower(victim, -SNConfigHandler.enderKillPower, damager.getName()
					+ " stole some of your power!");
			SuperNManager.alterPower(damager, SNConfigHandler.enderKillPower, "Stole power from "
					+ victim.getName());
			if (Math.random() == 0.10) {
				SuperNManager.convert(victim, "enderborn");
			}
		}
	}

	@Override
	public boolean playerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();
		SuperNPlayer snplayer = SuperNManager.get(player);

		ItemStack item = player.getInventory().getItemInMainHand();
		Material itemMaterial = item.getType();
		if (action.equals(Action.RIGHT_CLICK_AIR)
				|| action.equals(Action.RIGHT_CLICK_BLOCK)) {
			if (itemMaterial == Material.ENDER_PEARL) {
				if (willTele(snplayer)) {
					return true;
				}
				SuperNManager.alterPower(snplayer, SNConfigHandler.enderPearlPower, "Taken from pearl.");
				if (item.getAmount() == 1) {
					player.getInventory().setItemInMainHand(null);
				} else {
					item.setAmount(item.getAmount() - 1);
				}
				event.setCancelled(true);
				return true;
			}
		}
		if (action.equals(Action.LEFT_CLICK_AIR)
				|| action.equals(Action.LEFT_CLICK_BLOCK)) {
			if (itemMaterial == Material.ENDER_PEARL) {
				SuperNManager.sendMessage(snplayer, "EnderPearl teleportation set to: "
						+ changeTele(snplayer));
			}
		}
		return false;
	}

}