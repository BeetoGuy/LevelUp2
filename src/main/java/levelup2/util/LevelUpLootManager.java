package levelup2.util;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import levelup2.config.LevelUpConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.ForgeHooks;

import java.io.File;
import java.io.IOException;

public class LevelUpLootManager {
    private static final Gson GSON_INSTANCE = (new GsonBuilder()).registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer()).registerTypeAdapter(LootPool.class, new LootPool.Serializer()).registerTypeAdapter(LootTable.class, new LootTable.Serializer()).registerTypeHierarchyAdapter(LootEntry.class, new LootEntry.Serializer()).registerTypeHierarchyAdapter(LootFunction.class, new LootFunctionManager.Serializer()).registerTypeHierarchyAdapter(LootCondition.class, new LootConditionManager.Serializer()).registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer()).create();
    private final LoadingCache<ResourceLocation, LootTable> registeredLootTables = CacheBuilder.newBuilder().build(new LevelUpLootManager.Loader());
    private final File baseFolder;

    public LevelUpLootManager() {
        baseFolder = LevelUpConfig.lootDir.toFile();
        this.reloadLootTables();
    }

    public LootTable getLootTableFromLocation(ResourceLocation resources) {
        return this.registeredLootTables.getUnchecked(resources);
    }

    public void reloadLootTables() {
        this.registeredLootTables.invalidateAll();

        for (ResourceLocation location : Library.getLootTables()) {
            this.getLootTableFromLocation(location);
        }
    }

    class Loader extends CacheLoader<ResourceLocation, LootTable> {
        private Loader() {}

        @Override
        public LootTable load(ResourceLocation location) throws Exception {
            if (location.getPath().contains(".")) {
                System.out.println("[LEVELUP] ERROR: Cannot load loot table " + location.getPath());
                return LootTable.EMPTY_LOOT_TABLE;
            }
            else {
                LootTable table = this.loadLootTable(location);

                if (table == null) {
                    System.out.println("[LEVELUP] ERROR: Cannot load loot table " + location.getPath());
                    table = LootTable.EMPTY_LOOT_TABLE;
                }
                return table;
            }
        }

        private LootTable loadLootTable(ResourceLocation location) {
            File file = new File(LevelUpLootManager.this.baseFolder, location.getPath() + ".json");

            if (file.exists()) {
                if (file.isFile()) {
                    String s;
                    try {
                        s = Files.toString(file, Charsets.UTF_8);
                    }
                    catch (IOException e) {
                        System.out.println("[LEVELUP] ERROR: Cannot load loot table " + file.toString());
                        return LootTable.EMPTY_LOOT_TABLE;
                    }
                    return ForgeHooks.loadLootTable(GSON_INSTANCE, location, s, true, null);
                }
                else {
                    return LootTable.EMPTY_LOOT_TABLE;
                }
            }
            else {
                return null;
            }
        }
    }
}
