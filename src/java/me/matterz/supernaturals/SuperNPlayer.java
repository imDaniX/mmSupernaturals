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

package me.matterz.supernaturals;

import org.bukkit.Bukkit;

import java.io.Serializable;

public class SuperNPlayer implements Serializable {

	/**
	 * Auto-Generated serialVersionUID
	 */
	private static final long serialVersionUID = -2693531379993789149L;

	public String playername;
	public String superType = "human";
	public String oldSuperType = "human";
	public double oldSuperPower = 0;
	public double superPower = 0;
	public boolean truce = true;
	public int truceTimer = 0;

	public SuperNPlayer() {
	}

	public SuperNPlayer(String playername) {
		this.playername = playername;
		superType = "human";
		oldSuperType = "human";
		oldSuperPower = 0;
		superPower = 0;
		truceTimer = 0;
	}

	// -------------------------------------------- //
	// Parameters //
	// -------------------------------------------- //

	public String getName() {
		return playername;
	}

	public String getType() {
		return superType;
	}

	public void setType(String type) {
		superType = type;
	}

	public String getOldType() {
		return oldSuperType;
	}

	public void setOldType(String type) {
		oldSuperType = type;
	}

	public double getOldPower() {
		return oldSuperPower;
	}

	public void setOldPower(double amount) {
		oldSuperPower = amount;
	}

	public double getPower() {
		return superPower;
	}

	public void setPower(double amount) {
		superPower = this.limitDouble(amount);
	}

	public boolean getTruce() {
		return truce;
	}

	public void setTruce(boolean truce) {
		this.truce = truce;
		truceTimer = 0;
	}

	public int getTruceTimer() {
		return truceTimer;
	}

	public void setTruceTimer(int timer) {
		truceTimer = timer;
	}

	// -------------------------------------------- //
	// Booleans //
	// -------------------------------------------- //

	public boolean isSuper() {
		return !getType().equalsIgnoreCase("human")
				&& !getType().equalsIgnoreCase("priest")
				&& !getType().equalsIgnoreCase("witchhunter")
				&& !getType().equalsIgnoreCase("angel");
	}

	public boolean isAngel() {
		return getType().equalsIgnoreCase("angel");
	}

	public boolean isHuman() {
		return getType().equalsIgnoreCase("human");
	}

	public boolean isVampire() {
		return getType().equalsIgnoreCase("vampire");
	}

	public boolean isPriest() {
		return getType().equalsIgnoreCase("priest");
	}

	public boolean isWere() {
		return getType().equalsIgnoreCase("werewolf");
	}

	public boolean isEnderBorn() {
		return getType().equalsIgnoreCase("enderborn");
	}

	public boolean isGhoul() {
		return getType().equalsIgnoreCase("ghoul");
	}

	public boolean isHunter() {
		return getType().equalsIgnoreCase("witchhunter");
	}

	public boolean isDemon() {
		return getType().equalsIgnoreCase("demon");
	}

	public double scale(double input) {
		return input * (getPower() / 10000);
	}

	public boolean isOnline() {
		return Bukkit.getPlayer(playername) != null;
	}

	public boolean isDead() {
		return Bukkit.getPlayer(playername).isDead();
	}

	@Override
	public int hashCode() {
		return playername.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SuperNPlayer) {
			return playername.equals(((SuperNPlayer) obj).getName());
		}
		return false;
	}

	// -------------------------------------------- //
	// Limiting value of double //
	// -------------------------------------------- //
	public double limitDouble(double d, double min, double max) {
		if (d < min) {
			return min;
		}
		return Math.min(d, max);
	}

	public double limitDouble(double d) {
		return this.limitDouble(d, 0, 10000);
	}
}
