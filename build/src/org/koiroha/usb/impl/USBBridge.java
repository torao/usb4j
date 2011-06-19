/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: USBBridge.java,v 1.7 2009/05/21 12:02:54 torao Exp $
*/
package org.koiroha.usb.impl;

import java.util.List;

import org.koiroha.usb.*;
import org.koiroha.usb.desc.*;
import org.koiroha.usb.event.IsocTransferEvent;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// USBBridge: USB ブリッジインターフェース
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 低レベル USB 操作を行うためのブリッジインターフェースです。プラットフォームごとの usb4j
 * ポーティング実装のために最小限に押さえたインターフェースを定義しています。
 * <p>
 * @version $Revision: 1.7 $ $Date: 2009/05/21 12:02:54 $
 * @author takami torao
 * @since usb4j 1.0 (Java2 SE 5.0) 2009/05/15
 * @see USBServiceImpl#USBServiceImpl(USBBridge)
 */
public interface USBBridge {

	// ======================================================================
	// ライブラリ名の参照
	// ======================================================================
	/**
	 * このブリッジのライブラリ名を参照します。返値は人が認識するための文字列です。null を返す
	 * 事はできません。
	 * <p>
	 * @return ライブラリ名
	 * @throws USBException ライブラリ名の取得に失敗した場合
	 * @see USBService#getLibraryName()
	*/
	public String getLibraryName() throws USBException;

	// ======================================================================
	// コンテキストの構築
	// ======================================================================
	/**
	 * 新規のコンテキストを構築します。
	 * <p>
	 * @return コンテキスト
	 * @throws USBException コンテキストの構築に失敗した場合
	*/
	public USBContextImpl create() throws USBException;

	// ======================================================================
	// コンテキストの解放の解放
	// ======================================================================
	/**
	 * 指定されたコンテキストのリソースを解放します。サブクラスは usb4j 実装による以下の動作を期待
	 * することが出来ます。
	 * <p>
	 * <ul>
	 * <li>既に解放されているデバイスに対する重複呼び出し抑止</li>
	 * <li>メソッドが呼び出される前の暗黙的な {@link #close(DeviceImpl)},
	 * {@link #release(DeviceImpl, byte)} 呼び出し</li>
	 * <li>ファイナライザ、シャットダウンフックを使用した未解放デバイスに対する暗黙的な呼び出し</li>
	 * <li>コンテキスト中の全てのデバイスが解放済み</li>
	 * <ul>
	 * <p>
	 * @param session コンテキスト実装
	 * @throws USBException コンテキストの解放に失敗した場合
	 * @see USBContext#dispose()
	*/
	public void release(USBContextImpl session) throws USBException;

	// ======================================================================
	// デバイスの検索
	// ======================================================================
	/**
	 * 指定されたコンテキストでの USB デバイスを検索し論理トポロジーを構築して返します。
	 * <p>
	 * @param session コンテキスト
	 * @return バス実装の一覧
	 * @throws USBException USB デバイスの取得に失敗した場合
	*/
	public List<BusImpl> find(USBContextImpl session) throws USBException;

	// ======================================================================
	// デバイスの解放
	// ======================================================================
	/**
	 * 指定されたデバイスのリソースを解放します。サブクラスはデバイスごとに割り当てたリソースを
	 * このメソッドで解放することが出来ます。
	 * <p>
	 * このメソッドは {@link DeviceImpl#release()} をオーバーライドする代わりに以下の点
	 * で利点を持ちます。
	 * <p>
	 * <ul>
	 * <li>既に解放が完了しているデバイスに対してこのメソッドが重複して呼び出されることはありません。</li>
	 * <ul>
	 * <p>
	 * @param devid デバイス実装
	 * @throws USBException デバイスの解放に失敗した場合
	 * @see DeviceImpl#release()
	*/
	public void release(DeviceImpl devid) throws USBException;

