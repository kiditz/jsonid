/**
 * 
 */
package org.raden.jsonid;

/**
 * @author Rifky A.B
 *
 */
public interface JsonSerializer<T> {
	public void tulis(JsonID jsonID, Object obyek, Class<?> tipe);

	public T baca(JsonID jsonID, JsonElement bagian, Class<?> tipe);

}
