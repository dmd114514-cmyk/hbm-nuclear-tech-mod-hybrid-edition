package com.hbm.inventory.fluid.tank;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import api.hbm.fluid.IFluidTank;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidForgeBridge;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.IGuiTooltipHost;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.util.BobMathUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public class FluidTank implements Cloneable, IFluidHandler, IFluidTank {

	public static final FluidTank[] EMPTY_ARRAY = new FluidTank[0];

	/**
	 * The fill/drain handler list. It used to new three handlers straight into a static block; now content init registers them.
	 * Why: FluidLoaderStandard depends on FluidContainerRegistry, deleted in 1.12.2,
	 * needing a rewrite as FluidUtil + IFluidHandlerItem capability style — another batch of files' work.
	 * Static-initializing here would deadlock this class on the un-migrated loaders, blocking its own landing,
	 * so registration moved to content init; with an empty list, fill/drain simply do nothing — no crash.
	 */
	public static final List<FluidLoadingHandler> loadingHandlers = new ArrayList<FluidLoadingHandler>();

	/** The infinite bucket: no two-slot drain. Injected at content init so this class never back-depends on ModItems */
	public static Item infiniteBarrel = null;

	public static final Set<Item> noDualUnload = new HashSet<Item>();

	protected FluidType type;
	protected int fluid;
	protected int maxFluid;
	protected int pressure = 0;

	public FluidTank(FluidType type, int maxFluid) {
		this.type = type;
		this.maxFluid = maxFluid;
	}

	public FluidTank withPressure(int pressure) {
		if(this.pressure != pressure) this.setFill(0);
		this.pressure = pressure;
		return this;
	}

	public void setFill(int i) { fluid = i; }

	public void setTankType(FluidType type) {
		if(type == null) type = Fluids.NONE;
		if(this.type == type) return;

		this.type = type;
		this.setFill(0);
	}

	public void resetTank() {
		this.type = Fluids.NONE;
		this.fluid = 0;
		this.pressure = 0;
	}

	/** Adjusts type and pressure from a given fluid stack; typically for recipe-driven cell swaps */
	public FluidTank conform(FluidStack stack) {
		this.setTankType(stack.type);
		this.withPressure(stack.pressure);
		return this;
	}

	public FluidType getTankType() { return type; }
	public int getFill() { return fluid; }
	public int getMaxFill() { return maxFluid; }
	public int getPressure() { return pressure; }

	public int changeTankSize(int size) {
		maxFluid = size;

		if(fluid > maxFluid) {
			int dif = fluid - maxFluid;
			fluid = maxFluid;
			return dif;
		}
		return 0;
	}

	//pouring from the tank into an outside container
	public boolean loadTank(int in, int out, ItemStack[] slots) {
		if(slots[in].isEmpty()) return false;

		boolean isInfiniteBarrel = infiniteBarrel != null && slots[in].getItem() == infiniteBarrel;
		if(!isInfiniteBarrel && pressure != 0) return false;

		int prev = this.getFill();

		for(FluidLoadingHandler handler : loadingHandlers) {
			if(handler.emptyItem(slots, in, out, this)) {
				break;
			}
		}

		return this.getFill() > prev;
	}

	//drawing from outside into the tank
	public boolean unloadTank(int in, int out, ItemStack[] slots) {
		if(slots[in].isEmpty()) return false;

		int prev = this.getFill();

		for(FluidLoadingHandler handler : loadingHandlers) {
			if(handler.fillItem(slots, in, out, this)) {
				break;
			}
		}

		return this.getFill() < prev;
	}

	public boolean setType(int in, ItemStack[] slots) {
		return setType(in, in, slots);
	}

	/** Swaps the cell type, true on success; same in and out swaps in place, different ones move the contents to out */
	public boolean setType(int in, int out, ItemStack[] slots) {

		if(!slots[in].isEmpty() && slots[in].getItem() instanceof IItemFluidIdentifier) {
			IItemFluidIdentifier id = (IItemFluidIdentifier) slots[in].getItem();

			//a GUI has no position; pass null — the identifier handles it
			if(in == out) {
				FluidType newType = id.getType(null, null, slots[in]);

				if(type != newType) {
					type = newType;
					fluid = 0;
					return true;
				}

			} else if(slots[out].isEmpty()) {
				FluidType newType = id.getType(null, null, slots[in]);
				if(type != newType) {
					type = newType;
					slots[out] = slots[in].copy();
					slots[in] = ItemStack.EMPTY;
					fluid = 0;
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Draws the fluid texture into a GUI by fill height
	 * @param x the tank's left edge
	 * @param y the tank's bottom edge (the old system's convention; too costly to change now)
	 * @param z the GUI's zLevel
	 */
	@SideOnly(Side.CLIENT) public void renderTank(int x, int y, double z, int width, int height) {
		renderTank(x, y, z, width, height, 0);
	}

	@SideOnly(Side.CLIENT) public void renderTank(int x, int y, double z, int width, int height, int orientation) {

		GlStateManager.enableBlend();

		int color = type.getTint();
		double r = ((color & 0xff0000) >> 16) / 255D;
		double g = ((color & 0x00ff00) >> 8) / 255D;
		double b = ((color & 0x0000ff) >> 0) / 255D;
		GlStateManager.color((float) r, (float) g, (float) b, 1F);

		y -= height;

		Minecraft.getMinecraft().renderEngine.bindTexture(type.getTexture());

		int i = (fluid * height) / maxFluid;

		double minX = x;
		double maxX = x;
		double minY = y;
		double maxY = y;

		double minV = 1D - i / 16D;
		double maxV = 1D;
		double minU = 0D;
		double maxU = width / 16D;

		if(orientation == 0) {
			maxX += width;
			minY += height - i;
			maxY += height;
		}

		if(orientation == 1) {
			i = (fluid * width) / maxFluid;
			maxX += i;
			maxY += height;

			minV = 0D;
			maxV = height / 16D;
			minU = 1D;
			maxU = 1D - i / 16D;
		}

		//1.12.2's Tessellator has no instance/vertex methods anymore; grab the buffer yourself and endVertex every point
		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(minX, maxY, z).tex(minU, maxV).endVertex();
		buf.pos(maxX, maxY, z).tex(maxU, maxV).endVertex();
		buf.pos(maxX, minY, z).tex(maxU, minV).endVertex();
		buf.pos(minX, minY, z).tex(minU, minV).endVertex();
		Tessellator.getInstance().draw();

		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.disableBlend();
	}

	@SideOnly(Side.CLIENT) public void renderTankInfo(IGuiTooltipHost host, int mouseX, int mouseY, int x, int y, int width, int height) {
		if(x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY) {

			List<String> list = new ArrayList<String>();
			list.add(this.type.getLocalizedName());
			list.add(fluid + "/" + maxFluid + "mB");

			if(this.pressure != 0) {
				list.add(TextFormatting.RED + "Pressure: " + this.pressure + " PU");
				list.add((BobMathUtil.getBlink() ? TextFormatting.RED : TextFormatting.DARK_RED) + "Pressurized, use compressor!");
			}

			type.addInfo(list);
			GUIElements.drawHoveringTextFluid(list, mouseX, mouseY, host.getFontRenderer(), host.getItemRenderer(), host.getScaledWidth(), host.getScaledHeight(), this.type);
		}
	}

	//called by the TE, saving the fill
	public void writeToNBT(NBTTagCompound nbt, String s) {
		nbt.setInteger(s, fluid);
		nbt.setInteger(s + "_max", maxFluid);
		nbt.setInteger(s + "_type", type.getID());
		nbt.setShort(s + "_p", (short) pressure);
	}

	//called by the TE, reading the fill
	public void readFromNBT(NBTTagCompound nbt, String s) {
		fluid = nbt.getInteger(s);
		int max = nbt.getInteger(s + "_max");
		if(max > 0)
			maxFluid = max;

		//clamp by the effective capacity; the old code clamped against the just-read max, which is 0 when the key is missing and would wipe the fill
		fluid = MathHelper.clamp(fluid, 0, maxFluid);

		type = Fluids.fromNameCompat(nbt.getString(s + "_type")); //old-name compat
		if(type == Fluids.NONE)
			type = Fluids.fromID(nbt.getInteger(s + "_type"));

		this.pressure = nbt.getShort(s + "_p");
	}

	// ——— Forge IFluidHandler: the outside sees only the "cell" layer; FluidType is a definition class and takes no part in I/O ———

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[] {
				new FluidTankProperties(this.getForgeStack(), maxFluid, true, this.canDrainForeign()) };
	}

	@Override
	public int fill(net.minecraftforge.fluids.FluidStack resource, boolean doFill) {
		if(resource == null || resource.amount <= 0) return 0;

		FluidType incoming = FluidForgeBridge.getFluidType(resource.getFluid());
		if(incoming == null || incoming == Fluids.NONE) return 0;

		if(this.type != incoming) {
			//an empty can takes the incoming fluid's type; a used can doesn't blend in anyone else's
			if(this.type != Fluids.NONE || this.fluid > 0) return 0;
			if(doFill) this.setTankType(incoming);
		}

		int accepted = Math.min(resource.amount, this.maxFluid - this.fluid);
		if(accepted <= 0) return 0;
		if(doFill) this.setFill(this.fluid + accepted);
		return accepted;
	}

	@Override
	public net.minecraftforge.fluids.FluidStack drain(net.minecraftforge.fluids.FluidStack resource, boolean doDrain) {
		if(resource == null) return null;
		if(FluidForgeBridge.getFluidType(resource.getFluid()) != this.type) return null;
		return this.drain(resource.amount, doDrain);
	}

	@Override
	public net.minecraftforge.fluids.FluidStack drain(int maxDrain, boolean doDrain) {
		if(maxDrain <= 0 || this.fluid <= 0) return null;
		//the pressurized can's door is reserved for pressure-aware home gear; outside ambient-pressure draws are refused wholesale
		if(this.pressure != 0) return null;

		Fluid fluid = FluidForgeBridge.getFluid(this.type);
		if(fluid == null) return null;

		int drained = Math.min(maxDrain, this.fluid);
		if(drained <= 0) return null;
		if(doDrain) this.setFill(this.fluid - drained);

		return new net.minecraftforge.fluids.FluidStack(fluid, drained);
	}

	/** The tank's contents as a Forge fluid stack; null when empty or unrecognized */
	private net.minecraftforge.fluids.FluidStack getForgeStack() {
		if(this.fluid <= 0) return null;
		Fluid fluid = FluidForgeBridge.getFluid(this.type);
		return fluid == null ? null : new net.minecraftforge.fluids.FluidStack(fluid, this.fluid);
	}

	/** A pressurized can gives nothing to outsiders */
	private boolean canDrainForeign() {
		return this.pressure == 0;
	}

	public void serialize(ByteBuf buf) {
		buf.writeInt(fluid);
		buf.writeInt(maxFluid);
		buf.writeInt(type.getID());
		buf.writeShort((short) pressure);
	}

	public void deserialize(ByteBuf buf) {
		fluid = buf.readInt();
		maxFluid = buf.readInt();
		type = Fluids.fromID(buf.readInt());
		pressure = buf.readShort();
	}
}
