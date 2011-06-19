/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: usb4java.h,v 1.6 2009/05/16 05:24:28 torao Exp $
 *
 * **************************************************************************
 * This shared file is used in various environment, windows, macintosh or
 * else. So, this must be us-ascii encoding.
 * **************************************************************************
 *
 *
*/
#ifndef __USB4JAVA_H__
#define __USB4JAVA_H__

#include <stdarg.h>
#include <stdlib.h>
#include <assert.h>
#include <jni.h>

namespace usb4j {

#if 0  // reflection level debugging
#define REFDEBUG(...) { fprintf(stderr, "REFDEBUG: "); fprintf(stderr, __VA_ARGS__); fprintf(stderr, "\n");}
#else
#define REFDEBUG(...) /* */;
#endif

// return which bigger.
inline int bigger(int a, int b){
	return (a > b)? a: b;
}

// safe version of vsnprintf. this trancate overflow message.
inline void safe_vsnprintf(char* buf, size_t len, const char* fmt, va_list list){
#if defined(_MSC_VER) && _MSC_VER >= 1500
	vsnprintf_s(buf, len, _TRUNCATE, fmt, list);
#else
	vsnprintf(buf, len, fmt, list);
	buf[len - 1] = '\0';
#endif
	return;
}

// safe version of snprintf. this trancate overflow message.
inline void safe_snprintf(char* buf, size_t len, const char* fmt, ...){
	va_list list;
	va_start(list, fmt);
	safe_vsnprintf(buf, len, fmt, list);
	va_end(list);
	return;
}

// ==========================================================================
// Verbose Trace Macro
// ==========================================================================
/*
 * If the compiler supports C99 variable arguments, strip while of trace
 * arguments.
 */

#if ! defined(__NOTRACE)
#define TRACE(...) jvm.log(LOG_FINEST, __VA_ARGS__)
#else
#define TRACE(...) /* */;
#endif

#if defined(__DUMP)
#define DUMP(...) JVM::dump(__VA_ARGS__)
#else
#define DUMP(...) /* */;
#endif

#if defined(_DEBUG)
#define ASSERT(n) assert(n)
#else
#define ASSERT(n) /* */;
#endif

// ==========================================================================
// Class Name
// ==========================================================================
/*
 * The class name that supply java core library and utility functions.
 */
#define OUT_OF_MEMORY_ERROR_CLASS "java/lang/OutOfMemoryError"
#define ILLEGAL_ARGUMENT_EXCEPTION_CLASS "java/lang/IllegalArgumentException"
#define UNSUPPORTED_OPERATION_EXCEPTION  "java/lang/UnsupportedOperationException"
#define OBJECT_CLASS              "java/lang/Object"
#define STRING_CLASS              "java/lang/String"
#define LONG_CLASS                "java/lang/Long"
#define INTEGER_CLASS             "java/lang/Integer"
#define LIST_CLASS                "java/util/List"
#define ARRAYLIST_CLASS           "java/util/ArrayList"
#define LOGGER_CLASS              "java/util/logging/Logger"
#define LEVEL_CLASS               "java/util/logging/Level"
#define BYTEBUFFER_CLASS          "java/nio/ByteBuffer"
#define BYTEORDER_CLASS           "java/nio/ByteOrder"

#define USB_CLASSNAME(n)          "org/koiroha/usb/" #n
#define USB_DEVICEDESC_CLASS      USB_CLASSNAME(desc/DeviceDescriptor)
#define USB_CONFIGURATIONDESC_CLASS USB_CLASSNAME(desc/ConfigurationDescriptor)
#define USB_INTERFACEDESC_CLASS   USB_CLASSNAME(desc/InterfaceDescriptor)
#define USB_ENDPOINTDESC_CLASS    USB_CLASSNAME(desc/EndpointDescriptor)
#define USB_DEVICE_CLASS          USB_CLASSNAME(impl/DeviceImpl)
#define USB_CONFIGURATION_CLASS   USB_CLASSNAME(impl/ConfigurationImpl)
#define USB_INTERFACE_CLASS       USB_CLASSNAME(impl/InterfaceImpl)
#define USB_ENDPOINT_CLASS        USB_CLASSNAME(impl/EndpointImpl)
#define USB_ISOCHED_CLASS         USB_CLASSNAME(impl/IsochEventDispatcher)
#define USB_DEVICE_INTERFACE      USB_CLASSNAME(Device)
#define USB_CONFIGURATION_INTERFACE USB_CLASSNAME(Configuration)
#define USB_INTERFACE_INTERFACE   USB_CLASSNAME(Interface)
#define USB_RESOURCEHANDLER_CLASS USB_CLASSNAME(ResourceHandler)
#define USB_BUSY_EXCEPTION_CLASS  USB_CLASSNAME(ResourceBusyException)
#define USB_NSIF_EXCEPTION_CLASS  USB_CLASSNAME(NoSuchInterfaceException)
#define USB_NOTOPEN_EX_CLASS      USB_CLASSNAME(NotOpenException)
#define USB_ISOCHNOSENT_EX_CLASS  USB_CLASSNAME(IsochNoSentException)

#define DEFAULT_EXCEPTION_CLASS   USB_CLASSNAME(USBException)

// ==========================================================================
// Logging Level
// ==========================================================================
/*
 * The logging level number that defined in Java Logging API.
 */
#define LOG_SEVERE  "SEVERE"	// 1000
#define LOG_WARNING "WARNING"	// 900
#define LOG_INFO    "INFO"		// 800
#define LOG_CONFIG  "CONFIG"	// 700
#define LOG_FINE    "FINE"		// 500
#define LOG_FINER   "FINER"		// 400
#define LOG_FINEST  "FINEST"	// 300

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Utility Class for JNI
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This class is useful utility for the library that implements JNI.
//
class JVM{
private:
	char *buffer;			// internal buffer as working area format messages.
	size_t buffer_size;		// length of internal buffer.

public:
	JNIEnv *env;			// JNI environment pointer passed from JavaVM.

public:

