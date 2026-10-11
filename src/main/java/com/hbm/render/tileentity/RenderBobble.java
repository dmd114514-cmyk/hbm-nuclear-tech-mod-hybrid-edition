package com.hbm.render.tileentity;

import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.blocks.generic.BlockBobble.TileEntityBobble;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustomNamed;
import com.hbm.render.util.RenderDecoItem;
import com.hbm.util.LoggingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

/**
 * How the bobblehead draws: one contributor standing on a one-cell base, who it is recorded in the tile entity.
 *
 * This one is a gate case: block and tile are both registered (bobblehead), and the block itself says "body draws no model"
 * (getRenderType returns INVISIBLE), yet no renderer was bound ⇒ placing it gave a cell of air.
 *
 * Matches the old version, but two things changed shape:
 *   · models no longer read disk at class init. The old AdvancedModelLoader static fields became
 *     ResourceManager.getModel (lazy + cached), handing back the tree's own IModelCustomNamed;
 *     part names (Socket / LL17 / Head / Horn / PeepTail / PelletShine / Fumo / Drillgon /
 *     MrKimkimora / Fluoro / Glow) were checked line by line against the groups of the miniNuke, sacred_dragon, n_i_4_n_i, hev and hat OBJs,
 *     and all 26 textures were verified present one by one.
 *   · the small held items no longer hand-write their flat drawing. The old ItemRenderer.renderItemIn2D with atlas UVs becomes the tree's own
 *     RenderDecoItem.drawFlat, sized as "1 unit square", the caller's original scaling kept; if the size looks wrong in-game,
 *     change just the one number in drawHeld — never the poses of the individual branches.
 *
 * The two things not ported are not omissions: renderOrigin was the old debug vertex cross; and the VAER branch's only draw statement
 * was commented out in the old version already — it never drew anything to begin with.
 */
@SideOnly(Side.CLIENT)
public class RenderBobble extends TileEntitySpecialRenderer<TileEntityBobble> {

	/** The figure body: base and person geometry both live in this one OBJ; switching figures only swaps textures */
	private static final String MODEL_BOBBLE = "models/trinkets/bobble.obj";

	private static final String MODEL_FATMAN = "models/weapons/fatman.obj";
	private static final String MODEL_SACRED_DRAGON = "models/weapons/sacred_dragon.obj";
	private static final String MODEL_N_I_4_N_I = "models/weapons/n_i_4_n_i.obj";
	private static final String MODEL_SHIMMER_AXE = "models/shimmer_axe.obj";

	private static final ResourceLocation TRINKET = trinketTex("socket.png");
	private static final ResourceLocation GLOW = trinketTex("glow.png");
	private static final ResourceLocation LAMP = new ResourceLocation(RefStrings.MODID, "textures/blocks/fluorescent_lamp.png");

	/** The figure with no dedicated texture (NONE) wears this one, same source as the old ResourceManager.universal */
	private static final ResourceLocation FALLBACK = new ResourceLocation(RefStrings.MODID, "textures/models/TheGadget3_.png");

	private static final ResourceLocation TEX_VAULTBOY = trinketTex("vaultboy.png");
	private static final ResourceLocation TEX_HBM = trinketTex("hbm.png");
	private static final ResourceLocation TEX_PU238 = trinketTex("pellet.png");
	private static final ResourceLocation TEX_FRIZZLE = trinketTex("frizzle.png");
	private static final ResourceLocation TEX_VT = trinketTex("vt.png");
	private static final ResourceLocation TEX_DOC = trinketTex("doctor17ph.png");
	private static final ResourceLocation TEX_BLUEHAT = trinketTex("thebluehat.png");
	private static final ResourceLocation TEX_PHEO = trinketTex("pheo.png");
	private static final ResourceLocation TEX_ADAM29 = trinketTex("adam29.png");
	private static final ResourceLocation TEX_UFFR = trinketTex("uffr.png");
	private static final ResourceLocation TEX_VAER = trinketTex("vaer.png");
	private static final ResourceLocation TEX_NOS = trinketTex("nos.png");
	private static final ResourceLocation TEX_DRILLGON = trinketTex("drillgon200.png");
	private static final ResourceLocation TEX_CIRNO = trinketTex("cirno.png");
	private static final ResourceLocation TEX_GWEN = trinketTex("gwen.png");
	private static final ResourceLocation TEX_JUICE = trinketTex("Juicy_Lad.png");
	private static final ResourceLocation TEX_JAMESH_2 = trinketTex("JamesH_2.png");
	private static final ResourceLocation TEX_MICROWAVE = trinketTex("microwave.png");
	private static final ResourceLocation TEX_PEEP = trinketTex("peep.png");
	private static final ResourceLocation TEX_MELLOW = trinketTex("mellowrpg8.png");
	private static final ResourceLocation TEX_MELLOW_GLOW = trinketTex("mellowrpg8_glow.png");
	private static final ResourceLocation TEX_MRKIMKIMORA = trinketTex("MrKimkimora.png");
	private static final ResourceLocation TEX_ABEL = trinketTex("abel.png");
	private static final ResourceLocation TEX_ABEL_GLOW = trinketTex("abel_glow.png");

