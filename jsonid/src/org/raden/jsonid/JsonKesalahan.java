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
 * Adalah kelas yang extends dari {@link RuntimeException} untuk melakukan throw
 * saat kode bermasalah
 * 
 * @author kiditz
 */
public class JsonKesalahan extends RuntimeException {

	private static final long serialVersionUID = 4869792990012599652L;
	private StringBuffer trace = new StringBuffer();

	public JsonKesalahan() {
		super();
	}

	public JsonKesalahan(String message, Throwable cause) {
		super(message, cause);
	}

	public JsonKesalahan(String message) {
		super(message);
	}

	public JsonKesalahan(Throwable cause) {
		super(cause);
	}

	/**
	 * @param info
	 *            append trace info
	 */
	public void addTrace(String info) {
		if (info == null)
			throw new IllegalArgumentException("info cannot be null.");
		if (trace == null)
			trace = new StringBuffer(512);
		trace.append('\n');
		trace.append(info);
	}

}
