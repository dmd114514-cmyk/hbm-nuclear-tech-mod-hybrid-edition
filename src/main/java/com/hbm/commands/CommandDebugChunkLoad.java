package com.hbm.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;

/**
 * Debug tool: reads an unloaded chunk straight off disk and lists the tile entity positions it holds,
 * for hunting the "chunk carrying an out-of-bounds TE" class of save corruption (the offending entry gets renamed {@code INVALID_POS_*},
 * effective only on the in-memory NBT copy, nothing written back to disk).
 * <br><br>
 * The coordinate arguments are **block coords**, folded into chunk coords internally; only meaningful on a server provider,
 * so the three-layer checks (is it a ChunkProviderServer / is it an AnvilChunkLoader / does the Tag list read)
 * say so out loud at each step — no silently doing nothing.
 */
public class CommandDebugChunkLoad extends CommandBase {

	@Override
	public String getName() {
		return "ntmloadchunk";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/ntmloadchunk <x> <z>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

		if(args.length != 2) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + getUsage(sender)));
			return;
		}

		int x = parseInt(args[0]);
		int z = parseInt(args[1]);

		IChunkProvider prov = sender.getEntityWorld().getChunkProvider();

		if(!(prov instanceof ChunkProviderServer)) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Not ChunkProviderServer"));
			return;
		}

		ChunkProviderServer serv = (ChunkProviderServer) prov;
		IChunkLoader loader = serv.chunkLoader;

		if(!(loader instanceof AnvilChunkLoader)) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Not AnvilChunkLoader"));
			return;
		}

		AnvilChunkLoader anvil = (AnvilChunkLoader) loader;

		try {
			int cX = x >> 4;
			int cZ = z >> 4;

			if(serv.chunkExists(cX, cZ)) {
				Chunk chunk = sender.getEntityWorld().getChunkFromChunkCoords(cX, cZ);
				if(chunk.isLoaded()) {
					sender.sendMessage(new TextComponentString(TextFormatting.RED + "Chunk currently loaded."));
					return;
				}
			}

			Object[] data = anvil.loadChunk__Async(sender.getEntityWorld(), cX, cZ);

			// The command asks about the unloaded chunk by definition; not being on disk is the common case, not an error
			if(data == null || data[1] == null) {
				sender.sendMessage(new TextComponentString(TextFormatting.RED + "Chunk not saved on disk: " + cX + " " + cZ));
				return;
			}

			NBTTagCompound nbt = (NBTTagCompound) data[1];
			NBTTagList tagList = nbt.getCompoundTag("Level").getTagList("TileEntities", 10);

			if(tagList.tagCount() <= 0)
				sender.sendMessage(new TextComponentString(TextFormatting.RED + "Tag list empty"));

			for(int i1 = 0; i1 < tagList.tagCount(); ++i1) {

				NBTTagCompound tileCompound = tagList.getCompoundTagAt(i1);
				int tX = tileCompound.getInteger("x");
				int tY = tileCompound.getInteger("y");
				int tZ = tileCompound.getInteger("z");
				String name = tileCompound.getString("id");

				int i = tX - cX * 16;
				int j = tY;
				int k = tZ - cZ * 16;

				boolean outOfBounds = i < 0 || i > 15 || j < 0 || j > 255 || k < 0 || k > 15;

				sender.sendMessage(new TextComponentString((outOfBounds ? TextFormatting.RED : TextFormatting.GREEN) + name + " " + i + " " + j + " " + k));

				if(outOfBounds)
					tileCompound.setString("id", "INVALID_POS_" + name);
			}

		} catch(Exception e) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "" + e.getLocalizedMessage()));
		}
	}
}
