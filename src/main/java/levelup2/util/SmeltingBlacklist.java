package levelup2.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class SmeltingBlacklist {
    private static List<ItemStack> blacklist = new ArrayList<>();

    public static boolean addItem(ItemStack stack) {
        return blacklist.add(stack.copy());
    }

    public static void addOres(String name) {
        for(ItemStack stack : OreDictionary.getOres(name))
            addItem(stack);
    }

    public static boolean contains(ItemStack stack) {
        boolean match = false;
        boolean wild = stack.getMetadata() == OreDictionary.WILDCARD_VALUE;
        for (ItemStack black : blacklist) {
            if (match) break;
            else if (wild) match = black.getItem() == stack.getItem();
            else match = ItemStack.areItemsEqual(black, stack) || (black.getMetadata() == OreDictionary.WILDCARD_VALUE && stack.getItem() == black.getItem());
        }
        return match;
    }
}
