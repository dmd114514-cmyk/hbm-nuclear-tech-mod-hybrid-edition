package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.util.i18n.I18nUtil;
import com.hbm.lib.RefStrings;
import com.hbm.network.NetworkHandler;
import com.hbm.network.packet.NbtControlPacket;
import com.hbm.util.BobMathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/**
 * The GUI base with the info panel: "hover tooltips, power bars, small icons, scissoring, item rendering" —
 * everything every GUI writes once, collected in one place; concrete GUIs only place their own textures and widgets.
 */
public abstract class GuiInfoContainer extends GuiContainer {

	static final ResourceLocation guiUtil = new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_utility.png");

	public GuiInfoContainer(Container container) {
		super(container);
	}

	public void drawElectricityInfo(GuiInfoContainer gui, int mouseX, int mouseY, int x, int y, int width, int height, long power, long maxPower) {
		if(x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY)
			gui.drawInfo(new String[] { BobMathUtil.getShortNumber(power) + "/" + BobMathUtil.getShortNumber(maxPower) + "HE" }, mouseX, mouseY);
	}

	public void drawCustomInfoStat(int mouseX, int mouseY, int x, int y, int width, int height, int tPosX, int tPosY, String... text) {
		drawCustomInfoStat(mouseX, mouseY, x, y, width, height, tPosX, tPosY, Arrays.asList(text));
	}

	public void drawCustomInfoStat(int mouseX, int mouseY, int x, int y, int width, int height, int tPosX, int tPosY, List<String> text) {
		if(x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY)
			this.drawHoveringText(text, tPosX, tPosY);
	}

	public void drawInfo(String[] text, int x, int y) {
		this.drawHoveringText(Arrays.asList(text), x, y);
	}

	@Override
	public void drawHoveringText(List<String> lines, int x, int y) {
		GUIElements.drawHoveringText(lines, x, y, fontRenderer, itemRender, width, height);
	}

	/** If the machine implements IUpgradeInfoProvider, lay its upgrade effects into the tooltip; otherwise an empty list */
	public List<String> getUpgradeInfo(TileEntity tile) {

		List<String> lines = new ArrayList<String>();

		if(tile instanceof IUpgradeInfoProvider) {
			IUpgradeInfoProvider provider = (IUpgradeInfoProvider) tile;

			lines.add(I18nUtil.resolveKey("upgrade.gui.title"));

			for(UpgradeType type : UpgradeType.values()) {
				if(provider.canProvideInfo(type, 0, false)) {
					int maxLevel = provider.getValidUpgrades().get(type);
					switch(type) {
					case SPEED: lines.add(I18nUtil.resolveKey("upgrade.gui.speed", maxLevel)); break;
					case POWER: lines.add(I18nUtil.resolveKey("upgrade.gui.power", maxLevel)); break;
					case EFFECT: lines.add(I18nUtil.resolveKey("upgrade.gui.effectiveness", maxLevel)); break;
					case AFTERBURN: lines.add(I18nUtil.resolveKey("upgrade.gui.afterburner", maxLevel)); break;
					case OVERDRIVE: lines.add(I18nUtil.resolveKey("upgrade.gui.overdrive", maxLevel)); break;
					default: break;
					}
				}
			}
		}

		return lines;
	}

	@Deprecated
	public void drawCustomInfo(GuiInfoContainer gui, int mouseX, int mouseY, int x, int y, int width, int height, String[] text) {
		if(x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY)
			this.drawHoveringText(Arrays.asList(text), mouseX, mouseY);
	}

	public void drawInfoPanel(int x, int y, int width, int height, int type) {

		Minecraft.getMinecraft().getTextureManager().bindTexture(guiUtil);

		switch(type) {
		case 0: drawTexturedModalRect(x, y, 0, 0, 8, 8); break; //small blue I
		case 1: drawTexturedModalRect(x, y, 0, 8, 8, 8); break; //small green I
		case 2: drawTexturedModalRect(x, y, 8, 0, 16, 16); break; //large blue I
		case 3: drawTexturedModalRect(x, y, 24, 0, 16, 16); break; //large green I
		case 4: drawTexturedModalRect(x, y, 0, 16, 8, 8); break; //small red !
		case 5: drawTexturedModalRect(x, y, 0, 24, 8, 8); break; //small yellow !
		case 6: drawTexturedModalRect(x, y, 8, 16, 16, 16); break; //large red !
		case 7: drawTexturedModalRect(x, y, 24, 16, 16, 16); break; //large yellow !
		case 8: drawTexturedModalRect(x, y, 0, 32, 8, 8); break; //small blue *
		case 9: drawTexturedModalRect(x, y, 0, 40, 8, 8); break; //small gray *
		case 10: drawTexturedModalRect(x, y, 8, 32, 16, 16); break; //large blue *
		case 11: drawTexturedModalRect(x, y, 24, 32, 16, 16); break; //large gray *
		}
	}

