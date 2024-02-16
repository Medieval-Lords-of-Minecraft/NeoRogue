package me.neoblade298.neorogue.session.fight.status;

import java.util.UUID;

import org.bukkit.Bukkit;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.TickAction;
import net.kyori.adventure.text.Component;

public abstract class Status {
	protected String id;
	protected int stacks;
	protected TickAction action;
	protected FightData data;
	protected StatusSliceHolder slices = new StatusSliceHolder();
	protected int seconds;
	
	public Status(String id, FightData data) {
		this.id = id;
		this.data = data;
	}
	
	// Setting stacks or status to 0 means they will be untouched
	public abstract void apply(UUID applier, int stacks, int seconds);
	
	public static Status createByType(StatusType id, UUID applier, FightData target) {
		switch (id) {
		case POISON: return new PoisonStatus(target);
		case BLEED: return new BleedStatus(target);
		case BURN: return new DecrementStackStatus(id.name(), target);
		case FROST: return new DecrementStackStatus(id.name(), target);
		case ELECTRIFIED: return new DecrementStackStatus(id.name(), target);
		case CONCUSSED: return new ConcussedStatus(target);
		case INSANITY: return new DecrementStackStatus(id.name(), target);
		case SANCTIFIED: return new DecrementStackStatus(id.name(), target);
		case THORNS: return new BasicStatus(id.name(), target);
		case REFLECT: return new BasicStatus(id.name(), target);
		case BERSERK: return new BasicStatus(id.name(), target);
		case STRENGTH: return new StrengthStatus(target);
		}
		Bukkit.getLogger().warning("[NeoRogue] Failed to create status type " + id);
		return new BasicStatus(id.name(), target);
	}
	
	public static Status createByGenericType(GenericStatusType type, String id, UUID applier, FightData target) {
		switch (type) {
		case DECREMENT_STACK: return new DecrementStackStatus(id, target);
		case BASIC: return new BasicStatus(id, target);
		case DURATION: return new DurationStatus(id, target);
		default: return null;
		}
	}
	
	public void cleanup() {
		if (action != null) action.setCancelled(true);
	}
	
	public String getId() {
		return id;
	}
	
	public StatusSliceHolder getSlices() {
		return slices;
	}
	
	public int getStacks() {
		return stacks;
	}
	
	public enum StatusType {
		POISON("<dark_green>Poison</dark_green>"),
		BLEED("<red>Bleed</red>"),
		BURN("<gold>Burn</gold>"),
		FROST("<blue>Frost</blue>"),
		ELECTRIFIED("<yellow>Electrified</yellow>"),
		CONCUSSED("<dark_green>Concussed</dark_green>"),
		INSANITY("<dark_purple>Insanity</dark_purple>"),
		SANCTIFIED("<white>Sanctified</white>"),
		THORNS("<gold>Thorns</gold>"),
		REFLECT("<purple>Reflect</purple>"),
		BERSERK("<dark_red>Berserk</dark_red>"),
		STRENGTH("<red>Strength</red>");
		public String tag;
		public Component ctag;
		private StatusType(String tag) {
			this.tag = tag;
			this.ctag = SharedUtil.color(tag);
		}
	}
	public enum GenericStatusType {
		DECREMENT_STACK, BASIC, DURATION;
	}
}
