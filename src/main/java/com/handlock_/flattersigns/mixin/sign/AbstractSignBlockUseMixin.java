package com.handlock_.flattersigns.mixin.sign;

import com.handlock_.flattersigns.FlatterSigns;
import com.handlock_.flattersigns.FlatterSignsConfig;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom use behaviour for signs when enabled:
 * crouch (or empty sign) opens the editor, otherwise the text is echoed in chat.
 * Waxed signs stay non-editable but can still be read.
 */
@Mixin(AbstractSignBlock.class)
public abstract class AbstractSignBlockUseMixin {

    /**
     * Intercepts the call that would normally open the sign editor.
     * If crouch-edit-and-chat is enabled, we only open the editor while crouching
     * (or when the sign is empty). Otherwise we print the front text to chat.
     */
    @Redirect(
            method = "onUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/AbstractSignBlock;openEditScreen(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/block/entity/SignBlockEntity;Z)V"
            )
    )
    private void flattersigns$redirectOpenEditScreen(AbstractSignBlock instance,
                                                     PlayerEntity player,
                                                     SignBlockEntity signBlockEntity,
                                                     boolean front) {
        if (!FlatterSignsConfig.isCrouchEditAndChatEnabled()) {
            instance.openEditScreen(player, signBlockEntity, front);
            return;
        }

        // We are fully server-authoritative here; the client call is just prediction.
        if (player.getWorld().isClient) {
            return;
        }

        SignText frontText = signBlockEntity.getFrontText();
        boolean hasText = frontText.hasText(player);
        boolean sneaking = player.isSneaking();

        // Empty signs can still be edited without crouching.
        if (sneaking || !hasText) {
            instance.openEditScreen(player, signBlockEntity, true);
            return;
        }

        // Otherwise, right-click reads the sign in chat.
        flattersigns$sendSignTextToChat(player, frontText);
    }

    /* ------------------------------------------------------------------------------------------------
     * When glow ink is applied/removed on Forge via Sinytra, the light update can get stuck.
     * This inject uses a conservative approach: after the vanilla action succeeds, we send an
     * explicit BlockEntity update packet and then trigger a render rebuild on clients.
     * ------------------------------------------------------------------------------------------------ */

    @Inject(method = "onUse", at = @At("RETURN"))
    private void flattersigns$handleGlowInkUpdate(BlockState state,
                                                  World world,
                                                  BlockPos pos,
                                                  PlayerEntity player,
                                                  Hand hand,
                                                  BlockHitResult hit,
                                                  CallbackInfoReturnable<ActionResult> cir) {
        if (!FlatterSignsConfig.isFlatModelRenderingEnabled()
                || !FlatterSignsConfig.isGlowInkLightingEnabled()) {
            return;
        }

        // Only relevant for glow changes.
        if (!player.getStackInHand(hand).isOf(Items.GLOW_INK_SAC)
                && !player.getStackInHand(hand).isOf(Items.INK_SAC)) {
            return;
        }

        // Only if an action was actually accepted/performed.
        ActionResult result = cir.getReturnValue();
        if (result == null || !result.isAccepted()) {
            return;
        }

        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        BlockEntity be = serverWorld.getBlockEntity(pos);
        if (!(be instanceof SignBlockEntity)) {
            return;
        }

        be.markDirty();

        // Targets: always the interacting player + (if available) tracking players.
        Set<ServerPlayerEntity> targets = new HashSet<>();
        if (player instanceof ServerPlayerEntity sp) {
            targets.add(sp);
        }

        // On Fabric this works; on Forge+Connector it may be incomplete,
        // but it's fine because the player themselves is always included above.
        ChunkPos chunkPos = new ChunkPos(pos);
        for (ServerPlayerEntity p : PlayerLookup.tracking(serverWorld, chunkPos)) {
            targets.add(p);
        }

        BlockEntityUpdateS2CPacket bePacket = BlockEntityUpdateS2CPacket.create(be);

        for (ServerPlayerEntity p : targets) {
            // 1) Make sure the client gets the updated glowing flag.
            if (bePacket != null) {
                p.networkHandler.sendPacket(bePacket);
            }

            // 2) Then force a rerender/rebuild at this position.
            var buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            ServerPlayNetworking.send(p, FlatterSigns.FORCE_SIGN_RERENDER_PACKET_ID, buf);
        }

        // Keep vanilla-ish nudges too (harmless on Fabric; sometimes helps on other stacks).
        serverWorld.getChunkManager().markForUpdate(pos);
        serverWorld.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
    }

    @Inject(method = "onUse", at = @At("RETURN"))
    private void flattersigns$printWaxedSignText(BlockState state,
                                                 World world,
                                                 BlockPos pos,
                                                 PlayerEntity player,
                                                 Hand hand,
                                                 BlockHitResult hit,
                                                 CallbackInfoReturnable<ActionResult> cir) {
        if (!FlatterSignsConfig.isCrouchEditAndChatEnabled()) {
            return;
        }

        if (world.isClient) {
            return;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof SignBlockEntity signBlockEntity)) {
            return;
        }

        // Only for waxed signs, and not when applying wax.
        if (!signBlockEntity.isWaxed()) {
            return;
        }

        if (player.getStackInHand(hand).isOf(Items.HONEYCOMB)) {
            return;
        }

        SignText frontText = signBlockEntity.getFrontText();
        flattersigns$sendSignTextToChat(player, frontText);
    }

    private static boolean flattersigns$sendSignTextToChat(PlayerEntity player, SignText frontText) {
        if (frontText == null) {
            return false;
        }

        // Respect the built-in filtered/empty detection.
        if (!frontText.hasText(player)) {
            return false;
        }

        MutableText message = Text.literal("<Sign> ").formatted(Formatting.WHITE);

        Text[] lines = frontText.getMessages(false);
        MutableText content = Text.empty();

        boolean hasAnyText = false;
        for (int i = 0; i < lines.length; i++) {
            Text line = lines[i];
            if (line == null) {
                continue;
            }

            String s = line.getString();
            if (s == null || s.isBlank()) {
                continue;
            }

            if (hasAnyText) {
                content.append(Text.literal(" | ").formatted(Formatting.DARK_GRAY));
            }
            content.append(line);
            hasAnyText = true;
        }

        if (!hasAnyText) {
            return false;
        }

        // Apply sign dye color similar to the renderer.
        int rgb = frontText.getColor().getSignColor();
        TextColor textColor = TextColor.fromRgb(rgb);
        content = content.styled(style -> style.withColor(textColor));

        message.append(content);
        player.sendMessage(message, false);
        return true;
    }
}
