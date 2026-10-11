package com.hbm.commands;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;

/**
 * Reads and writes **total world time** ({@code totalTime}: ticks accumulated since world creation, doesn't wrap when /time is set).
 * <br><br>
 * Vanilla {@code /time} moves {@code worldTime}, which is a different thing: total time is a monotonic counter shared by all dimensions,
 * the spaceflight side (orbital phase, day cycles) runs on it, hence it needs its own setter. This changes **all** dimensions at once,
 * because total time should agree across dimensions anyway; touching only the sender's dimension desyncs the cycles.
 */
public class CommandTotalTime extends CommandBase {

	@Override
	public String getName() {
		return "totaltime";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.time.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

		if(args.length > 1) {

			if(args[0].equals("set")) {
				int time = parseInt(args[1], 0);
				setTotalTime(server, time);
				notifyCommandListener(sender, this, "commands.time.set", Integer.valueOf(time));
				return;
			}

			if(args[0].equals("add")) {
				int time = parseInt(args[1], 0);
				addTotalTime(server, time);
				notifyCommandListener(sender, this, "commands.time.added", Integer.valueOf(time));
				return;
			}
		}

		throw new WrongUsageException("commands.time.usage");
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, "set", "add") : null;
	}

	private static void setTotalTime(MinecraftServer server, int time) {
		for(WorldServer world : server.worlds)
			if(world != null)
				world.getWorldInfo().setWorldTotalTime(time);
	}

	private static void addTotalTime(MinecraftServer server, int time) {
		for(WorldServer world : server.worlds) {

			if(world == null)
				continue;

			WorldInfo info = world.getWorldInfo();
			info.setWorldTotalTime(info.getWorldTotalTime() + time);
		}
	}
}