	// ======================================================================
	// Constructor
	// ======================================================================
	// constructor needs JNI environment pointer.
	//
	JVM(JNIEnv *env){
		this->env = env;
		this->buffer = NULL;
		this->buffer_size = 0;
		return;
	}

	// ======================================================================
	// Destructor
	// ======================================================================
	// release all internal resources.
	//
	virtual ~JVM(){
		if(this->buffer != NULL){
			free(this->buffer);
		}
		return;
	}

	// ======================================================================
	// Create Instance
	// ======================================================================
	// Create new instance of specified class using constructor of signature.
	//
	jobject create(const char *className, const char *sig, ...);

	// ======================================================================
	// Get Field
	// ======================================================================
	// Refer the 8bit integer value from specified field.
	//
	inline jbyte getByte(jobject obj, const char *className, const char *fieldName){
		REFDEBUG("getByte(%p,%s,%s)", (void*)obj, className, fieldName);
		jfieldID field = getField(className, fieldName, "B", false);
		return env->GetByteField(obj, field);
	}

	// ======================================================================
	// Get Field
	// ======================================================================
	// Refer the 16bit integer value from specified field.
	//
	inline jshort getShort(jobject obj, const char *className, const char *fieldName){
		REFDEBUG("getShort(%p,%s,%s)", (void*)obj, className, fieldName);
		jfieldID field = getField(className, fieldName, "S", false);
		return env->GetShortField(obj, field);
	}

	// ======================================================================
	// Get Field
	// ======================================================================
	// Refer the 32bit integer value from specified field.
	//
	inline jint getInt(jobject obj, const char *className, const char *fieldName){
		REFDEBUG("getInt(%p,%s,%s)", (void*)obj, className, fieldName);
		jfieldID field = getField(className, fieldName, "I", false);
		return env->GetIntField(obj, field);
	}

	// ======================================================================
	// Get Field
	// ======================================================================
	// Refer the 64bit integer value from specified field.
	//
	inline jlong getLong(jobject obj, const char *className, const char *fieldName){
		REFDEBUG("getLong(%p,%s,%s)", (void*)obj, className, fieldName);
		jfieldID field = getField(className, fieldName, "J", false);
		return env->GetLongField(obj, field);
	}

	// ======================================================================
	// Get Field
	// ======================================================================
	// Refer the byte array value from specified field.
	//
	inline jbyteArray getByteArray(jobject obj, const char *className, const char *fieldName){
		REFDEBUG("getByteArray(%p,%s,%s)", (void*)obj, className, fieldName);
		return (jbyteArray)getObject(obj, className, "[B", fieldName);
	}

