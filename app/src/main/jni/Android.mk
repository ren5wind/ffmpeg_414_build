LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := ffmpeg
LOCAL_SRC_FILES := libffmpeg.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := ffmpeginvoke
LOCAL_SRC_FILES := com_huolient_star_commen_ffmpeg_FFmpegHelper.c ffmpeg.c ffmpeg_opt.c cmdutils.c ffmpeg_filter.c ffmpeg_hw.c

LOCAL_C_INCLUDES := /Users/rxm/AndroidStudioProjects/FFmpeg-n4.1.4

LOCAL_LDLIBS := -llog -lz -ldl
LOCAL_SHARED_LIBRARIES := ffmpeg

include $(BUILD_SHARED_LIBRARY)