/**
 * 
 */
package org.raden.jsonid.utils.json;

/**
 * @author Rifky A.B
 *
 */
public class JsonKosong extends JsonElement {

	public JsonKosong() {
		// DO NOTHING
	}

	@Override
	protected JsonElement salin() {
		return new JsonKosong();
	}

}