	// ======================================================================
	// Get Field
	// ======================================================================
	// Refer the object value from specified field.
	//
	inline jobject getObject(jobject obj, const char *className, const char* sig, const char *fieldName){
		REFDEBUG("getShort(%p,%s,%s)", (void*)obj, className, fieldName);
		jfieldID field = getField(className, fieldName, sig, false);
		return env->GetObjectField(obj, field);
	}

	// ======================================================================
	// Get Static Field
	// ======================================================================
	// Refer the object value from specified static field.
	//
	inline jobject getStaticObject(const char *className, const char *sig, const char *fieldName){
		REFDEBUG("getStaticObject(%s,%s)", className, fieldName);
		jclass clazz = getClass(className);
		jfieldID field = getField(className, fieldName, sig, true);
		return env->GetStaticObjectField(clazz, field);
	}

	// ======================================================================
	// Set Field
	// ======================================================================
	// Set the 8bit integer value to specified field.
	//
	inline void setByte(jobject obj, const char *className, const char *fieldName, jbyte value){
		REFDEBUG("setByte(%p,%s,%s,%d)", (void*)obj, className, fieldName, (int)value);
		jfieldID field = getField(className, fieldName, "B", false);
		env->SetByteField(obj, field, value);
		return;
	}

	// ======================================================================
	// Set Field
	// ======================================================================
	// Set the 16bit integer value to specified field.
	//
	inline void setShort(jobject obj, const char *className, const char *fieldName, jshort value){
		REFDEBUG("setShort(%p,%s,%s,%d)", (void*)obj, className, fieldName, (int)value);
		jfieldID field = getField(className, fieldName, "S", false);
		env->SetShortField(obj, field, value);
		return;
	}

	// ======================================================================
	// Set Field
	// ======================================================================
	// Set the 32bit integer value to specified field.
	//
	inline void setInt(jobject obj, const char *className, const char *fieldName, jint value){
		REFDEBUG("setInt(%p,%s,%s,%d)", (void*)obj, className, fieldName, (int)value);
		jfieldID field = getField(className, fieldName, "I", false);
		env->SetIntField(obj, field, value);
		return;
	}

	// ======================================================================
	// Set Field
	// ======================================================================
	// Set the 64bit integer value to specified field.
	//
	inline void setLong(jobject obj, const char *className, const char *fieldName, jlong value){
		REFDEBUG("setLong(%p,%s,%s,%lld)", (void*)obj, className, fieldName, (long long)value);
		jfieldID field = getField(className, fieldName, "J", false);
		env->SetLongField(obj, field, value);
		return;
	}

	// ======================================================================
	// Set Field
	// ======================================================================
	// Set the string value to specified field.
	//
	inline void setString(jobject obj, const char *className, const char *fieldName, const char* str){
		jstring s = nativeToString(str);
		setString(obj, className, fieldName, s);
		return;
	}

	// ======================================================================
	// Set Field
	// ======================================================================
	// Set the string value to specified field.
	//
	inline void setString(jobject obj, const char *className, const char *fieldName, jstring value){
		setObject(obj, className, fieldName, "L" STRING_CLASS ";", value);
		return;
	}

	// ======================================================================
	// Set Field
	// ======================================================================
	// Set the object value to specified field.
	//
	inline void setObject(jobject obj, const char *className, const char *fieldName, const char* sig, jobject value){
		jfieldID field = getField(className, fieldName, sig, false);
		env->SetObjectField(obj, field, value);
		return;
	}

	// ======================================================================
	// Call Instance Method
	// ======================================================================
	// Call specified method with variable-arguments and return result.
	//
	inline jobject call(jobject obj, const char *className, const char *methodName, const char *sig, ...){
		REFDEBUG("call(%p,%s,%s,%s,...)", (void*)obj, className, methodName, sig);
		jmethodID method = getMethod(className, methodName, sig, false);
		va_list list;
		va_start(list, sig);
		jobject ret = env->CallObjectMethodV(obj, method, list);
		va_end(list);
		return ret;
	}

