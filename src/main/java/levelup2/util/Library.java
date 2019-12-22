package levelup2.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import levelup2.config.LevelUpConfig;
import levelup2.config.OreChunkStorage;
import levelup2.skills.SkillRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public class Library {
    public static int[] tenLevels = {5, 7, 11, 13, 17, 23, 29, 31, 37, 41};
    public static int[] fiveLevels = {11, 17, 29, 37, 41};
    public static int[] highTenLevels = {11, 17, 23, 29, 37, 41, 43, 47, 51, 53};
    public static final UUID speedID = UUID.fromString("4f7637c8-6106-4050-96cb-e47f83bfa415");
    public static final UUID sneakID = UUID.fromString("a4dc0b04-f78a-43f6-8805-5ebfbab10b18");
    public static final ResourceLocation SKILL_LOCATION = new ResourceLocation("levelup", "skills");
    public static List<OreChunkStorage> ALL_ORES = Lists.newArrayList();
    public static List<OreChunkStorage> SURFACE_ORES = Lists.newArrayList();
    public static List<OreChunkStorage> NETHER_ORES = Lists.newArrayList();
    public static List<OreChunkStorage> END_ORES = Lists.newArrayList();
    private static Set<Block> ores = Sets.newIdentityHashSet();
    private static Map<String, ItemStack> oreToChunk = Maps.newHashMap();
    private static Set<ResourceLocation> LOOT_TABLES = Sets.newHashSet();
    private static LevelUpLootManager LEVELUP_MANAGER;
    public static Set<String> CLASSES;
    public static Set<String> SKILLS;

    public static EntityPlayer getPlayerFromUsername(String username) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            return null;
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(username);
    }

    public static EntityPlayer getPlayerFromUUID(UUID uuid) {
        return getPlayerFromUsername(getUsernameFromUUID(uuid));
    }

    public static String getUsernameFromUUID(UUID uuid) {
        return UsernameCache.getLastKnownUsername(uuid);
    }

    public static Set<Block> getOreList() {
        return ores;
    }

    public static ItemStack getOreChunk(ItemStack stack, Random rand, int fortune) {
        for (OreChunkStorage stor : ALL_ORES) {
            if (stor.oreMatches(stack)) {
                return stor.getHarvestItem(rand, fortune);
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getChunkFromName(String oreName, int fortune) {
        if (oreToChunk.containsKey(oreName)) {
            ItemStack chunk = oreToChunk.get(oreName).copy();
            chunk.grow(fortune);
            return chunk;
        }
        return ItemStack.EMPTY;
    }

    public static void registerOres(List<String> oreNames) {
        for (String ore : oreNames) {
            if (OreDictionary.doesOreNameExist(ore)) {
                if (!OreDictionary.getOres(ore).isEmpty()) {
                    for (ItemStack stack : OreDictionary.getOres(ore)) {
                        if (stack.getItem() instanceof ItemBlock) {
                            Block block = ((ItemBlock)stack.getItem()).getBlock();
                            if (!ores.contains(block))
                                ores.add(block);
                        }
                    }
                }
            }
        }
        if (!LevelUpConfig.oreBlocks.isEmpty()) {
            for (String ore : LevelUpConfig.oreBlocks) {
                Block block = Block.REGISTRY.getObject(new ResourceLocation(ore));
                if (block != Blocks.AIR) {
                    ores.add(block);
                }
            }
        }
    }

    public static void assignExperienceValues(List<String> oreNames, List<Integer> oreValue) {
        for (int i = 0; i < oreNames.size(); i++) {
            String oreName = oreNames.get(i);
            if (!oreName.equals("null")) {
                int value = i < oreValue.size() ? oreValue.get(i) : 1;
                if (value > 0)
                    SkillRegistry.addStackToOreBonus(oreName, value);
            }
        }
    }

    public static int getExperienceYield(ItemStack blockState) {
        for (OreChunkStorage stor : ALL_ORES) {
            if (stor.oreMatches(blockState)) {
                return stor.getExperienceYield();
            }
        }
        return 0;
    }

    public static String getOreNameForBlock(ItemStack blockStack) {
        for (OreChunkStorage stor : ALL_ORES) {
            if (stor.oreMatches(blockStack)) {
                return stor.getOreName();
            }
        }/*
        for (String ore : oreNames) {
            List<ItemStack> ores = OreDictionary.getOres(ore);
            for (ItemStack stack : ores) {
                if (ItemStack.areItemsEqual(stack, blockStack) || ItemStack.areItemsEqual(new ItemStack(blockStack.getItem(), 1, OreDictionary.WILDCARD_VALUE), stack)) {
                    return ore;
                }
            }
        }*/
        return null;
    }

    public static boolean isOre(ItemStack blockStack) {
        for (OreChunkStorage stor : ALL_ORES) {
            if (stor.oreMatches(blockStack)) {
                return true;
            }
        }/*
        for (String oreName : OreDictionary.getOreNames()) {
            if (oreName.startsWith("ore")) {
                if (OreDictionary.containsMatch(false, OreDictionary.getOres(oreName), blockStack))
                    return true;
            }
        }*/
        return false;
    }

    public static void registerOreToChunk(List<String> ores, Item item) {
        for (int i = 0; i < ores.size(); i++) {
            oreToChunk.put(ores.get(i), new ItemStack(item, 2, i));
        }
    }

    public static void removeFromList(List<ItemStack> drops, ItemStack toRemove) {
        Iterator<ItemStack> itr = drops.iterator();
        while (itr.hasNext()) {
            ItemStack drop = itr.next();
            if (!drop.isEmpty() && ItemStack.areItemsEqual(toRemove, drop)) {
                itr.remove();
            }
        }
    }

    public static void registerLootManager() {
        LootFunctionManager.registerFunction(new FortuneEnchantBonus.Serializer());
        LEVELUP_MANAGER = new LevelUpLootManager();
    }

    public static LevelUpLootManager getLootManager() {
        return LEVELUP_MANAGER;
    }

    public static Set<ResourceLocation> getLootTables() {
        return LOOT_TABLES;
    }

    public static void registerLootTableLocations(Set<String> files) {
        files.stream().forEach(s -> LOOT_TABLES.add(new ResourceLocation("levelup", s)));
    }

    static {
        CLASSES = Sets.newHashSet();
        CLASSES.add("archaeologist");
        CLASSES.add("artisan");
        CLASSES.add("assassin");
        CLASSES.add("farmer");
        CLASSES.add("fisherman");
        CLASSES.add("hermit");
        CLASSES.add("lumberjack");
        CLASSES.add("miner");
        CLASSES.add("scout");
        CLASSES.add("spelunker");
        CLASSES.add("warrior");
        CLASSES.add("zealot");
        SKILLS = Sets.newHashSet();
        SKILLS.add("arrowspeed");
        SKILLS.add("bowdraw");
        SKILLS.add("brewingspeed");
        SKILLS.add("combat_bonus");
        SKILLS.add("craft_bonus");
        SKILLS.add("cropgrowth");
        SKILLS.add("fallprotect");
        SKILLS.add("fishbonus");
        SKILLS.add("flintloot");
        SKILLS.add("furnacebonus");
        SKILLS.add("furnacespeed");
        SKILLS.add("harvestbonus");
        SKILLS.add("lumbering");
        SKILLS.add("mining_bonus");
        SKILLS.add("naturalarmor");
        SKILLS.add("prospecting");
        SKILLS.add("shieldblock");
        SKILLS.add("sprintspeed");
        SKILLS.add("stealth");
        SKILLS.add("stealthdamage");
        SKILLS.add("stonecutting");
        SKILLS.add("swordcrit");
        SKILLS.add("sworddamage");
        SKILLS.add("treasurehunting");
        SKILLS.add("woodcutting");
    }
}
