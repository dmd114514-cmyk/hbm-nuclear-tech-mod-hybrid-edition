package api.hbm.entity;

/**
 * Implementing this means radiation cannot touch it: contamination and dose maths skip the entity entirely.
 *
 * A marker interface instead of a lookup table, so any mod's entity (third party included) can declare immunity for itself
 * without shoving class names into an internal HBM list.
 */
public interface IRadiationImmune { }
