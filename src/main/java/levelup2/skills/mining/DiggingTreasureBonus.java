package levelup2.skills.mining;

import levelup2.config.LevelUpConfig;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Random;

public class DiggingTreasureBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:treasurehunting";
    }

    @Override
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public byte getSkillType() {
        return 0;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.GOLDEN_SHOVEL);
    }

    @SubscribeEvent
    public void dirtLooting(BlockEvent.HarvestDropsEvent evt) {
        if (!isActive()) return;
        if (evt.getHarvester() != null && !evt.getWorld().isRemote) {
            Random rand = evt.getHarvester().getRNG();
            int skill = SkillRegistry.getSkillLevel(evt.getHarvester(), getSkillName());
            if (skill > 0 && !evt.isSilkTouching()) {
                if (!evt.getDrops().isEmpty() && SkillRegistry.listContains(evt.getDrops().get(0), OreDictionary.getOres("dirt"))) {
                    ItemStack drop = evt.getDrops().get(0).copy();
                    if (rand.nextFloat() <= skill / 20F) {
                        ItemStack loot = getDigLoot(evt.getHarvester());
                        System.out.println("ItemStack: " + loot.toString());
                        if (!loot.isEmpty()) {
                            Library.removeFromList(evt.getDrops(), drop);
                            evt.getDrops().add(loot.copy());
                        }
                    }
                }
            }
        }
    }

    private ItemStack getDigLoot(EntityPlayer player) {
        if (!player.getEntityWorld().isRemote) {
            LootContext.Builder build = new LootContext.Builder(((WorldServer)player.getEntityWorld())).withPlayer(player);
            build.withLuck((float) EnchantmentHelper.getMaxEnchantmentLevel(Enchantment.getEnchantmentByLocation("fortune"), player) + player.getLuck());
            ResourceLocation location = getTableFromWeightedPool(player.getRNG());
            if (location != null)
                return Library.getLootManager().getLootTableFromLocation(location).generateLootForPools(player.getRNG(), build.build()).get(0);
        }
        return ItemStack.EMPTY;
    }

    private ResourceLocation getTableFromWeightedPool(Random rand) {
        if (LevelUpConfig.combinedChance == 0)
            return null;
        int RNG = rand.nextInt(LevelUpConfig.combinedChance);
        if (LevelUpConfig.rareChance > 0 && RNG <= LevelUpConfig.rareChance) {
            return new ResourceLocation("levelup", "digging/rare_dig");
        }
        else if (LevelUpConfig.uncommonChance > 0 && RNG <= LevelUpConfig.rareChance + LevelUpConfig.uncommonChance) {
            return new ResourceLocation("levelup", "digging/uncommon_dig");
        }
        else
            return new ResourceLocation("levelup", "digging/common_dig");
    }
}
