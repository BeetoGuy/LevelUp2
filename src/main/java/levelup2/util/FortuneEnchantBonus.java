package levelup2.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;

import java.util.Random;

public class FortuneEnchantBonus extends LootFunction {
    private final RandomValueRange count;
    private final int limit;
    public FortuneEnchantBonus(LootCondition[] conditions, RandomValueRange count, int limit) {
        super(conditions);
        this.count = count;
        this.limit = limit;
    }

    @Override
    public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
        Entity entity = context.getKillerPlayer();

        if (entity != null && entity instanceof EntityLivingBase) {
            int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantment.getEnchantmentByLocation("fortune"), (EntityLivingBase)entity);

            if (i == 0)
                return stack;

            float f = (float)i * this.count.generateFloat(rand);
            int size = stack.getCount() + Math.round(f);

            if (this.limit != 0 && size > this.limit) {
                stack.setCount(this.limit);
            }
            else {
                stack.setCount(size);
            }
        }
        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<FortuneEnchantBonus>
    {
        protected Serializer()
        {
            super(new ResourceLocation("fortune_enchant"), FortuneEnchantBonus.class);
        }

        @Override
        public void serialize(JsonObject object, FortuneEnchantBonus functionClazz, JsonSerializationContext serializationContext)
        {
            object.add("count", serializationContext.serialize(functionClazz.count));

            if (functionClazz.limit > 0)
            {
                object.add("limit", serializationContext.serialize(Integer.valueOf(functionClazz.limit)));
            }
        }

        @Override
        public FortuneEnchantBonus deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn)
        {
            int i = JsonUtils.getInt(object, "limit", 0);
            return new FortuneEnchantBonus(conditionsIn, (RandomValueRange)JsonUtils.deserializeClass(object, "count", deserializationContext, RandomValueRange.class), i);
        }
    }
}
