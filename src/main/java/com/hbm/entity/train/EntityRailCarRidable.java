package com.hbm.entity.train;

import java.util.ArrayList;
import java.util.List;

import com.hbm.main.MainRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The rideable layer: driver's seat + passenger seats. Passenger seats are dynamically spawned empty entities that follow the car,
 * so riders on a four-car long train follow their own car's pitch instead of being pinned to the core.
 *
 * This version's shape: riding is no longer a single riddenByEntity field but a passenger set (getControllingPassenger() picks the driver),
 * the rider-position hook changed from updateRiderPosition() to updatePassenger(Entity); boarding is startRiding.
 */
public abstract class EntityRailCarRidable extends EntityRailCarCargo {

	public double engineSpeed;
	public SeatDummyEntity[] passengerSeats;

	public EntityRailCarRidable(World world) {
		super(world);
		this.passengerSeats = new SeatDummyEntity[this.getPassengerSeats().length];
	}

	/** Returns the linear speed added per tick when using powered movement */
	public abstract double getPoweredAcceleration();
	/** A mulitplier used on the speed either is there is no player in the train or if the parking brake is active */
	public abstract double getPassivBrake();
	/** The parking brake can be toggled, assuming a player is present, otherwise it is implicitly ON */
	public abstract boolean shouldUseEngineBrake(EntityPlayer player);
	/** The max speed the engine can provide in both directions */
	public abstract double getMaxPoweredSpeed();
	/** Whether the engine is turned on */
	public abstract boolean canAccelerate();
	/** Called every tick if acceleration is successful */
	public void consumeFuel() { }

	/** An additive to the engine's speed yielding the total speed, caused by uneven surfaces */
	public double getGravitySpeed() {
		return 0D;
	}

	@Override
	public double getCurrentSpeed() { // in its current form, only call once per tick

		Entity rider = this.getControllingPassenger();

		if(rider instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer) rider;

			if(this.canAccelerate()) {
				if(player.moveForward > 0) {
					engineSpeed += this.getPoweredAcceleration();
					this.consumeFuel();
				} else if(player.moveForward < 0) {
					engineSpeed -= this.getPoweredAcceleration();
					this.consumeFuel();
				} else {
					if(this.shouldUseEngineBrake(player)) {
						engineSpeed *= this.getPassivBrake();
					} else {
						this.consumeFuel();
					}
				}
			} else {
				engineSpeed *= this.getPassivBrake();
			}

		} else {
			engineSpeed *= this.getPassivBrake();
		}

		double maxSpeed = this.getMaxPoweredSpeed();
		engineSpeed = MathHelper.clamp(engineSpeed, -maxSpeed, maxSpeed);

		return engineSpeed + this.getGravitySpeed();
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {

		if(super.processInitialInteract(player, hand)) return true;
		if(world.isRemote) return true;

		int nearestSeat = this.getNearestSeat(player);

		if(nearestSeat == -1) {
			player.startRiding(this);
		} else if(nearestSeat >= 0) {
			SeatDummyEntity dummySeat = new SeatDummyEntity(world, this, nearestSeat);
			Vec3d passengerSeat = rotateTo(this.getPassengerSeats()[nearestSeat]);
			dummySeat.setPosition(this.renderX + passengerSeat.x, this.renderY + passengerSeat.y - 1, this.renderZ + passengerSeat.z);
			passengerSeats[nearestSeat] = dummySeat;
			world.spawnEntity(dummySeat);
			player.startRiding(dummySeat);
		}

		return true;
	}

	/** Seats are offsets from the car core, rotated into the world by the car's current facing/pitch */
	private Vec3d rotateTo(Vec3d offset) {
		return offset.rotatePitch((float) (this.rotationPitch * Math.PI / 180D)).rotateYaw((float) (-this.rotationYaw * Math.PI / 180));
	}

