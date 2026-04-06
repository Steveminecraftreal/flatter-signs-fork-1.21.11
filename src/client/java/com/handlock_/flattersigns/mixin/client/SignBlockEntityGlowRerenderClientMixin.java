package com.handlock_.flattersigns.mixin.client;

import com.handlock_.flattersigns.FlatterSignsConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Client-side: when the sign's glowing state changes, force an immediate rerender of the block.
 *
 * Fabric/Yarn: update packets typically end up calling readNbt(NbtCompound).
 * Forge/Mojmap (via Sinytra): update packets typically end up calling load(CompoundTag) and/or handleUpdateTag(CompoundTag).
 *
 * We hook all of them with require=0 so missing methods never crash.
 */
@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityGlowRerenderClientMixin {

    @Unique
    private boolean flattersigns$lastGlowState = false;

    /* ------------------------
     * Fabric/Yarn hooks
     * ------------------------ */

    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"), require = 0)
    private void flattersigns$rerenderOnGlowChange_readNbt(NbtCompound nbt, CallbackInfo ci) {
        flattersigns$maybeRerenderOnGlowChange();
    }

    @Inject(method = "setText(Lnet/minecraft/block/entity/SignText;Z)Z", at = @At("TAIL"), require = 0)
    private void flattersigns$rerenderOnGlowChange_setText(SignText text, boolean front, CallbackInfoReturnable<Boolean> cir) {
        Boolean changed = cir.getReturnValue();
        if (changed != null && !changed) {
            return;
        }
        flattersigns$maybeRerenderOnGlowChange();
    }

    /* ------------------------
     * Forge/Mojmap hooks (via Sinytra Connector)
     *
     * We cannot reference CompoundTag at compile-time (Yarn uses NbtCompound),
     * so we use @Coerce Object and remap=false with a literal descriptor.
     * ------------------------ */

    @Inject(
            method = "load(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("TAIL"),
            remap = false,
            require = 0
    )
    private void flattersigns$rerenderOnGlowChange_load(@Coerce Object tag, CallbackInfo ci) {
        flattersigns$maybeRerenderOnGlowChange();
    }

    @Inject(
            method = "handleUpdateTag(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("TAIL"),
            remap = false,
            require = 0
    )
    private void flattersigns$rerenderOnGlowChange_handleUpdateTag(@Coerce Object tag, CallbackInfo ci) {
        flattersigns$maybeRerenderOnGlowChange();
    }

    @Unique
    private void flattersigns$maybeRerenderOnGlowChange() {
        if (!FlatterSignsConfig.isFlatModelRenderingEnabled()
                || !FlatterSignsConfig.isGlowInkLightingEnabled()) {
            return;
        }

        SignBlockEntity sign = (SignBlockEntity) (Object) this;
        World world = sign.getWorld();
        if (!(world instanceof ClientWorld clientWorld)) {
            return;
        }

        boolean glowingNow = flattersigns$isGlowing(sign);
        if (glowingNow == flattersigns$lastGlowState) {
            return;
        }

        flattersigns$lastGlowState = glowingNow;

        BlockPos pos = sign.getPos();
        BlockState state = clientWorld.getBlockState(pos);

        // Force a rerender: oldState must differ from newState, otherwise some paths no-op.
        clientWorld.scheduleBlockRerenderIfNeeded(pos, Blocks.AIR.getDefaultState(), state);
    }

    @Unique
    private static boolean flattersigns$isGlowing(SignBlockEntity sign) {
        SignText front = sign.getFrontText();
        SignText back = sign.getBackText();

        return (front != null && front.isGlowing())
                || (back != null && back.isGlowing());
    }
}
