/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: USBContextListener.java,v 1.1 2009/05/18 20:34:19 torao Exp $
*/
package org.koiroha.usb.event;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// USBContextListener: USB コンテキストリスナ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB コンテキストリスナです。
 * <p>
 * @version $Revision: 1.1 $ $Date: 2009/05/18 20:34:19 $
 * @author takami torao
 * @since usb4j 1.0 (Java2 SE 5.0) 2009/05/16
 */
public interface USBContextListener {

	// ======================================================================
	// デバイスの取り付け
	// ======================================================================
	/**
	 * コンテキストが新規のデバイスを検出した時に呼び出されます。このメソッドが呼び出された時点で
	 * イベントに付加されているデバイスのインスタンスが利用可能です。
	 * <p>
	 * @param e デバイスイベント
	*/
	public void deviceAttached(DeviceEvent e);

	// ======================================================================
	// デバイスの取り付け
	// ======================================================================
	/**
	 * コンテキストがデバイスの取り外しを検出した時に呼び出されます。このメソッドが呼び出された
	 * 時点でイベントに付加されているデバイスのインスタンスへの操作は無効化されています。つまり
	 * このメソッドは {@link DeviceListener#deviceReleased(DeviceEvent)} より後に
	 * 呼び出されます。
	 * <p>
	 * @param e デバイスイベント
	*/
	public void deviceDetached(DeviceEvent e);

}
