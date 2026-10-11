package com.hbm.entity.train;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.rail.IRailNTM.TrackGauge;
import com.hbm.inventory.container.ContainerBase;
import com.hbm.inventory.gui.GuiInfoContainer;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The flatbed cargo tram: powered car, 29-slot hold + one battery slot, driver's seat on the rear half's roof.
 *
 * The old `shouldRiderSit()` didn't come over: it overrode a 1.7 Entity method, deleted in this version;
 * and seating here is posed directly by updatePassenger, so that switch has no counterpart in the tree and would be dead code.
 * The GUI opener (the old item) lives on the item side; this class just readies container and GUI per IGUIProvider.
 */
public class TrainCargoTram extends EntityRailCarElectric implements IGUIProvider {

	/*
	 *     _________
	 *    | |       \          <--
	 *    | |       |___
	 *    | |       |  |                             |
	 * _O\|_|_______|__|_____________________________|/O_
	 * |____|                                      |____|
	 *    \__________________________________________/
	 *        '( + )'                      '( + )'
	 * 
	 */

	public TrainCargoTram(World world) {
		super(world);
		this.setSize(5F, 2F);
	}

	@Override public double getPoweredAcceleration() { return 0.01; }
	@Override public double getPassivBrake() { return 0.95; }
	@Override public boolean shouldUseEngineBrake(EntityPlayer player) { return Math.abs(this.engineSpeed) < 0.1; }
	@Override public double getMaxPoweredSpeed() { return 0.5; }
	@Override public double getMaxRailSpeed() { return 1; }

	@Override public TrackGauge getGauge() { return TrackGauge.STANDARD; }
	@Override public double getLengthSpan() { return 1.5; }
	@Override public double getCollisionSpan() { return 2.5; }
	@Override public Vec3d getRiderSeatPosition() { return new Vec3d(0.375, 2.375, 0.5); }
	@Override public int getSizeInventory() { return 29; }
	@Override public String getName() { return this.hasCustomName() ? this.getEntityName() : "container.trainTram"; }
	@Override public double getCouplingDist(TrainCoupling coupling) { return coupling != null ? 2.75 : 0; }

	@Override public int getMaxPower() { return this.getPowerConsumption() * 100; }
	@Override public int getPowerConsumption() { return 10; }
	@Override public boolean hasChargeSlot() { return true; }
	@Override public int getChargeSlot() { return 28; }

	@Override
	public DummyConfig[] getDummies() {
		return new DummyConfig[] {
				new DummyConfig(2F, 1F, new Vec3d(0, 0, 1.5)),
				new DummyConfig(2F, 1F, new Vec3d(0, 0, 0)),
				new DummyConfig(2F, 1F, new Vec3d(0, 0, -1.5))
		};
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if(!this.world.isRemote && !this.isDead) {
			this.setDead();
		}

		return true;
	}

	@Override
	public Vec3d[] getPassengerSeats() {
		return new Vec3d[] {
				new Vec3d(0.5, 1.75, -1.5),
				new Vec3d(-0.5, 1.75, -1.5)
		};
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new ContainerTrainCargoTram(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new GUITrainCargoTram(player.inventory, this);
	}

	/*
	 * ##### ##### #   # ##### ##### ### #   # ##### ####
	 * #     #   # ##  #   #   #   #  #  ##  # #     #   #
	 * #     #   # # # #   #   #####  #  # # # ###   ####
	 * #     #   # #  ##   #   #   #  #  #  ## #     #   #
	 * ##### ##### #   #   #   #   # ### #   # ##### #   #
	 */
	/** 4×7 hold + charge slot; the only special case is "battery to the charge slot, everything else to the hold" */
	public static class ContainerTrainCargoTram extends ContainerBase {

		public ContainerTrainCargoTram(InventoryPlayer invPlayer, TrainCargoTram train) {
			super(invPlayer, train);
			this.addSlots(train, 0, 8, 18, 4, 7);
			this.addSlotToContainer(new Slot(train, 28, 152, 72));
			this.playerInv(invPlayer, 8, 122, 180);
		}

		@Override
		public ItemStack transferStackInSlot(EntityPlayer player, int index) {

			Slot slot = inventorySlots.get(index);
			if(slot == null || !slot.getHasStack())
				return ItemStack.EMPTY;

			ItemStack slotStack = slot.getStack();
			ItemStack original = slotStack.copy();
			int invSize = tile.getSizeInventory();

			if(index < invSize) {
				if(!mergeItemStack(slotStack, invSize, inventorySlots.size(), true)) return ItemStack.EMPTY;
			} else if(slotStack.getItem() instanceof IBatteryItem) {
				if(!mergeItemStack(slotStack, 28, 29, false)) return ItemStack.EMPTY;
			} else {
				if(!mergeItemStack(slotStack, 0, 28, false)) return ItemStack.EMPTY;
			}

			if(slotStack.getCount() == 0)
				slot.putStack(ItemStack.EMPTY);
			else
				slot.onSlotChanged();

			slot.onTake(player, slotStack);
			return original;
		}
	}

	/*
	 * ##### #   # ###
	 * #     #   #  #
	 * #  ## #   #  #
	 * #   # #   #  #
	 * ##### ##### ###
	 */
	@SideOnly(Side.CLIENT)
	public static class GUITrainCargoTram extends GuiInfoContainer {

		private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/vehicles/gui_cargo_tram.png");
		private TrainCargoTram train;

		public GUITrainCargoTram(InventoryPlayer invPlayer, TrainCargoTram train) {
			super(new ContainerTrainCargoTram(invPlayer, train));
			this.train = train;
			this.xSize = 176;
			this.ySize = 204;
		}

		@Override
		public void drawScreen(int x, int y, float interp) {
			super.drawScreen(x, y, interp);
			this.drawElectricityInfo(this, x, y, guiLeft + 152, guiTop + 18, 16, 52, train.getPower(), train.getMaxPower());
		}

		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			String name = this.train.hasCustomName() ? this.train.getName() : I18nUtil.format(this.train.getName());
			this.fontRenderer.drawString(name, 140 / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 0xffffff);
			this.fontRenderer.drawString(I18nUtil.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float interp, int mouseX, int mouseY) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

			int i = train.getPower() * 53 / train.getMaxPower();
			drawTexturedModalRect(guiLeft + 152, guiTop + 70 - i, 176, 52 - i, 16, i);

			if(train.getPower() > train.getPowerConsumption()) {
				drawTexturedModalRect(guiLeft + 156, guiTop + 4, 176, 52, 9, 12);
			}
		}
	}
}
