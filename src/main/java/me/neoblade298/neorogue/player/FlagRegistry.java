package me.neoblade298.neorogue.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

// Central registry of the flag "namespaces" used across NeoRogue (tutorials, caravan, etc.). Every
// flag string follows the "namespace:key" convention (e.g. "tutorial:first_ability_cast",
// "caravan:upgrade:cargo_access"), so the namespace is simply the substring before the first colon.
//
// Systems register their namespace with a supplier that returns the full flag strings they know
// about. The supplier is queried lazily so config-driven flags (tutorials, caravan packages/upgrades)
// stay in sync across reloads. This powers tab-completion, listing, and clearing by namespace.
public class FlagRegistry {
	public static final String GENERAL = "general", TUTORIAL = "tutorial", CARAVAN = "caravan";

	private static final LinkedHashMap<String, Supplier<Collection<String>>> providers = new LinkedHashMap<String, Supplier<Collection<String>>>();

	private FlagRegistry() {
	}

	// Registers (or replaces) the flag provider for a namespace. Safe to call again on reload.
	public static void register(String namespace, Supplier<Collection<String>> flagProvider) {
		providers.put(namespace.toLowerCase(), flagProvider);
	}

	// All registered namespaces, in registration order.
	public static Set<String> getNamespaces() {
		return Collections.unmodifiableSet(providers.keySet());
	}

	public static boolean hasNamespace(String namespace) {
		return providers.containsKey(namespace.toLowerCase());
	}

	// Every known/registerable flag across all namespaces, sorted and de-duplicated.
	public static List<String> getKnownFlags() {
		TreeSet<String> all = new TreeSet<String>();
		for (Supplier<Collection<String>> provider : providers.values()) {
			all.addAll(provider.get());
		}
		return new ArrayList<String>(all);
	}

	// Known flags belonging to a single namespace, sorted. Empty if the namespace isn't registered.
	public static List<String> getKnownFlags(String namespace) {
		Supplier<Collection<String>> provider = providers.get(namespace.toLowerCase());
		if (provider == null) return Collections.emptyList();
		return new ArrayList<String>(new TreeSet<String>(provider.get()));
	}

	// The namespace portion of a flag (everything before the first colon), or "" if it has none.
	public static String namespaceOf(String flag) {
		int i = flag.indexOf(':');
		return i < 0 ? "" : flag.substring(0, i);
	}
}
