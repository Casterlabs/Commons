package co.casterlabs.commons.localization.helpers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.functional.Either;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.commons.localization.LocaleProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Use {@link Builder} to build your own instance.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BuildableLocaleProvider extends LocaleProvider {
    private final String prefix;
    private final Map<String, Either<String, ProviderFunction>> hashes;
    private final Pair<String, Either<String, ProviderFunction>>[] regexes;

    @Override
    public String prefix() {
        return this.prefix;
    }

    @Override
    protected final @Nullable String process0(@NonNull String key, @NonNull LocaleProvider externalLookup, @NonNull Map<String, String> knownPlaceholders, @NonNull List<String> knownComponents) {
        Either<String, ProviderFunction> either = this.hashes.get(key);

        if (either == null) {
            // We don't have a hash entry, so look for a regex entry instead.
            for (Pair<String, Either<String, ProviderFunction>> entry : this.regexes) {
                String regex = entry.a();

                if (key.matches(regex)) {
                    either = entry.b();
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

    @NoArgsConstructor
    public static class Builder {
        private Map<String, Either<String, ProviderFunction>> hashes = new HashMap<>();
        private List<Pair<String, Either<String, ProviderFunction>>> regexes = new LinkedList<>();

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
            this.regexes.add(new Pair<>(regex, Either.newA(value)));
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
            this.regexes.add(new Pair<>(regex, Either.newB(function)));
            return this;
        }

        /**
         * @return a new {@link BuildableLocaleProvider}.
         */
        @SuppressWarnings("unchecked")
        public BuildableLocaleProvider build() {
            return new BuildableLocaleProvider(
                this.prefix,
                new HashMap<String, Either<String, ProviderFunction>>(this.hashes),
                (Pair<String, Either<String, ProviderFunction>>[]) this.regexes.toArray()
            );
        }
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
