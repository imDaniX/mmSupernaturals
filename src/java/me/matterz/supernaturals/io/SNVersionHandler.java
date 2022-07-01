package me.matterz.supernaturals.io;

import me.matterz.supernaturals.SupernaturalsPlugin;

import java.io.*;

public class SNVersionHandler {

	public static final SupernaturalsPlugin plugin = SupernaturalsPlugin.instance;
	public static final File versionFile = new File(plugin.getDataFolder(), "VERSION");

	public static void writeVersion() {
		try {
			versionFile.createNewFile();
			BufferedWriter vout = new BufferedWriter(new FileWriter(versionFile));
			vout.write(plugin.getDescription().getVersion());
			vout.close();
		} catch (IOException | SecurityException ex) {
			ex.printStackTrace();
		}
	}

	public static String readVersion() {
		byte[] buffer = new byte[(int) versionFile.length()];
		try (BufferedInputStream f = new BufferedInputStream(new FileInputStream(versionFile))) {
			f.read(buffer);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return new String(buffer);
	}
}