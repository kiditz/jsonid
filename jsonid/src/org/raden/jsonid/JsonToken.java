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
 * Json Token yang digunakan oleh {@link JsonPembaca} untuk membaca nilai json
 */
public enum JsonToken {
	/**
	 * Mulai Array
	 */
	BEGIN_ARRAY,
	/**
	 * Akhir dari array
	 */
	END_ARRAY,
	/**
	 * Mulai Object
	 */
	BEGIN_OBJECT,
	/**
	 * Akhir dari object
	 */
	END_OBJECT,
	/**
	 * Sebuah nama yang dipakai oleh JSON
	 */
	NAME,

	/**
	 * Sebuah JSON String representasi dari dalam API Java
	 */
	STRING,

	/**
	 * Sebuah JSON angka representasi dari dalam API Java {@code double},
	 * {@code long}, atau {@code int}.
	 */
	ANGKA,

	/**
	 * Sebuah JSON{@code true} atau {@code false}.
	 */
	BOOLEAN,

	/**
	 * Sebuah JSON {@code null}.
	 */
	NULL,

	/**
	 * Akhir dari json kode {@link JsonPembaca#peek()} untuk melakukan signaling
	 * saat nilai tidak memiliki token lagi
	 */
	END_DOCUMENT
}
