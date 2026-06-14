package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs;

/*
 * Semantic category of a floating text.
 *
 * This is the seam that keeps game logic and visualization separated: the game
 * layer only states WHAT happened (damage dealt, reward gained), never how it
 * should look. The visualization layer (J2dGame) maps each kind to a colour and
 * font. No presentation detail (colour, RGB, font) lives in the game/ package.
 */
public enum FloatingTextKind {

    /** Damage dealt to an enemy by a projectile. */
    DAMAGE,

    /** Gold/score gained from destroying an enemy. */
    REWARD
}
