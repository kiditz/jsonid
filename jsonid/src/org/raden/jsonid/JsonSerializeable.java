/**
 * 
 */
package org.raden.jsonid;

/**
 * @author Rifky A.B
 *
 */
public interface JsonSerializeable {
	public void tulis(JsonID jsonID);

	public void baca(JsonID jsonID, JsonElement bagian);
}
