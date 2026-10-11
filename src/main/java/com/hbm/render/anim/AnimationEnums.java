package com.hbm.render.anim;

public class AnimationEnums {

	// The shell-loading caveat: the motion of stuffing shells into the tube must hang on RELOAD; only RELOAD_CYCLE would fire once per shell stuffed
	public static enum GunAnimation {
		RELOAD,			//The full reload, or the start of the reload motion
		RELOAD_CYCLE,	//The single-shell reload loop; shell-by-shell weapons like shotguns fire this once per shell stuffed
		RELOAD_END,		//The wind-down motion from the single-shell loop back to idle
		CYCLE,			//The loop played on every shot
		CYCLE_EMPTY,	//The motion for the last shell in the magazine
		CYCLE_DRY,		//The motion when the trigger is pulled dry
		ALT_CYCLE,		//The offhand firing loop
		SPINUP,			//Motion start (spin-up, that sort)
		SPINDOWN,		//Motion end
		EQUIP,			// draw weapon
		INSPECT,		//Inspecting the weapon
		JAMMED,			//Jammed state
	}

	public static enum ToolAnimation {
		SWING,
		EQUIP,
	}

}
