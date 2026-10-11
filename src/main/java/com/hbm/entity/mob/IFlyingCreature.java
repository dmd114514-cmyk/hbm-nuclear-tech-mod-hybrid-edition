package com.hbm.entity.mob;

/** The flying creatures' shared port: two state bits switching between "walk" and "fly" */
public interface IFlyingCreature {

	public static final int STATE_WALKING = 0;
	public static final int STATE_FLYING = 1;

	public int getFlyingState();
	public void setFlyingState(int state);
}
