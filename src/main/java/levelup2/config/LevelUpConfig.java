package levelup2.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import levelup2.skills.SkillRegistry;
import levelup2.util.JsonTransfer;
import levelup2.util.Library;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.OreDictionary;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class LevelUpConfig {
    public static boolean resetClassOnDeath = false;
    public static boolean furnaceEjection = false;
    private static boolean resetJsonFiles = false;
    public static boolean damageScaling = false;/*
    public static boolean alwaysDropChunks = false;
    public static boolean useOreChunks = false;
    public static boolean dupeAllOres = true;
    public static boolean fortuneOre = true;*/
    public static List<String> cropBlacklist;
    private static Configuration cfg;
    private static Property[] serverProperties;
    public static int rareChance = 1;
    public static int uncommonChance = 15;
    public static int commonChance = 85;
    public static int combinedChance;
    public static int reclassCost = 30;
    public static double refundValue = 0.5F;
    public static List<String> oreBlocks;
    public static List<Ingredient> blacklistOutputs;
    public static boolean giveSkillBook = true;
    public static int levelCost = 5;
    private static String[] oreBlockList = {"geolosys:ore_vanilla", "geolosys:ore"};
    private static String[] surfaceOresDefault = {"oreCoal,0x343434,1,minecraft:coal", "oreIron,0xBC9980,1,minecraft:iron_ingot", "oreGold,0xFCEE4B,2,minecraft:gold_ingot", "oreDiamond,0x5DECF5,4,minecraft:diamond", "oreEmerald,0x17DD62,4,minecraft:emerald", "oreRedstone,0xFF0000,2,minecraft:redstone", "oreLapis,0x193CB4,2,minecraft:dye:4", "oreCopper,0xFF6D11,1,thermalfoundation:material:128", "oreTin,0x8FB0CE,1,thermalfoundation:material:129",
    "oreSilver,0xA9CDDC,2,thermalfoundation:material:130", "oreLead,0x515C73,2,thermalfoundation:material:131", "oreAluminum,0xE2CEE1,2,thermalfoundation:material:132", "oreNickel,0xAAA37B,2,thermalfoundation:material:133", "orePlatinum,0xA1DCFF,3,thermalfoundation:material:134", "oreIridium,0xB7BFDC,4,thermalfoundation:material:135", "oreMithril,0x64B9D8,4,thermalfoundation:material:136"};
    private static String[] netherOresDefault = {"oreQuartz,0xE5DED5,2,minecraft:quartz", "oreCobalt,0x2979e7,4,tconstruct:ingots", "oreArdite,0xFFBD24,5,tconstruct:ingots:1"};

    private static Property resetJson;
    private static Map<String, Property> PROP_SYNC = Maps.newHashMap();

    private static Path configDir;
    public static Path jsonDir;
    public static Path lootDir;
    public static Path skillDir;
    public static Path classDir;

    public static void init(File file) {
        configDir = file.getParentFile().toPath().resolve("levelup2");
        jsonDir = configDir.resolve("json");
        lootDir = jsonDir.resolve("loot_tables");
        skillDir = jsonDir.resolve("skills").resolve("playerskill");
        classDir = jsonDir.resolve("skills").resolve("playerclass");
        cfg = new Configuration(file);
        PROP_SYNC.put("classreset", cfg.get(Configuration.CATEGORY_GENERAL, "Reset class on death", resetClassOnDeath, "Does the player lose all levels on death?"));
        PROP_SYNC.put("furnaceeject", cfg.get(Configuration.CATEGORY_GENERAL, "Furnace ejects bonus items", furnaceEjection, "Does the furnace eject doubled items?"));
        PROP_SYNC.put("skillrefund", cfg.get(Configuration.CATEGORY_GENERAL, "Skill refund cost", refundValue, "The refund value of lowering skill levels."));
        PROP_SYNC.put("levelcost", cfg.get(Configuration.CATEGORY_GENERAL, "Skill level cost", levelCost, "The amount of levels needed to gain a skill point.", 1, 30));
        PROP_SYNC.put("skillbook", cfg.get(Configuration.CATEGORY_GENERAL, "Spawn with book", giveSkillBook, "If the player spawns with a skill book."));
        /*
        serverProperties = new Property[] {
                cfg.get(Configuration.CATEGORY_GENERAL, "Reset class on death", resetClassOnDeath, "Does the player lose all levels on death?"),
                cfg.get(Configuration.CATEGORY_GENERAL, "Furnace ejects bonus items", furnaceEjection, "Does the furnace eject doubled items?"),
                cfg.get(Configuration.CATEGORY_GENERAL, "Sword skill damage scaling", damageScaling, "Get additional attack power if a mob's max HP is over 20"),
                cfg.get(Configuration.CATEGORY_GENERAL, "Always drop ore chunks", alwaysDropChunks, "Always drop ore chunks on ore harvest"),
                cfg.get(Configuration.CATEGORY_GENERAL, "Break ores into chunks", useOreChunks, "Use ore chunks for ore doubling"),
                cfg.get(Configuration.CATEGORY_GENERAL, "Duplicate any ore", dupeAllOres, "All ores can be doubled, even if they don't have a chunk."),
                cfg.get(Configuration.CATEGORY_GENERAL, "Reclass level cost", reclassCost, "How many levels it will cost to change classes.", 0, 100),
                cfg.get(Configuration.CATEGORY_GENERAL, "Fortune ore doubling", fortuneOre, "Doubled ores are affected by the Fortune enchant."),
                cfg.get(Configuration.CATEGORY_GENERAL, "Skill refund cost", refundValue, "The refund value of lowering skill levels.")
        };*/
        oreBlocks = Arrays.asList(cfg.getStringList("Special ore cases", "Whitelist", oreBlockList, "Blocks that don't have their own OreDict entry, but still drop registered ores."));
        cropBlacklist = Arrays.asList(cfg.getStringList("Crops for farming", "Blacklist", new String[] {""}, "Crops that won't be affected by farming growth skill, uses internal block name. No sync to client required."));
        assembleOreChunks(Library.SURFACE_ORES, cfg.getStringList("surfaceores", Configuration.CATEGORY_GENERAL, surfaceOresDefault, "Ores that split into chunks. (String build: Ore name, color, experience yield, smelting result (mod:item:metadata:stacksize), (optional) defined chunk"));
        assembleOreChunks(Library.NETHER_ORES, cfg.getStringList("netherores", Configuration.CATEGORY_GENERAL, netherOresDefault, "Ores that split into chunks. (String build: Ore name, color, experience yield, smelting result (mod:item:metadata:stacksize), (optional) defined chunk"));
        assembleOreChunks(Library.END_ORES, cfg.getStringList("endores", Configuration.CATEGORY_GENERAL, new String[0], "Ores that split into chunks. (String build: Ore name, color, experience yield, smelting result (mod:item:metadata:stacksize), (optional) defined chunk"));
        resetJson = cfg.get("debug", "Reset json files", resetJsonFiles, "Forces Level Up! to restore external json files to default");
        resetJsonFiles = resetJson.getBoolean();
        rareChance = cfg.getInt("Rare Digging Loot Chance", "digloot", rareChance, 0, 100, "Chances that a rare loot drop will appear");
        uncommonChance = cfg.getInt("Uncommon Digging Loot Chance", "digloot", uncommonChance, 0, 100, "Chances that an uncommon loot drop will appear");
        commonChance = cfg.getInt("Common Digging Loot Chance", "digloot", commonChance, 0, 100, "Chances that a common loot drop will appear");
        combinedChance = rareChance + uncommonChance + commonChance;
        if (cfg.hasChanged())
            cfg.save();
        useServerProperties();
        transferLootTables();
        if (resetJsonFiles) {
            resetJson.set(false);
            cfg.save();
        }
    }

    private static void assembleOreChunks(List<OreChunkStorage> chunkItem, String[] ores) {
        if (ores.length > 0) {
            for (int i = 0; i < ores.length; i++) {
                String[] parts = ores[i].split(",");
                String oreName = parts[0];
                int color = Integer.decode(parts[1]);
                int experience = Integer.parseInt(parts[2]);
                String stack = parts[3];
                //ItemStack stack = getStackFromString(parts[3]);
                if (parts.length == 5)
                    chunkItem.add(new OreChunkStorage(oreName, stack, color, experience, i, parts[4]));
                else
                    chunkItem.add(new OreChunkStorage(oreName, stack, color, experience, i));
            }
        }
    }

    public static ItemStack getStackFromString(String str) {
        String[] parts = str.split(":");
        int meta = parts.length > 3 ? Integer.parseInt(parts[2]) : 0;
        int stackSize = parts.length == 4 ? Integer.parseInt(parts[3]) : 1;
        Item item = Item.REGISTRY.getObject(new ResourceLocation(parts[0], parts[1]));
        if (item != null && item != Items.AIR) {
            return new ItemStack(item, stackSize, meta);
        }
        return ItemStack.EMPTY;
    }

    public static void getBlacklistOutputs() {
        String[] items = cfg.getStringList("CraftingOutputs", "blacklist", new String[] {}, "Which items, if any, do not give experience upon crafting. Format: modid:item OR modid:item:metadata");
        List<Ingredient> ing = Lists.newArrayList();
        if (items.length > 0) {
            for (String str : items) {
                String[] parts = str.split(":");
                int meta = parts.length == 3 ? Integer.parseInt(parts[2]) : OreDictionary.WILDCARD_VALUE;
                Item item = Item.REGISTRY.getObject(new ResourceLocation(parts[0], parts[1]));
                if (item != null && item != Items.AIR) {
                    ing.add(Ingredient.fromStacks(new ItemStack(item, 1, meta)));
                }
            }
        }
        if (cfg.hasChanged())
            cfg.save();
        blacklistOutputs = ing;
    }

    public static NBTTagCompound getServerProperties() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("classreset", PROP_SYNC.get("classreset").getBoolean());
        tag.setBoolean("furnaceeject", PROP_SYNC.get("furnaceeject").getBoolean());
        tag.setDouble("skillrefund", PROP_SYNC.get("skillrefund").getDouble());
        tag.setInteger("levelcost", PROP_SYNC.get("levelcost").getInt());
        tag.setBoolean("skillbook", PROP_SYNC.get("skillbook").getBoolean());
        return tag;
    }
