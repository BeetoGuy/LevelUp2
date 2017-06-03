package levelup2.skills.crafting;

import levelup2.api.IPlayerSkill;
import levelup2.api.IProcessor;
import levelup2.capability.CapabilityBrewingStand;
import levelup2.capability.PlayerCapability;
import levelup2.skills.BaseSkill;
import levelup2.util.Library;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BrewingEfficiencyBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:brewingspeed";
    }

    @Override
    public int getSkillRow() {
        return 1;
    }

    @Override
    public int getSkillColumn() {
        return 0;
    }

    @Override
    public byte getSkillType() {
        return 1;
    }

    @Override
    public int getLevelCost(int currentLevel) {
        if (currentLevel >= 0 && currentLevel < getMaxLevel())
            return Library.fiveLevels[currentLevel];
        return -1;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public String[] getPrerequisites() {
        return new String[] {"levelup:furnacespeed"};
    }

    @Override
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.BREWING_STAND);
    }

    @SubscribeEvent
    public void registerStandCapability(AttachCapabilitiesEvent<TileEntity> evt) {
        if (evt.getObject() instanceof TileEntityBrewingStand) {
            final TileEntityBrewingStand stand = (TileEntityBrewingStand)evt.getObject();
            evt.addCapability(new ResourceLocation("levelup", "furnacemods"), new ICapabilitySerializable<NBTTagCompound>() {
                IProcessor instance = new CapabilityBrewingStand(stand);

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                    return capability == PlayerCapability.MACHINE_PROCESSING;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                    return capability == PlayerCapability.MACHINE_PROCESSING ? PlayerCapability.MACHINE_PROCESSING.<T>cast(instance) : null;
                }

                @Override
                public NBTTagCompound serializeNBT() {
                    return ((NBTTagCompound)PlayerCapability.MACHINE_PROCESSING.getStorage().writeNBT(PlayerCapability.MACHINE_PROCESSING, instance, null));
                }

                @Override
                public void deserializeNBT(NBTTagCompound tag) {
                    PlayerCapability.MACHINE_PROCESSING.getStorage().readNBT(PlayerCapability.MACHINE_PROCESSING, instance, null, tag);
                }
            });
        }
    }
}
