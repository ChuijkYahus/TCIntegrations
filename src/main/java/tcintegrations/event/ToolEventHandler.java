package tcintegrations.event;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import tcintegrations.TCIntegrations;
import tcintegrations.items.modifiers.hooks.ArmorCrouchModifierHook;
import tcintegrations.items.modifiers.hooks.ArmorJumpModifierHook;

@Mod.EventBusSubscriber(modid = TCIntegrations.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ToolEventHandler {

    @SubscribeEvent
    static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity living = event.getEntityLiving();

        if (!living.isSpectator() && !living.level.isClientSide() && living.isAlive()) {
            ItemStack helmet = living.getItemBySlot(EquipmentSlot.HEAD);

            if (!helmet.isEmpty() && helmet.is(TinkerTags.Items.HELMETS)) {
                ToolStack tool = ToolStack.from(helmet);

                for (ModifierEntry modifier : tool.getModifierList()) {
                    ArmorCrouchModifierHook crouchModifier = modifier.getModifier().getModule(ArmorCrouchModifierHook.class);

                    if (crouchModifier != null) {
                        if (living.isCrouching() || living.isVisuallySwimming()) {
                            crouchModifier.onCrouch(tool, modifier, living);
                        }
                        else {
                            crouchModifier.onStand(modifier, living);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity living = event.getEntityLiving();

        if (!living.isSpectator() && !living.level.isClientSide() && living.isAlive()) {
            ItemStack boots = living.getItemBySlot(EquipmentSlot.FEET);

            if (!boots.isEmpty() && boots.is(TinkerTags.Items.BOOTS)) {
                ToolStack tool = ToolStack.from(boots);

                for (ModifierEntry entry : tool.getModifierList()) {
                    ArmorJumpModifierHook jumpModifier = entry.getModifier().getModule(ArmorJumpModifierHook.class);

                    if (jumpModifier != null) {
                        jumpModifier.onJump(tool, living);
                    }
                }
            }
        }

    }

}