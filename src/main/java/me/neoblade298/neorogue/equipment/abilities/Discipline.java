package me.neoblade298.neorogue.equipment.abilities;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Discipline extends Equipment {
	private static final String ID = "Discipline";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST);
	private static final int stamina = 50;
	private static final int INTERVAL = 15;
	private int staminaGain;
	
	public Discipline(boolean isUpgraded) {
		super(ID, "Discipline", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
		staminaGain = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"Every " + DescUtil.white(INTERVAL + "s") + ", give yourself " + DescUtil.white(stamina) + " stamina, " + DescUtil.yellow(staminaGain) + " max stamina, and"
						+ " take " + DescUtil.white(7) + " less damage " + DescUtil.duration(10, false) + ".");
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		String buffId = UUID.randomUUID().toString();
		int[] tick = {0};
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			tick[0]++;
			if (tick[0] < INTERVAL) return TriggerResult.keep();
			tick[0] = 0;
			Player p = data.getPlayer();
			Sounds.blazeDeath.play(p, p);
			pc.play(p, p);
			pdata.addMaxStamina(staminaGain);
			pdata.addStamina(stamina);
			data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, 7, 0, StatTracker.defenseBuffAlly(buffId, this)), 200);
			return TriggerResult.keep();
		});
	}
}
