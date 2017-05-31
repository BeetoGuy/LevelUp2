package levelup2.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.conditions.LootCondition;

import java.util.Collection;
import java.util.Random;

public class LootEntryLevelUpTable extends LootEntryTable {
    public LootEntryLevelUpTable(ResourceLocation tableIn, int weightIn, int qualityIn, LootCondition[] conditionsIn, String entryName)
    {
        super(tableIn, weightIn, qualityIn, conditionsIn, entryName);
    }

    @Override
    public void addLoot(Collection<ItemStack> stacks, Random rand, LootContext context)
    {
        LootTable loottable = Library.getLootManager().getLootTableFromLocation(this.table);
        Collection<ItemStack> collection = loottable.generateLootForPools(rand, context);
        stacks.addAll(collection);
    }

    protected void serialize(JsonObject json, JsonSerializationContext context)
    {
        json.addProperty("name", this.table.toString());
    }

    public static LootEntryTable deserialize(JsonObject object, JsonDeserializationContext deserializationContext, int weightIn, int qualityIn, LootCondition[] conditionsIn)
    {
        String name = net.minecraftforge.common.ForgeHooks.readLootEntryName(object, "loot_table");
        ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(object, "name"));
        return new LootEntryTable(resourcelocation, weightIn, qualityIn, conditionsIn, name);
    }
}