/*
    public static Property[] getServerProperties() {
        return serverProperties;
    }*/

    public static void useServerProperties() {
        resetClassOnDeath = PROP_SYNC.get("classreset").getBoolean();
        furnaceEjection = PROP_SYNC.get("furnaceeject").getBoolean();
        refundValue = PROP_SYNC.get("skillrefund").getDouble();
        levelCost = PROP_SYNC.get("levelcost").getInt();
        giveSkillBook = PROP_SYNC.get("skillbook").getBoolean();
        /*
        resetClassOnDeath = serverProperties[0].getBoolean();
        furnaceEjection = serverProperties[1].getBoolean();
        damageScaling = serverProperties[2].getBoolean();
        alwaysDropChunks = serverProperties[3].getBoolean();
        useOreChunks = serverProperties[4].getBoolean();
        dupeAllOres = serverProperties[5].getBoolean();
        reclassCost = serverProperties[6].getInt();
        fortuneOre = serverProperties[7].getBoolean();
        refundValue = serverProperties[8].getDouble();*/
    }

    public static void useServerProperties(NBTTagCompound tag) {
        resetClassOnDeath = tag.getBoolean("classreset");
        furnaceEjection = tag.getBoolean("furnaceeject");
        refundValue = tag.getDouble("skillrefund");
        levelCost = tag.getInteger("levelcost");
        giveSkillBook = tag.getBoolean("skillbook");
        SkillRegistry.resetForNewProps();
    }
