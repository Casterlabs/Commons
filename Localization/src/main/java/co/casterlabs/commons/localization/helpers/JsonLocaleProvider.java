package co.casterlabs.commons.localization.helpers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.localization.LocaleProvider;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.NonNull;

/**
 * Deserialize a Json string into this class using Rson:
 * 
 * <pre>
 * LocaleProvider provider = Rson.DEFAULT.fromJson(str, JsonLocaleProvider.class);
 * </pre>
 * 
 * <pre>
 * {
 *     "prefix": "com.example.json",
 *     "useRegexMode": false,
 *     "keys": {
 *         "hello.world": "Bonjour monde !"
 *     }
 * }
 * </pre>
 */
@JsonClass(exposeAll = true)
public class JsonLocaleProvider extends LocaleProvider {
    String prefix = null;
    Map<String, String> keys = Collections.emptyMap();
    boolean useRegexMode = false;

    @Override
    public @Nullable String prefix() {
        return this.prefix;
    }

    @Override
    protected final @Nullable String process0(@NonNull String key, @NonNull LocaleProvider externalLookup, @NonNull Map<String, String> knownPlaceholders, @NonNull List<String> knownComponents) {
        if (this.useRegexMode) {
            for (Map.Entry<String, String> entry : this.keys.entrySet()) {
                String regex = entry.getKey();
                String value = entry.getValue();

                if (key.matches(regex)) {
                    return value;
                }
            }

            return null;
        } else {
            return this.keys.get(key);
        }
    }

}
