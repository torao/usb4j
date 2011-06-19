/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: DeviceListener.java,v 1.1 2009/05/18 15:38:07 torao Exp $
*/
package org.koiroha.usb.event;

import java.util.EventListener;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// DeviceListener: デバイスリスナ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * デバイスリスナです。
 * <p>
 * @version $Revision: 1.1 $ $Date: 2009/05/18 15:38:07 $
 * @author takami torao
 * @since usb4j 1.0 (Java2 SE 5.0) 2009/05/18
 */
public interface DeviceListener extends EventListener{

	// ======================================================================
	// デバイスのオープン
	// ======================================================================
	/**
	 * デバイスがオープンされた時に呼び出されます。
	 * <p>
	 * @param e デバイスイベント
	*/
	public void deviceOpened(DeviceEvent e);

	// ======================================================================
	// デバイスのクローズ
	// ======================================================================
	/**
	 * デバイスがクローズされた時に呼び出されます。
	 * <p>
	 * @param e デバイスイベント
	*/
	public void deviceClosed(DeviceEvent e);

	// ======================================================================
	// デバイスの解放
	// ======================================================================
	/**
	 * デバイスが解放された時に呼び出されます。このイベントが通知された時点で該当するデバイス
	 * インスタンスは解放済みです。
	 * <p>
	 * @param e デバイスイベント
	*/
	public void deviceReleased(DeviceEvent e);

}
