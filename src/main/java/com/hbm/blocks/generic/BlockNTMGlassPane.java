package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockNaming;

import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * The two reinforced glass panes (reinforced_glass_pane / reinforced_laminate_pane).
 *
 * Connection, the four-way multipart state, collision and picking all ride the base {@code BlockPane}; what the old class truly owned was:
 *   1) "drops itself when broken" — expressed in the old version via {@code quantityDropped(doesDrop ? 1 : 0)}; this version's {@code BlockPane}
 *      made it a constructor argument (bytecode-verified: {@code getItemDropped} returns {@code Items.AIR} outright when the flag is false),
 *      so it goes back to the base; no local count override;
 *   2) light opacity takes 1 ({@code setLightOpacity(1)}, written twice in the old version — constructor and chain; the constructor copy stays)
 *
 * The old third override was {@code canPaneConnectTo} — "a neighbor of our own glass block counts as connected". This version's signature is
 * {@code canPaneConnectTo(IBlockAccess, BlockPos, EnumFacing)} (javap-verified), the check fetching the neighbor from the world per cell itself.
 * When this family registered, its own glass block (the {@code reinforced_glass} family) hadn't landed ⇒ zero call surface then; it has since become
 * {@link BlockNTMGlassCT}, and this line wires back with this batch.
 *
 * Two more old spots with no counterpart here, logged not ported:
 *   the {@code getRenderBlockPass(0/1)} two-pass rendering — this version's pane alpha rides the JSON model and vanilla's CUTOUT layer,
 *   no pass counts in code; {@code this.opaque = true} — {@code Block} has no such field here, and
 *   {@code BlockPane.isOpaqueCube} always returns false (bytecode-verified); unchangeable, and it shouldn't be.
 * Both keep stone material as before; textures are named by the five sub-models in {@code models/block/<name>_post.json}, and the methods
 */
public class BlockNTMGlassPane extends BlockPane implements IBlockNaming {

	/** @param dropsItself whether breaking drops itself (the old doesDrop; the pass tier belongs to models now, no longer a flag) */
	public BlockNTMGlassPane(Material mat, boolean dropsItself) {
		super(mat, dropsItself);

		this.setLightOpacity(1);
	}

	/** Textures come from JSON; this only catches the old chain; returning this class reaches the later setBlockName */
	public BlockNTMGlassPane setBlockTextureName(String name) {
		return this;
	}

	/** The old "a neighbor of our own glass counts as connected": vanilla only knows panes and glass blocks, our family needed its own check */
	@Override
	public boolean canPaneConnectTo(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return super.canPaneConnectTo(world, pos, side) || world.getBlockState(pos).getBlock() instanceof BlockNTMGlassCT;
	}
}
