package destiny.null_ouroboros.server.event;

import destiny.null_ouroboros.server.capability.ManifoldingCapability;
import destiny.null_ouroboros.server.registry.CapabilityRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Events {
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (event.level instanceof ServerLevel serverLevel) {
            serverLevel.getCapability(CapabilityRegistry.MANIFOLDING_CAPABILITY).ifPresent(cap -> {
                cap.serverTick(serverLevel);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (event.player.level() instanceof ServerLevel serverLevel) {
            serverLevel.getCapability(CapabilityRegistry.MANIFOLDING_CAPABILITY).ifPresent(cap -> {
                cap.applyWindToPlayer(event.player, serverLevel);
                cap.damagePlayerIfExposed(event.player, serverLevel);
            });
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (level.dimension().location().equals(ManifoldingCapability.DIMENSION_ID)) {
                level.setWeatherParameters(0, 0, false, false);
            }
        }
    }

    @SubscribeEvent
    public static void onServerLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (event.level instanceof ServerLevel level) {
            if (level.dimension().location().equals(ManifoldingCapability.DIMENSION_ID)) {
                level.rainLevel = 0;
                level.thunderLevel = 0;
            }
        }
    }
}