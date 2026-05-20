package me.neoblade298.neorogue.session.fight.mythicbukkit;

import org.bukkit.entity.Player;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class MechanicModifyStat implements ITargetedEntitySkill {
	private final Stat stat;
	private final Operation operation;
	private final double value;

    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.SYNC_ONLY;
    }

	public MechanicModifyStat(MythicLineConfig config) {
		this.stat = Stat.valueOf(config.getString(new String[] { "stat", "s" }).toUpperCase());
		this.operation = Operation.valueOf(config.getString(new String[] { "operation", "op" }).toUpperCase());
		this.value = config.getDouble(new String[] { "value", "v" }, 0);
	}

	@Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			if (!(target.getBukkitEntity() instanceof Player)) {
				return SkillResult.INVALID_TARGET;
			}
			Player p = (Player) target.getBukkitEntity();
			PlayerFightData pdata = FightInstance.getUserData(p.getUniqueId());
			
			if (pdata == null) {
				return SkillResult.INVALID_TARGET;
			}

			switch (stat) {
				case HEALTH:
					switch (operation) {
						case ADD:
							pdata.addHealth(value);
							break;
						case MULTIPLY:
							double currentHealth = p.getHealth();
							pdata.addHealth(currentHealth * value - currentHealth);
							break;
						case SET:
							p.setHealth(Math.min(value, pdata.getMaxHealth()));
							break;
					}
					break;
				case MAX_HEALTH:
					switch (operation) {
						case ADD:
							pdata.addMaxHealth(value);
							break;
						case MULTIPLY:
							double currentMaxHealth = pdata.getMaxHealth();
							pdata.addMaxHealth(currentMaxHealth * value - currentMaxHealth);
							break;
						case SET:
							double healthDiff = value - pdata.getMaxHealth();
							pdata.addMaxHealth(healthDiff);
							break;
					}
					break;
				case STAMINA:
					switch (operation) {
						case ADD:
							pdata.addStamina(value);
							break;
						case MULTIPLY:
							double currentStamina = pdata.getStamina();
							pdata.addStamina(currentStamina * value - currentStamina);
							break;
						case SET:
							pdata.setStamina(value);
							break;
					}
					break;
				case MAX_STAMINA:
					switch (operation) {
						case ADD:
							pdata.addMaxStamina(value);
							break;
						case MULTIPLY:
							double currentMaxStamina = pdata.getMaxStamina();
							pdata.addMaxStamina(currentMaxStamina * value - currentMaxStamina);
							break;
						case SET:
							pdata.setMaxStamina(value);
							break;
					}
					break;
				case MANA:
					switch (operation) {
						case ADD:
							pdata.addMana(value);
							break;
						case MULTIPLY:
							double currentMana = pdata.getMana();
							pdata.addMana(currentMana * value - currentMana);
							break;
						case SET:
							pdata.setMana(value);
							break;
					}
					break;
				case MAX_MANA:
					switch (operation) {
						case ADD:
							pdata.addMaxMana(value);
							break;
						case MULTIPLY:
							double currentMaxMana = pdata.getMaxMana();
							pdata.addMaxMana(currentMaxMana * value - currentMaxMana);
							break;
						case SET:
							pdata.setMaxMana(value);
							break;
					}
					break;
				case SPRINT_COST:
					switch (operation) {
						case ADD:
							pdata.addSprintCost(value);
							break;
						case MULTIPLY:
							double currentSprintCost = pdata.getSprintCost();
							pdata.addSprintCost(currentSprintCost * value - currentSprintCost);
							break;
						case SET:
							pdata.setSprintCost(value);
							break;
					}
					break;
				case STAMINA_REGEN:
					switch (operation) {
						case ADD:
							pdata.addStaminaRegen(value);
							break;
						case MULTIPLY:
							double currentStaminaRegen = pdata.getStaminaRegen();
							pdata.addStaminaRegen(currentStaminaRegen * value - currentStaminaRegen);
							break;
						case SET:
							pdata.setStaminaRegen(value);
							break;
					}
					break;
				case MANA_REGEN:
					switch (operation) {
						case ADD:
							pdata.addManaRegen(value);
							break;
						case MULTIPLY:
							double currentManaRegen = pdata.getManaRegen();
							pdata.addManaRegen(currentManaRegen * value - currentManaRegen);
							break;
						case SET:
							pdata.setManaRegen(value);
							break;
					}
					break;
			}
			
			return SkillResult.SUCCESS;
		}
		catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
    }

	private static enum Stat {
		HEALTH,
		MAX_HEALTH,
		STAMINA,
		MAX_STAMINA,
		STAMINA_REGEN,
		MANA,
		MAX_MANA,
		MANA_REGEN,
		SPRINT_COST;
	}

	private static enum Operation {
		ADD,
		MULTIPLY,
		SET;
	}
}
