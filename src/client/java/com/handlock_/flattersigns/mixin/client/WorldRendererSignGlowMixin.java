package com.handlock_.flattersigns.mixin.client;

import com.handlock_.flattersigns.FlatterSignsConfig;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes glowing-text signs render at full bright when using the flat block model.
 *
 * IMPORTANT: Minecraft 1.20.1 has TWO overloads of getLightmapCoordinates:
 *  - (BlockRenderView, BlockPos)
 *  - (BlockRenderView, BlockState, BlockPos)
 *
 * We MUST target them with explicit descriptors to avoid Mixin selecting the wrong overload.
 *
 * Also includes intermediary-name fallbacks (remap=false) for Sinytra Connector.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererSignGlowMixin {

    // Packed light value for full bright (equivalent to LightmapTextureManager.MAX_LIGHT_COORDINATE)
    private static final int FULL_BRIGHT = 0xF000F0;

    /* ------------------------
     * Named (Yarn) targets
     * ------------------------ */

    @Inject(
            method = "getLightmapCoordinates(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void flattersigns$fullbright_named3(
            BlockRenderView world,
            BlockState state,
            BlockPos pos,
            CallbackInfoReturnable<Integer> cir
    ) {
        flattersigns$maybeFullbright(world, state, pos, cir);
    }

    @Inject(
            method = "getLightmapCoordinates(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;)I",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void flattersigns$fullbright_named2(
            BlockRenderView world,
            BlockPos pos,
            CallbackInfoReturnable<Integer> cir
    ) {
        BlockState state = world.getBlockState(pos);
        flattersigns$maybeFullbright(world, state, pos, cir);
    }

    /* ------------------------
     * Intermediary targets (Connector)
     * ------------------------ */

    @Inject(
            method = "method_23793(Lnet/minecraft/class_1920;Lnet/minecraft/class_2680;Lnet/minecraft/class_2338;)I",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private static void flattersigns$fullbright_intermediary3(
            BlockRenderView world,
            BlockState state,
            BlockPos pos,
            CallbackInfoReturnable<Integer> cir
    ) {
        flattersigns$maybeFullbright(world, state, pos, cir);
    }

    @Inject(
            method = "method_23794(Lnet/minecraft/class_1920;Lnet/minecraft/class_2338;)I",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private static void flattersigns$fullbright_intermediary2(
            BlockRenderView world,
            BlockPos pos,
            CallbackInfoReturnable<Integer> cir
    ) {
        BlockState state = world.getBlockState(pos);
        flattersigns$maybeFullbright(world, state, pos, cir);
    }

    @Unique
    private static void flattersigns$maybeFullbright(
            BlockRenderView world,
            BlockState state,
            BlockPos pos,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (!FlatterSignsConfig.isFlatModelRenderingEnabled()
                || !FlatterSignsConfig.isGlowInkLightingEnabled()) {
            return;
        }

        if (!(state.getBlock() instanceof AbstractSignBlock)) {
            return;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof SignBlockEntity sign)) {
            return;
        }

        SignText front = sign.getFrontText();
        SignText back = sign.getBackText();

        boolean glowing = (front != null && front.isGlowing())
                || (back != null && back.isGlowing());

        if (glowing) {
            cir.setReturnValue(FULL_BRIGHT);
        }
    }
}
