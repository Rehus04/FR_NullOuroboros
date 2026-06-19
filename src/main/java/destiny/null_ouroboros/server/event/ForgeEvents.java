package destiny.null_ouroboros.server.event;

import destiny.null_ouroboros.NullOuroboros;
import destiny.null_ouroboros.server.capability.ManifoldingCapability;
import destiny.null_ouroboros.server.registry.CapabilityRegistry;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    @SubscribeEvent
    public static void attachToLevel(AttachCapabilitiesEvent<Level> event) {
        if (event.getObject() instanceof ServerLevel) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(NullOuroboros.MODID, "manifolding_capability"), new ICapabilitySerializable<CompoundTag>() {
                final ManifoldingCapability instance = new ManifoldingCapability();

                @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
                    return cap == CapabilityRegistry.MANIFOLDING_CAPABILITY ? LazyOptional.of(() -> instance).cast() : LazyOptional.empty();
                }

                @Override
                public CompoundTag serializeNBT() {
                    return instance.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundTag nbt) {
                    instance.deserializeNBT(nbt);
                }
            });
        }
    }
}
