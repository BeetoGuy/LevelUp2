package levelup2.skills;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import levelup2.api.BaseClass;
import levelup2.api.BaseSkill;
import levelup2.api.ICharacterClass;
import levelup2.api.IPlayerSkill;
import levelup2.capability.PlayerCapability;
import levelup2.config.LevelUpConfig;
import levelup2.config.OreChunkStorage;
import levelup2.items.ItemExperienceBook;
import levelup2.items.ItemOreChunk;
import levelup2.items.ItemRespecBook;
import levelup2.network.SkillPacketHandler;
import levelup2.player.IPlayerClass;
import levelup2.util.*;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SkillRegistry {
    private static Map<String, Integer> oreBonusXP = Maps.newHashMap();
    private static Map<ResourceLocation, IPlayerSkill> skillHashMap = Maps.newHashMap();
    private static Map<ResourceLocation, ICharacterClass> classMap = Maps.newHashMap();
    private static Map<ResourceLocation, SkillProperties> skillProperties = Maps.newHashMap();
    private static Map<ResourceLocation, ClassProperties> classProperties = Maps.newHashMap();
    private static Map<ResourceLocation, List<IPlayerSkill>> specSkillCache = Maps.newHashMap();
    private static List<IPlantable> cropBlacklist = Lists.newArrayList();
    private static List<ResourceLocation> specializations = Lists.newArrayList();
    public static int smallestDisplayColumn = 0;
    public static int smallestDisplayRow = 0;
    public static int largestDisplayColumn = 0;
    public static int largestDisplayRow = 0;

    public static Item surfaceOreChunk = new ItemOreChunk(Library.SURFACE_ORES).setTranslationKey("levelup:surfaceore").setRegistryName(new ResourceLocation("levelup2", "surfaceore"));
    public static Item netherOreChunk = new ItemOreChunk(Library.NETHER_ORES).setTranslationKey("levelup:netherore").setRegistryName(new ResourceLocation("levelup2", "netherore"));
    public static Item endOreChunk = new ItemOreChunk(Library.END_ORES).setTranslationKey("levelup:endore").setRegistryName(new ResourceLocation("levelup2", "endore"));
    public static Item respecBook = new ItemRespecBook().setTranslationKey("levelup:respec").setRegistryName(new ResourceLocation("levelup2", "respecbook"));
    public static Item skillBook = new ItemExperienceBook().setTranslationKey("levelup:skill").setRegistryName(new ResourceLocation("levelup2", "skillbook"));

    public static void loadSkills() {
        addCropsToBlacklist(LevelUpConfig.cropBlacklist);
        Library.registerLootManager();
        loadOreDict();
    }

    public static void postLoadSkills() {/*
        for (IPlayerSkill skill : skillRegistry) {
            if (skill.hasSubscription())
                MinecraftForge.EVENT_BUS.register(skill);
        }*/
        //initPlankCache();
    }

    private static void loadOreDict() {
        List<String> names = Lists.newArrayList();
        for (OreChunkStorage stor : Library.ALL_ORES) {
            names.add(stor.getOreName());
        }
        if (names.contains("oreRedstone")) {
            Library.getOreList().add(Blocks.LIT_REDSTONE_ORE);
        }
        Library.registerOres(names);
    }

    public static void registerRecipes() {
        if (!Library.SURFACE_ORES.isEmpty()) {
            for (OreChunkStorage stor : Library.SURFACE_ORES) {
                stor.setBaseItem(surfaceOreChunk);
                stor.registerOre();
            }
            Library.ALL_ORES.addAll(Library.SURFACE_ORES);
        }
        if (!Library.NETHER_ORES.isEmpty()) {
            for (OreChunkStorage stor : Library.NETHER_ORES) {
                stor.setBaseItem(netherOreChunk);
                stor.registerOre();
            }
            Library.ALL_ORES.addAll(Library.NETHER_ORES);
        }
        if (!Library.END_ORES.isEmpty()) {
            for (OreChunkStorage stor : Library.END_ORES) {
                stor.setBaseItem(endOreChunk);
                stor.registerOre();
            }
            Library.ALL_ORES.addAll(Library.END_ORES);
        }
        registerSmelting();/*
        List<String> names = Lists.newArrayList();
        for (OreChunkStorage stor : Library.ALL_ORES) {
            names.add(stor.getOreName());
        }
        if (names.contains("oreRedstone")) {
            Library.getOreList().add(Blocks.LIT_REDSTONE_ORE);
        }
        Library.registerOres(names);*/
        for (OreChunkStorage stor : Library.ALL_ORES) {
            stor.registerOreIngredientLate();
        }
    }

    private static void registerSmelting() {
        for (OreChunkStorage stor : Library.ALL_ORES) {
            ItemStack result = stor.getSmeltingResult();
            if (!result.isEmpty()) {
                Item item = stor.getBaseItem();
                float exp = FurnaceRecipes.instance().getSmeltingExperience(result);
                GameRegistry.addSmelting(new ItemStack(item, 1, stor.getMetadata()), result, exp);
            }
        }
        SmeltingBlacklist.addItem(new ItemStack(Blocks.SPONGE, 1, 1));
        for (String ore : OreDictionary.getOreNames()) {
            if (ore.startsWith("dust")) {
                SmeltingBlacklist.addOres(ore);
            }
        }
    }

    public static ICharacterClass getClassFromName(ResourceLocation name) {
        if (classMap.containsKey(name)) {
            return classMap.get(name);
        }
        return null;
    }

    public static IPlayerSkill getSkillFromName(ResourceLocation name) {
        if (skillHashMap.containsKey(name)) {
            return skillHashMap.get(name);
        }
        return null;
    }

    public static int getSkillLevel(EntityPlayer player, ResourceLocation skill) {
        return getPlayer(player).getSkillLevel(skill);
    }

    public static IPlayerClass getPlayer(EntityPlayer player) {
        return player.getCapability(PlayerCapability.PLAYER_CLASS, null);
    }

    public static void addStackToOreBonus(String stack, int bonusXP) {
        oreBonusXP.put(stack, bonusXP);
    }

    public static Map<String, Integer> getOreBonusXP() {
        return oreBonusXP;
    }

    public static void addExperience(EntityPlayer player, int amount) {
        player.addExperience(amount);
    }

    public static boolean stackMatches(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty())
            return false;
        else if (stack1.getItemDamage() == OreDictionary.WILDCARD_VALUE || stack2.getItemDamage() == OreDictionary.WILDCARD_VALUE)
            return stack1.getItem() == stack2.getItem();
        return stack1.isItemEqual(stack2);
    }

    public static boolean listContains(ItemStack stack, List<ItemStack> list) {
        if (list.isEmpty()) return false;
        for (ItemStack lStack : list) {
            if (stackMatches(stack, lStack))
                return true;
        }
        return false;
    }

    public static void loadPlayer(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            Map<ResourceLocation, Integer> skills = Maps.newHashMap();
            for (ResourceLocation name : skillHashMap.keySet()) {
                int level = getPlayer(player).getSkillLevel(name, false);
                skills.put(name, level);
            }
            ResourceLocation cl = getPlayer(player).getPlayerClass();
            SkillPacketHandler.initChannel.sendTo(SkillPacketHandler.getSkillPacket(Side.CLIENT, 0, skills, getPlayer(player).getLevelBank(), cl), (EntityPlayerMP)player);
        }
    }

    public static void addCropsToBlacklist(List<String> blacklist) {
        for (String str : blacklist) {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(str));
            if (block != null && block != Blocks.AIR) {
                if (block instanceof IPlantable)
                    cropBlacklist.add((IPlantable)block);
            }
        }
    }

    public static List<IPlantable> getCropBlacklist() {
        return cropBlacklist;
    }

    public static ItemStack getOreEntry(String ore) {
        if (OreDictionary.doesOreNameExist(ore)) {
            if (!OreDictionary.getOres(ore).isEmpty()) {
                for (ItemStack stack : OreDictionary.getOres(ore)) {
                    if (stack.getItem() instanceof ItemBlock) {
                        if (!stack.hasTagCompound()) {
                            return stack;
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static void initPlankCache() {
        PlankCache.refresh();
        for (ItemStack log : OreDictionary.getOres("logWood")) {
            if (log.getItem() instanceof ItemBlock) {
                if (log.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                    for (int i = 0; i < 4; i++) {
                        ItemStack logTest = new ItemStack(log.getItem(), 1, i);
                        ItemStack plank = getPlankOutput(logTest);
                        if (!plank.isEmpty()) {
                            registerLog(logTest, plank);
                        }
                    }
                }
                else {
                    ItemStack plank = getPlankOutput(log);
                    if (!plank.isEmpty()) {
                        registerLog(log, plank);
                    }
                }
            }
        }
    }

    private static void registerLog(ItemStack log, ItemStack plank) {
        Block block = ((ItemBlock)log.getItem()).getBlock();
        ItemStack plankCopy = plank.copy();
        plankCopy.setCount(plank.getCount() / 2);
        PlankCache.addBlock(block, log.getMetadata(), plankCopy);
    }

    private static ItemStack getPlankOutput(ItemStack log) {
        Iterator<IRecipe> it = CraftingManager.REGISTRY.iterator();
        ItemStack stack = ItemStack.EMPTY;
        while (it.hasNext() && stack.isEmpty()) {
            IRecipe recipe = it.next();
            NonNullList<Ingredient> ing = recipe.getIngredients();
            if (isPlank(recipe.getRecipeOutput())) {
                for (Ingredient in : ing) {
                    for (ItemStack check : in.getMatchingStacks()) {
                        if (check.isItemEqual(log)) {
                                stack = recipe.getRecipeOutput().copy();
                        }
                    }
                }
            }
        }
        return stack;
    }

    private static boolean isPlank(ItemStack output) {
        for (ItemStack plank : OreDictionary.getOres("plankWood")) {
            if (plank.getMetadata() == OreDictionary.WILDCARD_VALUE)
                return output.getItem() == plank.getItem();
            else if (plank.isItemEqual(output))
                return true;
        }
        return false;
    }

    public static void resetForNewProps() {
        skillHashMap.clear();
        classMap.clear();
        specializations.clear();
        specSkillCache.clear();
    }

    public static void addClass(ICharacterClass charClass) {
        classMap.put(charClass.getClassName(), charClass);
    }

    public static void addSkill(IPlayerSkill skill) {
        skillHashMap.put(skill.getSkillName(), skill);
    }

    public static Map<ResourceLocation, IPlayerSkill> getSkills() {
        return skillHashMap;
    }

    public static Map<ResourceLocation, ICharacterClass> getClasses() {
        return classMap;
    }

    public static List<ResourceLocation> getSpecializations() {
        return specializations;
    }

    public static List<IPlayerSkill> getSkillsForSpec(ResourceLocation location) {
        return specSkillCache.get(location);
    }

    public static void registerSkillProperties() {
        try {
            Files.walk(LevelUpConfig.skillDir).filter(Files::isRegularFile).forEach(l -> loadSkillsFromJson(l));
            Files.walk(LevelUpConfig.classDir).filter(Files::isRegularFile).forEach(l -> loadClassesFromJson(l));
        } catch(IOException e) {
            e.printStackTrace();
        }
        calculateHighLow();
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static void loadSkillsFromJson(Path location) {
        if (!Files.exists(location)) {
            return;
        }

        try (Reader read = Files.newBufferedReader(location)) {
            SkillProperties prop = SkillProperties.fromJson(JsonUtils.fromJson(gson, read, JsonObject.class));
            skillProperties.put(prop.getName(), prop);
            skillHashMap.put(prop.getName(), BaseSkill.fromProps(prop));
        } catch (IOException | JsonParseException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private static void loadClassesFromJson(Path location) {
        if (!Files.exists(location)) {
            return;
        }

        try (Reader read = Files.newBufferedReader(location)) {
            ClassProperties prop = ClassProperties.fromJson(JsonUtils.fromJson(gson, read, JsonObject.class));
            classProperties.put(prop.getClassName(), prop);
            classMap.put(prop.getClassName(), BaseClass.fromProperties(prop));
        } catch (IOException | JsonParseException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static ClassProperties getProperty(ICharacterClass cl) {
        return classProperties.get(cl.getClassName());
    }

    public static SkillProperties getProperty(IPlayerSkill skill) {
        return skillProperties.get(skill.getSkillName());
    }

    public static void calculateHighLow() {
        largestDisplayRow = 0;
        largestDisplayColumn = 0;
        smallestDisplayRow = 0;
        smallestDisplayColumn = 0;
        for (ResourceLocation skill : skillHashMap.keySet()) {
            IPlayerSkill sk = getSkillFromName(skill);
            if (sk != null) {
                if (!specializations.contains(sk.getSkillType())) {
                    specializations.add(sk.getSkillType());
                    specSkillCache.put(sk.getSkillType(), Lists.newArrayList());
                }
                getSkillsForSpec(sk.getSkillType()).add(sk);
                int column = sk.getSkillColumn();
                int row = sk.getSkillRow();
                if (column > largestDisplayColumn) largestDisplayColumn = column;
                else if (column < smallestDisplayColumn) smallestDisplayColumn = column;
                if (row > largestDisplayRow) largestDisplayRow = row;
                else if (row < smallestDisplayRow) smallestDisplayRow = row;
            }
        }
    }
}
