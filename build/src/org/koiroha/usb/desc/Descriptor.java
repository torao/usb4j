/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: Descriptor.java,v 1.9 2009/05/18 11:02:23 torao Exp $
*/
package org.koiroha.usb.desc;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.koiroha.usb.USB;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Descriptor: USB 記述子クラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB の記述子 (ディスクリプタ) を表すクラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.9 $ $Date: 2009/05/18 11:02:23 $
 * @author torao
 * @since 2009/04/27 Java2 SE 5.0
 */
public abstract class Descriptor implements Serializable {

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * デバイス記述子タイプを表す {@code bDescriptorType} 定数値 {@value} です。
	 * <p>
	 * @see #getDescriptorType()
	 */
	public static final byte TYPE_DEVICE = 0x01;

	/**
	 * コンフィギュレーション記述子タイプを表す {@code bDescriptorType} 定数値 {@value} です。
	 * <p>
	 * @see #getDescriptorType()
	 */
	public static final byte TYPE_CONFIGURATION = 0x02;

	/**
	 * 文字列記述子タイプを表す {@code bDescriptorType} 定数値 {@value} です。
	 * <p>
	 * @see #getDescriptorType()
	 */
	public static final byte TYPE_STRING = 0x03;

	/**
	 * インターフェース記述子タイプを表す {@code bDescriptorType} 定数値 {@value} です。
	 * <p>
	 * @see #getDescriptorType()
	 */
	public static final byte TYPE_INTERFACE = 0x04;

	/**
	 * エンドポイント記述子タイプを表す {@code bDescriptorType} 定数値 {@value} です。
	 * <p>
	 * @see #getDescriptorType()
	 */
	public static final byte TYPE_ENDPOINT = 0x05;

	/**
	 * デバイス修飾記述子タイプを表す {@code bDescriptorType} 定数値 {@value} です。
	 * <p>
	 * @see #getDescriptorType()
	 */
	public static final byte TYPE_DEVICE_QUALIFIER = 0x06;

	/**
	 * OTHER_SPEED_CONFIG記述子タイプを表す {@code bDescriptorType} 定数値 {@value} です。
	 * <p>
	 * @see #getDescriptorType()
	 */
	public static final byte TYPE_OTHER_SPEED_CONFIG = 0x07;

	/**
	 * インターフェース電力記述子タイプを表す {@code bDescriptorType} 定数値 {@value} です。
	 * <p>
	 * @see #getDescriptorType()
	 */
	public static final byte TYPE_INTERFACE_POWER = 0x08;

	/**
	 * OTG 記述子タイプを表す {@code bDescriptorType} 定数値 {@value} です。
	 * <p>
	 * @see #getDescriptorType()
	 */
	public static final byte TYPE_OTG = 0x09;

	/**
	 * デバッグ記述子タイプを表す {@code bDescriptorType} 定数値 {@value} です。
	 * <p>
	 * @see #getDescriptorType()
	 */
	public static final byte TYPE_DEBUG = 0x0A;

	/**
	 * インターフェース割り当て記述子タイプを表す {@code bDescriptorType} 定数値 {@value} です。
	 * <p>
	 * @see #getDescriptorType()
	 */
	public static final byte TYPE_INTERFACE_ASSOCIATION = 0x0B;

	/**
	 * クラス仕様 (Class Specific) 記述子 CS_INTERFACE を表す {@code bDescriptorType}
	 * 定数値 {@value} です。
	 * <p>
	 * @see #getDescriptorType()
	 * @see FunctionalDescriptor
	 */
	public static final byte TYPE_CS_INTERFACE = 0x24;

	/**
	 * クラス仕様 (Class Specific) 記述子 CS_ENCPOINT を表す {@code bDescriptorType}
	 * 定数値 {@value} です。
	 * <p>
	 * @see #getDescriptorType()
	 * @see FunctionalDescriptor
	 */
	public static final byte TYPE_CS_ENDPOINT = 0x25;

	// ======================================================================
	// 長さ
	// ======================================================================
	/**
	 * このディスクリプタの長さです。
	 * <p>
	 */
	private final byte bLength;

	// ======================================================================
	// ディスクリプタ識別子
	// ======================================================================
	/**
	 * このディスクリプタの識別子です。
	 * <p>
	 */
	private final byte bDescriptor;

	// ======================================================================
	// バイナリデータ
	// ======================================================================
	/**
	 * この記述子全体を表すバイト配列です。
	 * <p>
	 */
	private final byte[] binary;

	// ======================================================================
	// 読み出し位置
	// ======================================================================
	/**
	 * コンストラクタに渡されたバイトバッファの読み出し開始位置です。
	 * <p>
	 */
	private final int bufferPosition;

