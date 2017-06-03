package levelup2.skills.crafting;

import levelup2.api.IPlayerSkill;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class XPBonusCrafting extends BaseSkill {
    private Random rand = new Random();

    @Override
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public String getSkillName() {
        return "levelup:craft_bonus";
    }

    @Override
    public int getLevelCost(int currentLevel) {
        return -1;
    }

    @Override
    public byte getSkillType() {
        return 1;
    }

    @Override
    public String[] getPrerequisites() {
        return new String[0];
    }

    @SubscribeEvent
    public void onCrafting(PlayerEvent.ItemCraftedEvent evt) {
        if (SkillRegistry.getSkillLevel(evt.player, getSkillName()) > 0) {
            if (isNotOneItemCrafting(evt.craftMatrix)) {
                int craftingChances = getCraftingItems(evt.craftMatrix);
                if (craftingChances > 0) {
                    int experienceGain = 0;
                    for (int i = 0; i < getCraftingItems(evt.craftMatrix); i++) {
                        if (rand.nextFloat() < 0.55F) {
                            experienceGain++;
                        }
                    }
                    if (experienceGain > 0)
                        SkillRegistry.addExperience(evt.player, experienceGain);
                }
            }
        }
    }

    private boolean isNotOneItemCrafting(IInventory inv) {
        boolean notSame = false;
        ItemStack firstStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (!inv.getStackInSlot(i).isEmpty()) {
                if (firstStack.isEmpty()) {
                    firstStack = inv.getStackInSlot(i).copy();
                }
                else if (!firstStack.isItemEqual(inv.getStackInSlot(i)))
                    notSame = true;
            }
        }
        return notSame;
    }

    private int getCraftingItems(IInventory inv) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (!inv.getStackInSlot(i).isEmpty()) {
                ItemStack stack = inv.getStackInSlot(i).copy();
                stack.setCount(1);
                if (items.isEmpty())
                    items.add(stack);
                else if (!SkillRegistry.listContains(stack, items))
                    items.add(stack);
            }
        }
        return items.size();
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
        return new ItemStack(Blocks.CRAFTING_TABLE);
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
    public IPlayerSkill getNewInstance() {
        return new XPBonusCrafting();
    }
}
