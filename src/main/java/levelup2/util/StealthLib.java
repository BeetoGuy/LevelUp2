package levelup2.util;

import levelup2.skills.SkillRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class StealthLib {
    private static final ResourceLocation STEALTH = new ResourceLocation("levelup", "stealth");

    public static boolean canSeePlayer(EntityLivingBase entity) {
        EntityPlayer player = entity.world.getClosestPlayerToEntity(entity, 16D);
        return player != null && entity.canEntityBeSeen(player) && (!player.isSneaking() || entityHasVisionOf(entity, player));
    }

    public static boolean entityHasVisionOf(EntityLivingBase living, EntityPlayer player) {
        if (living == null || player == null) {
            return false;
        }
        if (getDistanceFrom(living, player) > 256F - SkillRegistry.getSkillLevel(player, STEALTH) * 12.8F) {
            return false;
        }
        return living.canEntityBeSeen(player) && entityIsFacing(player, living);
    }

    public static float getDistanceFrom(EntityLivingBase living, EntityLivingBase living1) {
        return MathHelper.floor((living1.posX - living.posX) * (living1.posX - living.posX) + (living1.posZ - living.posZ) * (living1.posZ - living.posZ));
    }

    public static boolean entityIsFacing(EntityLivingBase entityLiving, EntityLivingBase entityliving1) {
        if (entityLiving == null || entityliving1 == null) {
            return false;
        }
        float f = -(float) (entityliving1.posX - entityLiving.posX);
        float f1 = (float) (entityliving1.posZ - entityLiving.posZ);
        float f2 = entityLiving.rotationYaw;
        if (f2 < 0.0F) {
            float f3 = (MathHelper.floor(MathHelper.abs(f2) / 360F) + 1.0F) * 360F;
            f2 = f3 + f2;
        } else {
            while (f2 > 360F) {
                f2 -= 360F;
            }
        }
        float f4 = (float) ((Math.atan2(f, f1) * 180F) / Math.PI);
        if (f < 0.0F) {
            f4 = 360F + f4;
        }
        return compareAngles(f2, f4, 22.5F);
    }

    private static boolean compareAngles(float f, float f1, float f2) {
        if (MathHelper.abs(f - f1) < f2) {
            return true;
        }
        if (f + f2 >= 360F) {
            if ((f + f2) - 360F > f1) {
                return true;
            }
        }
        if (f1 + f2 >= 360F) {
            if ((f1 + f2) - 360F > f) {
                return true;
            }
        }
        return false;
    }
}
