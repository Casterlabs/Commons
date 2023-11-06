package co.casterlabs.commons.localization.helpers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.functional.Either;
import co.casterlabs.commons.localization.LocaleProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Use {@link Builder} to build your own instance.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BuildableLocaleProvider extends LocaleProvider {
    private final String prefix;
    private final Map<String, Either<String, ProviderFunction>> hashes;
    private final RegexEntry[] regexes;

    @Override
    public String prefix() {
        return this.prefix;
    }

    @Override
    protected final @Nullable String process0(@NonNull String key, @NonNull LocaleProvider externalLookup, @NonNull Map<String, String> knownPlaceholders, @NonNull List<String> knownComponents) {
        Either<String, ProviderFunction> either = this.hashes.get(key);

        if (either == null) {
            // We don't have a hash entry, so look for a regex entry instead.
            for (RegexEntry entry : this.regexes) {
                if (key.matches(entry.regex)) {
                    either = entry.value;
                    break;
                }
            }
        }

        // We have neither a regex entry nor a hash entry. So we don't have anything.
        if (either == null) return null;

        if (either.isA()) {
            return either.a();
        } else {
            return either.b().apply(key, externalLookup, knownPlaceholders, knownComponents);
        }
    }

    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Builder {
        private Map<String, Either<String, ProviderFunction>> hashes = new HashMap<>();
        private List<RegexEntry> regexes = new LinkedList<>();

        private @Setter String prefix;

        /**
         * Copies the hashes, regexes, and prefix from another existing provider. This
         * is useful if you wish to share translations between many similar locales (e.g
         * fr-CA and fr-FR share a lot of vocabulary).
         */
        public Builder(@NonNull BuildableLocaleProvider copyFrom) {
            this.prefix = copyFrom.prefix;
            this.hashes.putAll(copyFrom.hashes);
            this.regexes.addAll(Arrays.asList(copyFrom.regexes));
        }

        /**
         * Copies the keys and prefix from a {@link JsonLocaleProvider}. This is useful
         * if you wish to share translations between many similar locales (e.g fr-CA and
         * fr-FR share a lot of vocabulary).
         */
        public Builder(@NonNull JsonLocaleProvider copyFrom) {
            this.prefix = copyFrom.prefix;

            if (copyFrom.useRegexMode) {
                copyFrom.keys.forEach(this::Rstring);
            } else {
                copyFrom.keys.forEach(this::string);
            }
        }

        /**
         * @param  key   The key to match.
         * @param  value The value to return.
         * 
         * @return       this instance, for chaining.
         */
        public final Builder string(@NonNull String key, @NonNull String value) {
            this.hashes.put(key, Either.newA(value));
            return this;
        }

        /**
         * @param  key   The key to match with regex.
         * @param  value The value to return.
         * 
         * @return       this instance, for chaining.
         */
        public final Builder Rstring(@NonNull String regex, @NonNull String value) {
            this.regexes.add(new RegexEntry(regex, Either.newA(value)));
            return this;
        }

        /**
         * @param  key      The key to match.
         * @param  function A function that returns a string (or null).
         * 
         * @return          this instance, for chaining.
         */
        public final Builder function(@NonNull String key, @NonNull ProviderFunction function) {
            this.hashes.put(key, Either.newB(function));
            return this;
        }

        /**
         * @param  key      The key to match with regex.
         * @param  function A function that returns a string (or null).
         * 
         * @return          this instance, for chaining.
         */
        public final Builder Rfunction(@NonNull String regex, @NonNull ProviderFunction function) {
            this.regexes.add(new RegexEntry(regex, Either.newB(function)));
            return this;
        }

        /**
         * @return a new {@link BuildableLocaleProvider}.
         */
        public BuildableLocaleProvider build() {
            return new BuildableLocaleProvider(
                this.prefix,
                new HashMap<String, Either<String, ProviderFunction>>(this.hashes),
                this.regexes.toArray(new RegexEntry[0])
            );
        }
    }

    @AllArgsConstructor
    private static class RegexEntry {
        private String regex;
        private Either<String, ProviderFunction> value;
    }

    public static interface ProviderFunction {

        /**
         * @see      LocaleProvider#process(String, Map, List, LocaleProvider)
         * 
         * @implSpec Return null if you do not have a value.
         */
        // TODO keep this in sync with LocaleProvider#process
        public @Nullable String apply(@NonNull String key, @NonNull LocaleProvider externalLookup, @NonNull Map<String, String> knownPlaceholders, @NonNull List<String> knownComponents);

    }

}
