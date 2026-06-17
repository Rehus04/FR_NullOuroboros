package destiny.null_ouroboros.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import destiny.null_ouroboros.NullOuroboros;
import destiny.null_ouroboros.client.render.model.MechanicalSirenBlockModel;
import destiny.null_ouroboros.server.block.StrobelightBlock;
import destiny.null_ouroboros.server.block.entity.MechanicalSirenBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MechanicalSirenBlockEntityRenderer implements BlockEntityRenderer<MechanicalSirenBlockEntity> {
    public final MechanicalSirenBlockModel model;

    public MechanicalSirenBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.model = new MechanicalSirenBlockModel(ctx.getModelSet().bakeLayer(MechanicalSirenBlockModel.LAYER_LOCATION));
    }

    @Override
    public void render(MechanicalSirenBlockEntity mechanicalSirenBlockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = mechanicalSirenBlockEntity.getBlockState();
        boolean isOn = state.getValue(StrobelightBlock.LIT);
        Direction facing = state.getValue(StrobelightBlock.FACING);

        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(NullOuroboros.MODID, "textures/block/mechanical_siren.png");

        ModelPart bone = this.model.bb_main;
        float pivotX = bone.x / 16.0f;
        float pivotY = bone.y / 16.0f;
        float pivotZ = bone.z / 16.0f;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(facing.getOpposite().getRotation());
        poseStack.translate(0.5, 0.5, -0.5);
        poseStack.translate(-pivotX, -pivotY, -pivotZ);
        poseStack.translate(pivotX, pivotY, pivotZ);

        float angle = mechanicalSirenBlockEntity.getRotationAngle() + mechanicalSirenBlockEntity.getRotationSpeed() * partialTick;

        poseStack.pushPose();
        poseStack.translate(-0.5, -0.625, 0.5);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle % 360f));

        VertexConsumer mainConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));

        this.model.blades.render(poseStack, mainConsumer, packedLight, packedOverlay);

        poseStack.popPose();

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(MechanicalSirenBlockEntity entity) {
        return true;
    }

    @Override
    public boolean shouldRender(MechanicalSirenBlockEntity entity, Vec3 vec3) {
        return true;
    }
}
