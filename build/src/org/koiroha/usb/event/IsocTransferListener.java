/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: IsocTransferListener.java,v 1.2 2009/05/17 14:08:26 torao Exp $
*/
package org.koiroha.usb.event;

import java.util.EventListener;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// IsochTransferListener: Isochronous 転送リスナ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * Isochronous 転送を行うためのリスナです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.2 $ $Date: 2009/05/17 14:08:26 $
 * @author torao
 * @since 2009/04/26 Java2 SE 5.0
 */
public interface IsocTransferListener extends EventListener {

	// ======================================================================
	// Isochronou データ受信
	// ======================================================================
	/**
	 * Isochronous データの受信時に呼び出されます。
	 * <p>
	 * @param e 受信したデータ
	*/
	public void receive(IsocTransferEvent e);

	// ======================================================================
	// 中断
	// ======================================================================
	/**
	 * 例外により処理が中断された場合に呼び出されます。引数のイベントから中断原因となった例外を
	 * 参照することが出来ます。
	 * この呼び出しの後に {@link #transferStopped(IsocTransferEvent)} が呼び出され
	 * ます。
	 * <p>
	 * @param e 例外イベント
	*/
	public void transferAborted(IsocTransferEvent e);

	// ======================================================================
	// 転送処理の中止
	// ======================================================================
	/**
	 * 等時間隔転送の受信コールバックを終了する時に呼び出されます。このメソッドは例外による中断
	 * でも呼び出されます。
	 * <p>
	 * @param e 終了イベント
	*/
	public void transferStopped(IsocTransferEvent e);

}
