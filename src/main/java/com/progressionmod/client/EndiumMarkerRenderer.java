package com.progressionmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.progressionmod.ProgressionMod;
import com.progressionmod.entities.EndiumMarkerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

@Environment(EnvType.CLIENT)
public class EndiumMarkerRenderer extends EntityRenderer<EndiumMarkerEntity, EntityRenderState> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, "textures/entity/endium_marker.png");

    private static final int COLOR = ARGB.color(255, 255, 255, 255);
    private static final int LIGHT = 0xF000F0;

    private static final float LO  = -0.495f;
    private static final float HI  =  0.495f;
    private static final float TOP =  0.99f;

    public EndiumMarkerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0f;
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose pose,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float nx, float ny, float nz) {
        vc.addVertex(pose, x0, y0, z0).setColor(COLOR).setUv(0f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LIGHT).setNormal(nx, ny, nz);
        vc.addVertex(pose, x1, y1, z1).setColor(COLOR).setUv(1f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LIGHT).setNormal(nx, ny, nz);
        vc.addVertex(pose, x2, y2, z2).setColor(COLOR).setUv(1f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LIGHT).setNormal(nx, ny, nz);
        vc.addVertex(pose, x3, y3, z3).setColor(COLOR).setUv(0f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LIGHT).setNormal(nx, ny, nz);
    }

    private void submitCube(PoseStack poseStack, SubmitNodeCollector collector,
                            net.minecraft.client.renderer.rendertype.RenderType type) {
        collector.submitCustomGeometry(poseStack, type, (pose, vc) -> {
            quad(vc, pose, LO, 0f, LO, HI, 0f, LO, HI, 0f, HI, LO, 0f, HI,   0f, -1f,  0f);
            quad(vc, pose, LO, TOP, HI, HI, TOP, HI, HI, TOP, LO, LO, TOP, LO, 0f,  1f,  0f);
            quad(vc, pose, HI, TOP, LO, LO, TOP, LO, LO, 0f, LO, HI, 0f, LO,  0f,  0f, -1f);
            quad(vc, pose, LO, TOP, HI, HI, TOP, HI, HI, 0f, HI, LO, 0f, HI,  0f,  0f,  1f);
            quad(vc, pose, LO, TOP, LO, LO, TOP, HI, LO, 0f, HI, LO, 0f, LO, -1f,  0f,  0f);
            quad(vc, pose, HI, TOP, HI, HI, TOP, LO, HI, 0f, LO, HI, 0f, HI,  1f,  0f,  0f);
        });
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        var solidType = RenderTypes.entityCutout(TEXTURE);
        submitCube(poseStack, collector, solidType);

        // Emissive layer so the cube self-illuminates (visible at night, through darkness)
        submitCube(poseStack, collector, RenderTypes.entityTranslucentEmissive(TEXTURE));

        // Outline pass — entityCutout registers an outline render type in MC 26.2
        if (state.outlineColor != 0) {
            solidType.outline().ifPresent(outlineType ->
                submitCube(poseStack, collector, outlineType));
        }
    }
}
