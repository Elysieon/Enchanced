package net.collective.enchanced.client.render.entity.thrown_spear;

import net.collective.enchanced.common.entity.ThrownSpearEntity;
import net.collective.enchanced.common.entity.ThrownSpearEntityHitbox;
import net.collectively.geode.debug.Draw;
import net.collectively.geode.math.math;
import net.collectively.geode.types.double3;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.Items;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class ThrownSpearEntityRenderer extends EntityRenderer<ThrownSpearEntity, ThrownSpearEntityState> {
    private final ItemModelManager itemModelManager;

    public ThrownSpearEntityRenderer(EntityRendererFactory.Context context) {
        super(context);

        itemModelManager = context.getItemModelManager();
    }

    @Override
    public ThrownSpearEntityState createRenderState() {
        return new ThrownSpearEntityState();
    }

    @Override
    public void updateRenderState(ThrownSpearEntity entity, ThrownSpearEntityState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);

        state.pitch = entity.getLerpedPitch(tickProgress);
        state.yaw = entity.getLerpedYaw(tickProgress);
        state.shake = 0;

        if (entity.getRenderedItemStack() != null) {
            itemModelManager.updateForNonLivingEntity(state.itemState, entity.getRenderedItemStack(), ItemDisplayContext.THIRD_PERSON_LEFT_HAND, entity);
        }
    }

    @Override
    public void render(ThrownSpearEntityState renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(renderState.yaw - 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(renderState.pitch - 90f));
        matrices.translate(Vec3d.Y.multiply(-1));
        matrices.translate(Vec3d.Z.multiply(-0.15));
        matrices.translate(Vec3d.X.multiply(-0.15));

        if (renderState.itemState != null) {
            renderState.itemState.render(matrices, queue, renderState.light, OverlayTexture.DEFAULT_UV, 0x000000);
        } else {
            queue.submitBlock(matrices, Blocks.STONE.getDefaultState(), renderState.light, OverlayTexture.DEFAULT_UV, 0x000000);
        }

        matrices.pop();

        super.render(renderState, matrices, queue, cameraState);
    }
}