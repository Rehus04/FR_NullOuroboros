package destiny.null_ouroboros.server.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DisketteItem extends Item {
    public DisketteItem(Properties properties) {
        super(properties);
    }

    public int getColor(ItemStack stack) {
        if(stack.getTag() != null && stack.getTag().contains("color")) {
            return stack.getTag().getInt("color");
        }

        return 0;
    }
}
