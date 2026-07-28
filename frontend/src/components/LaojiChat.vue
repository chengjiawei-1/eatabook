<template>
  <div class="laoji-container">
    <Transition name="scale-fade">
      <div v-if="isCollapsed" class="mini-ball" @click="isCollapsed = false">
        <div class="ball-inner"></div>
        <div class="ball-pulse"></div>
      </div>
    </Transition>

    <Transition name="scale-fade">
      <div v-if="!isCollapsed" class="laoji-content-wrapper">
        <div class="avatar-group" @click="toggleChat">
          <div v-if="!showChat" class="classic-hover-tip">老己啊，在这呢。</div>
          
          <img :src="ipImageUrl" alt="老己" :class="['laoji-ip', { 'is-active': showChat }]" />
          
          <div class="collapse-x" @click.stop="isCollapsed = true" title="收起">×</div>
        </div>

        <Transition name="fade-up">
          <div v-if="showChat" class="chat-window">
            <div class="chat-header">
              <div class="header-title">
                <span class="classic-dot"></span>
                老己的安全屋
              </div>
              <div class="header-ops">
                <button class="icon-btn" title="刷新对话" @click="resetChat">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                    <path d="M17.65 6.35A7.958 7.958 0 0012 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
                  </svg>
                </button>
                <button
                  class="icon-btn"
                  :class="{ active: autoSpeakEnabled }"
                  :title="autoSpeakEnabled ? '关闭自动朗读' : '开启自动朗读'"
                  @click="toggleAutoSpeak"
                >
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                    <path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1-3.29-2.5-4.03v8.05A4.47 4.47 0 0016.5 12zm-2.5-8.45v2.06c2.89.86 5 3.54 5 6.39s-2.11 5.53-5 6.39v2.06c4.01-.91 7-4.49 7-8.45s-2.99-7.54-7-8.45z"/>
                  </svg>
                </button>
                <button class="close-btn" @click="showChat = false">×</button>
              </div>
            </div>
            
            <div class="chat-messages" ref="msgContainer">
              <div v-for="(msg, index) in messages" :key="index" :class="['msg-wrapper', msg.role]">
                <div class="msg-content">{{ displayMessageText(msg) }}</div>
                <button
                  v-if="msg.role === 'assistant'"
                  class="speak-msg-btn"
                  :class="{ active: speakingMessageIndex === index }"
                  :title="speakingMessageIndex === index ? '停止朗读' : '朗读这条回复'"
                  @click="toggleMessageSpeech(msg.text, index)"
                >
                  <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor">
                    <path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1-3.29-2.5-4.03v8.05A4.47 4.47 0 0016.5 12z"/>
                  </svg>
                </button>
              </div>
              
              <div v-if="isTyping" class="msg-wrapper assistant">
                <div class="msg-content typing">
                  {{ cleanAssistantText(currentStreamingText) || requestStatusText }}
                  <span class="cursor">|</span>
                </div>
              </div>
            </div>

            <div v-if="voiceStatusText" class="voice-status">
              {{ voiceStatusText }}
            </div>

            <div v-if="showMicDevicePicker && audioInputDevices.length > 1" class="mic-device-row">
              <select
                v-model="selectedAudioInputDeviceId"
                :disabled="isListening || isRecognizingVoice"
                @focus="refreshAudioInputDevices"
              >
                <option value="">系统默认麦克风</option>
                <option
                  v-for="device in audioInputDevices"
                  :key="device.deviceId"
                  :value="device.deviceId"
                >
                  {{ device.label }}
                </option>
              </select>
            </div>

            <div class="chat-input-area">
              <input 
                v-model="userInput" 
                @keyup.enter="handleSend" 
                :placeholder="isListening ? '老己在听，你慢慢说...' : '和老己说说话...'"
                :disabled="isTyping || isRecognizingVoice"
              />
              <button
                class="voice-btn"
                :class="{ listening: isListening }"
                @click="toggleVoiceInput"
                :disabled="isTyping || isRecognizingVoice"
                :title="isListening ? '停止语音输入' : '语音提问'"
              >
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                  <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.49 6-3.31 6-6.72h-1.7z"/>
                </svg>
              </button>
              <button @click="handleSend" :disabled="!userInput.trim() || isTyping">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                  <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"></path>
                </svg>
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onUnmounted } from 'vue';
import { chatStream } from '@/api/ai';
import { recognizeSpeech } from '@/api/speech';

