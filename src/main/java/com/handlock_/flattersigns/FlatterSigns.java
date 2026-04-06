package com.handlock_.flattersigns;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlatterSigns implements ModInitializer {
    public static final String MOD_ID = "flattersigns";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Used by generated blockstate JSON via "fabric:load_conditions".
    public static final Identifier FLAT_MODELS_ENABLED_CONDITION_ID =
            new Identifier(MOD_ID, "flat_models_enabled");

    /**
     * S2C packet: tells the client to force a rerender/rebuild at a given BlockPos.
     * Used to make glow-ink brightness update immediately on Forge via Sinytra Connector.
     */
    public static final Identifier FORCE_SIGN_RERENDER_PACKET_ID =
            new Identifier(MOD_ID, "force_sign_rerender");

    @Override
    public void onInitialize() {
        FlatterSignsConfig.load();

        ResourceConditions.register(
                FLAT_MODELS_ENABLED_CONDITION_ID,
                json -> FlatterSignsConfig.isFlatModelRenderingEnabled()
        );
    }
}
