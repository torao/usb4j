/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: ControlRequest.java,v 1.1 2009/05/21 12:02:54 torao Exp $
*/
package org.koiroha.usb;

import java.io.Serializable;
import java.nio.ByteBuffer;


// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// ControlRequest: コントロール要求
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * コントロール要求を表すクラスです。
 * <p>
 * このクラスにはデータ用のバッファが含まれています。
 * <p>
 * @version usb4j 1.0 $Revision: 1.1 $ $Date: 2009/05/21 12:02:54 $
 * @author takami torao
 * @since 2009/05/13 Java2 SE 5.0
 */
public class ControlRequest implements Serializable{

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// リクエスト
	// ======================================================================

	/**
	 * 対象のステータスを取得するリクエストを表す {@code bRequest} 定数 {@value} です。
	 * <p>
	 * @see #getRequest()
	*/
	public static final byte GET_STATUS = 0x00;

	/**
	 * 特定の機能をクリアするためのリクエストを表す {@code bRequest} 定数 {@value} です。
	 * ストールの解除などを行います。
	 * <p>
	 * @see #getRequest()
	*/
	public static final byte CLEAR_FEATURE = 0x01;
	// 0x02 は予約

	/**
	 * 特定の機能を設定するためのリクエストを表す {@code bRequest} 定数 {@value} です。
	 * リモートウェイクアップなどの設定を行います。
	 * <p>
	 * @see #getRequest()
	*/
	public static final byte SET_FEATURE = 0x03;
	// 0x04 は予約

	/**
	 * デバイスのアドレスを設定するためのリクエストを表す {@code bRequest} 定数 {@value}
	 * です。
	 * <p>
	 * @see #getRequest()
	*/
	public static final byte SET_ADDRESS = 0x05;

	/**
	 * 記述子を取得するためのリクエストを表す {@code bRequest} 定数 {@value} です。
	 * <p>
	 * @see #getRequest()
	*/
	public static final byte GET_DESCRIPTOR = 0x06;

	/**
	 * 記述子を設定するためのリクエストを表す {@code bRequest} 定数 {@value} です。
	 * <p>
	 * @see #getRequest()
	*/
	public static final byte SET_DESCRIPTOR = 0x07;

	/**
	 * 現在のコンフィギュレーション番号を取得するためのリクエストを表す {@code bRequest}
	 * 定数 {@value} です。
	 * <p>
	 * @see #getRequest()
	*/
	public static final byte GET_CONFIGURATION = 0x08;

	/**
	 * 現在のコンフィギュレーション番号を設定するためのリクエストを表す {@code bRequest}
	 * 定数 {@value} です。
	 * <p>
	 * @see #getRequest()
	*/
	public static final byte SET_CONFIGURATION = 0x09;

	/**
	 * 現在のインターフェース代替設定番号を取得するためのリクエストを表す {@code bRequest}
	 * 定数 {@value} です。
	 * <p>
	 * @see #getRequest()
	*/
	public static final byte GET_INTERFACE = 0x0A;

	/**
	 * 現在のインターフェース代替設定番号を設定するためのリクエストを表す {@code bRequest}
	 * 定数 {@value} です。
	 * <p>
	 * @see #getRequest()
	*/
	public static final byte SET_INTERFACE = 0x0B;

	/**
	 * パターン同期の ISOCH 転送でパターン開始番号を取得するためのリクエストを表す
	 * {@code bRequest} 定数 {@value} です。
	 * <p>
	 * @see #getRequest()
	*/
	public static final byte SYNCH_FRAME = 0x0C;

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// DIR: リクエスト方向
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * リクエスト方向を示す列挙です。{@code bmRequestType}[b7] で表されます。
	 */
	public enum DIR {

		/**
		 * ホストからデバイス方向へのリクエストを示す列挙です。{@code bmRequestType}[b7]
		 * が 0 である事を表します。
		 * <p>
		 * @see #getRequestType()
		 */
		IN,

		/**
		 * デバイスからホスト方向へのリクエストを示す列挙です。{@code bmRequestType}[b7]
		 * が 1 であることを表します。
		 * <p>
		 * @see #getRequestType()
		 */
		OUT,
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// TYPE: リクエストタイプ
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * リクエストタイプを示す列挙です。{@code bmRequestType}[b6-b5] で表されます。
	 */
	public enum TYPE {

		/**
		 * 標準のリクエストを示す列挙です。{@code bmRequestType}[b6-b5] が 0 で表されます。
		 * <p>
		 * @see #getRequestType()
		 */
		STANDARD,

		/**
		 * クラスのリクエストを示す列挙です。{@code bmRequestType}[b6-b5] が 1 で表されます。
		 * <p>
		 * @see #getRequestType()
		 */
		CLASS,

		/**
		 * ベンダーのリクエストを示す列挙です。{@code bmRequestType}[b6-b5] が 2 で表されます。
		 * <p>
		 * @see #getRequestType()
		 */
		VENDOR,