	// ======================================================================
	// エクストラフィールド
	// ======================================================================
	/**
	 * ベンダー固有のフィールドです。
	 * <p>
	 */
	private byte[] extra = null;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 指定されたディスクリプタのバイナリからインスタンスを構築します。
	 * <p>
	 * @param buffer ディスクリプタのバイナリ
	 */
	protected Descriptor(ByteBuffer buffer) {
		buffer.order(USB.BYTE_ORDER);
		bufferPosition = buffer.position();

		// 記述子の長さを取得
		this.bLength = buffer.get();
		if((this.bLength & 0xFF) < 2){
			throw new IllegalArgumentException("invalid descriptor length: " + bLength);
		}

		// 共通ヘッダを取得
		this.bDescriptor = buffer.get();

		// 記述子全体のバイナリを取得
		buffer.position(bufferPosition);
		int length = Math.min(bLength, buffer.remaining() + 2);
		binary = new byte[length];
		buffer.get(binary);
		buffer.position(bufferPosition + 2);

		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 指定されたバイトバッファをこの記述子の大きさ分を読み飛ばした位置に移動します。このメソッド
	 * はサブクラスのコンストラクタの一番最後に呼び出します。
	 * <p>
	 * このメソッドは final 宣言されたクラスからのみ呼び出すことが出来ます。
	 * <p>
	 * @param buffer 記述子のバイナリ
	 */
	protected void pack(ByteBuffer buffer){
		assert(Modifier.isFinal(getClass().getModifiers()));
		int pos = buffer.position();
		int end = Math.min(bufferPosition + getLength(), buffer.limit());
		extra = new byte[Math.max(end - pos, 0)];
		if(pos <= end){
			buffer.get(extra);
		} else {
			// ※toString() で extra が null の例外を発生させないよう注意
			throw new IllegalStateException("buffer overrun reading: end=" + end + ",pos=" + pos + "; " + this);
		}
		return;
	}

	// ======================================================================
	// 長さの参照
	// ======================================================================
	/**
	 * このディスクリプタの長さ {@code bLength} を参照します。返値は UINT8 の範囲となります。
	 * <p>
	 * @return ディスクリプタの長さ
	 */
	public int getLength() {
		return bLength & 0xFF;
	}

	// ======================================================================
	// タイプの参照
	// ======================================================================
	/**
	 * このディスクリプタのタイプ {@code bDescriptor} を参照します。返値は UINT8 の範囲と
	 * なります。
	 * <p>
	 * @return ディスクリプタのタイプ
	 */
	public int getDescriptorType() {
		return bDescriptor & 0xFF;
	}

	// ======================================================================
	// 追加データの参照
	// ======================================================================
	/**
	 * この記述子の既知のサイズを超えて付加されていたデータを参照します。追加データを持たない
	 * 場合は null を返します。
	 * <p>
	 * 返値はバイトオーダーが {@link USB#BYTE_ORDER} に設定された読み出し専用のバイト
	 * バッファです。
	 * <p>
	 * @return この記述子のバイトバッファ
	 */
	public ByteBuffer getExtraBinary(){
		if(extra.length == 0){
			return null;
		}
		ByteBuffer buffer = ByteBuffer.wrap(extra);
		buffer = buffer.asReadOnlyBuffer();
		buffer.order(USB.BYTE_ORDER);
		return buffer;
	}

	// ======================================================================
	// バイナリデータの参照
	// ======================================================================
	/**
	 * この記述子を表すバイトバッファを参照します。デバイスが記述子ごとの一般的な長さを超えた追加
	 * データを使用する場合、このメソッドを用いてデバイス依存の追加データを参照することが出来ます。
	 * <p>
	 * 返値はバイトオーダーが {@link USB#BYTE_ORDER} に設定された読み出し専用のバイト
	 * バッファです。
	 * <p>
	 * @return この記述子のバイトバッファ
	 */
	public ByteBuffer getRawBinary(){
		ByteBuffer buffer = ByteBuffer.wrap(binary);
		buffer = buffer.asReadOnlyBuffer();
		buffer.order(USB.BYTE_ORDER);
		return buffer;
	}

	// ======================================================================
	// ハッシュ値の参照
	// ======================================================================
	/**
	 * このインスタンスのハッシュ値を参照します。
	 * <p>
	 * @return インスタンスのハッシュ値
	 */
	@Override
	public int hashCode(){
		return Arrays.hashCode(binary);
	}

	// ======================================================================
	// 等価判定
	// ======================================================================
	/**
	 * 指定されたインスタンスとこのインスタンスが等しいかどうかを判定します。
	 * <p>
	 * @param o 比較するオブジェクト
	 * @return 等しい場合 true
	 */
	@Override
	public boolean equals(Object o){
		if(! (o instanceof Descriptor)){
			return false;
		}
		return Arrays.equals(this.binary, ((Descriptor)o).binary);
	}

	// ======================================================================
	// インスタンスの文字列化
	// ======================================================================
	/**
	 * このインスタンスを文字列化します。
	 * <p>
	 */
	@Override
	public String toString(){
		return String.format("bLength=%d,bDescriptor=0x%02X", getLength(), getDescriptorType());
	}

	// ======================================================================
	// 追加データの文字列化
	// ======================================================================
	/**
	 * この記述子の追加データを文字列化して返します。追加データが存在しない場合は空の文字列を返し
	 * ます。存在する場合はコンマに続いて 16 進数の文字列を返します。このメソッドはサブクラスの
	 * {@link #toString()} で使用します。
	 * <p>
	 * @return 追加データの文字列
	 */
	protected String toHexExtra(){
		StringBuilder buffer = new StringBuilder();
		if(extra.length > 0){
			buffer.append(",extra=");
			for(int i=0; i<extra.length; i++){
				int ch = extra[i] & 0xFF;
				buffer.append(Character.toUpperCase(Character.forDigit((ch >> 4) & 0x0F, 16)));
				buffer.append(Character.toUpperCase(Character.forDigit((ch >> 0) & 0x0F, 16)));
			}
		}
		return buffer.toString();
	}

}
