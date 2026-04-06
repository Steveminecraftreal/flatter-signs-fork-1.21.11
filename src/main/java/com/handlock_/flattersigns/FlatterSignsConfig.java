package com.handlock_.flattersigns;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON config for Flatter Signs.
 *
 * The file is generated at:
 *   config/flattersigns.json
 *
 * Each option is intentionally "boring": primitives only, no nested objects,
 * so users can easily edit it by hand.
 */
public final class FlatterSignsConfig {

    private static final String FILE_NAME = "flattersigns.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static boolean loaded = false;
    private static FlatterSignsConfig INSTANCE;

    // Toggles -------------------------------------------------------------
    /** Front-only edit / read behaviour for signs. */
    public boolean frontOnlyEdit = true;

    /**
     * Use flat cross / plate models and blank BER, instead of
     * vanilla entity rendering.
     */
    public boolean flatModelRendering = true;

    /** Hitbox / outline tweaks for signs and hanging signs. */
    public boolean hitboxTweaks = true;

    /**
     * Only edit when crouching (or when empty) and print text to chat on
     * normal right-click. This toggle controls both behaviours together.
     */
    public boolean crouchEditAndChat = true;

    /** Make the default sign text color white instead of black. */
    public boolean defaultWhiteText = true;

    /**
     * Make glowing signs render at full-bright and force a block update so the
     * change is visible immediately (especially on Forge+Sinytra).
     */
    public boolean glowInkLighting = true;

    /**
     * Wall sign texture crop offset (in pixels, 0..16).
     *
     * This shifts the cropped window down inside the sign item sprite.
     * Useful for resource packs that move the board/stem vertically.
     *
     * Value is clamped so (offset + wall_sign_texture_crop_height) never exceeds 16.
     *
     * Default is 0.
     */
    public int wallSignTextureCropOffset = 0;

    /**
     * Wall sign texture crop height (in pixels, 1..16).
     *
     * This is how many pixels (starting at wall_sign_texture_crop_offset) of the
     * vanilla sign ITEM texture the flat wall-sign model will display.
     *
     * Lower values cut off more of the bottom stem, which helps with resource packs
     * that change the stem length.
     *
     * Default is 11 (vanilla stem trim of 5px).
     */
    public int wallSignTextureCropHeight = 11;

    // Public API ----------------------------------------------------------

    /** Loads (or creates) the config file once. Safe to call multiple times. */
    public static void load() {
        if (loaded) {
            return;
        }
        loaded = true;

        FlatterSignsConfig cfg = new FlatterSignsConfig();
        Path path = getPath();

        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

                cfg.frontOnlyEdit = getBoolean(obj, "front_only_edit", cfg.frontOnlyEdit);
                cfg.flatModelRendering = getBoolean(obj, "flat_model_rendering", cfg.flatModelRendering);
                cfg.hitboxTweaks = getBoolean(obj, "hitbox_tweaks", cfg.hitboxTweaks);
                cfg.crouchEditAndChat = getBoolean(obj, "crouch_edit_and_chat", cfg.crouchEditAndChat);
                cfg.defaultWhiteText = getBoolean(obj, "default_white_text", cfg.defaultWhiteText);
                cfg.glowInkLighting = getBoolean(obj, "glow_ink_lighting", cfg.glowInkLighting);

                cfg.wallSignTextureCropHeight = getInt(
                        obj,
                        "wall_sign_texture_crop_height",
                        cfg.wallSignTextureCropHeight,
                        1,
                        16
                );

                cfg.wallSignTextureCropOffset = getInt(
                        obj,
                        "wall_sign_texture_crop_offset",
                        cfg.wallSignTextureCropOffset,
                        0,
                        16
                );
            } catch (Exception e) {
                // If the file is malformed, keep defaults and overwrite it.
                FlatterSigns.LOGGER.warn("Failed to read config file '{}'. Using defaults and rewriting it.", path, e);
            }
        }

        INSTANCE = cfg;

        // Always write back so the file exists and includes new keys after updates.
        try {
            write(cfg);
        } catch (IOException e) {
            FlatterSigns.LOGGER.warn("Failed to write config file '{}'.", path, e);
        }
    }

    public static boolean isFrontOnlyEditEnabled() {
        return get().frontOnlyEdit;
    }

    public static boolean isFlatModelRenderingEnabled() {
        return get().flatModelRendering;
    }

    public static boolean isHitboxTweaksEnabled() {
        return get().hitboxTweaks;
    }

    public static boolean isCrouchEditAndChatEnabled() {
        return get().crouchEditAndChat;
    }

    public static boolean isDefaultWhiteTextEnabled() {
        return get().defaultWhiteText;
    }

    public static boolean isGlowInkLightingEnabled() {
        return get().glowInkLighting;
    }

    /**
     * @return wall sign crop offset in pixels (clamped so offset + height <= 16)
     */
    public static int getWallSignTextureCropOffset() {
        int height = getWallSignTextureCropHeight();
        int v = get().wallSignTextureCropOffset;
        if (v < 0) v = 0;
        if (v > 16) v = 16;
        int max = 16 - height;
        if (v > max) v = max;
        return v;
    }

    /**
     * @return wall sign crop height in pixels (clamped 1..16)
     */
    public static int getWallSignTextureCropHeight() {
        int v = get().wallSignTextureCropHeight;
        if (v < 1) return 1;
        if (v > 16) return 16;
        return v;
    }

    // Internal helpers ---------------------------------

    private static FlatterSignsConfig get() {
        if (!loaded) {
            load();
        }
        return INSTANCE;
    }

    private static Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    private static boolean getBoolean(JsonObject obj, String key, boolean def) {
        try {
            if (obj.has(key) && obj.get(key).isJsonPrimitive()) {
                return obj.get(key).getAsBoolean();
            }
        } catch (Exception ignored) {}
        return def;
    }

    private static int getInt(JsonObject obj, String key, int def, int min, int max) {
        try {
            if (obj.has(key) && obj.get(key).isJsonPrimitive()) {
                int v = obj.get(key).getAsInt();
                if (v < min) v = min;
                if (v > max) v = max;
                return v;
            }
        } catch (Exception ignored) {}
        return def;
    }

    private static void write(FlatterSignsConfig cfg) throws IOException {
        Path path = getPath();
        Files.createDirectories(path.getParent());

        JsonObject obj = toJson(cfg);
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(obj, writer);
        }
    }

    private static JsonObject toJson(FlatterSignsConfig cfg) {
        JsonObject obj = new JsonObject();
        obj.addProperty("front_only_edit", cfg.frontOnlyEdit);
        obj.addProperty("flat_model_rendering", cfg.flatModelRendering);
        obj.addProperty("hitbox_tweaks", cfg.hitboxTweaks);
        obj.addProperty("crouch_edit_and_chat", cfg.crouchEditAndChat);
        obj.addProperty("default_white_text", cfg.defaultWhiteText);
        obj.addProperty("glow_ink_lighting", cfg.glowInkLighting);

        obj.addProperty("wall_sign_texture_crop_offset", cfg.wallSignTextureCropOffset);

        obj.addProperty("wall_sign_texture_crop_height", cfg.wallSignTextureCropHeight);
        return obj;
    }
}
