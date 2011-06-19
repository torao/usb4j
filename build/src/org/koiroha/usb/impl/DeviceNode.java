/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: DeviceNode.java,v 1.5 2009/05/21 12:02:53 torao Exp $
*/
package org.koiroha.usb.impl;

import java.util.*;

import org.koiroha.usb.USBException;
import org.koiroha.usb.desc.Descriptor;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// DeviceNode: デバイスノード
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * デバイス, コンフィギュレーション, インターフェース, 代替設定, エンドポイントの共通機能を実装
 * するクラスです。主にツリー構造に関する実装を行っています。
 * <p>
 * @version $Revision: 1.5 $ $Date: 2009/05/21 12:02:53 $
 * @author takami torao
 * @since usb4j 1.0 (Java2 SE 5.0) 2009/05/16
 * @param <PARENT> 親ノードの型
 * @param <CHILD> 子ノードの型
 * @param <DESC> 記述子の型
 */
public abstract class DeviceNode<PARENT extends DeviceNode<?,?,?>, CHILD extends DeviceNode<?,?,?>, DESC extends Descriptor> {

	// ======================================================================
	// 0 バイトバッファ
	// ======================================================================
	/**
	 * 0 バイトのバッファを表す定数です。
	 * <p>
	 */
	protected static final byte[] EMPTY_BUFFER = new byte[0];

	// ======================================================================
	// 親ノード
	// ======================================================================
	/**
	 * 親のノードです。
	 * <p>
	 */
	private final PARENT parent;

	// ======================================================================
	// 子ノード
	// ======================================================================
	/**
	 * 子のノードリストです。
	 * <p>
	 */
	private List<CHILD> child = null;

	// ======================================================================
	// 記述子
	// ======================================================================
	/**
	 * このノードの記述子です。
	 * <p>
	 */
	private final DESC desc;

	// ======================================================================
	// 追加の記述子
	// ======================================================================
	/**
	 * クラス仕様/ベンダー仕様としてこのノードに付属する記述子です。
	 * <p>
	 */
	private List<Descriptor> additional = null;

	// ======================================================================
	// デバイス実装
	// ======================================================================
	/**
	 * このノードの所有者であるデバイス実装です。
	 * <p>
	 */
	protected final DeviceImpl device;

