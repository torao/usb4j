/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: USB.java,v 1.4 2009/05/14 02:22:35 torao Exp $
*/
package org.koiroha.usb;

import java.io.Serializable;
import java.lang.reflect.*;
import java.nio.ByteOrder;
import java.util.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// USB: USB クラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB 仕様で定義されている定数とユーティリティ機能を実装するクラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.4 $ $Date: 2009/05/14 02:22:35 $
 * @author takami torao
 * @since 2009/05/11 Java2 SE 5.0
 */
public final class USB {

	// ======================================================================
	// USB バイトオーダー
	// ======================================================================
	/**
	 * USB の標準バイトオーダー {@code ByteOrder.LITTLE_ENDIAN} を表す定数です。
	 * <p>
	 */
	public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

	// ======================================================================
	// USB デバイス/インターフェースクラス
	// ======================================================================

	/** インターフェース記述子に詳細が指定されていることを示すクラス値です。 */
	public static final USB.Class DEV_COMPOSITE = new USB.Class(0);

	/** */
	public static final USB.Class DEV_COMMUNICATION = new USB.Class(2);

	/** USB ハブを示すクラス値です */
	public static final USB.Class DEV_HUB = new USB.Class(9);

	/** */
	public static final USB.Class DEV_DATA = new USB.Class(10);

	/** */
	public static final USB.Class DEV_DIAGNOSTIC = new USB.Class(220);

	/** Bluetooth のようなワイヤレスコントローラを表すクラス値です */
	public static final USB.Class DEV_WIRELESS_CONTROLLER = new USB.Class(224);

	/** */
	public static final USB.Class DEV_MISCELLANEOUS = new USB.Class(239);

	/** アプリケーション固有を表すクラス値です */
	public static final USB.Class DEV_APPLICATION_SPECIFIC = new USB.Class(254);

	/** ベンダー固有を表すクラス値です */
	public static final USB.Class DEV_VENDOR_SPECIFIC = new USB.Class(255);

	/** スピーカーなどのオーディオ機器を表すクラス値です。 */
	public static final USB.Class IFC_AUDIO = new USB.Class(1);

	/** */
	public static final USB.Class IFC_COMMUNICATION_CONTROL = new USB.Class(2);

	/** */
	public static final USB.Class IFC_COMMUNICATION_DATA = new USB.Class(10);

	/** マウスやキーボードなどのヒューマンデバイスインターフェースを示すクラス値です */
	public static final USB.Class IFC_HID = new USB.Class(3);

	/** */
	public static final USB.Class IFC_PHYSICAL = new USB.Class(5);

	/** */
	public static final USB.Class IFC_IMAGE = new USB.Class(6);

	/** プリンタなどの印刷機器を示すクラス値です */
	public static final USB.Class IFC_PRINTING = new USB.Class(7);

	/** ハードディスクや CD-ROM などのストレージを示すクラス値です */
	public static final USB.Class IFC_MASS_STRAGE = new USB.Class(8);

	/** */
	public static final USB.Class IFC_CHIP_SMART_CARD = new USB.Class(11);

	/** */
	public static final USB.Class IFC_CONTENT_SECURITY = new USB.Class(12);

	/** 映像デバイスを表すクラス値です。 */
	public static final USB.Class IFC_VIDEO = new USB.Class(14);

	/** */
	public static final USB.Class IFC_DIAGNOSTIC_DEVICE = new USB.Class(220);

	/** Bluetooth のようなワイヤレスコントローラを表すクラス値です */
	public static final USB.Class IFC_WIRELESS_CONTROLLER = new USB.Class(224);

	/** アプリケーション固有を表すクラス値です */
	public static final USB.Class IFC_APPLICATION_SPECIFIC = new USB.Class(254);