	// ======================================================================
	// デバイスのオープン
	// ======================================================================
	/**
	 * デバイス実装をオープンします。usb4j によって状態管理が行われているため、既にオープンされ
	 * ているデバイス実装に対してこのメソッドが呼び出されることはありません。
	 * <p>
	 * 返値のオブジェクトはデバイス実装が持つハンドルとして
	 * {@link DeviceImpl#getOpenedHandle()} で参照することが出来ます。サブクラスは
	 * オープンに成功した場合、ハンドルに相当するオブジェクトが存在しなくても null 以外の値を
	 * 返す必要があります。
	 * <p>
	 * @param device オープンするデバイス実装
	 * @return デバイスハンドル
	 * @throws USBException デバイスのオープンに失敗した場合
	 * @see Device#open()
	*/
	public Object open(DeviceImpl device) throws USBException;

	// ======================================================================
	// デバイスのクローズ
	// ======================================================================
	/**
	 * デバイス実装をクローズします。usb4j によって状態管理が行われているため、既にクローズされ
	 * ているデバイス実装に対してこのメソッドが呼び出されることはありません。
	 * このメソッドの完了以降の {@link DeviceImpl#getOpenedHandle()} は例外となります。
	 * <p>
	 * @param device クローズするデバイス実装
	 * @throws USBException デバイスのクローズに失敗した場合
	 * @see Device#close()
	*/
	public void close(DeviceImpl device) throws USBException;

	// ======================================================================
	// デバイスのリセット
	// ======================================================================
	/**
	 * デバイス実装をリセットします。このメソッドの完了後からデバイス実装のインスタンスは開放状態
	 * に遷移します。
	 * <p>
	 * このメソッドは既に解放されているデバイス実装に対しては呼び出されません。
	 * <p>
	 * @param device リセットするデバイス実装
	 * @throws USBException デバイスのリセットに失敗した場合
	 * @see Device#reset()
	*/
	public void reset(DeviceImpl device) throws USBException;

	// ======================================================================
	// インターフェースの要求
	// ======================================================================
	/**
	 * インターフェースを要求します。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @throws USBException インターフェースの要求に失敗した場合
	*/
	public void claim(DeviceImpl device, byte ifc) throws USBException;

	// ======================================================================
	// インターフェースの解放
	// ======================================================================
	/**
	 * インターフェースを解放します。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @throws USBException インターフェースの解放に失敗した場合
	*/
	public void release(DeviceImpl device, byte ifc) throws USBException;

	// ======================================================================
	// コントロール転送の実行
	// ======================================================================
	/**
	 * コントロール転送を実行します。
	 * タイムアウトに負の値を指定した場合は実装側で可能な限りの待機を行います。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @param ept エンドポイントインデックス
	 * @param request リクエスト
	 * @param timeout タイムアウト (ミリ秒)
	 * @return 実際に転送されたバイト数
	 * @throws USBException コントロール転送に失敗した場合
	*/
	public int controlTransfer(DeviceImpl device, byte ifc, byte ept, ControlRequest request, int timeout) throws USBException;

	// ======================================================================
	// 割り込み転送の実行
	// ======================================================================
	/**
	 * 割り込み転送を実行します。
	 * タイムアウトに負の値を指定した場合は実装側で可能な限りの待機を行います。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @param ept エンドポイントインデックス
	 * @param buffer データのバッファ
	 * @param offset バッファの入出力開始位置
	 * @param length 入出力データ長
	 * @param inout 入力/出力識別用
	 * @param timeout 入出力タイムアウト (ミリ秒)
	 * @return 実際に転送されたバイト数
	 * @throws USBException コントロール転送に失敗した場合
	*/
	public int interruptTransfer(DeviceImpl device, byte ifc, byte ept,
		byte[] buffer, int offset, int length, Direction inout, int timeout) throws USBException;

