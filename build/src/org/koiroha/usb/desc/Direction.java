/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: Direction.java,v 1.3 2009/05/14 02:22:33 torao Exp $
*/
package org.koiroha.usb.desc;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Direction: 転送方向
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB エンドポイントの転送方向を示す列挙型です。
 * <p>
 * @version usb4j 1.0 $Revision: 1.3 $ $Date: 2009/05/14 02:22:33 $
 * @author torao
 * @since 2009/04/27 Java2 SE 5.0
 */
public enum Direction {

	// ======================================================================
	// OUT 方向転送
	// ======================================================================
	/**
	 * ホストからデバイス方向への転送を表す定数です。
	 * <p>
	 * @see EndpointDescriptor#getDirection()
	 */
	OUT(0x0),

	// ======================================================================
	// IN 方向転送
	// ======================================================================
	/**
	 * デバイスからホスト方向への転送を表す定数です。
	 * <p>
	 * @see EndpointDescriptor#getDirection()
	 */
	IN(0x1),

	// ======================================================================
	// 双方向転送
	// ======================================================================
	/**
	 * 双方向の転送を表す定数です。この値はコントロール転送を表すエンドポイント 0 に対してのみ
	 * 使用されます。
	 * <p>
	 * @see EndpointDescriptor#getDirection()
	 */
	DUPLEX(-1),

	;

	// ======================================================================
	// 値
	// ======================================================================
	/**
	 * この方向の値です。
	 * <p>
	 */
	private final int value;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 値を指定して構築を行います。
	 * <p>
	 * @param value 値
	 */
	private Direction(int value){
		this.value = value;
		return;
	}

	// ======================================================================
	// 識別値の参照
	// ======================================================================
	/**
	 * この転送方向を表す数値を参照します。
	 * <p>
	 * @return 転送方向の数値
	 */
	public int getType(){
		return value;
	}

	// ======================================================================
	// タイプ値による参照
	// ======================================================================
	/**
	 * 指定された転送方向値に対するインスタンスを参照します。
	 * <p>
	 * @param type 参照する転送方向の値
	 * @return 転送方向
	 */
	public static Direction valueOf(int type){
		for(Direction t: values()){
			if(t.getType() == type){
				return t;
			}
		}
		assert(false): type;
		return null;
	}

}