const ipImageUrl = new URL('@/assets/images/argentip.png', import.meta.url).href;

const showChat = ref(false);
const isCollapsed = ref(false);
const userInput = ref('');
const isTyping = ref(false);
const currentStreamingText = ref('');
const requestStatusText = ref('');
const voiceStatusText = ref('');
const isListening = ref(false);
const isRecognizingVoice = ref(false);
const autoSpeakEnabled = ref(false);
const speakingMessageIndex = ref<number | null>(null);
const showMicDevicePicker = ref(false);
const msgContainer = ref<HTMLElement | null>(null);
const audioInputDevices = ref<Array<{ deviceId: string; label: string }>>([]);
const selectedAudioInputDeviceId = ref('');
const activeAudioTrackLabel = ref('');
let mediaStream: MediaStream | null = null;
let audioContext: AudioContext | null = null;
let audioSource: MediaStreamAudioSourceNode | null = null;
let audioProcessor: ScriptProcessorNode | null = null;
let audioSilenceGain: GainNode | null = null;
let recordedAudioChunks: Float32Array[] = [];
let recordingSampleRate = 48000;
let speechStopRequested = false;
let speechInputHintTimer: ReturnType<typeof setTimeout> | null = null;
let recordingStopTimer: ReturnType<typeof setTimeout> | null = null;
let voiceUiResetTimer: ReturnType<typeof setTimeout> | null = null;
const TARGET_SAMPLE_RATE = 16000;
const MAX_RECORDING_MILLIS = 60000;
const MIN_PCM_BYTES = 3200;

const defaultMessage = { role: 'assistant', text: '老己啊，看你气色一般，是不是又在外面“负重行”了？来进屋坐，咱翻翻古籍，喝碗热汤。' };
const messages = ref([{ ...defaultMessage }]);

const toggleChat = () => {
  showChat.value = !showChat.value;
  if (showChat.value) scrollToBottom();
};

const resetChat = () => {
  stopVoiceInput();
  stopSpeech();
  isTyping.value = false;
  currentStreamingText.value = '';
  requestStatusText.value = '';
  voiceStatusText.value = '';
  showMicDevicePicker.value = false;
  messages.value = [{ role: 'assistant', text: '老己啊，刚才那是翻篇了。咱重新唠唠，你想问点啥？' }];
};

const cleanAssistantText = (text: string) => {
  return text
    .replace(/\*\*([^*]+)\*\*/g, '$1')
    .replace(/^\s*-{3,}\s*$/gm, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim();
};

const displayMessageText = (msg: { role: string; text: string }) => {
  return msg.role === 'assistant' ? cleanAssistantText(msg.text) : msg.text;
};

const speechText = (text: string) => {
  return cleanAssistantText(text)
    .replace(/\s+/g, ' ')
    .trim();
};

const stopSpeech = () => {
  speechStopRequested = true;
  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel();
  }
  speakingMessageIndex.value = null;
};

const speakAssistantText = (text: string, index: number | null = null) => {
  if (!('speechSynthesis' in window)) {
    voiceStatusText.value = '当前浏览器不支持语音播报。';
    return;
  }
  const content = speechText(text);
  if (!content) return;

  stopSpeech();
  speechStopRequested = false;
  const utterance = new SpeechSynthesisUtterance(content);
  utterance.lang = 'zh-CN';
  utterance.rate = 0.95;
  utterance.pitch = 1;
  utterance.onend = () => {
    speakingMessageIndex.value = null;
  };
  utterance.onerror = (event) => {
    speakingMessageIndex.value = null;
    if (speechStopRequested || event.error === 'canceled' || event.error === 'interrupted') {
      return;
    }
    voiceStatusText.value = '语音播报失败，请检查浏览器权限或系统语音设置。';
  };
  speakingMessageIndex.value = index;
  window.speechSynthesis.speak(utterance);
};

const toggleMessageSpeech = (text: string, index: number) => {
  if (speakingMessageIndex.value === index) {
    stopSpeech();
    return;
  }
  speakAssistantText(text, index);
};

const toggleAutoSpeak = () => {
  autoSpeakEnabled.value = !autoSpeakEnabled.value;
  if (!autoSpeakEnabled.value) {
    stopSpeech();
  }
};