	// ======================================================================
	// Call Instance Method
	// ======================================================================
	// Call specified method with variable-arguments and return int result.
	//
	inline jint callInt(jobject obj, const char *className, const char *methodName, const char *sig, ...){
		REFDEBUG("callInt(%p,%s,%s,%s,...)", (void*)obj, className, methodName, sig);
		jmethodID method = getMethod(className, methodName, sig, false);
		va_list list;
		va_start(list, sig);
		jint ret = env->CallIntMethodV(obj, method, list);
		va_end(list);
		return ret;
	}

	// ======================================================================
	// Call Instance Method
	// ======================================================================
	// Call specified method with variable-arguments and return long result.
	//
	inline jlong callLong(jobject obj, const char *className, const char *methodName, const char *sig, ...){
		REFDEBUG("callLong(%p,%s,%s,%s,...)", (void*)obj, className, methodName, sig);
		jmethodID method = getMethod(className, methodName, sig, false);
		va_list list;
		va_start(list, sig);
		jlong ret =env->CallLongMethodV(obj, method, list);
		va_end(list);
		return ret;
	}

	// ======================================================================
	// Call Instance Method
	// ======================================================================
	// Call specified static method with variable-arguments and return result.
	//
	inline jobject callStatic(const char *className, const char *methodName, const char *sig, ...){
		REFDEBUG("callStatic(%s,%s,%s,...)", className, methodName, sig);
		jclass clazz = getClass(className);
		jmethodID method = getMethod(className, methodName, sig, true);
		va_list list;
		va_start(list, sig);
		jobject ret =env->CallStaticObjectMethodV(clazz, method, list);
		va_end(list);
		return ret;
	}

	// ======================================================================
	// Raise Exception
	// ======================================================================
	// Raise default exception with no message.
	//
	inline void raise(){
		raise(DEFAULT_EXCEPTION_CLASS, "");
		return;
	}

	// ======================================================================
	// Raise Exception
	// ======================================================================
	// Raise default exception with specified message.
	//
	inline void raise(const char *message){
		raise(DEFAULT_EXCEPTION_CLASS, message);
		return;
	}

	// ======================================================================
	// Raise Exception
	// ======================================================================
	// Raise specified exception with message. The exception is really thrown
	// by JavaVM after finish this jni call.
	//
	void raise(const char *exceptionClassName, const char *message){
		if(message == NULL || message[0] == '\0'){
			raise(exceptionClassName, (jstring)NULL);
		} else {
			raise(exceptionClassName, nativeToString(message));
		}
		return;
	}

	// ======================================================================
	// Raise Exception
	// ======================================================================
	// Raise specified exception with message. The exception is really thrown
	// by JavaVM after finish this jni call.
	//
	void raise(const char *exceptionClassName, jstring message);

	// ======================================================================
	// Raise Exception
	// ======================================================================
	// Raise specified exception with formatted message.
	//
	inline void raiseef(const char *exceptionClassName, const char* fmt, ...){
		va_list list;
		va_start(list, fmt);
		jstring msg = format(fmt, list);
		raise(exceptionClassName, msg);
		va_end(list);
		return;
	}

	// ======================================================================
	// Raise Exception
	// ======================================================================
	// Raise default exception with formatted message.
	//
	inline void raisef(const char* fmt, ...){
		va_list list;
		va_start(list, fmt);
		jstring msg = format(fmt, list);
		raise(DEFAULT_EXCEPTION_CLASS, msg);
		va_end(list);
		return;
	}

	// ======================================================================
	// Exception Check
	// ======================================================================
	// Check exception raised in this thread.
	//
	inline bool isError(){
		return (env->ExceptionCheck() != JNI_FALSE);
	}

public:

	// ======================================================================
	// Get Work Buffer
	// ======================================================================
	// Refer internal buffer for working such as to format message. Note that
	// this buffer is single per instance.
	//
	char* getBuffer(size_t size){
		if(buffer_size >= size){
			return buffer;
		}
		if(buffer != NULL){
			free(buffer);
		}
		buffer = (char*)malloc(size);
		buffer_size = size;
		return buffer;
	}

public:

	// ======================================================================
	// To Unicode String
	// ======================================================================
	// Convert specified native characters to jstring.
	//
	jstring nativeToString(const char *text);

private:

	// ======================================================================
	// Format Message
	// ======================================================================
	// Format specified argument like vsprintf().
	//
	jstring format(const char* format, va_list list);

public:

