package com.hbm.entity.mob;

import com.hbm.lib.RefStrings;

import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * The crab family: two sea-floor crabs (tropical/terracotta), form deciding texture (RenderEntityMulti picks by getEnum()).
 *
 * Two deltas:
 * - the old reported a clay block via {@code getDropItem()}; EntityWaterMob has no such door here (not even
 *   {@code dropFewItems}), drops go through loot tables, so this points at its own
 *   {@code entities/entity_scuttlecrab}, with the count tier (1~2 blocks) written in the table.
 * - the old {@code moveEntityWithHeading(strafe, forward)} is this version's {@code travel(strafe, up, forward)};
 *   the vertical argument exists only here with no old counterpart, passed through as-is.
 *
 * The old `return this.inWater` (reading 1.7's cached field) has no same-named field here, replaced by {@code super.isInWater()}
 * computed live — the same criterion, just not one tick stale.
 */
public class EntityScuttlecrab extends EntityWaterMob implements IEntityEnumMulti {

	private static final ResourceLocation LOOT_TABLE = new ResourceLocation(RefStrings.MODID, "entities/entity_scuttlecrab");

	public enum Scuttlecrab {
		TROPICAL,
		CLAYINFUSED,
	}

	public Scuttlecrab type;

	public EntityScuttlecrab(World world) {
		super(world);

		type = Scuttlecrab.TROPICAL;
		if(world.rand.nextInt(8) == 0) type = Scuttlecrab.CLAYINFUSED;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum getEnum() {
		return type;
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}

	// Crawling the sea floor: hence pinning the "am I in water" criterion across the whole behavior set
	private boolean seafloorWalking = false;

	@Override
	public boolean isInWater() {
		if(seafloorWalking) return false;
		return super.isInWater();
	}

	@Override
	public void onLivingUpdate() {
		seafloorWalking = true;
		super.onLivingUpdate();
		seafloorWalking = false;
	}

	// Sideways
	float crabDirection = 1;
	int switchTimer = 0;

	@Override
	public void travel(float strafe, float vertical, float forward) {
		switchTimer--;
		if(switchTimer <= 0) {
			crabDirection *= -1;
			switchTimer = 20 + world.rand.nextInt(80);
		}

		super.travel(forward * crabDirection, vertical, strafe);
	}
}
