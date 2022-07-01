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

package me.matterz.supernaturals.commands;

import me.matterz.supernaturals.SuperNPlayer;
import me.matterz.supernaturals.SupernaturalsPlugin;
import me.matterz.supernaturals.io.SNConfigHandler;
import me.matterz.supernaturals.manager.SuperNManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class SNCommandPower extends SNCommand {

	public SNCommandPower() {
		super();
		requiredParameters = new ArrayList<>();
		optionalParameters = new ArrayList<>();
		senderMustBePlayer = true;
		senderMustBeSupernatural = true;
		permissions = "supernatural.command.power";
		optionalParameters.add("playername");
		optionalParameters.add("power");
		helpNameAndParams = "power [amount] | power [playername] [amount]";
		helpDescription = "See current power level";
	}

	@Override
	public void perform() {

		Player senderPlayer = (Player) sender;
		String permissions2 = "supernatural.admin.command.power";

		if (SNConfigHandler.spanish) {
			if (parameters.isEmpty()) {
				if (!SupernaturalsPlugin.hasPermissions(senderPlayer, permissions)) {
					this.sendMessage("No tienes permiso para este comando.");
					return;
				}
				SuperNPlayer snplayer = SuperNManager.get(senderPlayer);

				this.sendMessage("Tu eres un " + ChatColor.WHITE
						+ snplayer.getType() + ChatColor.RED
						+ " y tus Poderes actuales son: " + ChatColor.WHITE
						+ (int) snplayer.getPower());
			} else {
				if (!SupernaturalsPlugin.hasPermissions(senderPlayer, permissions2)) {
					this.sendMessage("No tienes permiso para usar este comando.");
					return;
				}
				if (parameters.size() == 1) {
					double powerGain;

					try {
						powerGain = Double.parseDouble(parameters.get(0));
					} catch (NumberFormatException e) {
						this.sendMessage("N�mero invalido.");
						return;
					}
					if (powerGain >= 10000D) {
						powerGain = 9999;
					}

					SuperNPlayer snplayer = SuperNManager.get(senderPlayer);
					SuperNManager.alterPower(snplayer, powerGain, "Admin boost!");
				} else {
					String playername = parameters.get(0);
					Player player = SupernaturalsPlugin.instance.getServer().getPlayer(playername);
					if (player == null) {
						this.sendMessage("Jugador no encontrado!");
						return;
					}
					double powerGain;

					try {
						powerGain = Double.parseDouble(parameters.get(1));
					} catch (NumberFormatException e) {
						this.sendMessage("N�mero invalido.");
						return;
					}
					if (powerGain >= 10000D) {
						powerGain = 9999;
					}
					this.sendMessage(ChatColor.WHITE + player.getDisplayName()
							+ ChatColor.RED + " ha sido aumentado de Poderes!");
					SuperNPlayer snplayer = SuperNManager.get(player);
					SuperNManager.alterPower(snplayer, powerGain, "Admin boost!");
				}
			}
		} else {
			if (parameters.isEmpty()) {
				if (!SupernaturalsPlugin.hasPermissions(senderPlayer, permissions)) {
					this.sendMessage("You do not have permissions to use this command.");
					return;
				}
				SuperNPlayer snplayer = SuperNManager.get(senderPlayer);

				this.sendMessage("You are a " + ChatColor.WHITE
						+ snplayer.getType() + ChatColor.RED
						+ " and your current power level is: "
						+ ChatColor.WHITE + (int) snplayer.getPower());
			} else {
				if (!SupernaturalsPlugin.hasPermissions(senderPlayer, permissions2)) {
					this.sendMessage("You do not have permissions to use this command.");
					return;
				}
				if (parameters.size() == 1) {
					double powerGain;

					try {
						powerGain = Double.parseDouble(parameters.get(0));
					} catch (NumberFormatException e) {
						this.sendMessage("Invalid Number.");
						return;
					}
					if (powerGain >= 10000D) {
						powerGain = 9999;
					}

					SuperNPlayer snplayer = SuperNManager.get(senderPlayer);
					SuperNManager.alterPower(snplayer, powerGain, "Admin boost!");
				} else {
					String playername = parameters.get(0);
					Player player = SupernaturalsPlugin.instance.getServer().getPlayer(playername);
					if (player == null) {
						this.sendMessage("Player not found!");
						return;
					}
					double powerGain;

					try {
						powerGain = Double.parseDouble(parameters.get(1));
					} catch (NumberFormatException e) {
						this.sendMessage("Invalid Number.");
						return;
					}
					if (powerGain >= 10000D) {
						powerGain = 9999;
					}
					this.sendMessage(ChatColor.WHITE + player.getDisplayName()
							+ ChatColor.RED + " has been powered up!");
					SuperNPlayer snplayer = SuperNManager.get(player);
					SuperNManager.alterPower(snplayer, powerGain, "Admin boost!");
				}
			}
		}
	}
}