	// ######################################################################
	// DIAGNOSTIC FUNCTIONS
	// ######################################################################

	// ======================================================================
	// Output Log
	// ======================================================================
	// Output specified log message to the Logging API of Java.
	//
	void log(const char *level, const char *format, ...);

	// ======================================================================
	// Output Log
	// ======================================================================
	// Output specified log message to the Logging API of Java.
	//
	void log(const char *level, jstring msg);

	// ======================================================================
	// Abort Execution
	// ======================================================================
	// Abort program execution with formatted message.
	//
	void abort(const char *fmt, ...){
		if(env->ExceptionCheck()){
			env->ExceptionDescribe();
		}
		char buffer[2 * 1024];
		va_list list;
		va_start(list, fmt);
		safe_vsnprintf(buffer, sizeof(buffer), fmt, list);
		va_end(list);
		env->FatalError(buffer);
		return;
	}

#if defined(__DUMP)
	// ======================================================================
	// Dump Binary
	// ======================================================================
	// Dump specified binary data to file named "usb4jdump.log" on current
	// directory. You may use DUMP() macro instead of this method directly.
	//
	static void dump(const void *binary, size_t length, const char *format, ...);
#endif

private:

	// ======================================================================
	// Refer Class
	// ======================================================================
	// Refer the specified class.
	//
	jclass getClass(const char* className);

	// ======================================================================
	// Refer Field
	// ======================================================================
	// Refer the specified field.
	//
	jfieldID getField(const char* className, const char* fieldName, const char* sig, bool stat);

	// ======================================================================
	// Refer Method
	// ======================================================================
	// Refer the specified method.
	//
	jmethodID getMethod(const char* className, const char* methodName, const char* sig, bool stat);

};


// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// JNIEnv attach/detouch
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// JNIEnv scope closer for callback functions.
//
class JVMCallbackEnv{
private:
	JavaVM* javavm;

public:
	JNIEnv* env;

public:

	// ======================================================================
	// Constructor
	// ======================================================================
	// Set JNIEnv pointer to env. If no environment attached to current thread,
	// find JavaVM and attach. If error, set env to NULL.
	//
	JVMCallbackEnv();

