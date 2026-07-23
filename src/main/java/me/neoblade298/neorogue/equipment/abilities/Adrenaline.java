package me.neoblade298.neorogue.equipment.abilities;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
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

public class Adrenaline extends Equipment {
	private static final String ID = "Adrenaline";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_BLAZE_DEATH);
	private static final int stamina = 100;
	private static final int damageReduc = 2;
	
	public Adrenaline(boolean isUpgraded) {
		super(ID, "Adrenaline", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 5, 0));
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void setupReforges() {
		addReforge(Furor.get(), Burst.get(), Ferocity.get());
		addReforge(EnduranceTraining.get(), Discipline.get());
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"On cast, give yourself " + DescUtil.val(stamina) + " stamina and take " + DescUtil.val(damageReduc) + " less damage " + DescUtil.duration(10) + ". Can be cast " + DescUtil.val(isUpgraded ? "twice" : "once") + " per fight.");
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new AdrenalineInstance(data, sessionEq, slot, es));
	}
	
	private class AdrenalineInstance extends EquipmentInstance {
		private int count = 0;
		private int max;
		public AdrenalineInstance(PlayerFightData data, SessionEquipment sessionEq, int slot, EquipSlot es) {
			super(data, sessionEq, slot, es);
			Player p = data.getPlayer();
			max = isUpgraded ? 2 : 1;
			String id = UUID.randomUUID().toString();
			action = (pdata, in) -> {
				count++;
				sc.play(p, p);
				pc.play(p, p);
				pdata.addStamina(stamina);
				pdata.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(pdata, damageReduc, 0, StatTracker.defenseBuffAlly(id, eq, false)));
				if (count < max) return TriggerResult.keep();

				if (es == EquipSlot.HOTBAR) p.getInventory().setItem(slot, null);
				return TriggerResult.remove();
			};
		}
		
	}
}
