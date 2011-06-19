/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: USBException.java,v 1.4 2009/05/18 20:34:12 torao Exp $
*/
package org.koiroha.usb;

import java.io.IOException;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// USBException: USB 例外
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 *
 * <p>
 * @version usb4j 1.0 $Revision: 1.4 $ $Date: 2009/05/18 20:34:12 $
 * @author torao
 * @since 2009/04/22 Java2 SE 5.0
 */
public class USBException extends IOException {

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
	public USBException() {
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param message
	 */
	public USBException(String message) {
		super(message);
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param cause
	 */
	public USBException(Throwable cause) {
		initCause(cause);
		return;
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
	public USBException(String message, Throwable cause) {
		super(message);
		initCause(cause);
		return;
	}

}
