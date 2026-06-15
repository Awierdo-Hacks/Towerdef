package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs;

/**
 * Semantic category of a floating text.
 *
 * <p>This is the seam that keeps game logic and visualization separated: the game
 * layer only states <em>what</em> happened (damage dealt, reward gained), never how
 * it should look. The visualization layer ({@code J2dGame}) maps each kind to a
 * colour and font. No presentation detail (colour, RGB, font) lives in the
 * {@code game} package.</p>
 *
 * @author Tower Defence team
 */
public enum FloatingTextKind {

    /** Damage dealt to an enemy by a projectile. */
    DAMAGE,

    /** Gold/score gained from destroying an enemy. */
    REWARD
}
