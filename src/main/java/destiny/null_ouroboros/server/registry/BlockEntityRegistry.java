package destiny.null_ouroboros.server.registry;

import destiny.null_ouroboros.NullOuroboros;
import destiny.null_ouroboros.server.block.entity.MechanicalSirenBlockEntity;
import destiny.null_ouroboros.server.block.entity.StrobelightBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, NullOuroboros.MODID);

    public static final RegistryObject<BlockEntityType<StrobelightBlockEntity>> STROBELIGHT_BLOCK_ENTITY = BLOCK_ENTITIES.register("strobelight", () -> BlockEntityType.Builder.of(StrobelightBlockEntity::new, BlockRegistry.STROBELIGHT.get()).build(null));
    public static final RegistryObject<BlockEntityType<MechanicalSirenBlockEntity>> MECHANICAL_SIREN_BLOCK_ENTITY = BLOCK_ENTITIES.register("mechanical_siren", () -> BlockEntityType.Builder.of(MechanicalSirenBlockEntity::new, BlockRegistry.MECHANICAL_SIREN.get()).build(null));
}