const clearVoiceUiResetTimer = () => {
  if (voiceUiResetTimer) {
    clearTimeout(voiceUiResetTimer);
    voiceUiResetTimer = null;
  }
};

const setVoiceStatus = (text: string, autoHideMs = 0) => {
  clearVoiceUiResetTimer();
  voiceStatusText.value = text;
  if (autoHideMs > 0) {
    voiceUiResetTimer = setTimeout(() => {
      voiceStatusText.value = '';
      showMicDevicePicker.value = false;
      voiceUiResetTimer = null;
    }, autoHideMs);
  }
};

const canUseSpeechInput = () => {
  if (!window.isSecureContext) {
    setVoiceStatus('语音输入需要 HTTPS 或 localhost 环境；如果用局域网 IP 打开页面，浏览器会禁止麦克风。', 8000);
    return false;
  }
  if (!navigator.mediaDevices?.getUserMedia) {
    setVoiceStatus('当前浏览器不支持麦克风录音，请使用新版 Chrome 或 Edge。', 8000);
    return false;
  }
  return true;
};

const refreshAudioInputDevices = async () => {
  if (!navigator.mediaDevices?.enumerateDevices) return;
  try {
    const devices = await navigator.mediaDevices.enumerateDevices();
    audioInputDevices.value = devices
      .filter((device) => device.kind === 'audioinput')
      .map((device, index) => ({
        deviceId: device.deviceId,
        label: device.label || `麦克风 ${index + 1}`
      }));
  } catch (error) {
    console.warn('Failed to enumerate audio input devices', error);
  }
};

const clearSpeechInputHintTimer = () => {
  if (speechInputHintTimer) {
    clearTimeout(speechInputHintTimer);
    speechInputHintTimer = null;
  }
};

const clearRecordingStopTimer = () => {
  if (recordingStopTimer) {
    clearTimeout(recordingStopTimer);
    recordingStopTimer = null;
  }
};

const cleanupAudioRecorder = () => {
  clearSpeechInputHintTimer();
  clearRecordingStopTimer();
  if (audioProcessor) {
    audioProcessor.onaudioprocess = null;
    audioProcessor.disconnect();
    audioProcessor = null;
  }
  if (audioSilenceGain) {
    audioSilenceGain.disconnect();
    audioSilenceGain = null;
  }
  if (audioSource) {
    audioSource.disconnect();
    audioSource = null;
  }
  if (mediaStream) {
    mediaStream.getTracks().forEach((track) => track.stop());
    mediaStream = null;
  }
  if (audioContext && audioContext.state !== 'closed') {
    audioContext.close().catch(() => undefined);
  }
  audioContext = null;
  activeAudioTrackLabel.value = '';
  isListening.value = false;
};

const flattenAudioChunks = (chunks: Float32Array[]) => {
  const length = chunks.reduce((total, chunk) => total + chunk.length, 0);
  const samples = new Float32Array(length);
  let offset = 0;
  chunks.forEach((chunk) => {
    samples.set(chunk, offset);
    offset += chunk.length;
  });
  return samples;
};

const resampleAudio = (samples: Float32Array, sourceRate: number, targetRate: number) => {
  if (sourceRate === targetRate) return samples;
  const ratio = sourceRate / targetRate;
  const targetLength = Math.max(1, Math.round(samples.length / ratio));
  const result = new Float32Array(targetLength);

  for (let i = 0; i < targetLength; i += 1) {
    const sourceIndex = i * ratio;
    const left = Math.floor(sourceIndex);
    const right = Math.min(left + 1, samples.length - 1);
    const weight = sourceIndex - left;
    result[i] = samples[left] * (1 - weight) + samples[right] * weight;
  }
  return result;
};

const encodePcm16 = (samples: Float32Array) => {
  const buffer = new ArrayBuffer(samples.length * 2);
  const view = new DataView(buffer);
  samples.forEach((sample, index) => {
    const clamped = Math.max(-1, Math.min(1, sample));
    const value = clamped < 0 ? clamped * 0x8000 : clamped * 0x7fff;
    view.setInt16(index * 2, value, true);
  });
  return new Blob([buffer], { type: 'application/octet-stream' });
};

const buildRecordedPcm = () => {
  const samples = flattenAudioChunks(recordedAudioChunks);
  const resampled = resampleAudio(samples, recordingSampleRate, TARGET_SAMPLE_RATE);
  return encodePcm16(resampled);
};

