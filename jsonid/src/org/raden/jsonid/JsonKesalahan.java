/**
 * 
 */
package org.raden.jsonid;

/**
 * @author Rifky Aditya Bastara
 *
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
	 * @param string
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
