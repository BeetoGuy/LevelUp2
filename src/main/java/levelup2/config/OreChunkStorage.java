package levelup2.config;

import levelup2.items.ItemOreChunk;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import java.util.Random;

public class OreChunkStorage {
    private final String oreName;
    private OreIngredient ore;
    private ItemStack smeltingItem;
    private final String smeltingResult;
    private final int color;
    private final int experienceYield;
    private final int metadata;
    private boolean active;
    private Item baseItem;
    private String placeholderItem;
    private int itemMeta;

    public OreChunkStorage(String oreName, String smelting, int color, int experience, int metadata) {
        this(oreName, smelting, color, experience, metadata, "");
    }

    public OreChunkStorage(String oreName, String smelting, int color, int experience, int metadata, String placeholderItem) {
        this.oreName = oreName;
        this.smeltingResult = smelting;
        this.color = color;
        this.experienceYield = experience;
        this.metadata = metadata;
        itemMeta = metadata;
        this.placeholderItem = placeholderItem;
    }

    public String getOreName() {
        return oreName;
    }

    public boolean oreMatches(ItemStack test) {
        return ore.apply(test);
    }

    public void setBaseItem(Item item) {
        if (!placeholderItem.equals("") && !LevelUpConfig.getStackFromString(placeholderItem).isEmpty()) {
            ItemStack stack = LevelUpConfig.getStackFromString(placeholderItem);
            baseItem = stack.getItem();
            itemMeta = stack.getMetadata();
        }
        else
            baseItem = item;
    }

    public void registerOre() {
        if (baseItem instanceof ItemOreChunk) {
            OreDictionary.registerOre(oreName, new ItemStack(baseItem, 1, itemMeta));
        }
    }

    public void registerOreIngredientLate() {
        ore = new OreIngredient(oreName);
        active = OreDictionary.getOres(oreName).size() > 1;
    }

    public ItemStack getSmeltingResult() {
        if (smeltingItem == null) smeltingItem = LevelUpConfig.getStackFromString(smeltingResult);
        return smeltingItem.copy();
    }

    public int getColor() {
        return color;
    }

    public int getExperienceYield() {
        return experienceYield;
    }

    public boolean getActivation() {
        return active;
    }

    public Item getBaseItem() {
        return baseItem;
    }

    public int getMetadata() {
        return metadata;
    }

    public ItemStack getHarvestItem(Random rand, int fortune) {
        int count = 2;
        if (fortune > 0)
            count += rand.nextInt(fortune + 1);
        return new ItemStack(baseItem, count, itemMeta);
    }
}