	// ======================================================================
	// バルク転送の実行
	// ======================================================================
	/**
	 * バルク転送を実行します。
	 * タイムアウトに負の値を指定した場合は実装側で可能な限りの待機を行います。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @param ept エンドポイントインデックス
	 * @param buffer データのバッファ
	 * @param offset バッファの入出力開始位置
	 * @param length 入出力データ長
	 * @param inout 入力/出力識別用
	 * @param timeout 入出力タイムアウト (ミリ秒)
	 * @return 実際に転送されたバイト数
	 * @throws USBException バルク転送に失敗した場合
	*/
	public int bulkTransfer(DeviceImpl device, byte ifc, byte ept,
		byte[] buffer, int offset, int length, Direction inout, int timeout) throws USBException;

	// ======================================================================
	// 等時間隔転送の実行
	// ======================================================================
	/**
	 * 等時間隔転送を実行します。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @param ept エンドポイントインデックス
	 * @param event 等時間隔転送イベント
	 * @throws USBException バルク転送に失敗した場合
	*/
	public void isochronousTransfer(DeviceImpl device, byte ifc, byte ept, IsocTransferEvent event) throws USBException;

	// ======================================================================
	// エンドポイントのリセット
	// ======================================================================
	/**
	 * エンドポイントをリセットします。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @param ept エンドポイントインデックス
	 * @throws USBException エンドポイントのリセットに失敗した場合
	*/
	public void clearHalt(DeviceImpl device, byte ifc, byte ept) throws USBException;

	// ======================================================================
	// デバイスリクエストの実行
	// ======================================================================
	/**
	 * 指定されたデバイス要求を実行します。usb4j API の標準デバイスリクエストから発行される
	 * 要求は以下の通り。
	 * <p>
	 * <table>
	 * <tr><td>メソッド</td><th>bRequest</th><th>wValue</th><th>wLength</th><th>buffer</td></tr>
	 * <tr><td>{@link Device#setActiveConfiguration(int)}</td>
	 *     <td>{@link ControlRequest#SET_CONFIGURATION}</td>
	 *     <td>0</td><td>0</td>
	 *     <td>[0]設定するコンフィギュレーションの {@code bConfigurationValue} 値</td>
	 * </tr>
	 * <tr><td>{@link Device#getActiveConfiguration()}</td>
	 *     <td>{@link ControlRequest#GET_CONFIGURATION}</td>
	 *     <td>0</td><td>0</td>
	 *     <td>[0]現在のコンフィギュレーションの {@code bConfigurationValue} 値</td>
	 * </tr>
	 * <tr><td>{@link Interface#setActiveAltSetting(int)}</td>
	 *     <td>{@link ControlRequest#SET_INTERFACE}</td>
	 *     <td>0</td><td>0</td>
	 *     <td>[0]設定する代替設定の {@code bAlternateSetting} 値</td>
	 * </tr>
	 * <tr><td>{@link Interface#getActiveAltSetting()}</td>
	 *     <td>{@link ControlRequest#GET_INTERFACE}</td>
	 *     <td>0</td><td>0</td>
	 *     <td>[0]現在の代替設定の {@code bAlternateSetting} 値</td>
	 * </tr>
	 * <tr><td>{@link DeviceImpl#getRawDescriptor(byte, byte, short, byte[])}</td>
	 *     <td>{@link ControlRequest#GET_DESCRIPTOR}</td>
	 *     <td>上位:記述子タイプ,下位:インデックス</td><td>言語ID</td>
	 *     <td>記述子バイナリの格納先バッファ</td>
	 * </tr>
	 * </table>
	 * <p>
	 * マルチバイトの変換に注意。
	 * <p>
	 * このメソッドはオープンされているデバイスでのみ呼び出されます。
	 * <p>
	 * @param device デバイス実装
	 * @param request デバイスリクエスト
	 * @return 実際の入出力バイト数
	 * @throws USBException デバイスリクエストに失敗した場合
	 */
	public int deviceRequest(DeviceImpl device, ControlRequest request) throws USBException;

}