	// ======================================================================
	// ブリッジ
	// ======================================================================
	/**
	 * ブリッジです。
	 * <p>
	 */
	protected final USBBridge bridge;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 親ノードを指定して構築を行います。親ノードに null を指定出来るのはデバイス実装のみです。
	 * <p>
	 * @param parent 親ノード
	 * @param desc このノードの記述子
	 */
	protected DeviceNode(PARENT parent, DESC desc) {
		assert(parent != null);
		this.parent = parent;
		this.desc = desc;
		this.device = parent.device;
		this.bridge = device.bridge;
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * USB ブリッジを指定して構築を行います。このコンストラクタを使用できるのはデバイス実装のみ
	 * です。
	 * <p>
	 * @param bridge ブリッジインターフェース
	 * @param desc このノードの記述子
	 */
	protected DeviceNode(USBBridge bridge, DESC desc) {
		assert(DeviceImpl.class.isInstance(this)): this.getClass();
		this.parent = null;
		this.desc = desc;
		this.device = (DeviceImpl)this;
		this.bridge = bridge;
		return;
	}

	// ======================================================================
	// 親ノードの参照
	// ======================================================================
	/**
	 * 親ノードを参照します。
	 * <p>
	 * @return 親ノード
	 */
	protected PARENT getParentNode(){
		return parent;
	}

	// ======================================================================
	// 子ノードの参照
	// ======================================================================
	/**
	 * 子ノードの一覧を参照します。返値のリストは変更できません。
	 * <p>
	 * @return 子ノードのリスト
	 */
	protected List<CHILD> getChildNode(){
		if(child == null){
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(child);
	}

	// ======================================================================
	// 子ノードの追加
	// ======================================================================
	/**
	 * 子ノードを追加します。
	 * <p>
	 * @param node 子ノード
	 */
	protected void addChildNode(CHILD node){
		if(child == null){
			child = new ArrayList<CHILD>();
		}
		child.add(node);
		return;
	}

	// ======================================================================
	// 記述子の参照
	// ======================================================================
	/**
	 * このノードの記述子を参照します。
	 * <p>
	 * @return 記述子
	 */
	public DESC getDescriptor(){
		return desc;
	}

	// ======================================================================
	// 追加記述子の参照
	// ======================================================================
	/**
	 * クラス仕様/ベンダー仕様としてこのエンドポイントに付加されている記述子を参照します。
	 * <p>
	 * @return 追加の記述子
	 */
	public List<Descriptor> getAdditionalDescriptor(){
		if(additional == null){
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(additional);
	}

	// ======================================================================
	// 追加記述子の追加
	// ======================================================================
	/**
	 * クラス仕様/ベンダー仕様の記述子このエンドポイントに追加します。
	 * <p>
	 * @param desc 追加の記述子
	 */
	protected void addAdditionalDescriptor(Descriptor desc){
		if(additional == null){
			additional = new ArrayList<Descriptor>();
		}
		additional.add(desc);
		return;
	}

	// ======================================================================
	// デバイスの解放確認
	// ======================================================================
	/**
	 * このノードの所有者であるデバイス実装が解放されているかどうかを確認します。
	 * <p>
	 * @return 解放されている場合 true
	 */
	protected boolean isReleased(){
		return device.isReleased();
	}

	// ======================================================================
	// デバイスの有効性確認
	// ======================================================================
	/**
	 * このノードの所有者であるデバイス実装が解放されていないことを保証します。
	 * <p>
	 * @throws USBException デバイスが解放されている場合
	 */
	protected void ensureUnreleased() throws USBException{
		if(isReleased()){
			throw new USBException("device already released");
		}
		return;
	}

	// ======================================================================
	// デバイスオープンの確認
	// ======================================================================
	/**
	 * デバイスがオープンされていることを保証します。
	 * <p>
	 * @throws USBException デバイスがオープンされていない場合
	 */
	protected void ensureDeviceOpened() throws USBException{
		device.getOpenedHandle();
		return;
	}

	// ======================================================================
	// インターフェースの要求済み確認
	// ======================================================================
	/**
	 * このノードの所属するインターフェースが要求済みであることを保証します。インターフェースクラ
	 * スにオーバーライドされます。コンフィギュレーション以上のノードがこのメソッドを使用すると
	 * NullPointerException が発生します。
	 * <p>
	 * @throws USBException インターフェースが要求されていない場合
	 */
	protected void ensureInterfaceClaimed() throws USBException{
		ensureDeviceOpened();
		if(this instanceof InterfaceImpl){
			if(! ((InterfaceImpl)this).isClaimed()){
				throw new USBException("interface is not claimed");
			}
		}
		parent.ensureInterfaceClaimed();
		return;
	}

	// ======================================================================
	// インターフェースの代替設定確認
	// ======================================================================
	/**
	 * インターフェースの代替設定が指定された値であることを保証します。
	 * <p>
	 * @param alt 確認する代替設定
	 * @throws USBException 代替設定が異なる場合
	 */
	protected void ensureAltSetting(int alt) throws USBException{
		ensureInterfaceClaimed();
		if(this instanceof InterfaceImpl){
			if(((InterfaceImpl)this).getActiveAltSetting() == (alt & 0xFF)){
				throw new USBException("invalid alternate setting: " + alt);
			}
		}
		parent.ensureInterfaceClaimed();
		return;
	}

	// ======================================================================
	// 値の有効範囲確認
	// ======================================================================
	/**
	 * 指定された値が uint8_t の範囲であることを確認します。
	 * <p>
	 * @param name 値に対する名前
	 * @param value 確認する値
	 * @return uint8_t の値
	 * @throws USBException 値が uint8_t の範囲外の場合
	 */
	protected static byte toUINT8(String name, int value) throws USBException{
		if((value & 0xFF) != value){
			throw new USBException(name + " out of range 0-" + 0xFF + ": " + value);
		}
		return (byte)value;
	}

	// ======================================================================
	// 値の有効範囲確認
	// ======================================================================
	/**
	 * 指定された値が uint16_t の範囲であることを確認します。
	 * <p>
	 * @param name 値に対する名前
	 * @param value 確認する値
	 * @return uint16_t の値
	 * @throws USBException 値が uint16_t の範囲外の場合
	 */
	protected static short toUINT16(String name, int value) throws USBException{
		if((value & 0xFFFF) != value){
			throw new USBException(name + " out of range 0-" + 0xFFFF + ": " + value);
		}
		return (short)value;
	}

}
