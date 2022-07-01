package me.matterz.supernaturals.io;

import me.matterz.supernaturals.SupernaturalsPlugin;

import java.io.*;

public class SNVersionHandler {

	public static SupernaturalsPlugin plugin = SupernaturalsPlugin.instance;
	public static File versionFile = new File(plugin.getDataFolder(), "VERSION");

	public static void writeVersion() {
		try {
			versionFile.createNewFile();
			BufferedWriter vout = new BufferedWriter(new FileWriter(versionFile));
			vout.write(plugin.getDescription().getVersion());
			vout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (SecurityException ex) {
			ex.printStackTrace();
		}
	}

	public static String readVersion() {
		byte[] buffer = new byte[(int) versionFile.length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(versionFile));
			f.read(buffer);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (f != null) {
				try {
					f.close();
				} catch (IOException ignored) {
				}
			}
		}

		return new String(buffer);
	}
}