	protected boolean isMouseOverSlot(Slot slot, int x, int y) {
		return this.isPointInRegion(slot.xPos, slot.yPos, 16, 16, x, y);
	}

	/** The parent made this slot-lookup method private, but click-drag in the GUI needs it, so a copy is laid out here */
	public Slot getSlotAtPosition(int x, int y) {

		for(int k = 0; k < this.inventorySlots.inventorySlots.size(); ++k) {
			Slot slot = this.inventorySlots.inventorySlots.get(k);

			if(this.isMouseOverSlot(slot, x, y)) {
				return slot;
			}
		}

		return null;
	}

	protected boolean checkClick(int x, int y, int left, int top, int sizeX, int sizeY) {
		return guiLeft + left <= x && guiLeft + left + sizeX > x && guiTop + top < y && guiTop + top + sizeY >= y;
	}

	//for outsiders (numeric readout widgets and friends) to read the GUI rectangle's coordinates
	public int getGuiTop() {
		return this.guiTop;
	}

	public int getGuiLeft() {
		return this.guiLeft;
	}

	public float getZLevel() {
		return this.zLevel;
	}

	public void setZLevel(float level) {
		this.zLevel = level;
	}

	public RenderItem getItemRenderer() {
		return itemRender;
	}

	public FontRenderer getFontRenderer() {
		return this.fontRenderer;
	}

	public void pushScissor(int x, int y, int width, int height) {
		ScaledResolution res = new ScaledResolution(this.mc);
		int scale = res.getScaleFactor();

		//note: the scissor box measures from the screen's bottom up, so Y is inverted
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((guiLeft + x) * scale, (guiTop + ySize - y - height) * scale, width * scale, height * scale);
	}

