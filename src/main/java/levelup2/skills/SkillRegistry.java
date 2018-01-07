package levelup2.skills;

import levelup2.api.IPlayerSkill;
import levelup2.capability.PlayerCapability;
import levelup2.config.LevelUpConfig;
import levelup2.items.ItemOreChunk;
import levelup2.items.ItemRespecBook;
import levelup2.network.SkillPacketHandler;
import levelup2.player.IPlayerClass;
import levelup2.skills.combat.*;
import levelup2.skills.crafting.*;
import levelup2.skills.mining.*;
import levelup2.util.Library;
import levelup2.util.PlankCache;
import levelup2.util.SmeltingBlacklist;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.*;

public class SkillRegistry {
    private static Map<ItemStack, Integer> oreBonusXP = new HashMap<>();
    private static List<IPlayerSkill> skillRegistry = new ArrayList<>();
    private static Map<String, IPlayerSkill> skillHashMap = new HashMap<>();
    private static List<IPlantable> cropBlacklist = new ArrayList<>();
    public static int smallestDisplayColumn = 0;
    public static int smallestDisplayRow = 0;
    public static int largestDisplayColumn = 0;
    public static int largestDisplayRow = 0;

    public static Item surfaceOreChunk = new ItemOreChunk(LevelUpConfig.oreList).setUnlocalizedName("levelup:surfaceore").setRegistryName(new ResourceLocation("levelup2", "surfaceore"));
    public static Item netherOreChunk = new ItemOreChunk(LevelUpConfig.netherOreList).setUnlocalizedName("levelup:netherore").setRegistryName(new ResourceLocation("levelup2", "netherore"));
    public static Item endOreChunk = new ItemOreChunk(LevelUpConfig.endOreList).setUnlocalizedName("levelup:endore").setRegistryName(new ResourceLocation("levelup2", "endore"));
    public static Item respecBook = new ItemRespecBook().setUnlocalizedName("levelup:respec").setRegistryName(new ResourceLocation("levelup2", "respecbook"));

    public static void initItems() {
        GameRegistry.register(surfaceOreChunk);
        GameRegistry.register(netherOreChunk);
        GameRegistry.register(endOreChunk);
        GameRegistry.register(respecBook);
    }

