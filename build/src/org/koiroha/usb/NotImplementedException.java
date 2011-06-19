/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: NotImplementedException.java,v 1.4 2009/05/18 20:34:11 torao Exp $
*/
package org.koiroha.usb;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// NotImplementedException: 未実装例外
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 低レベル実装が操作を実装していない場合に発生する例外です。
 * <p>
 * @version usb4j 1.0 $Revision: 1.4 $ $Date: 2009/05/18 20:34:11 $
 * @author torao
 * @since 2009/04/27 Java2 SE 5.0
 */
public class NotImplementedException extends USBException {

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
	public NotImplementedException() {
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
	public NotImplementedException(String message) {
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
	public NotImplementedException(Throwable cause) {
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
	public NotImplementedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
