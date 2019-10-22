 package cn.ubox.usbPermission;
 //import android.hardware.usb.UsbDevice;
 /**
  * Created by shenran on 2015/4/27.
  */
interface PermissionRequest {
    boolean RequestUsbPermission(int VendorId,int ProductId , int uid);
    boolean testBind();
    }
