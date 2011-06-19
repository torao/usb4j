/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: openusb1_jni_bridge.cpp,v 1.3 2009/05/16 05:24:28 torao Exp $
*/
#define CONFDESC_MAXPOWER bMaxPower
#include <string.h>
#include <openusb.h>
#include "usb4java.h"
#include "org_koiroha_usb_impl_openusb_OpenUSB.h"

using namespace usb4j;

DECLARE_USB_LIBRARY("openusb_1_0", "OpenUSB 1.0", 0x0100);

// Class name definition
#define CLS_OPENUSB "org/koiroha/usb/impl/openusb/OpenUSB"
#define CLS_REF            CLS_OPENUSB "$ref_t"
#define CLS_DEVDATA        CLS_OPENUSB "$dev_data_t"

// Prototype declaration for local functions
static void set_reference(JVM& jvm, jobject reference, void* ref, jobject value);
static void* get_reference(JVM& jvm, jobject reference);
static void make_devid_array(JVM& jvm, jobject devids, openusb_devid_t* buf, uint32_t num_devids);
static void event_callback(openusb_handle_t handle, openusb_devid_t devid, openusb_event_t event, void *arg);
static void debug_callback(openusb_handle_t handle, const char *fmt, va_list args);

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    nativeInterfaceVersion
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB_nativeInterfaceVersion
  (JNIEnv *env, jobject)
{
	return initialize(env);
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _init
 * Signature: (I[J)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1init
  (JNIEnv *env, jclass, jint flags, jlongArray buf)
{
	JVM jvm(env);
	TRACE("init(0x%X)", (int)flags);
	openusb_handle_t handle = 0;

	// initialize OpenUSB handle and set to instance field
	int32_t ret = openusb_init(flags, &handle);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_init(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}
	TRACE("  --> handle=0x%X,ret=%d", (void*)handle, (int)ret);
	set_long_to_buffer(jvm, buf, handle);
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _fini
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1fini
  (JNIEnv *env, jclass, jlong handle)
{
	JVM jvm(env);
	TRACE("fini(%p)", (void*)handle);

	// stop all event callback
	for(int i=USB_ATTACH; i<OPENUSB_EVENT_TYPE_COUNT; i++){
		openusb_set_event_callback((openusb_handle_t)handle, (openusb_event_t)i, NULL, NULL);
	}

	// stop debug callback
	// *** SIGSEGV caused if debug callback from hotplug thread is passed to jvm after openusb_fini().
	uint32_t level = 0;
	char* openusb_debug = getenv("OPENUSB_DEBUG");
	if(openusb_debug != NULL){
		level = atoi(openusb_debug);
	}
	openusb_set_debug((openusb_handle_t)handle, level, 0, NULL);

	// release OpenUSB handle and set null to instance field
	openusb_fini((openusb_handle_t)handle);
	return;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _set_debug
 * Signature: (JIIZ)V
 */
JNIEXPORT void JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1set_1debug
  (JNIEnv *env, jclass, jlong handle, jint level, jint flags, jboolean enable)
{
	JVM jvm(env);
	TRACE("set_debug(%p,%d,0x%X,%s)", (void*)handle, (int)level, (int)flags, (enable)? "enable": "disable");

	// set debug level
	openusb_set_debug((openusb_handle_t)handle, (uint32_t)level, (uint32_t)flags, (enable)? debug_callback: NULL);
	return;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _set_event_callback
 * Signature: (JIZ)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1set_1event_1callback
  (JNIEnv *env, jclass, jlong handle, jint event, jboolean enable)
{
	JVM jvm(env);
	TRACE("set_event_callback(%p,%d,%s)", (void*)handle, (int)event, (enable? "enable": "disable"));

	return openusb_set_event_callback((openusb_handle_t)handle, (openusb_event_t)event, (enable? event_callback: NULL), NULL);
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _coldplug_callbacks_done
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1coldplug_1callbacks_1done
  (JNIEnv *env, jclass, jlong handle)
{
	JVM jvm(env);
	TRACE("coldplug_callbacks_done(%p)", (void*)handle);

	openusb_coldplug_callbacks_done((openusb_handle_t)handle);
	return;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _set_default_timeout
 * Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1set_1default_1timeout
  (JNIEnv *env, jclass, jlong handle, jint type, jint timeout)
{
	JVM jvm(env);
	TRACE("set_default_timeout(%p,%d,%d)", (void*)handle, (int)type, (int)timeout);

	return openusb_set_default_timeout((openusb_handle_t)handle, (openusb_transfer_type_t)type, (uint32_t)timeout);
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _get_busid_list
 * Signature: (JLorg/koiroha/usb/impl/openusb/OpenUSB$busid_t_array;)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1get_1busid_1list
  (JNIEnv *env, jclass, jlong handle, jobject busids)
{
	JVM jvm(env);
	TRACE("get_busid_list(%p,busids)", (void*)handle);

	// retrieve bus ids
	openusb_busid_t* buf = NULL;
	uint32_t num_busids = 0;
	int ret = openusb_get_busid_list((openusb_handle_t)handle, &buf, &num_busids);
	if(ret != OPENUSB_SUCCESS){
		return ret;
	}
	TRACE("  --> busids=%p", (void*)buf);

	// create busid array
	jlongArray refsArray = jvm.env->NewLongArray(num_busids);
	if(refsArray == NULL){
		openusb_free_busid_list(buf);
		return 0;
	}
	jlong* refs = jvm.env->GetLongArrayElements(refsArray, NULL);
	for(uint32_t i=0; i<num_busids; i++){
		refs[i] = buf[i];
	}
	jvm.env->ReleaseLongArrayElements(refsArray, refs, 0);

	// set reference to ref object to free after
	set_reference(jvm, busids, buf, refsArray);
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _free_busid_list
 * Signature: (Lorg/koiroha/usb/impl/openusb/OpenUSB$busid_t_array;)V
 */
JNIEXPORT void JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1free_1busid_1list
  (JNIEnv *env, jclass, jobject busids)
{
	JVM jvm(env);
	openusb_busid_t* buf = (openusb_busid_t*)get_reference(jvm, busids);
	TRACE("free_busid_list(%p)", (void*)buf);

	openusb_free_busid_list(buf);
	set_reference(jvm, busids, (void*)NULL, (jobject)NULL);
	return;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _get_devids
 * Signature: (JIJJJLorg/koiroha/usb/impl/openusb/OpenUSB$devid_t_array;)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1get_1devids
  (JNIEnv *env, jclass, jlong handle, jint mode, jlong arg1, jlong arg2, jlong arg3, jobject devids)
{
	JVM jvm(env);

	// retrieve bus ids
	openusb_devid_t* buf = NULL;
	uint32_t num_devids = 0;
	int ret = 0;
	switch(mode){
	case 0:
		TRACE("get_devids_by_bus(%p,%p,devids)", (void*)handle, (void*)arg1);
		ret = openusb_get_devids_by_bus((openusb_handle_t)handle, (openusb_busid_t)arg1, &buf, &num_devids);
		break;
	case 1:
		TRACE("get_devids_by_vendor(%p,0x%x,0x%x,devids)", (void*)handle, (int)arg1, (int)arg2);
		ret = openusb_get_devids_by_vendor((openusb_handle_t)handle, (int32_t)arg1, (int32_t)arg2, &buf, &num_devids);
		break;
	case 2:
		TRACE("get_devids_by_class(%p,0x%x,0x%x,0x%x,devids)", (void*)handle, (int)arg1, (int)arg2, (int)arg3);
		ret = openusb_get_devids_by_class((openusb_handle_t)handle, (int16_t)arg1, (int16_t)arg2, (int16_t)arg3, &buf, &num_devids);
		break;
	default:
		jvm.abort("unexpected devid mode: %d", mode);
		break;
	}
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_get_devids(): [%d] %s", ret, openusb_strerror(ret));
		return ret;
	}
	TRACE("  --> devids=%p", (void*)buf);
	make_devid_array(jvm, devids, buf, num_devids);
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _free_devid_list
 * Signature: (Lorg/koiroha/usb/impl/openusb/OpenUSB$devid_t_array;)V
 */
JNIEXPORT void JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1free_1devid_1list
  (JNIEnv *env, jclass, jobject devids)
{
	JVM jvm(env);
	openusb_devid_t* buf = (openusb_devid_t*)get_reference(jvm, devids);
	TRACE("free_devid_list(%p)", (void*)buf);

	openusb_free_devid_list(buf);
	set_reference(jvm, devids, (void*)NULL, (jobject)NULL);
	return;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _get_device_data
 * Signature: (JJILorg/koiroha/usb/impl/openusb/OpenUSB$dev_data_ref;)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1get_1device_1data
  (JNIEnv *env, jclass, jlong handle, jlong devid, jint flags, jobject data)
{
	JVM jvm(env);
	TRACE("get_device_data(%p,%p,0x%x,data)", (void*)handle, (void*)devid, (int)flags);

	openusb_dev_data_t* buf = NULL;
	int32_t ret = openusb_get_device_data((openusb_handle_t)handle, (openusb_devid_t)devid, (uint32_t)flags, &buf);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_get_device_data(): [%d] %s", ret, openusb_strerror(ret));
		return ret;
	}
	DUMP(buf, sizeof(openusb_dev_data_t), "openusb_dev_data_t");

	// retrieve descriptor info
	jobject dev_desc = create_device_descriptor(jvm, buf->dev_desc);
	jobject cfg_desc = create_configuration_descriptor(jvm, buf->cfg_desc);
	jstring manufacturer = (buf->manufacturer==NULL)? NULL: create_string_from_descriptor(jvm, *(buf->manufacturer));
	jstring product = (buf->product==NULL)? NULL: create_string_from_descriptor(jvm, *(buf->product));
	jstring serialnumber = (buf->serialnumber==NULL)? NULL: create_string_from_descriptor(jvm, *(buf->serialnumber));

	// build raw configuration descriptor binary
	jsize wTotalLength = le16_to_native(*(uint16_t*)(((char*)buf->raw_cfg_desc) + 2));
	jbyteArray raw_cfg_desc = jvm.env->NewByteArray(wTotalLength);
	jvm.env->SetByteArrayRegion(raw_cfg_desc, 0, wTotalLength, (jbyte*)buf->raw_cfg_desc);
	DUMP(buf->raw_cfg_desc, wTotalLength, "raw_cfg_desc[%d]", (int)wTotalLength);

	// build dev_data structure
	jobject devdata = jvm.create(CLS_DEVDATA, "()V");
	jvm.setLong(devdata, CLS_DEVDATA, "busid", (jlong)buf->busid);
	jvm.setLong(devdata, CLS_DEVDATA, "devid", (jlong)buf->devid);
	jvm.setByte(devdata, CLS_DEVDATA, "bus_address", (jbyte)buf->bus_address);
	jvm.setLong(devdata, CLS_DEVDATA, "pdevid", (jlong)buf->pdevid);
	jvm.setByte(devdata, CLS_DEVDATA, "pport", (jbyte)buf->pport);
	jvm.setByte(devdata, CLS_DEVDATA, "nports", (jbyte)buf->nports);
	jvm.setString(devdata, CLS_DEVDATA, "sys_path", buf->sys_path);
	jvm.setString(devdata, CLS_DEVDATA, "bus_path", buf->bus_path);
	jvm.setObject(devdata, CLS_DEVDATA, "dev_desc", "L" USB_DEVICEDESC_CLASS ";", dev_desc);
	jvm.setObject(devdata, CLS_DEVDATA, "cfg_desc", "L" USB_CONFIGURATIONDESC_CLASS ";", cfg_desc);
	jvm.setObject(devdata, CLS_DEVDATA, "raw_cfg_desc", "[B", raw_cfg_desc);
	jvm.setString(devdata, CLS_DEVDATA, "manufacturer", manufacturer);
	jvm.setString(devdata, CLS_DEVDATA, "product", product);
	jvm.setString(devdata, CLS_DEVDATA, "serialnumber", serialnumber);
	jvm.setInt(devdata, CLS_DEVDATA, "ctrl_max_xfer_size", buf->ctrl_max_xfer_size);
	jvm.setInt(devdata, CLS_DEVDATA, "intr_max_xfer_size", buf->intr_max_xfer_size);
	jvm.setInt(devdata, CLS_DEVDATA, "bulk_max_xfer_size", buf->bulk_max_xfer_size);
	jvm.setInt(devdata, CLS_DEVDATA, "isoc_max_xfer_size", buf->isoc_max_xfer_size);

	set_reference(jvm, data, buf, devdata);
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _free_device_data
 * Signature: (Lorg/koiroha/usb/impl/openusb/OpenUSB$dev_data_ref;)V
 */
JNIEXPORT void JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1free_1device_1data
  (JNIEnv *env, jclass, jobject data)
{
	JVM jvm(env);
	openusb_dev_data_t* buf = (openusb_dev_data_t*)get_reference(jvm, data);
	TRACE("free_devid_data(%p)", (void*)buf);

	openusb_free_device_data(buf);
	set_reference(jvm, data, (void*)NULL, (jobject)NULL);
	return;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _open_device
 * Signature: (JJI[J)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1open_1device
  (JNIEnv *env, jclass, jlong handle, jlong devid, jint flags, jlongArray dev)
{
	JVM jvm(env);
	TRACE("open_device(%p,%p,0x%x,dev)", (void*)handle, (void*)devid, (int)flags);

	openusb_dev_handle_t buf = 0;
	int32_t ret = openusb_open_device((openusb_handle_t)handle, (openusb_devid_t)devid, (openusb_init_flag_t)flags, &buf);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_open_device(): [%d] %s", ret, openusb_strerror(ret));
		return ret;
	}
	TRACE("  --> dev_handle=%p", (void*)buf);

	set_long_to_buffer(jvm, dev, (jlong)buf);
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _close_device
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1close_1device
  (JNIEnv *env, jclass, jlong dev)
{
	JVM jvm(env);
	TRACE("close_device(%p)", (void*)dev);

	int32_t ret = openusb_close_device((openusb_dev_handle_t)dev);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_close_device(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _get_devid
 * Signature: (J[J)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1get_1devid
  (JNIEnv *env, jclass, jlong dev, jlongArray devid)
{
	JVM jvm(env);
	TRACE("get_devid(%p,devid)", (void*)dev);

	openusb_devid_t buf = 0;
	int32_t ret = openusb_get_devid((openusb_dev_handle_t)dev, &buf);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_get_devid(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}

	set_long_to_buffer(jvm, devid, (jlong)buf);
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _get_lib_handle
 * Signature: (J[J)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1get_1lib_1handle
  (JNIEnv *env, jclass, jlong dev, jlongArray handle)
{
	JVM jvm(env);
	TRACE("get_lib_handle(%p,lib_handle)", (void*)dev);

	openusb_handle_t buf = 0;
	int32_t ret = openusb_get_lib_handle((openusb_dev_handle_t)dev, &buf);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_get_lib_handle(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}

	set_long_to_buffer(jvm, handle, (jlong)buf);
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _get_max_xfer_size
 * Signature: (JJI[I)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1get_1max_1xfer_1size
  (JNIEnv *env, jclass, jlong handle, jlong busid, jint type, jintArray bytes)
{
	JVM jvm(env);
	TRACE("get_max_xfer_size(%p,%p,0x%x,bytes)", (void*)handle, (void*)busid, (int)type);

	uint32_t buf = 0;
	int32_t ret = openusb_get_max_xfer_size((openusb_handle_t)handle, (openusb_busid_t)busid, (openusb_transfer_type_t)type, &buf);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_get_max_xfer_size(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}

	set_int_to_buffer(jvm, bytes, (jint)buf);
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _get_configuration
 * Signature: (J[B)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1get_1configuration
  (JNIEnv *env, jclass, jlong dev, jbyteArray cfg)
{
	JVM jvm(env);
	TRACE("get_configuration(%p,cfg)", (void*)dev);

	uint8_t buf = 0;
	int32_t ret = openusb_get_configuration((openusb_dev_handle_t)dev, &buf);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_get_configuration(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}

	set_byte_to_buffer(jvm, cfg, (jbyte)buf);
	return ret;
}


/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _set_configuration
 * Signature: (JB)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1set_1configuration
  (JNIEnv *env, jclass, jlong dev, jbyte cfg)
{
	JVM jvm(env);
	TRACE("set_configuration(%p,%d)", (void*)dev, (int)cfg & 0xFF);

	int32_t ret = openusb_set_configuration((openusb_dev_handle_t)dev, (uint8_t)cfg);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_set_configuration(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _claim_interface
 * Signature: (JBI)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1claim_1interface
  (JNIEnv *env, jclass, jlong dev, jbyte ifc, jint flags)
{
	JVM jvm(env);
	TRACE("claim_interface(%p,%d,0x%x)", (void*)dev, (int)ifc & 0xFF, (int)flags);

	int32_t ret = openusb_claim_interface((openusb_dev_handle_t)dev, (uint8_t)ifc, (openusb_init_flag_t)flags);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_claim_interface(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _release_interface
 * Signature: (JB)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1release_1interface
  (JNIEnv *env, jclass, jlong dev, jbyte ifc)
{
	JVM jvm(env);
	TRACE("release_interface(%p,%d)", (void*)dev, (int)ifc & 0xFF);

	int32_t ret = openusb_release_interface((openusb_dev_handle_t)dev, (uint8_t)ifc);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_release_interface(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _is_interface_claimed
 * Signature: (JB)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1is_1interface_1claimed
  (JNIEnv *env, jclass, jlong dev, jbyte ifc)
{
	JVM jvm(env);
	TRACE("is_interface_claimed(%p,%d)", (void*)dev, (int)ifc & 0xFF);

	return openusb_is_interface_claimed((openusb_dev_handle_t)dev, (uint8_t)ifc);
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _get_altsetting
 * Signature: (JB[B)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1get_1altsetting
  (JNIEnv *env, jclass, jlong dev, jbyte ifc, jbyteArray alt)
{
	JVM jvm(env);
	TRACE("get_altsetting(%p,%d,alt)", (void*)dev, (int)ifc & 0xFF);

	uint8_t buf = 0;
	int32_t ret = openusb_get_altsetting((openusb_dev_handle_t)dev, (uint8_t)ifc, &buf);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_get_altsetting(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}

	set_byte_to_buffer(jvm, alt, (jbyte)buf);
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _set_altsetting
 * Signature: (JBB)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1set_1altsetting
  (JNIEnv *env, jclass, jlong dev, jbyte ifc, jbyte alt)
{
	JVM jvm(env);
	TRACE("set_altsetting(%p,%d,%d)", (void*)dev, (int)ifc & 0xFF, (int)alt & 0xFF);

	int32_t ret = openusb_set_altsetting((openusb_dev_handle_t)dev, (uint8_t)ifc, (uint8_t)alt);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_get_altsetting(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _reset
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1reset
  (JNIEnv *env, jclass, jlong dev)
{
	JVM jvm(env);
	TRACE("reset(%p)", (void*)dev);

	int32_t ret = openusb_reset((openusb_dev_handle_t)dev);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_reset(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _get_raw_desc
 * Signature: (JJBBS[[B)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1get_1raw_1desc
  (JNIEnv *env, jclass, jlong handle, jlong devid, jbyte type, jbyte descidx, jshort langid, jobjectArray buffer)
{
	JVM jvm(env);
	TRACE("get_raw_desc(%p,%p,%d,%d,%d,buf)", (void*)handle, (void*)devid, (int)type & 0xFF, (int)descidx & 0xFF, (int)langid & 0xFFFF);

	uint8_t* buf = NULL;
	uint16_t buflen = 0;
	int32_t ret = openusb_get_raw_desc((openusb_handle_t)handle, (openusb_devid_t)devid, (uint8_t)type, (uint8_t)descidx, (uint16_t)langid, &buf, &buflen);
	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_get_raw_desc(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}

	jbyteArray binary = jvm.env->NewByteArray(buflen);
	jvm.env->SetByteArrayRegion(binary, 0, buflen, (jbyte*)buf);
	openusb_free_raw_desc(buf);

	set_object_to_buffer(jvm, buffer, binary);
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _parse_desc
 * Signature: (BJJ[BBBBB[Lorg/koiroha/usb/desc/Descriptor;)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1parse_1desc
  (JNIEnv *env, jclass, jbyte type, jlong handle, jlong devid, jbyteArray buffer, jbyte cfgidx, jbyte ifcidx, jbyte alt, jbyte eptidx, jobjectArray desc)
{
	JVM jvm(env);

	// retrieve raw binary buffer
	uint8_t* buf = NULL;
	uint16_t buflen = 0;
	if(buffer != NULL){
		buf = (uint8_t*)jvm.env->GetByteArrayElements(buffer, NULL);
		buflen = jvm.env->GetArrayLength(buffer);
	}

	int32_t ret = OPENUSB_SUCCESS;
	jobject value = NULL;
	switch(type){
	case USB_DESC_TYPE_DEVICE:
		TRACE("parse_device_desc(%p,%p,buf)", (void*)handle, (void*)devid);
		usb_device_desc_t devdesc;
		ret = openusb_parse_device_desc((openusb_handle_t)handle, (openusb_devid_t)devid, buf, buflen, &devdesc);
		value = create_device_descriptor(jvm, devdesc);
		break;
	case USB_DESC_TYPE_CONFIG:
		TRACE("parse_config_desc(%p,%p,buf,%d)", (void*)handle, (void*)devid, (int)cfgidx & 0xFF);
		usb_config_desc_t cfgdesc;
		ret = openusb_parse_config_desc((openusb_handle_t)handle, (openusb_devid_t)devid, buf, buflen, (uint8_t)cfgidx, &cfgdesc);
		value = create_configuration_descriptor(jvm, cfgdesc);
		break;
	case USB_DESC_TYPE_INTERFACE:
		TRACE("parse_interface_desc(%p,%p,buf,%d,%d,%d)", (void*)handle, (void*)devid, (int)cfgidx & 0xFF, (int)ifcidx & 0xFF, (int)alt & 0xFF);
		usb_interface_desc_t ifcdesc;
		ret = openusb_parse_interface_desc((openusb_handle_t)handle, (openusb_devid_t)devid, buf, buflen, (uint8_t)cfgidx, (uint8_t)ifcidx, (uint8_t)alt, &ifcdesc);
		value = create_interface_descriptor(jvm, ifcdesc);
		break;
	case USB_DESC_TYPE_ENDPOINT:
		TRACE("parse_endpoint_desc(%p,%p,buf,%d,%d,%d)", (void*)handle, (void*)devid, (int)cfgidx & 0xFF, (int)ifcidx & 0xFF, (int)alt & 0xFF);
		usb_endpoint_desc_t eptdesc;
		ret = openusb_parse_endpoint_desc((openusb_handle_t)handle, (openusb_devid_t)devid, buf, buflen, (uint8_t)cfgidx, (uint8_t)ifcidx, (uint8_t)alt, (uint8_t)eptidx, &eptdesc);
		value = create_endpoint_descriptor(jvm, eptdesc);
		break;
	default:
		jvm.abort("unexpected parse_desc() mode: %d", (int)type & 0xFF);
		break;
	}

	// release binary buffer
	if(buffer != NULL){
		jvm.env->ReleaseByteArrayElements(buffer, (jbyte*)buf, JNI_ABORT);
	}

	if(ret != OPENUSB_SUCCESS){
		TRACE("openusb_parse_config_desc(): %d: %s", ret, openusb_strerror(ret));
		return ret;
	}

	set_object_to_buffer(jvm, desc, value);
	return ret;
}




/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    strerror
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB_strerror
  (JNIEnv *env, jclass, jint error)
{
	JVM jvm(env);
	return jvm.nativeToString(openusb_strerror((int32_t)error));
}


// ##########################################################################

/*
 * Set reference pointer to specified ref object.
 */
static void set_reference(JVM& jvm, jobject reference, void* ref, jobject value){
	jvm.setLong(reference, CLS_REF, "ref", (jlong)ref);
	jvm.setObject(reference, CLS_REF, "value", "L" OBJECT_CLASS ";", value);
	return;
}

/*
 * Get reference pointer from specified ref object.
 */
static void* get_reference(JVM& jvm, jobject reference){
	return (void*)jvm.getLong(reference, CLS_REF, "ref");
}

/*
 * Create devid_array.
 */
static void make_devid_array(JVM& jvm, jobject devids, openusb_devid_t* buf, uint32_t num_devids){
	TRACE("make_devid_array(%p,%p,%d)", (void*)devids, (void*)buf, (int)num_devids);

	// create busid array
	jlongArray refsArray = jvm.env->NewLongArray(num_devids);
	if(refsArray == NULL){
		return;
	}
	jlong* refs = jvm.env->GetLongArrayElements(refsArray, NULL);
	for(uint32_t i=0; i<num_devids; i++){
		refs[i] = buf[i];
		TRACE("    devids[%d] %p", (int)i, (void*)buf[i]);
	}
	jvm.env->ReleaseLongArrayElements(refsArray, refs, 0);

	// set reference to ref object to free after
	set_reference(jvm, devids, buf, refsArray);
	return;
}

/*
 * Event callback function.
 */
static void event_callback(openusb_handle_t handle, openusb_devid_t devid, openusb_event_t event, void *arg){

	// find JNIEnv attached to current thread
	JVMCallbackEnv ce;
	if(ce.env == NULL){
		return;
	}
	JVM jvm(ce.env);

	// execute event callback
	jvm.callStatic(CLS_OPENUSB, "_event_callback", "(JJI)V", (jlong)handle, (jlong)devid, (jint)event);

	return;
}

/*
 * Debug callback function. All of debug messages are passed to JavaVM.
 */
static void debug_callback(openusb_handle_t handle, const char *fmt, va_list args){

	// find JNIEnv attached to current thread
	JVMCallbackEnv ce;
	if(ce.env == NULL){
		return;
	}
	JVM jvm(ce.env);

	// format debug message
	char buffer[2 * 1024];
	safe_vsnprintf(buffer, sizeof(buffer), fmt, args);
	jstring msg = jvm.nativeToString(buffer);

	// callback debug message
	jvm.callStatic(CLS_OPENUSB, "_debug_callback", "(JLjava/lang/String;)V", (jlong)handle, msg);

	return;
}
