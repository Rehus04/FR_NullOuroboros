package destiny.null_ouroboros.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import destiny.null_ouroboros.NullOuroboros;
import destiny.null_ouroboros.client.render.model.StrobelightBlockModel;
import destiny.null_ouroboros.server.block.StrobelightBlock;
import destiny.null_ouroboros.server.block.entity.StrobelightBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class StrobelightBlockEntityRenderer implements BlockEntityRenderer<StrobelightBlockEntity> {
    private final StrobelightBlockModel model;

    public StrobelightBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.model = new StrobelightBlockModel(ctx.getModelSet().bakeLayer(StrobelightBlockModel.LAYER_LOCATION));
    }

    public void render(StrobelightBlockEntity strobelightBlockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = strobelightBlockEntity.getBlockState();
        boolean isOn = state.getValue(StrobelightBlock.LIT);
        Direction facing = state.getValue(StrobelightBlock.FACING);

        ResourceLocation texture = isOn ? ResourceLocation.fromNamespaceAndPath(NullOuroboros.MODID, "textures/block/strobelight.png")
                : ResourceLocation.fromNamespaceAndPath(NullOuroboros.MODID, "textures/block/strobelight_off.png");

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));

        ModelPart bone = this.model.bone;
        float pivotX = bone.x / 16.0f;
        float pivotY = bone.y / 16.0f;
        float pivotZ = bone.z / 16.0f;

        poseStack.pushPose();
        // 1. Move to block centre
        poseStack.translate(0.5, 0.5, 0.5);
        // 2. Rotate to attach to the correct face
        poseStack.mulPose(facing.getOpposite().getRotation());
        // 3. Model-space correction (from your alignment fix)
        poseStack.translate(0.5, 0.5, -0.5);
        // 4. Cancel bone's own pivot
        poseStack.translate(-pivotX, -pivotY, -pivotZ);

        // --- Animate the siren ---
        float angle = strobelightBlockEntity.getRotationAngle() + strobelightBlockEntity.getRotationSpeed() * partialTick;
        this.model.siren.yRot = (float) Math.toRadians(angle % 360f);
        // DO NOT set xRot or zRot here! They stay at their model defaults (0,0).

        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(StrobelightBlockEntity entity) {
        return true;
    }

    @Override
    public boolean shouldRender(StrobelightBlockEntity entity, Vec3 vec3) {
        return true;
    }
}
