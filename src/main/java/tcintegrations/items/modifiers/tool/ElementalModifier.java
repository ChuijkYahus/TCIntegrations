package tcintegrations.items.modifiers.tool;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import net.minecraftforge.common.Tags;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipeCache;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import tcintegrations.common.TagManager;
import tcintegrations.items.modifiers.traits.ManaModifier;
import tcintegrations.util.BotaniaHelper;

import tcintegrations.TCIntegrations;

public class ElementalModifier extends ManaModifier implements MeleeHitModifierHook, ProcessLootModifierHook {

    private static final int MANA_PER_DAMAGE = 70;

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.PROCESS_LOOT);
    }

    @Override
    public int getManaPerDamage(ServerPlayer player) {
        return BotaniaHelper.getManaPerDamageBonus(player, MANA_PER_DAMAGE);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        final Player player = context.getPlayerAttacker() != null ? (Player) context.getPlayerAttacker() : null;

        if (player != null && !player.level.isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;
            ItemStack stack = sp.getItemInHand(InteractionHand.MAIN_HAND);

            if (TCIntegrations.RANDOM.nextFloat() <= 0.05F) {
                BotaniaHelper.spawnPixie(sp, stack, context.getLivingTarget());
            }
        }
    }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> generatedLoot, LootContext context) {
        if (!context.hasParam(LootContextParams.DAMAGE_SOURCE)) {
            return;
        }

        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);

        if (entity != null && !entity.level.isClientSide && entity.getType().is(TagManager.EntityTypes.ELEMENTAL_SEVERING_MOBS)) {
            if (generatedLoot.stream().noneMatch(stack -> stack.is(Tags.Items.HEADS))) {
                List<SeveringRecipe> recipes = SeveringRecipeCache.findRecipe(context.getLevel().getRecipeManager(), entity.getType());

                if (!recipes.isEmpty()) {
                    float chance = 0.0769F;
                    EntityType<?> entityType = entity.getType();

                    if (entityType == EntityType.SKELETON || entityType == EntityType.WITHER_SKELETON) {
                        chance = 0.1154F;
                    }

                    for (SeveringRecipe recipe : recipes) {
                        ItemStack result = recipe.getOutput(entity);

                        if (!result.isEmpty() && TCIntegrations.RANDOM.nextFloat() <= chance) {
                            if (result.getCount() > 1) {
                                result.setCount(TCIntegrations.RANDOM.nextInt(result.getCount()) + 1);
                            }

                            generatedLoot.add(result);
                        }
                    }
                }
            }
        }
    }

}
