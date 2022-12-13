
# BarcodeScanning：

高仿微信扫一扫，Android端极速二维码/条形码识别。
使用CameraX + MLKit机器学习套件实现。

 - [x]  支持一屏多码识别
 - [x]  支持双击及手势缩放
 - [x]  支持弱光环境监测提示开启闪光灯
 - [x]  支持相册扫码
 - [x]  CameraX自带生命周期管理
 - [x]  Android5.0及以上

#  效果

![效果](./images/1.jpg)![效果2](./images/2.jpg)
![效果3](./images/3.jpg)![效果4](./images/4.jpg)


[体验APK下载](./app-debug.apk)

#  如何使用

# 1、在根目录 build.gradle 添加:

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

# 2、在app项目下的build.gradle中添加：

```
dependencies {
	        implementation 'com.github.mtjsoft:CameraXBarcodeScanning:1.3.0'
	}
```

[![](https://jitpack.io/v/mtjsoft/CameraXBarcodeScanning.svg)](https://jitpack.io/#mtjsoft/CameraXBarcodeScanning)


# 3、使用前申请相关权限
```
Manifest.permission.CAMERA,
Manifest.permission.READ_EXTERNAL_STORAGE,
Manifest.permission.WRITE_EXTERNAL_STORAGE
```
# 4、扫一扫
```

    /**
     * 开始扫描
     */
    private fun startScann() {
        ScanningManager.instance.openScanningActivity(
            this,
            Config(
                true,
                ScanType.QR_CODE,
                object : AlbumOnClickListener {
                    override fun onClick(v: View, callBack: CallBackFileUri) {
                        // 1、检查申请必要的权限

                        // 2、相册选完图片，回调uri进行识别
                        pictureSelector(callBack)
                    }
                },
                object : ScanResultListener {
                    override fun onSuccessListener(value: String?) {
                        resultView.text = "扫码结果： \n$value"
                    }

                    override fun onFailureListener(error: String) {
                    }

                    override fun onCompleteListener(value: String?) {
                    }
                })
        )
    }


 # Config 参数说明

Config(
    /**
     * 是否开启多码识别
     * 不开启时，取最大范围的
     */
    val enabled: Boolean = true,
    /**
     * 默认扫码类型
     * 二维码 / 条形码
     */
    @ScanType
    val scanType: Int = ScanType.QR_CODE,

    /**
     * 相册点击回调
     */
    var albumOnClickListener: AlbumOnClickListener? = null,
    /**
     * 扫码回调
     */
    val scanResultListener: ScanResultListener? = null
)

```
# 5、版本说明

V1.3.0
--------------------------
- 相册按钮的处理使用回调交由App自定义处理。SDK不再内置相册处理

V1.2.0
--------------------------
- 相册选择图片处理

V1.1.0
--------------------------

- 相册扫码的图片加载引擎外置。详见[GlideEngine](./app/src/main/java/cn/mtjsoft/cameraxbarcodescanning/GlideEngine.java)