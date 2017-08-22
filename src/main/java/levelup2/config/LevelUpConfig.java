package levelup2.config;

import levelup2.util.JsonTransfer;
import levelup2.util.Library;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class LevelUpConfig {
    public static boolean resetClassOnDeath = true;
    public static boolean furnaceEjection = false;
    private static boolean resetJsonFiles = false;
    public static boolean damageScaling = false;
    public static boolean alwaysDropChunks = false;
    public static List<String> cropBlacklist;
    public static List<String> oreList;
    private static String[] ores = {"oreCoal", "oreIron", "oreGold", "oreDiamond", "oreEmerald", "oreRedstone", "oreLapis", "oreCopper", "oreTin"};
    public static List<Integer> oreColors;
    private static int[] colors = {0x343434, 0xBC9980, 0xFCEE4B, 0x5DECF5, 0x17DD62, 0xFF0000, 0x193CB4, 0xFF6D11, 0x8FB0CE};
    public static List<Integer> oreExperience;
    public static int[] experience = {1, 1, 2, 4, 4, 2, 2, 1, 1};
    public static List<String> netherOreList;
    private static String[] netherOres = {"oreQuartz"};
    public static List<Integer> netherOreColors;
    private static int[] netherColors = {0xE5DED5};
    public static List<Integer> netherOreExperience;
    private static int[] netherExperience = {2};
    public static List<String> endOreList;
    private static String[] endOres = {"null"};
    public static List<Integer> endOreColors;
    private static int[] endColors = {0};
    public static List<Integer> endOreExperience;
    private static int[] endExperience = {0};
    private static Configuration cfg;
    private static Property[] serverProperties;
    public static int rareChance = 1;
    public static int uncommonChance = 15;
    public static int commonChance = 85;
    public static int combinedChance;

    private static Property resetJson;

    private static Path configDir;
    private static Path jsonDir;
    public static Path lootDir;

    public static void init(File file) {
        configDir = file.getParentFile().toPath().resolve("levelup2");
        jsonDir = configDir.resolve("json");
        lootDir = jsonDir.resolve("loot_tables");
        cfg = new Configuration(file);
        serverProperties = new Property[] {
                cfg.get(Configuration.CATEGORY_GENERAL, "Reset class on death", resetClassOnDeath, "Does the player lose all levels on death?"),
                cfg.get(Configuration.CATEGORY_GENERAL, "Furnace ejects bonus items", furnaceEjection, "Does the furnace eject doubled items?"),
                cfg.get(Configuration.CATEGORY_GENERAL, "Sword skill damage scaling", damageScaling, "Get additional attack power if a mob's max HP is over 20"),
                cfg.get(Configuration.CATEGORY_GENERAL, "Always drop ore chunks", alwaysDropChunks, "Always drop ore chunks on ore harvest")
        };
        cropBlacklist = Arrays.asList(cfg.getStringList("Crops for farming", "Blacklist", new String[] {""}, "Crops that won't be affected by farming growth skill, uses internal block name. No sync to client required."));
        oreList = Arrays.asList(cfg.get(Configuration.CATEGORY_GENERAL, "Surface Ores to double", ores, "Ores that double from mining efficiency").getStringList());
        oreColors = getColorsFromProperty(cfg.get(Configuration.CATEGORY_GENERAL, "Surface Ore colors", colors, "Colors for the surface ore item"));
        oreExperience = getColorsFromProperty(cfg.get(Configuration.CATEGORY_GENERAL, "Surface Ore experience", experience, "XP credit levels for Mining Specialization"));
        netherOreList = Arrays.asList(cfg.get(Configuration.CATEGORY_GENERAL, "Nether Ores to double", netherOres, "Nether ores that double from mining efficiency").getStringList());
        netherOreColors = getColorsFromProperty(cfg.get(Configuration.CATEGORY_GENERAL, "Nether Ore colors", netherColors, "Colors for the nether ore item"));
        netherOreExperience = getColorsFromProperty(cfg.get(Configuration.CATEGORY_GENERAL, "Nether Ore experience", netherExperience, "XP credit level for Mining Specialization"));
        endOreList = Arrays.asList(cfg.get(Configuration.CATEGORY_GENERAL, "End Ores to double", endOres, "End ores that double from mining efficiency").getStringList());
        endOreColors = getColorsFromProperty(cfg.get(Configuration.CATEGORY_GENERAL, "End Ore colors", endColors, "Colors for the end ore item"));
        endOreExperience = getColorsFromProperty(cfg.get(Configuration.CATEGORY_GENERAL, "End Ore experience", endExperience, "XP credit level for Mining Specialization"));
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

    public static Property[] getServerProperties() {
        return serverProperties;
    }

    public static void useServerProperties() {
        resetClassOnDeath = serverProperties[0].getBoolean();
        furnaceEjection = serverProperties[1].getBoolean();
        damageScaling = serverProperties[2].getBoolean();
        alwaysDropChunks = serverProperties[3].getBoolean();
    }

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
        JsonTransfer.findResources(files).stream().forEach(r -> JsonTransfer.copyResource(r, configDir.resolve(r), resetJsonFiles));
        Library.registerLootTableLocations(files);
    }
}
