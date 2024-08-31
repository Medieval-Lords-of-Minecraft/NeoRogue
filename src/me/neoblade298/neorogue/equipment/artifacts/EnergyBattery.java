package me.neoblade298.neorogue.equipment.artifacts;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EnergyBattery extends Artifact {
	private static final String ID = "energyBattery";
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORKS_SPARK).count(10).speed(0.1).spread(0.5, 0.5);
	private int num;

	public EnergyBattery() {
		super(ID, "Energy Battery", Rarity.RARE, EquipmentClass.CLASSLESS);

		num = 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		EnergyBatteryInstance inst = new EnergyBatteryInstance();
		data.addTrigger(id, Trigger.PRE_CAST_USABLE, inst);
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			return inst.checkUsed(p, (CastUsableEvent) in);
		});
	}
	
	public class EnergyBatteryInstance implements TriggerAction {
		private int count = 0;
		String uuid = UUID.randomUUID().toString();

		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			if (count < num) {
				CastUsableEvent ev = (CastUsableEvent) inputs;
				if (ev.getInstance().getEffectiveStaminaCost() == 0 && ev.getInstance().getEffectiveManaCost() == 0) return TriggerResult.keep();
				if (ev.getBuff(PropertyType.STAMINA_COST).apply(ev.getInstance().getStaminaCost()) <= 0 &&
						ev.getBuff(PropertyType.MANA_COST).apply(ev.getInstance().getManaCost()) <= 0) return TriggerResult.keep();
				Player p = data.getPlayer();
				part.play(p, p);
				for (PropertyType type : new PropertyType[] { PropertyType.MANA_COST, PropertyType.STAMINA_COST, PropertyType.COOLDOWN }) {
					ev.addBuff(type, data, uuid, 1, true);
				}
				return TriggerResult.keep();
			}
			return TriggerResult.remove();
		}

		
		private TriggerResult checkUsed(Player p, CastUsableEvent ev) {
			if (ev.hasId(uuid)) {
				Sounds.success.play(p, p);
				part.play(p, p);
				Util.msg(p, display.append(Component.text(" was activated", NamedTextColor.GRAY)));
				if (++count >= 5) {
					return TriggerResult.remove();
				}
			}
			return TriggerResult.keep();
		}
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COPPER_BULB, 
				"Your first <white>" + num + "</white> skills are free to cast and have no cooldown.");
	}
}
