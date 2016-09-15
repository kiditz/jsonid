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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.raden.jsonid.utils.LazilyParsedNumber;
import org.raden.jsonid.utils.Predictable;

/**
 * @author Rifky A.B
 *
 */
public class JsonNilai extends JsonElement {
	private static final Class<?>[] TipePrimitif = { int.class, long.class, short.class, float.class, double.class,
			byte.class, boolean.class, char.class, Integer.class, Long.class, Short.class, Float.class, Double.class,
			Byte.class, Boolean.class, Character.class };
	public Object nilai;
	JsonToken tipe = null;

	public JsonNilai() {
	}

	public JsonNilai(Object nilai) {
		atur(nilai);
	}

	public JsonNilai(boolean nilai) {
		atur(nilai);
	}

	public JsonNilai(String nilai) {
		atur(nilai);
	}

	public JsonNilai(Character nilai) {
		atur(nilai);
	}

	public JsonNilai(Number nilai) {
		atur(nilai);
	}

	public void atur(Object nilai) {
		if (nilai instanceof Character) {
			char c = ((Character) nilai).charValue();
			this.nilai = String.valueOf(c);
		} else {
			Predictable.cekArgument(nilai instanceof Number || iniNilaiAtauString(nilai));
			this.nilai = nilai;
		}
	}

	@Override
	public BigDecimal sebagaiBigDecimal() {
		return nilai instanceof BigDecimal ? (BigDecimal) nilai : new BigDecimal(nilai.toString());
	}

	@Override
	public BigInteger sebagaiBigInteger() {
		return nilai instanceof BigInteger ? (BigInteger) nilai : new BigInteger(nilai.toString());
	}

	public boolean iniString() {
		return nilai instanceof String;
	}

	public boolean iniAngka() {
		return nilai instanceof Number;
	}

	public boolean iniBoolean() {
		return nilai instanceof Boolean;
	}

	@Override
	public Number sebagaiAngka() {
		return !(nilai instanceof String) ? (Number) nilai : new LazilyParsedNumber((String) nilai);
	}

	@Override
	public boolean sebagaiBoolean() {
		return nilai instanceof Boolean ? (Boolean) nilai : Boolean.parseBoolean(sebagaiString());
	}

	@Override
	public String sebagaiString() {
		if (iniAngka())
			return sebagaiAngka().toString();
		else if (iniBoolean())
			return String.valueOf(sebagaiBoolean());
		return (String) nilai;
	}

	@Override
	public double sebagaiDouble() {
		return iniAngka() ? sebagaiAngka().doubleValue() : Double.parseDouble(sebagaiString());
	}

	@Override
	public float sebagaiFloat() {
		return iniAngka() ? sebagaiAngka().floatValue() : Float.parseFloat(sebagaiString());
	}

	@Override
	public short sebagaiShort() {
		return iniAngka() ? sebagaiAngka().shortValue() : Short.parseShort(sebagaiString());
	}

	@Override
	public int sebagaiInt() {
		return iniAngka() ? sebagaiAngka().shortValue() : Integer.parseInt(sebagaiString());
	}

	@Override
	public byte sebagaiByte() {
		return iniAngka() ? sebagaiAngka().byteValue() : Byte.parseByte(sebagaiString());
	}

	@Override
	public long sebagaiLong() {
		return iniAngka() ? sebagaiAngka().longValue() : Long.parseLong(sebagaiString());
	}

	@Override
	public char sebagaiKarakter() {
		return sebagaiString().charAt(0);
	}

	@Override
	public char sebagaiKarakter(int indeks) {
		return sebagaiString().charAt(indeks);
	}

	private static boolean iniNilaiAtauString(Object target) {
		if (target instanceof String) {
			return true;
		}
		Class<?> classOfPrimitive = target.getClass();
		for (Class<?> standardPrimitive : TipePrimitif) {
			if (standardPrimitive.isAssignableFrom(classOfPrimitive)) {
				return true;
			}
		}
		return false;
	}

	public static boolean iniNilaiAtauString(Class<?> target) {
		for (Class<?> standardPrimitive : TipePrimitif) {
			if (standardPrimitive.isAssignableFrom(target)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.raden.utils.json.JsonBagian#salin()
	 */
	@Override
	protected JsonElement salin() {
		return this;
	}

	
	public static JsonNilai baru() {
		return new JsonNilai();
	}
}
