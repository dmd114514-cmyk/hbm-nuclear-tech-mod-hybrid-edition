package com.hbm.tileentity.machine.storage;

import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.inventory.container.ContainerCrateTungsten;
import com.hbm.inventory.gui.GUICrateTungsten;
import com.hbm.items.ModItems;
import com.hbm.tileentity.IDysonConverter;

import api.hbm.block.ILaserable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The tungsten crate: feeds it a laser and it catches fire, running its contents through the furnace recipes once,
 * while flipping itself into its "hot" state (visible only on the GUI texture; the world looks the same either way).
 */
public class TileEntityCrateTungsten extends TileEntityCrateBase implements ILaserable, IDysonConverter {

	private int heatTimer;

	public TileEntityCrateTungsten() {
		super(27);
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "container.crateTungsten";
	}

	@Override
	public void updateEntity() {

		if(!world.isRemote) {

			if(heatTimer > 0) heatTimer--;

			boolean hot = this.isHot();

			if(!hot && heatTimer > 0) {
				world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockStorageCrate.HEAT, true), 3);
			}

			if(hot && heatTimer <= 0) {
				world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockStorageCrate.HEAT, false), 3);
			}
		}

		if(this.isHot()) {
			spawnHeatParticles();
		}
	}

	private boolean isHot() {
		return world.getBlockState(pos).getValue(BlockStorageCrate.HEAT);
	}

	private void spawnHeatParticles() {

		spawnParticleAt(pos.getX(), pos.getY() + 1.1, pos.getZ());
		spawnParticleAt(pos.getX(), pos.getY() + 1.1, pos.getZ());
		spawnParticleAt(pos.getX() - 0.1, pos.getY(), pos.getZ());
		spawnParticleAt(pos.getX() - 0.1, pos.getY(), pos.getZ());
		spawnParticleAt(pos.getX() + 1.1, pos.getY(), pos.getZ());
		spawnParticleAt(pos.getX() + 1.1, pos.getY(), pos.getZ());
		spawnParticleAt(pos.getX(), pos.getY(), pos.getZ() - 0.1);
		spawnParticleAt(pos.getX(), pos.getY(), pos.getZ() - 0.1);
		spawnParticleAt(pos.getX(), pos.getY(), pos.getZ() + 1.1);
		spawnParticleAt(pos.getX(), pos.getY(), pos.getZ() + 1.1);
	}

	private void spawnParticleAt(double x, double y, double z) {
		world.spawnParticle(EnumParticleTypes.FLAME, x + world.rand.nextDouble(), y + world.rand.nextDouble(), z + world.rand.nextDouble(), 0.0, 0.0, 0.0);
		world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + world.rand.nextDouble(), y + world.rand.nextDouble(), z + world.rand.nextDouble(), 0.0, 0.0, 0.0);
	}

	/** These are not allowed to be extracted by automation: plutonium pellets, spent crucibles, anything that could smelt once more */
	@Override
	public boolean canExtractSlot(int slot, ItemStack stack) {

		if(stack == null || stack.isEmpty()) return true;

		if(stack.getItem() == ModItems.billet_polonium) return false;
		if(stack.getItem() == ModItems.crucible && stack.getMetadata() > 0) return false;

		return FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty();
	}

	@Override
	public void addEnergy(World world, int x, int y, int z, long energy, EnumFacing dir) {

		heatTimer = 5;

		for(int i = 0; i < slots.length; i++) {

			if(slots[i] == null || slots[i].isEmpty()) continue;

			ItemStack result = FurnaceRecipes.instance().getSmeltingResult(slots[i]);

			if(slots[i].getItem() == ModItems.billet_polonium && energy > 10000000)
				result = new ItemStack(ModItems.billet_yharonite);

			if(slots[i].getItem() == ModItems.crucible && slots[i].getMetadata() > 0 && energy > 10000000)
				result = new ItemStack(ModItems.crucible, 1, 0);

			int size = slots[i].getCount();

			if(!result.isEmpty() && result.getCount() * size <= result.getMaxStackSize()) {
				slots[i] = result.copy();
				slots[i].setCount(slots[i].getCount() * size);
			}
		}

		this.markDirty();
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new ContainerCrateTungsten(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new GUICrateTungsten(player.inventory, this);
	}

	//the dyson receiver side
	@Override
	public boolean provideEnergy(BlockPos pos, long energy) {
		addEnergy(world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), energy / 5000, EnumFacing.NORTH);
		return true;
	}

	@Override
	public long maximumEnergy() {
		return Long.MAX_VALUE;
	}
}
