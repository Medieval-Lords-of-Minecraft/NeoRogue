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
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CharmOfGallus extends Artifact {
	private static final String ID = "charmOfGallus";
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORK).count(10).speed(0.1).spread(0.5, 0.5);
	private int stamina;

	public CharmOfGallus() {
		super(ID, "Charm Of Gallus", Rarity.UNCOMMON, EquipmentClass.WARRIOR);

		stamina = 25;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		CharmOfGallusInstance inst = new CharmOfGallusInstance();
		data.addTrigger(id, Trigger.PRE_CAST_USABLE, inst);
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			return inst.checkUsed(p, (CastUsableEvent) in);
		});
	}
	
	public class CharmOfGallusInstance implements TriggerAction {
		private int count = 0;
		private String uuid = UUID.randomUUID().toString();

		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			if (count < 5) {
				CastUsableEvent ev = (CastUsableEvent) inputs;
				if (ev.getInstance().getEffectiveStaminaCost() == 0) return TriggerResult.keep();
				if (ev.getBuff(PropertyType.STAMINA_COST).apply(ev.getInstance().getStaminaCost()) <= 0) return TriggerResult.keep();
				ev.addBuff(PropertyType.STAMINA_COST, uuid, Buff.increase(data, stamina));
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
		item = createItem(Material.GOLD_NUGGET, 
				"The first 5 skills you cast have their stamina cost reduced by <white>" + stamina + "</white>.");
	}
}
