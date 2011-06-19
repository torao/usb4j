/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: LangIDDescriptor.java,v 1.2 2009/05/16 05:24:13 torao Exp $
*/
package org.koiroha.usb.desc;

import java.nio.ByteBuffer;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// LANGIDDescriptor: 言語 ID 記述子
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 言語 ID 記述子を表すクラスです。これはデバイスに対してインデックスに 0 を指定した文字列記述子
 * 要求で取得できる情報です。デバイスのデフォルトの言語 ID を参照することが出来ます。
 * <p>
 * @version usb4j 1.0 $Revision: 1.2 $ $Date: 2009/05/16 05:24:13 $
 * @author takami torao
 * @since 2009/05/12 Java2 SE 5.0
 */
public final class LangIDDescriptor extends Descriptor {

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// 言語 ID
	// ======================================================================
	/**
	 * 言語 ID の配列です。
	 * <p>
	 */
	private final short[] wLANGID;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 指定されたバイトバッファから
	 * <p>
	 * @param buffer バイトバッファ
	 */
	public LangIDDescriptor(ByteBuffer buffer) {
		super(buffer);
		wLANGID = new short[buffer.remaining() / 2];
		for(int i=0; buffer.remaining() >= 2; i++){
			wLANGID[i] = buffer.getShort();
		}
		pack(buffer);
		return;
	}

	// ======================================================================
	// 言語 ID の参照
	// ======================================================================
	/**
	 * この記述子が示す言語 ID の配列を参照します。返値の配列を変更してもこのインスタンスに影響
	 * はありません。
	 * <p>
	 * @return 言語 ID の配列
	 */
	public short[] getLangID(){
		return wLANGID.clone();
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
	public String toString(){
		StringBuilder buffer = new StringBuilder();
		for(int i=0; i<wLANGID.length; i++){
			if(i != 0){
				buffer.append(':');
			}
			buffer.append(String.format("0x%04X", wLANGID[i] & 0xFFFF));
		}
		return super.toString() + ",[" + buffer + "]" + toHexExtra();
	}

}
