package com.handlock_.flattersigns.client;

import com.handlock_.flattersigns.FlatterSigns;
import com.handlock_.flattersigns.FlatterSignsConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelElementTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class FlatterSignsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        if (FlatterSignsConfig.isFlatModelRenderingEnabled()) {
            // Ensure our flat sign models render with cutout.
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.OAK_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.SPRUCE_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BIRCH_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.JUNGLE_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.ACACIA_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.DARK_OAK_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.MANGROVE_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.CHERRY_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BAMBOO_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.CRIMSON_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.WARPED_SIGN, RenderLayer.getCutout());

            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.OAK_WALL_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.SPRUCE_WALL_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BIRCH_WALL_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.JUNGLE_WALL_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.ACACIA_WALL_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.DARK_OAK_WALL_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.MANGROVE_WALL_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.CHERRY_WALL_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BAMBOO_WALL_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.CRIMSON_WALL_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.WARPED_WALL_SIGN, RenderLayer.getCutout());

            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.OAK_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.SPRUCE_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BIRCH_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.JUNGLE_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.ACACIA_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.DARK_OAK_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.MANGROVE_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.CHERRY_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BAMBOO_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.CRIMSON_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.WARPED_HANGING_SIGN, RenderLayer.getCutout());

            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.OAK_WALL_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.SPRUCE_WALL_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BIRCH_WALL_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.JUNGLE_WALL_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.ACACIA_WALL_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.DARK_OAK_WALL_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.MANGROVE_WALL_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.CHERRY_WALL_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BAMBOO_WALL_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.CRIMSON_WALL_HANGING_SIGN, RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(Blocks.WARPED_WALL_HANGING_SIGN, RenderLayer.getCutout());

            // Replace BER with blank renderers to prevent vanilla text rendering.
            BlockEntityRendererFactories.register(BlockEntityType.SIGN, BlankSignRenderer::new);
            BlockEntityRendererFactories.register(BlockEntityType.HANGING_SIGN, BlankHangingSignRenderer::new);

            // Apply wall sign crop at model-load time (resourcepack-friendly).
            registerWallSignCropModelModifier();
        }

        // Register the client-side handler for the forced rerender packet.
        // This is the missing piece for Forge via Sinytra Connector: normal BE update
        // paths may not trigger a lighting/render rebuild, so the server sends an
        // explicit packet after glow ink changes.
        ClientPlayNetworking.registerGlobalReceiver(
                FlatterSigns.FORCE_SIGN_RERENDER_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    BlockPos pos = buf.readBlockPos();
                    client.execute(() -> {
                        if (client.world == null) return;
                        BlockState state = client.world.getBlockState(pos);
                        // Pass a different oldState so the engine doesn't skip the rebuild.
                        client.world.scheduleBlockRerenderIfNeeded(
                                pos, Blocks.AIR.getDefaultState(), state);
                    });
                }
        );
    }

    private static void registerWallSignCropModelModifier() {
        ModelLoadingPlugin.register(ctx -> ctx.modifyModelAfterBake().register((model, context) -> {
            Identifier id = context.id();
            if (id == null) {
                return model;
            }

            // Only touch our own generated wall sign models.
            String ns = id.getNamespace();
            String path = id.getPath();
            if (!"flattersigns".equals(ns) || !path.startsWith("block/") || !path.contains("_wall_sign_flat_")) {
                return model;
            }

            if (!(model instanceof JsonUnbakedModel jsonModel)) {
                return model;
            }

            int cropHeightPx = FlatterSignsConfig.getWallSignTextureCropHeight();
            int cropOffsetPx = FlatterSignsConfig.getWallSignTextureCropOffset();
            applyWallSignCrop(jsonModel, cropHeightPx, cropOffsetPx);
            return model;
        }));
    }

    private static void applyWallSignCrop(JsonUnbakedModel jsonModel, int cropHeightPx, int cropOffsetPx) {
        if (cropHeightPx < 1) cropHeightPx = 1;
        if (cropHeightPx > 16) cropHeightPx = 16;

        if (cropOffsetPx < 0) cropOffsetPx = 0;
        if (cropOffsetPx > 16) cropOffsetPx = 16;
        if (cropOffsetPx + cropHeightPx > 16) cropOffsetPx = 16 - cropHeightPx;

        for (ModelElement element : jsonModel.getElements()) {
            // Only resize elements that actually use our "#tex" layer.
            boolean usesTex = false;
            for (ModelElementFace face : element.faces.values()) {
                if (face != null && "#tex".equals(face.textureId)) {
                    usesTex = true;
                    break;
                }
            }
            if (!usesTex) {
                continue;
            }

            // Anchor the top and adjust height to match crop.
            float yTo = element.to.y;
            element.from.y = yTo - (float) cropHeightPx;

            // Update UVs so the visible region is exactly cropHeightPx tall.
            for (ModelElementFace face : element.faces.values()) {
                if (face == null || !"#tex".equals(face.textureId)) {
                    continue;
                }

                ModelElementTexture tex = face.textureData;
                if (tex == null) {
                    continue;
                }

                float[] uvs = tex.uvs;
                if (uvs == null || uvs.length != 4) {
                    // Default to full width and the requested window.
                    tex.setUvs(new float[]{0f, (float) cropOffsetPx, 16f, (float) (cropOffsetPx + cropHeightPx)});
                } else {
                    // Treat config as a window into the item texture.
                    float baseV1 = uvs[1];
                    float v1 = baseV1 + (float) cropOffsetPx;
                    float maxV1 = baseV1 + (16f - (float) cropHeightPx);
                    if (v1 > maxV1) v1 = maxV1;

                    float v2 = v1 + (float) cropHeightPx;
                    if (v2 > 16f) v2 = 16f;

                    uvs[1] = v1;
                    uvs[3] = v2;
                    tex.setUvs(uvs);
                }
            }
        }
    }

    // Standing / wall signs: do not render text.
    private static class BlankSignRenderer implements BlockEntityRenderer<SignBlockEntity> {
        public BlankSignRenderer(BlockEntityRendererFactory.Context ctx) {}

        @Override
        public void render(SignBlockEntity entity,
                           float tickDelta,
                           MatrixStack matrices,
                           VertexConsumerProvider vertexConsumers,
                           int light,
                           int overlay) {
            // Intentionally empty: blocks still render via models, but no text is drawn.
        }
    }

    // Hanging signs: do not render text.
    private static class BlankHangingSignRenderer implements BlockEntityRenderer<HangingSignBlockEntity> {
        public BlankHangingSignRenderer(BlockEntityRendererFactory.Context ctx) {}

        @Override
        public void render(HangingSignBlockEntity entity,
                           float tickDelta,
                           MatrixStack matrices,
                           VertexConsumerProvider vertexConsumers,
                           int light,
                           int overlay) {
            // Intentionally empty: blocks still render via models, but no text is drawn.
        }
    }
}