package levelup2.event;

import levelup2.config.LevelUpConfig;
import levelup2.skills.SkillRegistry;
import levelup2.util.StealthLib;
import levelup2.util.Library;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.EnumAction;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class CombatSkillHandler {
    public static final CombatSkillHandler INSTANCE = new CombatSkillHandler();

    private CombatSkillHandler() {}

    private static final ResourceLocation ARROWSPEED = new ResourceLocation("levelup", "arrowspeed");
    private static final ResourceLocation ARROWDRAW = new ResourceLocation("levelup", "bowdraw");
    private static final ResourceLocation NATURALARMOR = new ResourceLocation("levelup", "naturalarmor");
    private static final ResourceLocation SHIELDBLOCK = new ResourceLocation("levelup", "shieldblock");
    private static final ResourceLocation STEALTHDAMAGE = new ResourceLocation("levelup", "stealthdamage");
    private static final ResourceLocation STEALTHSPEED = new ResourceLocation("levelup", "stealthspeed");
    private static final ResourceLocation SWORDCRIT = new ResourceLocation("levelup", "swordcrit");
    private static final ResourceLocation SWORDDAMAGE = new ResourceLocation("levelup", "sworddamage");
    private static final ResourceLocation COMBATBONUS = new ResourceLocation("levelup", "combat_bonus");
    private static final ResourceLocation FALLDAMAGE = new ResourceLocation("levelup", "fallprotect");
    private static final ResourceLocation SPRINTSPEED = new ResourceLocation("levelup", "sprintspeed");

    @SubscribeEvent
    public void onArrowLoose(EntityJoinWorldEvent evt) {
        if (evt.getEntity() instanceof EntityArrow) {
            EntityArrow arrow = (EntityArrow)evt.getEntity();
            if (arrow.shootingEntity instanceof EntityPlayer && SkillRegistry.getPlayer(((EntityPlayer)arrow.shootingEntity)).isActive()) {
                int archer = SkillRegistry.getSkillLevel((EntityPlayer)arrow.shootingEntity, ARROWSPEED);
                if (archer > 0) {
                    double divisor = CapabilityEventHandler.getDivisor(ARROWSPEED);
                    arrow.motionX *= 1.0F + archer / divisor;
                    arrow.motionY *= 1.0F + archer / divisor;
                    arrow.motionZ *= 1.0F + archer / divisor;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBowUse(ArrowNockEvent evt) {
        int archery = SkillRegistry.getSkillLevel(evt.getEntityPlayer(), ARROWDRAW);
        if (archery > 0 && SkillRegistry.getPlayer(evt.getEntityPlayer()).isActive()) {
            evt.getEntityPlayer().setActiveHand(evt.getHand());
            setItemUseCount(evt.getEntityPlayer(), (int)(archery / CapabilityEventHandler.getDivisor(ARROWDRAW)));
            evt.setAction(new ActionResult<>(EnumActionResult.SUCCESS, evt.getBow()));
        }
    }

    private void setItemUseCount(EntityPlayer player, int archery) {
        player.activeItemStackUseCount -= archery;
    }

    @SubscribeEvent
    public void onDamageTaken(LivingHurtEvent evt) {
        if (evt.getEntityLiving() instanceof EntityPlayer && SkillRegistry.getPlayer((EntityPlayer)evt.getEntityLiving()).isActive()) {
            EntityPlayer player = (EntityPlayer)evt.getEntityLiving();
            int skill = SkillRegistry.getSkillLevel(player, NATURALARMOR);
            if (skill > 0) {
                if (!evt.getSource().isUnblockable()) {
                    float amount = evt.getAmount() * (float)(1.0F - skill / CapabilityEventHandler.getDivisor(NATURALARMOR));
                    evt.setAmount(amount);
                }
            }
            skill = SkillRegistry.getSkillLevel(player, SHIELDBLOCK);
            if (skill > 0) {
                if (isBlocking(player) && player.getRNG().nextFloat() < skill / CapabilityEventHandler.getDivisor(SHIELDBLOCK)) {
                    evt.setAmount(0F);
                }
            }
            skill = SkillRegistry.getSkillLevel(player, FALLDAMAGE);
            if (skill > 0 && evt.getSource() == DamageSource.FALL) {
                float divisor = (float)(1D / CapabilityEventHandler.getDivisor(FALLDAMAGE));
                float reduction = Math.min(skill * divisor, 0.9F);
                evt.setAmount(evt.getAmount() * (1.0F - reduction));
                return;
            }
        }
        DamageSource src = evt.getSource();
        float dmg = evt.getAmount();
        if (src.getTrueSource() instanceof EntityPlayer && SkillRegistry.getPlayer((EntityPlayer)src.getTrueSource()).isActive()) {
            EntityPlayer player = (EntityPlayer)src.getTrueSource();
            int level = SkillRegistry.getSkillLevel(player, STEALTHDAMAGE);
            if (level > 0) {
                if (src instanceof EntityDamageSourceIndirect) {
                    if (StealthLib.getDistanceFrom(evt.getEntityLiving(), player) < 256F && player.isSneaking() && !StealthLib.canSeePlayer(evt.getEntityLiving()) && !StealthLib.entityIsFacing(evt.getEntityLiving(), player)) {
                        dmg *= 1.0F + (0.15F * level);
                        player.sendStatusMessage(new TextComponentTranslation("sneak.attack", 1.0 + (0.15 * level)), true);
                    }
                } else {
                    if (player.isSneaking() && !StealthLib.canSeePlayer(evt.getEntityLiving()) && !StealthLib.entityIsFacing(evt.getEntityLiving(), player)) {
                        dmg *= 1.0F + (0.3F * level);
                        player.sendStatusMessage(new TextComponentTranslation("sneak.attack", 1.0 + (0.3 * level)), true);
                    }
                }
            }
            level = SkillRegistry.getSkillLevel(player, SWORDCRIT);
            if (level > 0) {
                if (!(src instanceof EntityDamageSourceIndirect)) {
                    if (!player.getHeldItemMainhand().isEmpty()) {
                        if (player.getRNG().nextDouble() <= level / CapabilityEventHandler.getDivisor(SWORDCRIT))
                            dmg *= 2.0F;
                    }
                }
            }
            level = SkillRegistry.getSkillLevel(player, SWORDDAMAGE);
            if (level > 0 && !(src instanceof EntityDamageSourceIndirect)) {
                if (!player.getHeldItemMainhand().isEmpty()) {
                    dmg *= 1.0F + level / CapabilityEventHandler.getDivisor(SWORDDAMAGE);
                    if (LevelUpConfig.damageScaling && !(evt.getEntityLiving() instanceof EntityPlayer) && evt.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue() > 20) {
                        double health = evt.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue();
                        float skillOutput = level / 40F;
                        dmg += Math.min(health * skillOutput, health * 0.375F);
                    }
                }
            } else if (SkillRegistry.getSkillLevel(player, ARROWSPEED) > 0 && src.getDamageType().equals("arrow")) {
                level = SkillRegistry.getSkillLevel(player, ARROWSPEED);
                dmg *= 1.0F + level / (float)(2F * CapabilityEventHandler.getDivisor(ARROWSPEED));
                if (LevelUpConfig.damageScaling && !(evt.getEntityLiving() instanceof EntityPlayer) && evt.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue() > 20) {
                    double health = evt.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue();
                    float skillOutput = level / 40F;
                    dmg += Math.min(health * skillOutput, health * 0.375F);
                }
            }

            if (dmg != evt.getAmount()) {
                evt.setAmount(dmg);
            }
        }
    }

    private boolean isBlocking(EntityPlayer player) {
        return player.isHandActive() && !player.getActiveItemStack().isEmpty() && player.getActiveItemStack().getItem().getItemUseAction(player.getActiveItemStack()) == EnumAction.BLOCK;
    }

    @SubscribeEvent
    public void onTargetSet(LivingSetAttackTargetEvent evt) {
        if (evt.getTarget() instanceof EntityPlayer && evt.getEntityLiving() instanceof EntityMob) {
            if (evt.getTarget().isSneaking() && !StealthLib.entityHasVisionOf(evt.getEntityLiving(), (EntityPlayer)evt.getTarget())
                    && evt.getEntityLiving().getRevengeTimer() != ((EntityMob) evt.getEntityLiving()).ticksExisted) {
                ((EntityMob) evt.getEntityLiving()).setAttackTarget(null);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerSneak(TickEvent.PlayerTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START && SkillRegistry.getPlayer(evt.player).isActive()) {
            int skill = SkillRegistry.getSkillLevel(evt.player, STEALTHSPEED);
            if (skill > 0) {
                IAttributeInstance attrib = evt.player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                AttributeModifier mod = new AttributeModifier(Library.sneakID, "SneakingSkillSpeed", skill / CapabilityEventHandler.getDivisor(STEALTHSPEED), 2);
                if (evt.player.isSneaking()) {
                    if (attrib.getModifier(Library.sneakID) == null)
                        attrib.applyModifier(mod);
                }
                else if (attrib.getModifier(Library.sneakID) != null)
                    attrib.removeModifier(mod);
            }
            else if (SkillRegistry.getSkillLevel(evt.player, SPRINTSPEED) > 0) {
                IAttributeInstance attrib = evt.player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                AttributeModifier mod = new AttributeModifier(Library.speedID, "SprintingSkillSpeed", SkillRegistry.getSkillLevel(evt.player, SPRINTSPEED) / (float)CapabilityEventHandler.getDivisor(SPRINTSPEED), 2);
                if (evt.player.isSprinting()) {
                    if (attrib.getModifier(Library.speedID) == null)
                        attrib.applyModifier(mod);
                }
                else if (attrib.getModifier(Library.speedID) != null)
                    attrib.removeModifier(mod);
            }
        }
    }

    @SubscribeEvent
    public void getCombatBonus(LivingDeathEvent evt) {
        if (evt.getEntityLiving() instanceof EntityMob && evt.getSource().getTrueSource() instanceof EntityPlayer) {
            if (SkillRegistry.getSkillLevel((EntityPlayer)evt.getSource().getTrueSource(), COMBATBONUS) > 0) {
                int deathXP = (int) evt.getEntityLiving().getMaxHealth();
                SkillRegistry.addExperience((EntityPlayer) evt.getSource().getTrueSource(), deathXP / (int)CapabilityEventHandler.getDivisor(COMBATBONUS));
            }
        }
    }
}
