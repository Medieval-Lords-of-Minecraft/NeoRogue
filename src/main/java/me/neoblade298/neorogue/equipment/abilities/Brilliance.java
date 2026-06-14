package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Brilliance extends Equipment implements Power {
	private static final String ID = "Brilliance";
	private static final ParticleContainer brillianceEffect = new ParticleContainer(Particle.END_ROD).count(15).spread(0.5, 0.5).speed(0.2);
	private static final double RESISTANCE_AMOUNT = 0.5; // 50% resistance
	private static final int RESISTANCE_DURATION = 100; // 5 seconds in ticks
	private static final int ACTIVATION_THRES = 3;
	
	private int protectShell;
	
	public Brilliance(boolean isUpgraded) {
		super(ID, "Brilliance", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		protectShell = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		HashSet<DamageType> seenTypes = new HashSet<>();
		boolean[] activated = {false};
		
		// Power: activates after dealing 3 different types of damage
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (ev.getMeta().getSlices().isEmpty()) return TriggerResult.keep();
			
			DamageSlice primarySlice = ev.getMeta().getSlices().getFirst();
			DamageType currentType = primarySlice.getPostBuffType();
			seenTypes.add(currentType);
			
			if (seenTypes.size() < ACTIVATION_THRES) return TriggerResult.keep();
			if (activated[0]) return TriggerResult.remove();
			activated[0] = true;
			
			Player p = data.getPlayer();
			Sounds.enchant.play(p, p);
			brillianceEffect.play(p, p);
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}
	
	private DamageCategory getDamageCategoryFromType(DamageType type) {
		switch (type) {
			case FIRE: return DamageCategory.FIRE;
			case ICE: return DamageCategory.ICE;
			case LIGHTNING: return DamageCategory.LIGHTNING;
			case EARTHEN: return DamageCategory.EARTHEN;
			case DARK: return DamageCategory.DARK;
			case LIGHT: return DamageCategory.LIGHT;
			case POISON: return DamageCategory.POISON;
			case SLASHING: return DamageCategory.SLASHING;
			case PIERCING: return DamageCategory.PIERCING;
			case BLUNT: return DamageCategory.BLUNT;
			default: return null;
		}
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		String buffId = UUID.randomUUID().toString();
		// After activation: protect/shell + resistance on type change
		DamageType[] lastType = {null};
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata2, in2) -> {
			DealDamageEvent ev2 = (DealDamageEvent) in2;
			if (ev2.getMeta().getSlices().isEmpty()) return TriggerResult.keep();

			DamageSlice slice = ev2.getMeta().getSlices().getFirst();
			DamageType type = slice.getPostBuffType();

			if (type != lastType[0]) {
				Player p2 = data.getPlayer();
				data.applyStatus(StatusType.PROTECT, data, protectShell, RESISTANCE_DURATION);
				data.applyStatus(StatusType.SHELL, data, protectShell, RESISTANCE_DURATION);

				DamageCategory category = getDamageCategoryFromType(type);
				if (category != null) {
					data.addDefenseBuff(DamageBuffType.of(category), 
						Buff.multiplier(data, RESISTANCE_AMOUNT, StatTracker.defenseBuffAlly(buffId, this)), 
						RESISTANCE_DURATION);
				}

				brillianceEffect.play(p2, p2);
				Sounds.success.play(p2, p2);
				lastType[0] = type;
			}

			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
	item = createItem(Material.NETHER_STAR,
			GlossaryTag.POWER.tag(this) + ". Activates after dealing " + DescUtil.white(ACTIVATION_THRES) + 
			" different types of damage. Whenever you deal a damage type that is different " +
			"from your previous damage type, gain " + GlossaryTag.PROTECT.tag(this, protectShell, true) + 
			" and " + GlossaryTag.SHELL.tag(this, protectShell, true) + " [" + DescUtil.white("5s") + "] and " +
			DescUtil.white("50%") + " resistance to the damage type you used [" + DescUtil.white("5s") + "].");
	}
}
