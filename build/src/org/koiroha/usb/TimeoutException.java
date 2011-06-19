/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: TimeoutException.java,v 1.1 2009/05/21 12:02:54 torao Exp $
*/
package org.koiroha.usb;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// TimeoutException:
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 *
 * <p>
 * @version $Revision: 1.1 $ $Date: 2009/05/21 12:02:54 $
 * @author takami torao
 * @since usb4j 1.0 (Java2 SE 5.0) 2009/05/21
 */
public class TimeoutException extends USBException {

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	public TimeoutException() {
		// TODO Auto-generated constructor stub
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param message
	 */
	public TimeoutException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param cause
	 */
	public TimeoutException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param message
	 * @param cause
	 */
	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
