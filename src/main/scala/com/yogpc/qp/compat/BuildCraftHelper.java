package com.yogpc.qp.compat;

//import buildcraft.api.tools.IToolWrench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

public class BuildCraftHelper {

    public static boolean isWrench(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        /*if (ModAPIManager.INSTANCE.hasAPI(QuarryPlus.Optionals.Buildcraft_tools)) {
            if (IToolWrench.class.isInstance(wrench.getItem())) {
                IToolWrench wrenchItem = (IToolWrench) wrench.getItem();
                if (wrenchItem.canWrench(player, hand, wrench, rayTrace)) {
                    wrenchItem.wrenchUsed(player, hand, wrench, rayTrace);
                    return true;
                }
            }
        }*/
        return false;
    }
}
