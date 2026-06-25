package destiny.null_ouroboros.server.event;

import destiny.null_ouroboros.server.item.DisketteItem;
import destiny.null_ouroboros.server.registry.ItemRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        DisketteItem disketteItem = (DisketteItem) ItemRegistry.DISKETTE.get();
        event.register(((stack, color) -> color != 1 ? -1 : disketteItem.getColor(stack)), ItemRegistry.DISKETTE.get());
    }
}
