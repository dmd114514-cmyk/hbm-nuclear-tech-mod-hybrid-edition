package com.hbm.entity.train;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.rail.IRailNTM.TrackGauge;
import com.hbm.inventory.container.ContainerBase;
import com.hbm.inventory.gui.GuiInfoContainer;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The flatbed cargo tram's trailer: 45 slots of pure hold, no power and no driver's seat.
 *
 * The old interactFirst's door-opening line becomes `EntityPlayer.openGui(...)` here, with x carrying the entity id —
 * the GUI dispatch chain's last branch looks the provider up by entity id via world.getEntityByID.
 * Return value as the old version: false when the base consumed the interaction, true only when this opened the GUI itself.
 */
public class TrainCargoTramTrailer extends EntityRailCarCargo implements IGUIProvider {

	/*
	 *                         <--
	 *                         
	 * _O\____________________________________________/O_
	 * |____|                                      |____|
	 *    \__________________________________________/
	 *        '( + )'                      '( + )'
	 * 
	 */

	public TrainCargoTramTrailer(World world) {
		super(world);
		this.setSize(5F, 2F);
	}

	@Override public double getMaxRailSpeed() { return 1; }
	@Override public TrackGauge getGauge() { return TrackGauge.STANDARD; }
	@Override public double getLengthSpan() { return 1.5; }
	@Override public double getCollisionSpan() { return 2.5; }
	@Override public int getSizeInventory() { return 45; }
	@Override public String getName() { return this.hasCustomName() ? this.getEntityName() : "container.trainTramTrailer"; }
	@Override public double getCouplingDist(TrainCoupling coupling) { return coupling != null ? 2.75 : 0; }
	@Override public double getCurrentSpeed() { return 0; }

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
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {

		if(super.processInitialInteract(player, hand)) return false;

		if(!this.world.isRemote) {
			player.openGui(MainRegistry.instance, 0, this.world, this.getEntityId(), 0, 0);
		}

		return true;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new ContainerTrainCargoTramTrailer(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new GUITrainCargoTramTrailer(player.inventory, this);
	}

	/*
	 * ##### ##### #   # ##### ##### ### #   # ##### ####
	 * #     #   # ##  #   #   #   #  #  ##  # #     #   #
	 * #     #   # # # #   #   #####  #  # # # ###   ####
	 * #     #   # #  ##   #   #   #  #  #  ## #     #   #
	 * ##### ##### #   #   #   #   # ### #   # ##### #   #
	 */
	/** One 5×9 hold, no special-case slots; merging is the base's own logic */
	public static class ContainerTrainCargoTramTrailer extends ContainerBase {

		public ContainerTrainCargoTramTrailer(InventoryPlayer invPlayer, TrainCargoTramTrailer train) {
			super(invPlayer, train);
			this.addSlots(train, 0, 8, 18, 5, 9);
			this.playerInv(invPlayer, 8, 140, 198);
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
	public static class GUITrainCargoTramTrailer extends GuiInfoContainer {

		private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/vehicles/gui_cargo_tram_trailer.png");
		private TrainCargoTramTrailer train;

		public GUITrainCargoTramTrailer(InventoryPlayer invPlayer, TrainCargoTramTrailer train) {
			super(new ContainerTrainCargoTramTrailer(invPlayer, train));
			this.train = train;
			this.xSize = 176;
			this.ySize = 222;
		}

		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			String name = this.train.hasCustomName() ? this.train.getName() : I18n.format(this.train.getName());
			this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 0xffffff);
			this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float interp, int mouseX, int mouseY) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		}
	}
}
