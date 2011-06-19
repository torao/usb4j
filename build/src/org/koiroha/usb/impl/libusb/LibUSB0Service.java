/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: LibUSB0Service.java,v 1.5 2009/05/16 19:28:56 torao Exp $
*/
package org.koiroha.usb.impl.libusb;

import org.koiroha.usb.impl.USBServiceImpl;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// LibUSB0Service: libusb 0.1 USB サービス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * libusb 0.1 を使用する USB サービス実装です。
 * <p>
 * @version usb4j 1.0 $Revision: 1.5 $ $Date: 2009/05/16 19:28:56 $
 * @author torao
 * @since 2009/05/01 Java2 SE 5.0
 */
public class LibUSB0Service extends USBServiceImpl{

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	public LibUSB0Service() {
		super(new LibUSB0Bridge());
		return;
	}

}
