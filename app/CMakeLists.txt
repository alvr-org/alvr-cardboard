cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY BOTH)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17 -g")
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS} -O3 -std=c++17 -g")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS} -O3 -std=c++17 -g")
set(ANDROID_STL c++_static)

set(libs_dir ${CMAKE_CURRENT_SOURCE_DIR}/libraries)
set(alvr_build_dir ${CMAKE_CURRENT_SOURCE_DIR}/../ALVR/build/alvr_client_core)

add_library(native_lib SHARED src/main/cpp/cpp_main.cpp)

include_directories(${alvr_build_dir})
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../cardboard)

target_link_libraries(
        native_lib
        android
        GLESv3
        EGL
        log
        ${libs_dir}/jni/${ANDROID_ABI}/libGfxPluginCardboard.so
        ${alvr_build_dir}/${ANDROID_ABI}/libalvr_client_core.so
        )
