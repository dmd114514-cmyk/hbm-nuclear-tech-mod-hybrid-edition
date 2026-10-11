package com.hbm.commands;

import com.hbm.handler.radiation.ChunkRadiationManager;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

/**
 * Conjures a **field of radiation** on the spot or **wipes a dimension's entire field**, for live-testing hazards and hazmat gear.
 * <br><br>
 * The radiation field is per-chunk bookkeeping, runtime state; without a command you'd wait for natural events (reactor leaks, mine detonations),
 * and one experiment takes forever to stage; {@code set} injects a value centered on the sender's own block, {@code clear} drains the whole dimension,
 * both so the "hazard settlement" leg can be triggered and reset over and over.
 * <br><br>
 * The value cap keeps the old version's 100k — raw bookkeeping units, not a survivable dose; set to the cap means "step in, drop dead",
 * so permission stays at {@code CommandBase}'s default level (2, i.e. OP required), no loosening.
 */
public class CommandRadiation extends CommandBase {

	@Override
	public String getName() {
		return "ntmrad";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/ntmrad <set/clear>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

		if(args.length == 1 && "clear".equals(args[0])) {
			ChunkRadiationManager.proxy.clearSystem(sender.getEntityWorld());
			sender.sendMessage(new TextComponentString("Cleared radiation data!"));
			return;
		}

		if(args.length == 2 && "set".equals(args[0])) {

			float amount = (float) parseDouble(args[1], 0D, 100_000D);
			BlockPos pos = sender.getPosition();
			ChunkRadiationManager.proxy.setRadiation(sender.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ(), amount);
			sender.sendMessage(new TextComponentString("Radiation set."));
			return;
		}

		sender.sendMessage(new TextComponentString(TextFormatting.RED + getUsage(sender)));
	}
}
