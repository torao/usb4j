/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: IsocTransferEvent.java,v 1.2 2009/05/17 14:08:26 torao Exp $
*/
package org.koiroha.usb.event;

import java.util.EventObject;

import org.koiroha.usb.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// IsochTransferEvent: 等時間隔転送イベント
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 等時間隔 (Isochronous) 転送のエンドポイントがデータを受信した事を表すイベントです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.2 $ $Date: 2009/05/17 14:08:26 $
 * @author torao
 * @since 2009/04/26 Java2 SE 5.0
 */
public class IsocTransferEvent extends EventObject {

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// 開始フレーム
	// ======================================================================
	/**
	 * 開始フレームです。
	 * <p>
	 */
	private final long startFrame;

	// ======================================================================
	// データバッファ
	// ======================================================================
	/**
	 * 受信したデータのバッファです。
	 * <p>
	 */
	private final byte[][] buffer;

	// ======================================================================
	// データバッファ
	// ======================================================================
	/**
	 * 受信したデータのバッファです。
	 * <p>
	 */
	private final int[] length;

	// ======================================================================
	// エラーフラグ
	// ======================================================================
	/**
	 * フレームごとのエラーフラグです。
	 * <p>
	 */
	private final boolean[] error;

	// ======================================================================
	// 例外
	// ======================================================================
	/**
	 * 発生した例外です。
	 * <p>
	 */
	private final USBException ex;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param endpoint エンドポイント
	 * @param startFrame 開始フレーム
	 * @param buffer データ用バッファ
	 */
	public IsocTransferEvent(Endpoint endpoint, long startFrame, byte[][] buffer) {
		super(endpoint);
		this.buffer = buffer;
		this.startFrame = startFrame;
		this.length = new int[buffer.length];
		this.error = new boolean[buffer.length];
		this.ex = null;
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param endpoint エンドポイント
	 * @param ex 発生した例外
	 */
	public IsocTransferEvent(Endpoint endpoint, USBException ex) {
		super(endpoint);
		this.buffer = null;
		this.length = null;
		this.error = null;
		this.startFrame = 0;
		this.ex = ex;
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param endpoint エンドポイント
	 */
	public IsocTransferEvent(Endpoint endpoint) {
		super(endpoint);
		this.buffer = null;
		this.length = null;
		this.error = null;
		this.startFrame = 0;
		this.ex = null;
		return;
	}

	// ======================================================================
	// エンドポイントの取得
	// ======================================================================
	/**
	 * このイベントの発生元となったエンドポイントを取得します。
	 * <p>
	 * @return エンドポイント
	 */
	public Endpoint getEndpoint(){
		return (Endpoint)getSource();
	}

	// ======================================================================
	// 開始フレームの参照
	// ======================================================================
	/**
	 * 開始フレームを参照します。
	 * <p>
	 * @return 開始フレーム
	 */
	public long getStartFrame(){
		return startFrame;
	}

	// ======================================================================
	// フレーム数の参照
	// ======================================================================
	/**
	 * このイベントのフレーム数を参照します。
	 * <p>
	 * @return フレーム数
	 */
	public int getFrameSize(){
		return buffer.length;
	}

	// ======================================================================
	// バッファサイズの参照
	// ======================================================================
	/**
	 * このイベントの 1 フレーム当たりのバッファサイズを参照します。
	 * <p>
	 * @return バッファサイズ
	 */
	public int getBufferSize(){
		return buffer[0].length;
	}

	// ======================================================================
	// データの参照
	// ======================================================================
	/**
	 * このイベントのデータを参照します。メソッドは効率的な理由から生のバイト配列バッファを返し
	 * ますが、このバッファに対する変更操作は保証されません。
	 * <p>
	 * 返値は frame × bufSize のバイト配列です。
	 * <p>
	 * @return データのバッファ
	 */
	public byte[][] getBuffer(){
		return buffer;
	}

	// ======================================================================
	// データ長
	// ======================================================================
	/**
	 * フレームごとのデータ長です。
	 * <p>
	 * @return データ長
	 */
	public int[] getLength(){
		return length;
	}

	// ======================================================================
	// エラーフラグの参照
	// ======================================================================
	/**
	 * フレームごとのエラーフラグを参照します。
	 * <p>
	 * @return エラーフラグ
	 */
	public boolean[] getError(){
		return error;
	}

	// ======================================================================
	// 例外の参照
	// ======================================================================
	/**
	 * 受信処理中に発生した例外を参照します。
	 * <p>
	 * @return 例外
	 */
	public USBException getException(){
		return ex;
	}

}