/*
    public static void useServerProperties(Property[] props) {
        resetClassOnDeath = props[0].getBoolean();
        furnaceEjection = props[1].getBoolean();
        damageScaling = props[2].getBoolean();
        alwaysDropChunks = props[3].getBoolean();
        useOreChunks = props[4].getBoolean();
        dupeAllOres = props[5].getBoolean();
        reclassCost = props[6].getInt();
        fortuneOre = props[7].getBoolean();
        refundValue = props[8].getDouble();
        SkillRegistry.resetForNewProps();
    }*/

    private static List<Integer> getColorsFromProperty(Property prop) {
        int[] colors = prop.getIntList();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < colors.length; i++)
            list.add(colors[i]);
        return list;
    }

    private static void transferLootTables() {
        Set<String> files = new HashSet<>();
        files.add("fishing/fishing_loot");
        files.add("digging/common_dig");
        files.add("digging/uncommon_dig");
        files.add("digging/rare_dig");
        JsonTransfer.findResources("json/loot_tables", files).stream().forEach(r -> JsonTransfer.copyResource(r, configDir.resolve(r), resetJsonFiles));
        Library.registerLootTableLocations(files);
    }

    public static void registerSkillProperties() {
        JsonTransfer.findResources("json/skills/playerskill", Library.SKILLS).stream().forEach(r -> JsonTransfer.copyResource(r, configDir.resolve(r), resetJsonFiles));
        JsonTransfer.findResources("json/skills/playerclass", Library.CLASSES).stream().forEach(r -> JsonTransfer.copyResource(r, configDir.resolve(r), resetJsonFiles));
        /*
        Set<String> files = new HashSet<>();
        for (IPlayerSkill skill : SkillRegistry.getSkillRegistry()) {
            if (skill.hasExternalJson()) {
                files.add(skill.getJsonLocation());
            }
        }
        //SkillRegistry.getSkillRegistry().forEach(e -> files.add(e.getJsonLocation()));
        JsonTransfer.findResources("json/skills", files).stream().forEach(r -> JsonTransfer.copyResource(r, configDir.resolve(r), resetJsonFiles));*/
        SkillRegistry.registerSkillProperties();
    }
}
