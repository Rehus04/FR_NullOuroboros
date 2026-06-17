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

    public void render(StrobelightBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        boolean isOn = state.getValue(StrobelightBlock.LIT);
        Direction facing = state.getValue(StrobelightBlock.FACING);

        ResourceLocation texture = isOn ? ResourceLocation.fromNamespaceAndPath(NullOuroboros.MODID, "textures/block/strobelight.png")
                : ResourceLocation.fromNamespaceAndPath(NullOuroboros.MODID, "textures/block/strobelight_off.png");

        ModelPart bone = this.model.bone;
        float pivotX = bone.x / 16.0f;
        float pivotY = bone.y / 16.0f;
        float pivotZ = bone.z / 16.0f;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(facing.getOpposite().getRotation());
        poseStack.translate(0.5, 0.5, -0.5);
        poseStack.translate(-pivotX, -pivotY, -pivotZ);
        poseStack.translate(pivotX, pivotY, pivotZ);

        float angle = be.getRotationAngle() + be.getRotationSpeed() * partialTick;

        VertexConsumer mainConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        this.model.cube2.render(poseStack, mainConsumer, packedLight, packedOverlay);
        this.model.cube3.render(poseStack, mainConsumer, packedLight, packedOverlay);

        poseStack.pushPose();

        poseStack.translate(-0.5, -0.625, 0.5);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle % 360f));

        this.model.cube1.render(poseStack, mainConsumer, packedLight, packedOverlay);

        if (isOn) {
            VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucentEmissive(texture));
            this.model.siren_emissive.render(poseStack, glow, packedLight, packedOverlay);
        } else {
            this.model.siren_emissive.render(poseStack, mainConsumer, packedLight, packedOverlay);
        }
        poseStack.popPose();

        if (isOn) {
            VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucentEmissive(texture));
            this.model.emissive.render(poseStack, glow, packedLight, packedOverlay);
        } else {
            this.model.emissive.render(poseStack, mainConsumer, packedLight, packedOverlay);
        }

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