	/** ベンダー固有を表すクラス値です */
	public static final USB.Class IFC_VENDOR_SPECIFIC = new USB.Class(255);

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタはクラス内に隠蔽されています。
	 * <p>
	 */
	private USB() {
		return;
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Class: クラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * デバイスクラス及びインターフェースクラスを表すクラスです。
	 * <p>
	 */
	public static class Class implements Serializable{

		// ==================================================================
		// シリアルバージョン
		// ==================================================================
		/**
		 * このクラスのシリアルバージョンです。
		 * <p>
		 */
		private static final long serialVersionUID = 1L;

		// ==================================================================
		// クラス
		// ==================================================================
		/**
		 * このインスタンスが表すクラスの数値です。
		 * <p>
		 */
		private final byte value;

		// ==================================================================
		// 名前
		// ==================================================================
		/**
		 * このインスタンスが表す名前です。
		 * <p>
		 */
		private String name = null;

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * このインスタンスのクラス値を指定して構築を行います。
		 * <p>
		 * @param value このインスタンスが示すクラス値
		 */
		private Class(int value){
			assert(value >= 0 && value <= 0xFF);
			this.value = (byte)value;
			this.name = "0x" + Integer.toHexString(value & 0xFF);
			return;
		}

		// ==================================================================
		// クラス値の参照
		// ==================================================================
		/**
		 * このインスタンスのクラス値を参照します。値は 0 から 255 までの範囲を取ります。
		 * <p>
		 * @return クラス値
		 */
		public int getValue(){
			return value & 0xFF;
		}

		// ==================================================================
		// ハッシュ値の参照
		// ==================================================================
		/**
		 * このデバイスクラスのハッシュ値を参照します。
		 * <p>
		 * @return ハッシュ値
		 */
		@Override
		public int hashCode(){
			return getValue();
		}

		// ==================================================================
		// 等価性の比較
		// ==================================================================
		/**
		 * 指定されたインスタンスとこのインスタンスが等しいかどうかを判定します。
		 * <p>
		 * @param obj 比較するオブジェクト
		 * @return 等しい場合 true
		 */
		@Override
		public boolean equals(Object obj){
			if(! (obj instanceof USB.Class)){
				return false;
			}
			USB.Class other = (USB.Class)obj;
			return (other.getValue() == this.getValue());
		}

		// ==================================================================
		// インスタンスの文字列化
		// ==================================================================
		/**
		 * このインスタンスを文字列化します。
		 * <p>
		 * @return インスタンスの文字列
		 */
		@Override
		public String toString(){
			return name;
		}
	}

	// ==================================================================
	// デバイスクラスマップ
	// ==================================================================
	/**
	 * デバイスクラスの定義マップです。
	 * <p>
	 */
	private static final Map<Byte,USB.Class> DEVICE = new HashMap<Byte,USB.Class>();

	// ==================================================================
	// インターフェースクラスマップ
	// ==================================================================
	/**
	 * インターフェースクラスの定義マップです。
	 * <p>
	 */
	private static final Map<Byte,USB.Class> INTERFACE = new HashMap<Byte,USB.Class>();

	// ==================================================================
	// スタティックイニシャライザ
	// ==================================================================
	/**
	 * 名前のマップを構築します。
	 * <p>
	 */
	static {

		// 定義済みの定数値をマップに設定
		try{
			Field[] fields = USB.class.getFields();
			for(int i=0; i<fields.length; i++){
				int mod = fields[i].getModifiers();
				if(! Modifier.isPublic(mod) || ! Modifier.isStatic(mod) || ! Modifier.isFinal(mod) || ! fields[i].getType().equals(USB.Class.class)){
					continue;
				}

				String name = fields[i].getName();
				USB.Class cls = (USB.Class)fields[i].get(null);

				// 名前の設定
				if(name.startsWith("DEV_")){
					name = name.substring("DEV_".length());
					cls.name = name;
					DEVICE.put(Byte.valueOf(cls.value), cls);
				} else if(name.startsWith("IFC_")){
					name = name.substring("IFC_".length());
					cls.name = name;
					INTERFACE.put(Byte.valueOf(cls.value), cls);
				} else {
					assert(false);
				}
			}
		} catch(Exception ex){
			throw new IllegalStateException(ex);
		}
	}

	// ==================================================================
	// インスタンスの参照
	// ==================================================================
	/**
	 * 指定された {@code bDeviceClass} 値に対するデバイスクラスのインスタンスを参照し
	 * ます。このメソッドが null を返すことはありません。
	 * <p>
	 * @param value クラス値
	 * @return デバイスクラス
	 */
	public static final USB.Class getDeviceClass(int value){
		USB.Class cls = DEVICE.get(Byte.valueOf((byte)value));
		if(cls != null){
			return cls;
		}
		return new USB.Class(value & 0xFF);
	}

	// ==================================================================
	// インスタンスの参照
	// ==================================================================
	/**
	 * 指定された {@code bInterfaceClass} 値に対するインターフェースクラスのインスタ
	 * ンスを参照します。このメソッドが null を返すことはありません。
	 * <p>
	 * @param value クラス値
	 * @return デバイスクラス
	 */
	public static final USB.Class getInterfaceClass(byte value){
		USB.Class cls = INTERFACE.get(Byte.valueOf((byte)value));
		if(cls != null){
			return cls;
		}
		return new USB.Class(value & 0xFF);
	}
}
