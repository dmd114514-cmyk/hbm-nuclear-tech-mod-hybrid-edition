package com.hbm.tileentity.network;

import java.util.Collections;
import java.util.List;

import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.energymk2.IEnergyConductorMK2;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

/**
 * The base of every "cable-like" TE: stores no power itself, does one thing — merge the same wire run into one net,
 * with the net doing neighbor push/pull on its behalf (see ConductorNetNT). §16 retired the node graph, so no node registration here;
 * outward it answers Forge's energy capability: third parties can feed it and draw from it alike.
 */
public class TileEntityCableBaseNT extends TileEntityLoadedBase implements IEnergyConductorMK2, ITickable {

	private ConductorNetNT net;

	@Override
	public void update() {

		if(world.isRemote) return;

		if(!shouldJoinNet()) {
			if(net != null) {
				net.leave(this);
				net = null;
			}
			return;
		}

		if(netOrCreate() != null) net.tick();
	}

	/** Only wires that can conduct build a net; clients have no net, and a broken-wire switch shouldn't either */
	private ConductorNetNT netOrCreate() {

		if(world.isRemote || !shouldJoinNet()) return null;
		if(net == null || net.isDissolved()) net = ConductorNetNT.attach(this);
		return net;
	}

	/** Switch-like wires stay out of the net when open; subclasses implement per their own state */
	public boolean shouldJoinNet() {
		return true;
	}

	/** Connections beyond the six faces: pylon-style "long-distance direct" edges come from subclasses as endpoints; none by default */
	public List<int[]> getExtraNetLinks() {
		return Collections.emptyList();
	}

	/** The connection table changed (tower added/removed): let the net dissolve and re-lay, don't leave lines in the old topology */
	protected void refreshNet() {
		if(net != null) {
			net.leave(this);
			net = null;
		}
	}

	ConductorNetNT getNet() {
		return net;
	}

	void setNet(ConductorNetNT net) {
		this.net = net;
	}

	/** Leave the net before being broken or moved, so the old net stops remembering this position */
	@Override
	public void invalidate() {
		super.invalidate();

		if(net != null) {
			net.leave(this);
			net = null;
		}
	}

	/** Back in the world (chunk reload included) means rejoining an organization */
	@Override
	public void validate() {
		super.validate();
		this.net = null;
	}

	// The one and only capability exit: the whole net's port. The grid doesn't exist client-side, so clients are always refused
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if(super.hasCapability(capability, facing)) return true;
		return capability == CapabilityEnergy.ENERGY && !world.isRemote;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		T own = super.getCapability(capability, facing);
		if(own != null) return own;

		if(capability == CapabilityEnergy.ENERGY && !world.isRemote) {
			if(net == null || net.isDissolved()) net = ConductorNetNT.attach(this);
			return CapabilityEnergy.ENERGY.cast(net.port());
		}
		return null;
	}
}