    public static void loadSkills() {
        addSkill(new XPBonusCombat());
        addSkill(new XPBonusCrafting());
        addSkill(new XPBonusMining());
        addSkill(new StoneSpeedBonus());
        addSkill(new StoneMiningBonus());
        addSkill(new WoodSpeedBonus());
        addSkill(new WoodcuttingBonus());
        addSkill(new FlintLootBonus());
        addSkill(new DiggingTreasureBonus());
        addSkill(new SwordCritBonus());
        addSkill(new SwordDamageBonus());
        addSkill(new DrawSpeedBonus());
        addSkill(new ArrowSpeedBonus());
        addSkill(new StealthBonus());
        addSkill(new StealthDamage());
        addSkill(new ShieldBlockBonus());
        addSkill(new NaturalArmorBonus());
        addSkill(new FoodGrowthBonus());
        addSkill(new FoodHarvestBonus());
        addSkill(new SprintSpeedBonus());
        addSkill(new FallDamageBonus());
        addSkill(new StealthSpeed());
        addSkill(new FurnaceEfficiencyBonus());
        addSkill(new FurnaceSmeltBonus());
        addSkill(new BrewingEfficiencyBonus());
        addSkill(new FishingLootBonus());
        addCropsToBlacklist(LevelUpConfig.cropBlacklist);
        Library.registerLootManager();
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Blocks.GRAVEL, 4), new ItemStack(Items.FLINT), new ItemStack(Items.FLINT), new ItemStack(Items.FLINT), new ItemStack(Items.FLINT)));
    }

    public static void postLoadSkills() {
        for (IPlayerSkill skill : skillRegistry) {
            if (skill.hasSubscription())
                MinecraftForge.EVENT_BUS.register(skill);
            int column = skill.getSkillColumn();
            int row = skill.getSkillRow();
            if (column > largestDisplayColumn) largestDisplayColumn = column;
            else if (column < smallestDisplayColumn) smallestDisplayColumn = column;
            if (row > largestDisplayRow) largestDisplayRow = row;
            else if (row < smallestDisplayRow) smallestDisplayRow = row;
        }
        initPlankCache();
    }

    public static void registerRecipes() {
        if (!isNullList(LevelUpConfig.oreList)) {
            Library.registerOreToChunk(LevelUpConfig.oreList, surfaceOreChunk);
            Library.addToOreList(LevelUpConfig.oreList);
            registerSmelting(LevelUpConfig.oreList, surfaceOreChunk);
            registerCrafting(LevelUpConfig.oreList, surfaceOreChunk);
            Library.registerOres(LevelUpConfig.oreList);
            if (LevelUpConfig.oreList.contains("oreRedstone"))
                Library.getOreList().add(Blocks.LIT_REDSTONE_ORE);
        }
        if (!isNullList(LevelUpConfig.netherOreList)) {
            Library.registerOreToChunk(LevelUpConfig.netherOreList, netherOreChunk);
            Library.addToOreList(LevelUpConfig.netherOreList);
            registerSmelting(LevelUpConfig.netherOreList, netherOreChunk);
            registerCrafting(LevelUpConfig.netherOreList, netherOreChunk);
            Library.registerOres(LevelUpConfig.netherOreList);
        }
        if (!isNullList(LevelUpConfig.endOreList)) {
            Library.registerOreToChunk(LevelUpConfig.endOreList, endOreChunk);
            Library.addToOreList(LevelUpConfig.endOreList);
            registerSmelting(LevelUpConfig.endOreList, endOreChunk);
            registerCrafting(LevelUpConfig.endOreList, endOreChunk);
            Library.registerOres(LevelUpConfig.endOreList);
        }
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(respecBook), " P ", "OBO", 'P', Items.DIAMOND_PICKAXE, 'O', "obsidian", 'B', Items.BOOK));
    }

    private static void registerSmelting(List<String> ores, Item item) {
        for (int i = 0; i < ores.size(); i++) {
            String names = ores.get(i);
            if (OreDictionary.doesOreNameExist(names)) {
                ItemStack ore = getOreEntry(names);
                if (!ore.isEmpty()) {
                    if (!FurnaceRecipes.instance().getSmeltingResult(ore).isEmpty()) {
                        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(ore);
                        FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(item, 1, i), result, FurnaceRecipes.instance().getSmeltingExperience(result));
                    }
                }
            }
        }
        SmeltingBlacklist.addItem(new ItemStack(Blocks.SPONGE, 1, 1));
    }

    private static void registerCrafting(List<String> ores, Item item) {
        for (int i = 0; i < ores.size(); i++) {
            String names = ores.get(i);
            if (OreDictionary.doesOreNameExist(names)) {
                ItemStack ore = getOreEntry(names);
                if (!ore.isEmpty()) {
                    ItemStack chunk = new ItemStack(item, 1, i);
                    GameRegistry.addRecipe(new ShapelessOreRecipe(ore.copy(), chunk, chunk));
                    OreDictionary.registerOre(names, chunk);
                }
            }
        }
    }

    private static boolean isNullList(List<String> list) {
        return !list.isEmpty() && list.get(0).equals("null");
    }

    public static List<IPlayerSkill> getSkillRegistry() {
        return skillRegistry;
    }

    public static IPlayerSkill getSkillFromName(String name) {
        if (skillHashMap.containsKey(name)) {
            return skillHashMap.get(name);
        }
        return null;
    }

    public static void addSkill(IPlayerSkill skill) {
        skillRegistry.add(skill);
        skillHashMap.put(skill.getSkillName(), skill);
    }

    public static int getSkillLevel(EntityPlayer player, String skill) {
        return getPlayer(player).getSkillLevel(skill);
    }

    public static IPlayerClass getPlayer(EntityPlayer player) {
        return player.getCapability(PlayerCapability.PLAYER_CLASS, null);
    }

    public static void addStackToOreBonus(ItemStack stack, int bonusXP) {
        oreBonusXP.put(stack, bonusXP);
    }

    public static Map<ItemStack, Integer> getOreBonusXP() {
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

    public static void increaseSkillLevel(EntityPlayer player, String skillName) {
        IPlayerSkill skill = getPlayer(player).getSkillFromName(skillName);
        int skillCost = skill.getLevelCost(getPlayer(player).getSkillLevel(skillName));
        if (skillCost > 0) {
            if (player.experienceLevel >= skillCost) {
                player.experienceLevel -= skillCost;
                getPlayer(player).addToSkill(skillName, 1);
            }
        }
    }

    public static void loadPlayer(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            byte spec = getPlayer(player).getSpecialization();
            Map<String, Integer> skills = getPlayer(player).getSkills();
            SkillPacketHandler.initChannel.sendTo(SkillPacketHandler.getPacket(Side.CLIENT, 0, spec, skills), (EntityPlayerMP)player);
        }
    }

    public static void addCropsToBlacklist(List<String> blacklist) {
        for (String str : blacklist) {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(str));
            if (block != null) {
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
                            return new ItemStack(stack.getItem(), 1, stack.getMetadata());
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static void initPlankCache() {
        for (ItemStack log : OreDictionary.getOres("logWood")) {
            if (log.getItem() != null && log.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock)log.getItem()).getBlock();
                if (log.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                    for (int i = 0; i < 4; i++) {
                        ItemStack planks = getPlankOutput(new ItemStack(log.getItem(), 1, i));
                        if (!planks.isEmpty()) {
                            planks.setCount(2);
                            PlankCache.addBlock(block, i, planks);
                        }
                    }
                }
                else {
                    ItemStack planks = getPlankOutput(log);
                    if (!planks.isEmpty()) {
                        planks.setCount(2);
                        PlankCache.addBlock(block, log.getMetadata(), planks);
                    }
                }
            }
        }
    }

    private static ItemStack getPlankOutput(ItemStack input) {
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        for (IRecipe recipe : recipes) {
            if (recipe instanceof ShapedRecipes) {
                ShapedRecipes shaped = (ShapedRecipes)recipe;
                if (shaped.getRecipeSize() == 1) {
                    if (shaped.recipeItems[0].isItemEqual(input))
                        return shaped.getRecipeOutput().copy();
                }
            }
            else if (recipe instanceof ShapedOreRecipe) {
                ShapedOreRecipe shaped = (ShapedOreRecipe)recipe;
                if (shaped.getRecipeSize() == 1) {
                    if (shaped.getInput()[0] instanceof ItemStack) {
                        ItemStack stack = (ItemStack)shaped.getInput()[0];
                        if (stack.isItemEqual(input))
                            return shaped.getRecipeOutput().copy();
                    }
                }
            }
            else if (recipe instanceof ShapelessRecipes) {
                ShapelessRecipes shapeless = (ShapelessRecipes)recipe;
                if (shapeless.recipeItems.size() == 1 && shapeless.recipeItems.get(0).isItemEqual(input))
                    return shapeless.getRecipeOutput().copy();
            }
            else if (recipe instanceof ShapelessOreRecipe) {
                ShapelessOreRecipe shapeless = (ShapelessOreRecipe)recipe;
                if (shapeless.getRecipeSize() == 1) {
                    if (shapeless.getInput().get(0) instanceof ItemStack) {
                        if (((ItemStack)shapeless.getInput().get(0)).isItemEqual(input))
                            return shapeless.getRecipeOutput().copy();
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
