package sk.stuba.fiit.items;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class HealingPotion extends Item {
    private final int healAmount;
    private static final Logger log = GameLogger.get(HealingPotion.class);


    public HealingPotion(int healAmount, Vector2D position) {
        super(2, position);
        this.healAmount = healAmount;
    }

    @Override
    public void use(PlayerCharacter character, Level level) {
        if (character.getHp() >= character.getMaxHp()) {
            log.warn("Healing potion used on full HP character: name={}, hp={}/{}",
                character.getName(), character.getHp(), character.getMaxHp());
            return;
        }
        int hpBefore = character.getHp();
        character.takeDamage(-healAmount);
        log.info("Healing potion used: target={}, healAmount={}, hp={}->{}/{}",
            character.getName(), healAmount, hpBefore,
            character.getHp(), character.getMaxHp());
        character.getInventory().removeItem(this);
    }

    @Override
    public String getIconPath() { return "icons/potion.png"; }
}
