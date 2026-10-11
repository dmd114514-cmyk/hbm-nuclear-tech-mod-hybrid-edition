package com.hbm.main;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.config.ClientConfig;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The crosshair overlay dispatcher: any block or item implementing {@link ILookOverlay} gets its readout drawn from here.
 * <br><br>
 * Order as before: the held item wins (look at a machine holding a scanner and you see the scanner's copy), then the block under the crosshair,
 * finally entities. The whole thing is client-only, registered via {@code ClientProxy.init}; the server never loads this class.
 * <br><br>
 * Triggered on "crosshair phase + switch on", as before; the switch defaults on, so the readouts are on screen by default.
 */
@SideOnly(Side.CLIENT)
public class LookOverlayHandler {

	@SubscribeEvent
	public static void onOverlayRender(RenderGameOverlayEvent.Pre event) {

		if(event.getType() != ElementType.CROSSHAIRS || !ClientConfig.doddRbmkDiagnostic)
			return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;
		World world = mc.world;
		RayTraceResult mop = mc.objectMouseOver;

		if(player == null || world == null || mop == null)
			return;

		ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);

		if(mop.typeOfHit == RayTraceResult.Type.BLOCK) {

			BlockPos pos = mop.getBlockPos();

			if(!held.isEmpty() && held.getItem() instanceof ILookOverlay) {
				((ILookOverlay) held.getItem()).printHook(event, world, pos);
			} else {
				Block b = world.getBlockState(pos).getBlock();
				if(b instanceof ILookOverlay)
					((ILookOverlay) b).printHook(event, world, pos);
			}

			if(ClientConfig.showBlockMetaOverlay) {
				List<String> text = new ArrayList<String>();
				text.add(world.getBlockState(pos).getBlock().getUnlocalizedName());
				text.add("Meta: " + world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)));
				ILookOverlay.printGeneric(event, "DEBUG", 0xffff00, 0x4040000, text);
			}

		} else if(mop.typeOfHit == RayTraceResult.Type.ENTITY && mop.entityHit instanceof ILookOverlay) {
			/* The old version passed 0,0,0; now that the interface takes coordinates, give it the entity's cell — that's the only meaningful one to read */
			((ILookOverlay) mop.entityHit).printHook(event, world, mop.entityHit.getPosition());
		}

		GlStateManager.color(1F, 1F, 1F, 1F);
	}
}
