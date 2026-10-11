package api.hbm.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Blocks a hand tool can wrench/pry/take apart; the signature follows this version's block interaction convention, BlockPos + EnumFacing */
public interface IToolable {

	public boolean onScrew(World world, EntityPlayer player, BlockPos pos, EnumFacing side, float fX, float fY, float fZ, ToolType tool);

	public static enum ToolType {
		SCREWDRIVER,
		HAND_DRILL,
		DEFUSER,
		WRENCH,
		TORCH,
		BOLT;

		public List<ItemStack> stacksForDisplay = new ArrayList<ItemStack>();
		private static HashMap<StackKey, ToolType> map = new HashMap<StackKey, ToolType>();

		public void register(ItemStack stack) {
			stacksForDisplay.add(stack);
		}

		public static ToolType getType(ItemStack stack) {

			if(!map.isEmpty()) {
				return map.get(new StackKey(stack));
			}

			//The first time it is asked, the display stacks get flattened into a lookup; after that it is a single hash
			for(ToolType type : ToolType.values()) {
				for(ItemStack tool : type.stacksForDisplay) {
					map.put(new StackKey(tool), type);
				}
			}

			return map.get(new StackKey(stack));
		}
	}

	/** Item identity key: id + meta + NBT, private to this enum so the api layer does not depend back on the content layer's comparable stack */
	final class StackKey {

		private final int itemId;
		private final int meta;
		private final NBTTagCompound nbt;

		StackKey(ItemStack stack) {
			if(stack == null || stack.isEmpty()) {
				this.itemId = 0;
				this.meta = 0;
				this.nbt = null;
			} else {
				this.itemId = net.minecraft.item.Item.getIdFromItem(stack.getItem());
				this.meta = stack.getMetadata();
				this.nbt = stack.getTagCompound();
			}
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof StackKey)) return false;
			StackKey other = (StackKey) obj;
			if(this.itemId != other.itemId || this.meta != other.meta) return false;
			if(this.nbt == null) return other.nbt == null;
			return this.nbt.equals(other.nbt);
		}

		@Override
		public int hashCode() {
			int result = itemId;
			result = result * 27644437 + meta;
			return result * 27644437 + (nbt == null ? 0 : nbt.hashCode());
		}
	}
}
