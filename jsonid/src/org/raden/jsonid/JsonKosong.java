/*******************************************************************************
 * Copyright 2016 By Raden Studio.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.raden.jsonid;

/**
 * {@link JsonKosong} adalah sebuah kelas untuk membuat sebuah nilai kosong
 * 
 * @author Rifky A.B
 *
 */
public class JsonKosong extends JsonElement {

	private JsonKosong() {
	}

	@Override
	protected JsonElement salin() {
		return new JsonKosong();
	}

	/**
	 * Merupakan instant untuk menentukan konstruktor
	 * 
	 * @return new {@link JsonKosong}
	 */
	public static JsonKosong baru() {
		return new JsonKosong();
	}
}
