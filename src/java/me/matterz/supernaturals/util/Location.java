package me.matterz.supernaturals.util;

import me.matterz.supernaturals.SupernaturalsPlugin;
import org.bukkit.World;

import java.io.Serializable;

public class Location implements Serializable {

	/**
	 * Auto-Generated serialVersionUID
	 */
	private static final long serialVersionUID = 8884729998863928105L;

	private final double x;
	private final double y;
	private final double z;
	private final String world;

	public Location(org.bukkit.Location location) {
		x = location.getX();
		y = location.getY();
		z = location.getZ();
		world = location.getWorld().getName();
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public World getWorld() {
		return SupernaturalsPlugin.instance.getServer().getWorld(world);
	}
}
