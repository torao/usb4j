/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: OpenUSBService.java,v 1.5 2009/05/16 19:28:57 torao Exp $
*/
package org.koiroha.usb.impl.openusb;

import org.koiroha.usb.impl.USBServiceImpl;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// OpenUSBService: OpenUSB サービス実装
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * OpenUSB サービスの実装です。
 * <p>
 * @version $Revision: 1.5 $ $Date: 2009/05/16 19:28:57 $
 * @author takami torao
 * @since 2009/05/14 Java2 SE 5.0
 */
public class OpenUSBService extends USBServiceImpl {

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	public OpenUSBService() {
		super(new OpenUSBBridge());
		return;
	}

}
