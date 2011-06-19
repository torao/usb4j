/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: libusb0_jni_bridge.cpp,v 1.6 2009/05/18 11:02:22 torao Exp $
*/
#include <string.h>
#include <usb.h>
#include "usb4java.h"
#include "org_koiroha_usb_impl_libusb_LibUSB0.h"

DECLARE_USB_LIBRARY("libusb_0_1", "libusb 0.1", 0x0100);

#define CLS_BUS           "org/koiroha/usb/impl/libusb/LibUSB0$Bus"
#define CLS_DEVICE        "org/koiroha/usb/impl/libusb/LibUSB0$Device"
#define CLS_CONFIGURATION "org/koiroha/usb/impl/libusb/LibUSB0$Configuration"
#define CLS_INTERFACE     "org/koiroha/usb/impl/libusb/LibUSB0$Interface"
#define CLS_ALTSETTING    "org/koiroha/usb/impl/libusb/LibUSB0$AltSetting"
#define CLS_ENDPOINT      "org/koiroha/usb/impl/libusb/LibUSB0$Endpoint"

using namespace usb4j;

/*
 * Convert pointers from/to java 64bit integer. These is used to reduce compiler cast warning.
 */
inline void* jlong_to_pointer(jlong value){
	return reinterpret_cast<void*>(value);
}
inline jlong pointer_to_jlong(void* ptr){
	return reinterpret_cast<jlong>(ptr);
}
inline usb_dev_handle* jlong_to_devhandle(jlong handle){
	return (usb_dev_handle*)jlong_to_pointer(handle);
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    nativeInterfaceVersion
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_nativeInterfaceVersion
  (JNIEnv *env, jobject)
{
	return initialize(env);
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_init(JNIEnv *, jclass){
	usb_init();
	return;
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    set_debug
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_set_1debug(JNIEnv *, jclass, jint level){
	usb_set_debug(level);
	return;
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    find_busses
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_find_1busses(JNIEnv *, jclass){
	return usb_find_busses();
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    find_devices
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_find_1devices(JNIEnv *, jclass){
	return usb_find_devices();
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    strerror
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_strerror(JNIEnv *env, jclass){
	JVM jvm(env);
	char* msg = usb_strerror();
	return jvm.nativeToString(msg);
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    open
 * Signature: (Lorg/koiroha/usb/impl/libusb/LibUSB0$Device;)J
 */
JNIEXPORT jlong JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_open(JNIEnv *env, jclass, jobject device){
	JVM jvm(env);
	struct usb_device* dev = (struct usb_device*)jlong_to_pointer(jvm.getLong(device, CLS_DEVICE, "peer"));
	return pointer_to_jlong(usb_open(dev));
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    close
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_close(JNIEnv *, jclass, jlong handle){
	return usb_close(jlong_to_devhandle(handle));
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    get_string
 * Signature: (JII[B)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_get_1string(JNIEnv *env, jclass, jlong handle, jint index, jint langid, jbyteArray buf){
	JVM jvm(env);
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	int length = env->GetArrayLength(buf);
	jbyte* buffer = env->GetByteArrayElements(buf, NULL);
	int result = usb_get_string(dev, index, langid, (char*)buffer, length);
	DUMP(buffer, bigger(0, result), "<< usb_get_string(%p,0x%X,0x%X,buf,%d):=%d", dev, index, langid, length, result);
	env->ReleaseByteArrayElements(buf, buffer, 0);
	return result;
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    get_string_simple
 * Signature: (JI[B)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_get_1string_1simple(JNIEnv *env, jclass, jlong handle, jint index, jbyteArray buf){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	int length = env->GetArrayLength(buf);
	jbyte* buffer = (jbyte*)env->GetPrimitiveArrayCritical(buf, NULL);
	int result = usb_get_string_simple(dev, index, (char*)buffer, length);
	env->ReleasePrimitiveArrayCritical(buf, buffer, 0);
	return result;
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    get_descriptor_by_endpoint
 * Signature: (JIBB[B)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_get_1descriptor_1by_1endpoint(JNIEnv *env, jclass, jlong handle, jint ep, jbyte type, jbyte index, jbyteArray buf){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	int length = env->GetArrayLength(buf);
	jbyte* buffer = (jbyte*)env->GetByteArrayElements(buf, NULL);
	int result = usb_get_descriptor_by_endpoint(dev, ep, type & 0xFF, index & 0xFF, buffer, length);
	DUMP(buffer, bigger(0, result), "<< usb_get_descriptor_by_endpoint(%p,%d,0x%X,0x%X,buf,%d):=%d", dev, ep, type & 0xFF, index & 0xFF, length, result);
	env->ReleaseByteArrayElements(buf, buffer, 0);
	return result;
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    get_descriptor
 * Signature: (JBB[B)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_get_1descriptor(JNIEnv *env, jclass, jlong handle, jbyte type, jbyte index, jbyteArray buf){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	int length = env->GetArrayLength(buf);
	jbyte* buffer = (jbyte*)env->GetByteArrayElements(buf, NULL);
	int result = usb_get_descriptor(dev, type & 0xFF, index & 0xFF, buffer, length);
	DUMP(buffer, bigger(0, result), "<< usb_get_descriptor(%p,0x%X,0x%X,buf,%d):=%d", dev, type & 0xFF, index & 0xFF, length, result);
	env->ReleaseByteArrayElements(buf, buffer, 0);
	return result;
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    bulk_write
 * Signature: (JI[BIII)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_bulk_1write(JNIEnv *env, jclass, jlong handle, jint ep, jbyteArray buf, jint offset, jint length, jint timeout){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	jbyte* buffer = (jbyte*)env->GetByteArrayElements(buf, NULL);
	int result = usb_bulk_write(dev, ep, ((char*)buffer) + offset, length, timeout);
	DUMP(buffer, bigger(0, result), ">> usb_bulk_write(%p,%d,buf,%d,%d):=%d", dev, ep, length, timeout, result);
	env->ReleaseByteArrayElements(buf, buffer, JNI_ABORT);
	return result;
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    bulk_read
 * Signature: (JI[BIII)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_bulk_1read(JNIEnv *env, jclass, jlong handle, jint ep, jbyteArray buf, jint offset, jint length, jint timeout){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	jbyte* buffer = (jbyte*)env->GetByteArrayElements(buf, NULL);
	int result = usb_bulk_read(dev, ep, ((char*)buffer) + offset, length, timeout);
	DUMP(buffer, bigger(0, result), "<< usb_bulk_read(%p,%d,buf,%d,%d):=%d", dev, ep, length, timeout, result);
	env->ReleaseByteArrayElements(buf, buffer, 0);
	return result;
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    interrupt_write
 * Signature: (JI[BIII)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_interrupt_1write(JNIEnv *env, jclass, jlong handle, jint ep, jbyteArray buf, jint offset, jint length, jint timeout){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	jbyte* buffer = (jbyte*)env->GetByteArrayElements(buf, NULL);
	int result = usb_interrupt_write(dev, ep, ((char*)buffer) + offset, length, timeout);
	DUMP(buffer, bigger(0, result), ">> usb_interrupt_write(%p,%d,buf,%d,%d):=%d", dev, ep, length, timeout, result);
	env->ReleaseByteArrayElements(buf, buffer, JNI_ABORT);
	return result;
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    interrupt_read
 * Signature: (JI[BIII)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_interrupt_1read(JNIEnv *env, jclass, jlong handle, jint ep, jbyteArray buf, jint offset, jint length, jint timeout){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	jbyte* buffer = (jbyte*)env->GetByteArrayElements(buf, NULL);
	int result = usb_interrupt_read(dev, ep, ((char*)buffer) + offset, length, timeout);
	DUMP(buffer, bigger(0, result), "<< usb_interrupt_read(%p,%d,buf,%d,%d):=%d", dev, ep, length, timeout, result);
	env->ReleaseByteArrayElements(buf, buffer, 0);
	return result;
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    control_msg
 * Signature: (JIIII[BI)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_control_1msg(JNIEnv *env, jclass, jlong handle, jint requesttype, jint request, jint value, jint index, jbyteArray buf, jint timeout){
	JVM jvm(env);
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	int length = env->GetArrayLength(buf);
	jbyte* buffer = env->GetByteArrayElements(buf, NULL);
	int result = usb_control_msg(dev, requesttype, request, value, index, (char*)buffer, length, timeout);
	if((requesttype & USB_ENDPOINT_DIR_MASK) == USB_ENDPOINT_IN){
		DUMP(buffer, bigger(0, result), "<< control_msg(%p,0x%X,0x%X,0x%X,%d,buf,%d,%d):=%d", dev, requesttype, request, value, index, length, timeout, result);
		env->ReleaseByteArrayElements(buf, buffer, 0);
	} else {
		DUMP(buffer, length, ">> control_msg(%p,0x%X,0x%X,0x%X,%d,buf,%d,%d):=%d", dev, requesttype, request, value, index, length, timeout, result);
		env->ReleaseByteArrayElements(buf, buffer, JNI_ABORT);
	}
	return result;
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    set_configuration
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_set_1configuration(JNIEnv *, jclass, jlong handle, jint configuration){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	return usb_set_configuration(dev, configuration);
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    claim_interface
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_claim_1interface(JNIEnv *, jclass, jlong handle, jint intf){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	return usb_claim_interface(dev, intf);
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    release_interface
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_release_1interface(JNIEnv *, jclass, jlong handle, jint intf){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	return usb_release_interface(dev, intf);
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    set_altinterface
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_set_1altinterface(JNIEnv *, jclass, jlong handle, jint alternate){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	return usb_set_altinterface(dev, alternate);
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    resetep
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_resetep(JNIEnv *, jclass, jlong handle, jint ep){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	return usb_resetep(dev, ep);
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    clear_halt
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_clear_1halt(JNIEnv *, jclass, jlong handle, jint ep){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	return usb_clear_halt(dev, ep);
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    reset
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_reset(JNIEnv *, jclass, jlong handle){
	usb_dev_handle* dev = jlong_to_devhandle(handle);
	return usb_reset(dev);
}

// ##########################################################################
// BUS/DEVICE STRUCTURE BUILD
// ##########################################################################

#define COPY_FIELD(src,dst,field) (dst).field = (src).field

//
static jobject create_endpoint(JVM& jvm, struct usb_endpoint_descriptor* ptr){
	TRACE("create_endpoint(%p)", ptr);

	// create endpoint descriptor
	jobject descriptor = create_endpoint_descriptor(jvm, *ptr, ptr->extra, ptr->extralen);

	// create endpoint extra field
	jbyteArray extra = jvm.env->NewByteArray(ptr->extralen);
	if(jvm.isError()){
		return NULL;
	}
	jvm.env->SetByteArrayRegion(extra, 0, ptr->extralen, (jbyte*)(ptr->extra));

	// create endpoint structure
	jobject endpoint = jvm.create(CLS_ENDPOINT, "()V");
	jvm.setObject(endpoint, CLS_ENDPOINT, "descriptor", "L" USB_ENDPOINTDESC_CLASS ";", descriptor);
	jvm.setObject(endpoint, CLS_ENDPOINT, "extra", "[B", extra);
	return endpoint;
}

//
static jobject create_altsetting(JVM& jvm, struct usb_interface_descriptor* ptr){
	TRACE("create_altsetting(%p)", ptr);

	// craete interface descriptor
	jobject descriptor = create_interface_descriptor(jvm, *ptr);

	// build altsetting array
	jclass clazz = jvm.env->FindClass(CLS_ENDPOINT);
	jobjectArray endpoint = jvm.env->NewObjectArray(ptr->bNumEndpoints, clazz, NULL);
	for(int i=0; i<ptr->bNumEndpoints; i++){
		jobject edpt = create_endpoint(jvm, &(ptr->endpoint[i]));
		if(jvm.isError()){
			return NULL;
		}
		jvm.env->SetObjectArrayElement(endpoint, i, edpt);
	}

	// extra descriptors info
	jbyteArray extra = jvm.env->NewByteArray(ptr->extralen);
	jvm.env->SetByteArrayRegion(extra, 0, ptr->extralen, (jbyte*)(ptr->extra));

	// create altsetting structure
	jobject altsetting = jvm.create(CLS_ALTSETTING, "()V");
	jvm.setObject(altsetting, CLS_ALTSETTING, "descriptor", "L" USB_INTERFACEDESC_CLASS ";", descriptor);
	jvm.setObject(altsetting, CLS_ALTSETTING, "endpoint", "[L" CLS_ENDPOINT ";", endpoint);
	jvm.setObject(altsetting, CLS_ALTSETTING, "extra", "[B", extra);
	return altsetting;
}

//
static jobject create_interface(JVM& jvm, struct usb_interface* ptr){
	TRACE("create_interface(%p)", ptr);

	// alternate settings
	jclass clazz = jvm.env->FindClass(CLS_ALTSETTING);
	jobjectArray altsetting = jvm.env->NewObjectArray(ptr->num_altsetting, clazz, NULL);
	for(int i=0; i<ptr->num_altsetting; i++){
		jobject alt = create_altsetting(jvm, &(ptr->altsetting[i]));
		if(jvm.isError()){
			return NULL;
		}
		jvm.env->SetObjectArrayElement(altsetting, i, alt);
	}

	// create altsetting structure
	jobject intf = jvm.create(CLS_INTERFACE, "()V");
	jvm.setObject(intf, CLS_INTERFACE, "altsetting", "[L" CLS_ALTSETTING ";", altsetting);
	return intf;
}

//
static jobject create_configuration(JVM& jvm, struct usb_config_descriptor* ptr){
	TRACE("create_configuration(%p)", ptr);

	// craete configuration descriptor
	jobject descriptor = create_configuration_descriptor(jvm, *ptr);

	// interface
	int ifc_num = ptr->bNumInterfaces;
	jclass clazz = jvm.env->FindClass(CLS_INTERFACE);
	jobjectArray interface = jvm.env->NewObjectArray(ifc_num, clazz, NULL);
	for(int i=0; i<ifc_num; i++){
		jobject intf = create_interface(jvm, &(ptr->interface[i]));
		if(jvm.isError()){
			return NULL;
		}
		jvm.env->SetObjectArrayElement(interface, i, intf);
	}

	// extra descriptors info
	jbyteArray extra = jvm.env->NewByteArray(ptr->extralen);
	jvm.env->SetByteArrayRegion(extra, 0, ptr->extralen, (jbyte*)(ptr->extra));

	// create configuration structure
	jobject configuration = jvm.create(CLS_CONFIGURATION, "()V");
	jvm.setObject(configuration, CLS_CONFIGURATION, "descriptor", "L" USB_CONFIGURATIONDESC_CLASS ";", descriptor);
	jvm.setObject(configuration, CLS_CONFIGURATION, "interfaces", "[L" CLS_INTERFACE ";", interface);
	jvm.setObject(configuration, CLS_CONFIGURATION, "extra", "[B", extra);
	return configuration;
}

//
static jobject create_device(JVM& jvm, int iBus, int iDev, struct usb_device* ptr){
	TRACE("create_device(%p)", ptr);

	// create device descriptor
	jobject descriptor = create_device_descriptor(jvm, ptr->descriptor);
	if(jvm.isError()){
		return NULL;
	}

	// build configurations array
	int conf_num = ptr->descriptor.bNumConfigurations;
	jclass clazz = jvm.env->FindClass(CLS_CONFIGURATION);
	jobjectArray configurations = jvm.env->NewObjectArray(conf_num, clazz, NULL);
	for(int i=0; i<conf_num; i++){
		if(&(ptr->config[i]) != NULL){
			jobject conf = create_configuration(jvm, &(ptr->config[i]));
			if(jvm.isError()){
				return NULL;
			}
			jvm.env->SetObjectArrayElement(configurations, i, conf);
		} else {
			TRACE("configuration descriptor is null[%s][%d]", ptr->filename, i);
		}
	}

	// filename
	jstring filename = jvm.env->NewStringUTF(ptr->filename);
	TRACE("[%d][%d]: device=%s", iBus, iDev, ptr->filename);

	// children
	jlong* array = (jlong*)malloc(ptr->num_children);
	for(int i=0; i<ptr->num_children; i++){
		array[i] = pointer_to_jlong(ptr->children[i]);
	}
	jlongArray children = jvm.env->NewLongArray(ptr->num_children);
	jvm.env->SetLongArrayRegion(children, 0, ptr->num_children, array);
	free(array);
	array = NULL;

	// create configuration structure
	jobject device = jvm.create(CLS_DEVICE, "()V");
	jvm.setLong(device, CLS_DEVICE, "peer", pointer_to_jlong(ptr));
	jvm.setObject(device, CLS_DEVICE, "filename", "L" STRING_CLASS ";", filename);
	jvm.setObject(device, CLS_DEVICE, "descriptor", "L" USB_DEVICEDESC_CLASS ";", descriptor);
	jvm.setObject(device, CLS_DEVICE, "config", "[L" CLS_CONFIGURATION ";", configurations);
	jvm.setLong(device, CLS_DEVICE, "dev", pointer_to_jlong(ptr->dev));
	jvm.setByte(device, CLS_DEVICE, "devnum", (jbyte)(ptr->devnum & 0xFF));
	jvm.setObject(device, CLS_DEVICE, "children", "[J", children);
	return device;
}

//
static jobject create_bus(JVM& jvm, int iBus, struct usb_bus* ptr){
	TRACE("create_bus(%p)", ptr);
	jobject bus = jvm.create(CLS_BUS, "()V");

	// dirname
	jstring dirname = jvm.env->NewStringUTF(ptr->dirname);
	TRACE("[%d]: bus=%s", iBus, ptr->dirname);

	// build link-list for devices
	int count = 0;
	jobject first = NULL;
	jobject prev = NULL;
	struct usb_device* mover = ptr->devices;
	for(int i=0; mover != NULL; i++){

		// create device
		jobject dev = create_device(jvm, iBus, i, mover);
		if(jvm.isError()){
			return NULL;
		}
		count ++;

		// set parent bus and next/previous device
		jvm.setObject(dev, CLS_DEVICE, "bus", "L" CLS_BUS ";", bus);

		// set sibling link-list
		if(prev != NULL){
			jvm.setObject(prev, CLS_DEVICE, "next", "L" CLS_DEVICE ";", dev);
		}
		jvm.setObject(dev, CLS_DEVICE, "prev", "L" CLS_DEVICE ";", prev);
		if(first == NULL){
			first = dev;
		}

		// if current device seems to be root
		if(ptr->root_dev != NULL && strcmp(mover->filename, ptr->root_dev->filename) == 0){
			TRACE("root device found[%s]: %p", mover->filename, dev);
			jvm.setObject(bus, CLS_BUS, "root_dev", "L" CLS_DEVICE ";", dev);
		}

		// move next device
		prev = dev;
		mover = mover->next;
	}
	TRACE("%d devices found on bus[%d]: %s", count, iBus, ptr->dirname);

	// create configuration structure
	jvm.setObject(bus, CLS_BUS, "dirname", "L" STRING_CLASS ";", dirname);
	jvm.setObject(bus, CLS_BUS, "devices", "L" CLS_DEVICE ";", first);
	jvm.setInt(bus, CLS_BUS, "location", ptr->location);
	return bus;
}

/*
 * Class:     org_koiroha_usb_impl_libusb_LibUSB0
 * Method:    get_busses
 * Signature: ()Lorg/koiroha/usb/impl/libusb/LibUSB0$Bus;
 */
JNIEXPORT jobject JNICALL Java_org_koiroha_usb_impl_libusb_LibUSB0_get_1busses(JNIEnv *env, jclass){
	JVM jvm(env);

	struct usb_bus* bus = usb_get_busses();
	jobject first = NULL;

	// build link-list for busses
	jobject prev = NULL;
	struct usb_bus* mover = bus;
	for(int i=0; mover != NULL; i++){
		jobject bus = create_bus(jvm, i, mover);
		if(env->ExceptionCheck()){
			return NULL;
		}

		// set sibling link-list
		if(prev != NULL){
			jvm.setObject(prev, CLS_BUS, "next", "L" CLS_BUS ";", bus);

		}
		jvm.setObject(bus, CLS_BUS, "prev", "L" CLS_BUS ";", prev);
		if(first == NULL){
			first = bus;
		}

		prev = bus;
		mover = mover->next;
	}

	// return the first bus of list
	return first;
}
