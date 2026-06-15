/**
 * Abstract entity hierarchy shared by all game objects.
 *
 * <p>Every game object derives from
 * {@link be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Entity}, which
 * defines position, size, and the abstract {@code update}/{@code render} contract. The
 * abstract subclasses {@code Tower}, {@code Enemy}, {@code Projectile} (with
 * {@code RayProjectile} and {@code CannonProjectile}) and {@code Base} capture shared
 * behaviour; concrete logic lives in the {@code towers}/{@code enemies} packages and
 * the visual implementations in {@code j2d.entities}.</p>
 *
 * @author Tower Defence team
 */
package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;
