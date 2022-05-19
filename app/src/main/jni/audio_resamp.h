//
// Created by 任晓明 on 2019-10-08.
//

#ifndef FFMPEG_BUILD_AUDIO_RESAMP_H
#define FFMPEG_BUILD_AUDIO_RESAMP_H

int resamplingFrame(AVCodecContext * audio_dec_ctx,AVFrame * pAudioDecodeFrame,
					int out_sample_fmt,int out_channels ,int out_sample_rate , uint8_t * out_buf);


#endif //FFMPEG_BUILD_AUDIO_RESAMP_H
