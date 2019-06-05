package com.yogpc.qp.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ContainerListTemplate extends Container {
    public final IInventory craftMatrix = new InventoryBasic("Matrix", false, 1) {
        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack) {
            return stack.getItem() instanceof ItemBlock;
        }
    };

    public ContainerListTemplate(EntityPlayer player) {
        addSlotToContainer(new SlotTile(this.craftMatrix, 0, 141, 90));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 135 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new SlotCanTake(player.inventory, k, 8 + k * 18, 193, k != player.inventory.currentItem));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        playerIn.inventory.placeItemBackInInventory(playerIn.world, craftMatrix.getStackInSlot(0));
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