const getAudioLevel = (samples: Float32Array) => {
  if (!samples.length) return { peak: 0, rms: 0 };
  let sum = 0;
  let peak = 0;
  samples.forEach((sample) => {
    const abs = Math.abs(sample);
    peak = Math.max(peak, abs);
    sum += sample * sample;
  });
  return {
    peak,
    rms: Math.sqrt(sum / samples.length)
  };
};

const recognizeRecordedVoice = async () => {
  const samples = flattenAudioChunks(recordedAudioChunks);
  const level = getAudioLevel(samples);
  const resampled = resampleAudio(samples, recordingSampleRate, TARGET_SAMPLE_RATE);
  const pcm = encodePcm16(resampled);
  recordedAudioChunks = [];
  console.info('Voice recording captured', {
    samples: samples.length,
    sourceSampleRate: recordingSampleRate,
    pcmBytes: pcm.size,
    peak: Number(level.peak.toFixed(6)),
    rms: Number(level.rms.toFixed(6))
  });

  if (pcm.size < MIN_PCM_BYTES) {
    setVoiceStatus('录音太短，没有检测到清晰语音。', 4000);
    return;
  }
  if (level.peak === 0 && level.rms === 0) {
    const deviceName = activeAudioTrackLabel.value ? `“${activeAudioTrackLabel.value}”` : '当前麦克风';
    showMicDevicePicker.value = audioInputDevices.value.length > 1;
    setVoiceStatus(`${deviceName}没有输入声音，请切换麦克风或检查系统输入音量。`, 10000);
    return;
  }

  isRecognizingVoice.value = true;
  setVoiceStatus('正在使用科大讯飞识别...');
  try {
    const result = await recognizeSpeech(pcm);
    const text = result.text?.trim();
    if (!text) {
      setVoiceStatus('讯飞没有返回识别文字，请靠近麦克风再试。', 6000);
      return;
    }
    userInput.value = text;
    showMicDevicePicker.value = false;
    setVoiceStatus('语音已识别，正在发送...', 2000);
    await nextTick();
    await handleSend();
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    setVoiceStatus(`讯飞语音识别失败：${message}`, 8000);
  } finally {
    isRecognizingVoice.value = false;
  }
};

const startVoiceInput = async () => {
  if (!canUseSpeechInput()) return;

  try {
    recordedAudioChunks = [];
    userInput.value = '';
    await refreshAudioInputDevices();
    showMicDevicePicker.value = audioInputDevices.value.length > 1;
    const deviceId = selectedAudioInputDeviceId.value;
    mediaStream = await navigator.mediaDevices.getUserMedia({
      audio: {
        ...(deviceId ? { deviceId: { exact: deviceId } } : {}),
        channelCount: 1,
        sampleRate: TARGET_SAMPLE_RATE,
        sampleSize: 16,
        echoCancellation: false,
        noiseSuppression: false,
        autoGainControl: false
      }
    });
    await refreshAudioInputDevices();
    const [audioTrack] = mediaStream.getAudioTracks();
    activeAudioTrackLabel.value = audioTrack?.label || '';
    console.info('Microphone stream started', {
      label: audioTrack?.label,
      enabled: audioTrack?.enabled,
      muted: audioTrack?.muted,
      readyState: audioTrack?.readyState,
      settings: audioTrack?.getSettings()
    });
    if (audioTrack) {
      audioTrack.onmute = () => {
        console.warn('Microphone track muted', { label: audioTrack.label });
      };
      audioTrack.onunmute = () => {
        console.info('Microphone track unmuted', { label: audioTrack.label });
      };
      audioTrack.onended = () => {
        console.warn('Microphone track ended', { label: audioTrack.label });
      };
    }
    const AudioContextCtor = window.AudioContext || (window as any).webkitAudioContext;
    audioContext = new AudioContextCtor();
    if (audioContext.state === 'suspended') {
      await audioContext.resume();
    }
    recordingSampleRate = audioContext.sampleRate;
    audioSource = audioContext.createMediaStreamSource(mediaStream);
    audioProcessor = audioContext.createScriptProcessor(4096, 1, 1);
    audioSilenceGain = audioContext.createGain();
    audioSilenceGain.gain.value = 0;
    audioProcessor.onaudioprocess = (event) => {
      if (!isListening.value) return;
      recordedAudioChunks.push(new Float32Array(event.inputBuffer.getChannelData(0)));
    };
    audioSource.connect(audioProcessor);
    audioProcessor.connect(audioSilenceGain);
    audioSilenceGain.connect(audioContext.destination);

    isListening.value = true;
    setVoiceStatus('正在录音，再点一次麦克风结束并识别。');
    speechInputHintTimer = setTimeout(() => {
      if (isListening.value) {
        setVoiceStatus('仍在录音中，单次最多 60 秒。');
      }
    }, 8000);
    recordingStopTimer = setTimeout(() => {
      if (isListening.value) {
        stopVoiceInput(true);
      }
    }, MAX_RECORDING_MILLIS);
  } catch (error) {
    cleanupAudioRecorder();
    const detail = error instanceof Error ? error.message : String(error);
    showMicDevicePicker.value = audioInputDevices.value.length > 1;
    setVoiceStatus(`麦克风启动失败：${detail}`, 8000);
  }
};

