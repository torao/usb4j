/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: DeviceEvent.java,v 1.1 2009/05/18 15:38:07 torao Exp $
*/
package org.koiroha.usb.event;

import java.util.EventObject;

import org.koiroha.usb.Device;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// DeviceEvent: USB デバイスイベント
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB デバイスのイベントを表すクラスです。
 * <p>
 * @version $Revision: 1.1 $ $Date: 2009/05/18 15:38:07 $
 * @author takami torao
 * @since usb4j 1.0 (Java2 SE 5.0) 2009/05/16
 */
public class DeviceEvent extends EventObject {

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
	 * イベントの発生した USB デバイスを指定して構築を行います。
	 * <p>
	 * @param device USB デバイス
	 */
	public DeviceEvent(Device device) {
		super(device);
		return;
	}

	// ======================================================================
	// デバイスの参照
	// ======================================================================
	/**
	 * このイベントの発生元となったデバイスを参照します。
	 * <p>
	 * @return デバイス
	 */
	public Device getDevice(){
		return (Device)getSource();
	}

}
