/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: ConfigurationImpl.java,v 1.7 2009/05/16 05:24:13 torao Exp $
*/
package org.koiroha.usb.impl;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;

import org.koiroha.usb.*;
import org.koiroha.usb.desc.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// ConfigurationImpl: コンフィギュレーション実装
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * コンフィギュレーションの実装クラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.7 $ $Date: 2009/05/16 05:24:13 $
 * @author torao
 * @since 2009/04/22 Java2 SE 5.0
 */
public class ConfigurationImpl extends DeviceNode<DeviceImpl, InterfaceImpl, ConfigurationDescriptor> implements Configuration{

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ConfigurationImpl.class.getName());

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * インスタンスは JNI 側で生成されます。
	 * <p>
	 * @param device デバイス
	 * @param buffer コンフィギュレーション記述子のバイナリ
	 */
	protected ConfigurationImpl(DeviceImpl device, ByteBuffer buffer) {
		super(device, new ConfigurationDescriptor(buffer));
		if(logger.isLoggable(Level.FINEST)){
			logger.finest("configuration @0: " + getDescriptor());
		}
		assert(buffer.limit() <= getDescriptor().getTotalLength()):
			"too large configuration descriptor binary passed: " + buffer.limit() + "/" + getDescriptor().getTotalLength();

		// インターフェース実装の構築
		for(InterfaceImpl intf: parseInterface(buffer)){
			addChildNode(intf);
		}
		return;
	}

	// ======================================================================
	// デバイスの参照
	// ======================================================================
	/**
	 * このコンフィギュレーションのデバイスを参照します。
	 * <p>
	 * @return デバイス
	 */
	public Device getDevice(){
		return super.getParentNode();
	}

	// ======================================================================
	// インターフェースの参照
	// ======================================================================
	/**
	 * このコンフィギュレーションの全てのインターフェースを参照します。返値のインターフェースは
	 * 呼び出し側が {@link InterfaceImpl#release()} を呼び出す必要があります。
	 * <p>
	 * @return インターフェースのリスト
	 */
	public List<Interface> getInterfaces(){
		return new ArrayList<Interface>(super.getChildNode());
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
		return getDescriptor().toString();
	}

	// ======================================================================
	// インターフェース実装の構築
	// ======================================================================
	/**
	 * 指定された記述子を持つインターフェース実装を構築します。このメソッドはサブクラスでオーバー
	 * ライドしインターフェースの実装クラスを変更できるように公開されています。
	 * <p>
	 * @return インターフェース実装
	 */
	protected InterfaceImpl getInterfaceImpl(){
		return new InterfaceImpl(this);
	}

	// ======================================================================
	// インターフェース実装の構築
	// ======================================================================
	/**
	 * 指定されたバッファからインターフェース以下の記述子を解析してインターフェース実装を構築しま
	 * す。バッファはインターフェース記述子の先頭にポイントされている必要があります。
	 * <p>
	 * @param buffer 記述子のバイナリ
	 * @return インターフェース実装
	 */
	private Collection<InterfaceImpl> parseInterface(ByteBuffer buffer){
		Map<Integer,InterfaceImpl> map = new TreeMap<Integer, InterfaceImpl>();

		// インターフェースに記述子を追加
		DescriptorIterator it = new DescriptorIterator(buffer,
				Descriptor.TYPE_DEVICE, Descriptor.TYPE_CONFIGURATION);
		while(it.next()){

			// コンフィギュレーションの追加記述子を設定
			if(it.getType() != Descriptor.TYPE_INTERFACE){
				this.addAdditionalDescriptor(it.getDescriptor());
				continue;
			}

			// インターフェース記述子を先読み
			int pos = buffer.position();
			InterfaceDescriptor desc = new InterfaceDescriptor(buffer);
			buffer.position(pos);

			// インターフェース実装を参照 (作成されていなければ新規に構築)
			InterfaceImpl intf = map.get(desc.getInterfaceNumber() & 0xFF);
			if(intf == null){
				intf = getInterfaceImpl();
				map.put(desc.getInterfaceNumber() & 0xFF, intf);
			}

			// 代替設定を構築してインターフェースに追加
			AltSettingImpl alt = parseAltSetting(buffer, intf);
			intf.addChildNode(alt);
		}
		return map.values();
	}

	// ======================================================================
	// 代替設定実装の構築
	// ======================================================================
	/**
	 * 指定されたバッファから代替設定以下の記述子を解析して実装を構築します。バッファはインター
	 * フェース記述子の先頭にポイントされている必要があります。
	 * <p>
	 * @param buffer 記述子のバイナリ
	 * @param intf インターフェース実装
	 * @return 代替設定実装
	 */
	private static AltSettingImpl parseAltSetting(ByteBuffer buffer, InterfaceImpl intf){
		int pos = buffer.position();

		// インターフェース記述子を構築
		InterfaceDescriptor desc = new InterfaceDescriptor(buffer);
		if(logger.isLoggable(Level.FINEST)){
			logger.finest("intf @" + pos + ": " + desc);
		}
		assert(desc.getDescriptorType() == (Descriptor.TYPE_INTERFACE & 0xFF));

		// 代替設定実装の構築
		AltSettingImpl alt = intf.getAltSettingImpl(desc);

		// 代替設定実装に記述子を追加
		DescriptorIterator it = new DescriptorIterator(buffer,
			Descriptor.TYPE_DEVICE, Descriptor.TYPE_CONFIGURATION, Descriptor.TYPE_INTERFACE);
		while(it.next()){
			if(it.getType() == (Descriptor.TYPE_ENDPOINT & 0xFF)){
				alt.addChildNode(parseEndpoint(buffer, alt));
			} else {
				alt.addAdditionalDescriptor(it.getDescriptor());
			}
		}
		return alt;
	}

	// ======================================================================
	// エンドポイント実装の構築
	// ======================================================================
	/**
	 * 指定されたバッファからエンドポイント記述子を解析して実装を構築します。バッファはエンドポイ
	 * ント記述子の先頭にポイントされている必要があります。
	 * <p>
	 * @param buffer 記述子のバイナリ
	 * @param alt 代替設定実装
	 * @return エンドポイント実装
	 */
	private static EndpointImpl parseEndpoint(ByteBuffer buffer, AltSettingImpl alt){
		int pos = buffer.position();

		// エンドポイント記述子を構築
		EndpointDescriptor desc = new EndpointDescriptor(buffer);
		if(logger.isLoggable(Level.FINEST)){
			logger.finest("edpt @" + pos + ": " + desc);
		}
		assert(desc.getDescriptorType() == (Descriptor.TYPE_ENDPOINT & 0xFF));

		// エンドポイント実装の構築
		EndpointImpl edpt = alt.getEndpointImpl(desc);

		// エンドポイントに記述子を追加
		DescriptorIterator it = new DescriptorIterator(buffer,
				Descriptor.TYPE_DEVICE, Descriptor.TYPE_CONFIGURATION,
				Descriptor.TYPE_INTERFACE, Descriptor.TYPE_ENDPOINT);
		while(it.next()){
			edpt.addAdditionalDescriptor(it.getDescriptor());
		}

		// エンドポイント実装の構築
		return edpt;
	}

	// ======================================================================
	// 16進数文字列の取得
	// ======================================================================
	/**
	 * 指定されたバイトバッファから 16 進数文字列を取得します。
	 * <p>
	 * @param binary バイトバッファ
	 * @param length 長さ
	 * @return ダンプ用文字列
	 */
	private static String hex(ByteBuffer binary, int length){
		StringBuilder buffer = new StringBuilder(length * 2);
		for(int i=0; i<length; i++){
			int ch = binary.get() & 0xFF;
			buffer.append(Character.forDigit((ch >> 4) & 0x0F, 16));
			buffer.append(Character.forDigit((ch >> 0) & 0x0F, 16));
		}
		return buffer.toString();
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// DescriptorIterator: 記述子列挙
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * バイトバッファの記述子を列挙するためのクラスです。
	 */
	private static final class DescriptorIterator{

		// ==================================================================
		// バイトバッファ
		// ==================================================================
		/**
		 * 記述子を列挙するバイトバッファです。
		 * <p>
		 */
		private final ByteBuffer buffer;

		// ==================================================================
		// 記述子の開始位置
		// ==================================================================
		/**
		 * 現在の記述子の開始位置です。
		 * <p>
		 */
		private int start = -1;

		// ==================================================================
		// 記述子の長さ
		// ==================================================================
		/**
		 * 現在の記述子の長さです。
		 * <p>
		 */
		private int length = -1;

		// ==================================================================
		// 記述子のタイプ
		// ==================================================================
		/**
		 * 現在の記述子のタイプです。
		 * <p>
		 */
		private int type = -1;

		// ==================================================================
		// 終了タイプ
		// ==================================================================
		/**
		 * 検出時に列挙を終了するタイプです。
		 * <p>
		 */
		private final byte[] stopType;

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * 記述子を列挙するバイトバッファを指定して構築を行います。
		 * <p>
		 * @param buffer バッファ
		 * @param stopType 終了タイプ
		 */
		public DescriptorIterator(ByteBuffer buffer, byte... stopType){
			this.buffer = buffer;
			this.stopType = stopType;
			this.start = buffer.position();
			return;
		}

		// ==================================================================
		// 次の記述子へ移動
		// ==================================================================
		/**
		 * 次の記述子へ移動します。これ以上記述子が存在しない場合は負の値を返します。
		 * <p>
		 * @return これ以上記述子が存在しない場合 false
		 */
		public boolean next(){

			// バッファ無いにデータが存在しない場合
			if(buffer.remaining() <= 2){
				return false;
			}

			// 記述子の長さとタイプを参照
			start = buffer.position();
			length = buffer.get() & 0xFF;
			type = buffer.get() & 0xFF;
			buffer.position(start);

			// 長さが不正な場合は終了
			// ※長さが 0 の場合は無限ループに陥る
			if(length < 2){
				logger.warning("invalid length descriptor found: " + length + " at " + buffer.position() + "/" + buffer.limit() + ",type=" + Integer.toHexString(type) + "; give up");
				buffer.position(start + 2);
				logger.finer(String.format("%02X:%02X:%s", length, type, hex(buffer, buffer.remaining())));
				return false;
			}

			// 終了タイプ以上の型を検出したら終了
			for(int i=0; i<stopType.length; i++){
				if((stopType[i] & 0xFF) == type){
					return false;
				}
			}

			return true;
		}

		// ==================================================================
		// タイプの参照
		// ==================================================================
		/**
		 * 現在の記述子のタイプを参照します。
		 * <p>
		 * @return 記述子のタイプ
		 */
		public int getType(){
			assert(type >= 0);
			return type;
		}

		// ==================================================================
		// 記述子の参照
		// ==================================================================
		/**
		 * 現在の位置からクラス仕様/ベンダー仕様記述子を構築します。
		 * <p>
		 * @return 現在の位置の追加記述子
		 */
		public Descriptor getDescriptor(){
			String logname = null;
			Descriptor desc = null;

			switch(getType()){
			case Descriptor.TYPE_INTERFACE:
				logname = "intf";
				desc = new InterfaceDescriptor(buffer);
				break;
			case Descriptor.TYPE_ENDPOINT:
				logname = "edpt";
				desc = new EndpointDescriptor(buffer);
				break;
			case Descriptor.TYPE_INTERFACE_ASSOCIATION:
				logname = "ifassoc";
				desc = new InterfaceAssociationDescriptor(buffer);
				break;
			case Descriptor.TYPE_CS_INTERFACE:
			case Descriptor.TYPE_CS_ENDPOINT & 0xFF:
				logname = "func";
				desc = new FunctionalDescriptor(buffer);
				break;
			default:
				logname = "unknown";
				desc = new UnknownDescriptor(buffer);
				break;
			}

			if(logger.isLoggable(Level.FINEST)){
				logger.finest(logname + " @" + start + ": " + desc);
			}
			return desc;
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// UnknownDescriptor: 不明な記述子
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * 不明な記述子を表すクラスです。
	 */
	private static final class UnknownDescriptor extends Descriptor{

		// ==================================================================
		// シリアルバージョン
		// ==================================================================
		/**
		 * このクラスのシリアルバージョンです。
		 * <p>
		 */
		private static final long serialVersionUID = 1L;

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * コンストラクタは何も行いません。
		 * <p>
		 * @param buffer 記述子のバイナリ
		 */
		public UnknownDescriptor(ByteBuffer buffer) {
			super(buffer);
			pack(buffer);
			return;
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
			return super.toString() + super.toHexExtra();
		}
	}
}
