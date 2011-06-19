/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: DeviceFilter.java,v 1.2 2009/05/18 20:34:12 torao Exp $
*/
package org.koiroha.usb;

import org.koiroha.usb.desc.DeviceDescriptor;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// DeviceFilter: デバイスフィルタ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB コンテキストからのデバイス検索時にフィルタリングを行うためのインターフェースです。デバイス
 * 記述子から判定を行います。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2009/05/18 20:34:12 $
 * @author takami torao
 * @since usb4j 1.0 (Java2 SE 5.0) 2009/05/15
 * @see USBContext#lookup(DeviceFilter)
 */
public interface DeviceFilter {

	// ======================================================================
	// 検索対象の判定
	// ======================================================================
	/**
	 * 指定されたデバイス記述子を持つデバイスが検索対象かどうかを判定します。
	 * <p>
	 * @param desc 判定するデバイス記述子
	 * @return 検索対象の場合 true
	*/
	public boolean accept(DeviceDescriptor desc);

}
