package co.casterlabs.commons.ipc;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.rakurai.json.validation.JsonValidationException;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonClass(exposeAll = true)
public class _FauxObject {
    private @Nullable String instanceId = null;
    private String clazz;
    private JsonElement o;

    _FauxObject(@Nullable Object obj) {
        if (obj == null) {
            this.clazz = null;
            this.o = JsonNull.INSTANCE;
            return;
        }

        this.clazz = obj.getClass().getTypeName();
        this.o = Rson.DEFAULT.toJson(obj);

        if (obj instanceof IpcObject) {
            this.instanceId = ((IpcObject) obj).$id;
        }
    }

    Object get(IpcConnection connection) throws ClassNotFoundException, JsonValidationException, JsonParseException {
        if (this.clazz == null) {
            return null;
        }

        Class<?> clazz = Class.forName(this.clazz);

        if (this.instanceId == null) {
            return Rson.DEFAULT.fromJson(this.o, clazz);
        } else {
            if (connection == null) {
                throw new IpcException("Unable to get remote instance without IpcConnection.");
            }

            return _RemoteObjectProxy.getProxy(clazz, this.instanceId, connection);
        }
    }

}
