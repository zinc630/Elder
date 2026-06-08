import { fetchFamilyVideoCall, updateFamilyVideoSignal } from "../api/family";

const ICE_SERVERS: RTCIceServer[] = [{ urls: "stun:stun.l.google.com:19302" }];

function waitIceGatheringComplete(pc: RTCPeerConnection): Promise<void> {
  if (pc.iceGatheringState === "complete") return Promise.resolve();
  return new Promise((resolve) => {
    const check = () => {
      if (pc.iceGatheringState === "complete") {
        pc.removeEventListener("icegatheringstatechange", check);
        resolve();
      }
    };
    pc.addEventListener("icegatheringstatechange", check);
    window.setTimeout(() => {
      pc.removeEventListener("icegatheringstatechange", check);
      resolve();
    }, 4000);
  });
}

function parseSdp(json: string | null | undefined): RTCSessionDescriptionInit | null {
  if (!json) return null;
  try {
    return JSON.parse(json) as RTCSessionDescriptionInit;
  } catch {
    return null;
  }
}

async function sleep(ms: number) {
  await new Promise((r) => window.setTimeout(r, ms));
}

export type FamilyRtcRole = "caller" | "callee";

/** 单通视频 WebRTC（信令走 family_video_call 表） */
export class FamilyVideoRtc {
  private pc: RTCPeerConnection | null = null;
  private stopped = false;
  private pollTimer: number | undefined;

  constructor(
    private readonly elderId: string,
    private readonly callId: string,
    private readonly role: FamilyRtcRole,
    private readonly onRemoteStream: (stream: MediaStream) => void
  ) {}

  async start(localStream: MediaStream) {
    this.stopped = false;
    this.pc = new RTCPeerConnection({ iceServers: ICE_SERVERS });
    localStream.getTracks().forEach((t) => this.pc!.addTrack(t, localStream));
    this.pc.ontrack = (ev) => {
      const stream = ev.streams[0];
      if (stream) this.onRemoteStream(stream);
    };

    if (this.role === "caller") {
      const offer = await this.pc.createOffer();
      await this.pc.setLocalDescription(offer);
      await waitIceGatheringComplete(this.pc);
      await updateFamilyVideoSignal(this.elderId, this.callId, {
        offerSdp: JSON.stringify(this.pc.localDescription)
      });
      this.startAnswerPoll();
    } else {
      await this.waitForOffer();
      const answer = await this.pc.createAnswer();
      await this.pc.setLocalDescription(answer);
      await waitIceGatheringComplete(this.pc);
      await updateFamilyVideoSignal(this.elderId, this.callId, {
        answerSdp: JSON.stringify(this.pc.localDescription)
      });
    }
  }

  private startAnswerPoll() {
    this.pollTimer = window.setInterval(() => void this.tryApplyAnswer(), 600);
    void this.tryApplyAnswer();
  }

  private async tryApplyAnswer() {
    if (this.stopped || !this.pc) return;
    if (this.pc.currentRemoteDescription) return;
    const dto = await fetchFamilyVideoCall(this.elderId);
    if (!dto || dto.callId !== this.callId) return;
    const answer = parseSdp(dto.answerSdp);
    if (!answer) return;
    await this.pc.setRemoteDescription(answer);
    if (this.pollTimer !== undefined) {
      window.clearInterval(this.pollTimer);
      this.pollTimer = undefined;
    }
  }

  private async waitForOffer() {
    for (let i = 0; i < 40 && !this.stopped; i++) {
      const dto = await fetchFamilyVideoCall(this.elderId);
      if (dto?.callId === this.callId) {
        const offer = parseSdp(dto.offerSdp);
        if (offer && this.pc) {
          await this.pc.setRemoteDescription(offer);
          return;
        }
      }
      await sleep(500);
    }
    throw new Error("等待对方视频信令超时");
  }

  stop() {
    this.stopped = true;
    if (this.pollTimer !== undefined) {
      window.clearInterval(this.pollTimer);
      this.pollTimer = undefined;
    }
    this.pc?.close();
    this.pc = null;
  }
}

export async function openLocalCamera(): Promise<MediaStream> {
  if (!navigator.mediaDevices?.getUserMedia) {
    throw new Error("当前浏览器不支持摄像头");
  }
  return navigator.mediaDevices.getUserMedia({ video: true, audio: true });
}

export function bindStreamToVideo(el: HTMLVideoElement | null, stream: MediaStream | null) {
  if (!el) return;
  el.srcObject = stream;
  void el.play().catch(() => undefined);
}

export function applyStreamTrackState(stream: MediaStream | null, muted: boolean, cameraOn: boolean) {
  if (!stream) return;
  const audio = stream.getAudioTracks()[0];
  const video = stream.getVideoTracks()[0];
  if (audio) audio.enabled = !muted;
  if (video) video.enabled = cameraOn;
}