const stopVoiceInput = (recognize = false) => {
  if (!isListening.value && !recognize) return;
  cleanupAudioRecorder();
  if (recognize) {
    void recognizeRecordedVoice();
  } else {
    recordedAudioChunks = [];
    showMicDevicePicker.value = false;
    setVoiceStatus('', 0);
  }
};

const toggleVoiceInput = () => {
  if (isListening.value) {
    stopVoiceInput(true);
    return;
  }
  void startVoiceInput();
};
const handleSend = async () => {
  if (!userInput.value.trim() || isTyping.value) return;
  if (isListening.value) {
    stopVoiceInput(false);
  }
  const prompt = userInput.value;
  messages.value.push({ role: 'user', text: prompt });
  userInput.value = '';
  isTyping.value = true;
  currentStreamingText.value = '';
  requestStatusText.value = '老己正在接话...';
  let finalized = false;

  const finishRequest = (message?: string) => {
    if (finalized) return;
    finalized = true;

    const text = (message ?? currentStreamingText.value).trim();
    const assistantText = text || '这次请求已经结束，但后端没有返回内容。请检查后端日志或稍后再试。';
    messages.value.push({
      role: 'assistant',
      text: assistantText
    });
    const assistantIndex = messages.value.length - 1;
    currentStreamingText.value = '';
    requestStatusText.value = '';
    isTyping.value = false;
    scrollToBottom();
    if (autoSpeakEnabled.value) {
      speakAssistantText(assistantText, assistantIndex);
    }
  };

  try {
    console.info('[LaojiChat] stream request started', { prompt });
    await chatStream({ agent: 'laoji', prompt }, (chunk) => {
      requestStatusText.value = '';
      currentStreamingText.value += chunk;
      scrollToBottom();
    }, () => {
      console.info('[LaojiChat] stream request finished', {
        chars: currentStreamingText.value.length
      });
      finishRequest();
    });
  } catch (err) {
    const detail = err instanceof Error ? err.message : String(err);
    console.error('[LaojiChat] stream request failed', err);
    finishRequest(`哎呀老己，我这会儿断片了。错误信息：${detail}`);
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight;
  });
};

onUnmounted(() => {
  stopVoiceInput();
  stopSpeech();
  clearVoiceUiResetTimer();
});
</script>

<style scoped>
/* 容器：固定宽高，作为所有组件的对齐基准点 */
.laoji-container {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 9999;
  font-family: "Microsoft YaHei", sans-serif;
  /* 固定为头像宽度，确保切换圆圈时中心点不抖动 */
  width: 90px;
  height: 90px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 1. 收起后的圆点：强制在父容器中心 */
.mini-ball {
  position: absolute;
  width: 40px;
  height: 40px;
  background: #b86207;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(184, 98, 7, 0.3);
}

.ball-inner {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
}

.ball-pulse {
  position: absolute;
  width: 40px;
  height: 40px;
  border: 2px solid #b86207;
  border-radius: 50%;
  animation: pulse 2s infinite ease-out;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.8; }
  100% { transform: scale(1.6); opacity: 0; }
}

