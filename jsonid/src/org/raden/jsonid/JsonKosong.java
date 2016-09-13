/**
 * 
 */
package org.raden.jsonid;

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
