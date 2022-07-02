/*
 * Supernatural Players Plugin for Bukkit
 * Copyright (C) 2011  Matt Walker <mmw167@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package me.matterz.supernaturals.manager;

import me.matterz.supernaturals.SuperNPlayer;
import me.matterz.supernaturals.SupernaturalsPlugin;
import me.matterz.supernaturals.io.SNConfigHandler;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PriestManager extends HumanManager {

	public PriestManager() {
		super();
	}

	// -------------------------------------------- //
	// Damage Events //
	// -------------------------------------------- //

	@Override
	public double victimEvent(EntityDamageEvent event, double damage) {
		return damage;
	}

	@Override
	public double damagerEvent(EntityDamageByEntityEvent event, double damage) {
		Player pDamager = (Player) event.getDamager();
		Entity victim = event.getEntity();

		SuperNPlayer snDamager = SuperNManager.get(pDamager);
		ItemStack item = pDamager.getInventory().getItemInMainHand();

		if (SNConfigHandler.priestWeapons.contains(item.getType())) {
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(pDamager.getName()
						+ " was not allowed to use "
						+ item.getType());
			}
			SuperNManager.sendMessage(snDamager, "Priests cannot use this weapon!");
			return 0;
		}

		if (victim instanceof Animals && !(victim instanceof Wolf)) {
			SuperNManager.sendMessage(SuperNManager.get(pDamager), "You cannot hurt innocent animals.");
			damage = 0;
		} else if (victim instanceof Player pVictim) {
			if (!SupernaturalsPlugin.instance.getPvP(pVictim)) {
				return damage;
			}
			SuperNPlayer snvictim = SuperNManager.get(pVictim);
			if (snvictim.isSuper()) {
				if (!snvictim.isDemon()) {
					pVictim.setFireTicks(SNConfigHandler.priestFireTicks);
				}
				damage += damage
						* SuperNManager.get(pDamager).scale(SNConfigHandler.priestDamageFactorAttackSuper);
			} else {
				damage += damage
						* SuperNManager.get(pDamager).scale(SNConfigHandler.priestDamageFactorAttackHuman);
			}
		} else if (victim instanceof Monster mVictim) {
			mVictim.setFireTicks(SNConfigHandler.priestFireTicks);
		}
		return damage;
	}

	@Override
	public void deathEvent(Player player) {
		super.deathEvent(player);
		SuperNPlayer snplayer = SuperNManager.get(player);
		SuperNManager.alterPower(snplayer, -SNConfigHandler.priestDeathPowerPenalty, "You died!");
	}

	// -------------------------------------------- //
	// Interact //
	// -------------------------------------------- //

	@Override
	public boolean playerInteract(PlayerInteractEvent event) {

		Action action = event.getAction();
		Player player = event.getPlayer();
		SuperNPlayer snplayer = SuperNManager.get(player);
		Material itemMaterial = event.getMaterial();

		if (action.equals(Action.LEFT_CLICK_AIR)
				|| action.equals(Action.LEFT_CLICK_BLOCK)) {
			if (itemMaterial == Material.BOWL) {
				if (SNConfigHandler.debugMode) {
					SupernaturalsPlugin.log(snplayer.getName()
							+ " is attempting to donate remotely.");
				}
				remoteDonations(player);
				return true;
			}
		}

		return false;
	}

	// -------------------------------------------- //
	// Armor //
	// -------------------------------------------- //

	// -------------------------------------------- //
	// Church //
	// -------------------------------------------- //

	public void useAltar(Player player) {
		Location location = player.getLocation();
		World world = location.getWorld();
		int locX = location.getBlockX();
		int locY = location.getBlockY();
		int locZ = location.getBlockZ();
		SuperNPlayer snplayer = SuperNManager.get(player);
		int amount = 0;
		int delta = 0;
		if (world.getName().equalsIgnoreCase(SNConfigHandler.priestChurchWorld)) {
			if (Math.abs(locX - SNConfigHandler.priestChurchLocationX) <= 10) {
				if (Math.abs(locY - SNConfigHandler.priestChurchLocationY) <= 10) {
					if (Math.abs(locZ - SNConfigHandler.priestChurchLocationZ) <= 10) {
						if (snplayer.isPriest()) {
							if (player.getInventory().getItemInMainHand().getType() == Material.COAL) {
								SuperNManager.sendMessage(snplayer, "The Church excommunicates you!");
								SuperNManager.cure(snplayer);
							} else {
								PlayerInventory inv = player.getInventory();
								ItemStack[] items = inv.getContents();
								for (Material mat : SNConfigHandler.priestDonationMap.keySet()) {
									for (ItemStack itemStack : items) {
										if (itemStack != null) {
											if (itemStack.getType().equals(mat)) {
												amount += itemStack.getAmount();
											}
										}
									}
									delta += amount
											* SNConfigHandler.priestDonationMap.get(mat);
									amount = 0;
								}
								for (Material mat : SNConfigHandler.priestDonationMap.keySet()) {
									inv.remove(mat);
								}
								player.updateInventory();
								SuperNManager.sendMessage(snplayer, "The Church accepts your gracious donations of Bread, Fish, Grilled Pork and Apples.");
								SuperNManager.alterPower(snplayer, delta, "Donations!");
							}
						} else {
							SuperNManager.sendMessage(snplayer, "The Church Altar radiates holy power.");
							if (snplayer.isSuper()) {
								SuperNManager.sendMessage(snplayer, "The holy power of the Church tears you asunder!");
								EntityDamageEvent event = new EntityDamageEvent(player, DamageCause.BLOCK_EXPLOSION, 20);
								player.setLastDamageCause(event);
								player.setHealth(0);
								if (snplayer.isGhoul()) {
									double random = Math.random();
									if (random < SNConfigHandler.spreadChance - 0.1) {
										SuperNManager.cure(snplayer);
									}
								}
								return;
							}
							if (SNConfigHandler.priestAltarRecipe.playerHasEnough(player)) {
								if(!SupernaturalsPlugin.hasPermissions(player, "supernatural.player.shrineuse.priest")) {
									SuperNManager.sendMessage(snplayer, "You cannot use priest altars.");
									return;
								}
								SuperNManager.sendMessage(snplayer, "You donate these items to the Church:");
								SuperNManager.sendMessage(snplayer, SNConfigHandler.priestAltarRecipe.getRecipeLine());
								SuperNManager.sendMessage(snplayer, "The Church recognizes your holy spirit and accepts you into the priesthood.");
								SNConfigHandler.priestAltarRecipe.removeFromPlayer(player);
								SuperNManager.convert(snplayer, "priest", SNConfigHandler.priestPowerStart);
							} else {
								SuperNManager.sendMessage(snplayer, "The Church judges your intended donate insufficient.  You must gather the following: ");
								SuperNManager.sendMessage(snplayer, SNConfigHandler.priestAltarRecipe.getRecipeLine());
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void remoteDonations(Player player) {
		SuperNPlayer snplayer = SuperNManager.get(player);
		PlayerInventory inv = player.getInventory();
		ItemStack[] items = inv.getContents();
		double delta = 0;
		invCheck: for (Material mat : SNConfigHandler.priestDonationMap.keySet()) {
			for (ItemStack itemStack : items) {
				if (itemStack != null) {
					if (itemStack.getType().equals(mat)) {
						delta = SNConfigHandler.priestDonationMap.get(mat);
						if (itemStack.getAmount() == 1) {
							inv.clear(inv.first(itemStack.getType()));
						} else {
							itemStack.setAmount(itemStack.getAmount() - 1);
						}
						break invCheck;
					}
				}
			}
		}
		if (delta == 0) {
			SuperNManager.sendMessage(snplayer, "The Church only accepts donations of Bread, Fish, Grilled Pork and Apples.");
		} else {
			player.updateInventory();
			SuperNManager.sendMessage(snplayer, "You receive some power for your remote donations.");
			SuperNManager.alterPower(snplayer, delta * .5, "Donations!");
		}
	}

	// -------------------------------------------- //
	// Spells //
	// -------------------------------------------- //

	@Override
	public void spellEvent(EntityDamageByEntityEvent event, Player target) {
		Player player = (Player) event.getDamager();
		SuperNPlayer snplayer = SuperNManager.get(player);
		Material itemMaterial = player.getInventory().getItemInMainHand().getType();

		boolean cancelled = false;

		if (SNConfigHandler.priestSpellMaterials.contains(itemMaterial)) {
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(snplayer.getName()
						+ " is attempting to cast a spell...");
			}
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(target.getName()
						+ " is targetted by spell.");
			}
			if (itemMaterial.equals(SNConfigHandler.priestSpellMaterials.get(0))) {
				banish(player, target);
				cancelled = true;
			} else if (itemMaterial.equals(SNConfigHandler.priestSpellMaterials.get(1))) {
				exorcise(player, target);
				cancelled = true;
			} else if (itemMaterial.equals(SNConfigHandler.priestSpellMaterials.get(2))) {
				cancelled = cure(player, target, itemMaterial);
			} else if (itemMaterial.equals(SNConfigHandler.priestSpellMaterials.get(3))) {
				cancelled = heal(player, target);
			} else if (itemMaterial.equals(SNConfigHandler.priestSpellMaterials.get(4))) {
				drainPower(player, target);
				cancelled = true;
			}
			if (!event.isCancelled()) {
				event.setCancelled(cancelled);
			}
		} else if (itemMaterial.toString().equalsIgnoreCase(SNConfigHandler.priestSpellGuardianAngel)) {
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(snplayer.getName()
						+ " is attempting to cast guardian angel...");
			}
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(target.getName()
						+ " is targetted by guardian angel.");
			}
			cancelled = guardianAngel(player, target);
			if (!event.isCancelled()) {
				event.setCancelled(cancelled);
			}
		} else if (itemMaterial == Material.BOWL) {
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(snplayer.getName()
						+ " is attempting to donate remotely.");
			}
			remoteDonations(player);
		}
	}

	public void banish(Player player, Player victim) {
		SuperNPlayer snplayer = SuperNManager.get(player);
		SuperNPlayer snvictim = SuperNManager.get(victim);
		if (!SupernaturalsPlugin.instance.getPvP(victim)) {
			SuperNManager.sendMessage(snplayer, "Cannot cast in a non-PvP zone.");
			return;
		}
		if (snplayer.getPower() > SNConfigHandler.priestPowerBanish) {
			if (snvictim.isSuper()) {
				SuperNManager.alterPower(snplayer, -SNConfigHandler.priestPowerBanish, "Banished "
						+ victim.getName());
				SuperNManager.sendMessage(snvictim, "You were banished by "
						+ ChatColor.WHITE + snplayer.getName() + ChatColor.RED
						+ "!");
				victim.teleport(SNConfigHandler.priestBanishLocation);
				player.getInventory().getItemInMainHand().subtract();
				return;
			}
			SuperNManager.sendMessage(snplayer, "Can only banish supernatural players.");
		} else {
			SuperNManager.sendMessage(snplayer, "Not enough power to banish.");
		}
	}

	public boolean heal(Player player, Player victim) {
		SuperNPlayer snplayer = SuperNManager.get(player);
		SuperNPlayer snvictim = SuperNManager.get(victim);
		if (snplayer.getPower() > SNConfigHandler.priestPowerHeal) {
			if (!snvictim.isSuper() && victim.getHealth() < 20
					&& !victim.isDead()) {
				SuperNManager.alterPower(snplayer, -SNConfigHandler.priestPowerHeal, "Healed "
						+ victim.getName());
				SuperNManager.sendMessage(snvictim, "You were healed by "
						+ ChatColor.WHITE + snplayer.getName() + ChatColor.RED
						+ "!");
				double health = victim.getHealth()
						+ SNConfigHandler.priestHealAmount;
				if (health > 20) {
					health = 20;
				}
				victim.setHealth(health);
				player.getInventory().getItemInMainHand().subtract();
				return true;
			} else {
				SuperNManager.sendMessage(snplayer, "Player cannot be healed.");
				return false;
			}
		} else {
			SuperNManager.sendMessage(snplayer, "Not enough power to heal.");
			return false;
		}
	}

	public void exorcise(Player player, Player victim) {
		SuperNPlayer snplayer = SuperNManager.get(player);
		SuperNPlayer snvictim = SuperNManager.get(victim);
		if (!SupernaturalsPlugin.instance.getPvP(victim)) {
			SuperNManager.sendMessage(snplayer, "Cannot cast in a non-PvP zone.");
			return;
		}
		if (snplayer.getPower() > SNConfigHandler.priestPowerExorcise) {
			if (snvictim.isSuper()) {
				SuperNManager.alterPower(snplayer, -SNConfigHandler.priestPowerExorcise, "Exorcised "
						+ victim.getName());
				SuperNManager.sendMessage(snvictim, "You were exorcised by "
						+ ChatColor.WHITE + snplayer.getName() + ChatColor.RED
						+ "!");
				SuperNManager.cure(snvictim);
				player.getInventory().getItemInMainHand().subtract();
			} else {
				SuperNManager.sendMessage(snplayer, "Only supernatural players can be exorcised.");
			}
		} else {
			SuperNManager.sendMessage(snplayer, "Not enough power to exorcise.");
		}
	}

	public boolean cure(Player player, Player victim, Material material) {
		SuperNPlayer snplayer = SuperNManager.get(player);
		SuperNPlayer snvictim = SuperNManager.get(victim);
		if (snplayer.getPower() > SNConfigHandler.priestPowerCure) {
			if (snvictim.isSuper()) {
				if (victim.getInventory().getItemInMainHand().getType() == material) {
					SuperNManager.alterPower(snplayer, -SNConfigHandler.priestPowerCure, "Cured "
							+ victim.getName());
					SuperNManager.sendMessage(snvictim, ChatColor.WHITE
							+ snplayer.getName() + ChatColor.RED
							+ " has restored your humanity");
					SuperNManager.cure(snvictim);
					player.getInventory().getItemInMainHand().subtract();
					victim.getInventory().getItemInMainHand().subtract();
					return true;
				} else {
					SuperNManager.sendMessage(snplayer, ChatColor.WHITE
							+ snvictim.getName() + ChatColor.RED
							+ " is not holding " + ChatColor.WHITE
							+ material.toString() + ChatColor.RED + ".");
					return false;
				}
			} else {
				SuperNManager.sendMessage(snplayer, "You can only cure supernatural players.");
				return false;
			}
		} else {
			SuperNManager.sendMessage(snplayer, "Not enough power to cure.");
			return false;
		}
	}

	public void drainPower(Player player, Player victim) {
		SuperNPlayer snplayer = SuperNManager.get(player);
		SuperNPlayer snvictim = SuperNManager.get(victim);
		if (!SupernaturalsPlugin.instance.getPvP(victim)) {
			SuperNManager.sendMessage(snplayer, "Cannot cast in a non-PvP zone.");
			return;
		}
		if (snplayer.getPower() > SNConfigHandler.priestPowerDrain) {
			if (snvictim.isSuper()) {
				double power = snvictim.getPower();
				power *= SNConfigHandler.priestDrainFactor;
				SuperNManager.alterPower(snplayer, -SNConfigHandler.priestPowerDrain, "Drained  "
						+ snvictim.getName() + "'s power!");
				SuperNManager.alterPower(snvictim, -power, "Drained by "
						+ snplayer.getName());
				player.getInventory().getItemInMainHand().subtract();
			} else {
				SuperNManager.sendMessage(snplayer, "Only supernatural players can be power drained.");
			}
		} else {
			SuperNManager.sendMessage(snplayer, "Not enough power to drain power.");
		}
	}

	public boolean guardianAngel(Player player, Player victim) {
		SuperNPlayer priest = SuperNManager.get(player);
		SuperNPlayer snvictim = SuperNManager.get(victim);

		if (priest.getPower() > SNConfigHandler.priestPowerGuardianAngel) {
			if (!snvictim.isSuper()) {
				if (SupernaturalsPlugin.instance.getDataHandler().hasAngel(priest)) {
					SuperNManager.sendMessage(priest, "Removed Guardian Angel from "
							+ ChatColor.WHITE
							+ SupernaturalsPlugin.instance.getDataHandler().getAngelPlayer(priest).getName());
					SuperNManager.sendMessage(SupernaturalsPlugin.instance.getDataHandler().getAngelPlayer(priest), "Guardian Angel removed!");
					SupernaturalsPlugin.instance.getDataHandler().removeAngel(priest);
				}
				SuperNManager.sendMessage(snvictim, "You now have a Guardian Angel!");
				SuperNManager.alterPower(priest, -SNConfigHandler.priestPowerGuardianAngel, "Guardian Angel on "
						+ ChatColor.WHITE
						+ snvictim.getName()
						+ ChatColor.RED
						+ "!");
				SupernaturalsPlugin.instance.getDataHandler().addAngel(priest, snvictim);

				player.getInventory().getItemInMainHand().subtract();
				return true;
			}
			SuperNManager.sendMessage(priest, "You cannot set a Guardian Angel on a Supernatural player.");
		} else {
			SuperNManager.sendMessage(priest, "Not enough power to cast Guardian Angel.");
		}
		return false;
	}
}