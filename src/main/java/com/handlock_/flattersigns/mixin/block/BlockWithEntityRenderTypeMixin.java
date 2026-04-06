package com.handlock_.flattersigns.mixin.block;

import com.handlock_.flattersigns.FlatterSignsConfig;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forces MODEL rendering for sign-related BlockWithEntity blocks when
 * the "flat_model_rendering" config option is enabled.
 * All sign variants (standing, wall, hanging, wall-hanging) extend
 * AbstractSignBlock, so a single instanceof check covers them all.
 */
@Mixin(BlockWithEntity.class)
public abstract class BlockWithEntityRenderTypeMixin {

    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void flattersigns$modelRenderType(BlockState state, CallbackInfoReturnable<BlockRenderType> cir) {
        if (!FlatterSignsConfig.isFlatModelRenderingEnabled()) {
            return;
        }

        if (state.getBlock() instanceof AbstractSignBlock) {
            cir.setReturnValue(BlockRenderType.MODEL);
        }
    }
}