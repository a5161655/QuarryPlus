package com.kotori316.marker.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.yogpc.qp.machines.base.IHandleButton;
import com.yogpc.qp.machines.base.ScreenUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import com.kotori316.marker.Marker;
import com.kotori316.marker.Tile16Marker;
import com.kotori316.marker.packet.Button16Message;
import com.kotori316.marker.packet.PacketHandler;

public class Gui16Marker extends ContainerScreen<ContainerMarker> implements IHandleButton {
    private static final ResourceLocation LOCATION = new ResourceLocation(Marker.modID, "textures/gui/marker.png");
    private static final int CHUNK = 16;
    private final Tile16Marker marker;
    private static final int BUTTON_WIDTH = 40;

    public Gui16Marker(ContainerMarker containerMarker, PlayerInventory inv, ITextComponent component) {
        super(containerMarker, inv, component);
        this.marker = ((Tile16Marker) inv.player.getEntityWorld().getTileEntity(containerMarker.pos));
        //217, 188
        this.xSize = 217;
        this.ySize = 188;
        this.field_238745_s_ = this.ySize - 96 + 2; // y position of text, inventory
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.func_230446_a_(matrixStack);// back ground
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY); // render tooltip
    }

    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        ScreenUtil.color4f();
        this.getMinecraft().getTextureManager().bindTexture(LOCATION);
        this.func_238474_b_(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, final int mouseX, final int mouseY) {
        super.func_230451_b_(matrixStack, mouseX, mouseY);
        FontRenderer font = field_230712_o_;
        font.func_238421_b_(matrixStack, "Size", (this.xSize - font.getStringWidth("Size")) / 2f, 6, 0x404040);
        String sizeText = Integer.toString(marker.getSize() / CHUNK);
        font.func_238421_b_(matrixStack, sizeText, (this.xSize - font.getStringWidth(sizeText)) / 2f, 15 + 23, 0x404040);
        String yMaxText = Integer.toString(marker.max().getY());
        String yMinText = Integer.toString(marker.min().getY());
        font.func_238421_b_(matrixStack, yMaxText, (this.xSize - font.getStringWidth(yMaxText)) / 2f + 10 + BUTTON_WIDTH, 15 + 23, 0x404040);
        font.func_238421_b_(matrixStack, yMinText, (this.xSize - font.getStringWidth(yMinText)) / 2f - 10 - BUTTON_WIDTH, 15 + 23, 0x404040);
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        final int tp = 15;
        final int middle = guiLeft + this.xSize / 2;
        this.func_230480_a_(new IHandleButton.Button(0, middle - BUTTON_WIDTH / 2, guiTop + tp, BUTTON_WIDTH, 20, "+", this));
        this.func_230480_a_(new IHandleButton.Button(1, middle - BUTTON_WIDTH / 2, guiTop + tp + 33, BUTTON_WIDTH, 20, "-", this));
        this.func_230480_a_(new IHandleButton.Button(2, middle + BUTTON_WIDTH / 2 + 10, guiTop + tp, BUTTON_WIDTH, 20, "Top+", this));
        this.func_230480_a_(new IHandleButton.Button(3, middle + BUTTON_WIDTH / 2 + 10, guiTop + tp + 33, BUTTON_WIDTH, 20, "Top-", this));
        this.func_230480_a_(new IHandleButton.Button(4, middle - BUTTON_WIDTH / 2 - 10 - BUTTON_WIDTH, guiTop + tp, BUTTON_WIDTH, 20, "Bottom+", this));
        this.func_230480_a_(new IHandleButton.Button(5, middle - BUTTON_WIDTH / 2 - 10 - BUTTON_WIDTH, guiTop + tp + 33, BUTTON_WIDTH, 20, "Bottom-", this));

    }

    @Override
    public void actionPerformed(Button button) {
        int size = marker.getSize();
        int yMin = marker.min().getY(), yMax = marker.max().getY();
        int n;
        if (Screen.func_231173_s_()) { // Shift
            n = 16;
        } else if (Screen.func_231172_r_()) {
            n = 4;
        } else {
            n = 1;
        }
        switch (button.id) {
            case 0: // Plus
                size = marker.getSize() + CHUNK;
                break;
            case 1: // Minus
                if (marker.getSize() > CHUNK) {
                    size = marker.getSize() - CHUNK;
                } else {
                    size = marker.getSize();
                }
                break;
            case 2:
                yMax = marker.max().getY() + n;
                break;
            case 3:
                yMax = Math.max(marker.max().getY() - n, yMin);
                break;
            case 4:
                yMin = Math.min(marker.min().getY() + n, yMax);
                break;
            case 5:
                yMin = Math.max(marker.min().getY() - n, 0);
                break;
        }
        PacketHandler.sendToServer(new Button16Message(marker.getPos(), PacketHandler.getDimId(marker.getWorld()), size, yMax, yMin));
    }
}
