package levelup2.skills.mining;

import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

public class XPBonusMining extends BaseSkill {
    @Override
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public String getSkillName() {
        return "levelup:mining_bonus";
    }

    @Override
    public int getLevelCost(int currentLevel) {
        return -1;
    }

    @Override
    public byte getSkillType() {
        return 0;
    }

    @Override
    public String[] getPrerequisites() {
        return new String[0];
    }

    @SubscribeEvent
    public void giveMiningXP(BlockEvent.HarvestDropsEvent evt) {
        if (evt.getHarvester() != null && !evt.getWorld().isRemote) {
            if (SkillRegistry.getSkillLevel(evt.getHarvester(), getSkillName()) > 0) {
                IBlockState state = evt.getState();
                ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
                boolean oreFound = false;
                for (String ore : SkillRegistry.getOreBonusXP().keySet()) {
                    if (SkillRegistry.listContains(stack, OreDictionary.getOres(ore))) {
                        SkillRegistry.addExperience(evt.getHarvester(), SkillRegistry.getOreBonusXP().get(ore));
                        oreFound = true;
                        break;
                    }
                }
                if (!oreFound) {
                    for (ItemStack s : evt.getDrops()) {
                        String ore = Library.getOreNameForBlock(s);
                        if (ore != null && SkillRegistry.getOreBonusXP().keySet().contains(ore)) {
                            SkillRegistry.addExperience(evt.getHarvester(), SkillRegistry.getOreBonusXP().get(ore));
                            oreFound = true;
                            break;
                        }
                    }
                    if (!oreFound) {
                        for (ItemStack s : evt.getDrops()) {
                            String ore = Library.getOreNameForBlock(s);
                            if (ore != null && ore.startsWith("ore")) {
                                int xp = state.getBlock().getHarvestLevel(state) + 1;
                                SkillRegistry.addExperience(evt.getHarvester(), xp);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getSkillRow() {
        return 0;
    }

    @Override
    public int getSkillColumn() {
        return 0;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.DIAMOND_PICKAXE);
    }

    @Override
    public boolean isMaxLevel(int level) {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean hasExternalJson() {
        return false;
    }
}
