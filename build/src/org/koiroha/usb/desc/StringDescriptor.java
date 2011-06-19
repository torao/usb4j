/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: StringDescriptor.java,v 1.7 2009/05/18 11:02:23 torao Exp $
*/
package org.koiroha.usb.desc;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// StringDescriptor: 文字列記述子
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 文字列記述子のバイナリから文字列を参照するためのクラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.7 $ $Date: 2009/05/18 11:02:23 $
 * @author takami torao
 * @since 2009/05/12 Java2 SE 5.0
 */
public final class StringDescriptor extends Descriptor {

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// 文字列
	// ======================================================================
	/**
	 * この記述子が表す文字列です。
	 * <p>
	 */
	private final String bString;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 指定されたバイトバッファから文字列記述子のインスタンスを構築します。
	 * <p>
	 * @param buffer 文字列記述子のバイトバッファ
	 */
	public StringDescriptor(ByteBuffer buffer) {
		super(buffer);
		byte[] binary = new byte[getLength() - 2];
		buffer.get(binary);
		try	{
			this.bString = new String(binary, "UnicodeLittle");
		} catch(UnsupportedEncodingException ex){
			throw new IllegalStateException(ex);
		}
		pack(buffer);
		return;
	}

	// ======================================================================
	// 文字列の参照
	// ======================================================================
	/**
	 * この記述子が表す文字列 {@code bString} を参照します。
	 * <p>
	 * @return 文字列
	 */
	public String getString(){
		return bString;
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
		return super.toString() + ",bString=" + getString() + toHexExtra();
	}

}
