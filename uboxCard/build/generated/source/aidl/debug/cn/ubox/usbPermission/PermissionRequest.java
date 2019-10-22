/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\UboxCard-hkkj\\uboxCard\\src\\main\\aidl\\cn\\ubox\\usbPermission\\PermissionRequest.aidl
 */
package cn.ubox.usbPermission;
//import android.hardware.usb.UsbDevice;
/**
  * Created by shenran on 2015/4/27.
  */
public interface PermissionRequest extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements cn.ubox.usbPermission.PermissionRequest
{
private static final java.lang.String DESCRIPTOR = "cn.ubox.usbPermission.PermissionRequest";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an cn.ubox.usbPermission.PermissionRequest interface,
 * generating a proxy if needed.
 */
public static cn.ubox.usbPermission.PermissionRequest asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof cn.ubox.usbPermission.PermissionRequest))) {
return ((cn.ubox.usbPermission.PermissionRequest)iin);
}
return new cn.ubox.usbPermission.PermissionRequest.Stub.Proxy(obj);
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
case TRANSACTION_RequestUsbPermission:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
boolean _result = this.RequestUsbPermission(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_testBind:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.testBind();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements cn.ubox.usbPermission.PermissionRequest
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
@Override public boolean RequestUsbPermission(int VendorId, int ProductId, int uid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(VendorId);
_data.writeInt(ProductId);
_data.writeInt(uid);
mRemote.transact(Stub.TRANSACTION_RequestUsbPermission, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean testBind() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_testBind, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_RequestUsbPermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_testBind = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public boolean RequestUsbPermission(int VendorId, int ProductId, int uid) throws android.os.RemoteException;
public boolean testBind() throws android.os.RemoteException;
}
