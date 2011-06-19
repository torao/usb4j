/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: GatheredUSBSession.java,v 1.2 2009/05/18 20:34:11 torao Exp $
*/
package org.koiroha.usb;

import java.util.*;

import org.koiroha.usb.event.USBContextListener;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// GatheredUSBSession: 集合 USB コンテキスト
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 複数の USB コンテキストをとりまとめる為のクラスです。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2009/05/18 20:34:11 $
 * @author takami torao
 * @since usb4j 1.0 (Java2 SE 5.0) 2009/05/18
 */
class GatheredUSBSession implements USBContext {

	// ======================================================================
	// USB コンテキスト
	// ======================================================================
	/**
	 * USB コンテキストです。
	 * <p>
	 */
	private final List<USBContext> context;

	// ======================================================================
	// USB コンテキスト
	// ======================================================================
	/**
	 * 指定された複数の USB コンテキストをまとめたコンテキストを構築します。
	 * <p>
	 * @param sessions USB コンテキスト
	 */
	public GatheredUSBSession(List<USBContext> sessions){
		this.context = sessions;
		return;
	}

	// ======================================================================
	// USB デバイス構成の同期化
	// ======================================================================
	/**
	 * このコンテキストの保持する USB デバイスを同期化します。現在のコンテキスト内に存在するデバイス
	 * インスタンスは全て解放されます。
	 * <p>
	 * @throws USBException USB デバイスの取得に失敗した場合
	*/
	public void sync() throws USBException{
		synchronized(context){
			for(int i=0; i<context.size(); i++){
				context.get(i).sync();
			}
		}
		return;
	}

	// ======================================================================
	// USB デバイスの取得
	// ======================================================================
	/**
	 * システムで検出された全ての USB デバイスを取得します。このメソッドは
	 * {@link #lookup(int, int) lookup}(-1, -1) と等価です。
	 * <p>
	 * @return USB デバイスのリスト
	 * @throws USBException USB デバイスの取得に失敗した場合
	*/
	public List<Device> lookup() throws USBException{
		List<Device> devices = new ArrayList<Device>();
		synchronized(context){
			for(int i=0; i<context.size(); i++){
				devices.addAll(context.get(i).lookup());
			}
		}
		return Collections.unmodifiableList(devices);
	}

	// ======================================================================
	// USB デバイスの取得
	// ======================================================================
	/**
	 * 指定されたベンダー ID、製品 ID に一致する USB デバイスを取得します。ベンダー ID に
	 * 負の値を指定した場合は全ての USB デバイスが検出対象となります。また製品 ID に負の値を
	 * 指定した場合は該当するベンダー ID を持つ全ての USB デバイスが検出対象となります。
	 * 該当する USB デバイスがシステムで検出されなかった場合、メソッドは長さ 0 のリストを返し
	 * ます。
	 * <p>
	 * @param idVendor 0〜65535 のベンダー ID (負の値を指定した場合はワイルドカード)
	 * @param idProduct 0〜65535 の製品 ID (負の値を指定した場合はワイルドカード)
	 * @return USB デバイスのリスト
	 * @throws USBException USB デバイスの取得に失敗した場合
	*/
	public List<Device> lookup(final int idVendor, final int idProduct) throws USBException{
		List<Device> devices = new ArrayList<Device>();
		synchronized(context){
			for(int i=0; i<context.size(); i++){
				devices.addAll(context.get(i).lookup(idVendor, idProduct));
			}
		}
		return Collections.unmodifiableList(devices);
	}

	// ======================================================================
	// USB デバイスの取得
	// ======================================================================
	/**
	 * 指定されたデバイスフィルタを使用して USB デバイスを取得します。フィルタに null を指定す
	 * ると全てのデバイスが対象となります。
	 * <p>
	 * @param filter デバイスフィルタ
	 * @return USB デバイスのリスト
	 * @throws USBException USB デバイスの取得に失敗した場合
	*/
	public List<Device> lookup(DeviceFilter filter) throws USBException{
		List<Device> devices = new ArrayList<Device>();
		synchronized(context){
			for(int i=0; i<context.size(); i++){
				devices.addAll(context.get(i).lookup(filter));
			}
		}
		return Collections.unmodifiableList(devices);
	}

	// ======================================================================
	// コンテキストの解放
	// ======================================================================
	/**
	 * このコンテキスト内で使用している全ての未解放デバイスを解放します。
	 * <p>
	 * @throws USBException コンテキストの解放に失敗した場合
	*/
	public void dispose() throws USBException{
		synchronized(context){
			for(int i=0; i<context.size(); i++){
				context.get(i).dispose();
			}
		}
		return;
	}

	// ======================================================================
	// リスナの追加
	// ======================================================================
	/**
	 * コンテキストにリスナを追加します。
	 * <p>
	 * @param l 追加するリスナ
	*/
	public void addContextListener(USBContextListener l){
		synchronized(context){
			for(int i=0; i<context.size(); i++){
				context.get(i).addContextListener(l);
			}
		}
		return;
	}

	// ======================================================================
	// リスナの削除
	// ======================================================================
	/**
	 * コンテキストからリスナを削除します。
	 * <p>
	 * @param l 削除するリスナ
	*/
	public void removeContextListener(USBContextListener l){
		synchronized(context){
			for(int i=0; i<context.size(); i++){
				context.get(i).removeContextListener(l);
			}
		}
		return;
	}

}