		/**
		 * 予約を示す列挙です。{@code bmRequestType}[b6-b5] が 3 で表されます。
		 * <p>
		 * @see #getRequestType()
		 */
		RESERVED,
	}

	/**
	 * デバイスへのリクエストを示す列挙です。{@code bmRequestType}[b4-b0] が 0 で
	 * 表されます。
	 * <p>
	 * @see #getRequestType()
	 */
	public static final int RCPT_DEVICE = 0x00;

	/**
	 * インターフェースへのリクエストを示す列挙です。{@code bmRequestType}[b4-b0]
	 * が 1 で表されます。
	 * <p>
	 * @see #getRequestType()
	 */
	public static final int RCPT_INTERFACE = 0x01;

	/**
	 * エンドポイントへのリクエストを示す列挙です。{@code bmRequestType}[b4-b0]
	 * が 2 で表されます。
	 * <p>
	 * @see #getRequestType()
	 */
	public static final int RCPT_ENDPOINT = 0x02;

	/**
	 * その他のリクエストを示す {@code bmRequestType} のビット値 {@value} です。
	 * <p>
	 * @see #getRequestType()
	 */
	public static final int RCPT_OTHER = 0x03;

	/**
	 * {@code bmRequestType} に対してリクエストの転送方向を示すビットマスク {@value} です。
	 * <p>
	 * @see #getRequestType()
	 */
	private static final int TYPEMASK_DIRECTION = 0x80;

	/**
	 * {@code bmRequestType} に対してリクエストの種類を示すビットマスク {@value} です。
	 * <p>
	 * @see #getRequestType()
	 */
	private static final int TYPEMASK_TYPE = 0x60;

	/**
	 * {@code bmRequestType} に対してリクエストの対象を示すビットマスク {@value} です。
	 * <p>
	 * @see #getRequestType()
	 */
	private static final int TYPEMASK_RCPT = 0x1F;

	// ======================================================================
	// リクエストタイプ
	// ======================================================================
	/**
	 * リクエストのタイプを表すビットフィールドです。
	 * <p>
	 */
	private final byte bmRequestType;

	// ======================================================================
	// リクエスト
	// ======================================================================
	/**
	 * リクエストの種類です。
	 * <p>
	 */
	private final byte bRequest;

	// ======================================================================
	// 値
	// ======================================================================
	/**
	 * 値です。
	 * <p>
	 */
	private final short wValue;

	// ======================================================================
	// インデックス
	// ======================================================================
	/**
	 * インデックスです。
	 * <p>
	 */
	private final short wIndex;