	private static final ResourceLocation TEX_N_I_4_N_I = new ResourceLocation(RefStrings.MODID, "textures/models/weapons/n_i_4_n_i.png");
	private static final ResourceLocation TEX_FATMAN_MININUKE = new ResourceLocation(RefStrings.MODID, "textures/models/weapons/fatman_mininuke.png");
	private static final ResourceLocation TEX_SACRED_DRAGON = new ResourceLocation(RefStrings.MODID, "textures/models/weapons/double_barrel_sacred_dragon.png");
	private static final ResourceLocation TEX_SHIMMER_AXE = new ResourceLocation(RefStrings.MODID, "textures/models/shimmer_axe.png");

	/** The old version took GunFactory.EnumModSpecial.DOUBLOONS's ordinal; that table hasn't come over, so it's written as a constant in the old table's order */
	private static final int META_DOUBLOONS = 17;

	/** This frame's timestamp, which the head wobble needs; refreshed at the top of every render */
	private long time;

	/** The OBJ to draw this time, fetched once in render, shared by every branch */
	private IModelCustomNamed bobble;

	private boolean announced = false;

	private static ResourceLocation trinketTex(String name) {
		return new ResourceLocation(RefStrings.MODID, "textures/models/trinkets/" + name);
	}

	@Override
	public void render(TileEntityBobble te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		this.time = System.currentTimeMillis();
		this.bobble = ResourceManager.getModel(MODEL_BOBBLE);

		// Facing is still the block meta (16 tiers), turning 22.5° per step as the old version did
		int meta = te.getBlockMetadata();

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(x + 0.5D, y, z + 0.5D);
			GlStateManager.scale(0.25D, 0.25D, 0.25D);
			GlStateManager.rotate(22.5F * meta + 90F, 0F, -1F, 0F);

			renderBobble(te.type);

			if(!this.announced) {
				this.announced = true;
				LoggingUtil.info("[hbm] bobblehead first frame drawn: {} facing meta {}", te.type.label, meta);
			}
		}
		GlStateManager.popMatrix();
	}

	/**
	 * The item-form drawing (the old version hung this in {@code ItemRenderLibrary}: the stack's durability says which figure).
	 * Before {@link #renderBobble} runs, model and time must be prepared — those two strokes were only done by the TESR's {@link #render} before,
	 * and this item entry has neither block coords nor meta, so it doesn't borrow that entry; it just adds the two strokes here.
	 */
	public void renderAsItem(BobbleType type) {

		this.time = System.currentTimeMillis();
		this.bobble = ResourceManager.getModel(MODEL_BOBBLE);

		renderBobble(type);
	}

	public void renderBobble(BobbleType type) {

		GlStateManager.enableLighting();
		GlStateManager.enableRescaleNormal();

		this.bindTexture(TRINKET);
		this.bobble.renderPart("Socket");

		this.bindTexture(textureFor(type));

		switch(type) {
		case PU238:			renderPellet(); break;
		case UFFR:			renderFumo(); break;
		case DRILLGON:		this.bobble.renderPart("Drillgon"); break;
		case MRKIMKIMORA:	this.bobble.renderPart("MrKimkimora"); break;
		default:			renderGuy(type);
		}

		GlStateManager.pushMatrix();
		renderPost(type);
		GlStateManager.popMatrix();

		renderSocket(type);
	}

	/** Which texture each figure wears; the seven-attribute family shares vaultboy, and unrecognized ones take FALLBACK */
	private ResourceLocation textureFor(BobbleType type) {

		switch(type) {
		case STRENGTH:
		case PERCEPTION:
		case ENDURANCE:
		case CHARISMA:
		case INTELLIGENCE:
		case AGILITY:
		case LUCK:			return TEX_VAULTBOY;

		case BOB:			return TEX_HBM;
		case PU238:			return TEX_PU238;
		case FRIZZLE:		return TEX_FRIZZLE;
		case VT:			return TEX_VT;
		case DOC:			return TEX_DOC;
		case BLUEHAT:		return TEX_BLUEHAT;
		case PHEO:			return TEX_PHEO;
		case CIRNO:			return TEX_CIRNO;
		case ADAM29:		return TEX_ADAM29;
		case UFFR:			return TEX_UFFR;
		case VAER:			return TEX_VAER;
		case NOS:			return TEX_NOS;
		case DRILLGON:		return TEX_DRILLGON;
		case GWEN:			return TEX_GWEN;
		case JUICE:			return TEX_JUICE;
		case JAMESH_2:		return TEX_JAMESH_2;
		case MICROWAVE:		return TEX_MICROWAVE;
		case PEEP:			return TEX_PEEP;
		case MELLOW:		return TEX_MELLOW;
		case MRKIMKIMORA:	return TEX_MRKIMKIMORA;
		case ABEL:			return TEX_ABEL;
		default:			return FALLBACK;
		}
	}

	/*
	 * The standard figure: each limb and the head is one part, posed entirely by rotating three angles about part roots
	 */

	private double[] rotLeftArm = { 0, 0, 0 };
	private double[] rotRightArm = { 0, 0, 0 };
	private double[] rotLeftLeg = { 0, 0, 0 };
	private double[] rotRightLeg = { 0, 0, 0 };
	private double rotBody = 0;
	private double[] rotHead = { 0, 0, 0 };

	private void resetFigurineRotation() {

		this.rotLeftArm = new double[] { 0, 0, 0 };
		this.rotRightArm = new double[] { 0, 0, 0 };
		this.rotLeftLeg = new double[] { 0, 0, 0 };
		this.rotRightLeg = new double[] { 0, 0, 0 };
		this.rotBody = 0;
		this.rotHead = new double[] { 0, 0, 0 };
	}

	@SuppressWarnings("incomplete-switch")
	private void setupFigurineRotation(BobbleType type) {

		switch(type) {
		case STRENGTH:
			this.rotLeftArm = new double[] { 0, 25, 135 };
			this.rotRightArm = new double[] { 0, -45, 135 };
			this.rotLeftLeg = new double[] { 0, 0, -5 };
			this.rotRightLeg = new double[] { 0, 0, 5 };
			this.rotHead = new double[] { 15, 0, 0 };
			break;
		case PERCEPTION:
			this.rotLeftArm = new double[] { 0, -15, 135 };
			this.rotRightArm = new double[] { -5, 0, 0 };
			break;
		case ENDURANCE:
			this.rotBody = 45;
			this.rotLeftArm = new double[] { 0, -25, 30 };
			this.rotRightArm = new double[] { 0, 45, 30 };
			this.rotHead = new double[] { 0, -45, 0 };
			break;
		case CHARISMA:
			this.rotBody = 45;
			this.rotRightArm = new double[] { 0, -45, 90 };
			this.rotLeftLeg = new double[] { 0, 0, -5 };
			this.rotRightLeg = new double[] { 0, 0, 5 };
			this.rotHead = new double[] { -5, -45, 0 };
			break;
		case INTELLIGENCE:
			this.rotHead = new double[] { 0, 30, 0 };
			this.rotLeftArm = new double[] { 5, 0, 0 };
			this.rotRightArm = new double[] { 15, 0, 170 };
			break;
		case AGILITY:
			this.rotLeftArm = new double[] { 0, 0, 60 };
			this.rotRightArm = new double[] { 0, 0, -45 };
			this.rotLeftLeg = new double[] { 0, 0, -15 };
			this.rotRightLeg = new double[] { 0, 0, 45 };
			break;
		case LUCK:
			this.rotLeftArm = new double[] { 135, 45, 0 };
			this.rotRightArm = new double[] { -135, -45, 0 };
			this.rotRightLeg = new double[] { -5, 0, 0 };
			break;
		case VT:
			this.rotLeftArm = new double[] { 0, -45, 60 };
			this.rotRightArm = new double[] { 0, 0, 45 };
			this.rotLeftLeg = new double[] { 2, 0, 0 };
			this.rotRightLeg = new double[] { -2, 0, 0 };
			break;
		case BLUEHAT:
			this.rotLeftArm = new double[] { 0, 90, 60 };
			break;
		case FRIZZLE:
			this.rotLeftArm = new double[] { 0, 15, 45 };
			this.rotRightArm = new double[] { 0, 0, 80 };
			this.rotLeftLeg = new double[] { 0, 0, 2 };
			this.rotRightLeg = new double[] { 0, 0, -2 };
			break;
		case ADAM29:
			this.rotRightArm = new double[] { 0, 0, 60 };
			break;
		case PHEO:
			this.rotLeftArm = new double[] { 0, 0, 80 };
			this.rotRightArm = new double[] { 0, 0, 45 };
			break;
		case VAER:
			this.rotLeftArm = new double[] { 0, -5, 45 };
			this.rotRightArm = new double[] { 0, 15, 45 };
			break;
		case PEEP:
			this.rotLeftArm = new double[] { 0, 0, 1 };
			this.rotRightArm = new double[] { 0, 0, 1 };
			break;
		case MELLOW:
			this.rotLeftArm = new double[] { 0, 10, 0 };
			this.rotRightArm = new double[] { 0, -10, 0 };
			this.rotLeftLeg = new double[] { 3, 5, 2 };
			this.rotRightLeg = new double[] { -3, -5, 0 };
			break;
		case ABEL:
			this.rotLeftArm = new double[] { 0, 80, 90 };
			this.rotRightArm = new double[] { 0, -80, 90 };
			break;
		}
	}

	private void renderGuy(BobbleType type) {

		resetFigurineRotation();
		setupFigurineRotation(type);

		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) this.rotBody, 0F, 1F, 0F);

		if(type == BobbleType.PEEP) this.bobble.renderPart("PeepTail");

		GlStateManager.disableCull();

		// The two-layer-skin figures lack that batch of 17-numbered parts (the old version drew only one layer of the same-named parts)
		String suffix = type.skinLayers ? "" : "17";

		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		// Left leg
		GlStateManager.pushMatrix();
		GlStateManager.translate(0D, 1D, -0.125D);
		rotateAround(this.rotLeftLeg);
		GlStateManager.translate(0D, -1D, 0.125D);
		this.bobble.renderPart("LL" + suffix);
		GlStateManager.popMatrix();

		// Right leg
		GlStateManager.pushMatrix();
		GlStateManager.translate(0D, 1D, 0.125D);
		rotateAround(this.rotRightLeg);
		GlStateManager.translate(0D, -1D, -0.125D);
		this.bobble.renderPart("RL" + suffix);
		GlStateManager.popMatrix();

		// Left arm
		GlStateManager.pushMatrix();
		GlStateManager.translate(0D, 1.625D, -0.25D);
		rotateAround(this.rotLeftArm);
		GlStateManager.translate(0D, -1.625D, 0.25D);
		this.bobble.renderPart("LA" + suffix);
		GlStateManager.popMatrix();

		// Right arm
		GlStateManager.pushMatrix();
		GlStateManager.translate(0D, 1.625D, 0.25D);
		rotateAround(this.rotRightArm);
		GlStateManager.translate(0D, -1.625D, -0.25D);
		this.bobble.renderPart("RA" + suffix);
		GlStateManager.popMatrix();

		this.bobble.renderPart("Body" + suffix);

		// Head: always wobbling, and the wobble's source is this frame's timestamp
		GlStateManager.pushMatrix();
		GlStateManager.translate(0D, 1.75D, 0D);
		GlStateManager.rotate((float) (Math.sin(this.time * 0.005D) * 1D), 1F, 0F, 0F);
		GlStateManager.rotate((float) (Math.sin(this.time * 0.005D + Math.PI * 0.5D) * 1D), 0F, 0F, 1F);

		GlStateManager.rotate((float) this.rotHead[0], 1F, 0F, 0F);
		GlStateManager.rotate((float) this.rotHead[1], 0F, 1F, 0F);
		GlStateManager.rotate((float) this.rotHead[2], 0F, 0F, 1F);

		GlStateManager.translate(0D, -1.75D, 0D);
		this.bobble.renderPart("Head" + suffix);

		if(type == BobbleType.VT) this.bobble.renderPart("Horn");
		if(type == BobbleType.PEEP) this.bobble.renderPart("PeepHat");

		World world = this.getWorld();

		if(type == BobbleType.VAER) {
			GlStateManager.translate(0.25D, 1.9D, 0.075D);
			GlStateManager.rotate(-60F, 0F, 0F, 1F);
			GlStateManager.scale(0.5D, 0.5D, 0.5D);
			drawHeld(ModItems.cigarette, 0, world, 1F);
		}

		if(type == BobbleType.NOS) {
			GlStateManager.translate(0D, 1.75D, 0D);
			GlStateManager.rotate(180F, 1F, 0F, 0F);
			GlStateManager.scale(0.095D, 0.095D, 0.095D);
			this.bindTexture(ResourceManager.hat);
			ResourceManager.getModel(ResourceManager.armor_hat).renderAll();
		}

		GlStateManager.popMatrix();

		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableBlend();

		GlStateManager.enableCull();

		GlStateManager.popMatrix();
	}

	/** Rotates three angles about a part root: the caller moves to the root first and back after */
	private static void rotateAround(double[] angles) {

		GlStateManager.rotate((float) angles[0], 1F, 0F, 0F);
		GlStateManager.rotate((float) angles[1], 0F, 1F, 0F);
		GlStateManager.rotate((float) angles[2], 0F, 0F, 1F);
	}

	private void renderPellet() {

		GlStateManager.enableCull();

		GlStateManager.pushMatrix();
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
		GlStateManager.disableLighting();
		this.bobble.renderPart("Pellet");

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);

		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color(1.0F, 1.0F, 0.0F, 0.1F + (float) Math.sin(this.time * 0.001D) * 0.05F);
		this.bobble.renderPart("PelletShine");
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();

		GlStateManager.enableLighting();
		GL11.glPopAttrib();
		GlStateManager.popMatrix();
	}

	/** The smoker's cloud: the body doesn't wobble, the cloud above the head does */
	private void renderFumo() {

		GlStateManager.enableCull();
		this.bobble.renderPart("Fumo");

		GlStateManager.pushMatrix();
		GlStateManager.translate(0D, 0.75D, 0D);
		GlStateManager.rotate((float) (Math.sin(this.time * 0.005D) * 1D), 1F, 0F, 0F);
		GlStateManager.rotate((float) (Math.sin(this.time * 0.005D + Math.PI * 0.5D) * 1D), 0F, 0F, 1F);
		GlStateManager.translate(0D, -0.75D, 0D);

		GlStateManager.disableCull();
		this.bobble.renderPart("FumoHead");

		GlStateManager.popMatrix();
	}

	/*
	 * Whatever extra item each figure holds
	 */
	@SuppressWarnings("incomplete-switch")
	private void renderPost(BobbleType type) {

		World world = this.getWorld();

		switch(type) {
		case BLUEHAT:
			GlStateManager.translate(0D, 0.875D, -0.5D);
			GlStateManager.rotate(-90F, 0F, 1F, 0F);
			GlStateManager.rotate(-160F, 0F, 0F, 1F);
			GlStateManager.scale(0.0625D, 0.0625D, 0.0625D);
			this.bindTexture(ResourceManager.hev_helmet);
			ResourceManager.getModel(ResourceManager.armor_hev).renderPart("Head");
			break;
		case FRIZZLE:
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.8D, 1.6D, 0.4D);
			GlStateManager.scale(0.125D, 0.125D, 0.125D);
			GlStateManager.rotate(90F, 0F, 1F, 0F);
			GlStateManager.rotate(10F, 1F, 0F, 0F);
			this.bindTexture(TEX_N_I_4_N_I);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			IModelCustomNamed revolver = ResourceManager.getModel(MODEL_N_I_4_N_I);
			revolver.renderPart("FrameDark");
			revolver.renderPart("Grip");
			revolver.renderPart("FrameLight");
			revolver.renderPart("Cylinder");
			revolver.renderPart("Barrel");
			GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.popMatrix();

			GlStateManager.translate(0.3D, 1.4D, -0.2D);
			GlStateManager.rotate(-100F, 1F, 0F, 0F);
			GlStateManager.scale(0.5D, 0.5D, 0.5D);
			drawHeld(ModItems.weapon_mod_special, META_DOUBLOONS, world, 1F);
			break;
		case ADAM29:
			GlStateManager.translate(0.4D, 1.15D, 0.4D);
			GlStateManager.scale(0.5D, 0.5D, 0.5D);
			drawHeld(ModItems.can_redbomb, 0, world, 1F);
			break;
		case PHEO:
			GlStateManager.translate(0.5D, 1.15D, 0.45D);
			GlStateManager.rotate(-60F, 1F, 0F, 0F);
			GlStateManager.scale(2D, 2D, 2D);
			this.bindTexture(TEX_SHIMMER_AXE);
			ResourceManager.getModel(MODEL_SHIMMER_AXE).renderAll();
			break;
		case BOB:
			GlStateManager.shadeModel(GL11.GL_SMOOTH);

			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0.6875F, 0.625F);
			GlStateManager.rotate(-90F, 1F, 0F, 0F);
			GlStateManager.scale(0.125D, 0.125D, 0.125D);
			this.bindTexture(TEX_FATMAN_MININUKE);
			IModelCustomNamed fatman = ResourceManager.getModel(MODEL_FATMAN);
			GlStateManager.translate(-6F, 0F, 0F);
			for(int i = -1; i <= 1; i++) {
				GlStateManager.translate(3F, 0F, 0F);
				fatman.renderPart("MiniNuke");
			}
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.25F, 0.3125F, -0.5F);
			GlStateManager.rotate(-90F, 1F, 0F, 0F);
			GlStateManager.rotate(90F, 0F, 1F, 0F);
			GlStateManager.scale(0.1D, 0.1D, 0.1D);
			this.bindTexture(TEX_SACRED_DRAGON);
			IModelCustomNamed shotgun = ResourceManager.getModel(MODEL_SACRED_DRAGON);
			shotgun.renderPart("Stock");
			shotgun.renderPart("BarrelShort");
			shotgun.renderPart("Buckle");
			shotgun.renderPart("Lever");
			GlStateManager.popMatrix();

			GlStateManager.shadeModel(GL11.GL_FLAT);
			break;
		case MELLOW:
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
			this.bindTexture(TEX_MELLOW_GLOW);
			renderGuy(type);
			GlStateManager.enableBlend();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			this.bindTexture(LAMP);
			this.bobble.renderPart("Fluoro");
			this.bindTexture(GLOW);
			this.bobble.renderPart("Glow");
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
			GlStateManager.disableBlend();
			GL11.glPopAttrib();
			break;
		case ABEL:
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
			this.bindTexture(TEX_ABEL_GLOW);
			renderGuy(type);
			GL11.glPopAttrib();
			break;
		}
	}

	/**
	 * The item in hand. cigarette and weapon_mod_special have fields declared but aren't registered yet,
	 * so those two draw person only, no object; once the item line registers them, this draws them with no changes.
	 */
	private static void drawHeld(Item item, int meta, World world, float sizeInBlocks) {

		if(item == null) return;

		RenderDecoItem.drawFlat(new ItemStack(item, 1, meta), world, sizeInBlocks);
	}

	/*
	 * The engraved name on the base's front edge
	 */
	private void renderSocket(BobbleType type) {

		GlStateManager.disableLighting();

		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		float unit = 0.01F;

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.63D, 0.175D, 0D);
		GlStateManager.scale(unit, -unit, unit);
		GlStateManager.translate(0D, 0D, font.getStringWidth(type.label) * 0.5D);
		GlStateManager.rotate(90F, 0F, 1F, 0F);
		GlStateManager.depthMask(false);
		GlStateManager.translate(0F, 1F, 0F);
		font.drawString(type.label, 0, 0, type == BobbleType.VT ? 0xff0000 : 0xffffff);
		GlStateManager.depthMask(true);
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
}
