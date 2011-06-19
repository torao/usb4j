/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: IsocEventDispatcher.java,v 1.3 2009/05/17 14:08:25 torao Exp $
*/
package org.koiroha.usb.impl;

import org.koiroha.usb.USBException;
import org.koiroha.usb.event.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// IsochEventDispatcher: 等時間隔転送イベントディスパッチャー
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 等時間隔のデータ受信をイベント配信するためのスレッドです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.3 $ $Date: 2009/05/17 14:08:25 $
 * @author torao
 * @since 2009/04/26 Java2 SE 5.0
 */
class IsocEventDispatcher extends Thread {

	// ======================================================================
	// エンドポイント
	// ======================================================================
	/**
	 * 読み込みを行うエンドポイントです。
	 * <p>
	 */
	private final EndpointImpl endpoint;

	// ======================================================================
	// 等時間隔リスナ
	// ======================================================================
	/**
	 * データ受信時にコールバックを行うリスナです。
	 * <p>
	 */
	private final IsocTransferListener listener;

	// ======================================================================
	// 等時間隔転送イベント
	// ======================================================================
	/**
	 * 等時間隔転送のイベントです。
	 * <p>
	 */
	private final IsocTransferEvent event;

	// ======================================================================
	// 例外
	// ======================================================================
	/**
	 * 処理中に発生した例外です。
	 * <p>
	 */
	private USBException ex = null;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param endpoint 読み込みを行うエンドポイント
	 * @param listener 受信時にコールバックを行うリスナ
	 * @param event イベント
	 */
	public IsocEventDispatcher(
			EndpointImpl endpoint, IsocTransferListener listener, IsocTransferEvent event)
	{
		this.endpoint = endpoint;
		this.listener = listener;
		this.event = event;
		return;
	}

	// ======================================================================
	// スレッドの開始
	// ======================================================================
	/**
	 * Isochronous 受信を開始します。
	 * <p>
	*/
	@Override
	public void run() {
		try{
			endpoint.runIsocRead(listener, event);
		} catch(USBException ex){
			this.ex = ex;
			listener.transferAborted(new IsocTransferEvent(endpoint, ex));
		} finally {
			endpoint.dispatcherFinished();
			listener.transferStopped(new IsocTransferEvent(endpoint));
		}
		return;
	}

	// ======================================================================
	// データ受信通知
	// ======================================================================
	/**
	 * データを受信した時に呼び出されます。
	 * <p>
	 * @throws USBException 停止に失敗した場合
	*/
	public void stopDispatch() throws USBException{
		this.interrupt();
		return;
	}

	// ======================================================================
	// 例外の参照
	// ======================================================================
	/**
	 * 例外を参照します。
	 * <p>
	 * @return 実行で発生した例外
	*/
	public USBException getException(){
		return ex;
	}

}