/* 2. 正常状态布局：绝对定位撑满容器 */
.laoji-content-wrapper {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-group {
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 还原经典气泡样式：基于头像中心对齐 */
.classic-hover-tip {
  position: absolute;
  top: -45px;
  left: 50%;
  transform: translateX(-50%) translateY(10px);
  background: white;
  padding: 6px 14px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  font-size: 13px;
  color: #5d534a;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  transition: all 0.3s ease;
}

.avatar-group:hover .classic-hover-tip {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.laoji-ip {
  width: 90px;
  height: auto;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: float 4s infinite ease-in-out;
}

.laoji-ip.is-active { transform: scale(0.85) rotate(-5deg); }

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

/* 收起叉叉：定位在头像边缘 */
.collapse-x {
  position: absolute;
  top: 0;
  left: 0;
  width: 22px;
  height: 22px;
  background: rgba(93, 83, 74, 0.6);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-group:hover .collapse-x { opacity: 1; }

/* 3. 聊天窗样式 (回归原版) */
.chat-window {
  position: absolute;
  bottom: 110px;
  right: 0;
  width: 340px;
  height: 500px;
  background: #fdfaf5;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 15px 50px rgba(0,0,0,0.15);
  overflow: hidden;
  border: 1px solid #efe8d8;
  cursor: default; /* 防止点击窗口触发关闭 */
}

.chat-header {
  background: #efe8d8;
  padding: 12px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title { display: flex; align-items: center; gap: 8px; font-weight: bold; color: #5d534a; }
.classic-dot { width: 8px; height: 8px; background: #8e9e82; border-radius: 50%; }

.chat-messages { flex: 1; overflow-y: auto; padding: 20px; display: flex; flex-direction: column; gap: 15px; }
.msg-wrapper { max-width: 85%; display: flex; align-items: flex-end; gap: 6px; }
.msg-wrapper.user { align-self: flex-end; }
.msg-wrapper.assistant { align-self: flex-start; }

.msg-content { padding: 10px 14px; border-radius: 15px; font-size: 14px; line-height: 1.6; white-space: pre-wrap; word-break: break-word; }
.user .msg-content { background: #b86207; color: white; border-bottom-right-radius: 2px; }
.assistant .msg-content { background: white; color: #4a4a4a; border: 1px solid #eceae0; border-bottom-left-radius: 2px; }

.speak-msg-btn {
  flex: 0 0 auto;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1px solid #eceae0;
  background: #fff;
  color: #b86207;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0.72;
}

.speak-msg-btn:hover,
.speak-msg-btn.active {
  background: #b86207;
  color: #fff;
  opacity: 1;
}

.voice-status {
  padding: 6px 15px 0;
  background: #fff;
  color: #8a6d51;
  font-size: 12px;
}

.mic-device-row {
  padding: 8px 15px 0;
  background: #fff;
}

.mic-device-row select {
  width: 100%;
  height: 32px;
  border: 1px solid #e0ddd5;
  border-radius: 8px;
  background: #faf9f6;
  color: #5d534a;
  font-size: 12px;
  outline: none;
}

.chat-input-area { padding: 15px; background: white; border-top: 1px solid #f0ede5; display: flex; gap: 10px; }
.chat-input-area input { flex: 1; border: 1px solid #e0ddd5; padding: 10px 14px; border-radius: 10px; outline: none; background: #faf9f6; }
.chat-input-area button { background: #b86207; color: white; border: none; width: 40px; height: 40px; border-radius: 10px; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.chat-input-area button.voice-btn {
  background: #8a6d51;
}
.chat-input-area button.voice-btn.listening {
  background: #9f2d20;
  box-shadow: 0 0 0 4px rgba(159, 45, 32, 0.12);
}
.chat-input-area button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

/* 动画效果：统一缩放原点 */
.scale-fade-enter-active, .scale-fade-leave-active { 
  transition: all 0.3s ease; 
  position: absolute; /* 切换时重叠，防止布局撑开 */
}
.scale-fade-enter-from, .scale-fade-leave-to { opacity: 0; transform: scale(0); }

.fade-up-enter-active, .fade-up-leave-active { transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1); }
.fade-up-enter-from, .fade-up-leave-to { opacity: 0; transform: translateY(30px) scale(0.95); }

.cursor { animation: blink 1s infinite; color: #b86207; }
@keyframes blink { 50% { opacity: 0; } }

.icon-btn, .close-btn { background: transparent; border: none; color: #a89f94; cursor: pointer; }
.icon-btn.active { color: #b86207; }
.close-btn { font-size: 24px; }
</style>
