/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.machines.item;


import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.EnchantmentFilter;
import com.yogpc.qp.machines.base.IHandleButton;
import com.yogpc.qp.machines.base.QuarryBlackList;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.mover.EnchantmentMessage;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import scala.jdk.javaapi.CollectionConverters;

@OnlyIn(Dist.CLIENT)
public class GuiEnchList extends ContainerScreen<ContainerEnchList> implements BooleanConsumer, IHandleButton {
    public static final int Toggle_id = 10, Remove_id = 12;
    private GuiSlotEnchList slot;
    private final EnchantmentFilter.Accessor tile;
    private final Enchantment target;

    public GuiEnchList(ContainerEnchList c, PlayerInventory i, ITextComponent t) {
        super(c, i, t);
        this.target = ForgeRegistries.ENCHANTMENTS.getValue(c.enchantmentName);
        this.tile = c.tile;
    }

    public boolean include() {
        if (this.target == Enchantments.FORTUNE)
            return this.tile.enchantmentFilter().fortuneInclude();
        return this.tile.enchantmentFilter().silktouchInclude();
    }

    private Set<QuarryBlackList.Entry> getBlockDataList(Enchantment enchantment) {
        if (enchantment == Enchantments.SILK_TOUCH) {
            return CollectionConverters.asJava(tile.enchantmentFilter().silktouchList());
        } else if (enchantment == Enchantments.FORTUNE) {
            return CollectionConverters.asJava(tile.enchantmentFilter().fortuneList());
        } else {
            QuarryPlus.LOGGER.error(String.format("GuiEnchList target is %s", enchantment));
            return Collections.emptySet();
        }
    }

    @Override
    public void func_231160_c_() {
        int width = this.field_230708_k_;
        int height = this.field_230709_l_;
        this.xSize = width;
        this.ySize = height;
        super.func_231160_c_(); // must be here!
//        PacketHandler.sendToServer(BlockListRequestMessage.create(inventorySlots.windowId));
        func_230480_a_(new IHandleButton.Button(-1,
            width / 2 - 125, height - 26, 250, 20, new TranslationTextComponent(TranslationKeys.DONE), this));
        func_230480_a_(new IHandleButton.Button(Toggle_id,
            width * 2 / 3 + 10, 140, 100, 20, "", this));
        func_230480_a_(new IHandleButton.Button(Remove_id,
            width * 2 / 3 + 10, 110, 100, 20, new TranslationTextComponent(TranslationKeys.DELETE), this));
        this.slot = new GuiSlotEnchList(this.getMinecraft(), width * 3 / 5, height - 60, 30, height - 30,
            18, this);
        this.field_230705_e_.add(slot);
        this.func_231035_a_(slot);
    }

    @Override
    public void actionPerformed(final IHandleButton.Button par1) {
        switch (par1.id) {
            case -1:
                this.getMinecraft().player.closeScreen();
                break;
            case Remove_id:
                this.getMinecraft().displayGuiScreen(new ConfirmScreen(this, new TranslationTextComponent(TranslationKeys.DELETE_BLOCK_SURE),
                    new StringTextComponent(Optional.ofNullable(this.slot.func_230958_g_()).map(GuiSlotEnchList.Entry::getData).map(Object::toString).orElse("None"))));
                break;
            default: //maybe toggle
                PacketHandler.sendToServer(EnchantmentMessage.create(tile, EnchantmentMessage.Type.Toggle, target,
                    new QuarryBlackList.Name(new ResourceLocation("dummy:toggle_button"))));
                break;
        }
    }

    @Override
    public void accept(boolean result) {
        GuiSlotEnchList.Entry selected = this.slot.func_230958_g_();
        if (selected != null && result) {
            final QuarryBlackList.Entry entry = selected.getData();
            PacketHandler.sendToServer(EnchantmentMessage.create(tile, EnchantmentMessage.Type.Remove, target, entry));

            if (target == Enchantments.SILK_TOUCH)
                tile.enchantmentFilter_$eq(tile.enchantmentFilter().removeSilktouch(entry));
            else if (target == Enchantments.FORTUNE)
                tile.enchantmentFilter_$eq(tile.enchantmentFilter().removeFortune(entry));
            refreshList();
        }
        this.getMinecraft().displayGuiScreen(this);
    }

    public void refreshList() {
        this.slot.refreshList();
        this.slot.func_241215_a_(null);
    }

    @Override
    protected void func_230450_a_(MatrixStack matrixStack,final float k, final int i, final int j) {
        if (slot != null)
            this.slot.func_230430_a_(matrixStack, i, j, k);
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, final int i, final int j) {
        func_238471_a_(matrixStack, this.field_230712_o_, I18n.format(TranslationKeys.QP_ENABLE_LIST, I18n.format(this.target.getName())),
            this.xSize / 2, 8, 0xFFFFFF); // drawCenteredString
    }

    @Override
    public void func_231023_e_() {
        super.func_231023_e_();
        this.field_230710_m_.get(1).func_238482_a_(new TranslationTextComponent(include() ? TranslationKeys.TOF_INCLUDE : TranslationKeys.TOF_EXCLUDE));
        this.field_230710_m_.get(2).field_230693_o_ = !getBlockDataList(target).isEmpty();
    }

    public void buildModList(Consumer<GuiSlotEnchList.Entry> modListViewConsumer, Function<QuarryBlackList.Entry, GuiSlotEnchList.Entry> newEntry) {
        getBlockDataList(target).stream().map(newEntry).forEach(modListViewConsumer);
    }
}
