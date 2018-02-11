[![Release](https://jitpack.io/v/adityaarora1/LiveEdgeDetection.svg)](https://jitpack.io/#adityaarora1/LiveEdgeDetection)

# LiveEdgeDetection

LiveEdgeDetection is an Android document detection library built on top of openCV. It scans documents from camera live mode and allows you to adjust crop using the selected 4 edges and performs perspective transformation of the cropped image.

It works best in a dark background.

# Docs

[JavaDoc](https://adityaarora1.github.io/LiveEdgeDetection/docs)

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
To use it make sure to add the below inside project build.gradle file (root gradle file)

```
allprojects {
    repositories {
        mavenCentral()
        maven { url "https://jitpack.io" }
    }
}
```

and the repository's url is added to the build.gradle file in your app.

```
dependencies {
   compile 'com.github.adityaarora1:LiveEdgeDetection:1.0.0'
   
    // Other dependencies your app might use
}
```

# License
```
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
