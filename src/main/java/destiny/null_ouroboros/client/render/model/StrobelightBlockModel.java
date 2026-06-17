package destiny.null_ouroboros.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import destiny.null_ouroboros.NullOuroboros;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class StrobelightBlockModel extends EntityModel<Entity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(NullOuroboros.MODID, "strobelight"), "main");
    public final ModelPart bone;
    public final ModelPart siren;
    public final ModelPart siren_emissive;
    public final ModelPart cube1;
    public final ModelPart emissive;
    public final ModelPart cube2;
    public final ModelPart cube3;

    public StrobelightBlockModel(ModelPart root) {
        this.bone = root.getChild("bone");
        this.siren = this.bone.getChild("siren");
        this.siren_emissive = this.siren.getChild("siren_emissive");
        this.cube1 = this.siren.getChild("cube1");
        this.emissive = this.bone.getChild("emissive");
        this.cube2 = this.bone.getChild("cube2");
        this.cube3 = this.bone.getChild("cube3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(8.0F, 24.0F, -8.0F));

        PartDefinition siren = bone.addOrReplaceChild("siren", CubeListBuilder.create(), PartPose.offset(-8.0F, -10.0F, 8.0F));

        PartDefinition siren_emissive = siren.addOrReplaceChild("siren_emissive", CubeListBuilder.create().texOffs(16, 17).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube1 = siren.addOrReplaceChild("cube1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube1_r1 = cube1.addOrReplaceChild("cube1_r1", CubeListBuilder.create().texOffs(0, 17).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition emissive = bone.addOrReplaceChild("emissive", CubeListBuilder.create(), PartPose.offset(-8.0F, -7.5F, 8.0F));

        PartDefinition emissive_r1 = emissive.addOrReplaceChild("emissive_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.5F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition cube2 = bone.addOrReplaceChild("cube2", CubeListBuilder.create(), PartPose.offset(-8.0F, -7.5F, 8.0F));

        PartDefinition cube2_r1 = cube2.addOrReplaceChild("cube2_r1", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.5F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition cube3 = bone.addOrReplaceChild("cube3", CubeListBuilder.create(), PartPose.offset(-8.0F, -14.0F, 8.0F));

        PartDefinition cube3_r1 = cube3.addOrReplaceChild("cube3_r1", CubeListBuilder.create().texOffs(24, 17).addBox(-5.0F, -2.0F, -5.0F, 10.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}