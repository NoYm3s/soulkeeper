package dev.soulkeeper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SoulkeeperConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("soulkeeper.json");

	private static int soulboundDamagePercent = 10;
	private static boolean protectAnchors = true;

	private SoulkeeperConfig() {
	}

	public static void load() {
		Data data = new Data();

		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
				Data loaded = GSON.fromJson(reader, Data.class);
				if (loaded != null) {
					data = loaded;
				}
			} catch (Exception exception) {
				Soulkeeper.LOGGER.warn("Could not read soulkeeper.json; using defaults.", exception);
			}
		}

		soulboundDamagePercent = Math.max(0, Math.min(100, data.soulboundDamagePercent));
		protectAnchors = data.protectAnchors;
		save();
	}

	private static void save() {
		Data data = new Data();
		data.soulboundDamagePercent = soulboundDamagePercent;
		data.protectAnchors = protectAnchors;

		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
				GSON.toJson(data, writer);
			}
		} catch (Exception exception) {
			Soulkeeper.LOGGER.warn("Could not write soulkeeper.json.", exception);
		}
	}

	public static int soulboundDamagePercent() {
		return soulboundDamagePercent;
	}

	public static boolean protectAnchors() {
		return protectAnchors;
	}

	private static final class Data {
		private int soulboundDamagePercent = 10;
		private boolean protectAnchors = true;
	}
}
