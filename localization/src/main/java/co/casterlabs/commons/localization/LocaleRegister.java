package co.casterlabs.commons.localization;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

/**
 * This class allows you to register several {@link LocaleProvider}s and
 * automatically iterate over them for each process().
 */
public class LocaleRegister extends LocaleProvider {
    private final String prefix;
    private final LocaleProvider[] providers;

    public LocaleRegister(@NonNull LocaleProvider... providers) {
        this(null, providers);
    }

    public LocaleRegister(@Nullable String prefix, @NonNull LocaleProvider... providers) {
        this.prefix = prefix;
        this.providers = providers;
    }

    @Override
    public @Nullable String prefix() {
        return this.prefix;
    }

    @Override
    protected @Nullable String process0(@NonNull String key, @NonNull LocaleProvider externalLookup, @NonNull Map<String, String> knownPlaceholders, @NonNull List<String> knownComponents) {
        for (LocaleProvider provider : this.providers) {
            String providerResult = provider.process(key, externalLookup, knownPlaceholders, knownComponents);

            if (providerResult != null) {
                // We found a valid value!
                return providerResult;
            }
        }

        return null;
    }

}
