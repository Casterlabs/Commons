package co.casterlabs.commons.localization;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

public abstract class LocaleProvider {
    private static final Pattern EXTERNAL_KEY_PATTERN = Pattern.compile("\\[[\\w\\.]+\\]");

    /**
     * This is a lookup prefix. All keys will have this prefix removed from them
     * when {@link LocaleProvider#process0(String, LocaleProvider, Map, List)} is
     * called.
     * 
     * @implSpec Return null if you do not wish to use prefixing.
     */
    public @Nullable String prefix() {
        return null;
    }

    /**
     * The following placeholders apply: <br />
     * - <b>{placeholder}</b>: Plain text placeholder. <br />
     * - <b>%component%</b>: UI components. <br />
     * - <b>[external_key]</b>: Pulls another translation key into the string.
     * <br />
     * <br />
     * 
     * Placeholders are replaced for you automatically EXCEPT for keys that end in
     * {@code .raw} or {@code .code}.
     * 
     * @return Null, if there is no value.
     */
    public final @Nullable String process(@NonNull String key, @Nullable LocaleProvider externalLookup, @NonNull Map<String, String> knownPlaceholders, @NonNull List<String> knownComponents) {
        if (this.prefix() != null) {
            if (!key.startsWith(this.prefix())) {
                // Not for us!
                return null;
            }

            key = key.substring(this.prefix().length());
            if (key.startsWith(".")) {
                // Swallow a leading period.
                key = key.substring(1);
            }
        }

        knownPlaceholders = Collections.unmodifiableMap(knownPlaceholders); // NOOP if already unmodifiable
        knownComponents = Collections.unmodifiableList(knownComponents); // NOOP if already unmodifiable

        if (externalLookup == null) {
            externalLookup = this; // circle.
        }

        String value = this.process0(key, externalLookup, knownPlaceholders, knownComponents);
        if (value == null) return null;

        // Avoid mangling keys that do not have placeholders.
        if (key.endsWith(".raw") || key.endsWith(".code")) return value;

        for (Map.Entry<String, String> placeholder : knownPlaceholders.entrySet()) {
            value = value.replace(
                '{' + placeholder.getKey() + '}',
                placeholder.getValue()
            );
        }

        {
            Matcher m = EXTERNAL_KEY_PATTERN.matcher(value);
            while (m.find()) {
                String match = m.group();
                String matchKey = match.substring(1, match.length() - 1);
                String matchValue = this.process(matchKey, externalLookup, knownPlaceholders, knownComponents);

                value = value.replace(
                    match,
                    matchValue
                );
            }
        }

        return value;
    }

    /**
     * @see      #process(String, Map, List, LocaleProvider)
     * 
     * @implSpec Return null if you do not have a value.
     */
    protected abstract @Nullable String process0(@NonNull String key, @NonNull LocaleProvider externalLookup, @NonNull Map<String, String> knownPlaceholders, @NonNull List<String> knownComponents);

}
