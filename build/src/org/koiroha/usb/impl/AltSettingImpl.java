/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: AltSettingImpl.java,v 1.4 2009/05/21 12:02:53 torao Exp $
*/
package org.koiroha.usb.impl;

import java.util.*;

import org.koiroha.usb.*;
import org.koiroha.usb.desc.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// AltSettingImpl: 代替設定実装
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB インターフェースの代替設定を表す実装クラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.4 $ $Date: 2009/05/21 12:02:53 $
 * @author torao
 * @since 2009/04/22 Java2 SE 5.0
 */
public class AltSettingImpl extends DeviceNode<InterfaceImpl, EndpointImpl, InterfaceDescriptor> implements AltSetting{

	// ======================================================================
	// インターフェース番号
	// ======================================================================
	/**
	 * インターフェース番号です。
	 * <p>
	 */
	private final byte intf;

	// ======================================================================
	// 代替設定
	// ======================================================================
	/**
	 * この代替設定の値です。
	 * <p>
	 */
	private final int alt;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param intf インターフェース
	 * @param desc インターフェース記述子
	 */
	protected AltSettingImpl(InterfaceImpl intf, InterfaceDescriptor desc) {
		super(intf, desc);
		this.intf = (byte)desc.getInterfaceNumber();
		this.alt = desc.getAlternateSetting();
		return;
	}

	// ======================================================================
	// インターフェースの参照
	// ======================================================================
	/**
	 * この代替設定のインターフェースを参照します。
	 * <p>
	 * @return インターフェース
	 */
	public Interface getInterface(){
		return super.getParentNode();
	}

	// ======================================================================
	// 代替設定の参照
	// ======================================================================
	/**
	 * この代替設定の {@code bAlternateSetting} 値を返します。
	 * <p>
	 * @return 代替設定
	 */
	public int getAltSetting(){
		return alt;
	}

	// ======================================================================
	// エンドポイントの参照
	// ======================================================================
	/**
	 * この代替設定のエンドポイントを参照します。
	 * <p>
	 * @return エンドポイント
	*/
	public List<Endpoint> getEndpoints(){
		return new ArrayList<Endpoint>(super.getChildNode());
	}
	// ======================================================================
	// コントロール転送の実行
	// ======================================================================
	/**
	 * この代替設定でコントロール転送を実行します。エンドポイント 0 に対する簡易メソッドです。
	 * <p>
	 * @param request デバイスリクエスト
	 * @param timeout タイムアウト (ミリ秒)
	 * @return 実際の入出力サイズ
	 * @throws USBException 言語 ID の取得に失敗した場合
	 */
	public int controlTransfer(ControlRequest request, int timeout) throws USBException{
		return controlTransfer(request, 0, timeout);
	}

	// ======================================================================
	// コントロール転送の実行
	// ======================================================================
	/**
	 * この代替設定の指定されたエンドポイントでコントロール転送を実行します。
	 * このメソッドの実行にはインターフェースが要求されている必要があります。
	 * <p>
	 * @param request デバイスリクエスト
	 * @param edpt エンドポイント番号
	 * @param timeout タイムアウト (ミリ秒)
	 * @return 実際の入出力サイズ
	 * @throws USBException 言語 ID の取得に失敗した場合
	 */
	public int controlTransfer(ControlRequest request, int edpt, int timeout) throws USBException{
		ensureAltSetting(alt);
		return bridge.controlTransfer(device, intf, toUINT8("edpt", edpt), request, timeout);
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
		return getDescriptor().toString();
	}

	// ======================================================================
	// エンドポイント実装の構築
	// ======================================================================
	/**
	 * 指定された記述子を持つエンドポイント実装を構築します。このメソッドはサブクラスでオーバー
	 * ライドし実装クラスを変更できるように公開されています。
	 * <p>
	 * @param desc エンドポイント記述子
	 * @return エンドポイント実装
	 */
	protected EndpointImpl getEndpointImpl(EndpointDescriptor desc){
		return new EndpointImpl(this, desc);
	}

}
