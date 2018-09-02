# Open CV Face Detection And Recognisition in Java FX

Thanks to [https://github.com/opencv-java/face-detection](https://github.com/opencv-java/face-detection) github repo 

## Install OpenCV 4.x under Linux mint 19
* Install following package
```
sudo apt install build-essential cmake cmake-gui git libgtk2.0-dev pkg-config libavcodec-dev libavformat-dev libswscale-dev

```
* Also download and extract `Apache Ant executable`

* After that
```
cd ~/<my_working_directory>
git clone https://github.com/opencv/opencv.git
git clone https://github.com/opencv/opencv_contrib.git
```

* create a temporary directory, which we denote as `<cmake_build_dir>`, where you want to put the generated Makefiles.

* Open cmake gui, Put the location of the OpenCV Where the source code is (e.g., /opencv/) and insert the destination directory of your build in the Where to build the binaries field. 

* Click `configure` and use the default compilers for `Unix Makefiles`.

* In the BUILD group, unselect:
```
 BUILD_PERF_TESTS
 BUILD_SHARED_LIBRARY to make the Java bindings dynamic
 library all-sufficient
 BUILD_TESTS
 BUILD_opencv_python

```
* In `Ungrouped Enteries`
```
ANT_EXECUTABLE <PATH_TO_ANT_IN_bin_directory>

```

* In the JAVA group
```
JAVA_AWT_INCLUDE_PATH <PATH_TO_JDK/Include/_DIRECTORY>
JAVA_AWT_LIBARY <PATH_TO_JDK/Include/jawt.h_file>
JAVA_INCLUDE_PATH <PATH_TO_JDK/Include/_DIRECTORY>
JAVA_INCLUDE_PATH2 <PATH_TO_JDK/Include/Linux_DIRECTORY>
JAVA_JVM_LIBARY <PATH_TO_JDK/Include/jni.h_file>

```
(if you dont find `JAVA` group then you can add them manually by clicking `Add Entry` button. if done manually just uncheck and check `Grouped` checkbox, so that it groups under `JAVA` )

* In `OPENCV` group
```
OPENCV_EXTRA_MODULES_PATH <PATH_TO_opencv_contrib_Modules_directory>
```
* Un-check Python (search “Python”) related check boxes under “BUILD” and “INSTALL” groups as we don’t need Python builds.

* Disable `WITH_MSMF` and `WITH_IPP & WITH_TBB`. These libs are only available for VS.

* Click Configure twice, every group highlighted with red  should appear with a white background.

* Click Generate.

* Once done, Navigate to `<cmake_build_dir>` directory  and issue following command  `make -j<nos_of_thread>` (eg `make -j4` will start compiling with 4 threads to speed up build) 

* If you want to install it system wide then just issue following command `make install` 

## Running project 

It requires following files
* `haarcascade_frontalface_alt.xml` or `lbpcascade_frontalface.xml`
 