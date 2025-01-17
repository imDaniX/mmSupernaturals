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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class SNCommandCure extends SNCommand {
	public SNCommandCure() {
		super();
		requiredParameters = new ArrayList<>();
		optionalParameters = new ArrayList<>();
		senderMustBePlayer = false;
		optionalParameters.add("playername");
		permissions = "supernatural.admin.command.cure";
	}

	@Override
	public void perform() {
		if (!(sender instanceof Player senderPlayer)) {
			if (parameters.isEmpty()) {
				this.sendMessage("Missing Player!");
			} else {
				String playername = parameters.get(0);
				Player player = Bukkit.getPlayer(playername);
				if (player == null) {
					if (!SNConfigHandler.spanish) {
						this.sendMessage("Player not found.");
					} else {
						this.sendMessage("Jugador no encontrado.");
					}
					return;
				}
				this.sendMessage(ChatColor.WHITE + player.getDisplayName()
						+ ChatColor.RED + " was cured of any curse!");

				SuperNPlayer snplayer = SuperNManager.get(player);
				SuperNManager.cure(snplayer);
			}
			return;
		}
		if (!SupernaturalsPlugin.hasPermissions(senderPlayer, permissions)) {
			if (!SNConfigHandler.spanish) {
				this.sendMessage("You do not have permissions to use this command.");
			} else {
				this.sendMessage("No tienes permiso para este comando.");
			}
			return;
		}

		if (parameters.isEmpty()) {
			SuperNPlayer snplayer = SuperNManager.get(senderPlayer);
			SuperNManager.cure(snplayer);
		} else {
			String playername = parameters.get(0);
			Player player = Bukkit.getPlayer(playername);
			if (player == null) {
				if (!SNConfigHandler.spanish) {
					this.sendMessage("Player not found.");
				} else {
					this.sendMessage("Jugador no encontrado.");
				}
				return;
			}
			this.sendMessage(ChatColor.WHITE + player.getDisplayName()
					+ ChatColor.RED + " was cured of any curse!");

			SuperNPlayer snplayer = SuperNManager.get(player);
			SuperNManager.cure(snplayer);
		}
	}
}