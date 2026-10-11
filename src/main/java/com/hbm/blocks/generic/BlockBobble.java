package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockMetaBase;
import com.hbm.inventory.gui.GUIScreenBobble;
import com.hbm.items.special.ItemPlasticScrap.ScrapType;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import com.hbm.world.gen.nbt.INBTTileEntityTransformable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The commemorative bobblehead: one block, 16 tiers of facing meta, and who the bobblehead is lives in the tile entity.
 * Breaking drops "the current one's" bobblehead, so drops are fully self-managed and vanilla's is disabled.
 */
public class BlockBobble extends BlockMetaBase implements IGUIProvider, INBTBlockTransformable {

	public BlockBobble() {
		super(Material.IRON);
	}

	/** Looks drawn by the tile entity renderer; the body ships no model */
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	// which bobblehead drops is in the tile entity; the block state only carries facing, so drops hook themselves in onBlockHarvested
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {

		TileEntity tile = world.getTileEntity(pos);

		if(tile instanceof TileEntityBobble)
			return new ItemStack(this, 1, ((TileEntityBobble) tile).type.ordinal());

		return ItemStack.EMPTY;
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {

		if(!player.capabilities.isCreativeMode) {
			harvesters.set(player);
			if(!world.isRemote) {
				TileEntity tile = world.getTileEntity(pos);
				if(tile instanceof TileEntityBobble) {
					EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new ItemStack(this, 1, ((TileEntityBobble) tile).type.ordinal()));
					item.motionX = 0;
					item.motionY = 0;
					item.motionZ = 0;
					world.spawnEntity(item);
				}
			}
			harvesters.set(null);
		}
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		player.addStat(StatList.getBlockStats(this), 1);
		player.addExhaustion(0.025F);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

		if(world.isRemote) {
			player.openGui(MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {

		for(int i = 1; i < BobbleType.values().length; i++)
			list.add(new ItemStack(this, 1, i));
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {

		int meta = MathHelper.floor((double)((placer.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
		world.setBlockState(pos, getStateFromMeta(meta), 2);

		TileEntity tile = world.getTileEntity(pos);

		if(tile instanceof TileEntityBobble) {
			TileEntityBobble bobble = (TileEntityBobble) tile;
			bobble.type = BobbleType.values()[Math.abs(stack.getItemDamage()) % BobbleType.values().length];
			bobble.markDirty();
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		float f = 0.0625F;
		return new AxisAlignedBB(5.5F * f, 0.0F, 5.5F * f, 1.0F - 5.5F * f, 0.625F, 1.0F - 5.5F * f);
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		return (meta + coordBaseMode * 4) % 16;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityBobble();
	}

	public static class TileEntityBobble extends TileEntity implements INBTTileEntityTransformable {

		public BobbleType type = BobbleType.NONE;

		@Override
		public NBTTagCompound getUpdateTag() {
			return this.writeToNBT(new NBTTagCompound());
		}

		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			return new SPacketUpdateTileEntity(this.getPos(), 0, this.writeToNBT(new NBTTagCompound()));
		}

		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.getNbtCompound());
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.type = BobbleType.values()[Math.abs(nbt.getByte("type")) % BobbleType.values().length];
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setByte("type", (byte) type.ordinal());
			return nbt;
		}

		/** Structure generation swaps in a random bobblehead; they're all ornaments anyway */
		@Override
		public void transformTE(World world, int coordBaseMode) {
			type = BobbleType.values()[world.rand.nextInt(BobbleType.values().length - 1) + 1];
		}
	}

	/** Each one is a contributor record; the last column is the scrap it drops when broken */
	public static enum BobbleType {

		NONE(			"null",								"null",			null,														null,																								false,	ScrapType.BOARD_BLANK),
		STRENGTH(		"Strength",							"Strength",		null,														"It's essential to give your arguments impact.",													false,	ScrapType.BRIDGE_BIOS),
		PERCEPTION(		"Perception",						"Perception",	null,														"Only through observation will you perceive weakness.",												false,	ScrapType.BRIDGE_NORTH),
		ENDURANCE(		"Endurance",						"Endurance",	null,														"Always be ready to take one for the team.",														false,	ScrapType.BRIDGE_SOUTH),
		CHARISMA(		"Charisma",							"Charisma",		null,														"Nothing says pizzaz like a winning smile.",														false,	ScrapType.BRIDGE_IO),
		INTELLIGENCE(	"Intelligence",						"Intelligence",	null,														"It takes the smartest individuals to realize$there's always more to learn.",						false,	ScrapType.BRIDGE_BUS),
		AGILITY(		"Agility",							"Agility",		null,														"Never be afraid to dodge the sensitive issues.",													false,	ScrapType.BRIDGE_CHIPSET),
		LUCK(			"Luck",								"Luck",			null,														"There's only one way to give 110%.",																false,	ScrapType.BRIDGE_CMOS),
		BOB(			"Robert \"The Bobcat\" Katzinsky",	"HbMinecraft",	"Hbm's Nuclear Tech Mod",									"I know where you live, " + System.getProperty("user.name"),										false,	ScrapType.CPU_SOCKET),
		FRIZZLE(		"Frooz",							"Frooz",		"Weapon models",											"BLOOD IS FUEL",																					true,	ScrapType.CPU_CLOCK),
		PU238(			"Pu-238",							"Pu-238",		"Improved Tom impact mechanics",							null,																								false,	ScrapType.CPU_REGISTER),
		VT(				"VT-6/24",							"VT-6/24",		"Balefire warhead model and general texturework",			"You cannot unfuck a horse.",																		true,	ScrapType.CPU_EXT),
		DOC(			"The Doctor",						"Doctor17PH",	"Russian localization, lunar miner",						"Perhaps the moon rocks were too expensive",														true,	ScrapType.CPU_CACHE),
		BLUEHAT(		"The Blue Hat",						"The Blue Hat",	"Textures",													"payday 2's deagle freeaim champ of the year 2022",													true,	ScrapType.MEM_16K_A),
		PHEO(			"Pheo",								"Pheonix",		"Deuterium machines, tantalium textures, Reliant Rocket",	"RUN TO THE BEDROOM, ON THE SUITCASE ON THE LEFT,$YOU'LL FIND MY FAVORITE AXE",						true,	ScrapType.MEM_16K_B),
		ADAM29(			"Adam29",							"Adam29",		"Ethanol, liquid petroleum gas",							"You know, nukes are really quite beatiful.$It's like watching a star be born for a split second.",	true,	ScrapType.MEM_16K_C),
		UFFR(			"UFFR",								"UFFR",			"All sorts of things from his PR",							"fried shrimp",																						false,	ScrapType.MEM_SOCKET),
		VAER(			"vaer",								"vaer",			"ZIRNOX",													"taken de family out to the weekend cigarette festival",											true,	ScrapType.MEM_16K_D),
		NOS(			"Dr Nostalgia",						"Dr Nostalgia",	"SSG and Vortex models",									"Take a picture, I'ma pose, paparazzi$I've been drinking, moving like a zombie",					true,	ScrapType.BOARD_TRANSISTOR),
		DRILLGON(		"Drillgon200",						"Drillgon200",	"1.12 Port",												null,																								false,	ScrapType.CPU_LOGIC),
		CIRNO(			"Cirno",							"Cirno",		"the only multi layered skin i had",						"No brain. Head empty.",																			true,	ScrapType.BOARD_BLANK),
		GWEN(			"Gwen",								"Gwen",			"Numero Uno Homie",											"HELP ME I'M TRAPPED IN THIS FUCKING POLYRESIN PRISON YOU NEED TO LET ME OUT PLEASE SMASH IT OPEN DO IT NOW CRACK IT",	true,	ScrapType.BOARD_BLANK),
		JUICE(			"Juicy_Lad",						"Juicy_Lad",	"The Mojave Testing for this funny fork",					"\"What should the Inscription be?\",$ \"Uhh.. I'll think about it.\"",								true,	ScrapType.BOARD_BLANK),
		JAMESH_2(		"JamesH_2",							"JamesH_2",		"The fork itself",										"COME ON AND SLAM",																						true,	ScrapType.BOARD_BLANK),
		PEEP(			"Peep",								"LePeeperSauvage",	"Coilgun, Leadburster, Congo Lake models, and the 737",	"Fluffy ears can't hide in ash, nor snow.",														true,	ScrapType.CPU_CLOCK),
		MICROWAVE(		"Microwave",						"Microwave",		"OC Compatibility and massive RBMK/packet optimizations",	"they call me the food heater$john optimization",													true,	ScrapType.BRIDGE_BIOS),
		MELLOW(			"MELLOWARPEGGIATION",				"Mellow",			"Celestial mechanics, rocketry,$atmospheric chemistry, orbital stations,$Dyson swarms",	"Make something cool now, ask for permission later.",												true,	ScrapType.CARD_PROCESSOR),
		MRKIMKIMORA(	"MrKimkimora",						"MrKimkimora",		"Part of NTM:Space models,$textures and russian localization",	"I know your IP. Your IP is 127.0.0.1.",															false,	ScrapType.BOARD_BLANK),
		ABEL(			"Abel1502", 						"Abel1502", 		"Abilities GUI, optimizations and many QoL improvements", 	"NANTO SUBARASHII",																				true,	ScrapType.CPU_REGISTER);

		public String name;			// the tooltip's title
		public String label;		// the name engraved on the base
		public String contribution;	// this person's contribution (if any)
		public String inscription;	// the inscription at the bottom
		public boolean skinLayers;
		public ScrapType scrap;

		private BobbleType(String name, String label, String contribution, String inscription, boolean layers, ScrapType scrap) {
			this.name = name;
			this.label = label;
			this.contribution = contribution;
			this.inscription = inscription;
			this.skinLayers = layers;
			this.scrap = scrap;
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new GUIScreenBobble((TileEntityBobble) world.getTileEntity(pos));
	}
}
