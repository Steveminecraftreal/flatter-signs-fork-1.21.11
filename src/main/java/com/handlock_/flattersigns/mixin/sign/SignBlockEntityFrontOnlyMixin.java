package com.handlock_.flattersigns.mixin.sign;

import com.handlock_.flattersigns.FlatterSignsConfig;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When enabled, treats the front as the only editable side.
 * The back always mirrors the front.
 */
@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityFrontOnlyMixin {
    @Shadow public abstract SignText getFrontText();
    @Shadow protected abstract boolean setFrontText(SignText frontText);

    @Inject(method = "getText(Z)Lnet/minecraft/block/entity/SignText;", at = @At("HEAD"), cancellable = true)
    private void flattersigns$forceFrontGet(boolean front, CallbackInfoReturnable<SignText> cir) {
        if (!FlatterSignsConfig.isFrontOnlyEditEnabled()) {
            return;
        }
        cir.setReturnValue(this.getFrontText());
    }

    @Inject(method = "setText(Lnet/minecraft/block/entity/SignText;Z)Z", at = @At("HEAD"), cancellable = true)
    private void flattersigns$forceFrontSet(SignText text, boolean front, CallbackInfoReturnable<Boolean> cir) {
        if (!FlatterSignsConfig.isFrontOnlyEditEnabled()) {
            return;
        }
        cir.setReturnValue(this.setFrontText(text));
    }

    @Inject(method = "setBackText(Lnet/minecraft/block/entity/SignText;)Z", at = @At("HEAD"), cancellable = true)
    private void flattersigns$redirectBackToFront(SignText backText, CallbackInfoReturnable<Boolean> cir) {
        if (!FlatterSignsConfig.isFrontOnlyEditEnabled()) {
            return;
        }
        cir.setReturnValue(this.setFrontText(backText));
    }

    @Inject(method = "getTextFacing(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/block/entity/SignText;", at = @At("HEAD"), cancellable = true)
    private void flattersigns$frontForFacing(PlayerEntity player, CallbackInfoReturnable<SignText> cir) {
        if (!FlatterSignsConfig.isFrontOnlyEditEnabled()) {
            return;
        }
        cir.setReturnValue(this.getFrontText());
    }

    @Inject(method = "isPlayerFacingFront(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void flattersigns$alwaysFront(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (!FlatterSignsConfig.isFrontOnlyEditEnabled()) {
            return;
        }
        cir.setReturnValue(true);
    }
}
