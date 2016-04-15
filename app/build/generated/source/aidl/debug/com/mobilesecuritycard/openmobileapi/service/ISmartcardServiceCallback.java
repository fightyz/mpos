/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\workspace\\Android\\studio_workspace\\mpos\\app\\src\\main\\aidl\\com\\mobilesecuritycard\\openmobileapi\\service\\ISmartcardServiceCallback.aidl
 */
package com.mobilesecuritycard.openmobileapi.service;
/**
 * Callback interface used by ISmartcardService to check if clients are alive.
 */
public interface ISmartcardServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mobilesecuritycard.openmobileapi.service.ISmartcardServiceCallback
{
private static final java.lang.String DESCRIPTOR = "com.mobilesecuritycard.openmobileapi.service.ISmartcardServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mobilesecuritycard.openmobileapi.service.ISmartcardServiceCallback interface,
 * generating a proxy if needed.
 */
public static com.mobilesecuritycard.openmobileapi.service.ISmartcardServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mobilesecuritycard.openmobileapi.service.ISmartcardServiceCallback))) {
return ((com.mobilesecuritycard.openmobileapi.service.ISmartcardServiceCallback)iin);
}
return new com.mobilesecuritycard.openmobileapi.service.ISmartcardServiceCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mobilesecuritycard.openmobileapi.service.ISmartcardServiceCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
}
}
}