	// ======================================================================
	// Destructor
	// ======================================================================
	// Detach if constructor attach java environment.
	//
	virtual ~JVMCallbackEnv();

};

// ##########################################################################
// Functions Prototype
// ##########################################################################

// ==========================================================================
// Add element to specified java.util.List object.
inline void add_list(JVM& jvm, jobject list, jobject obj){
	jvm.call(list, LIST_CLASS, "add", "(L" OBJECT_CLASS ";)Z", obj);
	return;
}

// ==========================================================================
// Create ByteBuffer instance with specified length.
inline jobject create_byte_buffer(JVM& jvm, size_t length){
	jobject bytebuffer = jvm.callStatic(BYTEBUFFER_CLASS, "allocate", "(I)L" BYTEBUFFER_CLASS ";", (jint)length);
	jobject order = jvm.getStaticObject(BYTEORDER_CLASS, "L" BYTEORDER_CLASS ";", "LITTLE_ENDIAN");
	jvm.call(bytebuffer, BYTEBUFFER_CLASS, "order", "(L" BYTEORDER_CLASS ";)L" BYTEBUFFER_CLASS ";", order);
	return bytebuffer;
}

// ==========================================================================
// Create ByteBuffer instance that wrap specified binary.
inline jobject create_byte_buffer(JVM& jvm, const void* buffer, size_t length){
	jbyteArray array = jvm.env->NewByteArray(length);
	jvm.env->SetByteArrayRegion(array, 0, length, (jbyte*)buffer);
	jobject bytebuffer = jvm.callStatic(BYTEBUFFER_CLASS, "wrap", "([B)L" BYTEBUFFER_CLASS ";", array);
	return bytebuffer;
}

// ==========================================================================
// Call ByteBuffer#put(byte).
inline void put_byte(JVM& jvm, jobject bytebuffer, jbyte value){
	jvm.call(bytebuffer, BYTEBUFFER_CLASS, "put", "(B)L" BYTEBUFFER_CLASS ";", value);
	return;
}

// ==========================================================================
// Call ByteBuffer#putShort(short).
inline void put_short(JVM& jvm, jobject bytebuffer, jshort value){
	jvm.call(bytebuffer, BYTEBUFFER_CLASS, "putShort", "(S)L" BYTEBUFFER_CLASS ";", value);
	return;
}

// ==========================================================================
// Create device descriptor from platform-depend structure.
template<class T> inline jobject create_device_descriptor(JVM& jvm, const T& desc){
	jobject buffer = create_byte_buffer(jvm, bigger(desc.bLength, 0x12));
	put_byte(jvm, buffer, desc.bLength);
	put_byte(jvm, buffer, desc.bDescriptorType);
	put_short(jvm, buffer, desc.bcdUSB);
	put_byte(jvm, buffer, desc.bDeviceClass);
	put_byte(jvm, buffer, desc.bDeviceSubClass);
	put_byte(jvm, buffer, desc.bDeviceProtocol);
	put_byte(jvm, buffer, desc.bMaxPacketSize0);
	put_short(jvm, buffer, desc.idVendor);
	put_short(jvm, buffer, desc.idProduct);
	put_short(jvm, buffer, desc.bcdDevice);
	put_byte(jvm, buffer, desc.iManufacturer);
	put_byte(jvm, buffer, desc.iProduct);
	put_byte(jvm, buffer, desc.iSerialNumber);
	put_byte(jvm, buffer, desc.bNumConfigurations);
	jvm.call(buffer, BYTEBUFFER_CLASS, "position", "(I)Ljava/nio/Buffer;", (jint)0);
	return jvm.create(USB_DEVICEDESC_CLASS, "(L" BYTEBUFFER_CLASS ";)V", buffer);
}

// ==========================================================================
// Create configuration descriptor from platform-depend structure.
template<class T> inline jobject create_configuration_descriptor(JVM& jvm, const T& desc){
	jobject buffer = create_byte_buffer(jvm, bigger(desc.bLength, 0x09));
	put_byte(jvm, buffer, desc.bLength);
	put_byte(jvm, buffer, desc.bDescriptorType);
	put_short(jvm, buffer, desc.wTotalLength);
	put_byte(jvm, buffer, desc.bNumInterfaces);
	put_byte(jvm, buffer, desc.bConfigurationValue);
	put_byte(jvm, buffer, desc.iConfiguration);
	put_byte(jvm, buffer, desc.bmAttributes);

	// either MaxPower or bMaxPower?
#ifdef CONFDESC_MAXPOWER
	put_byte(jvm, buffer, desc.CONFDESC_MAXPOWER);
#else
	put_byte(jvm, buffer, desc.MaxPower);
#endif

	jvm.call(buffer, BYTEBUFFER_CLASS, "position", "(I)Ljava/nio/Buffer;", (jint)0);
	return jvm.create(USB_CONFIGURATIONDESC_CLASS, "(L" BYTEBUFFER_CLASS ";)V", buffer);
}

// ==========================================================================
// Create interface descriptor from platform-depend structure.
template<class T> inline jobject create_interface_descriptor(JVM& jvm, const T& desc){
	jobject buffer = create_byte_buffer(jvm, bigger(desc.bLength, 0x09));
	put_byte(jvm, buffer, desc.bLength);
	put_byte(jvm, buffer, desc.bDescriptorType);
	put_byte(jvm, buffer, desc.bInterfaceNumber);
	put_byte(jvm, buffer, desc.bAlternateSetting);
	put_byte(jvm, buffer, desc.bNumEndpoints);
	put_byte(jvm, buffer, desc.bInterfaceClass);
	put_byte(jvm, buffer, desc.bInterfaceSubClass);
	put_byte(jvm, buffer, desc.bInterfaceProtocol);
	put_byte(jvm, buffer, desc.iInterface);
	jvm.call(buffer, BYTEBUFFER_CLASS, "position", "(I)Ljava/nio/Buffer;", (jint)0);
	return jvm.create(USB_INTERFACEDESC_CLASS, "(L" BYTEBUFFER_CLASS ";)V", buffer);
}

// ==========================================================================
// Create endpoint descriptor from platform-depend structure.
template<class T> inline jobject create_endpoint_descriptor(JVM& jvm, const T& desc, unsigned char* extra = NULL, int extralen = 0){
	int length = bigger(desc.bLength, 0x07);
	jobject buffer = create_byte_buffer(jvm, length + extralen);
	put_byte(jvm, buffer, desc.bLength);
	put_byte(jvm, buffer, desc.bDescriptorType);
	put_byte(jvm, buffer, desc.bEndpointAddress);
	put_byte(jvm, buffer, desc.bmAttributes);
	put_short(jvm, buffer, desc.wMaxPacketSize);
	put_byte(jvm, buffer, desc.bInterval);
	if(desc.bLength >= 0x08){
		put_byte(jvm, buffer, desc.bRefresh);
	}
	if(desc.bLength >= 0x09){
		put_byte(jvm, buffer, desc.bSynchAddress);
	}
	for(int i=0; i<extralen; i++){
		put_byte(jvm, buffer, extra[i]);
	}
	jvm.call(buffer, BYTEBUFFER_CLASS, "position", "(I)Ljava/nio/Buffer;", (jint)0);
	return jvm.create(USB_ENDPOINTDESC_CLASS, "(L" BYTEBUFFER_CLASS ";)V", buffer);
}

// ==========================================================================
// Create string from platform-depend string descriptor structure.
template<class T> inline jstring create_string_from_descriptor(JVM& jvm, const T& str_desc){
	jstring encoding = jvm.env->NewStringUTF("UnicodeLittle");
	if(encoding == NULL){
		return NULL;
	}
	size_t length = str_desc.bLength - 2;
	jbyteArray bytes = jvm.env->NewByteArray(length);
	jvm.env->SetByteArrayRegion(bytes, 0, length, (jbyte*)(str_desc.bString));
	jstring str = (jstring)jvm.create(STRING_CLASS, "([BL" STRING_CLASS ";)V", bytes, encoding);
	return str;
}

// ==========================================================================
// set object value to the first of specified return buffer.
inline void set_object_to_buffer(JVM& jvm, jobjectArray array, jobject value){
	jvm.env->SetObjectArrayElement(array, 0, value);
	return;
}

// ==========================================================================
// set int64_t value to the first of specified return buffer.
inline void set_long_to_buffer(JVM& jvm, jlongArray array, jlong value){
	jlong* buffer = jvm.env->GetLongArrayElements(array, NULL);
	buffer[0] = value;
	jvm.env->ReleaseLongArrayElements(array, buffer, 0);
	return;
}

// ==========================================================================
// set int32_t value to the first of specified return buffer.
inline void set_int_to_buffer(JVM& jvm, jintArray array, jint value){
	jint* buffer = jvm.env->GetIntArrayElements(array, NULL);
	buffer[0] = value;
	jvm.env->ReleaseIntArrayElements(array, buffer, 0);
	return;
}

// ==========================================================================
// set int8_t value to the first of specified return buffer.
inline void set_byte_to_buffer(JVM& jvm, jbyteArray array, jbyte value){
	jbyte* buffer = jvm.env->GetByteArrayElements(array, NULL);
	buffer[0] = value;
	jvm.env->ReleaseByteArrayElements(array, buffer, 0);
	return;
}

// ==========================================================================
// Initialize usb4j common library.
extern jlong initialize(JNIEnv *env);

// ==========================================================================
// Get JNIEnv pointer associate with current thread. If no JavaVM attached,
// find JavaVM and attach to current thread. If specified javavm parameter
// set to valid address, it specifies attach occured and caller must detouch
// it.
extern JNIEnv* get_jnienv(JavaVM** javavm);

// ==========================================================================
// Convert byte order to little endian.
extern unsigned short native_to_le16(unsigned short value);
inline unsigned short le16_to_native(unsigned short value){
	return native_to_le16(value);
}


// ==========================================================================
// Module Definition
// ==========================================================================
/*
 */
extern const char*    LOGGER_NAME;		// logger name of library for Java Logging API
extern const char*    LIBRARY_NAME;		// library name
extern unsigned short LIBRARY_VERSION;	// library version x256
#define DECLARE_USB_LIBRARY(id,name,version) \
	const char*    usb4j::LOGGER_NAME = "org.koiroha.usb." id;\
	const char*    usb4j::LIBRARY_NAME = name;\
	unsigned short usb4j::LIBRARY_VERSION = (version)

}
#endif