	// ======================================================================
	// バッファ
	// ======================================================================
	/**
	 * データ送受信用のバッファです。
	 * <p>
	 */
	private final byte[] buffer;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 指定されたバイトバッファの現在の位置からコントロール要求を読み込みます。
	 * <p>
	 * @param buffer バイトバッファ
	 */
	public ControlRequest(ByteBuffer buffer){
		buffer.order(USB.BYTE_ORDER);
		bmRequestType = buffer.get();
		bRequest = buffer.get();
		wValue = buffer.getShort();
		wIndex = buffer.getShort();
		int wLength = buffer.getShort() & 0xFFFF;
		this.buffer = new byte[wLength];
		buffer.get(this.buffer);
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * {@code bmRequestType} のビットフィールドを明示的に示すコンストラクタです。リクエスト
	 * の長さ {@code wLength} はバッファの長さが設定されます。
	 * <p>
	 * @param dir データの転送方向
	 * @param type リクエスト種別
	 * @param recipt リクエスト対象 ({@code RCPT_XXX})
	 * @param bRequest リクエスト
	 * @param wValue 値
	 * @param wIndex インデックス
	 * @param buffer 入出力バッファ
	 */
	public ControlRequest(DIR dir, TYPE type, int recipt, int bRequest, int wValue, int wIndex, byte[] buffer){
		this(getRequestType(dir, type, recipt), bRequest, wValue, wIndex, buffer);
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 指定されたプロパティでインスタンスを構築します。リクエストの長さ {@code wLength} は
	 * バッファの長さが設定されます。
	 * <p>
	 * @param bmRequestType リクエストタイプ
	 * @param bRequest リクエスト種別
	 * @param wValue 値
	 * @param wIndex インデックス
	 * @param buffer データ受送信バッファ
	 */
	public ControlRequest(int bmRequestType, int bRequest, int wValue, int wIndex, byte[] buffer){
		this.bmRequestType = (byte)bmRequestType;
		this.bRequest = (byte)bRequest;
		this.wValue = (short)wValue;
		this.wIndex = (short)wIndex;
		this.buffer = buffer;

		if(buffer.length > 0xFFFF){
			throw new IllegalArgumentException("buffer too large: " + buffer.length);
		}
		return;
	}

	// ======================================================================
	// リクエストタイプの参照
	// ======================================================================
	/**
	 * リクエストのタイプを {@code bmRequestType} 示すビットフィールド値を参照します。返値
	 * は uint8_t の範囲をとります。
	 * <p>
	 * @return リクエストのタイプ
	 */
	public int getRequestType() {
		return bmRequestType & 0xFF;
	}

	// ======================================================================
	// リクエスト方向の参照
	// ======================================================================
	/**
	 * リクエストタイプのビットフラグ {@code bmRequestType}[b8] からリクエスト方向を参照
	 * します。
	 * <p>
	 * @return リクエスト方向
	 */
	public DIR getDirection(){
		return ((getRequestType() & TYPEMASK_DIRECTION) == 0x00)? DIR.OUT: DIR.IN;
	}

	// ======================================================================
	// リクエストタイプの参照
	// ======================================================================
	/**
	 * リクエストタイプのビットフラグ {@code bmRequestType}[b7-b6] からリクエスト種類
	 * を参照します。
	 * <p>
	 * @return リクエスト種類
	 */
	public TYPE getType(){
		switch(getRequestType() & TYPEMASK_TYPE){
		case (0 << 5):	return TYPE.STANDARD;
		case (1 << 5):	return TYPE.CLASS;
		case (2 << 5):	return TYPE.VENDOR;
		case (3 << 5):	return TYPE.RESERVED;
		}
		assert(false);
		return null;
	}

	// ======================================================================
	// リクエスト対象の参照
	// ======================================================================
	/**
	 * リクエストタイプのビットフラグ {@code bmRequestType}[b5-b1] からリクエスト対象
	 * を参照します。
	 * <p>
	 * @return リクエスト対象
	 * @see #RCPT_DEVICE
	 * @see #RCPT_INTERFACE
	 * @see #RCPT_ENDPOINT
	 * @see #RCPT_OTHER
	 */
	public int getRecipient(){
		return getRequestType() & TYPEMASK_RCPT;
	}

	// ======================================================================
	// リクエストの参照
	// ======================================================================
	/**
	 * リクエスト {@code bRequest} の値を参照します。返値は uint8_t の範囲をとります。
	 * <p>
	 * @return リクエスト
	 */
	public int getRequest() {
		return bRequest & 0xFF;
	}

	// ======================================================================
	// 値の参照
	// ======================================================================
	/**
	 * 値 {@code wValue} の値を参照します。返値は uint16_t の範囲をとります。
	 * <p>
	 * @return リクエストの値
	 */
	public int getValue() {
		return wValue & 0xFFFF;
	}

	// ======================================================================
	// インデックスの参照
	// ======================================================================
	/**
	 * インデックス {@code wIndex} の値を参照します。これは文字列記述子を要求する場合のイン
	 * デックス指定などに使用します。
	 * 返値は uint16_t の範囲をとります。
	 * <p>
	 * @return 対象のインデックス
	 */
	public int getIndex() {
		return wIndex & 0xFFFF;
	}

	// ======================================================================
	// 転送長の参照
	// ======================================================================
	/**
	 * リクエストの転送長 {@code wLength} の値を参照します。
	 * 返値は uint16_t の範囲をとります。
	 * <p>
	 * @return 転送長
	 */
	public int getLength() {
		return buffer.length;
	}

	// ======================================================================
	// データ用バッファの参照
	// ======================================================================
	/**
	 * このインスタンスが保持するデータ用のバッファを参照します。返値のバイト配列に対して行った
	 * 変更は他の処理からも参照されます。
	 * <p>
	 * @return データバッファ
	 */
	public byte[] getRawBuffer(){
		return buffer;
	}

	// ======================================================================
	// インスタンスの文字列化
	// ======================================================================
	/**
	 * このインスタンスを文字列化します。
	 * <p>
	 * @return インスタンスの文字列
	*/
	@Override
	public String toString() {
		return String.format(
			"bmRequestType=0x%02X(%s,%s,0x%02X),bRequest=0x%02X,wValue=0x%04X,wLength=%d",
			getRequestType(), getDirection(), getType(), getRecipient(),
			getRequest(), getValue(), getLength());
	}

	// ======================================================================
	// ビットフィールド構築
	// ======================================================================
	/**
	 * リクエストタイプのビットフィールドを構築します。
	 * <p>
	 * @param dir データの転送方向
	 * @param type リクエスト種別
	 * @param recipt リクエスト対象
	 * @return リクエストタイプ
	 */
	private static int getRequestType(DIR dir, TYPE type, int recipt){

		// リクエストタイプのビットフィールドを構築
		int bmRequestType = 0;
		switch(dir){
		case OUT:	bmRequestType |= (0 << 7);	break;
		case IN:	bmRequestType |= (1 << 7);	break;
		}
		switch(type){
		case STANDARD:	bmRequestType |= (0 << 5);	break;
		case CLASS:		bmRequestType |= (1 << 5);	break;
		case VENDOR:	bmRequestType |= (2 << 5);	break;
		case RESERVED:	bmRequestType |= (3 << 5);	break;
		}
		bmRequestType |= (recipt & TYPEMASK_RCPT);
		return bmRequestType;
	}

}
