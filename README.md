[![Release](https://jitpack.io/v/adityaarora1/LiveEdgeDetection.svg)](https://jitpack.io/#adityaarora1/LiveEdgeDetection)

# LiveEdgeDetection

LiveEdgeDetection is an Android document detection library built on top of OpenCV. It scans documents from camera live mode and allows you to adjust crop using the detected 4 edges and performs perspective transformation of the cropped image.

**It works best with a dark background.**

# JavaDocs
You can browse the [JavaDocs for the latest release](https://adityaarora1.github.io/LiveEdgeDetection/docs)

# Download apk
Try the [sample app](https://drive.google.com/file/d/1sO26O4-1-2XAX16czREx7SiiCD8-4ecU/view?usp=sharing)

# Screenshots

![Use darker bg](https://github.com/adityaarora1/LiveEdgeDetection/blob/master/use_darker_bg.png)
![Move closer](https://github.com/adityaarora1/LiveEdgeDetection/blob/master/move_closer.png)
![Move away](https://github.com/adityaarora1/LiveEdgeDetection/blob/master/move_away.png)
![Adjust angle](https://github.com/adityaarora1/LiveEdgeDetection/blob/master/adjust_angle.png)
![Hold still](https://github.com/adityaarora1/LiveEdgeDetection/blob/master/hold_still.png)
![Adjust crop](https://github.com/adityaarora1/LiveEdgeDetection/blob/master/adjust_crop.png)
![Result](https://github.com/adityaarora1/LiveEdgeDetection/blob/master/cropped.png)

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
   compile 'com.github.adityaarora1:LiveEdgeDetection:1.0.6'
   
    // Other dependencies your app might use
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
<com.adityaarora.liveedgedetection.view.TouchImageView
        android:id="@+id/scanned_image"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="center" />
```

# Help
**Versioning policy**

We use [Semantic Versioning 2.0.0](https://semver.org/) as our versioning policy.

**Bugs, Feature requests**

Found a bug? Something that's missing? Feedback is an important part of improving the project, so please [open an issue](https://github.com/adityaarora1/LiveEdgeDetection/issues).

**Code**

Fork this project and start working on your own feature branch. When you're done, send a Pull Request to have your suggested changes merged into the master branch by the project's collaborators. Read more about the [GitHub flow](https://guides.github.com/introduction/flow/).

# License
```
Copyright 2018 Aditya Arora.

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

# Donation

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.me/adityaarora1)  [![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.me/adityaarora1)

![Paypal](https://github.com/adityaarora1/LiveEdgeDetection/blob/master/paypal_qr.gif)

