[![Release](https://jitpack.io/v/hannesa2/LiveEdgeDetection.svg)](https://jitpack.io/#hannesa2/LiveEdgeDetection)

# LiveEdgeDetection

LiveEdgeDetection is an Android document detection library built on top of OpenCV. It scans documents from camera live mode and allows you to adjust crop using the detected 4 edges and performs perspective transformation of the cropped image.

**It works best with a dark background.**

# Screenshots

![Use darker bg](https://github.com/hannesa2/LiveEdgeDetection/blob/master/use_darker_bg.png)
![Move closer](https://github.com/hannesa2/LiveEdgeDetection/blob/master/move_closer.png)
![Move away](https://github.com/hannesa2/LiveEdgeDetection/blob/master/move_away.png)
![Adjust angle](https://github.com/hannesa2/LiveEdgeDetection/blob/master/adjust_angle.png)
![Hold still](https://github.com/hannesa2/LiveEdgeDetection/blob/master/hold_still.png)
![Adjust crop](https://github.com/hannesa2/LiveEdgeDetection/blob/master/adjust_crop.png)
![Result](https://github.com/hannesa2/LiveEdgeDetection/blob/master/cropped.png)

# Integrating into your project
This library is available in [JitPack.io](https://jitpack.io/) repository.
To use it, make sure to add the below inside root build.gradle file

```
allprojects {
    repositories {
        mavenCentral()
        maven { url "https://jitpack.io" }
    }
}
```

and add the repository's url to the app's build.gradle file.

```
dependencies {
   implementation 'com.github.hannesa2:LiveEdgeDetection:1.0.6.7'
}
```
# Usage
Out of the box it uses OpenCV.

1. Start **startActivityForResult** from your activity
```
startActivityForResult(new Intent(this, ScanActivity.class), REQUEST_CODE);
```
2. Get a file path for cropped image on **onActivityResult**
```
String filePath = data.getExtras().getString(ScanConstants.SCANNED_RESULT);
Bitmap baseBitmap = ScanUtils.decodeBitmapFromFile(filePath, ScanConstants.IMAGE_NAME);
```
3. Display the image using **TouchImageView**
```
<info.hannes.liveedgedetection.view.TouchImageView
        android:id="@+id/scanned_image"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="center" />
```

# License
```
Copyright 2018 hannesa2.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