	public void popScissor() {
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	/** Item rendering with a custom label; GL state is the caller's business */
	protected void drawItemStack(ItemStack stack, int x, int y, String label) {
		GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		this.zLevel = 200.0F;
		itemRender.zLevel = 200.0F;
		FontRenderer font = stack.isEmpty() ? null : stack.getItem().getFontRenderer(stack);
		if(font == null) font = fontRenderer;
		itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		itemRender.renderItemOverlayIntoGUI(font, stack, x, y, label);
		this.zLevel = 0.0F;
		itemRender.zLevel = 0.0F;
	}

	//this class only loads when a GUI first opens on the client, long after item registration — the static init is safe
	public static final ItemStack TEMPLATE_FOLDER = new ItemStack(ModItems.template_folder);

	/** The standardized item rendering for recipe-style GUIs */
	public void renderItem(ItemStack stack, int x, int y) {
		renderItem(stack, x, y, 100F);
	}

	public void renderItem(ItemStack stack, int x, int y, float layer) {
		FontRenderer font = stack.isEmpty() ? null : stack.getItem().getFontRenderer(stack);
		if(font == null) font = fontRenderer;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderHelper.enableGUIStandardItemLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F / 1.0F, 240F / 1.0F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		itemRender.zLevel = layer;
		itemRender.renderItemAndEffectIntoGUI(stack, guiLeft + x, guiTop + y);
		itemRender.zLevel = 0.0F;
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
	}

	/** Draws tooltips line by line: a line can mix strings and item stacks (one Object[] element = one item, sandwiched between text) */
	protected void drawStackText(List<Object[]> lines, int x, int y, FontRenderer font) {

		if(!lines.isEmpty()) {
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);

			int height = 0;
			int longestline = 0;

			for(Object[] line : lines) {
				int lineWidth = 0;
				boolean hasStack = false;

				for(Object o : line) {
					if(o instanceof String) {
						lineWidth += font.getStringWidth((String) o);
					} else {
						lineWidth += 18;
						hasStack = true;
					}
				}

				height += hasStack ? 18 : 10;

				if(lineWidth > longestline) {
					longestline = lineWidth;
				}
			}

			int minX = x + 12;
			int minY = y - 12;

			if(minX + longestline > this.width) {
				minX -= 28 + longestline;
			}

			if(minY + height + 6 > this.height) {
				minY = this.height - height - 6;
			}

			this.zLevel = 400.0F;
			itemRender.zLevel = 400.0F;
			int colorBg = 0xF0100010;
			this.drawGradientRect(minX - 3, minY - 4, minX + longestline + 3, minY - 3, colorBg, colorBg);
			this.drawGradientRect(minX - 3, minY + height + 3, minX + longestline + 3, minY + height + 4, colorBg, colorBg);
			this.drawGradientRect(minX - 3, minY - 3, minX + longestline + 3, minY + height + 3, colorBg, colorBg);
			this.drawGradientRect(minX - 4, minY - 3, minX - 3, minY + height + 3, colorBg, colorBg);
			this.drawGradientRect(minX + longestline + 3, minY - 3, minX + longestline + 4, minY + height + 3, colorBg, colorBg);

			int color0 = 0x505000FF;
			int color1 = (color0 & 0xFEFEFE) >> 1 | color0 & 0xFF000000;
			this.drawGradientRect(minX - 3, minY - 3 + 1, minX - 3 + 1, minY + height + 3 - 1, color0, color1);
			this.drawGradientRect(minX + longestline + 2, minY - 3 + 1, minX + longestline + 3, minY + height + 3 - 1, color0, color1);
			this.drawGradientRect(minX - 3, minY - 3, minX + longestline + 3, minY - 3 + 1, color0, color0);
			this.drawGradientRect(minX - 3, minY + height + 2, minX + longestline + 3, minY + height + 3, color1, color1);

			for(int index = 0; index < lines.size(); ++index) {

				Object[] line = lines.get(index);
				int indent = 0;
				boolean hasStack = false;

				for(Object o : line) {
					if(!(o instanceof String)) {
						hasStack = true;
					}
				}

				for(Object o : line) {

					if(o instanceof String) {
						font.drawStringWithShadow((String) o, minX + indent, minY + (hasStack ? 4 : 0), -1);
						indent += font.getStringWidth((String) o) + 2;
					} else {
						ItemStack stack = (ItemStack) o;
						GL11.glColor3f(1F, 1F, 1F);

						if(stack.getCount() == 0) {
							//an empty stack draws a red frame saying "something belonged here", matching the vanilla recipe book
							this.drawGradientRect(minX + indent - 1, minY - 1, minX + indent + 17, minY + 17, 0xffff0000, 0xffff0000);
							this.drawGradientRect(minX + indent, minY, minX + indent + 16, minY + 16, 0xffb0b0b0, 0xffb0b0b0);
						}
						GL11.glEnable(GL11.GL_DEPTH_TEST);
						RenderHelper.enableGUIStandardItemLighting();
						GL11.glEnable(GL12.GL_RESCALE_NORMAL);
						itemRender.renderItemAndEffectIntoGUI(stack, minX + indent, minY);
						itemRender.renderItemOverlayIntoGUI(this.fontRenderer, stack, minX + indent, minY, null);
						RenderHelper.disableStandardItemLighting();
						GL11.glDisable(GL11.GL_DEPTH_TEST);
						indent += 18;
					}
				}

				if(index == 0) {
					minY += 2;
				}

				minY += hasStack ? 18 : 10;
			}

			this.zLevel = 0.0F;
			itemRender.zLevel = 0.0F;
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		}
	}

	/** One click sends a toggle name to the machine: the machine side answers via the boolean in IControlReceiver.receiveControl */
	public void clickSendFlag(TileEntity tile, int x, int y, int left, int top, int sizeX, int sizeY, String name) {
		if(checkClick(x, y, left, top, sizeX, sizeY)) {
			click();
			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean(name, true);
			NetworkHandler.sendToServer(new NbtControlPacket(data, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ()));
		}
	}

	public void click() {
		mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
}