	public int getNearestSeat(EntityPlayer player) {

		double nearestDist = Double.POSITIVE_INFINITY;
		int nearestSeat = -3;

		Vec3d[] seats = getPassengerSeats();
		Vec3d look = player.getLook(2).addVector(player.posX, player.posY + player.getEyeHeight() - player.getYOffset(), player.posZ);

		for(int i = 0; i < seats.length; i++) {

			Vec3d seat = seats[i];
			if(seat == null) continue;
			if(passengerSeats[i] != null) continue;

			Vec3d turned = rotateTo(seat);
			Vec3d delta = look.subtract(this.renderX + turned.x, this.renderY + turned.y, this.renderZ + turned.z);
			double dist = delta.lengthVector();

			if(dist < nearestDist) {
				nearestDist = dist;
				nearestSeat = i;
			}
		}

		if(this.getControllingPassenger() == null) {
			Vec3d turned = rotateTo(getRiderSeatPosition());
			Vec3d delta = look.subtract(this.renderX + turned.x, this.renderY + turned.y, this.renderZ + turned.z);
			double dist = delta.lengthVector();

			if(dist < nearestDist) {
				nearestDist = dist;
				nearestSeat = -1;
			}
		}

		if(nearestDist > 180) return -2;

		return nearestSeat;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!world.isRemote) {

			Vec3d[] seats = this.getPassengerSeats();
			for(int i = 0; i < passengerSeats.length; i++) {
				SeatDummyEntity seat = passengerSeats[i];

				if(seat != null) {
					if(seat.getControllingPassenger() == null) {
						passengerSeats[i] = null;
						seat.setDead();
					} else {
						Vec3d turned = rotateTo(seats[i]);
						seat.setPosition(this.renderX + turned.x, this.renderY + turned.y - 1, this.renderZ + turned.z);
					}
				}
			}
		}
	}

	@Override
	public void updatePassenger(Entity passenger) {

		Vec3d offset = rotateTo(getRiderSeatPosition());

		if(this.getControllingPassenger() == passenger) {
			passenger.setPosition(this.renderX + offset.x, this.renderY + offset.y, this.renderZ + offset.z);
		}
	}

	/** Returns a Vec3 showing the relative position from the driver to the core */
	public abstract Vec3d getRiderSeatPosition();

	public abstract Vec3d[] getPassengerSeats();

	/** Dynamic seats generated when a player clicks near a seat-spot, moves and rotates with the train as one would expect. */
	public static class SeatDummyEntity extends Entity {

		private static final DataParameter<Integer> DW_TRAIN = EntityDataManager.createKey(SeatDummyEntity.class, DataSerializers.VARINT);
		private static final DataParameter<Integer> DW_INDEX = EntityDataManager.createKey(SeatDummyEntity.class, DataSerializers.VARINT);

		private int turnProgress;
		private double trainX;
		private double trainY;
		private double trainZ;
		public EntityRailCarRidable train;

		public SeatDummyEntity(World world) { super(world); this.setSize(0.5F, 0.1F);}
		public SeatDummyEntity(World world, EntityRailCarRidable train, int index) {
			this(world);
			this.train = train;
			if(train != null) this.getDataManager().set(DW_TRAIN, Integer.valueOf(train.getEntityId()));
			this.getDataManager().set(DW_INDEX, Integer.valueOf(index));
		}

		@Override protected void entityInit() {
			this.getDataManager().register(DW_TRAIN, Integer.valueOf(0));
			this.getDataManager().register(DW_INDEX, Integer.valueOf(0));
		}
		@Override protected void writeEntityToNBT(NBTTagCompound nbt) { }
		@Override public boolean writeToNBTOptional(NBTTagCompound nbt) { return false; }
		@Override protected void readEntityFromNBT(NBTTagCompound nbt) { this.setDead(); }

		@Override public void onUpdate() {
			if(!world.isRemote) {
				if(this.train == null || this.train.isDead) {
					this.setDead();
				}
			} else {

				if(this.turnProgress > 0) {
					this.prevRotationYaw = this.rotationYaw;
					double x = this.posX + (this.trainX - this.posX) / (double) this.turnProgress;
					double y = this.posY + (this.trainY - this.posY) / (double) this.turnProgress;
					double z = this.posZ + (this.trainZ - this.posZ) / (double) this.turnProgress;
					--this.turnProgress;
					this.setPosition(x, y, z);
				} else {
					this.setPosition(this.posX, this.posY, this.posZ);
				}
			}
		}

		@Override @SideOnly(Side.CLIENT) public void setPositionAndRotationDirect(double posX, double posY, double posZ, float yaw, float pitch, int turnProg, boolean teleport) {
			this.trainX = posX;
			this.trainY = posY;
			this.trainZ = posZ;
			this.turnProgress = turnProg + 2;
		}

		@Override
		public void updatePassenger(Entity passenger) {
			if(this.getControllingPassenger() != passenger) return;

			if(train == null) {
				Entity entity = world.getEntityByID(this.getDataManager().get(DW_TRAIN).intValue());
				if(entity instanceof EntityRailCarRidable) {
					train = (EntityRailCarRidable) entity;
				}
			}

			//fallback for when train is null
			if(train == null) {
				passenger.setPosition(posX, posY + 1, posZ);
				return;
			}

			//doing it like this instead of with the position directly removes any discrepancies caused by entity tick order
			//mmhmhmhm silky smooth
			int index = this.getDataManager().get(DW_INDEX).intValue();
			Vec3d rot = train.getPassengerSeats()[index].rotatePitch((float) (train.rotationPitch * Math.PI / 180)).rotateYaw((float) (-train.rotationYaw * Math.PI / 180));
			passenger.setPosition(train.renderX + rot.x, train.renderY + rot.y, train.renderZ + rot.z);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, net.minecraft.util.math.BlockPos pos) {
		List<String> text = new ArrayList<String>();
		/*text.add("LTU: " + this.ltu);
		text.add("Front: " + this.coupledFront);
		text.add("Back: " + this.coupledBack);*/
		text.add("Nearest seat: " + this.getNearestSeat(MainRegistry.proxy.me()));
		//ILookOverlay.printGeneric(event, this.getClass().getSimpleName() + " " + this.hashCode(), 0xffff00, 0x404000, text);
	}
}
