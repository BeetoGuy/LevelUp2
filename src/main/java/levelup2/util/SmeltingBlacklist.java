package levelup2.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import java.util.ArrayList;
import java.util.List;

public class SmeltingBlacklist {
    private static List<Ingredient> blacklist = new ArrayList<>();

    public static boolean addItem(ItemStack stack) {
        return blacklist.add(Ingredient.fromStacks(stack));
    }

    public static void addOres(String name) {
        blacklist.add(new OreIngredient(name));
    }

    public static boolean contains(ItemStack stack) {
        for (Ingredient black : blacklist) {
            if (black.apply(stack))
                return true;
        }
        return false;
    }
}
