package simelectricity.essential.coverpanel;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import simelectricity.essential.utils.SEUnitHelper;
import simelectricity.essential.utils.client.gui.SEGuiContainer;

public class GuiVoltageSensor extends SEGuiContainer<ContainerVoltageSensor> {
    public GuiVoltageSensor(ContainerVoltageSensor screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        //draw text and stuff here
        //the parameters for drawString are: string, x, y, color

    	this.font.drawString(matrixStack, title.getString(), 8, 6, 4210752);

    	this.font.drawString(matrixStack, I18n.format("gui.simelectricity.voltage_threshold"), 18, 124, 4210752);

        this.font.drawString(matrixStack, SEUnitHelper.getVoltageStringWithUnit(this.container.thresholdVoltage), 20, 51, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float opacity, int par2, int par3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.textureManager.bindTexture(new ResourceLocation("sime_essential:textures/gui/voltage_sensor.png"));
        blit(matrixStack, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        blit(matrixStack, this.guiLeft + 70, this.guiTop + 30, this.container.inverted ? 52 : 0, 166, 52, 33);
        blit(matrixStack, this.guiLeft + 130, this.guiTop + 36, this.container.emitRedStoneSignal ? 180 : 176, 0, 4, 16);
    }

    @Override
    public void init() {
        super.init();
        int xbase = 18;
        int ybase = 97;

        addServerButton(6, this.guiLeft + xbase, this.guiTop + ybase, 140, 20, I18n.format("gui.sime_essential.redstone_toggle_behavior"));

        addServerButton(0, this.guiLeft + xbase, this.guiTop + ybase + 38, 30, 20, "-100");
        addServerButton(1, this.guiLeft + xbase + 30, this.guiTop + ybase + 38, 20, 20, "-10");
        addServerButton(2, this.guiLeft + xbase + 50, this.guiTop + ybase + 38, 20, 20, "-1");
        addServerButton(3, this.guiLeft + xbase + 70, this.guiTop + ybase + 38, 20, 20, "+1");
        addServerButton(4, this.guiLeft + xbase + 90, this.guiTop + ybase + 38, 20, 20, "+10");
        addServerButton(5, this.guiLeft + xbase + 110, this.guiTop + ybase + 38, 30, 20, "+100");
    }
}
