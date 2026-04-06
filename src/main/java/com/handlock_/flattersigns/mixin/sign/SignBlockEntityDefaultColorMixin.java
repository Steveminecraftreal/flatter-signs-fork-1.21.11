package com.handlock_.flattersigns.mixin.sign;

import com.handlock_.flattersigns.FlatterSignsConfig;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forces the default SignText color to white (instead of black) when enabled.
 *
 * <p>Also contains a targeted Forge+Sinytra fix: when glow ink is applied/removed, some stacks
 * don't propagate BE updates/light properly unless we explicitly send a BE update packet and
 * nudge the world listeners. The update logic is intentionally conservative.</p>
 */
@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityDefaultColorMixin {

    @Unique
    private boolean flattersigns$preSetTextGlowing = false;

    @Inject(method = "createText", at = @At("RETURN"), cancellable = true)
    private void flattersigns$defaultWhiteText(CallbackInfoReturnable<SignText> cir) {
        if (!FlatterSignsConfig.isDefaultWhiteTextEnabled()) {
            return;
        }

        SignText text = cir.getReturnValue();
        if (text == null) {
            return;
        }

        if (text.getColor() == DyeColor.BLACK) {
            cir.setReturnValue(text.withColor(DyeColor.WHITE));
        }
    }

    /* ------------------------------------------------------------------------------------------------
     * Glow-ink light update fix (server side).
     *
     * The important part is: AFTER glow state changes, emit a BE update packet and force
     * updateListeners, because some Forge+Sinytra paths otherwise keep stale light/cache state.
     *
     * Keep this logic as close to "do what vanilla would do, plus a little extra" as possible.
     * ------------------------------------------------------------------------------------------------ */

    @Inject(method = "setText(Lnet/minecraft/block/entity/SignText;Z)Z", at = @At("HEAD"), require = 0)
    private void flattersigns$beforeSetText(SignText text, boolean front, CallbackInfoReturnable<Boolean> cir) {
        if (!FlatterSignsConfig.isFlatModelRenderingEnabled()
                || !FlatterSignsConfig.isGlowInkLightingEnabled()) {
            return;
        }

        SignBlockEntity sign = (SignBlockEntity) (Object) this;
        flattersigns$preSetTextGlowing = flattersigns$isGlowing(sign);
    }

    @Inject(method = "setText(Lnet/minecraft/block/entity/SignText;Z)Z", at = @At("RETURN"), require = 0)
    private void flattersigns$afterSetText(SignText text, boolean front, CallbackInfoReturnable<Boolean> cir) {
        if (!FlatterSignsConfig.isFlatModelRenderingEnabled()
                || !FlatterSignsConfig.isGlowInkLightingEnabled()) {
            return;
        }

        Boolean changed = cir.getReturnValue();
        if (changed != null && !changed) {
            return;
        }

        SignBlockEntity sign = (SignBlockEntity) (Object) this;

        // Only care about glow state transitions.
        boolean nowGlowing = flattersigns$isGlowing(sign);
        if (nowGlowing == flattersigns$preSetTextGlowing) {
            return;
        }

        BlockPos pos = sign.getPos();
        if (pos == null) {
            return;
        }

        if (!(sign.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        // Run after this tick, to avoid doing updates mid-callchain.
        serverWorld.getServer().execute(() -> {
            if (!serverWorld.isChunkLoaded(pos)) {
                return;
            }

            BlockEntity be = serverWorld.getBlockEntity(pos);
            if (be == null) {
                return;
            }

            BlockState state = serverWorld.getBlockState(pos);

            // Make sure vanilla's 'dirty' flag gets set.
            be.markDirty();

            // Mark chunk section for render update.
            serverWorld.getChunkManager().markForUpdate(pos);

            // Explicit BE update packet to tracking players (this is the missing piece on Forge+Connector).
            BlockEntityUpdateS2CPacket bePacket = BlockEntityUpdateS2CPacket.create(be);
            if (bePacket != null) {
                ChunkPos chunkPos = new ChunkPos(pos);
                for (ServerPlayerEntity p : PlayerLookup.tracking(serverWorld, chunkPos)) {
                    p.networkHandler.sendPacket(bePacket);
                }
            }

            // Nudge a redraw / rebuild on the client side.
            serverWorld.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
        });
    }

    @Unique
    private static boolean flattersigns$isGlowing(SignBlockEntity sign) {
        SignText front = sign.getFrontText();
        SignText back = sign.getBackText();

        return (front != null && front.isGlowing())
                || (back != null && back.isGlowing());
    }
}
